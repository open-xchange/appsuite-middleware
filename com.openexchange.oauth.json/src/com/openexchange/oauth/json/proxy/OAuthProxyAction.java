package com.openexchange.oauth.json.proxy;

import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.DispatcherNotes;
import com.openexchange.exception.OXException;
import com.openexchange.http.client.HTTPClient;
import com.openexchange.http.client.builder.HTTPDeleteRequestBuilder;
import com.openexchange.http.client.builder.HTTPGenericRequestBuilder;
import com.openexchange.http.client.builder.HTTPGetRequestBuilder;
import com.openexchange.http.client.builder.HTTPPostRequestBuilder;
import com.openexchange.http.client.builder.HTTPPutRequestBuilder;
import com.openexchange.http.client.builder.HTTPRequest;
import com.openexchange.http.client.builder.HTTPResponse;
import com.openexchange.oauth.OAuthHTTPClientFactory;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.json.proxy.OAuthProxyRequest.HTTPMethod;
import com.openexchange.tools.session.ServerSession;

public class OAuthProxyAction implements AJAXActionService {

	private final OAuthService oauthService;
	private final OAuthHTTPClientFactory clients;
	
	public OAuthProxyAction(OAuthService service, OAuthHTTPClientFactory clients){
		oauthService = service;
		this.clients = clients;
	}
	
	@Override
	public AJAXRequestResult perform(AJAXRequestData requestData,
			ServerSession session) throws OXException {
		
		OAuthProxyRequest proxyRequest = new OAuthProxyRequest(requestData, session, oauthService);

		HTTPClient client = clients.create(proxyRequest.getAccount());
		
		HTTPMethod method = proxyRequest.getMethod();
		HTTPRequest httpRequest = null;
		
		switch (method) {
		case GET:
			httpRequest = buildGet(proxyRequest, client);
			break;
		case DELETE:
			httpRequest = buildDelete(proxyRequest, client);
			break;
		case PUT:
			httpRequest = buildPut(proxyRequest, client);
			break;
		case POST:
			httpRequest = buildPost(proxyRequest, client);
			break;

		default:
			break;
		}
		HTTPResponse httpResponse = httpRequest.execute();
		String payload = httpResponse.getPayload(String.class);
		return new AJAXRequestResult(payload, "string");
	}

	private HTTPRequest buildPost(OAuthProxyRequest proxyRequest, HTTPClient client) throws OXException {
		
		HTTPPostRequestBuilder builder = client.getBuilder().post();
		
		buildCommon(proxyRequest, builder);

		return builder.build();
	}

	private HTTPRequest buildPut(OAuthProxyRequest proxyRequest, HTTPClient client) throws OXException {
		
		HTTPPutRequestBuilder builder = client.getBuilder().put();
		
		buildCommon(proxyRequest, builder);
		builder.body(proxyRequest.getBody());
		return builder.build();
	}

	private HTTPRequest buildGet(OAuthProxyRequest proxyRequest,
			HTTPClient client) throws OXException {
		
		HTTPGetRequestBuilder builder = client.getBuilder().get();
		
		buildCommon(proxyRequest, builder);
		
		return builder.build();
		
	}
	
	private HTTPRequest buildDelete(OAuthProxyRequest proxyRequest,
			HTTPClient client) throws OXException {
		
		HTTPDeleteRequestBuilder builder = client.getBuilder().delete();
		
		buildCommon(proxyRequest, builder);
		
		return builder.build();
		
	}

	private void buildCommon(OAuthProxyRequest proxyRequest,
			HTTPGenericRequestBuilder<?> builder) throws OXException {
		builder.url(proxyRequest.getUrl()).headers(proxyRequest.getHeaders()).parameters(proxyRequest.getParameters());
	}
	
}
