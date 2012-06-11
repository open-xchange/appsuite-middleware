package com.openexchange.appstore.noms;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;

import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.appstore.noms.actions.ClearAction;
import com.openexchange.appstore.noms.actions.ListAction;
import com.openexchange.appstore.noms.actions.RegisterAction;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;

public class NOMSAppActionFactory implements AJAXActionServiceFactory {

	private final Map<String, AJAXActionService> ACTIONS = new HashMap<String, AJAXActionService>();
	private final ServiceLookup services;
	
	public NOMSAppActionFactory(ServiceLookup services) {
		this.services = services;
		
		JSONArray database = new JSONArray();
		
		ACTIONS.put("GET", new RegisterAction(database));
		ACTIONS.put("POST", new RegisterAction(database));
		ACTIONS.put("list", new ListAction(database));
		ACTIONS.put("clear", new ClearAction(database));
	}
	
	@Override
	public AJAXActionService createActionService(String action)
			throws OXException {
		return ACTIONS.get(action);
	}

	@Override
	public Collection<?> getSupportedServices() {
		return Arrays.asList("GET", "POST", "list", "clear");
	}

}
