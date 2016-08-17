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

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.folder.actions.UpdateRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;


/**
 * {@link PermissionsHandDownTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PermissionsHandDownTest extends AbstractAJAXSession {

    private AJAXClient client2;

    /**
     * Initializes a new {@link PermissionsHandDownTest}.
     */
    public PermissionsHandDownTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client2 = new AJAXClient(User.User2);
    }

    @Override
    protected void tearDown() throws Exception {
        if (null != client2) {
            client2.logout();
            client2 = null;
        }
        super.tearDown();
    }

    /**
     * testPermissionsHandDown
     */
    public void testPermissionsHandDown() throws Exception {
        class DeleteInfo {
            int fuid;
            long lastModified;
            DeleteInfo(final int fuid, final long lastModified) {
                super();
                this.fuid = fuid;
                this.lastModified = lastModified;
            }
        }
        final List<DeleteInfo> deletees = new LinkedList<DeleteInfo>();
        try {
            final String name = "permissions-hand-down_" + System.currentTimeMillis();
            FolderObject folder = Create.createPrivateFolder(name, FolderObject.TASK, client.getValues().getUserId());
            Date timestamp = null;
            {
                folder.setParentFolderID(client.getValues().getPrivateTaskFolder());
                final InsertResponse response = client.execute(new InsertRequest(EnumAPI.OUTLOOK, folder));
                response.fillObject(folder);
                final GetResponse response2 = client.execute(new GetRequest(EnumAPI.OUTLOOK, folder.getObjectID()));
                timestamp = response2.getTimestamp();
                folder.setLastModified(timestamp);
                deletees.add(new DeleteInfo(folder.getObjectID(), timestamp.getTime()));
            }

            final int objectId = folder.getObjectID();
            folder = Create.createPrivateFolder("sub-permissions-hand-down_" + System.currentTimeMillis(), FolderObject.TASK, client.getValues().getUserId());
            {
                folder.setParentFolderID(objectId);
                final InsertResponse response = client.execute(new InsertRequest(EnumAPI.OUTLOOK, folder));
                response.fillObject(folder);
                final GetResponse response2 = client.execute(new GetRequest(EnumAPI.OUTLOOK, folder.getObjectID()));
                folder.setLastModified(response2.getTimestamp());
                deletees.add(new DeleteInfo(folder.getObjectID(), response2.getTimestamp().getTime()));
            }

            final int childObjectId = folder.getObjectID();
            folder = Create.createPrivateFolder(name, FolderObject.TASK, client.getValues().getUserId());
            folder.setObjectID(objectId);
            {
                folder.addPermission(Create.ocl(
                    client2.getValues().getUserId(),
                    false,
                    false,
                    OCLPermission.READ_FOLDER,
                    OCLPermission.READ_OWN_OBJECTS,
                    OCLPermission.NO_PERMISSIONS,
                    OCLPermission.NO_PERMISSIONS));
                folder.setLastModified(timestamp);
                client.execute(new UpdateRequest(EnumAPI.OUTLOOK, folder).setHandDown(true));
            }

            final GetResponse getResponse = client.execute(new GetRequest(EnumAPI.OUTLOOK, childObjectId));
            final List<OCLPermission> permissions = getResponse.getFolder().getPermissions();
            
            final int pSize = permissions.size();
            assertTrue("Unexpected number of permissions: " + pSize, pSize > 1);

            boolean found = false;
            for (int i = 0; !found && i < pSize; i++) {
                found = permissions.get(i).getEntity() == client.getValues().getUserId();
            }
            assertTrue("Folder creator not found in permissions", found);
            
            found = false;
            for (int i = 0; !found && i < pSize; i++) {
                found = permissions.get(i).getEntity() == client2.getValues().getUserId();
            }
            assertTrue("Second user not found in permissions", found);
            
        } finally {
            if (!deletees.isEmpty()) {
                Collections.reverse(deletees);
                for (final DeleteInfo deleteInfo : deletees) {
                    client.execute(new DeleteRequest(EnumAPI.OUTLOOK, deleteInfo.fuid, new Date(deleteInfo.lastModified)));
                }
            }
        }
    }

}
