package com.openexchange.polling.json.actions;

import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.polling.Poll;
import com.openexchange.polling.json.PollParser;
import com.openexchange.tools.session.ServerSession;

public class PollRequest {

	private final ServerSession session;
	private final AJAXRequestData request;

	private static final PollParser PARSER = new PollParser();

	public PollRequest(final AJAXRequestData request, final ServerSession session) {
		this.session = session;
		this.request = request;
	}

	public int getContextId() {
		return session.getContextId();
	}

	public Poll getPoll() throws OXException {
		final JSONObject bodyObj = (JSONObject) request.getData();
		return PARSER.parse(bodyObj);
	}

	public int getId() throws OXException {
		return request.getParameter("id", int.class);
	}

}
