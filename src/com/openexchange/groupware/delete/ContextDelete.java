package com.openexchange.groupware.delete;

public abstract class ContextDelete implements DeleteListener {
	// I'll go to hell for this.
	protected final boolean isContextDelete(final DeleteEvent deleteEvent) {
		return deleteEvent.getType() == DeleteEvent.TYPE_USER
				&& deleteEvent.getId() == deleteEvent.getContext().getMailadmin();
	}
}
