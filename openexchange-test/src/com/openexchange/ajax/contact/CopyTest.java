
package com.openexchange.ajax.contact;

import static org.junit.Assert.fail;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.contact.action.CopyRequest;
import com.openexchange.ajax.contact.action.CopyResponse;
import com.openexchange.ajax.contact.action.GetRequest;
import com.openexchange.ajax.contact.action.GetResponse;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;

public class CopyTest extends AbstractContactTest {

    private int objectId1;
    private int objectId2;
    private int targetFolder;
    private FolderObject folder;

    @Test
    public void testCopy() throws Exception {
        final Contact contactObj = new Contact();
        contactObj.setSurName("testCopy");
        contactObj.setParentFolderID(contactFolderId);
        objectId1 = insertContact(contactObj);

        folder = Create.createPrivateFolder("testCopy", FolderObject.CONTACT, userId);
        folder.setParentFolderID(getClient().getValues().getPrivateContactFolder());
        final InsertResponse folderCreateResponse = getClient().execute(new InsertRequest(EnumAPI.OUTLOOK, folder));
        folderCreateResponse.fillObject(folder);

        targetFolder = folder.getObjectID();

        final CopyRequest request = new CopyRequest(objectId1, contactFolderId, targetFolder, true);
        final CopyResponse response = getClient().execute(request);

        if (response.hasError()) {
            fail("json error: " + response.getErrorMessage());
        }

        objectId2 = 0;

        final JSONObject data = (JSONObject) response.getData();
        if (data.has(DataFields.ID)) {
            objectId2 = data.getInt(DataFields.ID);
        } else {
            fail("Could not find copied contact.");
        }

        final GetRequest getFirstContactRequest = new GetRequest(contactFolderId, objectId1, tz);
        final GetResponse firstContactResponse = getClient().execute(getFirstContactRequest);
        final Contact firstContact = firstContactResponse.getContact();
        final GetRequest getSecondContactRequest = new GetRequest(targetFolder, objectId2, tz);
        final GetResponse seconContactResponse = getClient().execute(getSecondContactRequest);
        final Contact secondContact = seconContactResponse.getContact();
        secondContact.setObjectID(objectId1);
        secondContact.setParentFolderID(contactFolderId);

        compareObject(firstContact, secondContact, false);
    }

}
