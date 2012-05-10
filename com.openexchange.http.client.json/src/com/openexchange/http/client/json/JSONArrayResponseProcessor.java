package com.openexchange.http.client.json;

import org.json.JSONArray;
import org.json.JSONException;

import com.openexchange.exception.OXException;
import com.openexchange.http.client.builder.HTTPResponse;
import com.openexchange.http.client.builder.HTTPResponseProcessor;
import com.openexchange.http.client.builder.HTTPResponseWrapper;
import com.openexchange.http.client.exceptions.OxHttpClientExceptionCodes;

public class JSONArrayResponseProcessor implements
		HTTPResponseProcessor<String, JSONArray> {

	public Class<?>[] getTypes() {
		return new Class<?>[]{String.class, JSONArray.class};
	}

	public HTTPResponse<JSONArray> process(HTTPResponse<String> response)
			throws OXException {
		try {
			JSONArray array = new JSONArray(response.getPayload());
			return new HTTPResponseWrapper<JSONArray>(response, array);
		} catch (JSONException e) {
			throw OxHttpClientExceptionCodes.JSON_ERROR.create(e);
		}
	}

}
