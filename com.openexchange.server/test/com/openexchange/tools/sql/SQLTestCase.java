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

package com.openexchange.tools.sql;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
import org.junit.Before;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tx.ConfigurableDBProvider;
import com.openexchange.java.Streams;

/**
 * {@link SQLTestCase}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public abstract class SQLTestCase {
    private ConfigurableDBProvider dbProvider;
    protected Properties properties;

    @Before
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
        final String filename = System.getProperty("com.openexchange.test.sql.properties", "testconf/sql.properties");
        properties = new Properties();
        InputStream input = null;
        try {
            input = new FileInputStream(filename);
            properties.load(input);
        } finally {
            Streams.close(input);
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

    public void assertResult(final String sql) throws OXException, SQLException {
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

    public void assertNoResult(final String sql) throws OXException, SQLException {
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

    public void exec(final String sql, final Object...substitutes) throws SQLException, OXException {
        exec(sql, Arrays.asList(substitutes));
    }

    public void exec(final String sql) throws SQLException, OXException {
        exec(sql, new ArrayList<Object>(0));
    }

    public void exec(final String sql, final List<Object> substitues) throws SQLException, OXException {
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = getDBProvider().getReadConnection(null);
            stmt = con.prepareStatement(sql);
            int index = 1;
            for(final Object attr : substitues) {
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

    public List<Map<String, Object>> query(final String sql) throws SQLException, OXException {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        final List<Map<String, Object>> results = new LinkedList<Map<String, Object>>();

        try {
            con = getDBProvider().getReadConnection(null);
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery();
            final ResultSetMetaData metaData = rs.getMetaData();
            while (rs.next()) {
                final Map<String, Object> row = new HashMap<String, Object>();
                for(int i = 1; i <= metaData.getColumnCount(); i++) {
                    final String key = metaData.getColumnName(i);
                    final Object value = rs.getObject(i);
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
            if (rs != null) {
                rs.close();
            }
        }
        return results;
    }

    protected void copyTableStructure(final String origName, final String newName) throws OXException, SQLException {
        final Map<String, Object> createTableRow = query("SHOW CREATE TABLE "+origName).get(0);
        String createStatement = (String) createTableRow.get("Create Table");
        createStatement = createStatement.replaceAll(origName, newName);
        exec(createStatement);
    }

    protected void dropTable(final String tableName) throws OXException, SQLException {
        exec("DROP TABLE IF EXISTS "+tableName);
    }

    protected void insert(final String tableName, final Object...attrs) throws OXException, SQLException {
        final StringBuilder builder = new StringBuilder("INSERT INTO ").append(tableName).append(" (");
        final StringBuilder questionMarks = new StringBuilder();

        String key = null;
        final List<Object> values = new ArrayList<Object>();

        for(final Object attr : attrs) {
            if (key == null) {
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
        builder.append(") VALUES (").append(questionMarks).append(')');

        exec(builder.toString(), values);
    }


    protected void assertEntry(final String tableName, final Object...attrs) throws OXException, SQLException {
        final StringBuilder builder = new StringBuilder("SELECT 1 FROM ").append(tableName).append(" WHERE ");
        String key = null;
        for(final Object object : attrs) {
            if (key == null) {
                key = (String) object;
            } else {
                builder.append(key).append(" = ").append(object);
                key = null;
            }
        }
        assertResult(builder.toString());
    }
}
