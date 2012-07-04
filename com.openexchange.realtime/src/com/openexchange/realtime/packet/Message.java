package com.openexchange.realtime.packet;

public class Message extends Stanza {

	public static enum Type {
		normal, chat, groupchat, headline, error
	}

	private Type type;

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

}
