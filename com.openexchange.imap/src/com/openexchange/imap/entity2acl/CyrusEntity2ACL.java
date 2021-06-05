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

package com.openexchange.imap.entity2acl;

import java.util.Arrays;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.imap.services.Services;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.user.UserService;

/**
 * {@link CyrusEntity2ACL} - Handles the ACL entities used by Cyrus IMAP server.
 * <p>
 * The current supported identifiers are:
 * <ul>
 * <li><i>anyone</i> which refers to all users, including the anonymous user</li>
 * </ul>
 * <p>
 * Missing handling for identifiers:
 * <ul>
 * <li><i>anonymous</i> which refers to the anonymous, or unauthenticated user</li>
 * </ul>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CyrusEntity2ACL extends Entity2ACL {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CyrusEntity2ACL.class);

    private static final CyrusEntity2ACL INSTANCE = new CyrusEntity2ACL();

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static CyrusEntity2ACL getInstance() {
        return INSTANCE;
    }

    private static final String AUTH_ID_ANYONE = "anyone";

    /**
     * Default constructor
     */
    private CyrusEntity2ACL() {
        super();
    }

    @Override
    public String getACLName(int userId, Context ctx, Entity2ACLArgs entity2AclArgs) throws OXException {
        if (OCLPermission.ALL_GROUPS_AND_USERS == userId) {
            return AUTH_ID_ANYONE;
        }
        final MailAccountStorageService storageService = Services.getService(MailAccountStorageService.class);
        final String userLoginInfo;
        {
            final UserService userService = Services.getService(UserService.class);
            if (null == userService) {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(UserService.class.getName());
            }
            userLoginInfo = userService.getUser(userId, ctx).getLoginInfo();
        }
        final Object[] args = entity2AclArgs.getArguments(IMAPServer.CYRUS);
        if (args == null || args.length == 0) {
            throw Entity2ACLExceptionCode.MISSING_ARG.create();
        }
        try {
            return MailConfig.getMailLogin(storageService.getMailAccount(((Integer) args[0]).intValue(), userId, ctx.getContextId()), userLoginInfo, userId, ctx.getContextId());
        } catch (OXException e) {
            throw Entity2ACLExceptionCode.UNKNOWN_USER.create(e, Integer.valueOf(userId), Integer.valueOf(ctx.getContextId()), args[1].toString());
        }
    }

    @Override
    public UserGroupID getEntityID(String pattern, Context ctx, Entity2ACLArgs entity2AclArgs) throws OXException {
        if (AUTH_ID_ANYONE.equalsIgnoreCase(pattern)) {
            return ALL_GROUPS_AND_USERS;
        }
        final Object[] args = entity2AclArgs.getArguments(IMAPServer.CYRUS);
        if (args == null || args.length == 0) {
            throw Entity2ACLExceptionCode.MISSING_ARG.create();
        }
        try {
            final int accountId = ((Integer) args[0]).intValue();
            final String serverUrl = args[1].toString();
            final int sessionUser = ((Integer) args[2]).intValue();
            return getUserRetval(getUserIDInternal(pattern, ctx, accountId, serverUrl, sessionUser));
        } catch (ClassCastException e) {
            throw Entity2ACLExceptionCode.MISSING_ARG.create(e, new Object[0]);
        }
    }

    private static int getUserIDInternal(String pattern, Context ctx, int accountId, String serverUrl, int sessionUser) throws OXException {
        final int[] ids = MailConfig.getUserIDsByMailLogin(pattern, MailAccount.DEFAULT_ID == accountId, serverUrl, sessionUser, ctx);
        if (0 == ids.length) {
            throw Entity2ACLExceptionCode.RESOLVE_USER_FAILED.create(pattern);
        }
        if (1 == ids.length) {
            return ids[0];
        }
        // Prefer session user
        Arrays.sort(ids);
        final int pos = Arrays.binarySearch(ids, sessionUser);
        if (pos >= 0) {
            LOG.warn("Found multiple users with login \"{}\" subscribed to IMAP server \"{}\": {}\nThe session user's ID is returned.", pattern, serverUrl, Arrays.toString(ids));
            return ids[pos];
        }
        LOG.warn("Found multiple users with login \"{}\" subscribed to IMAP server \"{}\": {}\nThe first found user is returned.", pattern, serverUrl, Arrays.toString(ids));
        return ids[0];
    }

}
