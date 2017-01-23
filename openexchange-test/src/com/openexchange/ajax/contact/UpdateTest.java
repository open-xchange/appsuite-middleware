
package com.openexchange.ajax.contact;

import static org.junit.Assert.fail;
import java.util.Date;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.contact.action.GetRequest;
import com.openexchange.ajax.contact.action.GetResponse;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.test.OXTestToolkit;

public class UpdateTest extends AbstractContactTest {

    @Test
    public void testUpdate() throws Exception {
        final Contact contactObj = createContactObject("testUpdate");
        final int objectId = insertContact(contactObj);

        contactObj.setObjectID(objectId);

        contactObj.setTelephoneBusiness1("+49009988776655");
        contactObj.setStateBusiness(null);
        contactObj.removeParentFolderID();

        updateContact(contactObj, contactFolderId);
    }

    @Test
    public void testUpdateWithDistributionList() throws Exception {
        final Contact contactEntry = createContactObject("internal contact");
        contactEntry.setEmail1("internalcontact@x.de");
        final int contactId = insertContact(contactEntry);
        contactEntry.setObjectID(contactId);

        final int objectId = createContactWithDistributionList("testUpdateWithDistributionList", contactEntry);

        GetRequest getRequest = new GetRequest(contactFolderId, objectId, tz, false);
        GetResponse getResponse = getClient().execute(getRequest);
        Date lastModified = new Date(((JSONObject) getResponse.getData()).getLong("last_modified"));

        final Contact contactObj = new Contact();
        contactObj.setSurName("testUpdateWithDistributionList");
        contactObj.setParentFolderID(contactFolderId);
        contactObj.setObjectID(objectId);
        contactObj.setLastModified(lastModified);

        final DistributionListEntryObject[] entry = new DistributionListEntryObject[2];
        entry[0] = new DistributionListEntryObject("displayname a", "a@a.de", DistributionListEntryObject.INDEPENDENT);
        entry[1] = new DistributionListEntryObject("displayname b", "b@b.de", DistributionListEntryObject.INDEPENDENT);

        contactObj.setDistributionList(entry);
        contactObj.removeParentFolderID();

        updateContact(contactObj, contactFolderId);
    }

    @Test
    public void testContactWithImage() throws Exception {
        final Contact contactObj = createContactObject("testContactWithImage");
        contactObj.setImage1(image);
        contactObj.setImageContentType("image/png");
        final int objectId = insertContact(contactObj);

        final GetRequest request = new GetRequest(contactFolderId, objectId, tz);
        final GetResponse response = getClient().execute(request);
        final String imageUrl = response.getImageUrl();
        if (imageUrl == null) {
            fail("Contact contains no image URL.");
        }

        final byte[] b = loadImageByURL(getClient().getProtocol(), getClient().getHostname(), imageUrl);

        OXTestToolkit.assertImageBytesEqualsAndNotNull("image", contactObj.getImage1(), b);
    }

    @Test
    public void testUpdateContactWithImage() throws Exception {
        final Contact contactObj = createContactObject("testUpdateContactWithImageUpdate");
        final int objectId = insertContact(contactObj);

        contactObj.setImage1(image);
        contactObj.setImageContentType("image/png");
        contactObj.removeParentFolderID();
        updateContact(contactObj, contactFolderId);

        final GetRequest request = new GetRequest(contactFolderId, objectId, tz);
        final GetResponse response = getClient().execute(request);
        final String imageUrl = response.getImageUrl();
        if (imageUrl == null) {
            fail("Contact contains no image URL.");
        }

        final byte[] b = loadImageByURL(getClient().getProtocol(), getClient().getHostname(), imageUrl);

        OXTestToolkit.assertImageBytesEqualsAndNotNull("image", contactObj.getImage1(), b);
    }
}
