/**
 * 
 */
package com.openexchange.ajax.attach;

import java.util.Date;

import com.openexchange.ajax.TasksTest;
import com.openexchange.ajax.container.Response;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.tasks.Task;

/**
 * @author marcus
 *
 */
public class TaskAttachmentTest extends AbstractAttachmentTest {

    private Date lastModified;

    /**
     * @see com.openexchange.ajax.attach.AbstractAttachmentTest#createExclusiveWritableAttachable(java.lang.String, int)
     */
    @Override
    public int createExclusiveWritableAttachable(final String sessionId,
        final int folderId) throws Exception {
        final Task task = new Task();
        task.setTitle("AttachmentTest");
        task.setParentFolderID(folderId);
        final int taskId = TasksTest.insertTask(getWebConversation(),
            getHostName(), sessionId, task);
        final Response response = TasksTest.getTask(getWebConversation(),
            getHostName(), sessionId, folderId, taskId);
        lastModified = response.getTimestamp();
        return taskId;
    }

    /**
     * @see com.openexchange.ajax.attach.AbstractAttachmentTest#getExclusiveWritableFolder(java.lang.String)
     */
    @Override
    public int getExclusiveWritableFolder(final String sessionId) throws Exception {
        return TasksTest.getPrivateTaskFolder(getWebConversation(),
            getHostName(), sessionId);
    }

    /**
     * @see com.openexchange.ajax.attach.AbstractAttachmentTest#getModule()
     */
    @Override
    public int getModule() throws Exception {
        return Types.TASK;
    }

    /* (non-Javadoc)
     * @see com.openexchange.ajax.attach.AbstractAttachmentTest#removeAttachable(int, int, java.lang.String)
     */
    @Override
    public void removeAttachable(final int folder, final int id,
        final String sessionId) throws Exception {
        TasksTest.deleteTasks(getWebConversation(), getHostName(), sessionId,
            lastModified, new int[][] {{ folder, id }});
    }

    public void testForbidden() throws Throwable {
        doForbidden();
    }

    public void testAttach() throws Throwable {
        doGet();
    }
}
