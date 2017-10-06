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
package com.openexchange.admin.console.util.database;

import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.Naming;
import java.util.ArrayList;
import java.util.Map;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;

/**
 *
 * @author d7,cutmasta
 *
 */
public class CountDatabaseSchema extends DatabaseAbstraction {

    public CountDatabaseSchema(final String[] args2) {

        final AdminParser parser = new AdminParser("countdatabaseschema");

        setOptions(parser);

        try {
            parser.ownparse(args2);
            parseAndSetOnlyEmptySchemas(parser);

            final Credentials auth = credentialsparsing(parser);

            // get rmi ref
            final OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(RMI_HOSTNAME +OXUtilInterface.RMI_NAME);

            String searchpattern = "*";
            if (parser.getOptionValue(this.searchOption) != null) {
                searchpattern = (String)parser.getOptionValue(this.searchOption);
            }
            final Map<Database, Integer> databases = oxutil.countDatabaseSchema(searchpattern, onlyEmptySchemas, auth);

            if (null != parser.getOptionValue(this.csvOutputOption)) {
                precsvinfos(databases);
            } else {
                if (null == databases || databases.size() == 0) {
                    System.out.println("No such database schemas found");
                } else {
                    sysoutOutput(databases);
                }
            }

            sysexit(0);
        } catch (final Exception e) {
            printErrors(null, ctxid, e, parser);
        }

    }

    private void sysoutOutput(Map<Database, Integer> databases) throws InvalidDataException, URISyntaxException {
        final ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
        for (Map.Entry<Database, Integer> databaseCount : databases.entrySet()) {
            data.add(makeStandardData(databaseCount, false));
        }

        doOutput(new String[] { "r", "l", "l", "r" },
                 new String[] { "id", "name", "hostname", "number of schemas" }, data);
    }

    private void precsvinfos(Map<Database, Integer> databases) throws URISyntaxException, InvalidDataException {
        // needed for csv output, KEEP AN EYE ON ORDER!!!
        final ArrayList<String> columns = new ArrayList<String>();
        columns.add("id");
        columns.add("display_name");
        columns.add("url");
        columns.add("number_of_schemas");

        final ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();

        for (Map.Entry<Database, Integer> databaseCount : databases.entrySet()) {
            data.add(makeCSVData(databaseCount));
        }

        doCSVOutput(columns, data);
    }

    public static void main(final String args[]) {
        new CountDatabaseSchema(args);
    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);
        setSearchOption(parser);
        setCSVOutputOption(parser);
        setOnlyEmptySchemas(parser);
    }

    /**
     * @param db
     * @return
     * @throws URISyntaxException
     */
    private ArrayList<String> makeCSVData(Map.Entry<Database, Integer> databaseCount) throws URISyntaxException{
        final ArrayList<String> rea_data = makeStandardData(databaseCount, true);
        return rea_data;
    }

    private ArrayList<String> makeStandardData(Map.Entry<Database, Integer> databaseCount, final boolean csv) throws URISyntaxException {
        final ArrayList<String> rea_data = new ArrayList<String>();

        Database db = databaseCount.getKey();
        rea_data.add(db.getId().toString());

        if (null != db.getName()) {
            rea_data.add(db.getName());
        } else {
            rea_data.add(null);
        }

        if (null != db.getUrl()) {
            if (csv) {
                rea_data.add(db.getUrl());
            } else {
                rea_data.add(new URI(db.getUrl().substring("jdbc:".length())).getHost());
            }
        } else {
            rea_data.add(null);
        }

        Integer schemaCount = databaseCount.getValue();
        if (null != schemaCount) {
            rea_data.add(schemaCount.toString());
        } else {
            rea_data.add(null);
        }

        return rea_data;
    }

    @Override
    protected final String getObjectName() {
        return "database schemas";
    }

}
