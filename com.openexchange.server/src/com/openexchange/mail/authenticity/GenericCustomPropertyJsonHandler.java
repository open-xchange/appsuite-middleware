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

package com.openexchange.mail.authenticity;

import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link GenericCustomPropertyJsonHandler}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class GenericCustomPropertyJsonHandler implements CustomPropertyJsonHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GenericCustomPropertyJsonHandler.class);

    /**
     * Initialises a new {@link GenericCustomPropertyJsonHandler}.
     */
    public GenericCustomPropertyJsonHandler() {
        super();
    }

    @Override
    public JSONObject toJson(Map<String, Object> customProperties) {
        if (customProperties == null) {
            return null;
        }
        JSONObject result = new JSONObject(customProperties.size());
        for (Entry<String, Object> entry : customProperties.entrySet()) {
            Object value = entry.getValue();
            try {
                result.put(entry.getKey(), value == null ? JSONObject.NULL : value.toString());
            } catch (JSONException e) {
                LOG.warn("Error while parsing custom property '%s1': $s2", entry, value);
                // should never be thrown
            }
        }
        return result;
    }
}
