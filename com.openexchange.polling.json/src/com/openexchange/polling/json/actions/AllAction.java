package com.openexchange.polling.json.actions;

import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.polling.PollService;

public class AllAction extends AbstractPollingAction {

	protected AllAction(PollingActionFactory factory) {
		super(factory);
	}

	@Override
	protected AJAXRequestResult perform(PollingRequest req)
			throws OXException {
		
		PollService pollService = factory.getPollService();
		
		return new AJAXRequestResult(pollService.getPolls(req.getContextId()), "poll");
	}

}
