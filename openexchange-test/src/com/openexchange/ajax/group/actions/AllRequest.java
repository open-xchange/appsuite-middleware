package com.openexchange.ajax.group.actions;

import java.io.IOException;

import org.json.JSONException;

import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.Params;
import com.openexchange.java.Strings;

public class AllRequest extends AbstractGroupRequest<AllResponse>{

	private boolean failOnError;
	private boolean loadMembers;

	public AllRequest(boolean loadMembers, boolean failOnError){
		this.failOnError = failOnError;
		this.loadMembers = loadMembers;
	}
	
	public Object getBody() throws IOException, JSONException {
		return null;
	}

	public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
		return Method.GET;
	}

	public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters()
			throws IOException, JSONException {
		return new Params(
				AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_ALL,
				AJAXServlet.PARAMETER_LOAD_MEMBERS, ""+loadMembers).toArray();
	}

	public AbstractAJAXParser<? extends AllResponse> getParser() {
		return new AllParser(this.failOnError);
	}

}
