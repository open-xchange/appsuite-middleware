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

package com.openexchange.appsuite.client.common.calls.infostore.mapping;

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
