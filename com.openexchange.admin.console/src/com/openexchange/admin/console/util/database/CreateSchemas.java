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

    public CreateSchemas(final String[] args2) {
        final AdminParser parser = new AdminParser("createschemas");
        setOptions(parser);

        String successtext = null;
        try {
            parser.ownparse(args2);

            final Credentials auth = credentialsparsing(parser);

            // get rmi ref
            final OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(RMI_HOSTNAME + OXUtilInterface.RMI_NAME);

            final Database db = new Database();
            parseAndSetDatabaseID(parser, db);
            parseAndSetDatabasename(parser, db);
            parseAndSetNumberOfSchemas(parser);

            successtext = nameOrIdSet(this.dbid, this.dbname, "database");

            // Trigger creation of schemas
            final AtomicReference<String[]> createdSchemas = new AtomicReference<>(null);
            final AtomicReference<Exception> errorRef = new AtomicReference<Exception>();
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
            FutureTask<Void> ft = new FutureTask<Void>(runnbable, null);
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
                    infoMessage = "Creating "+numSchemas+" schemas in database " + (null == dbid ? dbname : dbid) + ". This may take a while";
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
        } catch (final Exception e) {
            printErrors(null, null, e, parser);
            sysexit(SYSEXIT_SERVERSTORAGE_ERROR);
        }

    }

    @Override
    protected String getObjectName() {
        return "Database schemas";
    }

    public static void main(final String args[]) {
        new CreateSchemas(args);
    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);

        setDatabaseIDOption(parser);
        setDatabaseNameOption(parser, NeededQuadState.eitheror);

        setNumberOfSchemasOption(parser);
    }
}
