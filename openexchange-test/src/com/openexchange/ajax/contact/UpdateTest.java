package com.openexchange.ajax.contact;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.ajax.ContactTest;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.groupware.container.LinkEntryObject;
import com.openexchange.test.OXTestToolkit;

public class UpdateTest extends ContactTest {
	
	private static final Log LOG = LogFactory.getLog(UpdateTest.class);
	
	public UpdateTest(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testUpdate() throws Exception {
		ContactObject contactObj = createContactObject("testUpdate");
		int objectId = insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getSessionId());
		
		contactObj.setObjectID(objectId);
		
		contactObj.setTelephoneBusiness1("+49009988776655");
		contactObj.setStateBusiness(null);
		contactObj.removeParentFolderID();
		
		updateContact(getWebConversation(), contactObj, objectId, contactFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testUpdateWithDistributionList() throws Exception {
		ContactObject contactEntry = createContactObject("internal contact");
		contactEntry.setEmail1("internalcontact@x.de");
		int contactId = insertContact(getWebConversation(), contactEntry, PROTOCOL + getHostName(), getSessionId());
		contactEntry.setObjectID(contactId);
		
		int objectId = createContactWithDistributionList("testUpdateWithDistributionList", contactEntry);
		
		ContactObject contactObj = new ContactObject();
		contactObj.setSurName("testUpdateWithDistributionList");
		contactObj.setParentFolderID(contactFolderId);
		contactObj.setObjectID(objectId);
		
		DistributionListEntryObject[] entry = new DistributionListEntryObject[2];
		entry[0] = new DistributionListEntryObject("displayname a", "a@a.de", DistributionListEntryObject.INDEPENDENT);
		entry[1] = new DistributionListEntryObject("displayname b", "b@b.de", DistributionListEntryObject.INDEPENDENT);
		
		contactObj.setDistributionList(entry);
		
		contactObj.removeParentFolderID();
		
		updateContact(getWebConversation(), contactObj, objectId, contactFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testUpdateWithLinks() throws Exception {
		ContactObject link1 = createContactObject("link1");
		ContactObject link2 = createContactObject("link2");
		int linkId1 = insertContact(getWebConversation(), link1, PROTOCOL + getHostName(), getSessionId());
		link1.setObjectID(linkId1);
		int linkId2 = insertContact(getWebConversation(), link2, PROTOCOL + getHostName(), getSessionId());
		link2.setObjectID(linkId2);
		
		int objectId = createContactWithLinks("testUpdateWithLinks", link1, link2);
		
		ContactObject link3 = createContactObject("link3");
		int linkId3 = insertContact(getWebConversation(), link3, PROTOCOL + getHostName(), getSessionId());
		
		ContactObject contactObj = new ContactObject();
		contactObj.setSurName("testUpdateWithLinks");
		contactObj.setParentFolderID(contactFolderId);
		contactObj.setObjectID(objectId);
		
		LinkEntryObject[] links = new LinkEntryObject[1];
		links[0] = new LinkEntryObject();
		links[0].setLinkID(linkId3);
		links[0].setLinkDisplayname(link3.getDisplayName());
		
		contactObj.setLinks(links);
		
		contactObj.removeParentFolderID();
		
		updateContact(getWebConversation(), contactObj, objectId, contactFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testContactWithImage() throws Exception {
		ContactObject contactObj = createContactObject("testContactWithImage");
		contactObj.setImage1(image);
		contactObj.setImageContentType("image/png");
		int objectId = insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getSessionId());
		
		byte[] b = loadImage(getWebConversation(), objectId, contactFolderId, PROTOCOL + getHostName(), getSessionId());
		
		OXTestToolkit.assertEqualsAndNotNull("image", contactObj.getImage1(), b);
	}
	
	public void testUpdateContactWithImage() throws Exception {
		ContactObject contactObj = createContactObject("testUpdateContactWithImageUpdate");
		int objectId = insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getSessionId());
		
		contactObj.setImage1(image);
		contactObj.removeParentFolderID();
		updateContact(getWebConversation(), contactObj, objectId, contactFolderId, PROTOCOL + getHostName(), getSessionId());
		
		byte[] b = loadImage(getWebConversation(), objectId, contactFolderId, PROTOCOL + getHostName(), getSessionId());
		
		OXTestToolkit.assertEqualsAndNotNull("image", contactObj.getImage1(), b);
	}
}

