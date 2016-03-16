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

package com.openexchange.file.storage.json;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFileStorageGuestObjectPermission;
import com.openexchange.file.storage.DefaultFileStorageObjectPermission;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.java.Enums;
import com.openexchange.share.core.tools.ShareTool;
import com.openexchange.share.recipient.RecipientType;


/**
 * {@link FileMetadataFieldParser} - Parses certain metadata fields and converts
 * their JSON representation into internal objects.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class FileMetadataFieldParser {

    /**
     * Converts the given value into its internal representation if the given field
     * is supported.
     *
     * @param field The fiele
     * @param value The fields value
     * @return The converted object or the original value, if the filed is not supported
     * @throws JSONException
     * @throws OXException
     */
    public static Object convert(Field field, Object value) throws JSONException, OXException {
        Object val = value;
        if (val == JSONObject.NULL) {
            val = null;
        }
        switch(field) {
        case CATEGORIES: {
            if(String.class.isInstance(val)) {
                return val;
            }
            return categories((JSONArray) val);
        }
        case META:
            if (value == null || value == JSONObject.NULL) {
                return null;
            }
            return JSONCoercion.coerceToNative(value);
        case OBJECT_PERMISSIONS:
            if (null == val || JSONObject.NULL.equals(val)) {
                return null;
            }
            JSONArray jsonArray = (JSONArray) val;
            List<FileStorageObjectPermission> objectPermissions = new ArrayList<FileStorageObjectPermission>(jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                objectPermissions.add(parseObjetPermission(jsonArray.getJSONObject(i), null)); //TODO: timezone from user/request?
            }
            return objectPermissions;
        default:
            return val;
        }
    }

    private static Object categories(final JSONArray value) throws JSONException {
        if(value.length() == 0) {
            return "";
        }
        final StringBuilder b = new StringBuilder();
        for(int i = 0, size = value.length(); i < size; i++) {
            b.append(value.getString(i)).append(", ");
        }
        b.setLength(b.length()-2);
        return b.toString();
    }

    /**
     * Parses an object permission from the supplied JSON object.
     *
     * @param jsonObject The JSON object to parse
     * @param timeZone The client timezone to consider, or <code>null</code> to not apply timezone offsets to parsed timestamps
     * @return The parsed permission
     */
    private static FileStorageObjectPermission parseObjetPermission(JSONObject jsonObject, TimeZone timeZone) throws OXException, JSONException {
        if (false == jsonObject.hasAndNotNull("bits")) {
            throw FileStorageExceptionCodes.MISSING_PARAMETER.create("bits");
        }
        int bits = jsonObject.getInt("bits");
        /*
         * check for external guest permissions
         */
        RecipientType type = Enums.parse(RecipientType.class, jsonObject.optString("type"), null);
        if (null != type && (RecipientType.ANONYMOUS == type || RecipientType.GUEST == type)) {
            /*
             * parse as guest permission entity
             */
            DefaultFileStorageGuestObjectPermission parsedGuestPermission = new DefaultFileStorageGuestObjectPermission();
            parsedGuestPermission.setRecipient(ShareTool.parseRecipient(jsonObject, null)); //TODO: timezone from user/request?
            parsedGuestPermission.setPermissions(bits);
            return parsedGuestPermission;
        } else {
            /*
             * parse as already known permission entity
             */
            DefaultFileStorageObjectPermission parsedPermission = new DefaultFileStorageObjectPermission();
            if (false == jsonObject.has("entity")) {
                throw FileStorageExceptionCodes.MISSING_PARAMETER.create("entity");
            }
            parsedPermission.setEntity(jsonObject.getInt("entity"));
            if (jsonObject.has("group")) {
                parsedPermission.setGroup(jsonObject.getBoolean("group"));
            } else if (null != type) {
                parsedPermission.setGroup(RecipientType.GROUP == type);
            } else {
                throw FileStorageExceptionCodes.MISSING_PARAMETER.create("group");
            }
            parsedPermission.setPermissions(bits);
            return parsedPermission;
        }
    }

}
