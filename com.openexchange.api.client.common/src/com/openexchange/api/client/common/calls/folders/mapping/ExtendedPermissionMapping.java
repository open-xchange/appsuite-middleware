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

package com.openexchange.api.client.common.calls.folders.mapping;

import java.util.Date;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.api.client.common.calls.folders.ExtendedPermission;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.json.ArrayMapping;
import com.openexchange.session.Session;

/**
 * {@link ExtendedPermissionMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.5
 */
public abstract class ExtendedPermissionMapping<O> extends ArrayMapping<ExtendedPermission, O> {

    /**
     * Initializes a new {@link ExtendedPermissionMapping}.
     *
     * @param ajaxName The mapped ajax name
     * @param columnID the mapped column identifier
     */
    public ExtendedPermissionMapping(String ajaxName, Integer columnID) {
        super(ajaxName, columnID);
    }

    @Override
    public ExtendedPermission[] newArray(int size) {
        return new ExtendedPermission[size];
    }

    @Override
    protected ExtendedPermission deserialize(JSONArray array, int index) throws JSONException, OXException {
        JSONObject jsonPermission = array.getJSONObject(index);
        if (null == jsonPermission) {
            return null;
        }
        ExtendedPermission permission = new ExtendedPermission();
        permission.setBits(jsonPermission.optInt("bits"));
        permission.setIdentifier(jsonPermission.optString("identifier", null));
        permission.setEntity(jsonPermission.optInt("entity", -1));
        permission.setType(jsonPermission.optString("type", null));
        permission.setDisplayName(jsonPermission.optString("display_name", null));
        permission.setInheritedFrom(jsonPermission.optString("isInheritedFrom", null));
        permission.setShareUrl(jsonPermission.optString("share_url", null));
        permission.setPassword(jsonPermission.optString("password", null));
        permission.setExpiryDate(jsonPermission.has("expiry_date") ? new Date(jsonPermission.getLong("expiry_date")) : null);
        permission.setInherited(jsonPermission.optBoolean("isInherited"));
        JSONObject jsonContact = jsonPermission.optJSONObject("contact");
        if (null != jsonContact) {
            ExtendedPermission.Contact contact = new ExtendedPermission.Contact();
            contact.setEmail1(jsonContact.optString("email1", null));
            contact.setTitle(jsonContact.optString("title", null));
            contact.setFirstName(jsonContact.optString("first_name", null));
            contact.setLastName(jsonContact.optString("last_name", null));
            contact.setImage1Url(jsonContact.optString("image1_url", null));
            permission.setContact(contact);
        }
        return permission;
    }

    @Override
    public Object serialize(O from, TimeZone timeZone, Session session) throws JSONException {
        throw new UnsupportedOperationException();
    }

}
