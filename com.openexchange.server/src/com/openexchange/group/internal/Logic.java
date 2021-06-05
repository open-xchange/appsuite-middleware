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

package com.openexchange.group.internal;

import java.util.Date;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.group.GroupExceptionCodes;
import com.openexchange.group.GroupStorage;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.data.Check;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.server.services.ServerServiceRegistry;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Logic {

    /**
     * Prevent instantiation.
     */
    private Logic() {
        super();
    }

    /**
     *
     * @param group
     * @throws OXException
     */
    final static void checkMandatoryForCreate(final Group group)
        throws OXException {
        if (!group.isSimpleNameSet()) {
            throw GroupExceptionCodes.MANDATORY_MISSING.create("simpleName");
        }
        String tmp = group.getSimpleName();
        if (null == tmp || tmp.length() == 0) {
            throw GroupExceptionCodes.MANDATORY_MISSING.create("simpleName");
        }
        if (!group.isDisplayNameSet()) {
            throw GroupExceptionCodes.MANDATORY_MISSING.create("displayName");
        }
        tmp = group.getDisplayName();
        if (null == tmp || tmp.length() == 0) {
            throw GroupExceptionCodes.MANDATORY_MISSING.create("displayName");
        }
        // lastModified is set here, to be able to simply store it in the
        // storage class. This allows storing a given lastModified.
        if (!group.isLastModifiedSet()) {
            group.setLastModified(new Date());
        }
        // Unique identifier will be set in transaction when the identifier gets
        // generated.
    }

    /**
     *
     * @param group
     * @throws OXException
     */
    final static void checkMandatoryForUpdate(final Group group)
        throws OXException {
        if (!group.isIdentifierSet()) {
            throw GroupExceptionCodes.MANDATORY_MISSING.create("identifier");
        }
        if (group.isSimpleNameSet()) {
            final String tmp = group.getSimpleName();
            if (null == tmp || tmp.length() == 0) {
                throw GroupExceptionCodes.MANDATORY_MISSING.create("simpleName");
            }
        }
        if (group.isDisplayNameSet()) {
            final String tmp = group.getDisplayName();
            if (null == tmp || tmp.length() == 0) {
                throw GroupExceptionCodes.MANDATORY_MISSING.create("displayName");
            }
        }
        // lastModified is set here, to be able to simply store it in the
        // storage class. This allows storing a given lastModified.
        if (!group.isLastModifiedSet()) {
            group.setLastModified(new Date());
        }
        // Unique identifier will be set in transaction when the identifier gets
        // generated.
    }

    private static final String ALLOWED_CHARS = "[ $@%\\.+a-zA-Z0-9_-]";
    private static final String ALLOWED_CHARS_PROPERTY="CHECK_GROUP_UID_REGEXP";

    /**
     * Validates of the simple name of the group only contains allowed
     * characters.
     * @param group Group.
     * @throws OXException if the name contains not allowed characters.
     */
    final static void validateSimpleName(final Group group)
        throws OXException {
        if (!group.isSimpleNameSet()) {
            return;
        }
        // Check for allowed chars:
        // abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-+.%$@
        final String groupName = group.getSimpleName();
        ConfigurationService configurationService = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
        String allowedChars = configurationService.getProperty(ALLOWED_CHARS_PROPERTY, ALLOWED_CHARS);
        final String illegal = groupName.replaceAll(allowedChars, "");
        if (illegal.length() > 0) {
            throw GroupExceptionCodes.NOT_ALLOWED_SIMPLE_NAME.create(illegal);
        }
    }

    /**
     * Checks if some string contains problematic data.
     * @param group Group.
     * @throws OXException if some string contains problematic data.
     */
    final static void checkData(final Group group) throws OXException {
        if (!group.isDisplayNameSet()) {
            return;
        }
        final String result = Check.containsInvalidChars(group
            .getDisplayName());
        if (null != result) {
            throw GroupExceptionCodes.INVALID_DATA.create(result);
        }
    }

    /**
     * Checks if other groups with same simple name exist.
     * @param storage Storage implementation.
     * @param ctx Context.
     * @param group Group to check for duplicates.
     * @throws OXException if a duplicate is detected.
     */
    final static void checkForDuplicate(GroupStorage storage, Context ctx, Group group, boolean checki18nNames) throws OXException {
        if (group.isSimpleNameSet()) {
            Group[] others = storage.searchGroups(group.getSimpleName(), true, ctx);
            for (Group other : others) {
                if (group.getSimpleName().equals(other.getSimpleName()) && group.getIdentifier() != other.getIdentifier()) {
                    throw GroupExceptionCodes.DUPLICATE.create(Integer.valueOf(other.getIdentifier()));
                }
            }
        }
        if (group.isDisplayNameSet()) {
            String displayName = group.getDisplayName();

            if (checki18nNames) {
                int id = group.getIdentifier();
                if (id > 0) {
                    if (!storage.getGroup(id, ctx).getDisplayName().equals(displayName)) {
                        for (String i18nName : GroupI18nNamesService.getInstance().getI18nNames()) {
                            if (displayName.equals(i18nName)) {
                                throw GroupExceptionCodes.RESERVED_DISPLAY_NAME.create(displayName);
                            }
                        }
                    }
                } else {
                    for (String i18nName : GroupI18nNamesService.getInstance().getI18nNames()) {
                        if (displayName.equals(i18nName)) {
                            throw GroupExceptionCodes.RESERVED_DISPLAY_NAME.create(displayName);
                        }
                    }
                }

            }

            Group[] others = storage.searchGroups(displayName, true, ctx);
            for (Group other : others) {
                if (displayName.equals(other.getDisplayName()) && group.getIdentifier() != other.getIdentifier()) {
                    throw GroupExceptionCodes.DUPLICATE.create(Integer.valueOf(other.getIdentifier()));
                }
            }
        }
    }

    /**
     * This method checks if all members of the group exist in the database.
     * @param ctx Context.
     * @param group Group.
     * @throws OXException if a member identifier is not found in the
     * database.
     */
    final static void doMembersExist(final Context ctx, final Group group) throws OXException {
        if (!group.isMemberSet()) {
            return;
        }
        final UserStorage storage = UserStorage.getInstance();
        final TIntSet set = new TIntHashSet();
        for (final int userId : storage.listAllUser(ctx)) {
            set.add(userId);
        }
        for (final int userId : group.getMember()) {
            if (!set.contains(userId)) {
                if (UserStorage.getInstance().isGuest(userId, ctx)) {
                    throw GroupExceptionCodes.NO_GUEST_USER_IN_GROUP.create(userId);
                }
                throw GroupExceptionCodes.NOT_EXISTING_MEMBER.create(Integer
                    .valueOf(userId));
            }
        }
    }
}
