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

package com.openexchange.admin.console.group;

import static com.openexchange.java.Autoboxing.I;
import java.rmi.RemoteException;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.rmi.OXGroupInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Group;
import com.openexchange.admin.rmi.exceptions.DuplicateExtensionException;

public abstract class CreateCore extends GroupAbstraction {

    protected final void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptions(parser);

        // create options for this command line tool
        setGroupNameOption(parser, NeededQuadState.needed);
        setGroupDisplayNameOption(parser, true);
        setAddMembersOption(parser, false);

        setFurtherOptions(parser);
    }

    protected abstract void setFurtherOptions(final AdminParser parser);

    protected final void commonfunctions(final AdminParser parser, final String[] args) {
        setOptions(parser);

        try {
            parser.ownparse(args);

            final Context ctx = contextparsing(parser);

            final Credentials auth = credentialsparsing(parser);

            final OXGroupInterface oxgrp = getGroupInterface();

            final Group grp = new Group();

            final String members = (String) parser.getOptionValue(this.addMemberOption);
            if (members != null) {
                final Integer[] newMemberList = getMembers(members);
                if (newMemberList != null) {
                    grp.setMembers(newMemberList);
                }
            }

            parseAndSetGroupAndDisplayName(parser, grp);

            maincall(parser, oxgrp, ctx, grp, auth);

            final Integer id = oxgrp.create(ctx, grp, auth).getId();

            displayCreatedMessage(String.valueOf(id), ctx.getId(), parser);
            sysexit(0);
        } catch (Exception e) {
            printErrors(null, ctxid, e, parser);
        }
    }

    private Integer[] getMembers(final String tmpmembers) {
        final String[] split = tmpmembers.split(",");
        final Integer[] memberList = new Integer[split.length];
        for (int i = 0; i < split.length; i++) {
            memberList[i] = I(Integer.parseInt(split[i]));
        }
        return memberList;
    }

    protected abstract void maincall(final AdminParser parser, final OXGroupInterface oxgrp, final Context ctx, final Group grp, final Credentials auth) throws RemoteException, DuplicateExtensionException;
}
