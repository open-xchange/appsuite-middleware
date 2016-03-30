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

package com.openexchange.ajax.folder;

import java.io.IOException;
import java.util.Random;
import org.json.JSONException;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.MultipleRequest;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Folder;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.test.FolderTestManager;

/**
 * Verifies if the object count on the folder works successfully for task folders.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class TaskObjectCountTest extends AbstractObjectCountTest {

    private static final Random rand = new Random(System.currentTimeMillis());

    public TaskObjectCountTest(String name) {
        super(name);
    }

    @Test
    public void testCountInPrivateFolder() throws Exception {
        FolderTestManager ftm = new FolderTestManager(client1);
        try {
            int folderId = createPrivateFolder(client1, ftm, FolderObject.TASK).getObjectID();
            Folder folder = getFolder(client1, folderId, DEFAULT_COLUMNS);
            assertEquals("Wrong object count", 0, folder.getTotal());

            int numTasks = rand.nextInt(20) + 1;
            createTasks(client1, folderId, numTasks);
            folder = getFolder(client1, folderId, DEFAULT_COLUMNS);
            assertEquals("Wrong object count", numTasks, folder.getTotal());
        } finally {
            ftm.cleanUp();
        }
    }

    @Test
    public void testCountInPublicFolder() throws Exception {
        FolderTestManager ftm = new FolderTestManager(client1);
        try {
            FolderObject created = createPublicFolder(client1, FolderObject.TASK, client2.getValues().getUserId(), ftm);
            int folderId = created.getObjectID();
            Folder folder = getFolder(client1, folderId, DEFAULT_COLUMNS);
            assertEquals("Wrong object count", 0, folder.getTotal());

            int numTasks1 = rand.nextInt(20) + 1;
            int numTasks2 = rand.nextInt(20) + 1;
            createTasks(client1, folderId, numTasks1);
            createTasks(client2, folderId, numTasks2);
            folder = getFolder(client2, folderId, DEFAULT_COLUMNS);
            assertEquals("Wrong object count for public folder reader.", numTasks2, folder.getTotal());
            folder = getFolder(client1, folderId, DEFAULT_COLUMNS);
            assertEquals("Wrong object count for public folder creator.", numTasks1 + numTasks2, folder.getTotal());
        } finally {
            ftm.cleanUp();
        }
    }

    @Test
    public void testCountInSharedFolder() throws Exception {
        FolderTestManager ftm = new FolderTestManager(client1);
        try {
            FolderObject created = createSharedFolder(client1, FolderObject.TASK, client2.getValues().getUserId(), ftm);
            int folderId = created.getObjectID();
            Folder folder = getFolder(client1, folderId, DEFAULT_COLUMNS);
            assertEquals("Wrong object count", 0, folder.getTotal());

            int numTasks1 = rand.nextInt(20) + 1;
            int numTasks2 = rand.nextInt(20) + 1;
            createTasks(client1, folderId, numTasks1, true);
            createTasks(client2, folderId, numTasks2);
            folder = getFolder(client2, folderId, DEFAULT_COLUMNS);
            assertEquals("Wrong object count for shared folder reader.", numTasks2, folder.getTotal());
            folder = getFolder(client1, folderId, DEFAULT_COLUMNS);
            assertEquals("Wrong object count for shared folder owner.", numTasks1 + numTasks2, folder.getTotal());
        } finally {
            ftm.cleanUp();
        }
    }

    private static void createTasks(AJAXClient client, int folderId, int count) throws OXException, IOException, JSONException {
        createTasks(client, folderId, count, false);
    }

    private static void createTasks(AJAXClient client, int folderId, int count, boolean privat) throws OXException, IOException, JSONException {
        final InsertRequest[] inserts = new InsertRequest[count];
        for (int i = 0; i < inserts.length; i++) {
            final Task task = new Task();
            task.setTitle("Task for folder count test " + (i + 1));
            task.setParentFolderID(folderId);
            task.setPrivateFlag(privat);
            inserts[i] = new InsertRequest(task, client.getValues().getTimeZone());
        }
        client.execute(MultipleRequest.create(inserts));
    }
}
