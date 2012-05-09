package com.openexchange.oauth.json.proxy;

import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.tools.session.ServerSession;

public class OAuthProxyAction implements AJAXActionService {

	@Override
	public AJAXRequestResult perform(AJAXRequestData requestData,
			ServerSession session) throws OXException {
		// Builder builder
		String action = requestData.getAction();
		if (action.equals("GET")) {
			return get(/*builder,*/ requestData);
		} else if (action.equals("PUT")) {
			return put(/*builder,*/ requestData);
		} else if (action.equals("POST")) {
			return post(/*builder,*/ requestData);
		} else if (action.equals("DELETE")) {
			return delete(/*builder,*/ requestData);
		}
		return null;
	}
	
	public AJAXRequestResult get(/* Builder builder, */ AJAXRequestData req) {
		/*
		 * ReqBuilder r = builder.get();
		 * initURL(r, req);
		 * initHeader(r, req);
		 * initParameters(r, req);
		 * 
		 */
		
		return null;
	}
	
	public AJAXRequestResult put(/* Builder req, */ AJAXRequestData req) {
		/*
		 * ReqBuilder r = builder.put();
		 * initURL(r, req);
		 * initHeader(r, req);
		 * initParameters(r, req);
		 * initBody(r, req);
		 */
		
		return null;
	}
	
	public AJAXRequestResult post(/* Builder req, */ AJAXRequestData req) {
		/*
		 * ReqBuilder r = builder.post(); // What about multipart POST ?
		 * initURL(r, req);
		 * initHeader(r, req);
		 * initParameters(r, req);
		 * initBody(r, req);
		 */
		
		return null;
	}
	
	public AJAXRequestResult delete(/* Builder req, */ AJAXRequestData req) {
		/*
		 * ReqBuilder r = builder.delete();
		 * initURL(r, req);
		 * initHeader(r, req);
		 * initParameters(r, req);
		 * 
		 */
		
		return null;
	}
	
	public void initURL(/* r ,*/ AJAXRequestData req) {
		
	}
	
	public void initHeader(/* r ,*/ AJAXRequestData req) {
		
	}
	
	public void initParameters(/* r ,*/ AJAXRequestData req) {
		
	}
	
	public void initBody(/* r ,*/ AJAXRequestData req) {
		
	}

}
