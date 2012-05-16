package com.openexchange.oauth.json.proxy;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.API;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthService;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

public class OAuthProxyRequest {
	private AJAXRequestData req;
	private OAuthService oauthService;
	private boolean analyzed;
	private HTTPMethod method;
	private Map<String, String> parameters;
	private Map<String, String> headers;
	private String url;
	private String body;
	private ServerSession session;
	
	public static enum HTTPMethod {
		GET, PUT, POST, DELETE
	}
	
	public OAuthProxyRequest(AJAXRequestData req, ServerSession session, OAuthService oauthService) {
		this.req = req;
		this.oauthService = oauthService;
		this.session = session;
	}
	
	private void analyzeBody() throws OXException {
		if (analyzed) {
			return;
		}
		analyzed = true;
		
		// TODO: Error handling
		
		JSONObject proxyRequest = (JSONObject) req.getData();
		
		String methodName = proxyRequest.optString("type");
		if (methodName == null || methodName.equals("")) {
			methodName = "GET";
		} else {
			methodName = methodName.toUpperCase();
		}
		
		method = HTTPMethod.valueOf(methodName);
		
		JSONObject paramsObj = proxyRequest.optJSONObject("params");
		parameters = new TreeMap<String,String>();
		if(paramsObj != null){
			for(Entry<String, Object> entry : paramsObj.entrySet()){
				parameters.put(entry.getKey(), String.valueOf(entry.getValue()));
			}
		}
		
		JSONObject headerObj = proxyRequest.optJSONObject("header");
		headers = new TreeMap<String,String>();
		if(headerObj != null){
			for(Entry<String, Object> entry : headerObj.entrySet()){
				headers.put(entry.getKey(), String.valueOf(entry.getValue()));
			}
		}
		
		if(proxyRequest.hasAndNotNull("content-type")){
			headers.put("Content-Type", String.valueOf(proxyRequest.opt("content-type")));
		}
		
		if (!headers.containsKey("Content-Type")) {
			headers.put("Content-Type", "application/x-www-form-urlencoded");
		}
		
		if(proxyRequest.hasAndNotNull("accepts")){
			headers.put("Accepts", String.valueOf(proxyRequest.opt("accepts")));
		}
		
		try {
			url = proxyRequest.getString("url");
		} catch (JSONException e) {
			throw AjaxExceptionCodes.MISSING_PARAMETER.create("url");
		}
		
		body = proxyRequest.optString("data");
		
	}
	
	public OAuthAccount getAccount() throws OXException {
		if (req.isSet("id")) {
			int id = req.getParameter("id", int.class);
			
			return oauthService.getAccount(id, session, session.getUserId(), session.getContextId());
		}
		
		if (req.isSet("api")){
			API api = API.valueOf(req.getParameter("api").toUpperCase());
			
			return oauthService.getDefaultAccount(api, session);
		}
		req.require("id");
		return null; //should not be reached
	}
	
	public API getAPI() throws OXException {
		analyzeBody();
		return getAccount().getAPI();
	}
	
	public HTTPMethod getMethod() throws OXException {
		analyzeBody();
		return method;
	}
	
	public Map<String, String> getParameters() throws OXException {
		analyzeBody();
		return parameters;
	}
	
	public Map<String, String> getHeaders() throws OXException {
		analyzeBody();
		return headers;
	}
	
	public String getBody() throws OXException {
		analyzeBody();
		return body;
	}
	
	public String getUrl() throws OXException {
		// TODO: Whitelist
		analyzeBody();
		return url;
	}
}
