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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A foreign key represents a foreign key from a certain table referencing a column in a target table.
 *
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class ForeignKeyOld {

    private String sourceTable;

    private String targetTable;

    private String sourceColumn;

    private String targetColumn;

    private String name;

    public static List<ForeignKeyOld> getForeignKeys(final Connection con, final String sourceTable) throws SQLException {
        ResultSet rs = null;
        final ArrayList list = new ArrayList();
        try {
            final DatabaseMetaData dbMetaData = con.getMetaData();
            rs = dbMetaData.getImportedKeys(null, null, sourceTable);
            while (rs.next()) {
                final ForeignKeyOld key = new ForeignKeyOld();
                final String sourceColumn = rs.getString("FKCOLUMN_NAME");
                final String targetTable = rs.getString("PKTABLE_NAME");
                final String targetColumn = rs.getString("PKCOLUMN_NAME");
                final String name = rs.getString("FK_NAME");

                key.setSourceTable(sourceTable);
                key.setSourceColumn(sourceColumn);
                key.setTargetTable(targetTable);
                key.setTargetColumn(targetColumn);
                key.setName(name);

                list.add(key);

            }
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
        return list;
    }

    public ForeignKeyOld() {

    }

    public ForeignKeyOld(final String sourceTable, final String sourceColumn, final String targetTable, final String targetColumn) {
        this.sourceTable = sourceTable;
        this.targetTable = targetTable;
        this.sourceColumn = sourceColumn;
        this.targetColumn = targetColumn;
    }

    private void setTargetColumn(final String targetColumn) {
        this.targetColumn = targetColumn;
    }

    private void setSourceColumn(final String sourceColumn) {
        this.sourceColumn = sourceColumn;
    }

    public String getSourceTable() {
        return sourceTable;
    }

    public String getTargetTable() {
        return targetTable;
    }

    public String getSourceColumn() {
        return sourceColumn;
    }

    public String getTargetColumn() {
        return targetColumn;
    }

    public void setSourceTable(final String sourceTable) {
        this.sourceTable = sourceTable;
    }

    public void setTargetTable(final String targetTable) {
        this.targetTable = targetTable;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    private void loadName(final Connection con) throws SQLException {
        final List<ForeignKeyOld> keys = ForeignKeyOld.getForeignKeys(con, sourceTable);
        for (final ForeignKeyOld key : keys) {
            if (key.equals(this)) {
                name = key.getName();
                return;
            }
        }
        throw new SQLException("Foreign key not in database: " + this);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ForeignKeyOld)) {
            return false;
        }

        final ForeignKeyOld that = (ForeignKeyOld) o;

        if (sourceColumn != null ? !sourceColumn.equals(that.sourceColumn) : that.sourceColumn != null) {
            return false;
        }
        if (sourceTable != null ? !sourceTable.equals(that.sourceTable) : that.sourceTable != null) {
            return false;
        }
        if (targetColumn != null ? !targetColumn.equals(that.targetColumn) : that.targetColumn != null) {
            return false;
        }
        if (targetTable != null ? !targetTable.equals(that.targetTable) : that.targetTable != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        result = (sourceTable != null ? sourceTable.hashCode() : 0);
        result = 31 * result + (targetTable != null ? targetTable.hashCode() : 0);
        result = 31 * result + (sourceColumn != null ? sourceColumn.hashCode() : 0);
        result = 31 * result + (targetColumn != null ? targetColumn.hashCode() : 0);
        return result;
    }

    public void drop(final Connection con) throws SQLException {
        if (name == null) {
            loadName(con);
        }
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("ALTER TABLE " + sourceTable + " DROP FOREIGN KEY " + name);
            stmt.executeUpdate();
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
    }

    @Override
    public String toString() {
        String myName = name;
        if (myName == null) {
            myName = "unnamed";
        }
        return "FK: (" + name + ") " + sourceTable + "." + sourceColumn + " -> " + targetTable + "." + targetColumn;
    }

    public void create(final Connection con) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("ALTER TABLE " + sourceTable + " ADD FOREIGN KEY (`" + sourceColumn + "`) REFERENCES `" + targetTable + "` (`" + targetColumn + "`)");
            stmt.executeUpdate();
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
    }

    public void createIfNotExists(final Connection con) throws SQLException {
        final List<ForeignKeyOld> keys = ForeignKeyOld.getForeignKeys(con, sourceTable);

        if (!keys.contains(this)) {
            create(con);
        }

    }
}
