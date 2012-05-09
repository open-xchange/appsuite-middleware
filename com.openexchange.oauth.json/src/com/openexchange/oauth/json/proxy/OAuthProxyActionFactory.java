package com.openexchange.oauth.json.proxy;

import java.util.Arrays;
import java.util.Collection;

import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.exception.OXException;

public class OAuthProxyActionFactory implements AJAXActionServiceFactory {

	private static final AJAXActionService PROXY_ACTION = new OAuthProxyAction();
	
	@Override
	public Collection<?> getSupportedServices() {
		return Arrays.asList("GET", "PUT", "POST", "DELETE");
	}

	@Override
	public AJAXActionService createActionService(String action)
			throws OXException {
		return PROXY_ACTION;
	}

}
