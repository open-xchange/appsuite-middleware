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

import java.rmi.RemoteException;
import java.util.ArrayList;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.rmi.OXGroupInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Group;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.DuplicateExtensionException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchGroupException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public abstract class ListCore extends GroupAbstraction {

    protected final void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptions(parser);

        // we need csv output , so we add this option
        setCSVOutputOption(parser);
        // create options for this command line tool
        this.searchOption = setShortLongOpt(parser, OPT_NAME_SEARCHPATTERN, OPT_NAME_SEARCHPATTERN_LONG, "The search pattern which is used for listing. This applies to name.", true, NeededQuadState.notneeded);
    }

    protected final void commonfunctions(final AdminParser parser, final String[] args) {
        setOptions(parser);

        try {
            parser.ownparse(args);
            final Context ctx = contextparsing(parser);

            final Credentials auth = credentialsparsing(parser);

            final OXGroupInterface oxgrp = getGroupInterface();

            String pattern = (String) parser.getOptionValue(this.searchOption);

            if (null == pattern) {
                pattern = "*";
            }

            final Group[] allgrps = oxgrp.list(ctx, pattern, auth);

            final ArrayList<Group> grplist = new ArrayList<>();

            maincall(parser, oxgrp, ctx, grplist, allgrps, auth);

            if (parser.getOptionValue(this.csvOutputOption) != null) {
                // DO csv output if needed
                precsvinfos(grplist);
            } else {
                sysoutOutput(grplist);
            }

            sysexit(0);
        } catch (Exception e) {
            printErrors(null, ctxid, e, parser);
            sysexit(1);
        }
    }

    protected abstract void maincall(final AdminParser parser, final OXGroupInterface oxgrp, final Context ctx, final ArrayList<Group> grplist, final Group[] allgrps, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchGroupException, DuplicateExtensionException;

    private void sysoutOutput(final ArrayList<Group> grouplist) throws InvalidDataException {
        final ArrayList<ArrayList<String>> data = new ArrayList<>();
        for (final Group group : grouplist) {
            printExtensionsError(group);
            data.add(makeStandardData(group));
        }

        //        doOutput(new String[] { "3r", "30l", "30l", "14l" },
        doOutput(new String[] { "r", "l", "l", "l" }, new String[] { "Id", "Name", "Displayname", "Members" }, data);
    }

    private void precsvinfos(final ArrayList<Group> grplist) throws InvalidDataException {
        // needed for csv output, KEEP AN EYE ON ORDER!!!
        final ArrayList<String> columns = new ArrayList<>();
        columns.add("id");
        columns.add("name");
        columns.add("displayname");
        columns.add("members");
        extendscvscolumns(columns);

        // Needed for csv output
        final ArrayList<ArrayList<String>> data = new ArrayList<>();

        for (final Group my_grp : grplist) {
            data.add(makeDataForCsv(my_grp));
            printExtensionsError(my_grp);
        }
        doCSVOutput(columns, data);
    }

    protected abstract void extendscvscolumns(final ArrayList<String> columns);

    /**
     * Generate data which can be processed by the csv output method.
     *
     * @param group
     * @param members
     * @return
     */
    private ArrayList<String> makeDataForCsv(final Group group) {
        final ArrayList<String> grp_data = makeStandardData(group);

        extendmakeCSVData(group, grp_data);
        return grp_data;
    }

    private ArrayList<String> makeStandardData(final Group group) {
        final ArrayList<String> grp_data = new ArrayList<>();

        grp_data.add(String.valueOf(group.getId())); // id

        final String name = group.getName();
        if (name != null && name.trim().length() > 0) {
            grp_data.add(name);
        } else {
            grp_data.add(null); // name
        }
        final String displayname = group.getDisplayname();
        if (displayname != null && displayname.trim().length() > 0) {
            grp_data.add(displayname);
        } else {
            grp_data.add(null); // displayname
        }
        grp_data.add(getObjectsAsString(group.getMembers())); // members
        return grp_data;
    }

    protected abstract void extendmakeCSVData(Group group, ArrayList<String> grp_data);

    @Override
    protected final String getObjectName() {
        return "groups";
    }
}
