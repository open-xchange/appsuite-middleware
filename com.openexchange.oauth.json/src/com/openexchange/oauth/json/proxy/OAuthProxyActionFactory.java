package com.openexchange.oauth.json.proxy;

import java.util.Arrays;
import java.util.Collection;

import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.OAuthHTTPClientFactory;
import com.openexchange.oauth.OAuthService;

public class OAuthProxyActionFactory implements AJAXActionServiceFactory {
	
	private AJAXActionService proxyAction;

	public OAuthProxyActionFactory(OAuthService service, OAuthHTTPClientFactory clients){
		proxyAction = new OAuthProxyAction(service, clients);
	}
	
	@Override
	public Collection<?> getSupportedServices() {
		return Arrays.asList("PUT", "POST");
	}

	@Override
	public AJAXActionService createActionService(String action) throws OXException {
		return proxyAction;
	}

}
