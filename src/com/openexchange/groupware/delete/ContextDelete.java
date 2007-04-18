package com.openexchange.groupware.delete;

public abstract class ContextDelete implements DeleteListener {
//	 I'll go to hell for this.
	protected boolean isContextDelete(DeleteEvent sqlDelEvent) {
		return sqlDelEvent.getType() == DeleteEvent.TYPE_USER && sqlDelEvent.getId() == sqlDelEvent.getContext().getMailadmin();
	}
}
