
package com.openexchange.webdav.xml.contact;

import static org.junit.Assert.fail;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.webdav.xml.ContactTest;
import com.openexchange.webdav.xml.FolderTest;
import com.openexchange.webdav.xml.XmlServlet;

public class PermissionTest extends ContactTest {

    @Test
    public void testInsertContactInPrivateFolderWithoutPermission() throws Exception {
        final FolderObject folderObj = new FolderObject();
        folderObj.setFolderName("testInsertContactInPrivateFolderWithoutPermission" + System.currentTimeMillis());
        folderObj.setModule(FolderObject.CONTACT);
        folderObj.setType(FolderObject.PRIVATE);
        folderObj.setParentFolderID(1);

        final OCLPermission[] permission = new OCLPermission[] { FolderTest.createPermission(userId, false, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.DELETE_OWN_OBJECTS),
        };

        folderObj.setPermissionsAsArray(permission);

        final int parentFolderId = FolderTest.insertFolder(getWebConversation(), folderObj, getHostURI(), getLogin(), getPassword());

        final Contact contactObj = new Contact();
        contactObj.setSurName("testInsertContactInPrivateFolderWithoutPermission");
        contactObj.setParentFolderID(parentFolderId);

        try {
            insertContact(getSecondWebConversation(), contactObj, getHostURI(), getSecondLogin(), getPassword());
            fail("permission exception expected!");
        } catch (final OXException exc) {
            assertExceptionMessage(exc.getMessage(), XmlServlet.PERMISSION_STATUS);
        }

        FolderTest.deleteFolder(getWebConversation(), new int[] { parentFolderId }, getHostURI(), getLogin(), getPassword());
    }

    @Test
    public void testInsertContactInPublicFolderWithoutPermission() throws Exception {
        final FolderObject folderObj = new FolderObject();
        folderObj.setFolderName("testInsertContactInPublicFolderWithoutPermission" + System.currentTimeMillis());
        folderObj.setModule(FolderObject.CONTACT);
        folderObj.setType(FolderObject.PUBLIC);
        folderObj.setParentFolderID(2);

        final OCLPermission[] permission = new OCLPermission[] { FolderTest.createPermission(userId, false, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.DELETE_OWN_OBJECTS)
        };

        folderObj.setPermissionsAsArray(permission);

        final int parentFolderId = FolderTest.insertFolder(getWebConversation(), folderObj, getHostURI(), getLogin(), getPassword());

        final Contact contactObj = new Contact();
        contactObj.setSurName("testInsertContactInPublicFolderWithoutPermission");
        contactObj.setParentFolderID(parentFolderId);

        try {
            insertContact(getSecondWebConversation(), contactObj, getHostURI(), getSecondLogin(), getPassword());
            fail("permission exception expected!");
        } catch (final OXException exc) {
            assertExceptionMessage(exc.getMessage(), XmlServlet.PERMISSION_STATUS);
        }

        FolderTest.deleteFolder(getWebConversation(), new int[] { parentFolderId }, getHostURI(), getLogin(), getPassword());
    }

    @Test
    public void testUpdateContactInPrivateFolderWithoutPermission() throws Exception {
        final FolderObject folderObj = new FolderObject();
        folderObj.setFolderName("testInsertContactInPrivateFolderWithoutPermission" + System.currentTimeMillis());
        folderObj.setModule(FolderObject.CONTACT);
        folderObj.setType(FolderObject.PRIVATE);
        folderObj.setParentFolderID(1);

        final OCLPermission[] permission = new OCLPermission[] { FolderTest.createPermission(userId, false, OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.DELETE_OWN_OBJECTS),
        };

        folderObj.setPermissionsAsArray(permission);

        final int parentFolderId = FolderTest.insertFolder(getWebConversation(), folderObj, getHostURI(), getLogin(), getPassword());

        final Contact contactObj = new Contact();
        contactObj.setSurName("testUpdateContactInPrivateFolderWithoutPermission");
        contactObj.setParentFolderID(parentFolderId);

        final int contactObjectId = insertContact(getWebConversation(), contactObj, getHostURI(), getLogin(), getPassword());
        contactObj.setObjectID(contactObjectId);

        try {
            updateContact(getSecondWebConversation(), contactObj, contactObjectId, parentFolderId, getHostURI(), getSecondLogin(), getPassword());
            fail("permission exception expected!");
        } catch (final OXException exc) {
            assertExceptionMessage(exc.getMessage(), XmlServlet.PERMISSION_STATUS);
        }

        FolderTest.deleteFolder(getWebConversation(), new int[] { parentFolderId }, getHostURI(), getLogin(), getPassword());
    }

    @Test
    public void testUpdateContactInPublicFolderWithoutPermission() throws Exception {
        final FolderObject folderObj = new FolderObject();
        folderObj.setFolderName("testUpdateContactInPublicFolderWithoutPermission" + System.currentTimeMillis());
        folderObj.setModule(FolderObject.CONTACT);
        folderObj.setType(FolderObject.PUBLIC);
        folderObj.setParentFolderID(2);

        final OCLPermission[] permission = new OCLPermission[] { FolderTest.createPermission(userId, false, OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.DELETE_OWN_OBJECTS)
        };

        folderObj.setPermissionsAsArray(permission);

        final int parentFolderId = FolderTest.insertFolder(getWebConversation(), folderObj, getHostURI(), getLogin(), getPassword());

        final Contact contactObj = new Contact();
        contactObj.setSurName("testUpdateContactInPublicFolderWithoutPermission");
        contactObj.setParentFolderID(parentFolderId);

        final int contactObjectId = insertContact(getWebConversation(), contactObj, getHostURI(), getLogin(), getPassword());
        contactObj.setObjectID(contactObjectId);

        try {
            updateContact(getSecondWebConversation(), contactObj, contactObjectId, parentFolderId, getHostURI(), getSecondLogin(), getPassword());
            fail("permission exception expected!");
        } catch (final OXException exc) {
            assertExceptionMessage(exc.getMessage(), XmlServlet.PERMISSION_STATUS);
        }

        FolderTest.deleteFolder(getWebConversation(), new int[] { parentFolderId }, getHostURI(), getLogin(), getPassword());
    }

    @Test
    public void testDeleteContactInPrivateFolderWithoutPermission() throws Exception {
        final FolderObject folderObj = new FolderObject();
        folderObj.setFolderName("testDeleteContactInPrivateFolderWithoutPermission" + System.currentTimeMillis());
        folderObj.setModule(FolderObject.CONTACT);
        folderObj.setType(FolderObject.PRIVATE);
        folderObj.setParentFolderID(1);

        final OCLPermission[] permission = new OCLPermission[] { FolderTest.createPermission(userId, false, OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.DELETE_OWN_OBJECTS),
        };

        folderObj.setPermissionsAsArray(permission);

        final int parentFolderId = FolderTest.insertFolder(getWebConversation(), folderObj, getHostURI(), getLogin(), getPassword());

        final Contact contactObj = new Contact();
        contactObj.setSurName("testDeleteContactInPrivateFolderWithoutPermission");
        contactObj.setParentFolderID(parentFolderId);

        final int contactObjectId = insertContact(getWebConversation(), contactObj, getHostURI(), getLogin(), getPassword());
        contactObj.setObjectID(contactObjectId);

        try {
            deleteContact(getSecondWebConversation(), contactObjectId, parentFolderId, getHostURI(), getSecondLogin(), getPassword());
            fail("permission exception expected!");
        } catch (final OXException exc) {
            assertExceptionMessage(exc.getMessage(), XmlServlet.PERMISSION_STATUS);
        }

        FolderTest.deleteFolder(getWebConversation(), new int[] { parentFolderId }, getHostURI(), getLogin(), getPassword());
    }

    @Test
    public void testDeleteContactInPublicFolderWithoutPermission() throws Exception {
        final FolderObject folderObj = new FolderObject();
        folderObj.setFolderName("testDeleteContactInPublicFolderWithoutPermission" + System.currentTimeMillis());
        folderObj.setModule(FolderObject.CONTACT);
        folderObj.setType(FolderObject.PUBLIC);
        folderObj.setParentFolderID(2);

        final OCLPermission[] permission = new OCLPermission[] { FolderTest.createPermission(userId, false, OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.DELETE_OWN_OBJECTS)
        };

        folderObj.setPermissionsAsArray(permission);

        final int parentFolderId = FolderTest.insertFolder(getWebConversation(), folderObj, getHostURI(), getLogin(), getPassword());

        final Contact contactObj = new Contact();
        contactObj.setSurName("testDeleteContactInPublicFolderWithoutPermission");
        contactObj.setParentFolderID(parentFolderId);

        final int contactObjectId = insertContact(getWebConversation(), contactObj, getHostURI(), getLogin(), getPassword());
        contactObj.setObjectID(contactObjectId);

        try {
            deleteContact(getSecondWebConversation(), contactObjectId, parentFolderId, getHostURI(), getSecondLogin(), getPassword());
            fail("permission exception expected!");
        } catch (final OXException exc) {
            assertExceptionMessage(exc.getMessage(), XmlServlet.PERMISSION_STATUS);
        }

        FolderTest.deleteFolder(getWebConversation(), new int[] { parentFolderId }, getHostURI(), getLogin(), getPassword());
    }
}
