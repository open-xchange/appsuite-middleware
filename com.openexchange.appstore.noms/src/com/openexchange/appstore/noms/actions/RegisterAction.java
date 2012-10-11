package com.openexchange.appstore.noms.actions;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.DispatcherNotes;
import com.openexchange.exception.OXException;
import com.openexchange.tools.session.ServerSession;

@DispatcherNotes(noSession=true)
public class RegisterAction implements AJAXActionService {

	private static final Log LOG = com.openexchange.log.Log.loggerFor(RegisterAction.class);
	
	private final JSONArray database;

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
			Map<String, String> parameters = requestData.getParameters();
			for(Map.Entry<String, String> entry: parameters.entrySet()) {
				app.put(entry.getKey(), entry.getValue());
			}
			database.put(app);
		} catch (JSONException e) {
			LOG.error(e.getMessage(), e);
		}
		
		return new AJAXRequestResult("Thanks", "string");
	}

}
