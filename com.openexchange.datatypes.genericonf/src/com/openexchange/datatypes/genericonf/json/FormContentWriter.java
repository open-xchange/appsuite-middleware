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

import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;

/**
 * {@link FormContentWriter}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class FormContentWriter {

    private static final ValueWriterSwitch VALUE_WRITER = new ValueWriterSwitch();

    /**
     * Initializes a new {@link FormContentWriter}.
     */
    public FormContentWriter() {
        super();
    }

    public static JSONObject write(final DynamicFormDescription form, final Map<String, Object> content, final String urlPrefix) throws JSONException {
        final JSONObject object = new JSONObject();
        if (form == null) {
            return object;
        }
        for (final FormElement element : form) {
            if (content.containsKey(element.getName())) {
                final Object value = content.get(element.getName());
                if (null != value && !"null".equals(value.toString())) {
                    object.put(element.getName(), element.doSwitch(VALUE_WRITER, value, urlPrefix));
                }
            }
        }
        return object;
    }

}
