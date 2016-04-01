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
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.imap.services.Services;
import com.openexchange.java.Strings;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.user.UserService;

/**
 * {@link CourierEntity2ACL} - Handles the ACL entities used by Courier IMAP server.
 * <p>
 * The current supported identifiers are:
 * <ul>
 * <li><i>owner</i></li>
 * <li><i>anyone</i></li>
 * </ul>
 * <p>
 * Missing handling for identifiers:
 * <ul>
 * <li><i>anonymous</i> (This is a synonym from <i>anyone</i>)</li>
 * <li><i>user=loginid</i> (Rights or negative rights for IMAP account "loginid")</li>
 * <li><i>group=name</i> (Rights or negative rights for account group "name")</li>
 * <li><i>administrators</i> (This is an alias for <i>group=administrators</i>)</li>
 * </ul>
 * <p>
 * The complete implementation should be able to handle an ACL like this one:
 *
 * <pre>
 * owner aceilrstwx anyone lr user=john w -user=mary r administrators aceilrstwx
 * </pre>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CourierEntity2ACL extends Entity2ACL {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CourierEntity2ACL.class);

    private static final CourierEntity2ACL INSTANCE = new CourierEntity2ACL();

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static CourierEntity2ACL getInstance() {
        return INSTANCE;
    }

    private static final String ALIAS_OWNER = "owner";

    private static final String ALIAS_ANYONE = "anyone";

    // ------------------------------------------------------------------------------------------------------------------------------

    private final Cache<String, CachedString> cacheSharedOwners;

    /**
     * Default constructor
     */
    private CourierEntity2ACL() {
        super();
        cacheSharedOwners = CacheBuilder.newBuilder().maximumSize(50000).expireAfterAccess(30, TimeUnit.MINUTES).build();
    }

    private final String getSharedFolderOwner(final String sharedFolderName, final char delim, String otherUserNamespace) {
        if (null == otherUserNamespace || !sharedFolderName.startsWith(otherUserNamespace, 0)) {
            return null;
        }

        CachedString wrapper = cacheSharedOwners.getIfPresent(sharedFolderName);
        if (null == wrapper) {
            String quotedDelim = Pattern.quote(String.valueOf(delim));
            // E.g. "Shared\\Q/\\E((?:\\\\\\Q/\\E|[^\\Q/\\E])+)\\Q/\\E\.+"
            StringBuilder abstractPattern = new StringBuilder().append(otherUserNamespace).append(quotedDelim);
            abstractPattern.append("((?:\\\\").append(quotedDelim).append("|[^").append(quotedDelim).append("])+)");
            abstractPattern.append(quotedDelim).append(".+");
            Pattern pattern = Pattern.compile(abstractPattern.toString(), Pattern.CASE_INSENSITIVE);
            Matcher m = pattern.matcher(sharedFolderName);
            if (m.matches()) {
                wrapper = CachedString.wrapperFor(m.group(1).replaceAll("\\s+", String.valueOf(delim)));
            } else {
                wrapper = CachedString.NIL;
            }
            cacheSharedOwners.put(sharedFolderName, wrapper);
        }

        return wrapper.string;
    }

    private boolean equalsOrStartsWith(String fullName, String publicNamespace, char separator) {
        if (Strings.isEmpty(publicNamespace)) {
            return false;
        }

        return (fullName.equals(publicNamespace) || fullName.startsWith(new StringBuilder(32).append(publicNamespace).append(separator).toString()));
    }

    @Override
    public String getACLName(final int userId, final Context ctx, final Entity2ACLArgs entity2AclArgs) throws OXException {
        if (userId == OCLPermission.ALL_GROUPS_AND_USERS) {
            return ALIAS_ANYONE;
        }

        Object[] args = entity2AclArgs.getArguments(IMAPServer.COURIER);
        if (args == null || args.length == 0) {
            throw Entity2ACLExceptionCode.MISSING_ARG.create();
        }

        int accountId = ((Integer) args[0]).intValue();
        String serverurl = args[1].toString();
        int sessionUser = ((Integer) args[2]).intValue();
        String fullName = (String) args[3];
        char separator = ((Character) args[4]).charValue();
        String sharedOwner = getSharedFolderOwner(fullName, separator, (String) args[5]);
        if (null == sharedOwner) {
            /*
             * A non-shared folder
             */
            if ((sessionUser == userId) && !equalsOrStartsWith(fullName, (String) args[6], separator)) {
                /*
                 * Logged-in user is equal to given user
                 */
                return ALIAS_OWNER;
            }
            return getACLNameInternal(userId, ctx, accountId, serverurl);
        }
        /*
         * A shared folder
         */
        final int sharedOwnerID = getUserIDInternal(sharedOwner, ctx, accountId, serverurl, sessionUser);
        if (sharedOwnerID == userId) {
            /*
             * Owner is equal to given user
             */
            return ALIAS_OWNER;
        }
        return getACLNameInternal(userId, ctx, accountId, serverurl);
    }

    private static final String getACLNameInternal(final int userId, final Context ctx, final int accountId, final String serverUrl) throws OXException {
        final MailAccountStorageService storageService = Services.getService(MailAccountStorageService.class);
        if (null == storageService) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create( MailAccountStorageService.class.getName());
        }
        final String userLoginInfo;
        {
            final UserService userService = Services.getService(UserService.class);
            if (null == userService) {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create( UserService.class.getName());
            }
            userLoginInfo = userService.getUser(userId, ctx).getLoginInfo();
        }
        try {
            return MailConfig.getMailLogin(storageService.getMailAccount(accountId, userId, ctx.getContextId()), userLoginInfo);
        } catch (final OXException e) {
            throw Entity2ACLExceptionCode.UNKNOWN_USER.create(
                Integer.valueOf(userId),
                Integer.valueOf(ctx.getContextId()),
                serverUrl);
        }
    }

    @Override
    public UserGroupID getEntityID(final String pattern, final Context ctx, final Entity2ACLArgs entity2AclArgs) throws OXException {
        if (ALIAS_ANYONE.equalsIgnoreCase(pattern)) {
            return ALL_GROUPS_AND_USERS;
        }
        final Object[] args = entity2AclArgs.getArguments(IMAPServer.COURIER);
        if (args == null || args.length == 0) {
            throw Entity2ACLExceptionCode.MISSING_ARG.create();
        }
        final int accountId = ((Integer) args[0]).intValue();
        final String serverUrl = args[1].toString();
        final int sessionUser = ((Integer) args[2]).intValue();
        final String sharedOwner = getSharedFolderOwner((String) args[3], ((Character) args[4]).charValue(), (String) args[5]);
        if (null == sharedOwner) {
            /*
             * A non-shared folder
             */
            if (ALIAS_OWNER.equalsIgnoreCase(pattern)) {
                /*
                 * Map alias "owner" to logged-in user
                 */
                return getUserRetval(sessionUser);
            }
            return getUserRetval(getUserIDInternal(pattern, ctx, accountId, serverUrl, sessionUser));
        }
        /*
         * A shared folder
         */
        if (ALIAS_OWNER.equalsIgnoreCase(pattern)) {
            /*
             * Map alias "owner" to shared folder owner
             */
            return getUserRetval(getUserIDInternal(sharedOwner, ctx, accountId, serverUrl, sessionUser));
        }
        return getUserRetval(getUserIDInternal(pattern, ctx, accountId, serverUrl, sessionUser));
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
