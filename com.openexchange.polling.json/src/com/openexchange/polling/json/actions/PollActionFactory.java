package com.openexchange.polling.json.actions;

import java.util.HashMap;
import java.util.Map;

import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;

public class PollActionFactory implements AJAXActionServiceFactory {

	private final Map<String, PollAction> ACTIONS = new HashMap<String, PollAction>();

	public PollActionFactory(ServiceLookup services) {
		ACTIONS.put("new", new CreateAction(services));
		ACTIONS.put("get", new GetAction(services));
	}

	@Override
    public AJAXActionService createActionService(String action)
			throws OXException {

		return ACTIONS.get(action);
	}

}
