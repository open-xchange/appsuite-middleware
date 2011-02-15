package com.openexchange.ajax.contact;

import org.json.JSONException;
import org.json.JSONObject;

public class TermSearchTest extends AbstractManagedContactTest {

	public TermSearchTest(String name) {
		super(name);
	}
	
	public void testSearchForFirstLetter() throws Exception{
		JSONObject query = new JSONObject("{ \"AND\" : [\"yomiLastName >= A\", \"yomiLastName < B\"] }");
		
	}

	public void testSearchForAll() throws Exception{
		JSONObject query = new JSONObject(
			"{ \"OR\": [" +
				"\"yomiLastName = Peter\", " +
				"\"yomiFirstName = Peter\"," +
				"\"yomiCompany = Peter\"," +
				"]" +
			"}");
		
	}

}
