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

package com.openexchange.subscribe.json;

import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.datatypes.genericonf.FormElement.Widget;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.i18n.Translator;
import com.openexchange.subscribe.SubscriptionSource;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class BasicSubscriptionSourceJSONWriterTest extends TestCase {

    private SubscriptionSource subscriptionSource;

    private List<SubscriptionSource> sourceList;

    private SubscriptionSource subscriptionSource2;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        Tools.initExceptionFactory();

        FormElement formElementLogin = new FormElement();
        formElementLogin.setName("login");
        formElementLogin.setDisplayName("Login");
        formElementLogin.setMandatory(true);
        formElementLogin.setWidget(FormElement.Widget.INPUT);
        formElementLogin.setDefaultValue("default login");

        FormElement formElementPassword = new FormElement();
        formElementPassword.setName("password");
        formElementPassword.setDisplayName("Password");
        formElementPassword.setMandatory(true);
        formElementPassword.setWidget(FormElement.Widget.PASSWORD);

        DynamicFormDescription formDescription = new DynamicFormDescription();
        formDescription.addFormElement(formElementLogin);
        formDescription.addFormElement(formElementPassword);

        subscriptionSource = new SubscriptionSource();
        subscriptionSource.setId("com.openexchange.subscribe.test.basic");
        subscriptionSource.setDisplayName("Basic Subscription for Tests");
        subscriptionSource.setIcon("http://path/to/icon");
        subscriptionSource.setFormDescription(formDescription);
        subscriptionSource.setFolderModule(FolderObject.CONTACT);

        FormElement formElementLogin2 = new FormElement();
        formElementLogin2.setName("login2");
        formElementLogin2.setDisplayName("Login2");
        formElementLogin2.setMandatory(true);
        formElementLogin2.setWidget(FormElement.Widget.INPUT);

        FormElement formElementPassword2 = new FormElement();
        formElementPassword2.setName("password2");
        formElementPassword2.setDisplayName("Password2");
        formElementPassword2.setMandatory(true);
        formElementPassword2.setWidget(FormElement.Widget.PASSWORD);

        DynamicFormDescription formDescription2 = new DynamicFormDescription();
        formDescription2.addFormElement(formElementLogin2);
        formDescription2.addFormElement(formElementPassword2);

        subscriptionSource2 = new SubscriptionSource();
        subscriptionSource2.setId("com.openexchange.subscribe.test.basic2");
        subscriptionSource2.setDisplayName("Basic Subscription for Tests 2");
        subscriptionSource2.setIcon("http://path/to/icon 2");
        subscriptionSource2.setFormDescription(formDescription2);
        subscriptionSource2.setFolderModule(FolderObject.CONTACT);


        sourceList = new ArrayList<SubscriptionSource>();
        sourceList.add(subscriptionSource);
        sourceList.add(subscriptionSource2);
    }

    @Override
    public void tearDown() throws Exception {
        subscriptionSource = null;
        sourceList = null;
        super.tearDown();
    }

    public void testBasicSubscriptionSourceParsing() throws Exception {
        SubscriptionSourceJSONWriterInterface parser = new SubscriptionSourceJSONWriter(Translator.EMPTY);
        JSONObject json = parser.writeJSON(subscriptionSource);
        checkJson(json);
    }

    public void testListSubscriptionSourceParsing() throws Exception {
        SubscriptionSourceJSONWriterInterface parser = new SubscriptionSourceJSONWriter(Translator.EMPTY);
        JSONArray rows = parser.writeJSONArray(sourceList, new String[]{"id", "displayName", "icon", "module"});
        assertEquals(2, rows.length());

        boolean foundFirst = false, foundSecond = false;

        for(int i = 0; i < 2; i++) {
            JSONArray row = rows.getJSONArray(i);
            String id = row.getString(0);
            if(id.equals(subscriptionSource.getId())) {
                foundFirst = true;
                assertRow(row, subscriptionSource.getId(), subscriptionSource.getDisplayName(), subscriptionSource.getIcon(), "contacts");
            } else if (id.equals(subscriptionSource2.getId())) {
                foundSecond = true;
            } else {
                fail("Got unexpected subscription id: "+id);
            }
        }

        assertTrue(foundFirst && foundSecond);

    }

    public void testUnknownColumn() {
        SubscriptionSourceJSONWriterInterface parser = new SubscriptionSourceJSONWriter(Translator.EMPTY);
        try {
            parser.writeJSONArray(sourceList, new String[]{"id", "unkownColumn"});
            fail("Unknown column was accepted");
        } catch (OXException x) {
            // Exception is expected
        }
    }

    public static final void assertRow(JSONArray array, Object...values) throws JSONException {
        assertEquals(array.length(), values.length);
        for(int i = 0; i < values.length; i++) {
            assertEquals(values[i], array.get(i));
        }
    }

    public void testMandatoryFieldCheck() throws Exception {
        SubscriptionSourceJSONWriterInterface parser = new SubscriptionSourceJSONWriter(Translator.EMPTY);

        String temp = subscriptionSource.getId();
        subscriptionSource.setId(null);
        checkForParseException(parser, subscriptionSource);
        subscriptionSource.setId(temp);

        temp = subscriptionSource.getDisplayName();
        subscriptionSource.setDisplayName(null);
        checkForParseException(parser, subscriptionSource);
        subscriptionSource.setDisplayName(temp);

        DynamicFormDescription dTemp = subscriptionSource.getFormDescription();
        subscriptionSource.setFormDescription(null);
        checkForParseException(parser, subscriptionSource);
        subscriptionSource.setFormDescription(dTemp);

        temp = subscriptionSource.getFormDescription().getFormElements().get(0).getName();
        subscriptionSource.getFormDescription().getFormElements().get(0).setName(null);
        checkForParseException(parser, subscriptionSource);
        subscriptionSource.getFormDescription().getFormElements().get(0).setName(temp);

        temp = subscriptionSource.getFormDescription().getFormElements().get(0).getDisplayName();
        subscriptionSource.getFormDescription().getFormElements().get(0).setDisplayName(null);
        checkForParseException(parser, subscriptionSource);
        subscriptionSource.getFormDescription().getFormElements().get(0).setDisplayName(temp);

        // TODO: Check mandatory

        Widget wTemp = subscriptionSource.getFormDescription().getFormElements().get(0).getWidget();
        subscriptionSource.getFormDescription().getFormElements().get(0).setWidget(null);
        checkForParseException(parser, subscriptionSource);
        subscriptionSource.getFormDescription().getFormElements().get(0).setWidget(wTemp);
    }

    private void checkForParseException(SubscriptionSourceJSONWriterInterface parser, SubscriptionSource source) throws Exception {
        try {
            parser.writeJSON(source);
            fail("ParseException expected");
        } catch (OXException e) {
            // expected
        }
    }

    private void checkJson(JSONObject json) throws JSONException {
        assertFalse("JSON is an Array", json.isArray());
        assertTrue("JSON does not contain id", json.hasAndNotNull("id"));
        assertTrue("JSON does not contain displayName", json.hasAndNotNull("displayName"));
        assertTrue("JSON does not contain icon", json.hasAndNotNull("icon"));
        assertTrue("JSON does not contain a formDescription", json.hasAndNotNull("formDescription"));
        assertTrue("JSON does not contain a module", json.hasAndNotNull("module"));

        assertEquals("Wrong id", "com.openexchange.subscribe.test.basic", json.getString("id"));
        assertEquals("Wrong displayName", "Basic Subscription for Tests", json.getString("displayName"));
        assertEquals("Wrong icon", "http://path/to/icon", json.getString("icon"));
        assertEquals("Wrong module", "contacts", json.getString("module"));

        JSONArray description = json.getJSONArray("formDescription");
        assertEquals("Wrong size of form descriptions", 2, description.length());

        for (int i = 0; i < description.length(); i++) {
            JSONObject elementJson = description.getJSONObject(i);
            assertFalse("Element is an Array", elementJson.isArray());
            assertTrue("Element does not contain displayName", elementJson.hasAndNotNull("displayName"));
            assertTrue("Element does not contain widget", elementJson.hasAndNotNull("widget"));
            assertTrue("Element does not contain mandatory value", elementJson.hasAndNotNull("mandatory"));
            assertTrue("Element does not contain name", elementJson.hasAndNotNull("name"));

            if (elementJson.getString("name").equals("login")) {
                assertTrue("Element does not contain a default value", elementJson.hasAndNotNull("defaultValue"));
                assertEquals("Wrong displayName", "Login", elementJson.getString("displayName"));
                assertEquals("Wrong widget", "input", elementJson.getString("widget"));
                assertTrue("Wrong mandatory value", elementJson.getBoolean("mandatory"));
            } else if (elementJson.getString("name").equals("password")) {
                assertEquals("Wrong displayName", "Password", elementJson.getString("displayName"));
                assertEquals("Wrong widget", "password", elementJson.getString("widget"));
                assertTrue("Wrong mandatory value", elementJson.getBoolean("mandatory"));
            } else {
                fail("Unexpected element");
            }
        }
    }

}
