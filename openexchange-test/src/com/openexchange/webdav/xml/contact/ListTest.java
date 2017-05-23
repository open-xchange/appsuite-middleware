
package com.openexchange.webdav.xml.contact;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.Date;
import java.util.Locale;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.webdav.xml.ContactTest;
import com.openexchange.webdav.xml.XmlServlet;

public class ListTest extends ContactTest {

    public ListTest() {
        super();
    }

    @Test
    public void testPropFindWithModified() throws Exception {
        final Contact contactObj = createContactObject("testPropFindWithModified");
        final int objectId1 = insertContact(webCon, contactObj, getHostURI(), login, password);
        final int objectId2 = insertContact(webCon, contactObj, getHostURI(), login, password);

        // prevent master/slave problem
        Thread.sleep(1000);

        final Contact loadContact = loadContact(getWebConversation(), objectId1, contactFolderId, getHostURI(), getLogin(), getPassword());
        final Date modified = loadContact.getLastModified();

        final Contact[] contactArray = listContact(webCon, contactFolderId, decrementDate(modified), true, false, getHostURI(), login, password);

        assertTrue("check response", contactArray.length >= 2);
        deleteContact(getWebConversation(), objectId1, contactFolderId, getHostURI(), getLogin(), getPassword());
        deleteContact(getWebConversation(), objectId2, contactFolderId, getHostURI(), getLogin(), getPassword());
    }

    @Test
    public void testPropFindWithDelete() throws Exception {
        final Contact contactObj = createContactObject("testPropFindWithModified");
        final int objectId1 = insertContact(webCon, contactObj, getHostURI(), login, password);
        final int objectId2 = insertContact(webCon, contactObj, getHostURI(), login, password);

        final int[][] objectIdAndFolderId = { { objectId1, contactFolderId }, { objectId2, contactFolderId } };

        final Contact loadContact = loadContact(getWebConversation(), objectId1, contactFolderId, getHostURI(), getLogin(), getPassword());
        final Date modified = loadContact.getLastModified();

        deleteContact(webCon, objectIdAndFolderId, getHostURI(), login, password);

        // prevent master/slave problem
        Thread.sleep(1000);

        final Contact[] appointmentArray = listContact(webCon, contactFolderId, decrementDate(modified), false, true, getHostURI(), login, password);

        assertTrue("wrong response array length", appointmentArray.length >= 2);
    }

    @Test
    public void testPropFindWithObjectId() throws Exception {
        final Contact contactObj = createContactObject("testPropFindWithObjectId");
        final int objectId = insertContact(webCon, contactObj, getHostURI(), login, password);

        final Contact loadContact = loadContact(webCon, objectId, contactFolderId, getHostURI(), login, password);

        contactObj.setObjectID(objectId);
        compareObject(contactObj, loadContact);
        deleteContact(getWebConversation(), objectId, contactFolderId, getHostURI(), getLogin(), getPassword());
    }

    @Test
    public void testObjectNotFound() throws Exception {
        final Contact contactObj = createContactObject("testObjectNotFound");
        final int objectId = insertContact(webCon, contactObj, getHostURI(), login, password);

        try {
            loadContact(webCon, (objectId + 1000), contactFolderId, getHostURI(), login, password);
            fail("object not found exception expected!");
        } catch (final OXException exc) {
            assertExceptionMessage(exc.getDisplayMessage(Locale.ENGLISH), XmlServlet.OBJECT_NOT_FOUND_STATUS);
        }

        final int[][] objectIdAndFolderId = { { objectId, contactFolderId } };
        deleteContact(webCon, objectIdAndFolderId, getHostURI(), login, password);
    }

    @Test
    public void testListWithAllFields() throws Exception {
        final Contact contactObj = createCompleteContactObject();

        final int objectId = insertContact(webCon, contactObj, getHostURI(), login, password);

        // prevent master/slave problem
        Thread.sleep(1000);

        Contact loadContact = loadContact(getWebConversation(), objectId, contactFolderId, getHostURI(), getLogin(), getPassword());
        final Date modified = loadContact.getLastModified();

        final Contact[] appointmentArray = listContact(webCon, contactFolderId, decrementDate(modified), true, false, getHostURI(), login, password);

        assertTrue("wrong response array length", appointmentArray.length >= 1);

        loadContact = appointmentArray[0];
        contactObj.setObjectID(objectId);

        compareObject(contactObj, loadContact);
        deleteContact(getWebConversation(), objectId, contactFolderId, getHostURI(), getLogin(), getPassword());
    }

    @Test
    public void testList() throws Exception {
        final Contact contactObj = createContactObject("testObjectNotFound");
        final int objectId = insertContact(webCon, contactObj, getHostURI(), login, password);

        final int[] idArray = listContact(getWebConversation(), contactFolderId, getHostURI(), getLogin(), getPassword());

        boolean found = false;
        for (int a = 0; a < idArray.length; a++) {
            if (idArray[a] == objectId) {
                found = true;
                break;
            }
        }

        assertTrue("id " + objectId + " not found in response", found);
        deleteContact(getWebConversation(), objectId, contactFolderId, getHostURI(), getLogin(), getPassword());
    }
}
