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
 * {@link PermissionMapper}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @param <O> The class of list element
 * @since v7.10.5
 */
public abstract class PermissionMapper<O> extends ListItemMapping<Permission, O, JSONObject> {

    /**
     * Initializes a new {@link PermissionMapper}.
     *
     * @param ajaxName The mapped ajax name
     * @param columnID the mapped column identifier
     */
    public PermissionMapper(String ajaxName, Integer columnID) {
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
        return Permissions.createPermission(
            from.has("entity") ? from.getInt("entity") : 0,
            from.has("group") && from.getBoolean("group"),
            from.has("bits") ? from.getInt("bits") : 0);
        //@formatter:on
    }

    @Override
    public JSONObject serialize(Permission from, TimeZone timeZone) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("entity", from.getEntity());
        json.put("group", from.isGroup());
        json.put("bits", Permissions.createPermissionBits(from));
        return json;
    }
}
