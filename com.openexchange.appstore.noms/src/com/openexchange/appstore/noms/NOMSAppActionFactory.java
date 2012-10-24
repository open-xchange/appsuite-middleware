package com.openexchange.appstore.noms;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.appstore.noms.actions.EnterAppAction;
import com.openexchange.appstore.noms.actions.EnterShopAction;
import com.openexchange.appstore.noms.actions.ListAction;
import com.openexchange.appstore.noms.actions.MarkupLinkAction;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;

public class NOMSAppActionFactory implements AJAXActionServiceFactory {

	private final Map<String, AJAXActionService> ACTIONS = new HashMap<String, AJAXActionService>();
	private final ServiceLookup services;
	
	public NOMSAppActionFactory(ServiceLookup services) {
		this.services = services;
		ACTIONS.put("list", new ListAction(services));
		ACTIONS.put("enterShop", new EnterShopAction(services));
		ACTIONS.put("enterApp", new EnterAppAction(services));
		ACTIONS.put("markup", new MarkupLinkAction(services));
			
	}
	
	@Override
	public AJAXActionService createActionService(String action)
			throws OXException {
		return ACTIONS.get(action);
	}

	@Override
	public Collection<?> getSupportedServices() {
		return Arrays.asList("list");
	}

}
