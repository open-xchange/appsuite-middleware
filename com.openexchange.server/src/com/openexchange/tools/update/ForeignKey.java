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
