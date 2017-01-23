
package com.openexchange.ajax.contact;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.contact.action.ContactUpdatesResponse;
import com.openexchange.ajax.contact.action.InsertResponse;
import com.openexchange.ajax.contact.action.UpdatesRequest;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.AbstractUpdatesRequest.Ignore;
import com.openexchange.ajax.framework.MultipleResponse;
import com.openexchange.groupware.container.Contact;

public class UpdatesTest extends AbstractContactTest {

    @Test
    public void testUpdates() throws Exception {
        // insert some
        final int numberofcontacts = 8;
        Contact[] newContacts = createSeveralContacts("updates", "contact", numberofcontacts);
        MultipleResponse<InsertResponse> insertResponses = createMultipleInsertRequest(newContacts);
        updateContactsWithTimeAndId(newContacts, insertResponses);

        // update 2
        Contact[] expectUpdatedContacts = { newContacts[0], newContacts[1] };
        Integer[] expectUpdatedContactIds = { newContacts[0].getObjectID(), newContacts[1].getObjectID() };
        updateContacts(expectUpdatedContacts);

        // delete 2
        Contact[] expectDeletedContacts = { newContacts[2], newContacts[3] };
        Integer[] expectDeletedContactIds = { newContacts[2].getObjectID(), newContacts[3].getObjectID() };
        deleteContacts(expectDeletedContacts);

        // check modified with timestamp from getAll
        Date lastModified = newContacts[numberofcontacts - 1].getLastModified();
        ContactUpdatesResponse modifiedContactsResponse = listModifiedContacts(contactFolderId, new int[] { Contact.OBJECT_ID, Contact.GIVEN_NAME, Contact.SUR_NAME, Contact.DISPLAY_NAME }, lastModified, Ignore.NONE);
        assertTrue(modifiedContactsResponse.getNewOrModifiedIds().containsAll(Arrays.asList(expectUpdatedContactIds)));
        assertTrue(modifiedContactsResponse.getDeletedIds().containsAll(Arrays.asList(expectDeletedContactIds)));

        // cleanup: delete all remaining
        List<Contact> contactsToDelete = new ArrayList<Contact>(numberofcontacts - expectDeletedContacts.length);
        for (int i = 0; i < newContacts.length; i++) {
            List<Integer> deletedIds = Arrays.asList(expectDeletedContactIds);
            if (!deletedIds.contains(newContacts[i].getObjectID())) {
                contactsToDelete.add(newContacts[i]);
            }
        }
        deleteContacts(contactsToDelete.toArray(new Contact[contactsToDelete.size()]));
    }

    private void updateContactsWithTimeAndId(Contact[] contacts, MultipleResponse<InsertResponse> insertResponses) throws Exception {
        for (int i = 0; i < contacts.length; i++) {
            Response response = insertResponses.getResponse(i).getResponse();
            Date timestamp = response.getTimestamp();
            JSONObject responseData = (JSONObject) response.getData();
            contacts[i].setLastModified(timestamp);
            contacts[i].setObjectID(responseData.getInt("id"));
        }
    }

    // Node 2652    @Test
    @Test
    public void testLastModifiedUTC() throws Exception {
        final int cols[] = new int[] { Contact.OBJECT_ID, Contact.FOLDER_ID, Contact.LAST_MODIFIED_UTC };

        final Contact contactObj = createContactObject("testLastModifiedUTC");
        final int objectId = insertContact(contactObj);
        try {
            final UpdatesRequest updatesRequest = new UpdatesRequest(contactFolderId, cols, -1, null, new Date(0));
            final AbstractAJAXResponse response = getClient().execute(updatesRequest);
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
            try {
                deleteContact(objectId, contactFolderId, true);
            } catch (final Exception e) {
                // ignore
            }
        }
    }
}
