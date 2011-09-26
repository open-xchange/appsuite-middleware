package com.openexchange.ajax.task;

import java.util.Date;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.test.TaskTestManager;

public abstract class ManagedTaskTest extends AbstractAJAXSession {

	protected TaskTestManager manager;
	protected int folderID;
	protected Task actual;

	public ManagedTaskTest(String name) {
		super(name);
	}

	@Override
	public void setUp() throws Exception {
	    super.setUp();
	    manager = new TaskTestManager(getClient());
	    folderID = getClient().getValues().getPrivateTaskFolder();
	    actual = null;
	}

	@Override
	public void tearDown() throws Exception {
	    manager.cleanUp();
	    super.tearDown();
	}

	public Task generateTask(String title) {
	    Task task = new Task();
	    task.setParentFolderID(folderID);
	    task.setTitle(title);
	    task.setStartDate(new Date());
	    task.setEndDate(new Date());
	    return task;
	}

}
