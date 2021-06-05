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

/*
 * DataFetcher.java
 */

package com.openexchange.admin.plugin.hosting.tools.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author cutmasta
 *
 * Should retrieve all data which is related to an context.
 *
 */
public interface DataFetcher {

     public String getCatalogName();

     public Connection getDbConnection();

     public void setDbConnection(Connection dbConnection,String catalog_name) throws SQLException;

     public String getMatchingColumn();

     /**
     * Set the column which should be matched
     */
     public void setMatchingColumn(String column_name);

     public int getColumnMatchType();

     public Object getColumnMatchObject();

     // fetches data for a table object
     public TableObject getDataForTable(TableObject to) throws SQLException;

     /**
     * Sets the criteria match object and its correspoding type.<br>
     * For example to match an integer:<br>
     * setCriteriaMatchObject(1337,java.sql.Types.INTEGER)
     */
     public void setColumnMatchObject(Object match_obj,int match_type);

     /**
     * Returns an unsorted list of tables with their data.<br>
     * Perhaps tables must be sorted cause of contraints etc.
     */
    public List<TableObject> fetchTableObjects() throws SQLException;

    /**
     * Returns an sorted list of tables with their data.<br>
     * Needed for contraints and primarys etc.
     */
    public List<TableObject> sortTableObjects() throws SQLException;
}
