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

package com.openexchange.admin.tools.database;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

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
    private Vector<TableColumnObject> columns = null;
    // all rows in table
    private Vector<TableRowObject> table_rows = null;

    // Holds all infos about a table
    public TableObject() {
        table_rows = new Vector<TableRowObject>();
        xreferencetables = new HashSet<String>();
        referencedby = new HashSet<String>();
        columns = new Vector<TableColumnObject>();
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

    public Vector<TableColumnObject> getColumns() {
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
        if (xreferencetables.size() > 0) {
            return true;
        } else {
            return false;
        }
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
        if (referencedby.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isReferencedBy(final TableObject checkref) {
        return referencedby.contains(checkref.getName());
    }

    @Override
    public int compareTo(String o) {
        return name.compareTo(o);
    }
}
