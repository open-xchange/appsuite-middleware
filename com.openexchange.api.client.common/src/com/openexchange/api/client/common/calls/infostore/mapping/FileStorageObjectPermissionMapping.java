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

package com.openexchange.api.client.common.calls.infostore.mapping;

import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFileStorageGuestObjectPermission;
import com.openexchange.file.storage.DefaultFileStorageObjectPermission;
import com.openexchange.file.storage.FileStorageGuestObjectPermission;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.groupware.tools.mappings.json.ListItemMapping;
import com.openexchange.share.core.tools.ShareTool;
import com.openexchange.share.recipient.AnonymousRecipient;
import com.openexchange.share.recipient.GuestRecipient;
import com.openexchange.share.recipient.RecipientType;
import com.openexchange.share.recipient.ShareRecipient;

/**
 * {@link FileStorageObjectPermissionMapping}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @param <O> Thy type of the list elements
 * @since v7.10.5
 */
public abstract class FileStorageObjectPermissionMapping<O> extends ListItemMapping<FileStorageObjectPermission, O, JSONObject> {

    /**
     * Initializes a new {@link FileStorageObjectPermissionMapping}.
     *
     * @param ajaxName The mapped ajax name
     * @param columnID The mapped column ID
     */
    public FileStorageObjectPermissionMapping(String ajaxName, Integer columnID) {
        super(ajaxName, columnID);
    }

    @Override
    public FileStorageObjectPermission deserialize(JSONObject from, TimeZone timeZone) throws JSONException {
        return deserializePermission(from);
    }

    @Override
    public JSONObject serialize(FileStorageObjectPermission from, TimeZone timeZone) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("bits", from.getPermissions());
        json.put("entity", from.getEntity());
        json.put("group", from.isGroup());
        if (from instanceof FileStorageGuestObjectPermission) {
            FileStorageGuestObjectPermission guestFrom = (FileStorageGuestObjectPermission) from;
            ShareRecipient recipient = guestFrom.getRecipient();
            json.put("type", recipient.getType().toString().toLowerCase());
            if (recipient.getType() == RecipientType.ANONYMOUS) {
                AnonymousRecipient anonRecipient = ((AnonymousRecipient) recipient);
                json.putOpt("password", anonRecipient.getPassword());
                json.putOpt("expiry_date", anonRecipient.getExpiryDate());
            } else if (recipient.getType() == RecipientType.GUEST) {
                GuestRecipient guestRecipient = ((GuestRecipient) recipient);
                json.putOpt("email_address", guestRecipient.getEmailAddress());
                json.putOpt("display_name", guestRecipient.getDisplayName());
                json.putOpt("contact_id", guestRecipient.getContactID());
                json.putOpt("contact_folder", guestRecipient.getContactFolder());
            }
        }
        return json;
    }

    @Override
    protected FileStorageObjectPermission deserialize(JSONArray array, int index, TimeZone timeZone) throws JSONException, OXException {
        JSONObject json = array.getJSONObject(index);
        return deserialize(json, timeZone);
    }

    private FileStorageObjectPermission deserializePermission(JSONObject json) throws JSONException {
        boolean isGuest = false;
        if (json.has("type")) {
            String type = json.getString("type");
            isGuest = type.toLowerCase().equals("guest") || type.toLowerCase().equals("anonymous");
        }

        DefaultFileStorageObjectPermission permission = isGuest ? new DefaultFileStorageGuestObjectPermission() : new DefaultFileStorageObjectPermission();
        if (json.has("bits")) {
            permission.setPermissions(json.getInt("bits"));
        }
        if (json.has("entity")) {
            permission.setEntity(json.getInt("entity"));
        }
        if (json.has("group")) {
            permission.setGroup(json.getBoolean("group"));
        }

        if (isGuest) {
            try {
                ShareRecipient recipient = ShareTool.parseRecipient(json, null);
                ((DefaultFileStorageGuestObjectPermission) permission).setRecipient(recipient);
            } catch (OXException e) {
                throw new JSONException(e.getMessage(), e);
            }
        }
        return permission;
    }
}
