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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import static com.openexchange.imap.services.IMAPServiceRegistry.getServiceRegistry;
import static com.openexchange.mail.utils.ProviderUtility.toSocketAddr;
import java.net.InetSocketAddress;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountException;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.server.ServiceException;
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

    private static final String AUTH_ID_ANYONE = "anyone";

    /**
     * Default constructor
     */
    public CyrusEntity2ACL() {
        super();
    }

    @Override
    public String getACLName(final int userId, final Context ctx, final Entity2ACLArgs entity2AclArgs) throws AbstractOXException {
        if (OCLPermission.ALL_GROUPS_AND_USERS == userId) {
            return AUTH_ID_ANYONE;
        }
        final MailAccountStorageService storageService = getServiceRegistry().getService(MailAccountStorageService.class, true);
        final String userLoginInfo;
        {
            final UserService userService = getServiceRegistry().getService(UserService.class, true);
            if (null == userService) {
                throw new ServiceException(ServiceException.Code.SERVICE_UNAVAILABLE, UserService.class.getName());
            }
            userLoginInfo = userService.getUser(userId, ctx).getLoginInfo();
        }
        final Object[] args = entity2AclArgs.getArguments(IMAPServer.COURIER);
        if (args == null || args.length == 0) {
            throw new Entity2ACLException(Entity2ACLException.Code.MISSING_ARG);
        }
        try {
            return MailConfig.getMailLogin(
                storageService.getMailAccount(((Integer) args[0]).intValue(), userId, ctx.getContextId()),
                userLoginInfo);
        } catch (final MailAccountException e) {
            throw new Entity2ACLException(
                Entity2ACLException.Code.UNKNOWN_USER,
                Integer.valueOf(userId),
                Integer.valueOf(ctx.getContextId()),
                args[1].toString());
        }
    }

    @Override
    public int[] getEntityID(final String pattern, final Context ctx, final Entity2ACLArgs entity2AclArgs) throws AbstractOXException {
        if (AUTH_ID_ANYONE.equalsIgnoreCase(pattern)) {
            return ALL_GROUPS_AND_USERS;
        }
        final Object[] args = entity2AclArgs.getArguments(IMAPServer.COURIER);
        if (args == null || args.length == 0) {
            throw new Entity2ACLException(Entity2ACLException.Code.MISSING_ARG);
        }
        final int accountId;
        final InetSocketAddress imapAddr;
        try {
            accountId = ((Integer) args[0]).intValue();
            imapAddr = (InetSocketAddress) args[1];
        } catch (final ClassCastException e) {
            throw new Entity2ACLException(Entity2ACLException.Code.MISSING_ARG, e, new Object[0]);
        }
        return getUserRetval(getUserIDInternal(pattern, ctx, accountId, imapAddr));
    }

    private static int getUserIDInternal(final String pattern, final Context ctx, final int accountId, final InetSocketAddress imapAddr) throws AbstractOXException {
        final int[] ids = MailConfig.getUserIDsByMailLogin(pattern, MailAccount.DEFAULT_ID == accountId, imapAddr, ctx);
        if (ids.length == 1) {
            return ids[0];
        }
        final MailAccountStorageService storageService = getServiceRegistry().getService(MailAccountStorageService.class, true);
        for (final int id : ids) {
            if (imapAddr.equals(toSocketAddr(
                MailConfig.getMailServerURL(storageService.getMailAccount(accountId, id, ctx.getContextId())),
                143))) {
                return id;
            }
        }
        throw new Entity2ACLException(Entity2ACLException.Code.RESOLVE_USER_FAILED, pattern);
    }
}
