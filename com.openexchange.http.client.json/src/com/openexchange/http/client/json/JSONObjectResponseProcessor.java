package com.openexchange.http.client.json;

import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.exception.OXException;
import com.openexchange.http.client.builder.HTTPResponseProcessor;
import com.openexchange.http.client.exceptions.OxHttpClientExceptionCodes;

public class JSONObjectResponseProcessor implements
		HTTPResponseProcessor {

	public Class<?>[] getTypes() {
		return new Class<?>[]{String.class, JSONObject.class};
	}

	public Object process(Object response)
			throws OXException {
		try {
			return new JSONObject((String)response);
		} catch (JSONException e) {
			throw OxHttpClientExceptionCodes.JSON_ERROR.create(e);
		}
	}

}
