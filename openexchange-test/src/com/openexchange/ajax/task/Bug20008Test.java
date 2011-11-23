package com.openexchange.ajax.task;

import java.util.TimeZone;
import junit.framework.AssertionFailedError;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.ajax.task.actions.UpdateRequest;
import com.openexchange.ajax.task.actions.UpdateResponse;
import com.openexchange.groupware.tasks.Task;

public class Bug20008Test extends AbstractAJAXSession {
    
    private AJAXClient client;
    private Task task;
    private TimeZone tz;

    public Bug20008Test(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
        tz = client.getValues().getTimeZone();
        task = new Task();
        task.setParentFolderID(client.getValues().getPrivateTaskFolder());
        task.setTitle("Test for bug 20008");
        task.setActualDuration(2L);
        task.setActualCosts(2.0f);
        task.setTargetDuration(10L);
        task.setTargetCosts(10.0f);
        InsertRequest request = new InsertRequest(task, tz);
        InsertResponse response = client.execute(request);
        response.fillTask(task);
    }
    
    @Override
    protected void tearDown() throws Exception {
        DeleteRequest delete = new DeleteRequest(task);
        client.execute(delete);
        super.tearDown();
    }
    
    public void testUpdate() throws Throwable {
        task.setActualDuration(null);
        task.setTargetDuration(null);
        task.setActualCosts(null);
        task.setTargetCosts(null);
        UpdateRequest req = new UpdateRequest(task, tz, false);
        try {
            UpdateResponse response = client.execute(req);
            task.setLastModified(response.getTimestamp());
        } catch (AssertionFailedError e) {
            fail("Deleting task attributes actualDuration, targetDuration, actualCosts, targetCosts failed!");
        }
        GetRequest request = new GetRequest(task);
        GetResponse response = client.execute(request);
        task.setLastModified(response.getTimestamp());
        Task test = response.getTask(tz);
        assertNull("Actual duration should not be set.", test.getActualDuration());
        assertNull("Target duration should not be set.", test.getTargetDuration());
        assertNull("Actual costs should not be set.", test.getActualCosts());
        assertNull("Target costs should not be set.", test.getTargetCosts());
    }

}
