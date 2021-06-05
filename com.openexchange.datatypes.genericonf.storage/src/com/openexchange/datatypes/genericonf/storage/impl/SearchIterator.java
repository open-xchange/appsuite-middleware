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

package com.openexchange.datatypes.genericonf.storage.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.datatypes.genericonf.IterationBreak;


/**
 * {@link SearchIterator}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class SearchIterator implements MapIterator<String, Object> {

    private final StringBuilder from = new StringBuilder();
    private final StringBuilder where = new StringBuilder();
    private final List<Object> queryReplacements = new LinkedList<>();
    private final Map<Class<?>, String> aliases = new HashMap<>();

    private boolean firstTable = true;
    private boolean stringsIncluded = false;
    private boolean boolsIncluded = false;
    
    
    /**
     * Initializes a new {@link SearchIterator}.
     */
    public SearchIterator() {
        super();
    }

    @Override
    public void handle(String key, Object object) throws IterationBreak {

        if (!stringsIncluded && object.getClass() == String.class) {
            stringTable();
        } else if (!boolsIncluded && object.getClass() == Boolean.class) {
            boolTable();
        }


        String prefix = getAlias(object.getClass());

        where.append("( ").append(prefix).append(".name = ? AND ").append(prefix).append(".value = ? ) AND ");
        queryReplacements.add(key);
        queryReplacements.add(object);
    }


    public String getWhere() {
        if (queryReplacements.isEmpty()) {
            return "1";
        }
        where.setLength(where.length()-4);
        return where.toString();
    }

    public String getFrom() {
        return from.toString();
    }


    public void setReplacements(PreparedStatement stmt) throws SQLException {
        for(int i = 0, size = queryReplacements.size(); i < size; i++) {
            stmt.setObject(i+1, queryReplacements.get(i));
        }
    }

    public void addReplacement(Object repl) {
        queryReplacements.add(repl);
    }


    private void stringTable() {
        if (stringsIncluded) {
            return;
        }
        if (firstTable) {
            from.append("genconf_attributes_strings AS p ");
            registerAlias(String.class, "p");
            firstTable = false;
        } else {
            from.append("JOIN genconf_attributes_strings AS str ON p.cid = str.cid AND p.id = str.id ");
            registerAlias(String.class, "str");
        }
        stringsIncluded = true;
    }

    private void boolTable() {
        if (boolsIncluded) {
            return;
        }
        if (firstTable) {
            from.append("genconf_attributes_bools AS p ");
            registerAlias(Boolean.class, "p");
            firstTable = false;
        } else {
            from.append("JOIN genconf_attributes_bools AS bool ON p.cid = bool.cid AND p.id = bool.id ");
            registerAlias(Boolean.class, "bool");
        }
        boolsIncluded = true;
    }

    private void registerAlias(Class<?> type, String alias) {
        aliases.put(type, alias);
    }

    private String getAlias(Class<?> type) {
        return aliases.get(type);
    }




}
