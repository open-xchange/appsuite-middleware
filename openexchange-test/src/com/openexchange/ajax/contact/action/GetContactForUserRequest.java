package com.openexchange.ajax.contact.action;

import java.io.IOException;
import java.util.TimeZone;

import org.json.JSONException;

import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.Params;

public class GetContactForUserRequest extends AbstractContactRequest<GetResponse> {

	private String id;
	private boolean failOnError;

	public GetContactForUserRequest(int id, boolean failOnError){
		this.id = String.valueOf(id);
		this.failOnError = failOnError;
	}
	
	public Method getMethod() {
		return Method.GET;
	}

	public Parameter[] getParameters() throws IOException, JSONException {
		return new Params(
			"action", "getuser", 
			"id",this.id)
		.toArray();
	}

	public GetParser getParser() {
		return new GetParser(failOnError, TimeZone.getDefault());
	}

	public Object getBody() throws IOException, JSONException {
		return null;
	}

}
