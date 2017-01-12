
package com.openexchange.ajax.importexport;

import static org.junit.Assert.assertTrue;
import java.util.Date;
import org.junit.Test;
import com.openexchange.ajax.importexport.actions.ICalExportRequest;
import com.openexchange.ajax.importexport.actions.ICalExportResponse;
import com.openexchange.ajax.task.ManagedTaskTest;
import com.openexchange.groupware.tasks.Task;

public class ICalTaskExportTest extends ManagedTaskTest {

    @Test
    public void testExportICalTask() throws Exception {
        final String title = "testExportICalTask" + System.currentTimeMillis();

        final Task taskObj = new Task();
        taskObj.setTitle(title);
        taskObj.setStartDate(new Date());
        taskObj.setEndDate(new Date());
        taskObj.setParentFolderID(folderID);

        ttm.insertTaskOnServer(taskObj);

        ICalExportResponse response = getClient().execute(new ICalExportRequest(folderID));

        String iCal = response.getICal();

        assertTrue(iCal.contains(title));
    }
}
