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
import junit.framework.JUnit4TestAdapter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.admin.rmi.dataobjects.Database;

/**
 * @author cutmasta
 */
public class UtilDatabaseTest extends AbstractTest {

    private OXUtilInterface oxu;

    private Database client_db;

    private String db_name;

    private OXUtilInterface getUtilClient() throws NotBoundException, MalformedURLException, RemoteException {
        return (OXUtilInterface) Naming.lookup(getRMIHostUrl() + OXUtilInterface.RMI_NAME);
    }

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(UtilDatabaseTest.class);
    }

    public UtilDatabaseTest() throws MalformedURLException, RemoteException, NotBoundException {
        oxu = getUtilClient();
    }

    @Before
    public final void setup() throws Exception {
        db_name = "db_" + System.currentTimeMillis();
        client_db = UtilTest.getTestDatabaseObject("localhost", db_name);
        if (null == client_db) {
            throw new NullPointerException("Database object is null");
        }
        client_db.setId(oxu.registerDatabase(client_db, ContextTest.DummyMasterCredentials()).getId());
    }

    @After
    public final void tearDown() throws Exception {
        if (client_db != null) {
            oxu.unregisterDatabase(new Database(client_db.getId()), ContextTest.DummyMasterCredentials());
        }
        db_name = null;
        client_db = null;
    }

    @Test
    public void testRegisterDatabase() throws Exception {

        Database[] srv_dbs = oxu.listDatabase("db_*", ContextTest.DummyMasterCredentials());
        boolean found_db = false;
        for (int a = 0; a < srv_dbs.length; a++) {
            Database tmp = srv_dbs[a];
            // we found our added db, check now the data
            if (tmp.getId().equals(client_db.getId())) {
                // check if data is same
                assertEquals(client_db.getName(), tmp.getName());
                assertEquals(client_db.getDriver(), tmp.getDriver());
                assertEquals(client_db.getLogin(), tmp.getLogin());
                assertEquals(client_db.getMaxUnits(), tmp.getMaxUnits());
                assertEquals(client_db.getPassword(), tmp.getPassword());
                assertEquals(client_db.getPoolHardLimit(), tmp.getPoolHardLimit());
                assertEquals(client_db.getPoolInitial(), tmp.getPoolInitial());
                assertEquals(client_db.getPoolMax(), tmp.getPoolMax());
                assertEquals(client_db.getUrl(), tmp.getUrl());
                found_db = true;
            }
        }

        assertTrue("Expected to find registered db with data", found_db);

    }

    @Test
    public void testChangeDatabase() throws Exception {
        Database[] srv_dbs = oxu.listDatabase("db_*", ContextTest.DummyMasterCredentials());
        boolean found_db = false;
        for (int a = 0; a < srv_dbs.length; a++) {
            Database tmp = srv_dbs[a];
            // we found our added db, check now the data
            if (tmp.getId().equals(client_db.getId())) {
                // check if data is same
                assertEquals(client_db.getName(), tmp.getName());
                assertEquals(client_db.getDriver(), tmp.getDriver());
                assertEquals(client_db.getLogin(), tmp.getLogin());
                assertEquals(client_db.getMaxUnits(), tmp.getMaxUnits());
                assertEquals(client_db.getPassword(), tmp.getPassword());
                assertEquals(client_db.getPoolHardLimit(), tmp.getPoolHardLimit());
                assertEquals(client_db.getPoolInitial(), tmp.getPoolInitial());
                assertEquals(client_db.getPoolMax(), tmp.getPoolMax());
                assertEquals(client_db.getUrl(), tmp.getUrl());
                found_db = true;
            }
        }

        assertTrue("Expected to find registered db with data", found_db);

        // now change the db data and fetch it again
        client_db.setName(client_db.getName() + change_suffix);
        client_db.setDriver(client_db.getDriver() + change_suffix);
        client_db.setLogin(client_db.getLogin() + change_suffix);
        client_db.setMaxUnits(2000);
        client_db.setPassword(client_db.getPassword());
        client_db.setPoolHardLimit(40);
        client_db.setPoolInitial(4);
        client_db.setPoolMax(200);
        client_db.setUrl(client_db.getUrl() + change_suffix);

        // change db data
        oxu.changeDatabase(client_db, ContextTest.DummyMasterCredentials());

        srv_dbs = oxu.listDatabase("db_*", ContextTest.DummyMasterCredentials());

        for (int a = 0; a < srv_dbs.length; a++) {
            Database tmp = srv_dbs[a];
            // we found our added db, check now the data
            if (tmp.getId() == client_db.getId()) {
                // check if data is same
                assertEquals(client_db.getName(), tmp.getName());
                assertEquals(client_db.getDriver(), tmp.getDriver());
                assertEquals(client_db.getLogin(), tmp.getLogin());
                assertEquals(client_db.getMaxUnits(), tmp.getMaxUnits());
                assertEquals(client_db.getPassword(), tmp.getPassword());
                assertEquals(client_db.getPoolHardLimit(), tmp.getPoolHardLimit());
                assertEquals(client_db.getPoolInitial(), tmp.getPoolInitial());
                assertEquals(client_db.getPoolMax(), tmp.getPoolMax());
                assertEquals(client_db.getUrl(), tmp.getUrl());
            }
        }
    }

    @Test
    public void testUnregisterDatabase() throws Exception {
        Database[] srv_dbs = oxu.listDatabase("db_*", ContextTest.DummyMasterCredentials());
        boolean found_db = false;
        for (int a = 0; a < srv_dbs.length; a++) {
            Database tmp = srv_dbs[a];
            // we found our added db, check now the data
            if (tmp.getId().equals(client_db.getId())) {
                // check if data is same
                assertEquals(client_db.getName(), tmp.getName());
                assertEquals(client_db.getDriver(), tmp.getDriver());
                assertEquals(client_db.getLogin(), tmp.getLogin());
                assertEquals(client_db.getMaxUnits(), tmp.getMaxUnits());
                assertEquals(client_db.getPassword(), tmp.getPassword());
                assertEquals(client_db.getPoolHardLimit(), tmp.getPoolHardLimit());
                assertEquals(client_db.getPoolInitial(), tmp.getPoolInitial());
                assertEquals(client_db.getPoolMax(), tmp.getPoolMax());
                assertEquals(client_db.getUrl(), tmp.getUrl());
                found_db = true;
            }
        }

        assertTrue("Expected to find registered db with data", found_db);

        // now unregister database
        oxu.unregisterDatabase(new Database(client_db.getId()), ContextTest.DummyMasterCredentials());

        srv_dbs = oxu.listDatabase("db_*", ContextTest.DummyMasterCredentials());
        found_db = false;
        for (int a = 0; a < srv_dbs.length; a++) {
            Database tmp = srv_dbs[a];
            // we found our added db, check now the data
            if (tmp.getId() == client_db.getId()) {
                found_db = true;
            }
        }

        assertTrue("Expected that the database is no more registered", !found_db);

        // Set client_db = null because it is already unregistered
        client_db = null;
    }

    @Test
    public void testListDatabase() throws Exception {
        Database[] srv_dbs = oxu.listDatabase("db_*", ContextTest.DummyMasterCredentials());
        boolean found_db = false;
        for (int a = 0; a < srv_dbs.length; a++) {
            Database tmp = srv_dbs[a];
            // we found our added db, check now the data
            if (tmp.getId().equals(client_db.getId())) {
                // check if data is same
                if (null != tmp.getName() && tmp.getName().equals(db_name) && null != tmp.getDriver() && tmp.getDriver().equals(
                    client_db.getDriver()) && null != tmp.getLogin() && tmp.getLogin().equals(client_db.getLogin()) && null != tmp.isMaster() && tmp.isMaster().equals(
                    client_db.isMaster()) && null != tmp.getMaxUnits() && tmp.getMaxUnits().equals(client_db.getMaxUnits()) && null != tmp.getPassword() && tmp.getPassword().equals(
                    client_db.getPassword()) && null != tmp.getPoolHardLimit() && tmp.getPoolHardLimit().equals(
                    client_db.getPoolHardLimit()) && null != tmp.getPoolInitial() && tmp.getPoolInitial().equals(client_db.getPoolInitial()) && null != tmp.getPoolMax() && tmp.getPoolMax().equals(
                    client_db.getPoolMax()) && null != tmp.getUrl() && tmp.getUrl().equals(client_db.getUrl())) {
                    found_db = true;
                }
            }
        }

        assertTrue("Expected to find registered db with data", found_db);
    }
}
