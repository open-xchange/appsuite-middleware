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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import java.sql.*;
import java.util.List;
import java.util.ArrayList;

/**
 * A foreign key represents a foreign key from a certain table referencing a column in a target table.
 *
 *
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class ForeignKey {
    private String sourceTable;
    private String targetTable;
    private String sourceColumn;
    private String targetColumn;

    private String name;

    public static List<ForeignKey> getForeignKeys(Connection con, String sourceTable) throws SQLException {
        ResultSet rs = null;
        ArrayList list = new ArrayList();
        try {
            DatabaseMetaData dbMetaData = con.getMetaData();
            rs = dbMetaData.getImportedKeys(null, null, sourceTable);
            while(rs.next()) {
                ForeignKey key = new ForeignKey();
                String sourceColumn = rs.getString("FKCOLUMN_NAME");
                String targetTable = rs.getString("PKTABLE_NAME");
                String targetColumn = rs.getString("PKCOLUMN_NAME");
                String name = rs.getString("FK_NAME");

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

    public ForeignKey() {
        
    }

    public ForeignKey(String sourceTable, String sourceColumn, String targetTable, String targetColumn) {
        this.sourceTable = sourceTable;
        this.targetTable = targetTable;
        this.sourceColumn = sourceColumn;
        this.targetColumn = targetColumn;
    }

    private void setTargetColumn(String targetColumn) {
        this.targetColumn = targetColumn;
    }

    private void setSourceColumn(String sourceColumn) {
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

    public void setSourceTable(String sourceTable) {
        this.sourceTable = sourceTable;
    }

    public void setTargetTable(String targetTable) {
        this.targetTable = targetTable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private void loadName(Connection con) throws SQLException {
        List<ForeignKey> keys = ForeignKey.getForeignKeys(con, sourceTable);
        for (ForeignKey key : keys) {
            if(key.equals(this)) {
                this.name = key.getName();
                return;
            }
        }
        throw new SQLException("Foreign key not in database: "+this);
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ForeignKey)) return false;

        ForeignKey that = (ForeignKey) o;

        if (sourceColumn != null ? !sourceColumn.equals(that.sourceColumn) : that.sourceColumn != null) return false;
        if (sourceTable != null ? !sourceTable.equals(that.sourceTable) : that.sourceTable != null) return false;
        if (targetColumn != null ? !targetColumn.equals(that.targetColumn) : that.targetColumn != null) return false;
        if (targetTable != null ? !targetTable.equals(that.targetTable) : that.targetTable != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (sourceTable != null ? sourceTable.hashCode() : 0);
        result = 31 * result + (targetTable != null ? targetTable.hashCode() : 0);
        result = 31 * result + (sourceColumn != null ? sourceColumn.hashCode() : 0);
        result = 31 * result + (targetColumn != null ? targetColumn.hashCode() : 0);
        return result;
    }

    public void drop(Connection con) throws SQLException{
        if(name == null) {
            loadName(con);
        }
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("ALTER TABLE "+sourceTable+" DROP FOREIGN KEY "+name);
            stmt.executeUpdate();
        } finally {
            if(stmt != null) {
                stmt.close();
            }
        }
    }

    public String toString() {
        String myName = name;
        if(myName == null) { myName = "unnamed"; }
        return "FK: ("+name+") "+sourceTable+"."+sourceColumn+" -> "+targetTable+"."+targetColumn;
    }

    public void create(Connection con) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("ALTER TABLE "+sourceTable+" ADD FOREIGN KEY (`"+sourceColumn+"`) REFERENCES `"+targetTable+"` (`"+targetColumn+"`)");
            stmt.executeUpdate();
        } finally {
            if(stmt != null) {
                stmt.close();
            }
        }
    }

    public void createIfNotExists(Connection con) throws SQLException {
        List<ForeignKey> keys = ForeignKey.getForeignKeys(con, sourceTable);

        if(! keys.contains(this))  { create(con); }

    }
}
