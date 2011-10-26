package com.openexchange.polling.json.actions;

import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.polling.PollService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

public abstract class PollAction implements AJAXActionService {

	private final ServiceLookup services;

	public PollAction(ServiceLookup services) {
		this.services = services;
	}

	@Override
    public AJAXRequestResult perform(AJAXRequestData requestData,
			ServerSession session) throws OXException {
		PollRequest req = new PollRequest(requestData, session);
		return perform(req);
	}

	protected abstract AJAXRequestResult perform(PollRequest req) throws OXException;

	protected PollService getPollService() {
		return services.getService(PollService.class);
	}
}
