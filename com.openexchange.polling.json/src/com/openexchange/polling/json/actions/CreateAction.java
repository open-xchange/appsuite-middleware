package com.openexchange.polling.json.actions;

import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.polling.Poll;
import com.openexchange.polling.PollService;
import com.openexchange.server.ServiceLookup;

public class CreateAction extends PollAction {

	public CreateAction(ServiceLookup services) {
		super(services);
	}

	@Override
	protected AJAXRequestResult perform(PollRequest req) throws OXException {
		PollService polls = getPollService();

		int cid = req.getContextId();
		Poll poll = req.getPoll();

		polls.createPoll(poll, cid);

		return new AJAXRequestResult(poll, "poll");
	}


}
