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

import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;

/**
 * {@link FormContentParser}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class FormContentParser {

    private static final ValueReaderSwitch valueReader = new ValueReaderSwitch();

    /**
     * Parses specified JSON object to a configuration map.
     *
     * @param object The JSON object
     * @param form The form description
     * @return The configuration map
     */
    public static Map<String, Object> parse(final JSONObject object, final DynamicFormDescription form) {
        final Map<String, Object> content = new HashMap<String, Object>();
        for (final FormElement element : form) {
            final String name = element.getName();
            if (object.hasAndNotNull(name)) {
                Object value = object.opt(name);
                if (value != null) {
                    value = element.doSwitch(valueReader, value);
                }
                content.put(name, value);
            }
        }
        return content;
    }

}
