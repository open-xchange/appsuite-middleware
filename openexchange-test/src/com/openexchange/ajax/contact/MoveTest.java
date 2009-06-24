package com.openexchange.ajax.contact;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.ajax.ContactTest;
import com.openexchange.groupware.configuration.AbstractConfigWrapper;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;

public class MoveTest extends ContactTest {

	private static final Log LOG = LogFactory.getLog(MoveTest.class);
	
	public MoveTest(final String name) {
		super(name);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testMove2PrivateFolder() throws Exception {
		final Contact contactObj = new Contact();
		contactObj.setSurName("testMove2PrivateFolder");
		contactObj.setParentFolderID(contactFolderId);
		final int objectId = insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getSessionId());
		
		final String login = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "login", "");
		final String password = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "password", "");
		
		final FolderObject folderObj = com.openexchange.webdav.xml.FolderTest.createFolderObject(userId, "testMove2PrivateFolder" + System.currentTimeMillis(), FolderObject.CONTACT, false);
		final int targetFolder = com.openexchange.webdav.xml.FolderTest.insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), login, password);

		contactObj.setParentFolderID(targetFolder);
		updateContact(getWebConversation(), contactObj, objectId, contactFolderId, PROTOCOL + getHostName(), getSessionId());
		final Contact loadContact = loadContact(getWebConversation(), objectId, targetFolder, PROTOCOL + getHostName(), getSessionId());
		contactObj.setObjectID(objectId);
		compareObject(contactObj, loadContact);
		
		deleteContact(getWebConversation(), objectId, targetFolder, PROTOCOL + getHostName(), getSessionId());
		//com.openexchange.webdav.xml.FolderTest.deleteFolder(getWebConversation(), new int[] { targetFolder }, PROTOCOL + getHostName(), login, password);
	}	
	
	public void testMove2PublicFolder() throws Exception {
		final Contact contactObj = new Contact();
		contactObj.setSurName("testMove2PublicFolder");
		contactObj.setParentFolderID(contactFolderId);
		final int objectId = insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getSessionId());
		
		final String login = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "login", "");
		final String password = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "password", "");
		
		final FolderObject folderObj = com.openexchange.webdav.xml.FolderTest.createFolderObject(userId, "testMove2PublicFolder" + System.currentTimeMillis(), FolderObject.CONTACT, true);
		final int targetFolder = com.openexchange.webdav.xml.FolderTest.insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), login, password);

		contactObj.setParentFolderID(targetFolder);
		updateContact(getWebConversation(), contactObj, objectId, contactFolderId, PROTOCOL + getHostName(), getSessionId());
		final Contact loadContact = loadContact(getWebConversation(), objectId, targetFolder, PROTOCOL + getHostName(), getSessionId());
		contactObj.setObjectID(objectId);
		compareObject(contactObj, loadContact);
		
		deleteContact(getWebConversation(), objectId, targetFolder, PROTOCOL + getHostName(), getSessionId());
		//com.openexchange.webdav.xml.FolderTest.deleteFolder(getWebConversation(), new int[] { targetFolder }, PROTOCOL + getHostName(), login, password);
	}
}

