/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.task;

import static org.junit.Assert.assertEquals;
import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.TimeZone;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.attach.actions.AttachRequest;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.framework.ListIDs;
import com.openexchange.ajax.task.actions.AllRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.ListRequest;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.test.common.groupware.tasks.Create;

/**
 * Attachment tests for tasks.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class TaskAttachmentTests extends AbstractAJAXSession {

    private int folderId;

    private TimeZone tz;

    private Task task;

    private int attachmentId;

    private Date creationDate;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        folderId = getClient().getValues().getPrivateTaskFolder();
        tz = getClient().getValues().getTimeZone();
        task = Create.createWithDefaults(folderId, "Test task for testing attachments");
        getClient().execute(new InsertRequest(task, tz)).fillTask(task);
        attachmentId = getClient().execute(new AttachRequest(task, "test.txt", new ByteArrayInputStream("Test".getBytes()), "text/plain")).getId();
        com.openexchange.ajax.attach.actions.GetResponse response = getClient().execute(new com.openexchange.ajax.attach.actions.GetRequest(task, attachmentId));
        long timestamp = response.getAttachment().getCreationDate().getTime();
        creationDate = new Date(timestamp - tz.getOffset(timestamp));
    }

    @Test
    public void testLastModifiedOfNewestAttachmentWithGet() throws Throwable {
        GetResponse response = getClient().execute(new GetRequest(task.getParentFolderID(), task.getObjectID()));
        task.setLastModified(response.getTimestamp());
        Task test = response.getTask(tz);
        assertEquals("Creation date of attachment does not match.", creationDate, test.getLastModifiedOfNewestAttachment());
    }

    @Test
    public void testLastModifiedOfNewestAttachmentWithAll() throws Throwable {
        CommonAllResponse response = getClient().execute(new AllRequest(task.getParentFolderID(), new int[] { Task.OBJECT_ID, Task.LAST_MODIFIED_OF_NEWEST_ATTACHMENT }, Task.OBJECT_ID, Order.ASCENDING));
        task.setLastModified(response.getTimestamp());
        Task test = null;
        int objectIdPos = response.getColumnPos(Task.OBJECT_ID);
        int lastModifiedOfNewestAttachmentPos = response.getColumnPos(Task.LAST_MODIFIED_OF_NEWEST_ATTACHMENT);
        for (Object[] objA : response) {
            if (task.getObjectID() == ((Integer) objA[objectIdPos]).intValue()) {
                test = new Task();
                test.setLastModifiedOfNewestAttachment(new Date(((Long) objA[lastModifiedOfNewestAttachmentPos]).longValue()));
                break;
            }
        }
        Assert.assertNotNull("Can not find the created task with an attachment.", test);
        assertEquals("Creation date of attachment does not match.", creationDate, test.getLastModifiedOfNewestAttachment());
    }

    @Test
    public void testLastModifiedOfNewestAttachmentWithList() throws Throwable {
        CommonListResponse response = getClient().execute(new ListRequest(ListIDs.l(new int[] { task.getParentFolderID(), task.getObjectID() }), new int[] { Task.OBJECT_ID, Task.LAST_MODIFIED_OF_NEWEST_ATTACHMENT }));
        task.setLastModified(response.getTimestamp());
        Task test = null;
        int objectIdPos = response.getColumnPos(Task.OBJECT_ID);
        int lastModifiedOfNewestAttachmentPos = response.getColumnPos(Task.LAST_MODIFIED_OF_NEWEST_ATTACHMENT);
        for (Object[] objA : response) {
            if (task.getObjectID() == ((Integer) objA[objectIdPos]).intValue()) {
                test = new Task();
                test.setLastModifiedOfNewestAttachment(new Date(((Long) objA[lastModifiedOfNewestAttachmentPos]).longValue()));
                break;
            }
        }
        Assert.assertNotNull("Can not find the created task with an attachment.", test);
        assertEquals("Creation date of attachment does not match.", creationDate, test.getLastModifiedOfNewestAttachment());
    }
}
