
package com.openexchange.ajax.contact;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;
import org.xml.sax.SAXException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.config.actions.GetRequest;
import com.openexchange.ajax.config.actions.GetResponse;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.contact.action.SearchRequest;
import com.openexchange.ajax.contact.action.SearchResponse;
import com.openexchange.ajax.framework.AJAXRequest.Parameter;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.search.ContactSearchObject;

public class SearchTest extends AbstractContactTest {

    @Test
    public void testSearchLoginUser() throws Exception {
        final Contact user = loadUser(userId);
        final String displayName = user.getDisplayName();
        final ContactSearchObject cso = new ContactSearchObject();
        cso.setDisplayName(displayName);
        cso.setFolders(FolderObject.SYSTEM_LDAP_FOLDER_ID);
        //        String username = AjaxInit.getAJAXProperty("user_participant1");
        //      final Contact[] contactArray = searchContact(username, FolderObject.SYSTEM_LDAP_FOLDER_ID, new int[] { Contact.INTERNAL_USERID });
        final Contact[] contactArray = searchContactAdvanced(cso, new int[] { Contact.INTERNAL_USERID });
        assertTrue("contact array size is 0", contactArray.length > 0);
        assertEquals("user id is not equals", userId, contactArray[0].getInternalUserId());
    }

    @Test
    public void testSearchStartCharacter() throws Exception {
        final Contact contactObj = new Contact();
        contactObj.setSurName("Meier");
        contactObj.setParentFolderID(contactFolderId);
        final int objectId1 = insertContact(contactObj);
        final int objectId2 = insertContact(contactObj);

        final Contact[] contactArray = searchContact("M", contactFolderId, new int[] { Contact.INTERNAL_USERID }, true);
        assertTrue("contact array size < 2", contactArray.length >= 2);

        deleteContacts(true, objectId1, objectId2);
    }

    @Test
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

        final int objectId1 = insertContact(contactObj);
        final int objectId2 = insertContact(contactObj2);
        final int objectId3 = insertContact(contactObj3);

        ContactSearchObject cso = new ContactSearchObject();
        cso.setSurname("Must*");
        cso.setEmailAutoComplete(true);

        final Contact[] contactArray = searchContactAdvanced(cso, new int[] { Contact.INTERNAL_USERID });
        assertTrue("contact array size >= 2", contactArray.length >= 2);

        cso = new ContactSearchObject();
        cso.setEmail1("*email.com");
        cso.setEmailAutoComplete(true);

        final Contact[] contactArray2 = searchContactAdvanced(cso, new int[] { Contact.INTERNAL_USERID });
        assertTrue("contact array size >= 3", contactArray2.length >= 3);

        deleteContacts(true, objectId1, objectId2, objectId3);
    }

    // Node 2652    @Test
    @Test
    public void testLastModifiedUTC() throws Exception {
        final int cols[] = new int[] { Contact.OBJECT_ID, Contact.FOLDER_ID, Contact.LAST_MODIFIED_UTC };

        final Contact contactObj = createContactObject("testLastModifiedUTC");
        final int objectId = insertContact(contactObj);
        try {

            final SearchRequest searchRequest = new SearchRequest("*", contactFolderId, cols, true);
            final SearchResponse response = Executor.execute(getClient(), searchRequest);
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
            deleteContact(objectId, contactFolderId, true);
        }
    }

    @Test
    public void testAutoCompleteWithContactCollectFolderAndGlobalAddressbook() throws Exception {
        int[] contactIds = new int[] {};
        try {
            int collectFolderId = -1;
            final GetResponse getResponse = getClient().execute(new GetRequest(Tree.ContactCollectFolder));
            if (getResponse.hasValue()) {
                if (getResponse.hasInteger()) {
                    collectFolderId = getResponse.getInteger();

                    if (-1 == collectFolderId) {
                        // Obviously contact collector folder is not present for test user
                        return;
                    }
                } else {
                    if (getResponse.hasString()) {
                        fail(getResponse.getString());
                    } else {
                        fail("Response is in wrong format:" + getResponse);
                    }
                }
            } else {
                fail("Response has no data:" + getResponse);
            }

            contactIds = insertSearchableContacts(collectFolderId);

            final ContactSearchObject searchObject = new ContactSearchObject();
            searchObject.setEmail1("*e*");
            searchObject.setEmailAutoComplete(true);
            searchObject.addFolder(6);
            searchObject.addFolder(collectFolderId);

            final int[] columns = new int[] { Contact.FOLDER_ID, Contact.OBJECT_ID, Contact.USE_COUNT };

            final List<Parameter> parameters = new ArrayList<Parameter>();
            parameters.add(new Parameter(AJAXServlet.PARAMETER_SORT, Contact.USE_COUNT_GLOBAL_FIRST));
            parameters.add(new Parameter(AJAXServlet.PARAMETER_ORDER, "ASC"));
            final com.openexchange.ajax.user.actions.SearchRequest request = new com.openexchange.ajax.user.actions.SearchRequest(searchObject, columns, true, parameters);
            final com.openexchange.ajax.user.actions.SearchResponse response = Executor.execute(getClient(), request);

            final Contact[] result = jsonArray2ContactArray((JSONArray) response.getData(), columns);

            boolean stillGlobal = true;
            int previousCount = Integer.MAX_VALUE;
            for (final Contact contactObject : result) {
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
            deleteContacts(true, contactIds);
        }

    }

    // Node 3087

    private int[] insertSearchableContacts(final int folderId) throws IOException, SAXException, JSONException, Exception {
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

        final int objectId1 = insertContact(contactObj);
        final int objectId2 = insertContact(contactObj2);
        final int objectId3 = insertContact(contactObj3);

        return new int[] { objectId1, objectId2, objectId3 };
    }

    private int[] insertSearchableContacts() throws IOException, SAXException, JSONException, Exception {
        return insertSearchableContacts(contactFolderId);
    }

    private void deleteContacts(final boolean ignoreFailure, final int... ids) throws IOException, SAXException, JSONException, Exception {
        for (final int objectId : ids) {
            deleteContact(objectId, contactFolderId, ignoreFailure);
        }
    }

    @Test
    public void testSearchByFirstAndLastName() throws Exception {
        final int[] objectIds = insertSearchableContacts();

        try {
            final ContactSearchObject cso = new ContactSearchObject();
            cso.setSurname("Must*");
            cso.setGivenName("U*");

            final SearchRequest search = new SearchRequest(cso, new int[] { Contact.OBJECT_ID, Contact.SUR_NAME, Contact.GIVEN_NAME }, true);

            final SearchResponse result = getClient().execute(search);
            final Object[][] rows = result.getArray();

            assertTrue("contact array size > 0. Expected at least 1 result.", rows.length > 0);

            for (final Object[] row : rows) {
                assertTrue(((String) row[1]).startsWith("Must"));
                assertTrue((((String) row[2]).length() > 0 && ((String) row[2]).charAt(0) == 'U'));
            }

        } finally {
            deleteContacts(true, objectIds);
        }

    }

    // Bug 13227
    @Test
    public void testOrSearchHabit() throws Exception {
        final int[] objectIds = insertSearchableContacts();

        try {
            final ContactSearchObject cso = new ContactSearchObject();
            cso.setSurname("Must*");
            cso.setGivenName("Gue*");
            cso.setOrSearch(true);

            final SearchRequest search = new SearchRequest(cso, new int[] { Contact.OBJECT_ID, Contact.SUR_NAME, Contact.GIVEN_NAME }, true);

            final SearchResponse result = getClient().execute(search);
            final Object[][] rows = result.getArray();

            assertTrue("contact array size > 0. Expected at least 1 result.", rows.length > 0);

            boolean foundTom = false, foundUte = false, foundGuenter = false;

            for (final Object[] row : rows) {
                if (row[2].equals("Ute")) {
                    foundUte = true;
                }
                if (row[2].equals("Tom")) {
                    foundTom = true;
                }
                if (row[2].equals("Guenter")) {
                    foundGuenter = true;
                }
            }
            assertTrue("Expected Ute Mustermann, but didn't find her", foundUte);
            assertTrue("Expected Tom Mustermann, but didn't find him", foundTom);
            assertTrue("Expected Guenter Glorreich, but didn't find her", foundGuenter);

        } finally {
            deleteContacts(true, objectIds);
        }
    }

}
