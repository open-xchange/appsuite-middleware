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

package com.openexchange.admin.plugin.hosting.tools.database;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * A simple POJO that holds metadata information about an SQL table
 *
 * @author cutmasta
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class TableObject implements Comparable<String> {

    // table name
    private String name = null;

    private HashSet<String> xreferencetables = null;
    private HashSet<String> referencedby = null;
    private List<TableColumnObject> columns = null;
    // all rows in table
    private List<TableRowObject> table_rows = null;

    // Holds all infos about a table
    public TableObject(String tableName) {
        this();
        this.name = tableName;
    }

    // Holds all infos about a table
    public TableObject() {
        table_rows = new ArrayList<TableRowObject>();
        xreferencetables = new HashSet<String>();
        referencedby = new HashSet<String>();
        columns = new ArrayList<TableColumnObject>();
    }

    public String getName() {
        return name;
    }

    public void addColumn(TableColumnObject to) {
        this.columns.add(to);
    }

    public void removeColumn(TableColumnObject to) {
        this.columns.remove(to);
    }

    public List<TableColumnObject> getColumns() {
        return this.columns;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDataRow(TableRowObject tc) {
        this.table_rows.add(tc);
    }

    public TableRowObject getDataRow(int position) {
        return table_rows.get(position);
    }

    public int getDataRowCount() {
        return table_rows.size();
    }

    public void addCrossReferenceTable(String reference_table) {
        this.xreferencetables.add(reference_table);
    }

    public void removeCrossReferenceTable(String reference_table) {
        this.xreferencetables.remove(reference_table);
    }

    public Iterator<String> getCrossReferenceTables() {
        return this.xreferencetables.iterator();
    }

    public boolean hasCrossReferences() {
        //System.out.println(this.name+"--> "+xreferencetables.size());
        return (xreferencetables.size() > 0);
    }

    public boolean hasCrossReference2Table(TableObject checkref) {
        return this.xreferencetables.contains(checkref.getName());
    }

    public void addReferencedBy(String table_name) {
        this.referencedby.add(table_name);
    }

    public void removeReferencedBy(String reference_table) {
        this.referencedby.remove(reference_table);
    }

    public Iterator<String> getReferencedByTables() {
        return this.referencedby.iterator();
    }

    public boolean isReferencedByTables() {
        //System.out.println(this.name+"--> "+referencedby.size());
        return (referencedby.size() > 0);
    }

    public boolean isReferencedBy(final TableObject checkref) {
        return referencedby.contains(checkref.getName());
    }

    @Override
    public int compareTo(String o) {
        return name.compareTo(o);
    }
}
