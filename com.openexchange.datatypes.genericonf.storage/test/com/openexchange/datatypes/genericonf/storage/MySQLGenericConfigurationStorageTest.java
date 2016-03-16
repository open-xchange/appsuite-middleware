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

package com.openexchange.datatypes.genericonf.storage;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.datatypes.genericonf.storage.impl.MySQLGenericConfigurationStorage;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.SimContext;
import com.openexchange.java.util.UUIDs;
import com.openexchange.tools.sql.SQLTestCase;

/**
 * {@link MySQLGenericConfigurationStorageTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class MySQLGenericConfigurationStorageTest extends SQLTestCase {

    private MySQLGenericConfigurationStorage storage = null;

    @Override
    public void setUp() throws Exception {
        loadProperties();
        super.setUp();
        storage = new MySQLGenericConfigurationStorage();
        final DBProvider provider = getDBProvider();
        storage.setDBProvider(provider);
    }

    @Override
    public void tearDown() throws Exception {
        exec("DELETE FROM genconf_attributes_strings");
        exec("DELETE FROM genconf_attributes_bools");

        super.tearDown();
    }

    public void testSaveDynamicConfiguration() throws OXException, SQLException {
        final Map<String, Object> content = new HashMap<String, Object>();
        content.put("login", "loginname");
        content.put("otherValue", "other");
        content.put("booleanValue", true);


        final int contextId = 1;
        final Context ctx = new SimContext(contextId);
        final int id = storage.save(ctx, content);

        assertTrue("Id should not be 0", id > 0);

        assertResult("SELECT 1 FROM genconf_attributes_strings WHERE name = 'login' AND value = 'loginname' AND cid = 1 AND id = " + id);
        assertResult("SELECT 1 FROM genconf_attributes_strings WHERE name = 'otherValue' AND value = 'other' AND cid = 1 AND id = " + id);
        assertResult("SELECT 1 FROM genconf_attributes_bools WHERE name = 'booleanValue' AND value = 1 AND cid = 1 AND id = " + id);

    }

    public void testLoadDynamicConfiguration() throws SQLException, OXException {
        byte[] uuid = getUUID();
        exec("INSERT INTO genconf_attributes_strings (cid, id, name, value, uuid) VALUES (1002,1001,'field', 'value')");
        uuid = getUUID();
        exec("INSERT INTO genconf_attributes_strings (cid, id, name, value, uuid) VALUES (1002,1001,'otherField', 'otherValue')");
        uuid = getUUID();
        exec("INSERT INTO genconf_attributes_bools (cid, id, name, value, uuid) VALUES (1002,1001,'booleanField', 1)");

        final Map<String, Object> content = new HashMap<String, Object>();

        final int contextId = 1002;
        final Context ctx = new SimContext(contextId);

        storage.fill(ctx, 1001, content);


        assertNotNull("Expected a value under key 'field", content.get("field"));
        assertEquals("Excpected value 'value' under key 'field'", "value", content.get("field"));
        assertNotNull("Expected a value under key 'otherField", content.get("otherField"));
        assertEquals("Excpected value 'otherValue' under key 'otherField'", "otherValue", content.get("otherField"));
        assertNotNull("Expected a value under key 'booleanField", content.get("booleanField"));
        assertEquals("Excpected value true under key 'booleanField'", true, content.get("booleanField"));

    }

    public void testUpdateDynamicConfiguration() throws SQLException, OXException {
        byte[] uuid = getUUID();
        exec("INSERT INTO genconf_attributes_strings (cid, id, name, value, uuid) VALUES (1002,1001,'field', 'value', " + uuid + ")");
        uuid = getUUID();
        exec("INSERT INTO genconf_attributes_strings (cid, id, name, value, uuid) VALUES (1002,1001,'otherField', 'otherValue', " + uuid + ")");
        uuid = getUUID();
        exec("INSERT INTO genconf_attributes_strings (cid, id, name, value, uuid) VALUES (1002,1001,'thirdField', 'thirdValue', " + uuid + ")");
        uuid = getUUID();
        exec("INSERT INTO genconf_attributes_bools (cid, id, name, value, uuid) VALUES (1002,1001,'bool', 0, " + uuid + ")");
        uuid = getUUID();
        exec("INSERT INTO genconf_attributes_bools (cid, id, name, value, uuid) VALUES (1002,1001,'otherBool', 0, " + uuid + ")");
        uuid = getUUID();
        exec("INSERT INTO genconf_attributes_bools (cid, id, name, value, uuid) VALUES (1002,1001,'thirdBool', 0, " + uuid + ")");

        final Map<String, Object> content = new HashMap<String, Object>();


        content.put("field", "updatedValue");
        content.put("fourthField", "newValue");
        content.put("thirdField", null); // Will be removed

        content.put("bool", true);
        content.put("fourthBool", true);
        content.put("thirdBool", null); // Will be removed

        final int id = 1001;
        final int contextId = 1002;
        final Context ctx = new SimContext(contextId);

        storage.update(ctx, id, content);

        assertResult("SELECT 1 FROM genconf_attributes_strings WHERE name = 'field' AND value = 'updatedValue' AND cid = 1002 AND id = " + id);
        assertResult("SELECT 1 FROM genconf_attributes_strings WHERE name = 'otherField' AND value = 'otherValue' AND cid = 1002 AND id = " + id);
        assertResult("SELECT 1 FROM genconf_attributes_strings WHERE name = 'fourthField' AND value = 'newValue' AND cid = 1002 AND id = " + id);
        assertNoResult("SELECT 1 FROM genconf_attributes_strings WHERE name = 'thirdField' AND cid = 1002 AND id = " + id);

        assertResult("SELECT 1 FROM genconf_attributes_bools WHERE name = 'bool' AND value = 1 AND cid = 1002 AND id = " + id);
        assertResult("SELECT 1 FROM genconf_attributes_bools WHERE name = 'otherBool' AND value = 0 AND cid = 1002 AND id = " + id);
        assertResult("SELECT 1 FROM genconf_attributes_bools WHERE name = 'fourthBool' AND value = 1 AND cid = 1002 AND id = " + id);
        assertNoResult("SELECT 1 FROM genconf_attributes_bools WHERE name = 'thirdBool' AND cid = 1002 AND id = " + id);

    }

    public void testDeleteDynamicConfiguration() throws SQLException, OXException {
        byte[] uuid = getUUID();
        exec("INSERT INTO genconf_attributes_strings (cid, id, name, value, uuid) VALUES (1002,1001,'field', 'value', " + uuid + ")");
        uuid = getUUID();
        exec("INSERT INTO genconf_attributes_strings (cid, id, name, value, uuid) VALUES (1002,1001,'otherField', 'otherValue', " + uuid + ")");
        uuid = getUUID();
        exec("INSERT INTO genconf_attributes_strings (cid, id, name, value, uuid) VALUES (1002,1001,'thirdField', 'thirdValue', " + uuid + ")");
        uuid = getUUID();
        exec("INSERT INTO genconf_attributes_strings (cid, id, name, value, uuid) VALUES (1002,1004,'otherID', 'otherID', " + uuid + ")");
        uuid = getUUID();
        exec("INSERT INTO genconf_attributes_bools (cid, id, name, value, uuid) VALUES (1002,1001,'bool', 1, " + uuid + ")");
        uuid = getUUID();
        exec("INSERT INTO genconf_attributes_bools (cid, id, name, value, uuid) VALUES (1002,1001,'otherBool', 1, " + uuid + ")");
        uuid = getUUID();
        exec("INSERT INTO genconf_attributes_bools (cid, id, name, value, uuid) VALUES (1002,1001,'thirdBool', 1, " + uuid + ")");
        uuid = getUUID();
        exec("INSERT INTO genconf_attributes_bools (cid, id, name, value, uuid) VALUES (1002,1004,'otherBoolID', 1, " + uuid + ")");


        final int id = 1001;
        final int contextId = 1002;
        final Context ctx = new SimContext(contextId);

        storage.delete(ctx, id);

        assertNoResult("SELECT 1 FROM genconf_attributes_strings WHERE cid = 1002 AND id = " + id);
        assertResult("SELECT 1 FROM genconf_attributes_strings WHERE cid = 1002 AND id = 1004");

        assertNoResult("SELECT 1 FROM genconf_attributes_bools WHERE cid = 1002 AND id = " + id);
        assertResult("SELECT 1 FROM genconf_attributes_bools WHERE cid = 1002 AND id = 1004");

    }

    public void testSearchDynamicConfiguration() throws SQLException, OXException {
        byte[] uuid = getUUID();
        exec("INSERT INTO genconf_attributes_strings (cid, id, name, value, uuid) VALUES (1002,1001,'field', 'value', " + uuid + ")");
        uuid = getUUID();
        exec("INSERT INTO genconf_attributes_strings (cid, id, name, value, uuid) VALUES (1002,1001,'otherField', 'otherValue', " + uuid + ")");
        uuid = getUUID();
        exec("INSERT INTO genconf_attributes_strings (cid, id, name, value, uuid) VALUES (1002,1001,'thirdField', 'thirdValue', " + uuid + ")");
        uuid = getUUID();
        exec("INSERT INTO genconf_attributes_strings (cid, id, name, value, uuid) VALUES (1002,1004,'otherID', 'otherID', " + uuid + ")");
        uuid = getUUID();
        exec("INSERT INTO genconf_attributes_bools (cid, id, name, value, uuid) VALUES (1002,1001,'bool', 1, " + uuid + ")");
        uuid = getUUID();
        exec("INSERT INTO genconf_attributes_bools (cid, id, name, value, uuid) VALUES (1002,1001,'otherBool', 1, " + uuid + ")");
        uuid = getUUID();
        exec("INSERT INTO genconf_attributes_bools (cid, id, name, value, uuid) VALUES (1002,1001,'thirdBool', 1, " + uuid + ")");
        uuid = getUUID();
        exec("INSERT INTO genconf_attributes_bools (cid, id, name, value, uuid) VALUES (1002,1004,'otherBoolID', 1, " + uuid + ")");


        final Map<String, Object> query = new HashMap<String, Object>();
        query.put("field", "value");

        List<Integer> ids = storage.search(new SimContext(1002), query);
        assertNotNull("ids was null", ids);
        assertEquals("Wrong size of search result", 1, ids.size());
        assertEquals("Got wrong ID", new Integer(1001), ids.get(0));

        // Try with join

        query.put("bool", true);
        ids = storage.search(new SimContext(1002), query);
        assertNotNull("ids was null", ids);
        assertEquals("Wrong size of search result", 1, ids.size());
        assertEquals("Got wrong ID", new Integer(1001), ids.get(0));

    }

    // Save encrypted passwords
    
    //Generate uuid
    private byte[] getUUID() {
        UUID uuid = UUID.randomUUID();
        byte[] uuidBinary = UUIDs.toByteArray(uuid);
        return uuidBinary;
    }


}
