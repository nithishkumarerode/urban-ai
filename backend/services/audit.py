from typing import Optional, Dict, Any
from sqlalchemy.ext.asyncio import AsyncSession
from backend.database.models import AuditLog

async def log_audit_event(
    session: AsyncSession,
    action: str,
    user_id: Optional[int] = None,
    user_email: Optional[str] = None,
    entity_type: Optional[str] = None,
    entity_id: Optional[str] = None,
    details: Optional[Dict[str, Any]] = None,
    ip_address: Optional[str] = None
):
    """
    Records an immutable audit event for regulatory cadastral compliance.
    """
    log_entry = AuditLog(
        user_id=user_id,
        user_email=user_email,
        action=action,
        entity_type=entity_type,
        entity_id=entity_id,
        details=details,
        ip_address=ip_address
    )
    session.add(log_entry)
    await session.commit()
