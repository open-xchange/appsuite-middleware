
package com.openexchange.ajax.importexport;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.Date;
import org.json.JSONException;
import org.junit.Test;
import com.openexchange.ajax.importexport.actions.ICalExportRequest;
import com.openexchange.ajax.importexport.actions.ICalExportResponse;
import com.openexchange.ajax.task.ManagedTaskTest;
import com.openexchange.exception.OXException;
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
    
    @Test
    public void testSingleExportICalTask() throws OXException, IOException, JSONException {
        final String title = "testSingleTaskToExport" + System.currentTimeMillis();
        final String secondTitle = "testTaskNoExport" + System.currentTimeMillis();
        
        Task taskObj = generateTask(title);
        Task secondTaskObj = generateTask(secondTitle);
        
        taskObj = ttm.insertTaskOnServer(taskObj);
        secondTaskObj = ttm.insertTaskOnServer(secondTaskObj);
        
        int taskId = taskObj.getObjectID();
        
        ICalExportResponse response = getClient().execute(new ICalExportRequest(folderID, String.valueOf(taskId)));
        
        String iCal = response.getICal();
        
        assertTrue(iCal.contains(title));
        assertFalse(iCal.contains(secondTitle));
    }
}
