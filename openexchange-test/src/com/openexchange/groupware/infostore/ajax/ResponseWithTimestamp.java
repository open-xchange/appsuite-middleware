package com.openexchange.groupware.infostore.ajax;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ResponseWithTimestamp {
	
	private String error;
	private JSONObject response;
	private String[] errorParams;
	private long timestamp;

	public ResponseWithTimestamp(String res) throws JSONException {
		this(new JSONObject(res));
	}
	
	public ResponseWithTimestamp(JSONObject obj) throws JSONException{
		response = obj;
		System.out.println(response);
		error = response.optString("error");
		timestamp = response.optLong("timestamp");
		if(error != null) {
			JSONArray jErrorParams = response.optJSONArray("error_params");
			if(jErrorParams != null) {
				errorParams = new String[jErrorParams.length()];
				for(int i = 0; i < errorParams.length; i++){
					errorParams[i] = jErrorParams.getString(i);
				}
			}
		}
	}
	
	public JSONArray getDataAsArray() throws JSONException{
		return response.getJSONArray("data");
	}
	
	public JSONObject getDataAsObject() throws JSONException {
		return response.getJSONObject("data");
	}
	
	public long getTimestamp(){
		return timestamp;
	}
	
	public String getError(){
		return error;
	}
	
	public String[] getErrorParams(){
		return errorParams;
	}
	
	public String toString(){
		return response.toString();
	}
}
