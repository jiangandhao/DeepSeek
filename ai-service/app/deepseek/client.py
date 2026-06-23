"""Mimo API 客户端封装。

兼容 OpenAI 的 /chat/completions 协议。支持普通调用与 SSE 流式输出。
"""
import json
from typing import AsyncIterator, Optional

import httpx

from app.config import settings


class MimoClient:
    def __init__(self) -> None:
        self.base_url = settings.llm_base_url.rstrip("/")
        self.api_key = settings.llm_api_key
        self.model = settings.llm_model
        self.timeout = settings.llm_timeout

    @property
    def _headers(self) -> dict:
        return {
            "Authorization": f"Bearer {self.api_key}",
            "Content-Type": "application/json",
        }


    @staticmethod
    def _raise_for_status(resp: httpx.Response) -> None:
        if resp.is_error:
            detail = resp.text.strip()
            raise httpx.HTTPStatusError(
                f"Mimo API returned {resp.status_code}: {detail}",
                request=resp.request,
                response=resp,
            )

    async def chat(
        self,
        messages: list[dict],
        temperature: float = 0.7,
        tools: Optional[list[dict]] = None,
    ) -> dict:
        """非流式对话,返回完整的 choices[0].message。"""
        payload: dict = {
            "model": self.model,
            "messages": messages,
            "temperature": temperature,
            "stream": False,
        }
        if tools:
            payload["tools"] = tools
        async with httpx.AsyncClient(timeout=self.timeout) as client:
            resp = await client.post(
                f"{self.base_url}/chat/completions",
                headers=self._headers,
                json=payload,
            )
            self._raise_for_status(resp)
            data = resp.json()
            return data["choices"][0]["message"]

    async def chat_content(self, messages: list[dict], temperature: float = 0.7) -> str:
        """便捷方法:只取文本内容。"""
        msg = await self.chat(messages, temperature=temperature)
        return msg.get("content", "") or ""

    async def stream_chat(
        self,
        messages: list[dict],
        temperature: float = 0.7,
    ) -> AsyncIterator[str]:
        """流式对话,逐段 yield 文本增量(delta.content)。"""
        payload = {
            "model": self.model,
            "messages": messages,
            "temperature": temperature,
            "stream": True,
        }
        async with httpx.AsyncClient(timeout=self.timeout) as client:
            async with client.stream(
                "POST",
                f"{self.base_url}/chat/completions",
                headers=self._headers,
                json=payload,
            ) as resp:
                self._raise_for_status(resp)
                async for line in resp.aiter_lines():
                    if not line or not line.startswith("data:"):
                        continue
                    data = line[len("data:"):].strip()
                    if data == "[DONE]":
                        break
                    try:
                        chunk = json.loads(data)
                        delta = chunk["choices"][0]["delta"]
                        piece = delta.get("content")
                        if piece:
                            yield piece
                    except (json.JSONDecodeError, KeyError, IndexError):
                        continue


mimo_client = MimoClient()

# Backward-compatible alias used by existing routers and agents.
deepseek_client = mimo_client
