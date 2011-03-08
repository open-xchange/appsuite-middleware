package com.openexchange.ajax.contact;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
		Contact alice = ContactTestManager.generateContact(folderID);
		alice.setGivenName("Alice");
		
		Contact bob = ContactTestManager.generateContact(folderID);
		bob.setGivenName("Bob");
		bob.setSurName(BOB_LASTNAME);
		
		Contact charlie = ContactTestManager.generateContact(folderID);
		charlie.setGivenName("Charlie");
		
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
			"{'filter' : [ 'and', " +
				"['=' , {'field' : '"+field.getAjaxName()+"'} , 'Bob'], " +
				"['=' , {'field' : '"+folderField.getAjaxName()+"'}, "+folderID+"]" +
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
			"{'filter' : [ '=' , {'field' : '"+field.getAjaxName()+"'} , '"+BOB_LASTNAME+"']}");
		
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
					"{'filter' : [ 'or', " +
						"['=' , {'field' : '"+field.getAjaxName()+"'} , '"+BOB_LASTNAME+"'], " +
						"['=' , {'field' : '"+field.getAjaxName()+"'}, '"+bobby+"']" +
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
	
	public void testSearchAlphabetRange() throws Exception{
		ContactField field = ContactField.GIVEN_NAME;
		JSONObject filter = new JSONObject(
				"{'filter' : [ 'and', " +
					"['>=' , {'field' : '"+field.getAjaxName()+"'} , 'A'], " +
					"['<' , {'field' : '"+field.getAjaxName()+"'}, 'C'], " +
					"['=' , {'field' : '"+ContactField.FOLDER_ID.getAjaxName()+"'}, "+folderID+"]" +
				"]})");
		
		AdvancedSearchRequest request = new AdvancedSearchRequest(filter, new int[]{field.getNumber()}, -1, null);
		CommonSearchResponse response = getClient().execute(request);
		assertFalse("Should work", response.hasError());
		
		Object[][] resultTable = response.getArray();
		assertNotNull("Should find at least a result", resultTable);
		assertEquals("Should find two results", 2, resultTable.length);
		
		int columnPos = response.getColumnPos(field.getNumber());
		Set<String> names = new HashSet<String>();
		names.add( (String) resultTable[0][columnPos] );
		names.add( (String) resultTable[1][columnPos] );
		
		assertTrue(names.contains("Bob"));
		assertTrue(names.contains("Alice"));
	}

	public void testSearchOrdering() throws Exception{
		manager.newAction(
			ContactTestManager.generateContact(folderID, "Elvis"),
			ContactTestManager.generateContact(folderID, "Feelvis"),
			ContactTestManager.generateContact(folderID, "Gelvis"),
			ContactTestManager.generateContact(folderID, "Geena"),
			ContactTestManager.generateContact(folderID, "Hellvis")
		);
		ContactField field = ContactField.SUR_NAME;
		JSONObject filter = new JSONObject(
				"{'filter' : [ 'and', " +
					"['>=' , {'field' : '"+field.getAjaxName()+"'} , 'E'], " +
					"['<' , {'field' : '"+field.getAjaxName()+"'}, 'I'], " +
					"['NOT' , ['=' , {'field' : '"+field.getAjaxName()+"'}, 'Geena']], " +
					"['=' , {'field' : '"+ContactField.FOLDER_ID.getAjaxName()+"'}, "+folderID+"]" +
				"]})");
		
		AdvancedSearchRequest request = new AdvancedSearchRequest(filter, new int[]{field.getNumber()}, field.getNumber(), "asc");
		CommonSearchResponse response = getClient().execute(request);
		assertFalse("Should work", response.hasError());
		
		Object[][] resultTable = response.getArray();
		assertNotNull("Should find at least a result", resultTable);
		assertEquals("Should find four results", 4, resultTable.length);
		
		int columnPos = response.getColumnPos(field.getNumber());
		
		assertTrue("Result should appear in the right order", resultTable[0][columnPos].equals("Elvis"));
		assertTrue("Result should appear in the right order", resultTable[1][columnPos].equals("Feelvis"));
		assertTrue("Result should appear in the right order", resultTable[2][columnPos].equals("Gelvis"));
		assertTrue("Result should appear in the right order", resultTable[3][columnPos].equals("Hellvis"));
		
		/* invert it */
		request = new AdvancedSearchRequest(filter, new int[]{field.getNumber()}, field.getNumber(), "desc");
		response = getClient().execute(request);
		assertFalse("Should work", response.hasError());
		
		resultTable = response.getArray();
		assertNotNull("Should find at least a result", resultTable);
		assertEquals("Should find four results", 4, resultTable.length);
		
		columnPos = response.getColumnPos(field.getNumber());
		
		assertTrue("Result should appear in the right order", resultTable[0][columnPos].equals("Hellvis"));
		assertTrue("Result should appear in the right order", resultTable[1][columnPos].equals("Gelvis"));
		assertTrue("Result should appear in the right order", resultTable[2][columnPos].equals("Feelvis"));
		assertTrue("Result should appear in the right order", resultTable[3][columnPos].equals("Elvis"));

	}
	
	public void testSearchOrderingWithKana() throws Exception{
		manager.newAction(
				ContactTestManager.generateContact(folderID, "\u30ef"),
				ContactTestManager.generateContact(folderID, "\u30ea"),
				ContactTestManager.generateContact(folderID, "\u30e9"),
				ContactTestManager.generateContact(folderID, "\u30e5"),
				ContactTestManager.generateContact(folderID, "\u30e4"),
				ContactTestManager.generateContact(folderID, "\u30df"),
				ContactTestManager.generateContact(folderID, "\u30de"),
				ContactTestManager.generateContact(folderID, "\u30d0"),
				ContactTestManager.generateContact(folderID, "\u30cf"),
				ContactTestManager.generateContact(folderID, "\u30cb"),
				ContactTestManager.generateContact(folderID, "\u30ca"),
				ContactTestManager.generateContact(folderID, "\u30c0"),
				ContactTestManager.generateContact(folderID, "\u30bf"),
				ContactTestManager.generateContact(folderID, "\u30b6"),
				ContactTestManager.generateContact(folderID, "\u30b5"),
				ContactTestManager.generateContact(folderID, "\u30ac"),
				ContactTestManager.generateContact(folderID, "\u30ab"),
				ContactTestManager.generateContact(folderID, "\u30a3"),
				ContactTestManager.generateContact(folderID, "\u30a2")
		);
		
		String[] letters = new String[]{"\u30a2", "\u30ab", "\u30b5", "\u30bf", "\u30ca", "\u30cf", "\u30de", "\u30e4", "\u30e9", "\u30ef"};
		
		ContactField field = ContactField.SUR_NAME;
		LinkedList<JSONObject> filters = new LinkedList<JSONObject>();
		for(int i = 0; i < letters.length -1; i++)
			filters.add( new JSONObject(
				"{'filter' : [ 'and', " +
					"['>=' , {'field' : '"+field.getAjaxName()+"'} , '"+letters[i]+"'], " +
					"['<' , {'field' : '"+field.getAjaxName()+"'}, '"+letters[i+1]+"'], " +
					"['=' , {'field' : '"+ContactField.FOLDER_ID.getAjaxName()+"'}, "+folderID+"]" +
				"]})"));
		
		int currentPosition = 0;
		for(JSONObject filter: filters){
			String ident = "Step #"+currentPosition + " from "+letters[currentPosition]+" to "+letters[currentPosition+1]+": ";
			AdvancedSearchRequest request = new AdvancedSearchRequest(filter, new int[]{field.getNumber()}, field.getNumber(), "asc");
			CommonSearchResponse response = getClient().execute(request);
			assertFalse(ident+"Should work", response.hasError());
			
			Object[][] resultTable = response.getArray();
			assertNotNull(ident+"Should find at least a result", resultTable);
			assertEquals(ident+"Should find two results", 2, resultTable.length);
			
			int columnPos = response.getColumnPos(field.getNumber());
			HashSet<String> names = new HashSet<String>();
			names.add((String) resultTable[0][columnPos]);
			names.add((String) resultTable[1][columnPos]);
			assertTrue(ident+"Should be contained", names.contains(letters[currentPosition]));
			
			currentPosition++;
		}
	}

	public void testSearchOrderingWithHanzi() throws Exception{
		List<String> sinograph = Arrays.asList( "阿", "波","次","的","鹅","富","哥","河","洁","科","了","么","呢","哦","批","七","如","四","踢","屋","西","衣","子");
		for(String graphem: sinograph){
			manager.newAction(ContactTestManager.generateContact(folderID, graphem));
		}
		
		
		ContactField field = ContactField.SUR_NAME;
		LinkedList<JSONObject> filters = new LinkedList<JSONObject>();
		for(int i = 0; i < sinograph.size() - 1; i++)
			filters.add( new JSONObject(
				"{'filter' : [ 'and', " +
					"['>=' , {'field' : '"+field.getAjaxName()+"'} , '"+sinograph.get(i)+"'], " +
					"['<' , {'field' : '"+field.getAjaxName()+"'}, '"+sinograph.get(i+1)+"'], " +
					"['=' , {'field' : '"+ContactField.FOLDER_ID.getAjaxName()+"'}, "+folderID+"]" +
				"]})"));
		
		int currentPosition = 0;
		for(JSONObject filter: filters){
			String ident = "Step #"+currentPosition + " from "+sinograph.get(currentPosition)+" to "+sinograph.get(currentPosition+1)+": ";
			AdvancedSearchRequest request = new AdvancedSearchRequest(filter, new int[]{field.getNumber()}, field.getNumber(), "asc");
			CommonSearchResponse response = getClient().execute(request);
			assertFalse(ident+"Should work", response.hasError());
			
			Object[][] resultTable = response.getArray();
			assertNotNull(ident+"Should find at least a result", resultTable);
			assertEquals(ident+"Should find one result", 1, resultTable.length);
			
			int columnPos = response.getColumnPos(field.getNumber());
			String actualName = (String) resultTable[0][columnPos];
			assertEquals(ident+"Should be contained", sinograph.get(currentPosition), actualName);
			
			currentPosition++;
		}
	}

	
}
