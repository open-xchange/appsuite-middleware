package com.openexchange.webdav.xml.task;

import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.webdav.xml.TaskTest;

public class ConfirmTest extends TaskTest {
	
	public void testDummy() {
		
	}
	
	public void _notestConfirm() throws Exception {
		Task taskObj = createTask("testConfirm");
		int objectId = insertTask(webCon, taskObj, PROTOCOL + hostName, login, password);
		confirmTask(webCon, objectId, CalendarObject.DECLINE, null, PROTOCOL + hostName, login, password);
	}
	
}

