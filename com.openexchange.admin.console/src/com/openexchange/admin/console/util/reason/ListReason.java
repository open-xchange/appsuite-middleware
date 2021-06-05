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

package com.openexchange.admin.console.util.reason;

import java.rmi.Naming;
import java.util.ArrayList;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;

/**
 *
 * @author d7,cutmasta
 *
 */
public class ListReason extends ReasonAbstraction {

    public void execute(final String[] args2) {
        final AdminParser parser = new AdminParser("listreason");
        setOptions(parser);
        setCSVOutputOption(parser);
        try {
            parser.ownparse(args2);

            final Credentials auth = credentialsparsing(parser);

            // get rmi ref
            final OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(RMI_HOSTNAME + OXUtilInterface.RMI_NAME);

            String pattern = "*";
            if (parser.getOptionValue(this.searchOption) != null) {
                pattern = (String) parser.getOptionValue(this.searchOption);
            }

            final MaintenanceReason[] mrs = oxutil.listMaintenanceReason(pattern, auth);

            if (null != parser.getOptionValue(this.csvOutputOption)) {
                precsvinfos(mrs);
            } else {
                sysoutOutput(mrs);
            }

            sysexit(0);
        } catch (Exception e) {
            printErrors(null, null, e, parser);
        }
    }

    public static void main(final String args[]) {
        new ListReason().execute(args);
    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);
        setSearchOption(parser);
    }

    private void sysoutOutput(final MaintenanceReason[] mrs) throws InvalidDataException {
        final ArrayList<ArrayList<String>> data = new ArrayList<>();
        for (final MaintenanceReason mr : mrs) {
            data.add(makeCSVData(mr));
        }

        //doOutput(new String[] { "3r", "72l" }, new String[] { "Id", "Text" }, data);
        doOutput(new String[] { "r", "l" }, new String[] { "Id", "Text" }, data);
    }

    private void precsvinfos(final MaintenanceReason[] mrs) throws InvalidDataException {
        // needed for csv output, KEEP AN EYE ON ORDER!!!
        final ArrayList<String> columns = new ArrayList<>();
        columns.add("id");
        columns.add("text");

        // Needed for csv output
        final ArrayList<ArrayList<String>> data = new ArrayList<>();

        for (final MaintenanceReason mr : mrs) {
            data.add(makeCSVData(mr));
        }

        doCSVOutput(columns, data);
    }

    private ArrayList<String> makeCSVData(final MaintenanceReason mr) {
        final ArrayList<String> rea_data = new ArrayList<>();
        rea_data.add(mr.getId().toString());
        rea_data.add(mr.getText());

        return rea_data;
    }

    @Override
    protected final String getObjectName() {
        return "reasons";
    }
}
