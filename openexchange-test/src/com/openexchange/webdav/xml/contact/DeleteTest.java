
package com.openexchange.webdav.xml.contact;

import static org.junit.Assert.fail;
import java.util.Date;
import java.util.Locale;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.webdav.xml.ContactTest;
import com.openexchange.webdav.xml.XmlServlet;

public class DeleteTest extends ContactTest {

    public DeleteTest() {
        super();
    }

    @Test
    public void testDelete() throws Exception {
        final Contact contactObj = createContactObject("testDelete");
        final int objectId1 = insertContact(webCon, contactObj, getHostURI(), login, password);
        final int objectId2 = insertContact(webCon, contactObj, getHostURI(), login, password);

        final int[][] objectIdAndFolderId = { { objectId1, contactFolderId }, { objectId2, contactFolderId } };

        deleteContact(webCon, objectIdAndFolderId, getHostURI(), login, password);
    }

    @Test
    public void testDeleteConcurentConflict() throws Exception {
        final Contact contactObj = createContactObject("testDeleteConcurentConflict");
        final int objectId = insertContact(webCon, contactObj, getHostURI(), login, password);

        try {
            deleteContact(webCon, objectId, contactFolderId, new Date(1), getHostURI(), login, password);
            fail("expected concurent modification exception!");
        } catch (final OXException exc) {
            assertExceptionMessage(exc.getDisplayMessage(Locale.ENGLISH), XmlServlet.MODIFICATION_STATUS);
        }

        deleteContact(webCon, objectId, contactFolderId, getHostURI(), login, password);
    }

    @Test
    public void testDeleteNotFound() throws Exception {
        final Contact contactObj = createContactObject("testUpdateContactNotFound");
        final int objectId = insertContact(webCon, contactObj, getHostURI(), login, password);

        try {
            deleteContact(webCon, (objectId + 1000), contactFolderId, getHostURI(), login, password);
            fail("expected object not found exception!");
        } catch (final OXException exc) {
            assertExceptionMessage(exc.getDisplayMessage(Locale.ENGLISH), XmlServlet.OBJECT_NOT_FOUND_STATUS);
        }

        deleteContact(webCon, objectId, contactFolderId, getHostURI(), login, password);
    }
}
