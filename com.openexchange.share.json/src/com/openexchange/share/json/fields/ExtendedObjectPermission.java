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

package com.openexchange.share.json.fields;

import static com.openexchange.java.Autoboxing.I;
import static org.slf4j.LoggerFactory.getLogger;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.core.tools.PermissionResolver;
import com.openexchange.share.recipient.RecipientType;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;

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
        jsonObject.put("identifier", null != permission.getIdentifier() ? permission.getIdentifier() : String.valueOf(permission.getEntity()));
        if (0 < permission.getEntity() || 0 == permission.getEntity() && permission.isGroup()) {
            jsonObject.put("entity", permission.getEntity());
        }
        jsonObject.put("bits", permission.getPermissions());
        if (permission.isGroup()) {
            jsonObject.put("type", "group");
            if (null != permission.getEntityInfo()) {
                addEntityInfo(requestData, jsonObject, permission.getEntityInfo());
            } else {
                addGroupInfo(requestData, jsonObject, resolver.getGroup(permission.getEntity()));
            }
            return jsonObject;
        }
        if (null != permission.getEntityInfo()) {
            /*
             * add extended information based on provided entity info object
             */
            addEntityInfo(requestData, jsonObject, permission.getEntityInfo());
            return jsonObject;
        }
        if (0 >= permission.getEntity()) {
            getLogger(ExtendedObjectPermission.class).debug("Can't resolve user permission entity {} for file {}", I(permission.getEntity()), file);
            return jsonObject;
        }
        /*
         * lookup and add extended information for internal user/guest
         */
        User user = resolver.getUser(permission.getEntity());
        if (null == user) {
            getLogger(ExtendedObjectPermission.class).debug("Can't resolve user permission entity {} for file {}", I(permission.getEntity()), file);
            return jsonObject;
        }
        if (false == user.isGuest()) {
            jsonObject.put("type", "user");
            addUserInfo(requestData, jsonObject, user);
            return jsonObject;
        }
        GuestInfo guest = resolver.getGuest(user.getId());
        if (guest == null) {
            // @formatter:off
            throw ShareExceptionCodes.UNEXPECTED_ERROR.create("Could not resolve guest info for ID " + user.getId() + " in context " + 
                    extractContextId(requestData) + ". " + "It might have been deleted in the mean time or is in an inconsistent state.");
            // @formatter:on
        }

        jsonObject.put("type", guest.getRecipientType().toString().toLowerCase());
        if (RecipientType.ANONYMOUS.equals(guest.getRecipientType())) {
            addShareInfo(requestData, jsonObject, resolver.getLink(file, permission.getEntity()));
        } else {
            addUserInfo(requestData, jsonObject, user);
        }
        return jsonObject;
    }

    /**
     * Extracts the context identifier from the specified request data.
     * If the session is non-existent, -1 will be returned
     *
     * @param requestData The request data
     * @return The context identifier or -1 if the session is <code>null</code>
     */
    private int extractContextId(AJAXRequestData requestData) {
        ServerSession session = requestData.getSession();
        if (session == null) {
            return -1;
        }
        return session.getContextId();
    }
}
