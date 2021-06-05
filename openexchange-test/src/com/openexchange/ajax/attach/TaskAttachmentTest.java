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

package com.openexchange.ajax.attach;

import org.junit.Test;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.tasks.Task;

/**
 * This class tests attachments for tasks through the ajax interface.
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class TaskAttachmentTest extends AbstractAttachmentTest {

    public TaskAttachmentTest() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int createExclusiveWritableAttachable(final int folderId) throws Exception {
        final Task task = new Task();
        task.setTitle("AttachmentTest");
        task.setParentFolderID(folderId);
        final int taskId = ttm.insertTaskOnServer(task).getObjectID();
        return taskId;
    }

    @Override
    public int getExclusiveWritableFolder() throws Exception {
        return getClient().getValues().getPrivateTaskFolder();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getModule() throws Exception {
        return Types.TASK;
    }

    /**
     * Tests to get all attachments for a task.
     * 
     * @throws Throwable if an error occurs.
     */
    @Test
    public void testAll() throws Throwable {
        doAll();
    }

    /**
     * Tests uploading of multiple Attachments in one request.
     * 
     * @throws Throwable if an error occurs.
     */
    @Test
    public void testMultiple() throws Throwable {
        doMultiple();
    }

    /**
     * Tests to detach an attachment.
     * 
     * @throws Throwable if an error occurs.
     */
    @Test
    public void testDetach() throws Throwable {
        doDetach();
    }

    /**
     * Tests if the attached file is the same.
     * 
     * @throws Throwable if an error occurs.
     */
    @Test
    public void testDocument() throws Throwable {
        doDocument();
    }

    /**
     * Tests to list all attachments for a task.
     * 
     * @throws Throwable if an error occurs.
     */
    @Test
    public void testList() throws Throwable {
        doList();
    }

    /**
     * Tests if not exists attachments are discovered correctly.
     * 
     * @throws Throwable if an error occurs.
     */
    @Test
    public void testNotExists() throws Throwable {
        doNotExists();
    }

    /**
     * Tests if updated attachments are correctly send by the server.
     * 
     * @throws Throwable if an error occurs.
     */
    @Test
    public void testUpdates() throws Throwable {
        doUpdates();
    }

    /**
     * Test to attach to a task that can't be read by a user. The server must
     * return an error if a user is trying to do so.
     * 
     * @throws Throwable if an error occurs.
     */
    @Test
    public void testForbidden() throws Throwable {
        doForbidden();
    }

    /**
     * Test to attach to a task.
     * 
     * @throws Throwable if an error occurs.
     */
    @Test
    public void testGet() throws Throwable {
        doGet();
    }
}
