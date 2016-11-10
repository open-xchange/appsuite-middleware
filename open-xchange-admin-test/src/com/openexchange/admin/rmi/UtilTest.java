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

package com.openexchange.admin.rmi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Vector;
import org.junit.Test;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;
import com.openexchange.admin.rmi.dataobjects.Server;
import junit.framework.JUnit4TestAdapter;

/**
 *
 * @author cutmasta
 */
public class UtilTest extends AbstractTest {

    private OXUtilInterface getUtilClient() throws NotBoundException, MalformedURLException, RemoteException {
        return (OXUtilInterface) Naming.lookup(getRMIHostUrl() + OXUtilInterface.RMI_NAME);
    }

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(UtilTest.class);
    }

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
        client_db.setUrl("jdbc:mysql://" + hostname + "/?useUnicode=true&characterEncoding=UTF-8&" + "autoReconnect=true&useUnicode=true&useServerPrepStmts=false&useTimezone=true&" + "serverTimezone=UTC&connectTimeout=15000&socketTimeout=15000");
        client_db.setClusterWeight(100);
        client_db.setMasterId(0);
        return client_db;
    }

    @Test
    public void testListMaintenanceReasons() throws Exception {

        OXUtilInterface oxu = getUtilClient();

        Vector<MaintenanceReason> c_reasons = new Vector<MaintenanceReason>();
        // add some reasons
        for (int a = 0; a < 10; a++) {
            MaintenanceReason mr = new MaintenanceReason();
            mr.setText("testcase-get-all-reasons-" + a + "-" + System.currentTimeMillis());
            // add reason to system
            int[] srv_id = { oxu.createMaintenanceReason(mr, ContextTest.DummyMasterCredentials()).getId().intValue() };
            mr.setId(srv_id[0]);
            c_reasons.add(mr);
        }

        // now fetch all reasons, and look if my added reasons are within this data set        
        int resp = 0;
        MaintenanceReason[] srv_reasons = oxu.listMaintenanceReason("*", ContextTest.DummyMasterCredentials());
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

    @Test
    public void testRegisterServer() throws Exception {
        OXUtilInterface oxu = getUtilClient();

        Server reg_srv = new Server();
        reg_srv.setName("testcase-register-server-" + System.currentTimeMillis());

        reg_srv.setId(oxu.registerServer(reg_srv, ContextTest.DummyMasterCredentials()).getId());

        Server[] srv_resp = oxu.listServer("testcase-register-server-*", ContextTest.DummyMasterCredentials());
        int resp = 0;
        for (Server server : srv_resp) {
            if (server.getName().equals(reg_srv.getName()) && server.getId().intValue() == reg_srv.getId().intValue()) {
                resp++;
            }
        }
        //        for(int a = 0;a<=srv_resp.length;a++){
        //            
        //        }
        // resp muss 1 sein , ansonsten gibts 2 server mit selber id und name
        assertTrue("Expected 1 server", resp == 1);
    }

    @Test
    public void testUnregisterServer() throws Exception {
        OXUtilInterface oxu = getUtilClient();

        Server reg_srv = new Server();
        reg_srv.setName("testcase-register-server-" + System.currentTimeMillis());

        reg_srv.setId(oxu.registerServer(reg_srv, ContextTest.DummyMasterCredentials()).getId());

        Server[] srv_resp = oxu.listServer("testcase-register-server-*", ContextTest.DummyMasterCredentials());
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
        oxu.unregisterServer(sv, ContextTest.DummyMasterCredentials());

        srv_resp = oxu.listServer("testcase-register-server-*", ContextTest.DummyMasterCredentials());
        resp = 0;
        for (int a = 0; a < srv_resp.length; a++) {
            if (srv_resp[a].getName().equals(reg_srv.getName()) && srv_resp[a].getId().intValue() == reg_srv.getId().intValue()) {
                resp++;
            }
        }
        assertTrue("Expected that server is not found", resp == 0);
    }

    @Test
    public void testListServer() throws Exception {
        OXUtilInterface oxu = getUtilClient();

        Server client_srv = new Server();
        client_srv.setName("testcase-search-server-" + System.currentTimeMillis());
        client_srv.setId(oxu.registerServer(client_srv, ContextTest.DummyMasterCredentials()).getId());

        Server[] srv_response = oxu.listServer("testcase-search-server-*", ContextTest.DummyMasterCredentials());
        boolean found_srv = false;
        for (int a = 0; a < srv_response.length; a++) {
            Server tmp = srv_response[a];
            if (tmp.getId().intValue() == client_srv.getId().intValue() && tmp.getName().equals(client_srv.getName())) {
                found_srv = true;
            }
        }

        assertTrue("Expected to find registered server with data", found_srv);

    }
}
