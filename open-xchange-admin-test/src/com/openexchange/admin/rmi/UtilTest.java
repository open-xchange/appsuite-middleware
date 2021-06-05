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

package com.openexchange.admin.rmi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.Vector;
import org.junit.Test;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;
import com.openexchange.admin.rmi.dataobjects.Server;

/**
 * {@link UtilTest}
 * 
 * @author cutmasta
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class UtilTest extends AbstractRMITest {

    /**
     * Initialises a new {@link UtilTest}.
     */
    public UtilTest() {
        super();
    }

    /**
     * Tests listing maintenance reasons
     */
    @Test
    public void testListMaintenanceReasons() throws Exception {
        Vector<MaintenanceReason> c_reasons = new Vector<MaintenanceReason>();
        // add some reasons
        for (int a = 0; a < 10; a++) {
            MaintenanceReason mr = new MaintenanceReason();
            mr.setText("testcase-get-all-reasons-" + a + "-" + System.currentTimeMillis());
            // add reason to system
            int[] srv_id = { getMaintenanceReasonManager().create(mr).getId().intValue() };
            mr.setId(srv_id[0]);
            c_reasons.add(mr);
        }

        // now fetch all reasons, and look if my added reasons are within this data set
        int resp = 0;
        MaintenanceReason[] srv_reasons = getMaintenanceReasonManager().search("*");
        for (int c = 0; c < c_reasons.size(); c++) {

            MaintenanceReason tmp = c_reasons.get(c);

            for (int b = 0; b < srv_reasons.length; b++) {
                if (srv_reasons[b].getId().intValue() == tmp.getId().intValue() && srv_reasons[b].getText().equals(tmp.getText())) {
                    resp++;
                }
            }
        }

        // check if size is same, then all added reasons were found also in the data from server
        assertEquals(resp, c_reasons.size());

    }

    /**
     * Tests server registration
     */
    @Test
    public void testRegisterServer() throws Exception {
        Server reg_srv = new Server();
        reg_srv.setName("testcase-register-server-" + System.currentTimeMillis());
        reg_srv.setId(getServerManager().register(reg_srv).getId());

        Server[] srv_resp = getServerManager().search("testcase-register-server-*");
        int resp = 0;
        for (Server server : srv_resp) {
            if (server.getName().equals(reg_srv.getName()) && server.getId().intValue() == reg_srv.getId().intValue()) {
                resp++;
            }
        }
        assertTrue("Expected 1 server", resp == 1);
    }

    /**
     * Tests server un-registration
     */
    @Test
    public void testUnregisterServer() throws Exception {
        Server reg_srv = new Server();
        reg_srv.setName("testcase-register-server-" + System.currentTimeMillis());

        reg_srv.setId(getServerManager().register(reg_srv).getId());

        Server[] srv_resp = getServerManager().search("testcase-register-server-*");
        int resp = 0;
        for (int a = 0; a < srv_resp.length; a++) {
            if (srv_resp[a].getName().equals(reg_srv.getName()) && srv_resp[a].getId().intValue() == reg_srv.getId().intValue()) {
                resp++;
            }
        }
        // resp muss 1 sein , ansonsten gibts 2 server mit selber id und name
        assertTrue("Expected 1 server", resp == 1);

        Server sv = new Server();
        sv.setId(reg_srv.getId());

        // here the server was added correctly to the server, now delete it
        getServerManager().unregister(sv);

        srv_resp = getServerManager().search("testcase-register-server-*");
        resp = 0;
        for (int a = 0; a < srv_resp.length; a++) {
            if (srv_resp[a].getName().equals(reg_srv.getName()) && srv_resp[a].getId().intValue() == reg_srv.getId().intValue()) {
                resp++;
            }
        }
        assertTrue("Expected that server is not found", resp == 0);
    }

    /**
     * Tests list servers
     */
    @Test
    public void testListServer() throws Exception {
        Server client_srv = new Server();
        client_srv.setName("testcase-search-server-" + System.currentTimeMillis());
        client_srv.setId(getServerManager().register(client_srv).getId());

        Server[] srv_response = getServerManager().search("testcase-search-server-*");
        boolean found_srv = false;
        for (int a = 0; a < srv_response.length; a++) {
            Server tmp = srv_response[a];
            if (tmp.getId().intValue() == client_srv.getId().intValue() && tmp.getName().equals(client_srv.getName())) {
                found_srv = true;
            }
        }

        assertTrue("Expected to find registered server with data", found_srv);

    }

    /////////////////// TODO: move to database factory ///////////////
    public static Database getTestDatabaseObject(String hostname, String name) {

        if (System.getProperty("rmi_test_dbhost") != null) {
            hostname = System.getProperty("rmi_test_dbhost");
        }

        Database client_db = new Database();
        client_db.setName(name);
        client_db.setDriver("com.mysql.jdbc.Driver");
        client_db.setLogin("openexchange");
        client_db.setMaster(true);
        client_db.setMaxUnits(1000);
        String dbpw = "secret";
        if (System.getProperty("rmi_test_dbpw") != null) {
            dbpw = System.getProperty("rmi_test_dbpw");
        }
        client_db.setPassword(dbpw);
        client_db.setPoolHardLimit(20);
        client_db.setPoolInitial(2);
        client_db.setPoolMax(100);
        client_db.setUrl("jdbc:mysql://" + hostname);
        client_db.setMasterId(0);
        return client_db;
    }
}
