package com.openexchange.ajax.importexport;

import java.io.ByteArrayInputStream;
import org.json.JSONArray;

import com.openexchange.ajax.contact.AbstractManagedContactTest;
import com.openexchange.ajax.importexport.actions.CSVImportRequest;
import com.openexchange.ajax.importexport.actions.CSVImportResponse;


public class Bug20516Test extends AbstractManagedContactTest {

	public Bug20516Test(String name) {
		super(name);
	}

	public void testEmail() throws Exception{
		String ical = "Sur name,Given name,Email 1\nBroken,E-Mail,notanaddress\n";
		CSVImportRequest request = new CSVImportRequest(folderID, new ByteArrayInputStream(ical.getBytes()));
		CSVImportResponse response = getClient().execute(request);
		JSONArray data = (JSONArray) response.getData();
		assertTrue(data.getJSONObject(0).has("error"));
	}

}
