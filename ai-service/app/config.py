"""应用配置:从环境变量 / .env 读取。"""
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", extra="ignore")

    # Mimo (OpenAI-compatible /chat/completions)
    mimo_api_key: str = "sk-replace-me"
    mimo_base_url: str = "https://api.xiaomimimo.com/v1"
    mimo_model: str = "mimo-v2.5"
    mimo_timeout: float = 60.0

    # Legacy DeepSeek env fallback. Remove after all deploy environments switch to MIMO_*.
    deepseek_api_key: str | None = None
    deepseek_base_url: str | None = None
    deepseek_model: str | None = None
    deepseek_timeout: float | None = None

    # 服务
    app_name: str = "health-ai-service"

    @property
    def llm_api_key(self) -> str:
        return self.mimo_api_key or self.deepseek_api_key or "sk-replace-me"

    @property
    def llm_base_url(self) -> str:
        return self.mimo_base_url or self.deepseek_base_url or "https://api.xiaomimimo.com/v1"

    @property
    def llm_model(self) -> str:
        return self.mimo_model or self.deepseek_model or "mimo-v2.5"

    @property
    def llm_timeout(self) -> float:
        return self.mimo_timeout or self.deepseek_timeout or 60.0


settings = Settings()
