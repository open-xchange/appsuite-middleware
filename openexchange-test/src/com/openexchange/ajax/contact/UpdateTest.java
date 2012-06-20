
package com.openexchange.ajax.contact;

import java.util.Date;
import com.openexchange.ajax.contact.action.GetRequest;
import com.openexchange.ajax.contact.action.GetResponse;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.groupware.container.LinkEntryObject;
import com.openexchange.test.OXTestToolkit;

public class UpdateTest extends AbstractContactTest {

    public UpdateTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testUpdate() throws Exception {
        final Contact contactObj = createContactObject("testUpdate");
        final int objectId = insertContact(contactObj);

        contactObj.setObjectID(objectId);

        contactObj.setTelephoneBusiness1("+49009988776655");
        contactObj.setStateBusiness(null);
        contactObj.removeParentFolderID();

        updateContact(contactObj, contactFolderId);
    }

    public void testUpdateWithDistributionList() throws Exception {
        final Contact contactEntry = createContactObject("internal contact");
        contactEntry.setEmail1("internalcontact@x.de");
        final int contactId = insertContact(contactEntry);
        contactEntry.setObjectID(contactId);

        final int objectId = createContactWithDistributionList("testUpdateWithDistributionList", contactEntry);

        final GetRequest getRequest = new GetRequest(contactFolderId, objectId, getClient().getValues().getTimeZone());
        final Date lastModified = client.execute(getRequest).getResponse().getTimestamp();

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

    public void testUpdateWithLinks() throws Exception {
        final Contact link1 = createContactObject("link1");
        final Contact link2 = createContactObject("link2");
        final int linkId1 = insertContact(link1);
        link1.setObjectID(linkId1);
        final int linkId2 = insertContact(link2);
        link2.setObjectID(linkId2);

        final int objectId = createContactWithLinks("testUpdateWithLinks", link1, link2);
        
        final GetRequest getRequest = new GetRequest(contactFolderId, objectId, getClient().getValues().getTimeZone());
        final Date lastModified = client.execute(getRequest).getResponse().getTimestamp();

        final Contact link3 = createContactObject("link3");
        final int linkId3 = insertContact(link3);

        final Contact contactObj = new Contact();
        contactObj.setSurName("testUpdateWithLinks");
        contactObj.setParentFolderID(contactFolderId);
        contactObj.setObjectID(objectId);

        final LinkEntryObject[] links = new LinkEntryObject[1];
        links[0] = new LinkEntryObject();
        links[0].setLinkID(linkId3);
        links[0].setLinkDisplayname(link3.getDisplayName());

        contactObj.setLinks(links);
        contactObj.setLastModified(lastModified);
        contactObj.removeParentFolderID();

        updateContact(contactObj, contactFolderId);
    }

    public void testContactWithImage() throws Exception {
        final Contact contactObj = createContactObject("testContactWithImage");
        contactObj.setImage1(image);
        contactObj.setImageContentType("image/png");
        final int objectId = insertContact(contactObj);

        final GetRequest request = new GetRequest(contactFolderId, objectId, tz);
        final GetResponse response = client.execute(request);
        final String imageUrl = response.getImageUrl();
        if (imageUrl == null) {
            fail("Contact contains no image URL.");
        }

        final byte[] b = loadImageByURL(client.getProtocol(), client.getHostname(), imageUrl);

        OXTestToolkit.assertImageBytesEqualsAndNotNull("image", contactObj.getImage1(), b);
    }

    public void testUpdateContactWithImage() throws Exception {
        final Contact contactObj = createContactObject("testUpdateContactWithImageUpdate");
        final int objectId = insertContact(contactObj);

        contactObj.setImage1(image);
        contactObj.setImageContentType("image/png");
        contactObj.removeParentFolderID();
        updateContact(contactObj, contactFolderId);

        final GetRequest request = new GetRequest(contactFolderId, objectId, tz);
        final GetResponse response = client.execute(request);
        final String imageUrl = response.getImageUrl();
        if (imageUrl == null) {
            fail("Contact contains no image URL.");
        }

        final byte[] b = loadImageByURL(client.getProtocol(), client.getHostname(), imageUrl);

        OXTestToolkit.assertImageBytesEqualsAndNotNull("image", contactObj.getImage1(), b);
    }
}
