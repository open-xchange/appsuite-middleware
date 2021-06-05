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

import java.rmi.Naming;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;

/**
 *
 * @author d7,cutmasta
 *
 */
public final class RegisterDatabase extends DatabaseAbstraction {

    public void execute(final String[] args2) {

        final AdminParser parser = new AdminParser("registerdatabase");

        setOptions(parser);

        try {
            parser.ownparse(args2);

            final Credentials auth = credentialsparsing(parser);

            // get rmi ref
            final OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(RMI_HOSTNAME + OXUtilInterface.RMI_NAME);

            final Database db = new Database();
            parseAndSetDatabasename(parser, db);

            parseAndSetMandatoryOptions(parser, db);

            parseAndSetMasterAndID(parser, db);

            parseAndSetCreateAndNumberOfSchemas(parser);

            if (null == createSchemas || !createSchemas.booleanValue()) {
                // Simple database registration w/o pre-creation of schemas
                displayRegisteredMessage(oxutil.registerDatabase(db, createSchemas, numberOfSchemas, auth).getId().toString(), parser);
                sysexit(0);
                return;
            }

            // Trigger database registration w/ pre-creation of schemas
            final AtomicInteger dbId = new AtomicInteger(0);
            final AtomicReference<Exception> errorRef = new AtomicReference<>();
            Runnable runnbable = new Runnable() {

                @Override
                public void run() {
                    try {
                        dbId.set(oxutil.registerDatabase(db, createSchemas, numberOfSchemas, auth).getId().intValue());
                    } catch (Exception e) {
                        errorRef.set(e);
                    }
                }
            };
            FutureTask<Void> ft = new FutureTask<>(runnbable, null);
            new Thread(ft, "Open-Xchange Database Registerer").start();

            // Await termination
            String infoMessage;
            if (null == numberOfSchemas || numberOfSchemas.intValue() <= 0) {
                infoMessage = "Registering database " + dbname + " and pre-creating schemas. This may take a while";
            } else {
                int numSchemas = numberOfSchemas.intValue();
                if (1 == numSchemas) {
                    infoMessage = "Registering database " + dbname + " and pre-creating 1 schema. This may take a while";
                } else {
                    infoMessage = "Registering database " + dbname + " and pre-creating " + numSchemas + " schemas. This may take a while";
                }
            }
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
            displayRegisteredMessage(String.valueOf(dbId.get()), parser);
            sysexit(0);
        } catch (Exception e) {
            printErrors(null, null, e, parser);
        }

    }

    public static void main(final String args[]) {
        new RegisterDatabase().execute(args);
    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);

        setDatabaseNameOption(parser, NeededQuadState.needed);
        setDatabaseHostnameOption(parser, false);
        setDatabaseUsernameOption(parser, false);
        setDatabaseDriverOption(parser, OXUtilInterface.DEFAULT_DRIVER, false);
        setDatabasePasswdOption(parser, true);
        setDatabaseIsMasterOption(parser, true);
        setDatabaseMasterIDOption(parser, false);
        setDatabaseMaxUnitsOption(parser, String.valueOf(OXUtilInterface.DEFAULT_MAXUNITS), false);
        setDatabasePoolHardlimitOption(parser, String.valueOf(OXUtilInterface.DEFAULT_POOL_HARD_LIMIT), false);
        setDatabasePoolInitialOption(parser, String.valueOf(OXUtilInterface.DEFAULT_POOL_INITIAL), false);
        setDatabasePoolMaxOption(parser, String.valueOf(OXUtilInterface.DEFAULT_POOL_MAX), false);

        setCreateAndNumberOfSchemasOption(parser);
    }
}
