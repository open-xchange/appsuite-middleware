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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.JSONException;
import com.openexchange.ajax.folder.actions.API;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.folder.actions.ListRequest;
import com.openexchange.ajax.folder.actions.ListResponse;
import com.openexchange.ajax.folder.actions.UpdateRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class FolderTools {

    /**
     * Prevent instantiation
     */
    private FolderTools() {
        super();
    }

    /**
     * @deprecated the generic type of the request now deals with that.
     */
    @Deprecated
    public static ListResponse list(final AJAXClient client, final ListRequest request) throws OXException, IOException, JSONException {
        return Executor.execute(client, request);
    }

    public static List<FolderObject> getSubFolders(AJAXClient client, String parent, boolean ignoreMailFolder) throws OXException, IOException, JSONException {
        final ListRequest request = new ListRequest(EnumAPI.OX_OLD, parent, ignoreMailFolder);
        final ListResponse response = client.execute(request);
        final List<FolderObject> retval = new ArrayList<FolderObject>();
        final Iterator<FolderObject> iter = response.getFolder();
        while (iter.hasNext()) {
            retval.add(iter.next());
        }
        return retval;
    }

    public static List<FolderObject> convert(Iterator<FolderObject> iter) {
        List<FolderObject> retval = new ArrayList<FolderObject>();
        while (iter.hasNext()) {
            retval.add(iter.next());
        }
        return retval;
    }

    public static void shareFolder(AJAXClient client, API api, int folderId, int userId, int fp, int opr, int opw, int opd) throws OXException, IOException, JSONException {
        GetRequest getQ = new GetRequest(api, folderId);
        GetResponse getR = client.execute(getQ);
        FolderObject origFolder = getR.getFolder();
        FolderObject changed = new FolderObject();
        changed.setObjectID(folderId);
        changed.setLastModified(getR.getTimestamp());
        List<OCLPermission> permissions = new ArrayList<OCLPermission>();
        for (OCLPermission permission : origFolder.getPermissions()) {
            if (permission.getEntity() != userId) {
                permissions.add(permission);
            }
        }
        OCLPermission addedPerm = new OCLPermission();
        addedPerm.setEntity(userId);
        addedPerm.setAllPermission(fp, opr, opw, opd);
        permissions.add(addedPerm);
        changed.setPermissions(permissions);
        UpdateRequest updQ = new UpdateRequest(api, changed);
        client.execute(updQ);
    }

    public static void unshareFolder(AJAXClient client, API api, int folderId, int userId) throws OXException, IOException, JSONException {
        GetRequest getQ = new GetRequest(api, folderId);
        GetResponse getR = client.execute(getQ);
        FolderObject origFolder = getR.getFolder();
        List<OCLPermission> permissions = new ArrayList<OCLPermission>();
        permissions.addAll(origFolder.getPermissions());
        Iterator<OCLPermission> iter = permissions.iterator();
        while (iter.hasNext()) {
            if (iter.next().getEntity() == userId) {
                iter.remove();
            }
        }
        FolderObject changed = new FolderObject();
        changed.setObjectID(folderId);
        changed.setLastModified(getR.getTimestamp());
        changed.setPermissions(permissions);
        UpdateRequest updQ = new UpdateRequest(api, changed);
        client.execute(updQ);
    }
}
