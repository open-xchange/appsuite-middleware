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

package com.openexchange.ajax.folder.api2;

import java.util.Iterator;
import java.util.UUID;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.folder.actions.ListRequest;
import com.openexchange.ajax.folder.actions.ListResponse;
import com.openexchange.ajax.folder.actions.UpdateRequest;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link Bug29853Test}
 *
 * Folder with the same name on the server
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug29853Test extends AbstractFolderTest {

    private static final int THREAD_COUNT = 20;

    private FolderObject folder;

    public Bug29853Test(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        folder = createSingle(FolderObject.INFOSTORE, UUID.randomUUID().toString());
        folder.setParentFolderID(client.getValues().getPrivateInfostoreFolder());
        InsertRequest insertRequest = new InsertRequest(EnumAPI.OX_NEW, folder);
        InsertResponse insertResponse = client.execute(insertRequest);
        insertResponse.fillObject(folder);
    }

    @Override
    protected void tearDown() throws Exception {
        if (null != folder) {
            super.deleteFolders(true,folder);
        }
        super.tearDown();
    }

    public void testCreateWithSameName() throws Throwable {
        String folderName = "a";
        FolderObject subfolder = createSingle(FolderObject.INFOSTORE, folderName);
        subfolder.setParentFolderID(folder.getObjectID());
        final InsertRequest insertRequest = new InsertRequest(EnumAPI.OX_NEW, subfolder);
        Runnable insertFolderRunnable = new Runnable() {

            @Override
            public void run() {
                client.executeSafe(insertRequest);
            }
        };
        Thread[] insertThreads = new Thread[THREAD_COUNT];
        for (int i = 0; i < insertThreads.length; i++) {
            insertThreads[i] = new Thread(insertFolderRunnable);
            insertThreads[i].start();
        }
        for (int i = 0; i < insertThreads.length; i++) {
            insertThreads[i].join();
        }
        ListRequest listRequest = new ListRequest(EnumAPI.OX_NEW, folder.getObjectID());
        ListResponse listResponse = client.execute(listRequest);
        Iterator<FolderObject> iterator = listResponse.getFolder();
        assertTrue("No folder created", iterator.hasNext());
        FolderObject createdFolder = iterator.next();
        assertNotNull("No folder created", createdFolder);
        assertEquals("Folder name wrong", folderName, createdFolder.getFolderName());
        assertFalse("Folder was created " + listResponse.getArray().length + " times: " + listResponse.getResponse(), iterator.hasNext());
    }

    public void testRenameToSameName() throws Throwable {
        FolderObject[] subfolders = new FolderObject[THREAD_COUNT];
        for (int i = 0; i < THREAD_COUNT; i++) {
            subfolders[i] = createSingle(FolderObject.INFOSTORE, "f" + i);
            subfolders[i].setParentFolderID(folder.getObjectID());
            InsertRequest insertRequest = new InsertRequest(EnumAPI.OX_NEW, subfolders[i]);
            InsertResponse insertResponse = client.execute(insertRequest);
            insertResponse.fillObject(subfolders[i]);
        }
        String folderName = "a";
        Thread[] updateThreads = new Thread[THREAD_COUNT];
        for (int i = 0; i < THREAD_COUNT; i++) {
            subfolders[i].setFolderName(folderName);
            final UpdateRequest updateRequest = new UpdateRequest(EnumAPI.OX_NEW, subfolders[i]);
            Runnable updateRunnable = new Runnable() {

                @Override
                public void run() {
                    client.executeSafe(updateRequest);
                }
            };
            updateThreads[i] = new Thread(updateRunnable);
            updateThreads[i].start();
        }
        for (int i = 0; i < THREAD_COUNT; i++) {
            updateThreads[i].join();
        }
        ListRequest listRequest = new ListRequest(EnumAPI.OX_NEW, folder.getObjectID());
        ListResponse listResponse = client.execute(listRequest);
        FolderObject renamedFolder = null;
        Iterator<FolderObject> iterator = listResponse.getFolder();
        while (iterator.hasNext()) {
            FolderObject folder = iterator.next();
            if (folderName.equals(folder.getFolderName())) {
                if (null == renamedFolder) {
                    renamedFolder = folder;
                } else {
                    fail("Folder was renamed more than once: " + listResponse.getResponse());
                }
            }
        }
        assertNotNull("No folder renamed", renamedFolder);
    }

}
