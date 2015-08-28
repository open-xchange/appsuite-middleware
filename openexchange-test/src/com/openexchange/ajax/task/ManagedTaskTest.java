package com.openexchange.ajax.task;

import java.util.Calendar;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.modules.Module;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.test.FolderTestManager;
import com.openexchange.test.TaskTestManager;

public abstract class ManagedTaskTest extends AbstractAJAXSession {

	protected TaskTestManager manager;
	protected int folderID;
	protected Task actual;
	private FolderTestManager fManager;

	public ManagedTaskTest(String name) {
		super(name);
	}

	@Override
	public void setUp() throws Exception {
	    super.setUp();
	    manager = new TaskTestManager(getClient());
	    fManager = new FolderTestManager(getClient());
	    folderID = fManager.insertFolderOnServer(
	    	fManager.generatePublicFolder(
	    			"Managed task test folder #"+System.currentTimeMillis(),
	    			Module.TASK.getFolderConstant(),
	    			getClient().getValues().getPrivateTaskFolder(),
	    			getClient().getValues().getUserId())
	    	).getObjectID();
	    actual = null;
	}

	@Override
	public void tearDown() throws Exception {
	    manager.cleanUp();
	    fManager.cleanUp();
	    super.tearDown();
	}

	public Task generateTask(String title) {
	    Task task = new Task();
	    task.setParentFolderID(folderID);
	    task.setTitle(title);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(TimeTools.D("next friday at 00:00"));
	    task.setStartDate(calendar.getTime());
	    calendar.add(Calendar.DAY_OF_YEAR, 2);
	    task.setEndDate(calendar.getTime());
	    return task;
	}

}
