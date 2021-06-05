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

package com.openexchange.admin.storage.mysqlStorage;

import static com.openexchange.java.Autoboxing.I;
import java.util.HashMap;
import java.util.Map;

/**
 * Combines the context file store information for a specific database server and a certain file store. Collects across all schemas of that
 * database server the file store usage for the given file store.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
class FilestoreContextBlock {

    public final int writeDBPoolID;
    public final String schema;
    public final int filestoreID;

    public final Map<Integer, FilestoreInfo> contextFilestores = new HashMap<Integer, FilestoreInfo>();
    public final Map<Integer, Map<Integer, FilestoreInfo>> userFilestores = new HashMap<Integer, Map<Integer, FilestoreInfo>>();

    public FilestoreContextBlock(int writeDBPoolID, String schema, int filestoreID) {
        super();
        this.writeDBPoolID = writeDBPoolID;
        this.schema = schema;
        this.filestoreID = filestoreID;
    }

    public boolean isEmpty() {
        return contextFilestores.isEmpty() && userFilestores.isEmpty();
    }

    public int sizeForContext() {
        return contextFilestores.size();
    }

    public void addForContext(FilestoreInfo newInfo) {
        contextFilestores.put(I(newInfo.contextID), newInfo);
    }

    public void updateForContext(int contextID, final long usage) {
        final FilestoreInfo info = contextFilestores.get(I(contextID));
        if (info != null) {
            info.usage = usage;
        }
        // The schema may contain contexts having the files stored in another file store.
    }

    public int sizeFoUser() {
        return userFilestores.size();
    }

    public void addForUser(FilestoreInfo newInfo) {
        Map<Integer, FilestoreInfo> users = userFilestores.get(I(newInfo.contextID));
        if (null == users) {
            users = new HashMap<Integer, FilestoreInfo>();
            userFilestores.put(I(newInfo.contextID), users);
        }

        users.put(I(newInfo.userID), newInfo);
    }

    public void updateForUser(int contextID, int userID, long usage) {
        Map<Integer, FilestoreInfo> users = userFilestores.get(I(contextID));
        if (null != users) {
            FilestoreInfo info = users.get(I(userID));
            if (info != null) {
                info.usage = usage;
            }
        }
    }

    @Override
    public String toString(){
        return "["+filestoreID+"] Elements: " + (sizeForContext() + sizeFoUser()) + ", writepoolID: " + writeDBPoolID;
    }
}
