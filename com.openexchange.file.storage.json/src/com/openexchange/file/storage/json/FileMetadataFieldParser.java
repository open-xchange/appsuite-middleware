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
     * @param field The field
     * @param value The fields value
     * @return The converted object or the original value, if the field is not supported
     */
    public static Object convert(Field field, Object value) throws JSONException, OXException {
        return convert(field, value, null);
    }

    /**
     * Converts the given value into its internal representation if the given field
     * is supported.
     *
     * @param field The field
     * @param value The fields value
     * @param timeZone The client timezone to consider, or <code>null</code> if not set
     * @return The converted object or the original value, if the field is not supported
     * @throws JSONException
     * @throws OXException
     */
    public static Object convert(Field field, Object value, TimeZone timeZone) throws JSONException, OXException {
        Object val = value;
        if (val == JSONObject.NULL) {
            val = null;
        }
        switch (field) {
        case CATEGORIES: {
            if (val == null) {
                return null;
            }
            if (String.class.isInstance(val)) {
                return val;
            }
            return categories((JSONArray) val);
        }
        case META:
            if (value == null) {
                return null;
            }
            return JSONCoercion.coerceToNative(value);
        case OBJECT_PERMISSIONS:
            if (null == val) {
                return null;
            }
            JSONArray jsonArray = (JSONArray) val;
            List<FileStorageObjectPermission> objectPermissions = new ArrayList<FileStorageObjectPermission>(jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                objectPermissions.add(parseObjectPermission(jsonArray.getJSONObject(i)));
            }
            return objectPermissions;
        case LAST_MODIFIED:
        case CREATED:
            if (null != timeZone && null != val && Long.class.isInstance(val)) {
                long timestamp = ((Long) val).longValue();
                return Long.valueOf(timestamp - timeZone.getOffset(timestamp));
            }
            return val;
        default:
            return val;
        }
    }

    private static Object categories(final JSONArray value) throws JSONException {
        if (value.length() == 0) {
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
     * @return The parsed permission
     */
    private static FileStorageObjectPermission parseObjectPermission(JSONObject jsonObject) throws OXException, JSONException {
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
        }
        /*
         * parse as already known permission entity
         */
        DefaultFileStorageObjectPermission parsedPermission = new DefaultFileStorageObjectPermission();
        if (false == jsonObject.has("entity")) {
            throw FileStorageExceptionCodes.MISSING_PARAMETER.create("entity");
        }
        parsedPermission.setEntity(jsonObject.getInt("entity"));
        parsedPermission.setIdentifier(jsonObject.optString("identifier", null));
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
