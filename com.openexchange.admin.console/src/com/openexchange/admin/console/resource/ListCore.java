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
package com.openexchange.admin.console.resource;

import java.rmi.RemoteException;
import java.util.ArrayList;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.rmi.OXResourceInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Resource;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.DuplicateExtensionException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchResourceException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public abstract class ListCore extends ResourceAbstraction {

    protected final void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptions(parser);
        // we need csv output , so we add this option
        setCSVOutputOption(parser);
        this.searchOption = setShortLongOpt(parser, OPT_NAME_SEARCHPATTERN, OPT_NAME_SEARCHPATTERN_LONG, "The search pattern which is used for listing. This applies to name.", true, NeededQuadState.notneeded);
    }

    protected final void commonfunctions(final AdminParser parser, final String[] args) {
        setOptions(parser);

        try {
            parser.ownparse(args);

            final Context ctx = contextparsing(parser);

            final Credentials auth = credentialsparsing(parser);

            String pattern = (String) parser.getOptionValue(this.searchOption);

            if (null == pattern) {
                pattern = "*";
            }

            final OXResourceInterface oxres = getResourceInterface();

            final Resource[] allres = oxres.list(ctx, pattern, auth);

            final ArrayList<Resource> resourceList = new ArrayList<Resource>();
            maincall(parser, oxres, ctx, resourceList, allres, auth);
            if (parser.getOptionValue(this.csvOutputOption) != null) {
                // do CSV output
                precsvinfos(resourceList);
            } else {
                sysoutOutput(resourceList);
            }

            sysexit(0);
        } catch (Exception e) {
            printErrors(null, ctxid, e, parser);
        }
    }

    protected final void sysoutOutput(final ArrayList<Resource> resources) throws InvalidDataException {
        final ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
        for (final Resource resource : resources) {
            printExtensionsError(resource);
            data.add(makeStandardData(resource));
        }

        //doOutput(new String[] { "3r", "30l", "20l", "14l", "9l" },
        doOutput(new String[] { "r", "l", "l", "l", "l" },
                 new String[] { "Id", "Name", "Displayname", "Email", "Available" }, data);
    }


    protected abstract void maincall(final AdminParser parser, final OXResourceInterface oxres, final Context ctx, final ArrayList<Resource> reslist, final Resource[] allres, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchResourceException, DuplicateExtensionException;

    private void precsvinfos(final ArrayList<Resource> resourceList) throws InvalidDataException {
        // needed for csv output, KEEP AN EYE ON ORDER!!!
        final ArrayList<String> columns = new ArrayList<String>();
        columns.add("id");
        columns.add("name");
        columns.add("displayname");
        columns.add("email");
        columns.add("available");
        columns.add("description");
        extendscvscolumns(columns);

        // Needed for csv output
        final ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();

        for (final Resource my_res : resourceList) {
            final ArrayList<String> makeCsvData = makeCsvData(my_res);
            makeCsvData.add(my_res.getDescription());
            data.add(makeCsvData);
            printExtensionsError(my_res);
        }
        doCSVOutput(columns, data);
    }

    protected abstract void extendscvscolumns(final ArrayList<String> columns);

    private ArrayList<String> makeCsvData(final Resource my_res) {
        final ArrayList<String> res_data = makeStandardData(my_res);

        extendmakeCSVData(my_res, res_data);
        return res_data;
    }

    private ArrayList<String> makeStandardData(final Resource my_res) {
        final ArrayList<String> res_data = new ArrayList<String>();

        res_data.add(String.valueOf(my_res.getId())); // id

        final String name = my_res.getName();
        if (name != null && name.trim().length() > 0) {
            res_data.add(name); // name
        } else {
            res_data.add(null); // name
        }

        final String displayname = my_res.getDisplayname();
        if (displayname != null && displayname.trim().length() > 0) {
            res_data.add(displayname); // displayname
        } else {
            res_data.add(null); // displayname
        }

        final String email = my_res.getEmail();
        if (email != null && email.trim().length() > 0) {
            res_data.add(email); // email
        } else {
            res_data.add(null); // email
        }

        final Boolean available = my_res.getAvailable();
        if (available != null) {
            res_data.add(available.toString()); // available
        } else {
            res_data.add(null);
        }

        return res_data;
    }

    protected abstract void extendmakeCSVData(final Resource my_res, final ArrayList<String> res_data);

    @Override
    protected final String getObjectName() {
        return "resources";
    }
}
