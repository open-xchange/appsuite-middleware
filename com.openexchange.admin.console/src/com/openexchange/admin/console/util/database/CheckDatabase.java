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
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;

/**
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CheckDatabase extends DatabaseAbstraction {

    public CheckDatabase(final String[] args2) {

        final AdminParser parser = new AdminParser("checkdatabase");

        setOptions(parser);

        try {
            parser.ownparse(args2);

            final Credentials auth = credentialsparsing(parser);

            // get rmi ref
            final OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(RMI_HOSTNAME +OXUtilInterface.RMI_NAME);
            final Database[][] databases = oxutil.checkDatabase(auth);

            if (null != parser.getOptionValue(this.csvOutputOption)) {
                precsvinfos(databases);
            } else {
                sysoutOutput(databases);
            }

            sysexit(0);
        } catch (final Exception e) {
            printErrors(null, ctxid, e, parser);
        }

    }

    private void sysoutOutput(Database[][] databases) throws InvalidDataException, URISyntaxException {
        Database[] needingUpdate = databases[0];
        Database[] currentlyUpdating = databases[1];
        Database[] outdatedUpdating = databases[2];

        ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>(needingUpdate.length + currentlyUpdating.length + outdatedUpdating.length);
        for (Database database : needingUpdate) {
            data.add(makeStandardData(database, false, "Needs update"));
        }
        for (Database database : currentlyUpdating) {
            data.add(makeStandardData(database, false, "Blocking updates running"));
        }
        for (Database database : outdatedUpdating) {
            data.add(makeStandardData(database, false, "Blocking updates running for too long"));
        }

        doOutput(new String[] { "r", "l", "l", "l", "l" },
                 new String[] { "id", "name", "hostname", "scheme", "status" }, data);
    }

    private void precsvinfos(final Database[][] databases) throws URISyntaxException, InvalidDataException {
        // needed for csv output, KEEP AN EYE ON ORDER!!!
        final ArrayList<String> columns = new ArrayList<String>(5);
        columns.add("id");
        columns.add("display_name");
        columns.add("url");
        columns.add("scheme");
        columns.add("status");

        Database[] needingUpdate = databases[0];
        Database[] currentlyUpdating = databases[1];
        Database[] outdatedUpdating = databases[2];

        ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>(needingUpdate.length + currentlyUpdating.length + outdatedUpdating.length);
        for (final Database database : needingUpdate) {
            data.add(makeCSVData(database, "Needs update"));
        }
        for (final Database database : currentlyUpdating) {
            data.add(makeCSVData(database, "Blocking updates running"));
        }
        for (final Database database : outdatedUpdating) {
            data.add(makeCSVData(database, "Blocking updates running for too long"));
        }

        doCSVOutput(columns, data);
    }

    public static void main(final String args[]) {
        new CheckDatabase(args);
    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);
        setCSVOutputOption(parser);
    }

    /**
     * @param db
     * @return
     * @throws URISyntaxException
     */
    private ArrayList<String> makeCSVData(final Database db, String status) throws URISyntaxException{
        ArrayList<String> rea_data = makeStandardData(db, true, status);
        return rea_data;
    }

    private ArrayList<String> makeStandardData(Database db, boolean csv, String status) throws URISyntaxException {
        ArrayList<String> rea_data = new ArrayList<String>();

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

        if (null != db.getScheme()) {
            rea_data.add(db.getScheme().toString());
        } else {
            rea_data.add(null);
        }

        if (null != status) {
            rea_data.add(status);
        } else {
            rea_data.add(null);
        }

        return rea_data;
    }

    @Override
    protected final String getObjectName() {
        return "databases";
    }

}
