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
import org.junit.Before;
import org.junit.Test;
import com.openexchange.admin.rmi.dataobjects.Database;

/**
 * @author cutmasta
 */
public class UtilDatabaseTest extends AbstractRMITest {

    private Database client_db;

    private String db_name;

    public UtilDatabaseTest() {
        super();
    }

    @Before
    public final void setup() throws Exception {
        db_name = "db_" + System.currentTimeMillis();
        client_db = UtilTest.getTestDatabaseObject("localhost", db_name);
        if (null == client_db) {
            throw new NullPointerException("Database object is null");
        }
        client_db.setId(getDatabaseManager().register(client_db, Boolean.FALSE, Integer.valueOf(0)).getId());
    }

    @Test
    public void testRegisterDatabase() throws Exception {
        Database[] srv_dbs = getDatabaseManager().search("db_*");
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
        Database[] srv_dbs = getDatabaseManager().search("db_*");
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
        getDatabaseManager().change(client_db);

        srv_dbs = getDatabaseManager().search("db_*");

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
        Database[] srv_dbs = getDatabaseManager().search("db_*");
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
        getDatabaseManager().unregister(new Database(client_db.getId()));

        srv_dbs = getDatabaseManager().search("db_*");
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
        Database[] srv_dbs = getDatabaseManager().search("db_*");
        boolean found_db = false;
        for (int a = 0; a < srv_dbs.length; a++) {
            Database tmp = srv_dbs[a];
            // we found our added db, check now the data
            if (tmp.getId().equals(client_db.getId())) {
                // check if data is same
                //@formatter:off
                if (null != tmp.getName() && tmp.getName().equals(db_name) && null != tmp.getDriver() && tmp.getDriver().equals(
                    client_db.getDriver()) && null != tmp.getLogin() && tmp.getLogin().equals(client_db.getLogin()) && null != tmp.isMaster() && tmp.isMaster().equals(
                    client_db.isMaster()) && null != tmp.getMaxUnits() && tmp.getMaxUnits().equals(client_db.getMaxUnits()) && null != tmp.getPassword() && tmp.getPassword().equals(
                    client_db.getPassword()) && null != tmp.getPoolHardLimit() && tmp.getPoolHardLimit().equals(
                    client_db.getPoolHardLimit()) && null != tmp.getPoolInitial() && tmp.getPoolInitial().equals(client_db.getPoolInitial()) && null != tmp.getPoolMax() && tmp.getPoolMax().equals(
                    client_db.getPoolMax()) && null != tmp.getUrl() && tmp.getUrl().equals(client_db.getUrl())) {
                    found_db = true;
                }
                //@formatter:on
            }
        }

        assertTrue("Expected to find registered db with data", found_db);
    }
}
