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

/*
 * DataFetcher.java
 */

package com.openexchange.admin.tools.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Vector;

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
    public Vector<TableObject> fetchTableObjects() throws SQLException;

    /**
     * Returns an sorted list of tables with their data.<br>
     * Needed for contraints and primarys etc.
     */
    public Vector<TableObject> sortTableObjects() throws SQLException;
}
