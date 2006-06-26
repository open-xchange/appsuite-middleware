package com.openexchange.ajax;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import org.json.JSONObject;

import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.writer.TaskWriter;
import com.openexchange.api.OXObject;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.tools.URLParameter;

public class TasksTest extends AbstractAJAXTest {

    private static final String TASKS_URL = "/ajax/tasks";
    
    /**
     * Test method for 'com.openexchange.ajax.Tasks.doPut(HttpServletRequest, HttpServletResponse)'
     */
    public void testInsertTask() throws Throwable {
        final Task task = new Task();
        task.setTitle("Test task");
        task.setParentFolderID(62);
        assertTrue(insertTask(getWebConversation(), hostName, getSessionId(),
            task) > 0);
    }

    /**
     * This method implements storing of a task through the AJAX interface.
     * @param wc WebConversation.
     * @param hostname Host name of the server.
     * @param sessionId Session identifier of the user.
     * @param task Task to store.
     * @return the unique identifer of the task.
     * @throws Exception if an error occurs while storing the task.
     */
    public static int insertTask(final WebConversation wc,
        final String hostname, final String sessionId, final Task task)
        throws Exception {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        final TaskWriter tw = new TaskWriter(pw);
        tw.writeTask(task);
        pw.flush();
        String object = sw.toString();
        System.out.println(object);
        final ByteArrayInputStream bais = new ByteArrayInputStream(object
            .getBytes("UTF-8"));
        final URLParameter parameter = new URLParameter();
        parameter.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
        parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_NEW);
        final WebRequest req = new PutMethodWebRequest(PROTOCOL + hostname +
            TASKS_URL + parameter.getURLParameters(), bais, AJAXServlet
            .CONTENTTYPE_JAVASCRIPT);
        final WebResponse resp = wc.getResponse(req);
        assertEquals("Response code is not okay.", 200, resp.getResponseCode());
        String text = resp.getText();
        System.out.println("!" + text + "!");
        final JSONObject jsonId = new JSONObject(text);
        if (jsonId.has("error")) {
            fail(jsonId.getString("error"));
        }
        final int identifier = jsonId.getInt(OXObject.OBJECT_ID);
        assertTrue("Unique identifier of task is zero.", identifier > 0);
        return identifier;
    }
}
