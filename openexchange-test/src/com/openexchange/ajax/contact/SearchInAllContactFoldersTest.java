package com.openexchange.ajax.contact;

import java.io.IOException;

import org.json.JSONException;
import org.xml.sax.SAXException;

import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.contact.action.InsertRequest;
import com.openexchange.ajax.contact.action.InsertResponse;
import com.openexchange.ajax.contact.action.SearchRequest;
import com.openexchange.ajax.contact.action.DeleteRequest;
import com.openexchange.ajax.contact.action.SearchResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.tools.servlet.AjaxException;

/**
* This test creates one folder and two users (one user in the new folder and one user in the private contacts folder). Then a search is performed for their common first name.
* The search is asserted to return both contacts.
* @author <a href="mailto:karsten.will@open-xchange.org">Karsten Will</a>
*/
public class SearchInAllContactFoldersTest extends AbstractAJAXSession {
	
	ContactObject contactObject1;
	ContactObject contactObject2;
	FolderObject newFolderObject;
	int privateFolderId;
	int newFolderId;
	
	public SearchInAllContactFoldersTest(final String name) {
		super(name);
	}

	public void setUp() throws Exception {
		super.setUp();
		final AJAXClient client = getClient();
		//get the id of the private contacts-folder
        privateFolderId = client.getValues().getPrivateContactFolder();
        //create a new folder
        newFolderObject = Create.createPublicFolder(client, "Testfolder2", FolderObject.CONTACT);
        newFolderId = newFolderObject.getObjectID();
        //create a contact in the private folder
        contactObject1 = new ContactObject();
        contactObject1.setSurName("Meier");
        contactObject1.setGivenName("Herbert");
        contactObject1.setEmail1("herbert.meier@internet.com");
        contactObject1.setParentFolderID(privateFolderId);
        InsertRequest insertContact1 = new InsertRequest(contactObject1);
        InsertResponse insertResponse = (InsertResponse) client.execute(insertContact1);
        insertResponse.fillObject(contactObject1);
        //create a contact in the new folder
        contactObject2 = new ContactObject();
        contactObject2.setSurName("MŸller");
        contactObject2.setGivenName("Herbert");
        contactObject2.setEmail1("herbert.mueller@internet.com");
        contactObject2.setParentFolderID(newFolderId);
        InsertRequest insertContact2 = new InsertRequest(contactObject2);
        insertResponse = (InsertResponse) client.execute(insertContact2);
        insertResponse.fillObject(contactObject2);
	}
	
	public void tearDown() throws Exception {
		final AJAXClient client = getClient();
		//delete the two contacts
		DeleteRequest contactDeleteRequest = new DeleteRequest(contactObject1);
		client.execute(contactDeleteRequest);
		contactDeleteRequest = new DeleteRequest(contactObject2);
		client.execute(contactDeleteRequest);
		//delete the new folder
		com.openexchange.ajax.folder.actions.DeleteRequest folderDeleteRequest  = new com.openexchange.ajax.folder.actions.DeleteRequest(newFolderObject);
		client.execute(folderDeleteRequest);
		super.tearDown();
	}
	
	public void testAllContactFoldersSearch(){
		
        try {
        	final AJAXClient client = getClient();
			//execute a search over first name and last name in all folders (folder id -1) that matches both contacts
			int [] columns = new int [] {ContactObject.GIVEN_NAME, ContactObject.SUR_NAME};
			SearchRequest searchRequest = new SearchRequest("Herbert", -1, columns, false);
			
			SearchResponse searchResponse = client.execute(searchRequest);
			String responseString = searchResponse.getResponse().getData().toString();
			//System.out.println("***** Response : " + responseString);
			
			//assert that both contacts are found
			assertTrue(responseString.indexOf("[\"Herbert\",\"MŸller\"]")  != -1 && responseString.indexOf("[\"Herbert\",\"Meier\"]")  != -1);
		} catch (AjaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
