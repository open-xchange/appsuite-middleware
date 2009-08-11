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

package com.openexchange.tools.sql;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import com.openexchange.groupware.tx.ConfigurableDBProvider;
import com.openexchange.groupware.tx.DBProvider;
import com.openexchange.groupware.tx.TransactionException;
import junit.framework.TestCase;

/**
 * {@link SQLTestCase}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public abstract class SQLTestCase extends TestCase {

    private ConfigurableDBProvider dbProvider;
    protected Properties properties;

    public void setUp() throws Exception {
        loadProperties();
        dbProvider = new ConfigurableDBProvider();
        dbProvider.setDriver(getDriver());
        dbProvider.setLogin(getLogin());
        dbProvider.setPassword(getPassword());
        dbProvider.setUrl(getUrl());
    }

    public DBProvider getDBProvider() {
        return dbProvider;
    }

    protected void loadProperties() throws IOException {
        String filename = System.getProperty("com.openexchange.test.sql.properties", "conf/sql.properties");
        properties = new Properties();
        InputStream input = null;
        try {
            input = new FileInputStream(filename);
            properties.load(input);
        } finally {
            if (input != null) {
                input.close();
            }
        }
    }

    public String getDriver() {
        return properties.getProperty("driver");
    }

    public String getLogin() {
        return properties.getProperty("login");
    }

    public String getPassword() {
        return properties.getProperty("password");
    }

    public String getUrl() {
        return properties.getProperty("url");
    }

    public void assertResult(String sql) throws TransactionException, SQLException {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            con = getDBProvider().getReadConnection(null);
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery();
            assertTrue(sql + " had no result", rs.next());
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (stmt != null) {
                stmt.close();
            }
            if (con != null) {
                getDBProvider().releaseReadConnection(null, con);
            }
        }
    }

    public void assertNoResult(String sql) throws TransactionException, SQLException {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            con = getDBProvider().getReadConnection(null);
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery();
            assertFalse(sql + " had a result!", rs.next());
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (stmt != null) {
                stmt.close();
            }
            if (con != null) {
                getDBProvider().releaseReadConnection(null, con);
            }
        }
    }

    public void exec(String sql, Object...substitutes) throws TransactionException, SQLException {
        exec(sql, Arrays.asList(substitutes));
    }
    
    public void exec(String sql) throws TransactionException, SQLException {
        exec(sql, new ArrayList<Object>(0));
    }
    
    public void exec(String sql, List<Object> substitues) throws SQLException, TransactionException {
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = getDBProvider().getReadConnection(null);
            stmt = con.prepareStatement(sql);
            int index = 1;
            for(Object attr : substitues) {
                stmt.setObject(index++, attr);
            }
            stmt.execute();
        } finally {
            if (stmt != null) {
                stmt.close();
            }
            if (con != null) {
                getDBProvider().releaseReadConnection(null, con);
            }
        }
    }
    
    public List<Map<String, Object>> query(String sql) throws TransactionException, SQLException {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        List<Map<String, Object>> results = new LinkedList<Map<String, Object>>();
        
        try {
            con = getDBProvider().getReadConnection(null);
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            while(rs.next()) {
                Map<String, Object> row = new HashMap<String, Object>();
                for(int i = 1; i <= metaData.getColumnCount(); i++) {
                    String key = metaData.getColumnName(i);
                    Object value = rs.getObject(i);
                    row.put(key, value);
                }
                results.add(row);
            }
            
        } finally {
            if (stmt != null) {
                stmt.close();
            }
            if (con != null) {
                getDBProvider().releaseReadConnection(null, con);
            }
            if(rs != null) {
                rs.close();
            }
        }
        return results;
    }
    
    protected void copyTableStructure(String origName, String newName) throws TransactionException, SQLException {
        Map<String, Object> createTableRow = query("SHOW CREATE TABLE "+origName).get(0);
        String createStatement = (String) createTableRow.get("Create Table");
        createStatement = createStatement.replaceAll(origName, newName);
        exec(createStatement);
    }
    
    protected void dropTable(String tableName) throws TransactionException, SQLException {
        exec("DROP TABLE IF EXISTS "+tableName);
    }
    
    protected void insert(String tableName, Object...attrs) throws TransactionException, SQLException {
        StringBuilder builder = new StringBuilder("INSERT INTO ").append(tableName).append(" ");
        StringBuilder questionMarks = new StringBuilder();
        
        String key = null;
        List<Object> values = new ArrayList<Object>();
        
        for(Object attr : attrs) {
            if(key == null) {
                key = (String) attr;
                builder.append(key).append(", ");
            } else {
                values.add(attr);
                questionMarks.append("?, ");
                key = null;
            }
        }
        
        builder.setLength(builder.length()-2);
        questionMarks.setLength(questionMarks.length()-2);
        builder.append(" (").append(questionMarks).append(")");
        
        exec(builder.toString(), values);
    }
}
