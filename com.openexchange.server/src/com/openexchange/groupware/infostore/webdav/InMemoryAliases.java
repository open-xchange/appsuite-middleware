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
package com.openexchange.groupware.infostore.webdav;

import java.util.HashMap;
import java.util.Map;
import com.openexchange.groupware.infostore.WebdavFolderAliases;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class InMemoryAliases implements WebdavFolderAliases {
    private final Map<Integer, String> names = new HashMap<Integer, String>();
    private final Map<String, Integer> ids = new HashMap<String, Integer>();
    private final Map<Integer, Integer> parents = new HashMap<Integer, Integer>();

    @Override
    public void registerNameWithIDAndParent(String folderName, int id, int parent) {
        names.put(id, folderName);
        ids.put(folderName, id);
        parents.put(id, parent);
    }

    @Override
    public String getAlias(int id) {
        return names.get(id);
    }

    @Override
    public int getId(String alias, int parent) {
        if (! ids.containsKey(alias)) {
            return NOT_REGISTERED;
        }
        int id = ids.get(alias);
        int realParent = parents.get(id);
        if (realParent != parent) {
            return NOT_REGISTERED;
        }
        return id;
    }
}
