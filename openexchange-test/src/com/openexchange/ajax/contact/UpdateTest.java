package com.openexchange.ajax.contact;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.ajax.ContactTest;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.groupware.container.LinkEntryObject;
import com.openexchange.test.OXTestToolkit;

public class UpdateTest extends ContactTest {
	
	private static final Log LOG = LogFactory.getLog(UpdateTest.class);
	
	public UpdateTest(final String name) {
		super(name);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testUpdate() throws Exception {
		final Contact contactObj = createContactObject("testUpdate");
		final int objectId = insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getSessionId());
		
		contactObj.setObjectID(objectId);
		
		contactObj.setTelephoneBusiness1("+49009988776655");
		contactObj.setStateBusiness(null);
		contactObj.removeParentFolderID();
		
		updateContact(getWebConversation(), contactObj, objectId, contactFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testUpdateWithDistributionList() throws Exception {
		final Contact contactEntry = createContactObject("internal contact");
		contactEntry.setEmail1("internalcontact@x.de");
		final int contactId = insertContact(getWebConversation(), contactEntry, PROTOCOL + getHostName(), getSessionId());
		contactEntry.setObjectID(contactId);
		
		final int objectId = createContactWithDistributionList("testUpdateWithDistributionList", contactEntry);
		
		final Contact contactObj = new Contact();
		contactObj.setSurName("testUpdateWithDistributionList");
		contactObj.setParentFolderID(contactFolderId);
		contactObj.setObjectID(objectId);
		
		final DistributionListEntryObject[] entry = new DistributionListEntryObject[2];
		entry[0] = new DistributionListEntryObject("displayname a", "a@a.de", DistributionListEntryObject.INDEPENDENT);
		entry[1] = new DistributionListEntryObject("displayname b", "b@b.de", DistributionListEntryObject.INDEPENDENT);
		
		contactObj.setDistributionList(entry);
		
		contactObj.removeParentFolderID();
		
		updateContact(getWebConversation(), contactObj, objectId, contactFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testUpdateWithLinks() throws Exception {
		final Contact link1 = createContactObject("link1");
		final Contact link2 = createContactObject("link2");
		final int linkId1 = insertContact(getWebConversation(), link1, PROTOCOL + getHostName(), getSessionId());
		link1.setObjectID(linkId1);
		final int linkId2 = insertContact(getWebConversation(), link2, PROTOCOL + getHostName(), getSessionId());
		link2.setObjectID(linkId2);
		
		final int objectId = createContactWithLinks("testUpdateWithLinks", link1, link2);
		
		final Contact link3 = createContactObject("link3");
		final int linkId3 = insertContact(getWebConversation(), link3, PROTOCOL + getHostName(), getSessionId());
		
		final Contact contactObj = new Contact();
		contactObj.setSurName("testUpdateWithLinks");
		contactObj.setParentFolderID(contactFolderId);
		contactObj.setObjectID(objectId);
		
		final LinkEntryObject[] links = new LinkEntryObject[1];
		links[0] = new LinkEntryObject();
		links[0].setLinkID(linkId3);
		links[0].setLinkDisplayname(link3.getDisplayName());
		
		contactObj.setLinks(links);
		
		contactObj.removeParentFolderID();
		
		updateContact(getWebConversation(), contactObj, objectId, contactFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testContactWithImage() throws Exception {
		final Contact contactObj = createContactObject("testContactWithImage");
		contactObj.setImage1(image);
		contactObj.setImageContentType("image/png");
		final int objectId = insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getSessionId());
		
		final byte[] b = loadImage(getWebConversation(), objectId, contactFolderId, PROTOCOL + getHostName(), getSessionId());
		
		OXTestToolkit.assertEqualsAndNotNull("image", contactObj.getImage1(), b);
	}
	
	public void testUpdateContactWithImage() throws Exception {
		final Contact contactObj = createContactObject("testUpdateContactWithImageUpdate");
		final int objectId = insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getSessionId());
		
		contactObj.setImage1(image);
		contactObj.removeParentFolderID();
		updateContact(getWebConversation(), contactObj, objectId, contactFolderId, PROTOCOL + getHostName(), getSessionId());
		
		final byte[] b = loadImage(getWebConversation(), objectId, contactFolderId, PROTOCOL + getHostName(), getSessionId());
		
		OXTestToolkit.assertEqualsAndNotNull("image", contactObj.getImage1(), b);
	}
}

