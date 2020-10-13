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

import java.util.Date;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.api.client.common.calls.folders.ExtendedPermission;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.json.ArrayMapping;

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

}
