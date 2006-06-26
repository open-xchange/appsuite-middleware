package com.openexchange.ajax;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

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
        Task task = new Task();
        task.setTitle("Test task");
        insertTask(getWebConversation(), hostName, getSessionId(), task);
    }

    public static int insertTask(final WebConversation wc,
        final String hostname, final String sessionId, final Task task)
        throws Exception {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        TaskWriter tw = new TaskWriter(pw);
        tw.writeTask(task);
        pw.flush();
        ByteArrayInputStream bais = new ByteArrayInputStream(sw.toString()
            .getBytes("UTF-8"));
        URLParameter parameter = new URLParameter();
        parameter.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
        parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_NEW);
        WebRequest req = new PutMethodWebRequest(PROTOCOL + hostname +
            TASKS_URL + parameter.getURLParameters(), bais, AJAXServlet
            .CONTENTTYPE_JAVASCRIPT);
        WebResponse resp = wc.getResponse(req);
        assertEquals(200, resp.getResponseCode());
        JSONObject jsonId = new JSONObject(resp.getText());
        if (jsonId.has("error")) {
            fail(jsonId.getString("error"));
        }
        final int id = jsonId.getInt(OXObject.OBJECT_ID);
        assertTrue(id > 0);
        return id;
    }
}
