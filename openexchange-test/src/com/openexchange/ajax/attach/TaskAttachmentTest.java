/*
 * OPEN-XCHANGE - "the communication and information enviroment"
 *
 * All intellectual property rights in the Software are protected by
 * international copyright laws.
 *
 * OPEN-XCHANGE is a trademark of Netline Internet Service GmbH and all other
 * brand and product names are or may be trademarks of, and are used to identify
 * products or services of, their respective owners.
 *
 * Please make sure that third-party modules and libraries are used according to
 * their respective licenses.
 *
 * Any modifications to this package must retain all copyright notices of the
 * original copyright holder(s) for the original code used.
 *
 * After any such modifications, the original code will still remain copyrighted
 * by the copyright holder(s) or original author(s).
 *
 * Copyright (C) 1998 - 2005 Netline Internet Service GmbH
 * mail:                    info@netline-is.de
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */

package com.openexchange.ajax.attach;

import java.util.Date;

import com.openexchange.ajax.TasksTest;
import com.openexchange.ajax.container.Response;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.tasks.Task;

/**
 * This class tests attachments for tasks through the ajax interface.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class TaskAttachmentTest extends AbstractAttachmentTest {

    /**
     * {@inheritDoc}
     */
    @Override
    public int createExclusiveWritableAttachable(final String sessionId,
        final int folderId) throws Exception {
        final Task task = new Task();
        task.setTitle("AttachmentTest");
        task.setParentFolderID(folderId);
        final int taskId = TasksTest.insertTask(getWebConversation(),
            getHostName(), sessionId, task);
        return taskId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getExclusiveWritableFolder(final String sessionId)
        throws Exception {
        return TasksTest.getPrivateTaskFolder(getWebConversation(),
            getHostName(), sessionId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getModule() throws Exception {
        return Types.TASK;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeAttachable(final int folder, final int taskId,
        final String sessionId) throws Exception {
        final Response response = TasksTest.getTask(getWebConversation(),
            getHostName(), sessionId, folderId, taskId);
        final Date lastModified = response.getTimestamp();
        TasksTest.deleteTask(getWebConversation(), getHostName(), sessionId,
            lastModified, new int[] { folder, taskId });
    }

    /**
     * Tests to get all attachments for a task.
     * @throws Throwable if an error occurs.
     */
    public void testAll() throws Throwable {
        doAll();
    }

    /**
     * Tests to detach an attachment.
     * @throws Throwable if an error occurs.
     */
    public void testDetach() throws Throwable {
        doDetach();
    }

    /**
     * Tests if the attached file is the same.
     * @throws Throwable if an error occurs.
     */
    public void testDocument() throws Throwable {
        doDocument();
    }

    /**
     * Tests to list all attachments for a task.
     * @throws Throwable if an error occurs.
     */
    public void testList() throws Throwable {
        doList();
    }

    /**
     * Tests if not exists attachments are discovered correctly.
     * @throws Throwable if an error occurs.
     */
    public void testNotExists() throws Throwable {
        doNotExists();
    }

    /**
     * Tests if updated attachments are correctly send by the server.
     * @throws Throwable if an error occurs.
     */
    public void testUpdates() throws Throwable {
        doUpdates();
    }

    /**
     * Test to attach to a task that can't be read by a user. The server must
     * return an error if a user is trying to do so.
     * @throws Throwable if an error occurs.
     */
    public void testForbidden() throws Throwable {
        doForbidden();
    }

    /**
     * Test to attach to a task.
     * @throws Throwable if an error occurs.
     */
    public void testGet() throws Throwable {
        doGet();
    }
}
