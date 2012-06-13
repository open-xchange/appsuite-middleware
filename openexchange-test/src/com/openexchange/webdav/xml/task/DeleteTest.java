package com.openexchange.webdav.xml.task;

import java.util.Date;
import java.util.Locale;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.webdav.xml.TaskTest;
import com.openexchange.webdav.xml.XmlServlet;

public class DeleteTest extends TaskTest {

	public DeleteTest(final String name) {
		super(name);
	}

	public void testDelete() throws Exception {
		final Task taskObj = createTask("testDelete");
		final int objectId1 = insertTask(webCon, taskObj, PROTOCOL + hostName, login, password, context);
		final int objectId2 = insertTask(webCon, taskObj, PROTOCOL + hostName, login, password, context);

		final int[][] objectIdAndFolderId = { { objectId1, taskFolderId }, { objectId2, taskFolderId } };

		deleteTask(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password, context);
	}

	public void testDeleteConcurentConflict() throws Exception {
		final Task appointmentObj = createTask("testUpdateTaskConcurentConflict");
		final int objectId = insertTask(webCon, appointmentObj, PROTOCOL + hostName, login, password, context);

		try {
			deleteTask(webCon, objectId, taskFolderId, new Date(0), PROTOCOL + hostName, login, password, context);
			fail("expected concurent modification exception!");
		} catch (final OXException exc) {
			assertExceptionMessage(exc.getDisplayMessage(Locale.ENGLISH), XmlServlet.MODIFICATION_STATUS);
		}

		deleteTask(webCon, objectId, taskFolderId, PROTOCOL + hostName, login, password, context);
	}

	public void testDeleteNotFound() throws Exception {
		final Task appointmentObj = createTask("testUpdateTaskNotFound");
		final int objectId = insertTask(webCon, appointmentObj, PROTOCOL + hostName, login, password, context);

		try {
			deleteTask(webCon, (objectId + 1000), taskFolderId, PROTOCOL + hostName, login, password, context);
			fail("expected object not found exception!");
		} catch (final OXException exc) {
			assertExceptionMessage(exc.getDisplayMessage(Locale.ENGLISH), XmlServlet.OBJECT_NOT_FOUND_STATUS);
		}

		deleteTask(webCon, objectId, taskFolderId, PROTOCOL + hostName, login, password, context);
	}

}

