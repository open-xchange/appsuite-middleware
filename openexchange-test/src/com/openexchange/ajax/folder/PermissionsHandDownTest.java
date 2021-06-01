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

package com.openexchange.ajax.folder;

import static org.junit.Assert.assertTrue;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.folder.actions.UpdateRequest;
import com.openexchange.ajax.framework.Abstrac2UserAJAXSession;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link PermissionsHandDownTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PermissionsHandDownTest extends Abstrac2UserAJAXSession {

    /**
     * testPermissionsHandDown
     */
    @Test
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
            FolderObject folder = Create.createPrivateFolder(name, FolderObject.TASK, getClient().getValues().getUserId());
            Date timestamp = null;
            {
                folder.setParentFolderID(getClient().getValues().getPrivateTaskFolder());
                final InsertResponse response = getClient().execute(new InsertRequest(EnumAPI.OUTLOOK, folder));
                response.fillObject(folder);
                final GetResponse response2 = getClient().execute(new GetRequest(EnumAPI.OUTLOOK, folder.getObjectID()));
                timestamp = response2.getTimestamp();
                folder.setLastModified(timestamp);
                deletees.add(new DeleteInfo(folder.getObjectID(), timestamp.getTime()));
            }

            final int objectId = folder.getObjectID();
            folder = Create.createPrivateFolder("sub-permissions-hand-down_" + UUID.randomUUID().toString(), FolderObject.TASK, getClient().getValues().getUserId());
            {
                folder.setParentFolderID(objectId);
                final InsertResponse response = getClient().execute(new InsertRequest(EnumAPI.OUTLOOK, folder));
                response.fillObject(folder);
                final GetResponse response2 = getClient().execute(new GetRequest(EnumAPI.OUTLOOK, folder.getObjectID()));
                folder.setLastModified(response2.getTimestamp());
                deletees.add(new DeleteInfo(folder.getObjectID(), response2.getTimestamp().getTime()));
            }

            final int childObjectId = folder.getObjectID();
            folder = Create.createPrivateFolder(name, FolderObject.TASK, getClient().getValues().getUserId());
            folder.setObjectID(objectId);
            {
                folder.addPermission(Create.ocl(client2.getValues().getUserId(), false, false, OCLPermission.READ_FOLDER, OCLPermission.READ_OWN_OBJECTS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS));
                folder.setLastModified(timestamp);
                getClient().execute(new UpdateRequest(EnumAPI.OUTLOOK, folder).setHandDown(true));
            }

            final GetResponse getResponse = getClient().execute(new GetRequest(EnumAPI.OUTLOOK, childObjectId));
            final List<OCLPermission> permissions = getResponse.getFolder().getPermissions();

            final int pSize = permissions.size();
            assertTrue("Unexpected number of permissions: " + pSize, pSize > 1);

            boolean found = false;
            for (int i = 0; !found && i < pSize; i++) {
                found = permissions.get(i).getEntity() == getClient().getValues().getUserId();
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
                    getClient().execute(new DeleteRequest(EnumAPI.OUTLOOK, deleteInfo.fuid, new Date(deleteInfo.lastModified)));
                }
            }
        }
    }

}
