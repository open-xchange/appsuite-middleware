package com.openexchange.ajax.contact;

import java.util.Date;

import org.json.JSONObject;
import com.openexchange.ajax.contact.action.AdvancedSearchRequest;
import com.openexchange.ajax.framework.CommonSearchResponse;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.test.ContactTestManager;

public class AdvancedSearchTest extends AbstractManagedContactTest{

	private static final String BOB_LASTNAME = "Rather complicated last name with timestamp ("+new Date().getTime() +") that does not appear in other folders";

	public AdvancedSearchTest(String name) {
		super(name);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		Contact alice = ContactTestManager.generateContact();
		alice.setGivenName("Alice");
		alice.setParentFolderID(folderID);
		
		Contact bob = ContactTestManager.generateContact();
		bob.setGivenName("Bob");
		bob.setSurName(BOB_LASTNAME);
		bob.setParentFolderID(folderID);
		
		Contact charlie = ContactTestManager.generateContact();
		charlie.setGivenName("Charlie");
		charlie.setParentFolderID(folderID);
		
		manager.newAction(alice,bob,charlie);
	}


	@Override
	public void tearDown() throws Exception {
		super.tearDown();
	}
	

	public void testSearchWithEquals() throws Exception {
		ContactField field = ContactField.GIVEN_NAME;
		ContactField folderField = ContactField.FOLDER_ID; 
		JSONObject filter = new JSONObject(
			"{\"filter\" : [ \"and\", " +
				"[\"equals\" , {\"field\" : \""+field.getAjaxName()+"\"} , \"Bob\"], " +
				"[\"equals\" , {\"field\" : \""+folderField.getAjaxName()+"\"}, "+folderID+"]" +
			"]})");
		
		AdvancedSearchRequest request = new AdvancedSearchRequest(filter, new int[]{Contact.GIVEN_NAME}, -1, null);
		CommonSearchResponse response = getClient().execute(request);
		assertFalse("Should work", response.hasError());
		
		Object[][] resultTable = response.getArray();
		assertNotNull("Should find a result", resultTable);
		assertEquals("Should find one result", 1, resultTable.length);
		
		int columnPos = response.getColumnPos(field.getNumber());
		String actual = (String) resultTable[0][columnPos];
		
		assertEquals("Bob", actual);
	}

	
	public void testSearchWithEqualsInAllFolders() throws Exception {
		ContactField field = ContactField.SUR_NAME;
		JSONObject filter = new JSONObject(
			"{\"filter\" : [ \"equals\" , {\"field\" : \""+field.getAjaxName()+"\"} , \""+BOB_LASTNAME+"\"]}");
		
		AdvancedSearchRequest request = new AdvancedSearchRequest(filter, Contact.ALL_COLUMNS, -1, null);
		CommonSearchResponse response = getClient().execute(request);
		assertFalse("Should work", response.hasError());
		
		Object[][] resultTable = response.getArray();
		assertNotNull("Should find a result", resultTable);
		assertEquals("Should find one result", 1, resultTable.length);
		
		int columnPos = response.getColumnPos(field.getNumber());
		String actual = (String) resultTable[0][columnPos];
		
		assertEquals(BOB_LASTNAME, actual);
	}
	
	/**
	 * Tests a SQL injection using our good friend Bobby, from 'Exploits of a mom', http://xkcd.com/327/
	 */
	public void testLittleBobbyTables() throws Exception {
		ContactField field = ContactField.SUR_NAME;
		String bobby = "Robert\\\"); DROP TABLE prg_contacts; --";
		
		JSONObject filter = new JSONObject(
					"{\"filter\" : [ \"or\", " +
						"[\"equals\" , {\"field\" : \""+field.getAjaxName()+"\"} , \""+BOB_LASTNAME+"\"], " +
						"[\"equals\" , {\"field\" : \""+field.getAjaxName()+"\"}, \""+bobby+"\"]" +
					"]})");
		
		AdvancedSearchRequest request = new AdvancedSearchRequest(filter, Contact.ALL_COLUMNS, -1, null);
		CommonSearchResponse response = getClient().execute(request);
		assertFalse("Should work", response.hasError());
		
		Object[][] resultTable = response.getArray();
		assertNotNull("Should find a result", resultTable);
		assertEquals("Should find one result", 1, resultTable.length);
		
		int columnPos = response.getColumnPos(field.getNumber());
		String actual = (String) resultTable[0][columnPos];
		
		assertEquals(BOB_LASTNAME, actual);
	}


}
