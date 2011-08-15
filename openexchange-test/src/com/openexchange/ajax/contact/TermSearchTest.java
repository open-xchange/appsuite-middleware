package com.openexchange.ajax.contact;

import org.json.JSONObject;

public class TermSearchTest extends AbstractManagedContactTest {

	public TermSearchTest(String name) {
		super(name);
	}

	public void testSearchForFirstLetter() throws Exception{
		new JSONObject("{ \"AND\" : [\"yomiLastName >= A\", \"yomiLastName < B\"] }");

	}

	public void testSearchForAll() throws Exception{
		new JSONObject(
			"{ \"OR\": [" +
				"\"yomiLastName = Peter\", " +
				"\"yomiFirstName = Peter\"," +
				"\"yomiCompany = Peter\"," +
				"]" +
			"}");

	}

}
