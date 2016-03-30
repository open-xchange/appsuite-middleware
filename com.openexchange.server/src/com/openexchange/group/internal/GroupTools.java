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

import java.util.Date;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.group.GroupStorage;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.i18n.Groups;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.i18n.LocaleTools;
import com.openexchange.i18n.tools.StringHelper;

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
        Group group = new Group();
        group.setIdentifier(GroupStorage.GROUP_ZERO_IDENTIFIER);
        group.setMember(UserStorage.getInstance().listAllUser(null, context, false, false));
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
        Group group = new Group();
        group.setIdentifier(GroupStorage.GUEST_GROUP_IDENTIFIER);
        group.setMember(UserStorage.getInstance().listAllUser(null, context, true, true));
        group.setLastModified(new Date());
        User admin = UserStorage.getInstance().getUser(context.getMailadmin(), context);
        group.setDisplayName(StringHelper.valueOf(LocaleTools.getLocale(admin.getPreferredLanguage())).getString(Groups.GUEST_GROUP));
        return group;
    }

}
