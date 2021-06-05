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

package com.openexchange.admin.console.util.server;

import java.rmi.Naming;
import java.util.ArrayList;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Server;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;

/**
 *
 * @author d7
 *
 */
public class ListServer extends ServerAbstraction {

    public void execute(final String[] args2) {
        final AdminParser parser = new AdminParser("listserver");

        setOptions(parser);
        setCSVOutputOption(parser);

        try {
            parser.ownparse(args2);

            final Credentials auth = credentialsparsing(parser);

            // get rmi ref
            final OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(RMI_HOSTNAME + OXUtilInterface.RMI_NAME);

            String searchpattern = "*";
            if (parser.getOptionValue(this.searchOption) != null) {
                searchpattern = (String) parser.getOptionValue(this.searchOption);
            }
            // Setting the options in the dataobject
            final Server[] servers = oxutil.listServer(searchpattern, auth);

            if (null != parser.getOptionValue(this.csvOutputOption)) {
                precsvinfos(servers);
            } else {
                sysoutOutput(servers);
            }

            sysexit(0);
        } catch (Exception e) {
            printErrors(null, null, e, parser);
        }
    }

    public static void main(final String args[]) {
        new ListServer().execute(args);
    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);
        setSearchOption(parser);
    }

    private void sysoutOutput(Server[] servers) throws InvalidDataException {
        final ArrayList<ArrayList<String>> data = new ArrayList<>();
        for (final Server server : servers) {
            data.add(makeCSVData(server));
        }

        //doOutput(new String[] { "3r", "35l" }, new String[] { "Id", "Name" }, data);
        doOutput(new String[] { "r", "l" }, new String[] { "Id", "Name" }, data);
    }

    private void precsvinfos(Server[] servers) throws InvalidDataException {
        // needed for csv output, KEEP AN EYE ON ORDER!!!
        final ArrayList<String> columns = new ArrayList<>();
        columns.add("id");
        columns.add("name");

        final ArrayList<ArrayList<String>> data = new ArrayList<>();

        for (final Server server : servers) {
            data.add(makeCSVData(server));
        }

        doCSVOutput(columns, data);
    }

    private ArrayList<String> makeCSVData(Server server) {
        final ArrayList<String> srv_data = new ArrayList<>();
        srv_data.add(String.valueOf(server.getId()));
        final String servername = server.getName();
        if (servername != null) {
            srv_data.add(servername);
        } else {
            srv_data.add(null);
        }
        return srv_data;
    }

    @Override
    protected final String getObjectName() {
        return "servers";
    }

}
