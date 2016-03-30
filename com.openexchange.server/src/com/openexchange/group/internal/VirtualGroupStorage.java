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

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.group.GroupStorage;
import com.openexchange.groupware.contexts.Context;

/**
 * Implementation of the group storage that adds the virtual groups {@link GroupStorage#GROUP_ZERO_IDENTIFIER}
 * and {@link GroupStorage#GUEST_GROUP_IDENTIFIER} to all requests.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class VirtualGroupStorage extends GroupStorage {

    /**
     * Underlying group storage handling groups except group with identifier 0.
     */
    private final GroupStorage delegate;

    /**
     * Default constructor.
     *
     * @param ctx Context.
     * @param delegate underlying group storage.
     */
    public VirtualGroupStorage(final GroupStorage delegate) {
        super();
        this.delegate = delegate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Group getGroup(final int gid, final Context ctx) throws OXException {
        final Group retval;
        if (GroupStorage.GROUP_ZERO_IDENTIFIER == gid) {
            retval = GroupTools.getGroupZero(ctx);
        } else if (GroupStorage.GUEST_GROUP_IDENTIFIER == gid) {
            retval = GroupTools.getGuestGroup(ctx);
        } else {
            retval = delegate.getGroup(gid, ctx);
        }
        return retval;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Group[] getGroups(final boolean loadMembers, final Context ctx) throws OXException {
        final Group[] groups = delegate.getGroups(loadMembers, ctx);
        final Group[] retval = new Group[groups.length + 2];
        retval[0] = GroupTools.getGroupZero(ctx);
        retval[1] = GroupTools.getGuestGroup(ctx);
        System.arraycopy(groups, 0, retval, 2, groups.length);
        return retval;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Group[] listModifiedGroups(final Date modifiedSince, final Context ctx) throws OXException {
        final Group[] groups = delegate.listModifiedGroups(modifiedSince, ctx);
        final Group[] retval = new Group[groups.length + 2];
        retval[0] = GroupTools.getGroupZero(ctx);
        retval[1] = GroupTools.getGuestGroup(ctx);
        System.arraycopy(groups, 0, retval, 2, groups.length);
        return retval;
    }

    @Override
    public Group[] listDeletedGroups(final Date modifiedSince, final Context ctx) throws OXException {
        final Group[] groups = delegate.listDeletedGroups(modifiedSince, ctx);
        final Group[] retval = new Group[groups.length + 2];
        retval[0] = GroupTools.getGroupZero(ctx);
        retval[1] = GroupTools.getGuestGroup(ctx);
        System.arraycopy(groups, 0, retval, 2, groups.length);
        return retval;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Group[] searchGroups(final String pattern, final boolean loadMembers, final Context ctx) throws OXException {
        final Pattern pat = Pattern.compile(wildcardToRegex(pattern), Pattern.CASE_INSENSITIVE);
        final Group zero = GroupTools.getGroupZero(ctx);
        final Matcher zeroMatch = pat.matcher(zero.getDisplayName());
        final Group guests = GroupTools.getGuestGroup(ctx);
        final Matcher guestsMatch = pat.matcher(guests.getDisplayName());
        final List<Group> groups = new ArrayList<Group>();
        groups.addAll(Arrays.asList(delegate.searchGroups(pattern, loadMembers, ctx)));
        if (zeroMatch.find()) {
            groups.add(zero);
        }
        if (guestsMatch.find()) {
            groups.add(guests);
        }
        return groups.toArray(new Group[groups.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insertGroup(final Context ctx, final Connection con, final Group group, final StorageType type) throws OXException {
        delegate.insertGroup(ctx, con, group, type);
    }

    @Override
    public void deleteMember(final Context ctx, final Connection con, final Group group, final int[] members) throws OXException {
        delegate.deleteMember(ctx, con, group, members);
    }

    @Override
    public void insertMember(final Context ctx, final Connection con, final Group group, final int[] members) throws OXException {
        delegate.insertMember(ctx, con, group, members);
    }

    @Override
    public void updateGroup(final Context ctx, final Connection con, final Group group, final Date lastRead) throws OXException {
        delegate.updateGroup(ctx, con, group, lastRead);
    }

    @Override
    public void deleteGroup(final Context ctx, final Connection con, final int groupId, final Date lastRead) throws OXException {
        delegate.deleteGroup(ctx, con, groupId, lastRead);
    }

    private static final gnu.trove.set.TIntSet SPECIALS = new gnu.trove.set.hash.TIntHashSet(new int[] {
        '+', '(', ')', '[', ']', '$', '^', '.', '{', '}', '|', '\\' });

    /**
     * Converts specified wild-card string to a regular expression
     *
     * @param wildcard The wild-card string to convert
     * @return An appropriate regular expression ready for being used in a {@link Pattern#compile(String) pattern}
     */
    private static String wildcardToRegex(final String wildcard) {
        if (null == wildcard) {
            // Accept all if null
            return "^.*$";
        }
        if (wildcard.indexOf('*') < 0 && wildcard.indexOf('?') < 0) {
            // Literal pattern
            return Pattern.quote(wildcard);
        }
        // Generate appropriate regex
        final StringBuilder s = new StringBuilder(wildcard.length());
        s.append('^');
        final int len = wildcard.length();
        for (int i = 0; i < len; i++) {
            final char c = wildcard.charAt(i);
            if (c == '*') {
                s.append(".*");
            } else if (c == '?') {
                s.append('.');
            } else if (SPECIALS.contains(c)) {
                s.append('\\');
                s.append(c);
            } else {
                s.append(c);
            }
        }
        s.append('$');
        return (s.toString());
    }

}
