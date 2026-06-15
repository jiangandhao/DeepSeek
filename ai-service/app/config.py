"""应用配置:从环境变量 / .env 读取。"""
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", extra="ignore")

    # DeepSeek
    deepseek_api_key: str = "sk-replace-me"
    deepseek_base_url: str = "https://api.deepseek.com"
    deepseek_model: str = "deepseek-chat"
    deepseek_timeout: float = 60.0

    # 服务
    app_name: str = "health-ai-service"

    # 内部共享密钥:后端调用 AI 服务时须带 X-Internal-Key 头。
    # 为空时不强制校验(便于本地开发 / 单测);生产由 INTERNAL_API_KEY 注入。
    internal_api_key: str = ""


settings = Settings()
