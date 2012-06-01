package com.openexchange.appstore.noms.actions;

import org.json.JSONArray;

import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.tools.session.ServerSession;

public class ListAction implements AJAXActionService {

	private JSONArray database;

	public ListAction(JSONArray database) {
		this.database = database;
	}

	@Override
	public AJAXRequestResult perform(AJAXRequestData requestData,
			ServerSession session) throws OXException {
		return new AJAXRequestResult(database, "json");
	}

}
