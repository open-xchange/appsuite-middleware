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

import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Permissions;
import com.openexchange.groupware.tools.mappings.json.ListItemMapping;

/**
 * {@link PermissionMapping}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @param <O> The class of list element
 * @since v7.10.5
 */
public abstract class PermissionMapping<O> extends ListItemMapping<Permission, O, JSONObject> {

    /**
     * Initializes a new {@link PermissionMapping}.
     *
     * @param ajaxName The mapped ajax name
     * @param columnID the mapped column identifier
     */
    public PermissionMapping(String ajaxName, Integer columnID) {
        super(ajaxName, columnID);
    }

    @Override
    protected Permission deserialize(JSONArray array, int index, TimeZone timeZone) throws JSONException, OXException {
        JSONObject jsonObject = array.getJSONObject(index);
        return deserialize(jsonObject, timeZone);
    }

    @Override
    public Permission deserialize(JSONObject from, TimeZone timeZone) throws JSONException {
        //@formatter:off
        Permission permission = Permissions.createPermission(
            from.has("entity") ? from.getInt("entity") : -1,
            from.has("group") && from.getBoolean("group"),
            from.has("bits") ? from.getInt("bits") : 0);
        //@formatter:on
        permission.setIdentifier(from.optString("identifier", null));
        return permission;
    }

    @Override
    public JSONObject serialize(Permission from, TimeZone timeZone) throws JSONException {
        JSONObject json = new JSONObject();
        json.putOpt("identifier", from.getIdentifier());
        if (0 < from.getEntity() || 0 == from.getEntity() && from.isGroup()) {
            json.put("entity", from.getEntity());
        }
        json.put("group", from.isGroup());
        json.put("bits", Permissions.createPermissionBits(from));
        return json;
    }

}
