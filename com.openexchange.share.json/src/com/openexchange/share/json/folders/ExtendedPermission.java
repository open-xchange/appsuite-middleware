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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.share.json.folders;

import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.ContactFields;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.folderstorage.Permissions;
import com.openexchange.group.Group;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.ldap.User;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.ShareInfo;
import com.openexchange.share.core.DefaultRequestContext;
import com.openexchange.share.recipient.RecipientType;

/**
 * {@link ExtendedPermission}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ExtendedPermission {

    private final ExtendedPermissionResolver resolver;
    private final FolderObject folder;
    private final OCLPermission permission;

    /**
     * Initializes a new {@link ExtendedPermission}.
     *
     * @param permissionResolver The permission resolver
     * @param folder The folder
     * @param parentPermission The underlying OCL permissions
     */
    public ExtendedPermission(ExtendedPermissionResolver permissionResolver, FolderObject folder, OCLPermission parentPermission) {
        super();
        this.permission = parentPermission;
        this.resolver = permissionResolver;
        this.folder = folder;
    }

    /**
     * Serializes the extended permissions as JSON.
     *
     * @param requestData The underlying request data, or <code>null</code> if not available
     * @return The serialized extended permissions
     */
    public JSONObject toJSON(AJAXRequestData requestData) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("entity", permission.getEntity());
        jsonObject.put("bit", Permissions.createPermissionBits(permission.getFolderPermission(), permission.getReadPermission(),
            permission.getWritePermission(), permission.getDeletePermission(), permission.isFolderAdmin()));
        if (permission.isGroupPermission()) {
            jsonObject.put("group", permission.isGroupPermission());
            addGroupInfo(requestData, jsonObject, resolver.getGroup(permission.getEntity()));
        } else {
            User user = resolver.getUser(permission.getEntity());
            if (user.isGuest()) {
                ShareInfo share = resolver.getShare(folder, permission);
                addShareInfo(requestData, jsonObject, resolver.getShare(folder, permission));
                if (null != share && RecipientType.GUEST.equals(share.getGuest().getRecipientType())) {
                    addUserInfo(requestData, jsonObject, user);
                }
            } else {
                addUserInfo(requestData, jsonObject, user);
            }
        }
        return jsonObject;
    }

    private void addGroupInfo(AJAXRequestData requestData, JSONObject jsonObject, Group group) throws JSONException {
        if (null != group) {
            jsonObject.put(ContactFields.DISPLAY_NAME, group.getDisplayName());
        }
    }

    private void addUserInfo(AJAXRequestData requestData, JSONObject jsonObject, User user) throws JSONException {
        if (null != user) {
            Contact userContact = resolver.getUserContact(user.getId());
            if (null != userContact) {
                addContactInfo(requestData, jsonObject, userContact);
            } else {
                addContactInfo(requestData, jsonObject, user);
            }
        }
    }

    private void addContactInfo(AJAXRequestData requestData, JSONObject jsonObject, Contact userContact) throws JSONException {
        if (null != userContact) {
            jsonObject.putOpt(ContactFields.DISPLAY_NAME, userContact.getDisplayName());
            JSONObject jsonContact = new JSONObject();
            jsonContact.putOpt(ContactFields.EMAIL1, userContact.getEmail1());
            jsonContact.putOpt(ContactFields.TITLE, userContact.getTitle());
            jsonContact.putOpt(ContactFields.LAST_NAME, userContact.getSurName());
            jsonContact.putOpt(ContactFields.FIRST_NAME, userContact.getGivenName());
            jsonContact.putOpt(ContactFields.IMAGE1_URL, resolver.getImageURL(userContact.getInternalUserId()));
            jsonObject.put("contact", jsonContact);
        }
    }

    private void addContactInfo(AJAXRequestData requestData, JSONObject jsonObject, User user) throws JSONException {
        if (null != user) {
            jsonObject.putOpt(ContactFields.DISPLAY_NAME, user.getDisplayName());
            JSONObject jsonContact = new JSONObject();
            jsonContact.putOpt(ContactFields.EMAIL1, user.getMail());
            jsonContact.putOpt(ContactFields.LAST_NAME, user.getSurname());
            jsonContact.putOpt(ContactFields.FIRST_NAME, user.getGivenName());
            jsonContact.putOpt(ContactFields.IMAGE1_URL, resolver.getImageURL(user.getId()));
            jsonObject.put("contact", jsonContact);
        }
    }

    private void addShareInfo(AJAXRequestData requestData, JSONObject jsonObject, ShareInfo share) throws JSONException {
        if (null != share) {
            GuestInfo guest = share.getGuest();
            jsonObject.put("type", guest.getRecipientType().toString().toLowerCase());
//            jsonObject.putOpt("authentication", null != guest.getAuthentication() ? guest.getAuthentication().toString().toLowerCase() : null);
//            jsonObject.put("base_token", guest.getBaseToken());
//            jsonObject.put("token", share.getToken());
            if (null != requestData) {
                jsonObject.putOpt("share_url", share.getShareURL(DefaultRequestContext.newInstance(requestData)));
            }
            Date expiryDate = share.getShare().getTarget().getExpiryDate();
            if (null != expiryDate) {
                long time = null != requestData ? addTimeZoneOffset(expiryDate.getTime(), getTimeZone(requestData)) : expiryDate.getTime();
                jsonObject.put("expiry", time);
            }
            Map<String, Object> meta = share.getShare().getTarget().getMeta();
            if (null != meta) {
                jsonObject.put("meta", JSONCoercion.coerceToJSON(meta));
            }
            jsonObject.putOpt("password", guest.getPassword());
        }
    }

    private static long addTimeZoneOffset(long date, TimeZone timeZone) {
        return null == timeZone ? date : date + timeZone.getOffset(date);
    }

    private static TimeZone getTimeZone(AJAXRequestData requestData) {
        String timeZoneID = requestData.getParameter("timezone");
        if (null == timeZoneID) {
            timeZoneID = requestData.getSession().getUser().getTimeZone();
        }
        return TimeZone.getTimeZone(timeZoneID);
    }

}
