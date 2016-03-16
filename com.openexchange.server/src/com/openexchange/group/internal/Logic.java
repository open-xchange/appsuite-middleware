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

package com.openexchange.group.internal;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import java.util.Date;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.group.GroupExceptionCodes;
import com.openexchange.group.GroupStorage;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.data.Check;
import com.openexchange.groupware.ldap.UserStorage;

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

    private static final String ALLOWED_CHARS = "[$@%\\.+a-zA-Z0-9_-]";

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
        final String illegal = groupName.replaceAll(ALLOWED_CHARS, "");
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
