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
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.group.GroupStorage;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.i18n.Groups;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.i18n.LocaleTools;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.user.User;

/**
 * Tool methods for groups.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class GroupTools {

    /**
     * Prevent instantiation
     */
    private GroupTools() {
        super();
    }
    /**
     * Gets the static "All users" group (group <code>0</code>), containing all regular users in the context.
     *
     * @param context The context to get the all users group for
     * @return The all users group
     */
    public static Group getGroupZero(Context context) throws OXException {
        return getGroupZero(context, true);
    }

    /**
     * Gets the static "All users" group (group <code>0</code>), containing all regular users in the context.
     *
     * @param context The context to get the all users group for
     * @param loadMembers <code>true</code> to load and set the group members, <code>false</code>, otherwise
     * @return The all users group
     */
    public static Group getGroupZero(Context context, boolean loadMembers) throws OXException {
        Group group = new Group();
        group.setIdentifier(GroupStorage.GROUP_ZERO_IDENTIFIER);
        if (loadMembers) {
            group.setMember(UserStorage.getInstance().listAllUser(null, context, false, false));
        }
        group.setLastModified(new Date());
        User admin = UserStorage.getInstance().getUser(context.getMailadmin(), context);
        group.setDisplayName(StringHelper.valueOf(LocaleTools.getLocale(admin.getPreferredLanguage())).getString(Groups.ALL_USERS));
        return group;
    }

    /**
     * Gets the static "Guests" group (group {@link Integer#MAX_VALUE}), containing all guest users in the context.
     *
     * @param context The context to get the guest group for
     * @return The guest group
     */
    public static Group getGuestGroup(Context context) throws OXException {
        return getGroupZero(context, true);
    }

    /**
     * Gets the static "Guests" group (group {@link Integer#MAX_VALUE}), containing all guest users in the context.
     *
     * @param context The context to get the guest group for
     * @param loadMembers <code>true</code> to load and set the group members, <code>false</code>, otherwise
     * @return The guest group
     */
    public static Group getGuestGroup(Context context, boolean loadMembers) throws OXException {
        Group group = new Group();
        group.setIdentifier(GroupStorage.GUEST_GROUP_IDENTIFIER);
        if (loadMembers) {
            group.setMember(UserStorage.getInstance().listAllUser(null, context, true, true));
        }
        group.setLastModified(new Date());
        User admin = UserStorage.getInstance().getUser(context.getMailadmin(), context);
        group.setDisplayName(StringHelper.valueOf(LocaleTools.getLocale(admin.getPreferredLanguage())).getString(Groups.GUEST_GROUP));
        return group;
    }

}
