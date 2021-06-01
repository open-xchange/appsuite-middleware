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

import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link DovecotEntity2ACL} - Handles the ACL entities used by Dovecot IMAP server.
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
public class DovecotEntity2ACL extends AbstractOwnerCapableEntity2ACL {

    private static final DovecotEntity2ACL INSTANCE = new DovecotEntity2ACL();

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static DovecotEntity2ACL getInstance() {
        return INSTANCE;
    }

    private static final String ALIAS_ANYONE = "anyone";

    // ------------------------------------------------------------------------------------------------------------------------------

    /**
     * Default constructor
     */
    private DovecotEntity2ACL() {
        super();
    }

    @Override
    public String getACLName(int userId, Context ctx, Entity2ACLArgs entity2AclArgs) throws OXException {
        if (userId == OCLPermission.ALL_GROUPS_AND_USERS) {
            return ALIAS_ANYONE;
        }

        Object[] args = entity2AclArgs.getArguments(IMAPServer.DOVECOT);
        if (args == null || args.length == 0) {
            throw Entity2ACLExceptionCode.MISSING_ARG.create();
        }

        int accountId = ((Integer) args[0]).intValue();
        String serverUrl = args[1].toString();
        int sessionUser = ((Integer) args[2]).intValue();
        String fullName = (String) args[3];
        char separator = ((Character) args[4]).charValue();
        String sharedOwner = getSharedFolderOwner(fullName, separator, (String[]) args[5]);
        if (null == sharedOwner) {
            /*
             * A non-shared folder
             */
            if ((sessionUser == userId) && !equalsOrStartsWith(fullName, (String[]) args[6], separator)) {
                /*
                 * Logged-in user is equal to given user
                 */
                return ALIAS_OWNER;
            }
            return getACLNameInternal(userId, ctx, accountId, serverUrl);
        }
        /*
         * A shared folder
         */
        final int sharedOwnerID = getUserIDInternal(sharedOwner, ctx, accountId, serverUrl, sessionUser);
        if (sharedOwnerID == userId) {
            /*
             * Owner is equal to given user
             */
            return ALIAS_OWNER;
        }
        return getACLNameInternal(userId, ctx, accountId, serverUrl);
    }

    @Override
    public UserGroupID getEntityID(String pattern, Context ctx, Entity2ACLArgs entity2AclArgs) throws OXException {
        if (ALIAS_ANYONE.equalsIgnoreCase(pattern)) {
            return ALL_GROUPS_AND_USERS;
        }
        final Object[] args = entity2AclArgs.getArguments(IMAPServer.DOVECOT);
        if (args == null || args.length == 0) {
            throw Entity2ACLExceptionCode.MISSING_ARG.create();
        }
        final int accountId = ((Integer) args[0]).intValue();
        final String serverUrl = args[1].toString();
        final int sessionUser = ((Integer) args[2]).intValue();
        final String sharedOwner = getSharedFolderOwner((String) args[3], ((Character) args[4]).charValue(), (String[]) args[5]);
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

}
