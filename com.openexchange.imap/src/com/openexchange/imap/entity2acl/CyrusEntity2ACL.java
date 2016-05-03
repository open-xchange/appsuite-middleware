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

package com.openexchange.imap.entity2acl;

import java.util.Arrays;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.imap.services.Services;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountFacade;
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
    public String getACLName(final int userId, final Context ctx, final Entity2ACLArgs entity2AclArgs) throws OXException {
        if (OCLPermission.ALL_GROUPS_AND_USERS == userId) {
            return AUTH_ID_ANYONE;
        }
        final MailAccountFacade mailAccountFacade = Services.getService(MailAccountFacade.class);
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
            return MailConfig.getMailLogin(mailAccountFacade.getMailAccount(((Integer) args[0]).intValue(), userId, ctx.getContextId()), userLoginInfo);
        } catch (final OXException e) {
            throw Entity2ACLExceptionCode.UNKNOWN_USER.create(Integer.valueOf(userId), Integer.valueOf(ctx.getContextId()), args[1].toString());
        }
    }

    @Override
    public UserGroupID getEntityID(final String pattern, final Context ctx, final Entity2ACLArgs entity2AclArgs) throws OXException {
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
        } catch (final ClassCastException e) {
            throw Entity2ACLExceptionCode.MISSING_ARG.create(e, new Object[0]);
        }
    }

    private static int getUserIDInternal(final String pattern, final Context ctx, final int accountId, final String serverUrl, final int sessionUser) throws OXException {
        final int[] ids = MailConfig.getUserIDsByMailLogin(pattern, MailAccount.DEFAULT_ID == accountId, serverUrl, ctx);
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
