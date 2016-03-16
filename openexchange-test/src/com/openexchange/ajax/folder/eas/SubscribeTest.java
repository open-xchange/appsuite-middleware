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
 * {@link SubscribeTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SubscribeTest extends AbstractAJAXSession {

    private AJAXClient client;

    /**
     * Initializes a new {@link SubscribeTest}.
     *
     * @param name The name of the test.
     */
    public SubscribeTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
    }

    public void testSubscribePrivate() throws Throwable {
        final String parent = FolderStorage.ROOT_ID;
        String newId = null;
        boolean unsubscribe = false;
        try {
            final FolderObject fo = new FolderObject();
            fo.setParentFolderID(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);
            fo.setFolderName("testCalendarFolder" + System.currentTimeMillis());
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
            newId = (String) response.getResponse().getData();
            assertNotNull("New ID must not be null!", newId);

            SubscribeRequest subscribeRequest = new SubscribeRequest(EnumAPI.EAS_FOLDERS, parent, true);
            subscribeRequest.addFolderId(newId, true);
            client.execute(subscribeRequest);
            unsubscribe = true;

            ListRequest listRequest = new ListRequest(EnumAPI.EAS_FOLDERS, parent);
            ListResponse listResponse = client.execute(listRequest);
            boolean found = false;
            for (final Object[] vals : listResponse.getArray()) {
                if (newId.equals(vals[0].toString())) {
                    found = true;
                    break;
                }
            }
            assertTrue("Subscribed folder not found.", found);

            subscribeRequest = new SubscribeRequest(EnumAPI.EAS_FOLDERS, parent, true);
            subscribeRequest.addFolderId(newId, false);
            client.execute(subscribeRequest);
            unsubscribe = false;

            listRequest = new ListRequest(EnumAPI.EAS_FOLDERS, parent);
            listResponse = client.execute(listRequest);
            found = false;
            for (final Object[] vals : listResponse.getArray()) {
                if (newId.equals(vals[0].toString())) {
                    found = true;
                    break;
                }
            }
            assertFalse("Unsubscribed folder still available.", found);
        } finally {
            if (null != newId) {
                // Unsubscribe folder
                if (unsubscribe) {
                    final SubscribeRequest subscribeRequest = new SubscribeRequest(EnumAPI.EAS_FOLDERS, parent, true);
                    subscribeRequest.addFolderId(newId, false);
                    client.execute(subscribeRequest);
                }
                // Delete folder
                try {
                    final DeleteRequest deleteRequest = new DeleteRequest(EnumAPI.OUTLOOK, newId, new Date());
                    client.execute(deleteRequest);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
