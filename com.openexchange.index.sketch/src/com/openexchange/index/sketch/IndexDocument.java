package com.openexchange.index.sketch;

public interface IndexDocument {

	public static enum Type {
		MAIL,
		CONTACT,
		APPOINTMENT,
		TASK,
		INFOSTORE_DOCUMENT;
	}
	
	Type getType();

}
