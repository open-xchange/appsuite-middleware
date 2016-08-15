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

package com.openexchange.database.internal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Test when a SQL connection stays in CLOSE_WAIT state.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class CloseWaitTest {

    public CloseWaitTest() {
        super();
    }

    public static void main(String[] args) throws SQLException, InterruptedException {
        // Check that wait_timeout and interactive_timeout are configured in MySQL to something like 80 seconds. Waits in this code need to be higher, that the server side wait timeouts.
        Connection con = DriverManager.getConnection("jdbc:mysql://slave01.devel.open-xchange.com/configdb?user=openexchange&password=secret&autoReconnect=false&socketTimeout=15000&connectTimeout=15000");
        // Wait for connection to go into CLOSE_WAIT state due to server side close.
        Thread.sleep(2* 60 * 1000);
        System.out.println("Check now connection state.");
        // Time to checkout whether connection is in CLOSE_WAIT.
        Thread.sleep(2* 60 * 1000);
        // What does Connector/J think about the connection?
        System.out.println("Closed? " + con.isClosed()); // -> false
        Statement stmt = con.createStatement();
        try {
            // Statement will fail due to closed socket and autoReconnect=false.
            stmt.execute("SELECT 1");
        } catch (SQLException e) {
            e.printStackTrace(); // Will show an error.
        }
        stmt.close();
        System.out.println("Closed? " + con.isClosed()); // -> true
        System.out.println("Check now connection state again.");
        // Time to check connection again. It will be gone away because Connector/J discovered that it is already closed. There will be no connection in CLOSE_WAIT state left.
        Thread.sleep(2* 60 * 1000);
        con.close(); // Does not give any error.
        // Time to checkout whether something changed but the connection should be already gone.
        System.out.println("Check now connection state again.");
        Thread.sleep(2* 60 * 1000);
    }
}
