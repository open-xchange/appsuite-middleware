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

package com.openexchange.datatypes.genericonf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.Test;


/**
 * {@link DynamicFormDescriptionTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class DynamicFormDescriptionTest {         @Test
     public void testMissingMandatories() {
        Map<String, Object> content = new HashMap<String, Object>();
        content.put("login", "Blupp");

        DynamicFormDescription form = new DynamicFormDescription();
        form.add(FormElement.input("login", "Login Name", true, null)).add(FormElement.password("password", "Password", true, null)).add(FormElement.input("other", "Other", true, null));

        Set<String> missing = form.getMissingMandatoryFields(content);
        assertEquals(2, missing.size());
        assertTrue(missing.contains("password"));
        assertTrue(missing.contains("other"));
    }
}
