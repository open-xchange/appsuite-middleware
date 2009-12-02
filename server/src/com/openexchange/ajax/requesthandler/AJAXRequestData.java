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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONObject;
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

    public static interface InputStreamProvider {

        /**
         * Gets this provider's input stream.
         * 
         * @return The input stream
         * @throws IOException If an I/O error occurs
         */
        InputStream getInputStream() throws IOException;
    }

    private final Map<String, String> params;

    private Object data;

    private InputStreamProvider uploadStreamProvider;

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
     * @throws NullPointerException If name is <code>null</code>
     */
    public void putParameter(final String name, final String value) {
        if (null == name) {
            throw new NullPointerException("name is null");
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
     * @throws NullPointerException If name is <code>null</code>
     */
    public String getParameter(final String name) {
        if (null == name) {
            throw new NullPointerException("name is null");
        }
        return params.get(name);
    }

    /**
     * Gets all available parameter names wrapped by an {@link Iterator iterator}.
     * 
     * @return The {@link Iterator iterator} for available parameter names
     */
    public Iterator<String> getParameterNames() {
        return params.keySet().iterator();
    }

    /**
     * Gets an {@link Iterator iterator} for those parameters not matching given parameter names.
     * 
     * @param nonMatchingParameterNames The non-matching parameter names
     * @return An {@link Iterator iterator} for non-matching parameters
     */
    public Iterator<Entry<String, String>> getNonMatchingParameters(final Collection<String> nonMatchingParameterNames) {
        final Map<String, String> clone = new HashMap<String, String>(params);
        clone.keySet().removeAll(nonMatchingParameterNames);
        return clone.entrySet().iterator();
    }

    /**
     * Gets an {@link Iterator iterator} for those parameters matching given parameter names.
     * 
     * @param matchingParameterNames The matching parameter names
     * @return An {@link Iterator iterator} for matching parameters
     */
    public Iterator<Entry<String, String>> getMatchingParameters(final Collection<String> matchingParameterNames) {
        final Map<String, String> clone = new HashMap<String, String>(params);
        clone.keySet().retainAll(matchingParameterNames);
        return clone.entrySet().iterator();
    }

    /**
     * Gets the data object.
     * 
     * @return The data object or <code>null</code> if not available
     */
    public Object getData() {
        return data;
    }

    /**
     * Sets the data object.
     * 
     * @param data The data object to set
     */
    public void setData(final Object data) {
        this.data = data;
    }

    /**
     * Gets the upload stream. Retrieves the body of the request as binary data as an {@link InputStream}.
     * 
     * @return The upload stream or <code>null</code> if not available
     * @throws IOException If an I/O error occurs
     */
    public InputStream getUploadStream() throws IOException {
        return uploadStreamProvider.getInputStream();
    }

    /**
     * Sets the upload stream provider
     * 
     * @param uploadStream The upload stream provider to set
     */
    public void setUploadStreamProvider(final InputStreamProvider uploadStreamProvider) {
        this.uploadStreamProvider = uploadStreamProvider;
    }

}
