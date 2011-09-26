package com.openexchange.ajax.importexport;

import java.util.Date;
import com.openexchange.ajax.importexport.actions.ICalExportRequest;
import com.openexchange.ajax.importexport.actions.ICalExportResponse;
import com.openexchange.ajax.task.ManagedTaskTest;
import com.openexchange.groupware.tasks.Task;

public class ICalTaskExportTest extends ManagedTaskTest {

	public ICalTaskExportTest(String name) {
		super(name);
	}

	public void testExportICalTask() throws Exception {
		final String title = "testExportICalTask" + System.currentTimeMillis();


		final Task taskObj = new Task();
		taskObj.setTitle(title);
		taskObj.setStartDate(new Date());
		taskObj.setEndDate(new Date());
		taskObj.setParentFolderID(folderID);

		manager.insertTaskOnServer(taskObj);

		ICalExportResponse response = client.execute( new ICalExportRequest(folderID) );

		String iCal = response.getICal();

		assertTrue(iCal.contains(title));
	}
}
