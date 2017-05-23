
package com.openexchange.webdav.xml.task;

import static org.junit.Assert.fail;
import java.util.Date;
import java.util.Locale;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.webdav.xml.TaskTest;
import com.openexchange.webdav.xml.XmlServlet;

public class DeleteTest extends TaskTest {

    public DeleteTest() {
        super();
    }

    @Test
    public void testDelete() throws Exception {
        final Task taskObj = createTask("testDelete");
        final int objectId1 = insertTask(webCon, taskObj, getHostURI(), login, password);
        final int objectId2 = insertTask(webCon, taskObj, getHostURI(), login, password);

        final int[][] objectIdAndFolderId = { { objectId1, taskFolderId }, { objectId2, taskFolderId } };

        deleteTask(webCon, objectIdAndFolderId, getHostURI(), login, password);
    }

    @Test
    public void testDeleteConcurentConflict() throws Exception {
        final Task appointmentObj = createTask("testUpdateTaskConcurentConflict");
        final int objectId = insertTask(webCon, appointmentObj, getHostURI(), login, password);

        try {
            deleteTask(webCon, objectId, taskFolderId, new Date(0), getHostURI(), login, password);
            fail("expected concurent modification exception!");
        } catch (final OXException exc) {
            assertExceptionMessage(exc.getDisplayMessage(Locale.ENGLISH), XmlServlet.MODIFICATION_STATUS);
        }

        deleteTask(webCon, objectId, taskFolderId, getHostURI(), login, password);
    }

    @Test
    public void testDeleteNotFound() throws Exception {
        final Task appointmentObj = createTask("testUpdateTaskNotFound");
        final int objectId = insertTask(webCon, appointmentObj, getHostURI(), login, password);

        try {
            deleteTask(webCon, (objectId + 1000), taskFolderId, getHostURI(), login, password);
            fail("expected object not found exception!");
        } catch (final OXException exc) {
            assertExceptionMessage(exc.getDisplayMessage(Locale.ENGLISH), XmlServlet.OBJECT_NOT_FOUND_STATUS);
        }

        deleteTask(webCon, objectId, taskFolderId, getHostURI(), login, password);
    }

}
