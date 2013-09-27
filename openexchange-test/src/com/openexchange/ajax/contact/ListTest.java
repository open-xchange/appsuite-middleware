package com.openexchange.ajax.contact;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.json.JSONArray;
import com.openexchange.ajax.contact.action.ListRequest;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.framework.ListIDs;
import com.openexchange.groupware.container.Contact;

public class ListTest extends AbstractContactTest {

	public ListTest(final String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testList() throws Exception {
		final Contact contactObj = createContactObject("testList");
		final int id1 = insertContact(contactObj);
		final int id2 = insertContact(contactObj);
		final int id3 = insertContact(contactObj);

		// prevent problems with master/slave
		Thread.sleep(1000);

		final int[][] objectIdAndFolderId = { { id3, contactFolderId }, { id2, contactFolderId }, { id1, contactFolderId } };

		final int cols[] = new int[]{ Contact.OBJECT_ID, Contact.SUR_NAME, Contact.DISPLAY_NAME, Contact.FOLDER_ID } ;

		final Contact[] contactArray = listContact(objectIdAndFolderId, cols);
		assertEquals("check response array", 3, contactArray.length);

		// Check order of returned contacts
		for (int i = 0; i < contactArray.length; i++) {
            final int[] ids = objectIdAndFolderId[i];
            assertTrue("Returned contacts object id differs from the requested one.", ids[0] == contactArray[i].getObjectID());
            assertTrue("Returned contacts folder id differs from the requested one.", ids[1] == contactArray[i].getParentFolderID());
        }
	}

	public void testSortDuration() throws Exception {
	    final int size = 10000;
	    final List<Contact> contacts = new ArrayList<Contact>();
	    final int[][] objectIdsAndFolderIds = new int[size][2];
	    for (int i = 0; i < size; i++) {
	        final int objectId = (size - 1) - i;
	        final Contact contact = createContactObject("testList");
	        contact.setObjectID(objectId);
            contacts.add(contact);

            objectIdsAndFolderIds[i] = new int[] { i, contactFolderId };
        }

	    // Sort loaded contacts in the order they were requested
	    final long start = System.currentTimeMillis();
        final List<Contact> sortedContacts = new ArrayList<Contact>(contacts.size());
        for (int i = 0; i < objectIdsAndFolderIds.length; i++) {
            final int[] objectIdsAndFolderId = objectIdsAndFolderIds[i];
            final int objectId = objectIdsAndFolderId[0];
            final int folderId = objectIdsAndFolderId[1];

            for (final Contact contact : contacts) {
                if (contact.getObjectID() == objectId && contact.getParentFolderID() == folderId) {
                    sortedContacts.add(contact);
                    break;
                }
            }
        }
        final long end = System.currentTimeMillis();
        final long diff = end -start;

	    // System.out.println("Duration: " + diff);
	}

	public void testListWithAllFields() throws Exception {
		final Contact contactObject = createCompleteContactObject();

		final int objectId = insertContact(contactObject);

		final int[][] objectIdAndFolderId = { { objectId, contactFolderId } };

		final Contact[] contactArray = listContact(objectIdAndFolderId, CONTACT_FIELDS);

		assertEquals("check response array", 1, contactArray.length);

		final Contact loadContact = contactArray[0];

		contactObject.setObjectID(objectId);
		compareObject(contactObject, loadContact);
	}

	public void testListWithNotExistingEntries() throws Exception {
		final Contact contactObject = createCompleteContactObject();
		final int objectId = insertContact(contactObject);
		contactObject.setDisplayName(UUID.randomUUID().toString());
		final int objectId2 = insertContact(contactObject);
		final int cols[] = new int[]{ Contact.OBJECT_ID, Contact.SUR_NAME, Contact.DISPLAY_NAME } ;

		// not existing object last
		final int[][] objectIdAndFolderId1 = { { objectId, contactFolderId }, { objectId+100, contactFolderId } };
		Contact[] contactArray = listContact(objectIdAndFolderId1, cols);
		assertEquals("check response array", 1, contactArray.length);

		// not existing object first
		final int[][] objectIdAndFolderId2 = { { objectId+100, contactFolderId }, { objectId, contactFolderId } };
		contactArray = listContact(objectIdAndFolderId2, cols);
		assertEquals("check response array", 1, contactArray.length);

		// not existing object first
		final int[][] objectIdAndFolderId3 = { { objectId+100, contactFolderId }, { objectId, contactFolderId }, { objectId2, contactFolderId } };
		contactArray = listContact(objectIdAndFolderId3, cols);
		assertEquals("check response array", 2, contactArray.length);

		deleteContact(objectId, contactFolderId, true);
	}

    // Node 2652
    public void testLastModifiedUTC() throws Exception {
        final int cols[] = new int[]{ Contact.OBJECT_ID, Contact.FOLDER_ID, Contact.LAST_MODIFIED_UTC};

        final Contact contactObj = createContactObject("testLastModifiedUTC");
		final int objectId = insertContact(contactObj);
        try {
            final ListRequest listRequest = new ListRequest(ListIDs.l(new int[]{contactFolderId, objectId}), cols, true);
            final CommonListResponse response = Executor.execute(client, listRequest);
            final JSONArray arr = (JSONArray) response.getResponse().getData();

            assertNotNull(arr);
            final int size = arr.length();
            assertTrue(size > 0);
            for(int i = 0; i < size; i++ ){
                final JSONArray objectData = arr.optJSONArray(i);
                assertNotNull(objectData);
                assertNotNull(objectData.opt(2));
            }
        } finally {
            deleteContact(objectId, contactFolderId, true);
        }
    }


}
