package com.openexchange.appstore.noms.actions;

import org.apache.commons.logging.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.DispatcherNotes;
import com.openexchange.exception.OXException;
import com.openexchange.log.LogFactory;
import com.openexchange.tools.session.ServerSession;

@DispatcherNotes(noSession=true)
public class RegisterAction implements AJAXActionService {

	private static final Log LOG = LogFactory.getLog(RegisterAction.class);
	
	private JSONArray database;

	public RegisterAction(JSONArray database) {
		this.database = database;
	}

	@Override
	public AJAXRequestResult perform(AJAXRequestData requestData,
			ServerSession session) throws OXException {
		JSONObject app = new JSONObject();
		
		System.out.println("######## "+requestData.getParameters());
		
		
		try {
			app.put("img", requestData.getParameter("image"));
			app.put("url", requestData.getParameter("url"));
			database.put(app);
		} catch (JSONException e) {
			LOG.error(e.getMessage(), e);
		}
		
		return new AJAXRequestResult("Thanks", "string");
	}

}
