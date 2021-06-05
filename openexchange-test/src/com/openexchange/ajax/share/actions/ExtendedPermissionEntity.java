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

package com.openexchange.ajax.share.actions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.parser.ContactParser;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFileStorageObjectPermission;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.folderstorage.Permissions;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.Enums;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.recipient.RecipientType;

/**
 * {@link ExtendedPermissionEntity}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ExtendedPermissionEntity {

    public static List<ExtendedPermissionEntity> parse(JSONArray json, TimeZone timeZone) throws JSONException {
        if (null == json) {
            return null;
        }
        List<ExtendedPermissionEntity> entities = new ArrayList<ExtendedPermissionEntity>(json.length());
        for (int i = 0; i < json.length(); i++) {
            entities.add(new ExtendedPermissionEntity(json.getJSONObject(i), timeZone));
        }
        return entities;
    }

    private final Contact contact;
    private final int entity;
    private final RecipientType type;
    private final String displayName;
    private final int bits;
    private final String shareURL;
    private final String password;
    private final Date expiry;

    /**
     * Initializes a new {@link ExtendedPermissionEntity}.
     *
     * @param json The JSON object to parse
     * @param timeZone The timezone to use
     */
    public ExtendedPermissionEntity(JSONObject json, TimeZone timeZone) throws JSONException {
        super();
        JSONObject jsonContact = json.optJSONObject("contact");
        if (null != jsonContact) {
            contact = new Contact();
            try {
                new ContactParser().parse(contact, jsonContact);
            } catch (OXException e) {
                throw new JSONException(e);
            }
        } else {
            contact = null;
        }
        entity = json.optInt("entity");
        type = Enums.parse(RecipientType.class, json.getString("type"));
        shareURL = json.optString("share_url", null);
        displayName = json.optString("display_name", null);
        password = json.optString("password", null);
        bits = json.getInt("bits");
        if (json.hasAndNotNull("expiry_date")) {
            long date = json.getLong("expiry_date");
            if (null != timeZone) {
                date -= timeZone.getOffset(date);
            }
            expiry = new Date(date);
        } else {
            expiry = null;
        }
    }

    public int getEntity() {
        return entity;
    }

    public Contact getContact() {
        return contact;
    }

    public RecipientType getType() {
        return type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getBits() {
        return bits;
    }

    public String getShareURL() {
        return shareURL;
    }

    public String getPassword() {
        return password;
    }

    public Date getExpiry() {
        return expiry;
    }

    public OCLPermission toFolderPermission(int folderID) {
        OCLPermission permission = new OCLPermission(entity, folderID);
        permission.setGroupPermission(RecipientType.GROUP.equals(type));
        int[] permissionBits = Permissions.parsePermissionBits(bits);
        permission.setAllPermission(permissionBits[0], permissionBits[1], permissionBits[2], permissionBits[3]);
        permission.setFolderAdmin(0 < permissionBits[4]);
        return permission;
    }

    public FileStorageObjectPermission toObjectPermission() {
        return new DefaultFileStorageObjectPermission(entity, RecipientType.GROUP.equals(type), bits);
    }

}
