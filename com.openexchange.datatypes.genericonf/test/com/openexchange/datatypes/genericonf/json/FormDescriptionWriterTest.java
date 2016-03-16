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

package com.openexchange.datatypes.genericonf.json;

import static com.openexchange.json.JSONAssertion.assertValidates;
import junit.framework.TestCase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.i18n.Translator;
import com.openexchange.json.JSONAssertion;


/**
 * {@link FormDescriptionWriterTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class FormDescriptionWriterTest extends TestCase{

    private DynamicFormDescription form;

    private final TestTranslator translator = new TestTranslator();

    @Override
    public void setUp() throws Exception {
         form = new DynamicFormDescription();
    }

    public void testWriteElement() throws JSONException {
        FormElement element = new FormElement();
        element.setWidget(FormElement.Widget.INPUT);
        element.setDisplayName("Login name");
        element.setName("login");
        element.setMandatory(true);
        element.setDefaultValue("admin");

        JSONObject object = new FormDescriptionWriter(translator).write(element);

        JSONAssertion assertion = new JSONAssertion().isObject()
            .hasKey("widget").withValue("input")
            .hasKey("name").withValue("login")
            .hasKey("displayName").withValue("Login name")
            .hasKey("mandatory").withValue(true)
            .hasKey("defaultValue").withValue("admin")
        .objectEnds();

        assertValidates(assertion, object);

        element.setWidget(FormElement.Widget.PASSWORD);
        element.setDisplayName("Password");
        element.setName("password");
        element.setMandatory(false);
        element.setDefaultValue(null);

        object = new FormDescriptionWriter(translator).write(element);

        assertion = new JSONAssertion().isObject()
            .hasKey("widget").withValue("password")
            .hasKey("name").withValue("password")
            .hasKey("displayName").withValue("Password")
            .hasKey("mandatory").withValue(false)
        .objectEnds();

        assertValidates(assertion, object);

        element.setWidget(FormElement.Widget.CHECKBOX);
        element.setDisplayName("Checkbox");
        element.setName("checkbox");

        object = new FormDescriptionWriter(translator).write(element);

        assertion = new JSONAssertion().isObject()
            .hasKey("widget").withValue("checkbox")
            .hasKey("name").withValue("checkbox")
            .hasKey("displayName").withValue("Checkbox")
            .hasKey("mandatory").withValue(false)
        .objectEnds();

        assertValidates(assertion, object);

    }

    public void testWriteArray() throws JSONException {
        FormElement element = new FormElement();
        element.setWidget(FormElement.Widget.INPUT);
        element.setDisplayName("Login name");
        element.setName("login");
        element.setMandatory(true);
        element.setDefaultValue("admin");

        form.add(element);
        JSONArray array = new FormDescriptionWriter(translator).write(form);

        assertEquals(1, array.length());

        JSONAssertion assertion = new JSONAssertion().isObject()
            .hasKey("widget").withValue("input")
            .hasKey("name").withValue("login")
            .hasKey("displayName").withValue("Login name")
            .hasKey("mandatory").withValue(true)
            .hasKey("defaultValue").withValue("admin")
       .objectEnds();

        assertValidates(assertion, array.getJSONObject(0));
    }

    public void testCustom() throws JSONException {
        FormElement element = new FormElement();
        element.setWidget(FormElement.Widget.CUSTOM);
        element.setDisplayName("Thingamajick");
        element.setName("thingamajick");
        element.setMandatory(true);
        element.setDefaultValue("admin");
        element.setCustomWidget("com.openexchange.test.thingamajickChooser");

        JSONObject object = new FormDescriptionWriter(translator).write(element);

        JSONAssertion assertion = new JSONAssertion().isObject()
            .hasKey("widget").withValue("com.openexchange.test.thingamajickChooser")
            .hasKey("name").withValue("thingamajick")
            .hasKey("displayName").withValue("Thingamajick")
            .hasKey("mandatory").withValue(true)
            .hasKey("defaultValue").withValue("admin")
        .objectEnds();

        assertValidates(assertion,object);
    }

    public void testOptions() throws JSONException {
        FormElement element = new FormElement();
        element.setWidget(FormElement.Widget.INPUT);
        element.setDisplayName("Login name");
        element.setName("login");
        element.setMandatory(true);
        element.setDefaultValue("admin");
        element.setOption("someOption", "12")
               .setOption("someOtherOption", "23");

        JSONObject object = new FormDescriptionWriter(translator).write(element);

        JSONAssertion assertion = new JSONAssertion().isObject()
            .hasKey("widget").withValue("input")
            .hasKey("name").withValue("login")
            .hasKey("displayName").withValue("Login name")
            .hasKey("mandatory").withValue(true)
            .hasKey("defaultValue").withValue("admin")
            .hasKey("options").withValueObject()
                .hasKey("someOption").withValue("12")
                .hasKey("someOtherOption").withValue("23")
                .objectEnds()
        .objectEnds();

        assertValidates(assertion,object);
    }

    private static final class TestTranslator implements Translator {

        @Override
        public String translate(String toTranslate) {
            return toTranslate;
        }

    }
}
