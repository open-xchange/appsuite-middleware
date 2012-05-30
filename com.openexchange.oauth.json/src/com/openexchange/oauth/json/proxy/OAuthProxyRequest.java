package com.openexchange.oauth.json.proxy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.API;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.OAuthExceptionMessages;
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
	
	protected Map<API, List<Pattern>> whitelist = new HashMap<API,List<Pattern>>(){{
		put(API.FACEBOOK, 	Arrays.asList(Pattern.compile("^https:\\/\\/graph\\.facebook\\.com")));
		put(API.LINKEDIN, 	Arrays.asList(Pattern.compile("^http:\\/\\/api\\.linkedin\\.com")));
		put(API.TWITTER,	Arrays.asList(Pattern.compile("^https?:\\/\\/(.*?\\.)?twitter.com")));
		put(API.YAHOO, 	Arrays.asList(Pattern.compile("^https?:\\/\\/(.*?\\.)?yahoo(apis)?\\.com")));
	}};
>>>>>>> f5b2a1f6becf671faab49765c300917ee8528be2
	
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
		
		JSONObject proxyRequest = (JSONObject) req.getData();
		if(proxyRequest == null){
			throw OAuthExceptionCodes.MISSING_BODY.create();
		}
		
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
		whitelist(url);
		analyzeBody();
		return url;
	}

	private void whitelist(String checkMe) throws OXException {
		API proposedApi = getAccount().getAPI();
		List<Pattern> patterns = whitelist.get(proposedApi);
		if(patterns == null){
			throw OAuthExceptionCodes.NOT_A_WHITELISTED_URL.create(checkMe, proposedApi); //TODO: debatable
		}
		for(Pattern p: patterns){
			if(p.matcher(checkMe).find()){
				return;
			}
		}
		throw OAuthExceptionCodes.NOT_A_WHITELISTED_URL.create(checkMe, proposedApi);
	}
}
