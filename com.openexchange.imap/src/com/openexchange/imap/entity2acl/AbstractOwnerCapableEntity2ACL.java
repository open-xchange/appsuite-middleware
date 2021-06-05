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
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
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
import com.openexchange.user.UserService;


/**
 * {@link AbstractOwnerCapableEntity2ACL}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public abstract class AbstractOwnerCapableEntity2ACL extends Entity2ACL {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractOwnerCapableEntity2ACL.class);
    }

    /** The identifier of special "owner" ACL */
    protected static final String ALIAS_OWNER = "owner";

    private final Cache<String, CachedString> cacheSharedOwners;

    /**
     * Initializes a new {@link AbstractOwnerCapableEntity2ACL}.
     */
    protected AbstractOwnerCapableEntity2ACL() {
        super();
        cacheSharedOwners = CacheBuilder.newBuilder().maximumSize(50000).expireAfterAccess(30, TimeUnit.MINUTES).build();
    }

    /**
     * Gets the identifier of the owner for specified full name of a possibly shared folder.
     *
     * @param sharedFolderName The full name of the folder that might be shared
     * @param delim The delimiting character
     * @param otherUserNamespaces The paths of known shared folders
     * @return The identifier or <code>null</code>
     */
    protected final String getSharedFolderOwner(String sharedFolderName, char delim, String[] otherUserNamespaces) {
        if (null == otherUserNamespaces) {
            return null;
        }

        for (String otherUserNamespace : otherUserNamespaces) {
            if (sharedFolderName.startsWith(otherUserNamespace, 0)) {
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
        }

        return null;
    }

    /**
     * Checks if given full name is equal to or starts with any of given public folder paths
     *
     * @param fullName The full name to check
     * @param publicNamespaces The paths of known public folders
     * @param separator The separator character
     * @return
     */
    protected boolean equalsOrStartsWith(String fullName, String[] publicNamespaces, char separator) {
        if (publicNamespaces == null) {
            return false;
        }

        for (String publicNamespace : publicNamespaces) {
            if (Strings.isNotEmpty(publicNamespace) && (fullName.equals(publicNamespace) || fullName.startsWith(new StringBuilder(32).append(publicNamespace).append(separator).toString()))) {
                return true;
            }
        }

        return false;
    }

    protected static final String getACLNameInternal(int userId, Context ctx, int accountId, String serverUrl) throws OXException {
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
            return MailConfig.getMailLogin(storageService.getMailAccount(accountId, userId, ctx.getContextId()), userLoginInfo, userId, ctx.getContextId());
        } catch (OXException e) {
            throw Entity2ACLExceptionCode.UNKNOWN_USER.create(e,
                Integer.valueOf(userId),
                Integer.valueOf(ctx.getContextId()),
                serverUrl);
        }
    }

    protected static int getUserIDInternal(String pattern, Context ctx, int accountId, String serverUrl, int sessionUser) throws OXException {
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
            LoggerHolder.LOG.warn("Found multiple users with login \"{}\" subscribed to IMAP server \"{}\": {}\nThe session user's ID is returned.", pattern, serverUrl, Arrays.toString(ids));
            return ids[pos];
        }
        LoggerHolder.LOG.warn("Found multiple users with login \"{}\" subscribed to IMAP server \"{}\": {}\nThe first found user is returned.", pattern, serverUrl, Arrays.toString(ids));
        return ids[0];
    }

}
