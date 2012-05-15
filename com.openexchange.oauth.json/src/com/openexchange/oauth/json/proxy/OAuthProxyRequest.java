package com.openexchange.oauth.json.proxy;

import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthService;

public class OAuthProxyRequest {
	private final AJAXRequestData req;
	private final OAuthService oauthService;
	
	public OAuthProxyRequest(AJAXRequestData req, OAuthService oauthService) {
		this.req = req;
		this.oauthService = oauthService;
	}
	
	public OAuthAccount getAccount() {
		// TODO
		return null;
	}
}
