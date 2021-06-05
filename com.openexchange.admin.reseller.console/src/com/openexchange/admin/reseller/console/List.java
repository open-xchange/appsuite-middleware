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

package com.openexchange.admin.reseller.console;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.reseller.rmi.OXResellerInterface;
import com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin;
import com.openexchange.admin.reseller.rmi.dataobjects.Restriction;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;

/**
 * @author choeger
 */
public class List extends ResellerAbstraction {

    protected final void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);
        setCSVOutputOption(parser);
    }

    /**
     *
     */
    public List() {
    }

    /**
     * @param args
     */
    public static void main(final String[] args) {
        final List list = new List();
        list.start(args);
    }

    public void start(final String[] args) {
        final AdminParser parser = new AdminParser("listadmin");

        setOptions(parser);

        ResellerAdmin[] adms = null;
        // parse the command line
        try {
            parser.ownparse(args);

            final Credentials auth = credentialsparsing(parser);

            final OXResellerInterface rsi = getResellerInterface();

            adms = rsi.list("*", auth);
        } catch (Exception e) {
            printErrors(null, null, e, parser);
            sysexit(1);
        }
        try {
            if (null != parser.getOptionValue(this.csvOutputOption)) {
                // map user data to corresponding module access
                precsvinfos(Arrays.asList(adms));
            } else {
                sysoutOutput(Arrays.asList(adms));
            }

            sysexit(0);
        } catch (InvalidDataException e) {
            printError(null, null, "Invalid data : " + e.getMessage(), parser);
            sysexit(1);
        } catch (RuntimeException e) {
            printError(null, null, e.getClass().getSimpleName() + ": " + e.getMessage(), parser);
            sysexit(1);
        }
    }

    private void sysoutOutput(final java.util.List<ResellerAdmin> admns) throws InvalidDataException {
        final ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
        for (final ResellerAdmin admin : admns) {
            data.add(makeStandardData(admin));
        }

        // doOutput(new String[] { "3r", "30l", "30l", "14l" },
        doOutput(new String[] { "r", "l", "l", "l", "l" }, new String[] { "Id", "Name", "Displayname", "Parent", "Restrictions" }, data);
    }

    private void precsvinfos(final java.util.List<ResellerAdmin> adminlist) throws InvalidDataException {
        // needed for csv output, KEEP AN EYE ON ORDER!!!
        final ArrayList<String> columns = new ArrayList<String>();
        columns.add("id");
        columns.add("name");
        columns.add("displayname");
        columns.add("parentid");
        columns.add("restrictions");

        // Needed for csv output
        final ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();

        for (final ResellerAdmin admin : adminlist) {
            data.add(makeDataForCsv(admin));
        }
        doCSVOutput(columns, data);
    }

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
    private ArrayList<String> makeDataForCsv(final ResellerAdmin admin) {
        final ArrayList<String> admin_data = makeStandardData(admin);

        return admin_data;
    }

    private ArrayList<String> makeStandardData(final ResellerAdmin admin) {
        final ArrayList<String> admin_data = new ArrayList<String>();

        admin_data.add(String.valueOf(admin.getId())); // id

        final String name = admin.getName();
        if (name != null && name.trim().length() > 0) {
            admin_data.add(name);
        } else {
            admin_data.add(null); // name
        }
        final String displayname = admin.getDisplayname();
        if (displayname != null && displayname.trim().length() > 0) {
            admin_data.add(displayname);
        } else {
            admin_data.add(null); // displayname
        }
        admin_data.add(admin.getParentId().toString());
        final Restriction[] restrictions = admin.getRestrictions();
        if (null != restrictions) {
            admin_data.add(ResellerAbstraction.getObjectsAsString(restrictions)); // restrictions
        } else {
            admin_data.add("");
        }
        return admin_data;
    }

}
