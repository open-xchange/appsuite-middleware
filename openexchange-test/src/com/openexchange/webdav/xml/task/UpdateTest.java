
package com.openexchange.webdav.xml.task;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.Date;
import java.util.Locale;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.webdav.xml.GroupUserTest;
import com.openexchange.webdav.xml.TaskTest;
import com.openexchange.webdav.xml.XmlServlet;

public class UpdateTest extends TaskTest {

    public UpdateTest() {
        super();
    }

    @Test
    public void testUpdateTask() throws Exception {
        Task taskObj = createTask("testUpdateTask");
        final int objectId = insertTask(webCon, taskObj, getHostURI(), login, password);

        taskObj = createTask("testUpdateTask2");
        taskObj.setNote(null);

        updateTask(webCon, taskObj, objectId, taskFolderId, getHostURI(), login, password);
        deleteTask(getWebConversation(), objectId, taskFolderId, getHostURI(), getLogin(), getPassword());
    }

    @Test
    public void testUpdateTaskRemoveAlarm() throws Exception {
        Task taskObj = createTask("testUpdateTaskRemoveAlarm");
        taskObj.setAlarm(new Date(startTime.getTime() - (2 * dayInMillis)));
        final int objectId = insertTask(webCon, taskObj, getHostURI(), login, password);

        taskObj = createTask("testUpdateTaskRemoveAlarm2");
        taskObj.setNote(null);
        taskObj.setAlarmFlag(false);

        updateTask(webCon, taskObj, objectId, taskFolderId, getHostURI(), login, password);
        final Task loadTask = loadTask(getWebConversation(), objectId, taskFolderId, getHostURI(), getLogin(), getPassword());
        compareObject(taskObj, loadTask);
        deleteTask(getWebConversation(), objectId, taskFolderId, getHostURI(), getLogin(), getPassword());
    }

    @Test
    public void testUpdateTaskWithParticipants() throws Exception {
        Task taskObj = createTask("testUpdateTask");
        final int objectId = insertTask(webCon, taskObj, getHostURI(), login, password);

        taskObj = createTask("testUpdateTask");

        final int userParticipantId = GroupUserTest.getUserId(getWebConversation(), getHostURI(), userParticipant3, getPassword());
        assertTrue("user participant not found", userParticipantId != -1);
        final Group[] groupArray = GroupUserTest.searchGroup(webCon, groupParticipant, new Date(0), getHostURI(), login, password);
        assertTrue("group array size is not > 0", groupArray.length > 0);
        final int groupParticipantId = groupArray[0].getIdentifier();

        final com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[2];
        participants[0] = new UserParticipant();
        participants[0].setIdentifier(userParticipantId);
        participants[1] = new GroupParticipant(groupParticipantId);

        taskObj.setParticipants(participants);

        updateTask(webCon, taskObj, objectId, taskFolderId, getHostURI(), login, password);
        final Task loadTask = loadTask(getWebConversation(), objectId, taskFolderId, getHostURI(), getLogin(), getPassword());
        compareObject(taskObj, loadTask);
        deleteTask(getWebConversation(), objectId, taskFolderId, getHostURI(), getLogin(), getPassword());
    }

    @Test
    public void testUpdateConcurentConflict() throws Exception {
        Task taskObj = createTask("testUpdateTaskConcurentConflict");
        final int objectId = insertTask(webCon, taskObj, getHostURI(), login, password);

        taskObj = createTask("testUpdateTaskConcurentConflict2");

        try {
            updateTask(webCon, taskObj, objectId, taskFolderId, new Date(0), getHostURI(), login, password);
            fail("expected concurent modification exception!");
        } catch (final OXException exc) {
            assertExceptionMessage(exc.getDisplayMessage(Locale.ENGLISH), XmlServlet.MODIFICATION_STATUS);
        }

        final int[][] objectIdAndFolderId = { { objectId, taskFolderId } };
        deleteTask(webCon, objectIdAndFolderId, getHostURI(), login, password);
    }

    @Test
    public void testUpdateNotFound() throws Exception {
        Task taskObj = createTask("testUpdateTaskNotFound");
        final int objectId = insertTask(webCon, taskObj, getHostURI(), login, password);

        taskObj = createTask("testUpdateTaskNotFound2");

        try {
            updateTask(webCon, taskObj, (objectId + 1000), taskFolderId, new Date(0), getHostURI(), login, password);
            fail("expected object not found exception!");
        } catch (final OXException exc) {
            assertExceptionMessage(exc.getDisplayMessage(Locale.ENGLISH), XmlServlet.OBJECT_NOT_FOUND_STATUS);
        }

        final int[][] objectIdAndFolderId = { { objectId, taskFolderId } };
        deleteTask(webCon, objectIdAndFolderId, getHostURI(), login, password);
    }
}
