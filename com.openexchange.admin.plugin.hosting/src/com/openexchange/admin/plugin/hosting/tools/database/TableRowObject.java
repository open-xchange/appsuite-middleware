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

import java.util.Enumeration;
import java.util.Hashtable;

/**
 *
 * @author cutmasta
 */
public class TableRowObject {

    private Hashtable<String, TableColumnObject> columns = null;

    /**
     * Creates a new instance of TableRowObject
     */
    public TableRowObject() {
        columns = new Hashtable<String, TableColumnObject>();
    }

    public void setColumn(TableColumnObject tc){
        columns.put(tc.getName(),tc);
    }

    public TableColumnObject getColumn(String name){
        return columns.get(name);
    }

    public Enumeration<String> getColumnNames(){
        return this.columns.keys();
    }

}
