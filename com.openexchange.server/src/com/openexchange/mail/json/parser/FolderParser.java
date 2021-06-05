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

package com.openexchange.mail.json.parser;

import static com.openexchange.mail.utils.MailFolderUtility.prepareMailFolderParam;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Folder;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.ajax.fields.FolderFields;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailProviderRegistry;
import com.openexchange.mail.api.MailProvider;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolderDescription;
import com.openexchange.mail.permission.MailPermission;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;

/**
 * {@link FolderParser} - Parses instances of {@link JSONObject} to instances of {@link MailFolder}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderParser {

    /**
     * No instantiation
     */
    private FolderParser() {
        super();
    }

    /**
     * Parses given instance of {@link JSONObject} to given instance of {@link MailFolder}
     *
     * @param jsonObj The JSON object (source)
     * @param mailFolder The mail folder (target), which should be empty
     * @param session The session
     * @param accountId The account ID
     * @throws OXException If parsing fails
     */
    public static void parse(JSONObject jsonObj, MailFolderDescription mailFolder, Session session, int accountId) throws OXException {
        try {
            if (jsonObj.has(FolderFields.TITLE)) {
                mailFolder.setName(jsonObj.getString(FolderFields.TITLE));
            }
            if (jsonObj.has(FolderChildFields.FOLDER_ID)) {
                final FullnameArgument arg = prepareMailFolderParam(jsonObj.getString(FolderChildFields.FOLDER_ID));
                mailFolder.setParentFullname(arg.getFullname());
                mailFolder.setParentAccountId(arg.getAccountId());
            }
            if (jsonObj.has(FolderFields.MODULE) && !jsonObj.getString(FolderFields.MODULE).equalsIgnoreCase(AJAXServlet.MODULE_MAIL)) {

                throw MailExceptionCode.MISSING_PARAMETER.create(FolderFields.MODULE);
            }
            if (jsonObj.hasAndNotNull(FolderFields.SUBSCRIBED)) {
                try {
                    mailFolder.setSubscribed(jsonObj.getInt(FolderFields.SUBSCRIBED) > 0);
                } catch (JSONException e) {
                    /*
                     * Not an integer value
                     */
                    mailFolder.setSubscribed(jsonObj.getBoolean(FolderFields.SUBSCRIBED));
                }
            }
            if (jsonObj.has(FolderFields.PERMISSIONS) && !jsonObj.isNull(FolderFields.PERMISSIONS)) {
                final JSONArray jsonArr = jsonObj.getJSONArray(FolderFields.PERMISSIONS);
                final int len = jsonArr.length();
                if (len > 0) {
                    final List<MailPermission> mailPerms = new ArrayList<MailPermission>(len);
                    final UserStorage us = UserStorage.getInstance();
                    final MailProvider provider = MailProviderRegistry.getMailProviderBySession(session, accountId);
                    for (int i = 0; i < len; i++) {
                        final JSONObject elem = jsonArr.getJSONObject(i);
                        if (!elem.has(FolderFields.ENTITY)) {
                            throw MailExceptionCode.MISSING_PARAMETER.create(FolderFields.ENTITY);
                        }
                        int entity;
                        try {
                            entity = elem.getInt(FolderFields.ENTITY);
                        } catch (JSONException e) {
                            final String entityStr = elem.getString(FolderFields.ENTITY);
                            entity = us.getUserId(entityStr, ContextStorage.getStorageContext(session.getContextId()));
                        }
                        final MailPermission mailPerm = provider.createNewMailPermission(session, accountId);
                        mailPerm.setEntity(entity);
                        if (!elem.has(FolderFields.BITS)) {
                            throw MailExceptionCode.MISSING_PARAMETER.create(FolderFields.BITS);
                        }
                        final int[] permissionBits = parsePermissionBits(elem.getInt(FolderFields.BITS));
                        if (!mailPerm.setAllPermission(permissionBits[0], permissionBits[1], permissionBits[2], permissionBits[3])) {
                            throw MailExceptionCode.INVALID_PERMISSION.create(
                                Integer.valueOf(permissionBits[0]),
                                Integer.valueOf(permissionBits[1]),
                                Integer.valueOf(permissionBits[2]),
                                Integer.valueOf(permissionBits[3]));
                        }
                        mailPerm.setFolderAdmin(permissionBits[4] > 0 ? true : false);
                        if (!elem.has(FolderFields.GROUP)) {
                            throw MailExceptionCode.MISSING_PARAMETER.create(FolderFields.GROUP);
                        }
                        mailPerm.setGroupPermission(elem.getBoolean(FolderFields.GROUP));
                        mailPerms.add(mailPerm);
                    }
                    mailFolder.addPermissions(mailPerms);
                }
            }
        } catch (JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private static final int[] mapping = { 0, 2, 4, -1, 8 };

    private static int[] parsePermissionBits(int bitsArg) {
        int bits = bitsArg;
        final int[] retval = new int[5];
        for (int i = retval.length - 1; i >= 0; i--) {
            final int shiftVal = (i * 7); // Number of bits to be shifted
            retval[i] = bits >> shiftVal;
            bits -= (retval[i] << shiftVal);
            if (retval[i] == Folder.MAX_PERMISSION) {
                retval[i] = OCLPermission.ADMIN_PERMISSION;
            } else if (i < (retval.length - 1)) {
                retval[i] = mapping[retval[i]];
            } else {
                retval[i] = retval[i];
            }
        }
        return retval;
    }
}
