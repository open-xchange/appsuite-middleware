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

package com.openexchange.admin.console.util.filestore;

import java.rmi.Naming;
import java.util.ArrayList;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.CLIOption;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;

/**
 * CLT for listing file stores.
 *
 * @author d7,cutmasta
 */
public class ListFilestore extends FilestoreAbstraction {

    private CLIOption omitUsageOption = null;

    public ListFilestore() {
        super();
    }

    protected void execute(String[] args) {
        final AdminParser parser = new AdminParser("listfilestore");
        setOptions(parser);
        setCSVOutputOption(parser);
        try {
            parser.ownparse(args);
            final Credentials auth = credentialsparsing(parser);
            String searchpattern = "*";
            if (parser.getOptionValue(this.searchOption) != null) {
                searchpattern = (String) parser.getOptionValue(this.searchOption);
            }
            boolean omitUsage;
            if (null == parser.getOptionValue(omitUsageOption)) {
                omitUsage = false;
            } else {
                omitUsage = ((Boolean) parser.getOptionValue(omitUsageOption)).booleanValue();
            }
            final OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(RMI_HOSTNAME + OXUtilInterface.RMI_NAME);
            final Filestore[] filestores = oxutil.listFilestore(searchpattern, auth, omitUsage);
            if (null != parser.getOptionValue(this.csvOutputOption)) {
                precsvinfos(filestores);
            } else {
                sysoutOutput(filestores);
            }
            sysexit(0);
        } catch (Exception e) {
            printErrors(null, ctxid, e, parser);
        }
    }

    private void sysoutOutput(final Filestore[] filestores) throws InvalidDataException {
        final ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
        for (final Filestore filestore : filestores) {
            data.add(makeCSVData(filestore));
        }
        //doOutput(new String[] { "3r", "35l", "7r", "8r", "7r", "7r", "7r" },
        doOutput(new String[] { "r", "l", "r", "r", "r", "r", "r" },
                 new String[] { "id", "path", "size", "reserved", "used", "max-entities", "cur-entities" }, data);
    }

    private void precsvinfos(final Filestore[] filestores) throws InvalidDataException {
        // needed for csv output, KEEP AN EYE ON ORDER!!!
        final ArrayList<String> columns = new ArrayList<String>();
        columns.add("id");
        columns.add("uri");
        columns.add("size");
        columns.add("reserved");
        columns.add("used");
        columns.add("maxentities");
        columns.add("currententities");
        // Needed for csv output
        final ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
        for (final Filestore filestore : filestores) {
            data.add(makeCSVData(filestore));
        }
        doCSVOutput(columns, data);
    }

    public static void main(String[] args) {
        new ListFilestore().execute(args);
    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);
        setSearchOption(parser);
        omitUsageOption = setShortLongOpt(parser, 'u', "omitUsage", "Do not load the usage of the file stores, which is expensive.", false, NeededQuadState.notneeded);
    }

    private static ArrayList<String> makeCSVData(final Filestore fstore) {
        final ArrayList<String> rea_data = new ArrayList<String>();
        rea_data.add(fstore.getId().toString());
        rea_data.add(fstore.getUrl());
        if (fstore.getSize() != null) {
            rea_data.add(fstore.getSize().toString());
        } else {
            rea_data.add(null);
        }
        if (fstore.getReserved() != null) {
            rea_data.add(fstore.getReserved().toString());
        } else {
            rea_data.add(null);
        }
        if (fstore.getUsed() != null) {
            rea_data.add(fstore.getUsed().toString());
        } else {
            rea_data.add(null);
        }
        if (fstore.getMaxContexts() != null) {
            rea_data.add(fstore.getMaxContexts().toString());
        } else {
            rea_data.add(null);
        }
        if (fstore.getCurrentContexts() != null) {
            rea_data.add(fstore.getCurrentContexts().toString());
        } else {
            rea_data.add(null);
        }
        return rea_data;
    }

    @Override
    protected final String getObjectName() {
        return "filestores";
    }
}
