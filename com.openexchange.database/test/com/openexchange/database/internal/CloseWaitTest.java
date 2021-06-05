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
        Connection con = DriverManager.getConnection("jdbc:mysql://slave01.devel.open-xchange.com/configdb?user=openexchange&password=secret&autoReconnect=false&socketTimeout=15000&connectTimeout=15000&useSSL=false");
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
