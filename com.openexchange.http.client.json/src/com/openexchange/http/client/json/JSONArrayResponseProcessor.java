package com.openexchange.http.client.json;

import org.json.JSONArray;
import org.json.JSONException;

import com.openexchange.exception.OXException;
import com.openexchange.http.client.builder.HTTPResponseProcessor;
import com.openexchange.http.client.exceptions.OxHttpClientExceptionCodes;

public class JSONArrayResponseProcessor implements
		HTTPResponseProcessor {

	public Class<?>[] getTypes() {
		return new Class<?>[]{String.class, JSONArray.class};
	}

	public Object process(Object response)
			throws OXException {
		try {
			return new JSONArray((String)response);
		} catch (JSONException e) {
			throw OxHttpClientExceptionCodes.JSON_ERROR.create(e);
		}
	}

}
