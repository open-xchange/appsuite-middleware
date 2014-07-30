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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.group.GroupStorage;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.i18n.Groups;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserExceptionCode;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.i18n.LocaleTools;
import com.openexchange.i18n.tools.StringHelper;

/**
 * Tool methods for groups.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class GroupTools {

    /**
     * Cloneable object for group 0.
     */
    static final Group GROUP_ZERO;

    static final Group GUEST_GROUP;

    /**
     * Prevent instantiation
     */
    private GroupTools() {
        super();
    }

    public static Group getGroupZero(final Context ctx) throws OXException {
        final Group retval;
        try {
            retval = (Group) GROUP_ZERO.clone();
        } catch (final CloneNotSupportedException e) {
            throw UserExceptionCode.NOT_CLONEABLE.create(e, Group.class.getName());
        }
        final UserStorage ustor = UserStorage.getInstance();
        retval.setMember(ustor.listAllUser(ctx));
        retval.setLastModified(new Date());
        final User admin = ustor.getUser(ctx.getMailadmin(), ctx);
        final StringHelper helper = StringHelper.valueOf(LocaleTools.getLocale(admin.getPreferredLanguage()));
        retval.setDisplayName(helper.getString(Groups.ALL_USERS));
        return retval;
    }

    public static Group getGuestGroup(final Context ctx) throws OXException {
        final Group retval;
        try {
            retval = (Group) GUEST_GROUP.clone();
        } catch (final CloneNotSupportedException e) {
            throw UserExceptionCode.NOT_CLONEABLE.create(e, Group.class.getName());
        }

        final UserStorage ustor = UserStorage.getInstance();
        // FIXME
        User[] users = ustor.getUser(ctx);
        List<Integer> guestIds = new ArrayList<Integer>(users.length);
        for (User user : users) {
            if (user.isGuest()) {
                guestIds.add(user.getId());
            }
        }

        int[] tmp = new int[guestIds.size()];
        for (int i = 0; i < tmp.length; i++) {
            tmp[i] = guestIds.get(i);
        }
        retval.setMember(tmp);
        retval.setLastModified(new Date());
        final User admin = ustor.getUser(ctx.getMailadmin(), ctx);
        final StringHelper helper = StringHelper.valueOf(LocaleTools.getLocale(admin.getPreferredLanguage()));
        retval.setDisplayName(helper.getString(Groups.GUEST_GROUP));
        return retval;
    }

    static {
        GROUP_ZERO = new Group();
        GROUP_ZERO.setIdentifier(GroupStorage.GROUP_ZERO_IDENTIFIER);
        GUEST_GROUP = new Group();
        GUEST_GROUP.setIdentifier(GroupStorage.GUEST_GROUP_IDENTIFIER);
    }
}
