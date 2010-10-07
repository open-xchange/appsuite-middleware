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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.unitedinternet.smartdrive.client.internal;

import java.util.Collection;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONValue;
import com.openexchange.unitedinternet.smartdrive.client.SmartDriveException;
import com.openexchange.unitedinternet.smartdrive.client.SmartDriveExceptionCodes;

/**
 * {@link DefaultSmartDriveResponse} - The fallback SmartDrive response.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DefaultSmartDriveResponse extends AsbtractSmartDriveResponse<Object> {

    private Object responseObject;

    /**
     * Initializes a new {@link DefaultSmartDriveResponse}.
     */
    public DefaultSmartDriveResponse() {
        super();
    }

    public Object getResponse() {
        return responseObject;
    }

    /**
     * Gets the response as a {@link Map}; response is a JSON object.
     * 
     * @return The response as a {@link Map}
     * @throws SmartDriveException If response cannot be returned as a map
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getResponseAsMap() throws SmartDriveException {
        try {
            return (Map<String, Object>) responseObject;
        } catch (final ClassCastException e) {
            throw SmartDriveExceptionCodes.NOT_OF_TYPE.create(e, Map.class.getName());
        }
    }

    /**
     * Gets the response as a {@link List}; response is a JSON array.
     * 
     * @return The response as a {@link List}
     * @throws SmartDriveException If response cannot be returned as a collection
     */
    @SuppressWarnings("unchecked")
    public Collection<Object> getResponseAsList() throws SmartDriveException {
        try {
            return (Collection<Object>) responseObject;
        } catch (final ClassCastException e) {
            throw SmartDriveExceptionCodes.NOT_OF_TYPE.create(e, Collection.class.getName());
        }
    }

    /**
     * Sets the JSON response object which is coerced to Java object.
     * 
     * @param responseObject The response object to set
     * @throws SmartDriveException If coercion fails
     */
    public void setJSONResponseObject(final JSONValue responseObject) throws SmartDriveException {
        try {
            this.responseObject = null == responseObject ? null : JSONCoercion.coerceToNative(responseObject);
        } catch (final JSONException e) {
            throw SmartDriveExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

}
