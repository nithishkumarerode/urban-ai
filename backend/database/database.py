import os
from dotenv import load_dotenv
from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession
from sqlalchemy.orm import declarative_base, sessionmaker

load_dotenv()

DATABASE_URL = os.getenv(
    "DATABASE_URL",
    "sqlite+aiosqlite:///urbancadastral.db"
)

# Async engine for production FastAPI async endpoints
is_sqlite = DATABASE_URL.startswith("sqlite")
engine = create_async_engine(
    DATABASE_URL,
    echo=False,
    future=True,
    **({} if is_sqlite else {"pool_pre_ping": True})
)

AsyncSessionLocal = sessionmaker(
    bind=engine,
    class_=AsyncSession,
    expire_on_commit=False
)

Base = declarative_base()

async def get_db():
    async with AsyncSessionLocal() as session:
        try:
            yield session
        finally:
            await session.close()
