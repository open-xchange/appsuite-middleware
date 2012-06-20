package com.openexchange.ajax.contact;

import java.util.Date;
import org.json.JSONObject;
import com.openexchange.ajax.contact.action.CopyRequest;
import com.openexchange.ajax.contact.action.CopyResponse;
import com.openexchange.ajax.contact.action.DeleteRequest;
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


    public CopyTest(final String name) {

		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testCopy() throws Exception {
		final Contact contactObj = new Contact();
		contactObj.setSurName("testCopy");
		contactObj.setParentFolderID(contactFolderId);
		objectId1 = insertContact(contactObj);

		folder = Create.createPrivateFolder("testCopy", FolderObject.CONTACT, userId);
		folder.setParentFolderID(client.getValues().getPrivateContactFolder());
		InsertResponse folderCreateResponse = client.execute(new InsertRequest(EnumAPI.OUTLOOK, folder));
		folderCreateResponse.fillObject(folder);

		targetFolder = folder.getObjectID();

		CopyRequest request = new CopyRequest(objectId1, contactFolderId, targetFolder, true);
		CopyResponse response = client.execute(request);


		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}

		objectId2 = 0;

		final JSONObject data = (JSONObject)response.getData();
		if (data.has(DataFields.ID)) {
			objectId2 = data.getInt(DataFields.ID);
		} else {
		    fail("Could not find copied contact.");
		}

		GetRequest getFirstContactRequest = new GetRequest(contactFolderId, objectId1, tz);
		GetResponse firstContactResponse = client.execute(getFirstContactRequest);
		Contact firstContact = firstContactResponse.getContact();

		GetRequest getSecondContactRequest = new GetRequest(targetFolder, objectId2, tz);
		GetResponse seconContactResponse = client.execute(getSecondContactRequest);
		Contact secondContact = seconContactResponse.getContact();
		secondContact.setObjectID(objectId1);
		secondContact.setParentFolderID(contactFolderId);

		compareObject(firstContact, secondContact);
	}


    @Override
    protected void tearDown() throws Exception {
        GetRequest getRequest1 = new GetRequest(contactFolderId, objectId1, tz, false);
        GetResponse getResponse1 = client.execute(getRequest1);
        Date lastModified1 = new Date(((JSONObject) getResponse1.getData()).getLong("last_modified"));
        client.execute(new DeleteRequest(contactFolderId, objectId1, lastModified1));
        if (objectId2 > 0) {
            GetRequest getRequest2 = new GetRequest(contactFolderId, objectId2, tz, false);
            GetResponse getResponse2 = client.execute(getRequest2);
            Date lastModified2 = new Date(((JSONObject) getResponse2.getData()).getLong("last_modified"));
            client.execute(new DeleteRequest(targetFolder, objectId2, lastModified2));
        }
        client.execute(new com.openexchange.ajax.folder.actions.DeleteRequest(EnumAPI.OUTLOOK, folder));

        super.tearDown();
    }
}
