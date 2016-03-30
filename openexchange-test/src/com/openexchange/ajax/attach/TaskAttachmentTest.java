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

package com.openexchange.ajax.attach;

import java.util.Date;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.task.TaskTools;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.tasks.Task;

/**
 * This class tests attachments for tasks through the ajax interface.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class TaskAttachmentTest extends AbstractAttachmentTest {

    public TaskAttachmentTest(final String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int createExclusiveWritableAttachable(final String sessionId,
        final int folderId) throws Exception {
        final Task task = new Task();
        task.setTitle("AttachmentTest");
        task.setParentFolderID(folderId);
        final int taskId = TaskTools.extractInsertId(TaskTools.insertTask(
            getWebConversation(), getHostName(), sessionId, task));
        return taskId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getExclusiveWritableFolder(final String sessionId)
        throws Exception {
        return TaskTools.getPrivateTaskFolder(getWebConversation(),
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

        final Response response = TaskTools.getTask(getWebConversation(),
            getHostName(), sessionId, folderId, taskId);
        final Date lastModified = response.getTimestamp();
        TaskTools.deleteTask(getWebConversation(), getHostName(), sessionId,
            lastModified, folder, taskId);
    }

    /**
     * Tests to get all attachments for a task.
     * @throws Throwable if an error occurs.
     */
    public void testAll() throws Throwable {
        doAll();
    }

    /**
     * Tests uploading of multiple Attachments in one request.
     * @throws Throwable if an error occurs.
     */
    public void testMultiple() throws Throwable {
        doMultiple();
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
