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

import java.util.Iterator;
import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.folder.actions.ListRequest;
import com.openexchange.ajax.folder.actions.ListResponse;
import com.openexchange.ajax.folder.actions.UpdateRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.task.actions.SearchRequest;
import com.openexchange.ajax.task.actions.SearchResponse;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.search.TaskSearchObject;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link Bug16724Test}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class Bug16724Test extends AbstractAJAXSession {

    private AJAXClient client;
    private AJAXClient client2;
    private FolderObject folder;
    private int userId1;

    public Bug16724Test(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
        userId1 = client.getValues().getUserId();
        folder = Create.createPrivateFolder("test for bug 16724_" + System.currentTimeMillis(), FolderObject.TASK, client.getValues().getUserId());
        folder.setParentFolderID(client.getValues().getPrivateTaskFolder());
        client2 = new AJAXClient(User.User2);
        folder.addPermission(Create.ocl(
            client2.getValues().getUserId(),
            false,
            false,
            OCLPermission.READ_FOLDER,
            OCLPermission.READ_OWN_OBJECTS,
            OCLPermission.NO_PERMISSIONS,
            OCLPermission.NO_PERMISSIONS));
        InsertResponse response = client.execute(new InsertRequest(EnumAPI.OUTLOOK, folder));
        response.fillObject(folder);
        GetResponse response2 = client.execute(new GetRequest(EnumAPI.OUTLOOK, folder.getObjectID()));
        folder.setLastModified(response2.getTimestamp());
    }

    @Override
    protected void tearDown() throws Exception {
        client.execute(new DeleteRequest(EnumAPI.OUTLOOK, folder));
        super.tearDown();
    }

    public void testCachedAccess() throws Throwable {
        ListResponse listResponse1 = client2.execute(new ListRequest(EnumAPI.OUTLOOK, FolderObject.SHARED_PREFIX + userId1));
        Iterator<FolderObject> iter = listResponse1.getFolder();
        boolean found = false;
        while (iter.hasNext() && !found) {
            if (iter.next().getObjectID() == folder.getObjectID()) {
                found = true;
            }
        }
        assertTrue("Shared folder not found.", found);
        GetResponse getResponse1 = client2.execute(new GetRequest(EnumAPI.OUTLOOK, folder.getObjectID()));

        folder.setLastModified(getResponse1.getTimestamp());
        folder.setPermissionsAsArray(new OCLPermission[] { Create.ocl(
            userId1,
            false,
            true,
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION) });
        InsertResponse updateResponse = client.execute(new UpdateRequest(EnumAPI.OUTLOOK, folder));
        folder.setLastModified(updateResponse.getTimestamp());

        TaskSearchObject search = new TaskSearchObject();
        search.setPattern("");
        SearchResponse searchResponse = client2.execute(new SearchRequest(search, new int[] { Task.OBJECT_ID, Task.TITLE }, false));
        assertFalse("Search response should not have an error.", searchResponse.hasError());

        ListResponse listResponse2 = client2.execute(new ListRequest(EnumAPI.OUTLOOK, FolderObject.SYSTEM_SHARED_FOLDER_ID));
        iter = listResponse2.getFolder();
        found = false;
        while (iter.hasNext()) {
            if (iter.next().getFullName().equals(FolderObject.SHARED_PREFIX + userId1)) {
                found = true;
            }
        }
        if (found) {
            /*
             * Found a shared folder, check if it is not the unshared one
             */
            listResponse2 = client2.execute(new ListRequest(EnumAPI.OUTLOOK, FolderObject.SHARED_PREFIX + userId1));
            iter = listResponse2.getFolder();
            found = false;
            while (iter.hasNext()) {
                if (iter.next().getObjectID() == folder.getObjectID()) {
                    found = true;
                }
            }
            assertFalse("Parent user folder in shared folder should not be there.", found);
        }
        ListResponse listResponse3 = client2.execute(new ListRequest(EnumAPI.OUTLOOK, FolderObject.SHARED_PREFIX + userId1));
        iter = listResponse3.getFolder();
        found = false;
        while (iter.hasNext() && !found) {
            if (iter.next().getObjectID() == folder.getObjectID()) {
                found = true;
            }
        }
        assertFalse("Shared folder should not be found.", found);
        GetResponse getResponse2 = client2.execute(new GetRequest(EnumAPI.OUTLOOK, folder.getObjectID(), false));
        assertTrue("Getting that not shared folder should give a error.", getResponse2.hasError());
    }
}
