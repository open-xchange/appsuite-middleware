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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.file.storage.owncloud.rest;

import java.io.Serializable;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageExceptionCodes;

/**
 * {@link OCCapabilities}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public class OCCapabilities extends AbstractOCJSONResponse implements Serializable {

    private static final String SHARING_FIELD = "files_sharing";
    private static final String FILES_FIELD = "files";
    private static final long serialVersionUID = 1L;

    JSONObject capabilities;
    // Data fields for important entries
    Integer searchMinLength = null;
    Boolean versioning = null;
    Boolean trash = null;

    /**
     * Initializes a new {@link OCCapabilities}.
     * @param json
     * @throws OXException
     */
    public OCCapabilities(JSONObject json) throws OXException {
        super(json);
    }

    /**
     * Parses the {@link JSONObject} to a {@link OCCapabilities} object
     *
     * @param json The json to parse
     * @return The {@link OCCapabilities}
     * @throws OXException
     */
    public static OCCapabilities parse(JSONObject json) throws OXException {
        OCCapabilities result = new OCCapabilities(json);
        try {
            result.capabilities = ((JSONObject) result.getData()).getJSONObject("capabilities");
            return result;
        } catch (JSONException e) {
            throw FileStorageExceptionCodes.JSON_ERROR.create(e.getMessage(), e);
        }
    }

    /**
     * Gets the minimum search length
     *
     * @return The minimum search length
     * @throws OXException In case of a parsing error
     */
    public int getSearchMinLength() throws OXException {
        if(searchMinLength != null) {
            return searchMinLength.intValue();
        }
        try {
            JSONObject sharing = capabilities.getJSONObject(SHARING_FIELD);
            Integer searchMinLength = null;
            if(sharing.has("search_min_length")){
                searchMinLength =  Integer.valueOf(sharing.getInt("search_min_length"));
            }
            return searchMinLength == null ? 0 : searchMinLength.intValue();
        } catch (JSONException e) {
            throw FileStorageExceptionCodes.JSON_ERROR.create(e.getMessage(), e);
        }
    }

    /**
     * Checks whether versioning is enabled or not
     *
     * @return <code>true</code> if versioning is enabled, <code>false</code> otherwise
     * @throws OXException In case of a parsing error
     */
    public boolean supportsVersioning() throws OXException {
        if(versioning != null) {
            return versioning.booleanValue();
        }
        try {
            versioning =  Boolean.valueOf(capabilities.getJSONObject(FILES_FIELD).getBoolean("versioning"));
            return versioning == null ? false : versioning.booleanValue();
        } catch (JSONException e) {
            throw FileStorageExceptionCodes.JSON_ERROR.create(e.getMessage(), e);
        }
    }

    /**
     * Checks whether trash is supported
     *
     * @return <code>true</code> if trash is supported, <code>false</code> otherwise
     * @throws OXException In case of a parsing error
     */
    public boolean supportsTrash() throws OXException {
        if(trash != null) {
            return trash.booleanValue();
        }
        try {
            trash =  Boolean.valueOf(capabilities.getJSONObject(FILES_FIELD).getBoolean("undelete"));
            return trash == null ? false : trash.booleanValue();
        } catch (JSONException e) {
            throw FileStorageExceptionCodes.JSON_ERROR.create(e.getMessage(), e);
        }
    }

}
