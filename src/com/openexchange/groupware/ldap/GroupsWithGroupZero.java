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

package com.openexchange.groupware.ldap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.openexchange.groupware.contexts.Context;

/**
 * Implementation of the group storage that adds group with identifier 0 to all
 * requests.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class GroupsWithGroupZero extends GroupStorage {

    /**
     * Context.
     */
    private final Context ctx;

    /**
     * Underlying group storage handling groups except group with identifier 0.
     */
    private final GroupStorage delegate;

    /**
     * Default constructor.
     * @param ctx Context.
     * @param delegate underlying group storage.
     */
    GroupsWithGroupZero(final Context ctx, final GroupStorage delegate) {
        super();
        this.ctx = ctx;
        this.delegate = delegate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Group getGroup(final int gid) throws LdapException {
        final Group retval;
        if (GroupTools.GROUP_ZERO.getIdentifier() == gid) {
            try {
                retval = GroupTools.getGroupZero(ctx);
            } catch (UserException e) {
                throw new LdapException(e);
            }
        } else {
            retval = delegate.getGroup(gid);
        }
        return retval;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Group[] getGroups() throws LdapException {
        final Group[] groups = delegate.getGroups();
        final Group[] retval = new Group[groups.length + 1];
        try {
            retval[0] = GroupTools.getGroupZero(ctx);
        } catch (UserException e) {
            throw new LdapException(e);
        }
        System.arraycopy(groups, 0, retval, 1, groups.length);
        return retval;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Group[] listModifiedGroups(final Date modifiedSince)
        throws LdapException {
        final Group[] groups = delegate.listModifiedGroups(modifiedSince);
        final Group[] retval = new Group[groups.length + 1];
        try {
            retval[0] = GroupTools.getGroupZero(ctx);
        } catch (UserException e) {
            throw new LdapException(e);
        }
        System.arraycopy(groups, 0, retval, 1, groups.length);
        return retval;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Group[] searchGroups(final String pattern) throws LdapException {
        final Pattern pat = Pattern.compile(pattern.replace("*", ".*"), Pattern
            .CASE_INSENSITIVE);
        final Group zero;
        try {
            zero = GroupTools.getGroupZero(ctx);
        } catch (UserException e) {
            throw new LdapException(e);
        }
        final Matcher match = pat.matcher(zero.getDisplayName());
        final List<Group> groups = new ArrayList<Group>();
        groups.addAll(Arrays.asList(delegate.searchGroups(pattern)));
        if (match.find()) {
            groups.add(zero);
        }
        return groups.toArray(new Group[groups.size()]);
    }

}
