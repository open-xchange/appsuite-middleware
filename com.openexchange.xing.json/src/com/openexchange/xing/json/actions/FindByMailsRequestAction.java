package com.openexchange.xing.json.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.xing.XingAPI;
import com.openexchange.xing.access.XingExceptionCodes;
import com.openexchange.xing.access.XingOAuthAccess;
import com.openexchange.xing.exception.XingException;
import com.openexchange.xing.json.XingRequest;
import com.openexchange.xing.session.WebAuthSession;

public class FindByMailsRequestAction extends AbstractXingAction {

	public FindByMailsRequestAction(ServiceLookup services) {
		super(services);
	}

	@Override
	protected AJAXRequestResult perform(XingRequest req) throws OXException,
			JSONException, XingException {
        Object objData = req.getRequest().getData();
        if (objData == null) {
            throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
        }

        if (!(objData instanceof JSONObject)) {
            throw AjaxExceptionCodes.BAD_REQUEST.create();
        }
        
        JSONObject jsonData = (JSONObject) objData;
        JSONArray jsonArray = (JSONArray) jsonData.optJSONArray("emails");
        if(jsonArray == null) {
        	throw XingExceptionCodes.MANDATORY_REQUEST_DATA_MISSING.create("emails");
        }
        
        List<String> emails = new ArrayList<String>();
       
        for(int i=0; i<jsonArray.length(); i++){
        	String email = jsonArray.getString(i);
        	email = validateMailAddress(email);
        	emails.add(email);
        }
        
        String token = req.getParameter("testToken");
		String secret = req.getParameter("testSecret");

		final XingOAuthAccess xingOAuthAccess;
		{
			if (!Strings.isEmpty(token) && !Strings.isEmpty(secret)) {
				xingOAuthAccess = getXingOAuthAccess(token, secret,
						req.getSession());
			} else {
				xingOAuthAccess = getXingOAuthAccess(req);
			}
		}

		XingAPI<WebAuthSession> xingAPI = xingOAuthAccess.getXingAPI();
		Map<String, Object> xingUser = xingAPI.findByEmailsGetXingAttributes(emails);
		final JSONObject result = (JSONObject) JSONCoercion.coerceToJSON(xingUser);

		return new AJAXRequestResult(result);
	}
}
