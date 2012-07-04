package com.openexchange.realtime.packet;

public abstract class Stanza {

	private ID to, from;

	private String namespace;

	private Payload payload;

	public ID getTo() {
		return to;
	}

	public void setTo(ID to) {
		this.to = to;
	}

	public ID getFrom() {
		return from;
	}

	public void setFrom(ID from) {
		this.from = from;
	}

	public void setPayload(Payload payload) {
		this.payload = payload;
	}

	public Payload getPayload() {
		return payload;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

}
