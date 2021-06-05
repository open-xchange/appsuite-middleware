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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.Map;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;

/**
 * {@link FormContentParserTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class FormContentParserTest {

    private JSONObject object = null;
    private DynamicFormDescription form = null;

    @Before
    public void setUp() throws Exception {
        object = new JSONObject();
        object.put("login", "blupp");
        object.put("password", "secret");
        object.put("checkbox", true);

        form = new DynamicFormDescription();
        form.add(FormElement.input("login", "Login Name")).add(FormElement.password("password", "Password")).add(FormElement.checkbox("checkbox", "Checkbox"));
    }

    @Test
    public void testParsing() {
        Map<String, Object> content = FormContentParser.parse(object, form);

        assertNotNull("Content was null!", content);
        assertEquals("login was wrong", "blupp", content.get("login"));
        assertEquals("password was wrong", "secret", content.get("password"));
        assertEquals("checkbox was wrong", Boolean.TRUE, content.get("checkbox"));

    }
}
