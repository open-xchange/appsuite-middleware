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

package com.openexchange.datatypes.genericonf.storage;

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
        entries.remove(id);
    }

    @Override
    public void delete(Connection con, Context ctx, int id) throws OXException {
        entries.remove(id);
    }

    @Override
    public void delete(Connection writeConnection, Context ctx) throws OXException {
        entries.clear();
    }

    @Override
    public void fill(Context ctx, int id, Map<String, Object> content) throws OXException {
        if (!entries.containsKey(id)) {
            return;
        }
        content.putAll(entries.get(id));
    }

    @Override
    public void fill(Connection con, Context ctx, int id, Map<String, Object> content) throws OXException {
        if (!entries.containsKey(id)) {
            return;
        }
        content.putAll(entries.get(id));
    }

    @Override
    public int save(Context ctx, Map<String, Object> content) throws OXException {
        int id = currentId++;
        entries.put(id, content);
        return id;
    }

    @Override
    public int save(Connection con, Context ctx, Map<String, Object> content) throws OXException {
        int id = currentId++;
        entries.put(id, content);
        return id;
    }

    @Override
    public void update(Context ctx, int id, Map<String, Object> content) throws OXException {
        entries.put(id, content);
    }

    @Override
    public void update(Connection con, Context ctx, int id, Map<String, Object> content) throws OXException {
        entries.put(id, content);
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
