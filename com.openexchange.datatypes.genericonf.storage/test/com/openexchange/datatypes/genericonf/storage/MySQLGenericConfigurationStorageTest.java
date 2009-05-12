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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.datatypes.genericonf.storage.impl.MySQLGenericConfigurationStorage;
import com.openexchange.exceptions.StringComponent;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.SimContext;
import com.openexchange.groupware.tx.DBProvider;
import com.openexchange.groupware.tx.TransactionException;
import com.openexchange.test.sql.SQLTestCase;

/**
 * {@link MySQLGenericConfigurationStorageTest}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class MySQLGenericConfigurationStorageTest extends SQLTestCase {

    private MySQLGenericConfigurationStorage storage = null;

    public void setUp() throws Exception {
        GenericConfigStorageErrorMessage.EXCEPTIONS.setApplicationId("com.openexchange.genericonf.storage");
        GenericConfigStorageErrorMessage.EXCEPTIONS.setComponent(new StringComponent("GCS"));

        loadProperties();
        super.setUp();
        storage = new MySQLGenericConfigurationStorage();
        DBProvider provider = getDBProvider();
        storage.setDBProvider(provider);
    }

    public void tearDown() throws Exception {
        exec("DELETE FROM genconf_attributes_strings");
        exec("DELETE FROM genconf_attributes_bools");

        super.tearDown();
    }

    public void testSaveDynamicConfiguration() throws GenericConfigStorageException, TransactionException, SQLException {
        Map<String, Object> content = new HashMap<String, Object>();
        content.put("login", "loginname");
        content.put("otherValue", "other");
        content.put("booleanValue", true);

        DynamicFormDescription form = new DynamicFormDescription();
        form.add(FormElement.input("login", "loginname")).add(FormElement.input("otherValue", "Other Value")).add(
            FormElement.checkbox("booleanValue", ""));

        int contextId = 1;
        Context ctx = new SimContext(contextId);
        int id = storage.save(ctx, content, form);

        assertTrue("Id should not be 0", id > 0);

        assertResult("SELECT 1 FROM genconf_attributes_strings WHERE name = 'login' AND value = 'loginname' AND widget = 'input' AND cid = 1 AND id = " + id);
        assertResult("SELECT 1 FROM genconf_attributes_strings WHERE name = 'otherValue' AND value = 'other' AND widget = 'input' AND cid = 1 AND id = " + id);
        assertResult("SELECT 1 FROM genconf_attributes_bools WHERE name = 'booleanValue' AND value = 1 AND widget = 'checkbox' AND cid = 1 AND id = " + id);

    }

    public void testLoadDynamicConfiguration() throws TransactionException, SQLException, GenericConfigStorageException {
        exec("INSERT INTO genconf_attributes_strings (cid, id, name, value, widget) VALUES (1002,1001,'field', 'value', 'input')");
        exec("INSERT INTO genconf_attributes_strings (cid, id, name, value, widget) VALUES (1002,1001,'otherField', 'otherValue', 'input')");
        exec("INSERT INTO genconf_attributes_bools (cid, id, name, value, widget) VALUES (1002,1001,'booleanField', 1, 'checkbox')");

        Map<String, Object> content = new HashMap<String, Object>();
        DynamicFormDescription form = new DynamicFormDescription();

        int contextId = 1002;
        Context ctx = new SimContext(contextId);

        storage.fill(ctx, 1001, content, form);

        assertNotNull("Expected a form element with the name 'field'", form.getField("field"));
        assertEquals("Expected input with the name 'field'", form.getField("field").getWidget(), FormElement.Widget.INPUT);
        assertNotNull("Expected a form element with the name 'otherField'", form.getField("otherField"));
        assertEquals("Expected input with the name 'otherField'", form.getField("otherField").getWidget(), FormElement.Widget.INPUT);
        assertNotNull("Expected a form element with the name 'booleanField'", form.getField("booleanField"));
        assertEquals(
            "Expected checkbox with the name 'booleanField'",
            form.getField("booleanField").getWidget(),
            FormElement.Widget.CHECKBOX);

        assertNotNull("Expected a value under key 'field", content.get("field"));
        assertEquals("Excpected value 'value' under key 'field'", "value", content.get("field"));
        assertNotNull("Expected a value under key 'otherField", content.get("otherField"));
        assertEquals("Excpected value 'otherValue' under key 'otherField'", "otherValue", content.get("otherField"));
        assertNotNull("Expected a value under key 'booleanField", content.get("booleanField"));
        assertEquals("Excpected value true under key 'booleanField'", true, content.get("booleanField"));

    }

    public void testUpdateDynamicConfiguration() throws TransactionException, SQLException, GenericConfigStorageException {
        exec("INSERT INTO genconf_attributes_strings (cid, id, name, value, widget) VALUES (1002,1001,'field', 'value', 'input')");
        exec("INSERT INTO genconf_attributes_strings (cid, id, name, value, widget) VALUES (1002,1001,'otherField', 'otherValue', 'input')");
        exec("INSERT INTO genconf_attributes_strings (cid, id, name, value, widget) VALUES (1002,1001,'thirdField', 'thirdValue', 'input')");

        exec("INSERT INTO genconf_attributes_bools (cid, id, name, value, widget) VALUES (1002,1001,'bool', 0, 'checkbox')");
        exec("INSERT INTO genconf_attributes_bools (cid, id, name, value, widget) VALUES (1002,1001,'otherBool', 0, 'checkbox')");
        exec("INSERT INTO genconf_attributes_bools (cid, id, name, value, widget) VALUES (1002,1001,'thirdBool', 0, 'checkbox')");

        Map<String, Object> content = new HashMap<String, Object>();
        DynamicFormDescription form = new DynamicFormDescription();
        form.add(FormElement.input("field", "")).add(FormElement.input("otherField", "")).add(FormElement.input("thirdField", "")).add(
            FormElement.input("fourthField", "")).add(FormElement.checkbox("bool", "")).add(FormElement.checkbox("otherBool", "")).add(
            FormElement.checkbox("thirdBool", "")).add(FormElement.checkbox("fourthBool", ""));

        content.put("field", "updatedValue");
        content.put("fourthField", "newValue");
        content.put("thirdField", null); // Will be removed

        content.put("bool", true);
        content.put("fourthBool", true);
        content.put("thirdBool", null); // Will be removed
        
        int id = 1001;
        int contextId = 1002;
        Context ctx = new SimContext(contextId);

        storage.update(ctx, id, content, form);

        assertResult("SELECT 1 FROM genconf_attributes_strings WHERE name = 'field' AND value = 'updatedValue' AND widget = 'input' AND cid = 1002 AND id = " + id);
        assertResult("SELECT 1 FROM genconf_attributes_strings WHERE name = 'otherField' AND value = 'otherValue' AND widget = 'input' AND cid = 1002 AND id = " + id);
        assertResult("SELECT 1 FROM genconf_attributes_strings WHERE name = 'fourthField' AND value = 'newValue' AND widget = 'input' AND cid = 1002 AND id = " + id);
        assertNoResult("SELECT 1 FROM genconf_attributes_strings WHERE name = 'thirdField' AND cid = 1002 AND id = " + id);

        assertResult("SELECT 1 FROM genconf_attributes_bools WHERE name = 'bool' AND value = 1 AND widget = 'checkbox' AND cid = 1002 AND id = " + id);
        assertResult("SELECT 1 FROM genconf_attributes_bools WHERE name = 'otherBool' AND value = 0 AND widget = 'checkbox' AND cid = 1002 AND id = " + id);
        assertResult("SELECT 1 FROM genconf_attributes_bools WHERE name = 'fourthBool' AND value = 1 AND widget = 'checkbox' AND cid = 1002 AND id = " + id);
        assertNoResult("SELECT 1 FROM genconf_attributes_bools WHERE name = 'thirdBool' AND cid = 1002 AND id = " + id);

    }

    public void testDeleteDynamicConfiguration() throws TransactionException, SQLException, GenericConfigStorageException {
        exec("INSERT INTO genconf_attributes_strings (cid, id, name, value, widget) VALUES (1002,1001,'field', 'value', 'input')");
        exec("INSERT INTO genconf_attributes_strings (cid, id, name, value, widget) VALUES (1002,1001,'otherField', 'otherValue', 'input')");
        exec("INSERT INTO genconf_attributes_strings (cid, id, name, value, widget) VALUES (1002,1001,'thirdField', 'thirdValue', 'input')");

        exec("INSERT INTO genconf_attributes_strings (cid, id, name, value, widget) VALUES (1002,1004,'otherID', 'otherID', 'input')");

        exec("INSERT INTO genconf_attributes_bools (cid, id, name, value, widget) VALUES (1002,1001,'bool', 1, 'checkbox')");
        exec("INSERT INTO genconf_attributes_bools (cid, id, name, value, widget) VALUES (1002,1001,'otherBool', 1, 'checkbox')");
        exec("INSERT INTO genconf_attributes_bools (cid, id, name, value, widget) VALUES (1002,1001,'thirdBool', 1, 'checkbox')");

        exec("INSERT INTO genconf_attributes_bools (cid, id, name, value, widget) VALUES (1002,1004,'otherBoolID', 1, 'checkbox')");

        
        int id = 1001;
        int contextId = 1002;
        Context ctx = new SimContext(contextId);

        storage.delete(ctx, id);

        assertNoResult("SELECT 1 FROM genconf_attributes_strings WHERE cid = 1002 AND id = " + id);
        assertResult("SELECT 1 FROM genconf_attributes_strings WHERE cid = 1002 AND id = 1004");

        assertNoResult("SELECT 1 FROM genconf_attributes_bools WHERE cid = 1002 AND id = " + id);
        assertResult("SELECT 1 FROM genconf_attributes_bools WHERE cid = 1002 AND id = 1004");

    }

    // Save encrypted passwords

}
