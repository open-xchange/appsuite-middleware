
package com.openexchange.webdav.xml.task;

import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import org.junit.Test;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.impl.AttachmentImpl;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.webdav.xml.AttachmentTest;
import com.openexchange.webdav.xml.TaskTest;

public class Bug10991Test extends TaskTest {

    @Test
    public void testBug10991() throws Exception {
        final Task taskObj = createTask("testBug10991");
        final int objectId = insertTask(webCon, taskObj, getHostURI(), login, password);
        taskObj.setObjectID(objectId);

        final AttachmentMetadata attachmentMeta = new AttachmentImpl();
        attachmentMeta.setAttachedId(objectId);
        attachmentMeta.setFolderId(taskFolderId);
        attachmentMeta.setFileMIMEType("text/plain");
        attachmentMeta.setModuleId(Types.TASK);
        attachmentMeta.setFilename("test.txt");

        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream("test".getBytes());
        AttachmentTest.insertAttachment(webCon, attachmentMeta, byteArrayInputStream, getHostURI(), getLogin(), getPassword());

        final Task loadTask = loadTask(getWebConversation(), objectId, taskFolderId, getHostURI(), getLogin(), getPassword());
        final Task[] taskArray = listTask(getWebConversation(), taskFolderId, decrementDate(loadTask.getLastModified()), true, false, getHostURI(), getLogin(), getPassword());

        boolean found = false;
        for (int a = 0; a < taskArray.length; a++) {
            if (taskArray[a].getObjectID() == objectId) {
                compareObject(taskObj, taskArray[a]);
                found = true;
            }
        }

        assertTrue("task not found", found);
    }
}
