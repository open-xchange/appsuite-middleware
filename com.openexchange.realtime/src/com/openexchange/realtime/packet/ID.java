package com.openexchange.realtime.packet;

/**
 * An ID describes a valid sender or recipient of a {@link Stanza}. It consists of an optional channel, a mandatory user name and mandatory 
 * context name and an optional resource. 
 * TODO: Blabla....
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ID {
	
	private String protocol;
	private String user;
	private String context;
	private String resource;
	
	public ID(String id) {
		// TODO: Parse
		validate();
	}

	public ID(String protocol, String user, String context, String resource) {
		super();
		this.protocol = protocol;
		this.user = user;
		this.context = context;
		this.resource = resource;
		validate();
	}

	private void validate() throws IllegalArgumentException{
		if (user == null) {
			throw new IllegalArgumentException("User must not be null");
		}

		if (context == null) {
			throw new IllegalArgumentException("Context must not be null");
		}

	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
		validate();
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
		validate();

	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
		validate();
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
		validate();
	}
	
	
	
}
