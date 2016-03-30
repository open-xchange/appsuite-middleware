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
package com.openexchange.admin.console.group;

import java.rmi.RemoteException;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.rmi.OXGroupInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Group;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.DuplicateExtensionException;

public abstract class ChangeCore extends GroupAbstraction {

    protected final void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptions(parser);

        setGroupIdOption(parser, NeededQuadState.eitheror);
        setGroupNameOption(parser, NeededQuadState.eitheror);

        // create options for this command line tool
        setGroupDisplayNameOption(parser, false);
        setAddMembersOption(parser, false);
        setRemoveMembersOption(parser, false);

        setFurtherOptions(parser);
    }

    protected abstract void setFurtherOptions(final AdminParser parser);

    protected final void commonfunctions(final AdminParser parser, final String[] args) {
        setOptions(parser);

        String successtext = null;
        try {
            parser.ownparse(args);

            final Group grp = new Group();

            parseAndSetGroupId(parser, grp);
            parseAndSetGroupName(parser, grp);

            successtext = nameOrIdSet(this.groupid, this.groupName, "group");

            final Context ctx = contextparsing(parser);

            final Credentials auth = credentialsparsing(parser);

            final OXGroupInterface oxgrp = getGroupInterface();

            final String addMembers = (String) parser.getOptionValue(this.addMemberOption);
            if (addMembers != null) {
                final User[] newMemberList = getMembers(parser, addMembers);
                if (newMemberList != null) {
                    oxgrp.addMember(ctx, grp, newMemberList, auth);
                }
            }

            final String removeMembers = (String) parser.getOptionValue(this.removeMemberOption);
            if (removeMembers != null) {
                final User[] removeMemberList = getMembers(parser, removeMembers);
                if (removeMemberList != null) {
                    oxgrp.removeMember(ctx, grp, removeMemberList, auth);
                }
            }

            parseAndSetGroupAndDisplayName(parser, grp);

            maincall(parser, oxgrp, ctx, grp, auth);

            oxgrp.change(ctx, grp, auth);

            displayChangedMessage(successtext, ctxid, parser);
            sysexit(0);
        } catch (final Exception e) {
            printErrors(successtext, ctxid, e, parser);
        }
    }

    private User[] getMembers(final AdminParser parser, final String tmpmembers) {
        final String[] split = tmpmembers.split(",");
        final User[] memberList = new User[split.length];
        for (int i = 0; i < split.length; i++) {
            memberList[i] = new User(Integer.parseInt(split[i]));
        }
        return memberList;
    }

    protected abstract void maincall(final AdminParser parser, final OXGroupInterface oxgrp, final Context ctx, final Group grp, final Credentials auth) throws RemoteException, DuplicateExtensionException;
}
