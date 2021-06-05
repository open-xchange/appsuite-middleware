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

package com.openexchange.datatypes.genericonf.json;

import static com.openexchange.json.JSONAssertion.assertValidates;
import static org.junit.Assert.assertEquals;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
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
public class FormDescriptionWriterTest {

    private DynamicFormDescription form;

    private final Translator translator = Translator.EMPTY;

    @Before
    public void setUp() {
        form = new DynamicFormDescription();
    }

    @Test
    public void testWriteElement() throws JSONException {
        FormElement element = new FormElement();
        element.setWidget(FormElement.Widget.INPUT);
        element.setDisplayName("Login name");
        element.setName("login");
        element.setMandatory(true);
        element.setDefaultValue("admin");

        JSONObject object = new FormDescriptionWriter(translator).write(element);

        JSONAssertion assertion = new JSONAssertion().isObject().hasKey("widget").withValue("input").hasKey("name").withValue("login").hasKey("displayName").withValue("Login name").hasKey("mandatory").withValue(Boolean.TRUE).hasKey("defaultValue").withValue("admin").objectEnds();

        assertValidates(assertion, object);

        element.setWidget(FormElement.Widget.PASSWORD);
        element.setDisplayName("Password");
        element.setName("password");
        element.setMandatory(false);
        element.setDefaultValue(null);

        object = new FormDescriptionWriter(translator).write(element);

        assertion = new JSONAssertion().isObject().hasKey("widget").withValue("password").hasKey("name").withValue("password").hasKey("displayName").withValue("Password").hasKey("mandatory").withValue(Boolean.FALSE).objectEnds();

        assertValidates(assertion, object);

        element.setWidget(FormElement.Widget.CHECKBOX);
        element.setDisplayName("Checkbox");
        element.setName("checkbox");

        object = new FormDescriptionWriter(translator).write(element);

        assertion = new JSONAssertion().isObject().hasKey("widget").withValue("checkbox").hasKey("name").withValue("checkbox").hasKey("displayName").withValue("Checkbox").hasKey("mandatory").withValue(Boolean.FALSE).objectEnds();

        assertValidates(assertion, object);

    }

    @Test
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

        JSONAssertion assertion = new JSONAssertion().isObject().hasKey("widget").withValue("input").hasKey("name").withValue("login").hasKey("displayName").withValue("Login name").hasKey("mandatory").withValue(Boolean.TRUE).hasKey("defaultValue").withValue("admin").objectEnds();

        assertValidates(assertion, array.getJSONObject(0));
    }

    @Test
    public void testCustom() throws JSONException {
        FormElement element = new FormElement();
        element.setWidget(FormElement.Widget.CUSTOM);
        element.setDisplayName("Thingamajick");
        element.setName("thingamajick");
        element.setMandatory(true);
        element.setDefaultValue("admin");
        element.setCustomWidget("com.openexchange.test.thingamajickChooser");

        JSONObject object = new FormDescriptionWriter(translator).write(element);

        JSONAssertion assertion = new JSONAssertion().isObject().hasKey("widget").withValue("com.openexchange.test.thingamajickChooser").hasKey("name").withValue("thingamajick").hasKey("displayName").withValue("Thingamajick").hasKey("mandatory").withValue(Boolean.TRUE).hasKey("defaultValue").withValue("admin").objectEnds();

        assertValidates(assertion, object);
    }

    @Test
    public void testOptions() throws JSONException {
        FormElement element = new FormElement();
        element.setWidget(FormElement.Widget.INPUT);
        element.setDisplayName("Login name");
        element.setName("login");
        element.setMandatory(true);
        element.setDefaultValue("admin");
        element.setOption("someOption", "12").setOption("someOtherOption", "23");

        JSONObject object = new FormDescriptionWriter(translator).write(element);

        JSONAssertion assertion = new JSONAssertion().isObject().hasKey("widget").withValue("input").hasKey("name").withValue("login").hasKey("displayName").withValue("Login name").hasKey("mandatory").withValue(Boolean.TRUE).hasKey("defaultValue").withValue("admin").hasKey("options").withValueObject().hasKey("someOption").withValue("12").hasKey("someOtherOption").withValue("23").objectEnds().objectEnds();

        assertValidates(assertion, object);
    }

}
