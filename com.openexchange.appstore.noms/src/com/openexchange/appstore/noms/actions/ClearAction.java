package com.openexchange.appstore.noms.actions;

import org.json.JSONArray;

import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.DispatcherNotes;
import com.openexchange.exception.OXException;
import com.openexchange.tools.session.ServerSession;

@DispatcherNotes(noSession=true)
public class ClearAction implements AJAXActionService {

	private JSONArray database;

	public ClearAction(JSONArray database) {
		this.database = database;
	}

	@Override
	public AJAXRequestResult perform(AJAXRequestData requestData,
			ServerSession session) throws OXException {
		database.reset();
		return new AJAXRequestResult("Thanks", "string");
	}

}
