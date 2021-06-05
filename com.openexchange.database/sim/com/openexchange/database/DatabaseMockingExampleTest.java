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

package com.openexchange.database;

import static com.openexchange.database.DatabaseMocking.connection;
import static com.openexchange.database.DatabaseMocking.verifyConnection;
import static com.openexchange.database.DatabaseMocking.whenConnection;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.Test;

/**
 * {@link DatabaseMockingExampleTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class DatabaseMockingExampleTest {
    
     @Test
     public void testStubbingSingleRows() throws SQLException {
        Connection con = connection();
        
        whenConnection(con).isQueried("SELECT login, id FROM users WHERE loginName = ?").withParameter("cisco").thenReturnColumns("login", "id").andRow("cisco", 12);
        whenConnection(con).isQueried("SELECT login, id FROM users WHERE loginName = ?").withParameter("thorben").thenReturnColumns("login id").andRow("thorben", 13);
    
        PreparedStatement stmt = con.prepareStatement("SELECT login, id FROM users WHERE loginName = ?");
        
        stmt.setString(1, "cisco");
        ResultSet rs = stmt.executeQuery();
        
        assertTrue(rs.next());
        assertEquals("cisco", rs.getString("login"));
        assertEquals("cisco", rs.getString(1));
        assertEquals(12, rs.getInt("id"));
        assertEquals(12, rs.getInt(2));
        assertFalse(rs.next());
        
        stmt = con.prepareStatement("SELECT login, id FROM users WHERE loginName = ?");
        
        stmt.setString(1, "thorben");
        rs = stmt.executeQuery();
        
        assertTrue(rs.next());
        assertEquals("thorben", rs.getString("login"));
        assertEquals("thorben", rs.getString(1));
        assertEquals(13, rs.getInt("id"));
        assertEquals(13, rs.getInt(2));
        assertFalse(rs.next());
    }
    
     @Test
     public void testStubbingMultipleRows() throws SQLException {
        Connection con = connection();
        
        whenConnection(con).isQueried("SELECT title FROM songs WHERE folder = ?").withParameter(12).thenReturnColumns("title").andRow("Babylon").andRow("Somewhere over the Rainbow").andRow("Manic Monday");
        
        PreparedStatement stmt = con.prepareStatement("SELECT title FROM songs WHERE folder = ?");
        stmt.setInt(1, 12);
        
        ResultSet rs = stmt.executeQuery();
        
        assertTrue(rs.next());
        assertEquals("Babylon", rs.getString("title"));
        
        assertTrue(rs.next());
        assertEquals("Somewhere over the Rainbow", rs.getString("title"));
        
        assertTrue(rs.next());
        assertEquals("Manic Monday", rs.getString("title"));
        
        assertFalse(rs.next());
    }
    
     @Test
     public void testVerify() throws SQLException {
        Connection con = connection();
                
        PreparedStatement stmt = con.prepareStatement("INSERT INTO songs (title, folder) VALUES (?, ?)");
        stmt.setString(1, "Somewhere over the Rainbow");
        stmt.setInt(2, 12);
        
        stmt.executeUpdate();
        
        
        verifyConnection(con).receivedQuery("INSERT INTO songs (title, folder) VALUES (?, ?)").withParameter("Somewhere over the Rainbow").andParameter(12);
        
    }
    
     @Test
     public void testCombined() throws SQLException {
        Connection con = connection();
        
        whenConnection(con).isQueried("SELECT title FROM songs WHERE folder = ?").withParameter(12).thenReturnColumns("title").andRow("Babylon").andRow("Somewhere over the Rainbow").andRow("Manic Monday");

        PreparedStatement stmt = con.prepareStatement("SELECT title FROM songs WHERE folder = ?");
        stmt.setInt(1, 12);
        
        ResultSet rs = stmt.executeQuery();
        
        assertTrue(rs.next());
        assertEquals("Babylon", rs.getString("title"));
        
        assertTrue(rs.next());
        assertEquals("Somewhere over the Rainbow", rs.getString("title"));
        
        assertTrue(rs.next());
        assertEquals("Manic Monday", rs.getString("title"));
        
        assertFalse(rs.next());
        
        stmt = con.prepareStatement("INSERT INTO songs (title, folder) VALUES (?, ?)");
        stmt.setString(1, "Somewhere over the Rainbow");
        stmt.setInt(2, 12);
        
        stmt.executeUpdate();
        
        
        verifyConnection(con).receivedQuery("INSERT INTO songs (title, folder) VALUES (?, ?)").withParameter("Somewhere over the Rainbow").andParameter(12);

    }
}
