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

            final ArrayList<Group> grplist = new ArrayList<Group>();

            maincall(parser, oxgrp, ctx, grplist, allgrps, auth);

            if (parser.getOptionValue(this.csvOutputOption) != null) {
                // DO csv output if needed
                precsvinfos(grplist);
            } else {
                sysoutOutput(grplist);
            }

            sysexit(0);
        } catch (final Exception e) {
            printErrors(null, ctxid, e, parser);
            sysexit(1);
        }
    }

    protected abstract void maincall(final AdminParser parser, final OXGroupInterface oxgrp, final Context ctx, final ArrayList<Group> grplist, final Group[] allgrps, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchGroupException, DuplicateExtensionException;

    private void sysoutOutput(final ArrayList<Group> grouplist) throws InvalidDataException {
        final ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
        for (final Group group : grouplist) {
            printExtensionsError(group);
            data.add(makeStandardData(group));
        }

//        doOutput(new String[] { "3r", "30l", "30l", "14l" },
        doOutput(new String[] { "r", "l", "l", "l" },
                 new String[] { "Id", "Name", "Displayname", "Members" }, data);
    }

    private void precsvinfos(final ArrayList<Group> grplist) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        // needed for csv output, KEEP AN EYE ON ORDER!!!
        final ArrayList<String> columns = new ArrayList<String>();
        columns.add("id");
        columns.add("name");
        columns.add("displayname");
        columns.add("members");
        extendscvscolumns(columns);

        // Needed for csv output
        final ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();

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
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws InvalidDataException
     */
    private ArrayList<String> makeDataForCsv(final Group group) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        final ArrayList<String> grp_data = makeStandardData(group);

        extendmakeCSVData(group, grp_data);
        return grp_data;
    }

    private ArrayList<String> makeStandardData(final Group group) {
        final ArrayList<String> grp_data = new ArrayList<String>();

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
