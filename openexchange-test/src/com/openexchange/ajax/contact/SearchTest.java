
package com.openexchange.ajax.contact;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.ContactTest;
import com.openexchange.ajax.config.actions.GetRequest;
import com.openexchange.ajax.config.actions.GetResponse;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.contact.action.SearchRequest;
import com.openexchange.ajax.contact.action.SearchResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.framework.AJAXRequest.Parameter;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.search.ContactSearchObject;

public class SearchTest extends ContactTest {

    private static final Log LOG = LogFactory.getLog(SearchTest.class);

    public SearchTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testSearchLoginUser() throws Exception {
        String username = getAJAXProperty("username");

        if (username == null) {
            username = getLogin();
        }

        final Contact[] contactArray = searchContact(
            getWebConversation(),
            username,
            FolderObject.SYSTEM_LDAP_FOLDER_ID,
            new int[] { Contact.INTERNAL_USERID },
            PROTOCOL + getHostName(),
            getSessionId());
        assertTrue("contact array size is 0", contactArray.length > 0);
        assertEquals("user id is not equals", userId, contactArray[0].getInternalUserId());
    }

    public void testSearchStartCharacter() throws Exception {
        final Contact contactObj = new Contact();
        contactObj.setSurName("Meier");
        contactObj.setParentFolderID(contactFolderId);
        final int objectId1 = insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getSessionId());
        final int objectId2 = insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getSessionId());

        final Contact[] contactArray = searchContact(
            getWebConversation(),
            "M",
            contactFolderId,
            new int[] { Contact.INTERNAL_USERID },
            true,
            PROTOCOL + getHostName(),
            getSessionId());
        assertTrue("contact array size < 2", contactArray.length >= 2);
        
        deleteContacts(objectId1, objectId2);
    }

    public void testSearchEmailComplete() throws Exception {
        final Contact contactObj = new Contact();
        contactObj.setSurName("Mustermann");
        contactObj.setGivenName("Tom");
        contactObj.setEmail1("tom.mustermann@email.com");
        contactObj.setParentFolderID(contactFolderId);

        final Contact contactObj2 = new Contact();
        contactObj2.setSurName("Mustermann");
        contactObj2.setGivenName("Ute");
        contactObj2.setEmail1("ute.mustermann@email.com");
        contactObj2.setParentFolderID(contactFolderId);

        final Contact contactObj3 = new Contact();
        contactObj3.setSurName("Gloreich");
        contactObj3.setGivenName("Guenter");
        contactObj3.setEmail1("g.gloreich@email.com");
        contactObj3.setParentFolderID(contactFolderId);

        final int objectId1 = insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getSessionId());
        final int objectId2 = insertContact(getWebConversation(), contactObj2, PROTOCOL + getHostName(), getSessionId());
        final int objectId3 = insertContact(getWebConversation(), contactObj3, PROTOCOL + getHostName(), getSessionId());

        ContactSearchObject cso = new ContactSearchObject();
        cso.setSurname("Must*");
        cso.setEmailAutoComplete(true);

        final Contact[] contactArray = searchContactAdvanced(
            getWebConversation(),
            cso,
            contactFolderId,
            new int[] { Contact.INTERNAL_USERID },
            PROTOCOL + getHostName(),
            getSessionId());
        assertTrue("contact array size >= 2", contactArray.length >= 2);

        cso = new ContactSearchObject();
        cso.setEmail1("*email.com");
        cso.setEmailAutoComplete(true);

        final Contact[] contactArray2 = searchContactAdvanced(
            getWebConversation(),
            cso,
            contactFolderId,
            new int[] { Contact.INTERNAL_USERID },
            PROTOCOL + getHostName(),
            getSessionId());
        assertTrue("contact array size >= 3", contactArray2.length >= 3);
        
        deleteContacts(objectId1, objectId2, objectId3);
    }

    // Node 2652
    public void testLastModifiedUTC() throws Exception {
        final AJAXClient client = new AJAXClient(new AJAXSession(getWebConversation(), getSessionId()));
        final int cols[] = new int[] { Contact.OBJECT_ID, Contact.FOLDER_ID, Contact.LAST_MODIFIED_UTC };

        final Contact contactObj = createContactObject("testLastModifiedUTC");
        final int objectId = insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getSessionId());
        try {

            final SearchRequest searchRequest = new SearchRequest("*", contactFolderId, cols, true);
            final SearchResponse response = Executor.execute(client, searchRequest);
            final JSONArray arr = (JSONArray) response.getResponse().getData();

            assertNotNull(arr);
            final int size = arr.length();
            assertTrue(size > 0);
            for (int i = 0; i < size; i++) {
                final JSONArray objectData = arr.optJSONArray(i);
                assertNotNull(objectData);
                assertNotNull(objectData.opt(2));
            }
        } finally {
            deleteContact(getWebConversation(), objectId, contactFolderId, getHostName(), getSessionId());
        }
    }
    
    public void testAutoCompleteWithContactCollectFolderAndGlobalAddressbook() throws Exception {
        int[] contactIds = new int[]{};
        try {
            final AJAXClient client = new AJAXClient(new AJAXSession(getWebConversation(), getSessionId()));
            final GetResponse getResponse = client.execute(new GetRequest(Tree.ContactCollectFolder));
            int collectFolderId = getResponse.getInteger();
            contactIds = insertSearchableContacts(collectFolderId);
            
            ContactSearchObject searchObject = new ContactSearchObject();
            searchObject.setEmail1("*e*");
            searchObject.setEmailAutoComplete(true);
            searchObject.addFolder(6);
            searchObject.addFolder(collectFolderId);
            
            int[] columns = new int[] {Contact.FOLDER_ID, Contact.OBJECT_ID, Contact.USE_COUNT};
            
            List<Parameter> parameters = new ArrayList<Parameter>();
            parameters.add(new Parameter(AJAXServlet.PARAMETER_SORT, Contact.USE_COUNT_GLOBAL_FIRST));
            parameters.add(new Parameter(AJAXServlet.PARAMETER_ORDER, "ASC"));
            com.openexchange.ajax.user.actions.SearchRequest request = new com.openexchange.ajax.user.actions.SearchRequest(searchObject, columns, true, parameters);
            com.openexchange.ajax.user.actions.SearchResponse response = Executor.execute(client, request);
            
            Contact[] result = jsonArray2ContactArray((JSONArray) response.getData(), columns);
            
            boolean stillGlobal = true;
            int previousCount = Integer.MAX_VALUE;
            for (Contact contactObject : result) {
                if (!stillGlobal) {
                    assertFalse("Did not expect global contacts any more.", contactObject.getParentFolderID() == 6);
                }
                if (contactObject.getParentFolderID() != 6) {
                    stillGlobal = false;
                }
                if (!stillGlobal) {
                    assertTrue("Wrong order of collected contacts.", previousCount >= contactObject.getUseCount());
                    previousCount = contactObject.getUseCount();
                }
            }
        } finally {
            deleteContacts(contactIds);
        }
        
    }

    // Node 3087

    private int[] insertSearchableContacts(int folderId) throws IOException, SAXException, JSONException, Exception {
        final Contact contactObj = new Contact();
        contactObj.setSurName("Mustermann");
        contactObj.setGivenName("Tom");
        contactObj.setEmail1("tom.mustermann@email.com");
        contactObj.setParentFolderID(folderId);
        contactObj.setUseCount(1);

        final Contact contactObj2 = new Contact();
        contactObj2.setSurName("Mustermann");
        contactObj2.setGivenName("Ute");
        contactObj2.setEmail1("ute.mustermann@email.com");
        contactObj2.setParentFolderID(folderId);
        contactObj2.setUseCount(2);

        final Contact contactObj3 = new Contact();
        contactObj3.setSurName("Gloreich");
        contactObj3.setGivenName("Guenter");
        contactObj3.setParentFolderID(folderId);
        contactObj3.setUseCount(3);

        final int objectId1 = insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getSessionId());
        final int objectId2 = insertContact(getWebConversation(), contactObj2, PROTOCOL + getHostName(), getSessionId());
        final int objectId3 = insertContact(getWebConversation(), contactObj3, PROTOCOL + getHostName(), getSessionId());

        return new int[] { objectId1, objectId2, objectId3 };
    }
    
    private int[] insertSearchableContacts() throws IOException, SAXException, JSONException, Exception {
        return insertSearchableContacts(contactFolderId);
    }

    private void deleteContacts(int... ids) throws IOException, SAXException, JSONException, Exception {
        for (int objectId : ids) {
            deleteContact(getWebConversation(), objectId, contactFolderId, PROTOCOL + getHostName(), getSessionId());
        }
    }

    public void testSearchByFirstAndLastName() throws Exception {

        int[] objectIds = insertSearchableContacts();
        
        try {
            ContactSearchObject cso = new ContactSearchObject();
            cso.setSurname("Must*");
            cso.setGivenName("U*");
            cso.setFolder(contactFolderId);

            SearchRequest search = new SearchRequest(
                cso,
                new int[] { Contact.OBJECT_ID, Contact.SUR_NAME, Contact.GIVEN_NAME },
                true);

            AJAXClient client = new AJAXClient(new AJAXSession(getWebConversation(), getSessionId()));

            SearchResponse result = client.execute(search);
            Object[][] rows = result.getArray();

            assertTrue("contact array size > 0. Expected at least 1 result.", rows.length > 0);

            for (Object[] row : rows) {
                assertTrue(((String) row[1]).startsWith("Must"));
                assertTrue(((String) row[2]).startsWith("U"));
            }
            
        } finally {
            deleteContacts(objectIds);
        }

    }

    // Bug 13227

    public void testOrSearchHabit() throws Exception {
        int[] objectIds = insertSearchableContacts();
        
        try {
            ContactSearchObject cso = new ContactSearchObject();
            cso.setSurname("Must*");
            cso.setGivenName("Gue*");
            cso.setFolder(contactFolderId);
            cso.setOrSearch(true);
            
            SearchRequest search = new SearchRequest(
                cso,
                new int[] { Contact.OBJECT_ID, Contact.SUR_NAME, Contact.GIVEN_NAME },
                true);

            AJAXClient client = new AJAXClient(new AJAXSession(getWebConversation(), getSessionId()));

            SearchResponse result = client.execute(search);
            Object[][] rows = result.getArray();

            assertTrue("contact array size > 0. Expected at least 1 result.", rows.length > 0);
            
            boolean foundTom = false, foundUte = false, foundGuenter = false;
            
            for (Object[] row : rows) {
                if(row[2].equals("Ute")) {
                    foundUte = true;
                }
                if(row[2].equals("Tom")) {
                    foundTom = true;
                }
                if(row[2].equals("Guenter")) {
                    foundGuenter = true;
                }
            }
            assertTrue("Expected Ute Mustermann, but didn't find her", foundUte);
            assertTrue("Expected Tom Mustermann, but didn't find him", foundTom);
            assertTrue("Expected Guenter Glorreich, but didn't find her", foundGuenter);
            
        } finally {
            deleteContacts(objectIds);
        }
    }

}
