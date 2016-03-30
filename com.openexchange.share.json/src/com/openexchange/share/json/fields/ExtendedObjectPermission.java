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

package com.openexchange.share.json.fields;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.groupware.ldap.User;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.core.tools.PermissionResolver;
import com.openexchange.share.recipient.RecipientType;

/**
 * {@link ExtendedObjectPermission}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ExtendedObjectPermission extends ExtendedPermission {

    private final File file;
    private final FileStorageObjectPermission permission;

    /**
     * Initializes a new {@link ExtendedObjectPermission}.
     *
     * @param permissionResolver The permission resolver
     * @param folder The folder
     * @param parentPermission The underlying permissions
     */
    public ExtendedObjectPermission(PermissionResolver permissionResolver, File file, FileStorageObjectPermission parentPermission) {
        super(permissionResolver);
        this.permission = parentPermission;
        this.file = file;
    }

    /**
     * Serializes the extended permissions as JSON.
     *
     * @param requestData The underlying request data, or <code>null</code> if not available
     * @return The serialized extended permissions
     */
    public JSONObject toJSON(AJAXRequestData requestData) throws JSONException, OXException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("entity", permission.getEntity());
        jsonObject.put("bits", permission.getPermissions());
        if (permission.isGroup()) {
            jsonObject.put("type", "group");
            addGroupInfo(requestData, jsonObject, resolver.getGroup(permission.getEntity()));
        } else {
            User user = resolver.getUser(permission.getEntity());
            if (null == user) {
                org.slf4j.LoggerFactory.getLogger(ExtendedObjectPermissionsField.class).warn(
                    "Can't resolve user permission entity {} for file {}", permission.getEntity(), file);
            } else if (user.isGuest()) {
                GuestInfo guest = resolver.getGuest(user.getId());
                if (guest == null) {
                    int contextId = requestData.getSession() == null ? -1 : requestData.getSession().getContextId();
                    throw ShareExceptionCodes.UNEXPECTED_ERROR.create("Could not resolve guest info for ID " + user.getId() + " in context " + contextId + ". " +
                        "It might have been deleted in the mean time or is in an inconsistent state.");
                }

                jsonObject.put("type", guest.getRecipientType().toString().toLowerCase());
                if (RecipientType.ANONYMOUS.equals(guest.getRecipientType())) {
                    addShareInfo(requestData, jsonObject, resolver.getLink(file, permission.getEntity()));
                } else {
                    addUserInfo(requestData, jsonObject, user);
                }
            } else {
                jsonObject.put("type", "user");
                addUserInfo(requestData, jsonObject, user);
            }
        }
        return jsonObject;
    }

}
