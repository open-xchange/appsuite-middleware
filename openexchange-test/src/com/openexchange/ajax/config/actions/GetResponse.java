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
