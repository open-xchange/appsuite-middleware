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

package com.openexchange.tools.update;

import java.util.ArrayList;
import java.util.List;

/**
 * New implementation of a data class helping to deal with foreign keys.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
final class ForeignKey {

    private final String name;

    private final String primaryTable;

    private final String foreignTable;

    private final List<String> primaryColumns = new ArrayList<String>();

    private final List<String> foreignColumns = new ArrayList<String>();

    public ForeignKey(String name, String primaryTable, String foreignTable) {
        super();
        this.name = name;
        this.primaryTable = primaryTable;
        this.foreignTable = foreignTable;
    }

    public ForeignKey(String name, String primaryTable, String[] primaryColumns, String foreignTable, String[] foreignColumns) {
        this(name, primaryTable, foreignTable);
        for (int i = 0; i < primaryColumns.length; i++) {
            setPrimaryColumn(i, primaryColumns[i]);
            setForeignColumn(i, foreignColumns[i]);
        }
    }

    public String getName() {
        return name;
    }

    public String getPrimaryTable() {
        return primaryTable;
    }

    public String getForeignTable() {
        return foreignTable;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
        result = prime * result + primaryTable.hashCode();
        result = prime * result + foreignTable.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ForeignKey other = (ForeignKey) obj;
        return name.equals(other.name) && primaryTable.equals(other.primaryTable) && foreignTable.equals(other.foreignTable) && primaryColumns.equals(other.primaryColumns) && foreignColumns.equals(other.foreignColumns);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("FK ");
        sb.append(name);
        sb.append(' ');
        sb.append(foreignTable);
        sb.append(' ');
        sb.append(foreignColumns);
        sb.append(" references ");
        sb.append(primaryTable);
        sb.append(' ');
        sb.append(primaryColumns);
        return sb.toString();
    }

    public void setPrimaryColumn(int columnPos, String primaryColumn) {
        while (primaryColumns.size() <= columnPos) {
            primaryColumns.add(null);
        }
        primaryColumns.set(columnPos, primaryColumn);
    }

    public void setForeignColumn(int columnPos, String foreignColumn) {
        while (foreignColumns.size() <= columnPos) {
            foreignColumns.add(null);
        }
        foreignColumns.set(columnPos, foreignColumn);
    }

    public boolean isSame(ForeignKey other) {
        return null != other && name.equals(other.name) && primaryTable.equals(other.primaryTable) && foreignTable.equals(other.foreignTable);
    }

    public boolean matches(String[] otherPrimaryColumns, String[] otherForeignColumns) {
        boolean matches = primaryColumns.size() == foreignColumns.size() && primaryColumns.size() == otherPrimaryColumns.length && foreignColumns.size() == otherForeignColumns.length;
        for (int i = 0; matches && i < primaryColumns.size(); i++) {
            matches = primaryColumns.get(i).equals(otherPrimaryColumns[i]) && foreignColumns.get(i).equals(otherForeignColumns[i]);
        }
        return matches;
    }
}
