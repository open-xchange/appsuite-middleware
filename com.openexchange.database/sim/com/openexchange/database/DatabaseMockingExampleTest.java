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

package com.openexchange.database;

import static org.junit.Assert.*;
import static com.openexchange.database.DatabaseMocking.*;
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
