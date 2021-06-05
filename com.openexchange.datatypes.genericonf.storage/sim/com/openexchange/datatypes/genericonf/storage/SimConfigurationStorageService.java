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

package com.openexchange.datatypes.genericonf.storage;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class SimConfigurationStorageService implements GenericConfigurationStorageService {

    private static int currentId = 0;

    public Map<Integer, Map<String, Object>> entries = new HashMap<Integer, Map<String, Object>>();

    @Override
    public void delete(Context ctx, int id) throws OXException {
        entries.remove(I(id));
    }

    @Override
    public void delete(Connection con, Context ctx, int id) throws OXException {
        entries.remove(I(id));
    }

    @Override
    public void delete(Connection writeConnection, Context ctx) throws OXException {
        entries.clear();
    }

    @Override
    public void fill(Context ctx, int id, Map<String, Object> content) throws OXException {
        if (!entries.containsKey(I(id))) {
            return;
        }
        content.putAll(entries.get(I(id)));
    }

    @Override
    public void fill(Connection con, Context ctx, int id, Map<String, Object> content) throws OXException {
        if (!entries.containsKey(I(id))) {
            return;
        }
        content.putAll(entries.get(I(id)));
    }

    @Override
    public int save(Context ctx, Map<String, Object> content) throws OXException {
        int id = currentId++;
        entries.put(I(id), content);
        return id;
    }

    @Override
    public int save(Connection con, Context ctx, Map<String, Object> content) throws OXException {
        int id = currentId++;
        entries.put(I(id), content);
        return id;
    }

    @Override
    public void update(Context ctx, int id, Map<String, Object> content) throws OXException {
        entries.put(I(id), content);
    }

    @Override
    public void update(Connection con, Context ctx, int id, Map<String, Object> content) throws OXException {
        entries.put(I(id), content);
    }

    @Override
    public List<Integer> search(Context ctx, Map<String, Object> query) throws OXException {
        return search(query);
    }

    @Override
    public List<Integer> search(Connection con, Context ctx, Map<String, Object> query) throws OXException {
        return search(query);
    }

    private List<Integer> search(Map <String, Object> query) {
        List<Integer> retval = new ArrayList<Integer>();

        Set<Integer> keySet = entries.keySet();
        for (Iterator<Integer> iter = keySet.iterator(); iter.hasNext();) {
            Set<String> queryKeySet = query.keySet();
            Integer currentId = iter.next();
            Map<String, Object> currentMap = entries.get(currentId);
            if (currentMap.size() != query.size()) {
                continue;
            }
            boolean check = true;
            for (Iterator<String> queryIterator = queryKeySet.iterator(); iter.hasNext();) {
                String queryString = queryIterator.next();
                Object queryObject = query.get(queryString);
                if (!currentMap.containsKey(queryString) || !currentMap.get(queryString).equals(queryObject)) {
                    check = false;
                    break;
                }
            }
            if (check) {
                retval.add(currentId);
            }
        }

        return retval;
    }

}
