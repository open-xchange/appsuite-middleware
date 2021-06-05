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

package com.openexchange.admin.console.util.database;

import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.Naming;
import java.util.ArrayList;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;

/**
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CheckDatabase extends DatabaseAbstraction {

    public void execute(final String[] args2) {

        final AdminParser parser = new AdminParser("checkdatabase");

        setOptions(parser);

        try {
            parser.ownparse(args2);

            final Credentials auth = credentialsparsing(parser);

            // get rmi ref
            final OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(RMI_HOSTNAME +OXUtilInterface.RMI_NAME);

            // Trigger checkdatabase
            final AtomicReference<Database[][]> checkedDatabases = new AtomicReference<>(null);
            final AtomicReference<Exception> errorRef = new AtomicReference<>();
            Runnable runnbable = new Runnable() {

                @Override
                public void run() {
                    try {
                        checkedDatabases.set(oxutil.checkDatabase(auth));
                    } catch (Exception e) {
                        errorRef.set(e);
                    }
                }
            };
            FutureTask<Void> ft = new FutureTask<>(runnbable, null);
            new Thread(ft, "Open-Xchange Database Checker").start();

            // Await termination
            String infoMessage = "Checking database schemas";
            System.out.print(infoMessage);
            int c = infoMessage.length();
            while (false == ft.isDone()) {
                System.out.print(".");
                if (c++ >= 76) {
                    c = 0;
                    System.out.println();
                }
                LockSupport.parkNanos(TimeUnit.NANOSECONDS.convert(500L, TimeUnit.MILLISECONDS));
            }
            System.out.println();

            // Check for error
            Exception error = errorRef.get();
            if (null != error) {
                throw error;
            }

            // Success..
            System.out.println();
            Database[][] databases = checkedDatabases.get();

            if (null != parser.getOptionValue(this.csvOutputOption)) {
                precsvinfos(databases);
            } else {
                sysoutOutput(databases, parser);
            }

            sysexit(0);
        } catch (Exception e) {
            printErrors(null, ctxid, e, parser);
        }

    }

    private void sysoutOutput(Database[][] databases, AdminParser parser) throws InvalidDataException, URISyntaxException {
        Database[] needingUpdate = databases[0];
        Database[] currentlyUpdating = databases[1];
        Database[] outdatedUpdating = databases[2];

        int total = needingUpdate.length + currentlyUpdating.length + outdatedUpdating.length;
        if (total <= 0) {
            createMessageForStdout("Neither pending, blocking nor stale schemas found", parser);
        } else {
            ArrayList<ArrayList<String>> data = new ArrayList<>(total);
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
    }

    private void precsvinfos(final Database[][] databases) throws URISyntaxException, InvalidDataException {
        // needed for csv output, KEEP AN EYE ON ORDER!!!
        final ArrayList<String> columns = new ArrayList<>(5);
        columns.add("id");
        columns.add("display_name");
        columns.add("url");
        columns.add("scheme");
        columns.add("status");

        Database[] needingUpdate = databases[0];
        Database[] currentlyUpdating = databases[1];
        Database[] outdatedUpdating = databases[2];

        ArrayList<ArrayList<String>> data = new ArrayList<>(needingUpdate.length + currentlyUpdating.length + outdatedUpdating.length);
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
        new CheckDatabase().execute(args);
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
        ArrayList<String> rea_data = new ArrayList<>();

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
