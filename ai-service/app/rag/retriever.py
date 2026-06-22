"""持久化医学知识向量库。

使用稳定哈希嵌入把中英文医学文本映射到固定维度向量，并持久化到 SQLite。
该方案不下载外部 embedding 模型，容器可离线启动；后续可在不改变 retrieve
接口的前提下替换为 BGE-M3/Chroma 等生产级语义向量方案。
"""
import hashlib
import json
import os
import re
import sqlite3
from dataclasses import dataclass
from pathlib import Path

import numpy as np

KNOWLEDGE_DIR = Path(__file__).parent / "knowledge"
VECTOR_DB_PATH = Path(os.getenv("VECTOR_DB_PATH", "/app/chroma_db/health_vectors.db"))
if not Path("/app").exists():
    VECTOR_DB_PATH = Path(__file__).parents[2] / "data" / "health_vectors.db"
VECTOR_DIM = 384
CJK = re.compile(r"[\u4e00-\u9fff]")
ASCII_WORD = re.compile(r"[a-zA-Z][a-zA-Z0-9_.-]*")


@dataclass
class Chunk:
    source: str
    title: str
    text: str


def _tokenize(text: str) -> list[str]:
    chars = CJK.findall(text)
    tokens = chars + [chars[i] + chars[i + 1] for i in range(len(chars) - 1)]
    tokens.extend(word.lower() for word in ASCII_WORD.findall(text))
    return tokens


def _embed(text: str) -> np.ndarray:
    vector = np.zeros(VECTOR_DIM, dtype=np.float32)
    for token in _tokenize(text):
        digest = hashlib.blake2b(token.encode("utf-8"), digest_size=8).digest()
        value = int.from_bytes(digest, "little")
        index = value % VECTOR_DIM
        vector[index] += 1.0 if (value >> 8) & 1 else -1.0
    norm = float(np.linalg.norm(vector))
    return vector / norm if norm else vector


def _load_chunks() -> list[Chunk]:
    chunks: list[Chunk] = []
    for path in sorted(KNOWLEDGE_DIR.glob("*.md")):
        content = path.read_text(encoding="utf-8")
        for part in re.split(r"\n(?=## )", content):
            part = part.strip()
            if not part:
                continue
            title = part.splitlines()[0].lstrip("# ").strip()
            chunks.append(Chunk(path.stem, title, part))
    return chunks


class Retriever:
    def __init__(self) -> None:
        self.chunks = _load_chunks()
        self.vocab = {token for chunk in self.chunks for token in _tokenize(chunk.text)}
        self._rows: list[tuple[Chunk, np.ndarray]] = []
        self._initialize()

    def _connect(self) -> sqlite3.Connection:
        VECTOR_DB_PATH.parent.mkdir(parents=True, exist_ok=True)
        return sqlite3.connect(VECTOR_DB_PATH)

    def _initialize(self) -> None:
        fingerprint = hashlib.sha256("\n".join(c.text for c in self.chunks).encode()).hexdigest()
        with self._connect() as db:
            db.execute("CREATE TABLE IF NOT EXISTS meta (key TEXT PRIMARY KEY, value TEXT NOT NULL)")
            db.execute("""CREATE TABLE IF NOT EXISTS vectors (
                id INTEGER PRIMARY KEY, source TEXT NOT NULL, title TEXT NOT NULL,
                text TEXT NOT NULL, embedding TEXT NOT NULL)""")
            current = db.execute("SELECT value FROM meta WHERE key='fingerprint'").fetchone()
            if not current or current[0] != fingerprint:
                db.execute("DELETE FROM vectors")
                for idx, chunk in enumerate(self.chunks):
                    db.execute("INSERT INTO vectors VALUES (?, ?, ?, ?, ?)",
                               (idx, chunk.source, chunk.title, chunk.text,
                                json.dumps(_embed(chunk.text).tolist())))
                db.execute("INSERT OR REPLACE INTO meta VALUES ('fingerprint', ?)", (fingerprint,))
            rows = db.execute("SELECT source,title,text,embedding FROM vectors ORDER BY id").fetchall()
        self._rows = [(Chunk(row[0], row[1], row[2]),
                       np.asarray(json.loads(row[3]), dtype=np.float32)) for row in rows]

    def retrieve(self, query: str, k: int = 3) -> list[dict]:
        if not query.strip() or not self._rows:
            return []
        query_vector = _embed(query)
        if not np.any(query_vector):
            return []
        scored = [(float(np.dot(vector, query_vector)), chunk) for chunk, vector in self._rows]
        scored.sort(key=lambda item: item[0], reverse=True)
        return [{"source": chunk.source, "title": chunk.title, "text": chunk.text,
                 "score": round(score, 4)} for score, chunk in scored[:k] if score > 0]

    def status(self) -> dict:
        return {"engine": "sqlite-hash-vector", "path": str(VECTOR_DB_PATH),
                "dimension": VECTOR_DIM, "documents": len(self._rows)}


retriever = Retriever()
