package com.openexchange.http.client.json;

import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.exception.OXException;
import com.openexchange.http.client.builder.HTTPResponse;
import com.openexchange.http.client.builder.HTTPResponseProcessor;
import com.openexchange.http.client.builder.HTTPResponseWrapper;
import com.openexchange.http.client.exceptions.OxHttpClientExceptionCodes;

public class JSONObjectResponseProcessor implements
		HTTPResponseProcessor<String, JSONObject> {

	public Class<?>[] getTypes() {
		return new Class<?>[]{String.class, JSONObject.class};
	}

	public HTTPResponse<JSONObject> process(HTTPResponse<String> response)
			throws OXException {
		String payload = response.getPayload();
		try {
			JSONObject object = new JSONObject(payload);
			return new HTTPResponseWrapper<JSONObject>(response, object);
		} catch (JSONException e) {
			throw OxHttpClientExceptionCodes.JSON_ERROR.create(e);
		}
	}

}
