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

package com.openexchange.datatypes.genericonf.storage.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
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
    private final List<Object> queryReplacements = new ArrayList<Object>();
    private final Map<Class, String> aliases = new HashMap<Class, String>();


    private boolean firstTable = true;
    private boolean stringsIncluded = false;
    private boolean boolsIncluded = false;


    private static final ToSQLType toSQL = new ToSQLType();


    @Override
    public void handle(String key, Object object) throws IterationBreak {

        if(!stringsIncluded && object.getClass() == String.class) {
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
        if(queryReplacements.isEmpty()) {
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
        if(stringsIncluded) {
            return;
        }
        if(firstTable) {
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
        if(boolsIncluded) {
            return;
        }
        if(firstTable) {
            from.append("genconf_attributes_bools AS p ");
            registerAlias(Boolean.class, "p");
            firstTable = false;
        } else {
            from.append("JOIN genconf_attributes_bools AS bool ON p.cid = bool.cid AND p.id = bool.id ");
            registerAlias(Boolean.class, "bool");
        }
        boolsIncluded = true;
    }

    private void registerAlias(Class type, String alias) {
        aliases.put(type, alias);
    }

    private String getAlias(Class type) {
        return aliases.get(type);
    }




}
