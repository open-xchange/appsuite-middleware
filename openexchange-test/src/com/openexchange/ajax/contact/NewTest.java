package com.openexchange.ajax.contact;

import java.io.ByteArrayInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.ajax.ContactTest;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.impl.AttachmentImpl;
import com.openexchange.groupware.container.Contact;

public class NewTest extends ContactTest {
	
	private static final Log LOG = LogFactory.getLog(NewTest.class);
	
	public NewTest(final String name) {
		super(name);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testNew() throws Exception {
		final Contact contactObj = createContactObject("testNew");
		final int objectId = insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testNewWithDistributionList() throws Exception {
		final Contact contactEntry = createContactObject("internal contact");
		contactEntry.setEmail1("internalcontact@x.de");
		final int contactId = insertContact(getWebConversation(), contactEntry, PROTOCOL + getHostName(), getSessionId());
		contactEntry.setObjectID(contactId);
		
		final int objectId = createContactWithDistributionList("testNewWithDistributionList", contactEntry);
	}
	
	public void testNewWithLinks() throws Exception {
		final Contact link1 = createContactObject("link1");
		final Contact link2 = createContactObject("link2");
		final int linkId1 = insertContact(getWebConversation(), link1, PROTOCOL + getHostName(), getSessionId());
		link1.setObjectID(linkId1);
		final int linkId2 = insertContact(getWebConversation(), link2, PROTOCOL + getHostName(), getSessionId());
		link2.setObjectID(linkId2);
		
		final int objectId = createContactWithLinks("testNewWithLinks", link1, link2);
	}
	
	public void testNewContactWithAttachment() throws Exception {
		final Contact contactObj = createContactObject("testNewContactWithAttachment");
		final int objectId = insertContact(getWebConversation(), contactObj, getHostName(), getSessionId());
		contactObj.setNumberOfAttachments(2);
		contactObj.setObjectID(objectId);
		
		final AttachmentMetadata attachmentObj = new AttachmentImpl();
		attachmentObj.setFilename(System.currentTimeMillis() + "test1.txt");
		attachmentObj.setModuleId(Types.CONTACT);
		attachmentObj.setAttachedId(objectId);
		attachmentObj.setFolderId(contactFolderId);
		attachmentObj.setRtfFlag(false);
		attachmentObj.setFileMIMEType("plain/text");
		
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream("t1".getBytes());
		
		final int attachmentId1 = com.openexchange.webdav.xml.AttachmentTest.insertAttachment(getWebConversation(), attachmentObj, byteArrayInputStream, getHostName(), getLogin(), getPassword());
		
		byteArrayInputStream = new ByteArrayInputStream("t2".getBytes());
		final int attachmentId2 = com.openexchange.webdav.xml.AttachmentTest.insertAttachment(getWebConversation(), attachmentObj, byteArrayInputStream, getHostName(), getLogin(), getPassword());
		
		final Contact loadContact = ContactTest.loadContact(getWebConversation(), objectId, contactFolderId, getHostName(), getSessionId());
		compareObject(contactObj, loadContact);	
	}
}

