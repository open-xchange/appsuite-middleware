
package com.openexchange.ajax.contact;

import java.util.UUID;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.contact.action.UpdateRequest;
import com.openexchange.ajax.contact.action.UpdateResponse;
import com.openexchange.ajax.fields.CommonFields;
import com.openexchange.ajax.user.actions.GetRequest;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;

public class UpdateNotAllowedFieldsTest extends AbstractManagedContactTest {

    public UpdateNotAllowedFieldsTest(String name) {
        super(name);
    }

    public void testTryUpdateContextID() throws Exception {
        for (Contact contact : getContactsToUpdate()) {
            Contact changedContextID = new Contact();
            changedContextID.setContextId(3465474);
            Contact updatedContact = tryToUpdate(contact.getObjectID(), contact.getParentFolderID(), contact.getLastModified().getTime(), changedContextID, null);
            assertEquals("context ID was changed", contact.getContextId(), updatedContact.getContextId());
            changedContextID.setContextId(0);
            updatedContact = tryToUpdate(contact.getObjectID(), contact.getParentFolderID(), updatedContact.getLastModified().getTime(), changedContextID, null);
            assertEquals("context ID was changed", contact.getContextId(), updatedContact.getContextId());
            changedContextID.setContextId(43654754);
            changedContextID.setParentFolderID(getClient().getValues().getPrivateContactFolder());
            updatedContact = tryToUpdate(contact.getObjectID(), contact.getParentFolderID(), contact.getLastModified().getTime(), changedContextID, null);
            assertEquals("context ID was changed", contact.getContextId(), updatedContact.getContextId());

        }
    }

    public void testTryUpdateObjectID() throws Exception {
        for (Contact contact : getContactsToUpdate()) {
            Contact changedObjectID = new Contact();
            changedObjectID.setObjectID(1533523456);
            Contact updatedContact = tryToUpdate(contact.getObjectID(), contact.getParentFolderID(), contact.getLastModified().getTime(), changedObjectID, Category.CATEGORY_PERMISSION_DENIED);
            assertEquals("object ID was changed", contact.getObjectID(), updatedContact.getObjectID());
            changedObjectID.setObjectID(0);
            updatedContact = tryToUpdate(contact.getObjectID(), contact.getParentFolderID(), updatedContact.getLastModified().getTime(), changedObjectID, null);
            assertEquals("object ID was changed", contact.getObjectID(), updatedContact.getObjectID());
            changedObjectID.setObjectID(8794);
            changedObjectID.setParentFolderID(getClient().getValues().getPrivateContactFolder());
            updatedContact = tryToUpdate(contact.getObjectID(), contact.getParentFolderID(), contact.getLastModified().getTime(), changedObjectID, null);
            assertEquals("object ID was changed", contact.getObjectID(), updatedContact.getObjectID());
        }
    }

    public void testTryUpdateUserID() throws Exception {
        for (Contact contact : getContactsToUpdate()) {
            Contact changedUserID = new Contact();
            changedUserID.setInternalUserId(23235235);
            Contact updatedContact = tryToUpdate(contact.getObjectID(), contact.getParentFolderID(), contact.getLastModified().getTime(), changedUserID, Category.CATEGORY_PERMISSION_DENIED);
            assertEquals("user ID was changed", contact.getInternalUserId(), updatedContact.getInternalUserId());
            changedUserID.setInternalUserId(0);
            updatedContact = tryToUpdate(contact.getObjectID(), contact.getParentFolderID(), updatedContact.getLastModified().getTime(), changedUserID, null);
            assertEquals("user ID was changed", contact.getInternalUserId(), updatedContact.getInternalUserId());
            changedUserID.setInternalUserId(45600);
            changedUserID.setParentFolderID(getClient().getValues().getPrivateContactFolder());
            updatedContact = tryToUpdate(contact.getObjectID(), contact.getParentFolderID(), updatedContact.getLastModified().getTime(), changedUserID, Category.CATEGORY_PERMISSION_DENIED);
            assertEquals("user ID was changed", contact.getInternalUserId(), updatedContact.getInternalUserId());
        }
    }

    public void testTryUpdateUID() throws Exception {
        for (Contact contact : getContactsToUpdate()) {
            Contact changedUID = new Contact();
            changedUID.setUid(UUID.randomUUID().toString());
            Contact updatedContact = tryToUpdate(contact.getObjectID(), contact.getParentFolderID(), contact.getLastModified().getTime(), changedUID, Category.CATEGORY_PERMISSION_DENIED);
            assertEquals("UID was changed", contact.getUid(), updatedContact.getUid());
            changedUID.setUid(null);
            updatedContact = tryToUpdate(contact.getObjectID(), contact.getParentFolderID(), updatedContact.getLastModified().getTime(), changedUID, null);
            assertEquals("UID was changed", contact.getUid(), updatedContact.getUid());
            changedUID.setUid("");
            updatedContact = tryToUpdate(contact.getObjectID(), contact.getParentFolderID(), updatedContact.getLastModified().getTime(), changedUID, null);
            assertEquals("UID was changed", contact.getUid(), updatedContact.getUid());
            changedUID.setUid(UUID.randomUUID().toString());
            changedUID.setParentFolderID(getClient().getValues().getPrivateContactFolder());
            updatedContact = tryToUpdate(contact.getObjectID(), contact.getParentFolderID(), updatedContact.getLastModified().getTime(), changedUID, Category.CATEGORY_PERMISSION_DENIED);
            assertEquals("UID was changed", contact.getUid(), updatedContact.getUid());
        }
    }

    private Contact tryToUpdate(int objectID, int folderID, long timestamp, Contact changes, Category expectedExceptionCategory) throws Exception {
        DeltaUpdateRequest updateRequest = new DeltaUpdateRequest(objectID, folderID, timestamp, changes, false);
        UpdateResponse updateResponse = super.getClient().execute(updateRequest);
        assertNotNull("got no response", updateResponse);
        if (null != expectedExceptionCategory) {
            OXException exception = updateResponse.getException();
            assertNotNull("got no exception", exception);
            assertEquals("unexpected exception category", expectedExceptionCategory, exception.getCategory());
        }
        return manager.getAction(folderID, objectID);
    }

    private Contact[] getContactsToUpdate() throws Exception {
        return new Contact[] {
            client.execute(new GetRequest(client.getValues().getUserId(), client.getValues().getTimeZone())).getContact(),
            manager.getAction(manager.newAction(generateContact())) };
    }

    private static class DeltaUpdateRequest extends UpdateRequest {

        private final int objectID;
        private final int folderID;
        private final long timestamp;

        public DeltaUpdateRequest(int objectID, int folderID, long timestamp, Contact changes, boolean failOnErrors) {
            super(folderID, changes, failOnErrors);
            this.objectID = objectID;
            this.folderID = folderID;
            this.timestamp = timestamp;
        }

        @Override
        public Parameter[] getParameters() {
            return new Parameter[] {
                new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_UPDATE),
                new Parameter(AJAXServlet.PARAMETER_INFOLDER, String.valueOf(folderID)),
                new Parameter(AJAXServlet.PARAMETER_ID, String.valueOf(objectID)),
                new Parameter(AJAXServlet.PARAMETER_TIMESTAMP, String.valueOf(timestamp))
            };
        }

        @Override
        public Object getBody() throws JSONException {
            Contact contact = super.getContact();
            JSONObject jsonObject = convert(contact);
            if (contact.containsContextId()) {
                jsonObject.put("cid", contact.getContextId());
            }
            if (contact.containsUid()) {
                jsonObject.put(CommonFields.UID, null == contact.getUid() ? JSONObject.NULL : contact.getUid());
            }
            return jsonObject;
        }

    }

}
