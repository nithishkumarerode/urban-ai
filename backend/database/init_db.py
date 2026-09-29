import asyncio
import os
import bcrypt
from sqlalchemy import select
from .database import engine, Base, AsyncSessionLocal
from .models import User

TEST_USER_EMAIL = os.getenv("TEST_USER_EMAIL", "test@example.com")
TEST_USER_PASSWORD = os.getenv("TEST_USER_PASSWORD", "AdminCadastral2026!")
TEST_USER_NAME = os.getenv("TEST_USER_NAME", "Senior Cadastral Surveyor")

async def init_db():
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)

    async with AsyncSessionLocal() as session:
        # Check if seed user exists
        stmt = select(User).where(User.email == TEST_USER_EMAIL)
        result = await session.execute(stmt)
        user = result.scalars().first()

        if not user:
            salt = bcrypt.gensalt()
            hashed_pwd = bcrypt.hashpw(TEST_USER_PASSWORD.encode("utf-8"), salt).decode("utf-8")
            new_user = User(
                email=TEST_USER_EMAIL,
                hashed_password=hashed_pwd,
                full_name=TEST_USER_NAME,
                role="admin",
                is_active=True
            )
            session.add(new_user)
            await session.commit()
            print(f"[UrbanCadastral DB] Initialized test user: {TEST_USER_EMAIL}")
        else:
            print(f"[UrbanCadastral DB] Test user already exists: {TEST_USER_EMAIL}")

if __name__ == "__main__":
    asyncio.run(init_db())
