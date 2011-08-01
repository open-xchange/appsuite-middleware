package com.openexchange.polling.json.actions;

import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.polling.Poll;
import com.openexchange.polling.PollService;
import com.openexchange.server.ServiceLookup;

public class GetAction extends PollAction {

	public GetAction(ServiceLookup services) {
		super(services);
	}

	@Override
	protected AJAXRequestResult perform(PollRequest req) throws OXException {
		PollService polls = getPollService();

		int cid = req.getContextId();
		int id = req.getId();

		Poll poll = polls.getPoll(id, cid);
		return new AJAXRequestResult(poll, "poll");
	}

}
