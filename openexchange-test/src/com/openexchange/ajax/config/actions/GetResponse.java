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

package com.openexchange.ajax.config.actions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXResponse;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class GetResponse extends AbstractAJAXResponse {

    private Object value;

    /**
     * @param response
     */
    GetResponse(final Response response) {
        super(response);
    }

    private void fetchValue() {
        if (null == value) {
            value = getData();
            if (JSONObject.NULL.equals(value)) {
                value = null;
            }
        }
    }

    /**
     * Checks if obtained value is not <code>null</code>
     *
     * @return <code>true</code> if obtained value is not <code>null</code>; otherwise <code>false</code>
     */
    public boolean hasValue() {
        fetchValue();
        return null != value;
    }

    /**
     * Checks if obtained value is of the type <code>Number</code>
     *
     * @return <code>true</code> if obtained value is not <code>null</code> and of type <code>Number</code>; otherwise <code>false</code>
     */
    public boolean hasInteger() {
        fetchValue();
        return null != value ? value instanceof Number : false;
    }

    /**
     * Gets the <code>int</code> value or <code>-1</code> if not present.
     *
     * @return The <code>int</code> value or <code>-1</code> if not present
     */
    public int getInteger() {
        fetchValue();
        return null == value ? -1 : ((Number) value).intValue();
    }

    /**
     * Checks if obtained value is of the type <code>String</code>
     *
     * @return <code>true</code> if obtained value is not <code>null</code> and of type <code>String</code>; otherwise <code>false</code>
     */
    public boolean hasString() {
        fetchValue();
        return null != value ? value instanceof String : false;
    }

    /**
     * Gets the <code>java.lang.String</code> value or <code>null</code> if not present.
     *
     * @return The <code>java.lang.String</code> value or <code>null</code> if not present
     */
    public String getString() {
        fetchValue();
        return null == value ? null : (String) value;
    }

    /**
     * Checks if obtained value is of the type <code>Long</code>
     *
     * @return <code>true</code> if obtained value is not <code>null</code> and of type <code>Long</code>; otherwise <code>false</code>
     */
    public boolean hasLong() {
        fetchValue();
        return null != value ? value instanceof Long : false;
    }

    /**
     * Gets the <code>long</code> value or <code>-1</code> if not present.
     *
     * @return The <code>long</code> value or <code>-1</code> if not present
     */
    public long getLong() {
        fetchValue();
        return null == value ? -1 : ((Long) value).longValue();
    }

    /**
     * Gets the <code>boolean</code> value or <code>false</code> if not present.
     *
     * @return The <code>boolean</code> value or <code>false</code> if not present
     */
    public boolean getBoolean() {
        fetchValue();
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return null == value ? false : ((Boolean) value).booleanValue();
    }

    /**
     * Gets the <code>java.lang.Object[]</code> value or <code>null</code> if not present.
     *
     * @return The <code>java.lang.Object[]</code> value or <code>null</code> if not present
     */
    public Object[] getArray() throws JSONException {
        fetchValue();
        if (null == value) {
            return null;
        }
        final JSONArray array = (JSONArray) value;
        final Object[] retval = new Object[array.length()];
        for (int i = 0; i < array.length(); i++) {
            retval[i] = array.get(i);
        }
        return retval;
    }

    /**
     * Gets the <code>org.json.JSONObject</code> value or <code>null</code> if not present.
     *
     * @return The <code>org.json.JSONObject</code> value or <code>null</code> if not present
     */
    public JSONObject getJSON() {
        fetchValue();
        return null == value ? null : (JSONObject) value;
    }
}
