/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.ajax.task;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.TimeZone;
import junit.framework.AssertionFailedError;
import com.openexchange.ajax.attach.actions.AttachRequest;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.framework.ListIDs;
import com.openexchange.ajax.task.actions.AllRequest;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.ListRequest;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.tasks.Create;
import com.openexchange.groupware.tasks.Task;

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

    public TaskAttachmentTests(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        folderId = client.getValues().getPrivateTaskFolder();
        tz = client.getValues().getTimeZone();
        task = Create.createWithDefaults(folderId, "Test task for testing attachments");
        client.execute(new InsertRequest(task, tz)).fillTask(task);
        attachmentId = client.execute(new AttachRequest(task, "test.txt", new ByteArrayInputStream("Test".getBytes()), "text/plain")).getId();
        com.openexchange.ajax.attach.actions.GetResponse response = client.execute(new com.openexchange.ajax.attach.actions.GetRequest(task, attachmentId));
        long timestamp = response.getAttachment().getCreationDate().getTime();
        creationDate = new Date(timestamp - tz.getOffset(timestamp));
    }

    @Override
    protected void tearDown() throws Exception {
        client.execute(new DeleteRequest(task));
        super.tearDown();
    }

    public void testLastModifiedOfNewestAttachmentWithGet() throws Throwable {
        GetResponse response = client.execute(new GetRequest(task.getParentFolderID(), task.getObjectID()));
        task.setLastModified(response.getTimestamp());
        Task test = response.getTask(tz);
        assertEquals("Creation date of attachment does not match.", creationDate, test.getLastModifiedOfNewestAttachment());
    }

    public void testLastModifiedOfNewestAttachmentWithAll() throws Throwable {
        CommonAllResponse response = client.execute(new AllRequest(task.getParentFolderID(), new int[] {
            Task.OBJECT_ID, Task.LAST_MODIFIED_OF_NEWEST_ATTACHMENT }, Task.OBJECT_ID, Order.ASCENDING));
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
        if (null == test) {
            throw new AssertionFailedError("Can not find the created task with an attachment.");
        }
        assertEquals("Creation date of attachment does not match.", creationDate, test.getLastModifiedOfNewestAttachment());
    }

    public void testLastModifiedOfNewestAttachmentWithList() throws Throwable {
        CommonListResponse response = client.execute(new ListRequest(
            ListIDs.l(new int[] { task.getParentFolderID(), task.getObjectID() }),
            new int[] { Task.OBJECT_ID, Task.LAST_MODIFIED_OF_NEWEST_ATTACHMENT }));
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
        if (null == test) {
            throw new AssertionFailedError("Can not find the created task with an attachment.");
        }
        assertEquals("Creation date of attachment does not match.", creationDate, test.getLastModifiedOfNewestAttachment());
    }
}
