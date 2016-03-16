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

package com.openexchange.ajax.folder.eas;

import java.util.Date;
import java.util.LinkedList;
import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.folder.actions.ListRequest;
import com.openexchange.ajax.folder.actions.ListResponse;
import com.openexchange.ajax.folder.actions.SubscribeRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link MultipleSubscribeWithoutParentTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MultipleSubscribeWithoutParentTest extends AbstractAJAXSession {

    private AJAXClient client;

    /**
     * Initializes a new {@link MultipleSubscribeWithoutParentTest}.
     *
     * @param name The name of the test.
     */
    public MultipleSubscribeWithoutParentTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
    }

    private String createPrivateCalendarFolder(final String prefix, final int parentId) throws Throwable {
        final FolderObject fo = new FolderObject();
        fo.setParentFolderID(parentId > 0 ? parentId : FolderObject.SYSTEM_PRIVATE_FOLDER_ID);
        fo.setFolderName((null == prefix ? "testCalendarFolder" : prefix) + System.currentTimeMillis());
        fo.setModule(FolderObject.CALENDAR);

        final OCLPermission oclP = new OCLPermission();
        oclP.setEntity(client.getValues().getUserId());
        oclP.setGroupPermission(false);
        oclP.setFolderAdmin(true);
        oclP.setAllPermission(
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION);
        fo.setPermissionsAsArray(new OCLPermission[] { oclP });
        final InsertRequest request = new InsertRequest(EnumAPI.OUTLOOK, fo);
        final InsertResponse response = client.execute(request);
        return (String) response.getResponse().getData();
    }

    public void testSubscribeMultiplePrivate() throws Throwable {
        final String parent = FolderStorage.ROOT_ID;
        final LinkedList<String> ids = new LinkedList<String>();
        try {
            final String newId = createPrivateCalendarFolder("testCalendarParentFolder", -1);
            assertNotNull("New ID must not be null!", newId);
            ids.addFirst(newId);
            
            /*-
             * ---------------------------------------------------
             */
            
            final String newSubId = createPrivateCalendarFolder("testCalendarChildFolder", Integer.parseInt(newId));
            assertNotNull("New ID must not be null!", newSubId);
            ids.addFirst(newSubId);

            SubscribeRequest subscribeRequest = new SubscribeRequest(EnumAPI.EAS_FOLDERS, newId, true);
            subscribeRequest.addFolderId(newSubId, true);
            client.execute(subscribeRequest);

            ListRequest listRequest = new ListRequest(EnumAPI.EAS_FOLDERS, newId);
            ListResponse listResponse = client.execute(listRequest);
            boolean found = false;
            for (final Object[] vals : listResponse.getArray()) {
                if (newSubId.equals(vals[0].toString())) {
                    found = true;
                    break;
                }
            }
            assertTrue("Subscribed subfolder not found.", found);

            listRequest = new ListRequest(EnumAPI.EAS_FOLDERS, parent);
            listResponse = client.execute(listRequest);
            found = false;
            for (final Object[] vals : listResponse.getArray()) {
                if (newId.equals(vals[0].toString())) {
                    found = true;
                    break;
                }
            }
            assertTrue("Subscribed folder not found.", found);
            
            /*-
             * ---------------------------------------------------
             */

            subscribeRequest = new SubscribeRequest(EnumAPI.EAS_FOLDERS, newId, true);
            subscribeRequest.addFolderId(newSubId, false);
            client.execute(subscribeRequest);

            listRequest = new ListRequest(EnumAPI.EAS_FOLDERS, newId);
            listResponse = client.execute(listRequest);
            found = false;
            for (final Object[] vals : listResponse.getArray()) {
                if (newSubId.equals(vals[0].toString())) {
                    found = true;
                    break;
                }
            }
            assertFalse("Unsubscribed subfolder still available.", found);
        } finally {
            final int size = ids.size();
            for (int i = 0; i < size; i++) {
                final String id = ids.get(i);
                // Try unsubscribe folder
                {
                    final SubscribeRequest subscribeRequest = new SubscribeRequest(EnumAPI.EAS_FOLDERS, parent, true);
                    subscribeRequest.addFolderId(id, false);
                    client.execute(subscribeRequest);
                }
                // Delete folder
                try {
                    final DeleteRequest deleteRequest = new DeleteRequest(EnumAPI.OUTLOOK, id, new Date());
                    client.execute(deleteRequest);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
