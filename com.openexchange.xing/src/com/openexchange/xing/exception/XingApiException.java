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

package com.openexchange.xing.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONObject;
import com.openexchange.xing.util.JSONCoercion;

/**
 * {@link XingApiException} - As specified <a href="https://dev.xing.com/docs#error-responses">here</a>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class XingApiException extends XingException {

    private static final long serialVersionUID = 1363598065006504788L;

    private final String errorName;
    private final Map<String, Object> properties;

    /**
     * Initializes a new {@link XingApiException}.
     *
     * @param errorObject The JSON error object
     */
    public XingApiException(final JSONObject errorObject) {
        super(errorObject.optString("message"));
        errorName = errorObject.optString("error_name");
        properties = new HashMap<String, Object>(4);
        for (Entry<String, Object> entry : errorObject.entrySet()) {
            final String name = entry.getKey();
            if (!"message".equals(name) && !"error_name".equals(name)) {
                properties.put(name, JSONCoercion.coerceToNative(entry.getValue()));
            }
        }
    }

    /**
     * Gets the error name
     *
     * @return The error name
     */
    public String getErrorName() {
        return errorName;
    }

    /**
     * Gets the properties
     *
     * @return The properties
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

}
