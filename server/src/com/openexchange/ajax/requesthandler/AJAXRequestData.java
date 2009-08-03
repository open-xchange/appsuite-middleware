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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.ajax.requesthandler;

import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.ajax.fields.RequestConstants;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.tools.servlet.AjaxException;

/**
 * {@link AJAXRequestData} contains the parameters and the payload of the request.
 * 
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AJAXRequestData {

    private final Map<String, String> params;

    private JSONValue data;

    /**
     * Initializes a new {@link AJAXRequestData}.
     * 
     * @param json The JSON data
     * @throws AjaxException If an AJAX error occurs
     */
    public AJAXRequestData(final JSONObject json) throws AjaxException {
        this();
        data = DataParser.checkJSONObject(json, RequestConstants.DATA);
    }

    /**
     * Initializes a new {@link AJAXRequestData}.
     */
    public AJAXRequestData() {
        super();
        params = new HashMap<String, String>();
    }

    /**
     * Puts given name-value-pair into this data's parameters.
     * <p>
     * A <code>null</code> value removes the mapping.
     * 
     * @param name The parameter name
     * @param value The parameter value
     */
    public void putParameter(final String name, final String value) {
        if (null == name) {
            return;
        }
        if (null == value) {
            params.remove(name);
        } else {
            params.put(name, value);
        }
    }

    /**
     * Gets the value mapped to given parameter name.
     * 
     * @param name The parameter name
     * @return The value mapped to given parameter name or <code>null</code> if not present
     */
    public String getParameter(final String name) {
        if (null == name) {
            return null;
        }
        return params.get(name);
    }

    /**
     * Gets the JSON data.
     * 
     * @return The JSON data
     */
    public JSONValue getData() {
        return data;
    }

    /**
     * Sets the JSON data.
     * 
     * @param data The JSON data to set
     */
    public void setData(final JSONValue data) {
        this.data = data;
    }

}
