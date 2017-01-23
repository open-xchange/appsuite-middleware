
package com.openexchange.ajax.task;

import java.util.Calendar;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.modules.Module;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.test.TaskTestManager;

public abstract class ManagedTaskTest extends AbstractAJAXSession {

    protected TaskTestManager ttm;
    protected int folderID;
    protected Task actual;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        ttm = new TaskTestManager(getClient());
        folderID = ftm.insertFolderOnServer(ftm.generatePublicFolder("Managed task test folder #" + UUID.randomUUID().toString(), Module.TASK.getFolderConstant(), getClient().getValues().getPrivateTaskFolder(), getClient().getValues().getUserId())).getObjectID();
        actual = null;
    }

    @After
    public void tearDown() throws Exception {
        try {
            ttm.cleanUp();
        } finally {
            super.tearDown();
        }
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
