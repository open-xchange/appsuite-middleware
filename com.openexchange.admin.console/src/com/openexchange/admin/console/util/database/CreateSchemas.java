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
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;

/**
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class CreateSchemas extends DatabaseAbstraction {

    public void execute(final String[] args2) {
        final AdminParser parser = new AdminParser("createschemas");
        setOptions(parser);

        try {
            parser.ownparse(args2);

            final Credentials auth = credentialsparsing(parser);

            // get rmi ref
            final OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(RMI_HOSTNAME + OXUtilInterface.RMI_NAME);

            final Database db = new Database();
            parseAndSetDatabaseID(parser, db);
            parseAndSetDatabasename(parser, db);
            parseAndSetNumberOfSchemas(parser);

            nameOrIdSet(this.dbid, this.dbname, "database");

            // Trigger creation of schemas
            final AtomicReference<String[]> createdSchemas = new AtomicReference<>(null);
            final AtomicReference<Exception> errorRef = new AtomicReference<>();
            Runnable runnbable = new Runnable() {

                @Override
                public void run() {
                    try {
                        createdSchemas.set(oxutil.createSchemas(db, numberOfSchemas, auth));
                    } catch (Exception e) {
                        errorRef.set(e);
                    }
                }
            };
            FutureTask<Void> ft = new FutureTask<>(runnbable, null);
            new Thread(ft, "Open-Xchange Database Schema Creator").start();

            // Await termination
            String infoMessage;
            if (null == numberOfSchemas || numberOfSchemas.intValue() <= 0) {
                infoMessage = "Creating schemas in database " + (null == dbid ? dbname : dbid) + ". This may take a while";
            } else {
                int numSchemas = numberOfSchemas.intValue();
                if (1 == numSchemas) {
                    infoMessage = "Creating 1 schema in database " + (null == dbid ? dbname : dbid) + ". This may take a while";
                } else {
                    infoMessage = "Creating " + numSchemas + " schemas in database " + (null == dbid ? dbname : dbid) + ". This may take a while";
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
            String[] schemas = createdSchemas.get();
            System.out.println(schemas.length + " schemas successfully created for database " + (null == dbid ? dbname : dbid));

            sysexit(0);
        } catch (Exception e) {
            printErrors(null, null, e, parser);
            sysexit(SYSEXIT_SERVERSTORAGE_ERROR);
        }

    }

    @Override
    protected String getObjectName() {
        return "Database schemas";
    }

    public static void main(final String args[]) {
        new CreateSchemas().execute(args);
    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);

        setDatabaseIDOption(parser);
        setDatabaseNameOption(parser, NeededQuadState.eitheror);

        setNumberOfSchemasOption(parser);
    }
}
