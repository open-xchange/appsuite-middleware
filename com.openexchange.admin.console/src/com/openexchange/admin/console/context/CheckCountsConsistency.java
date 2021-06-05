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

package com.openexchange.admin.console.context;

import java.rmi.Naming;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;

/**
 * {@link CheckCountsConsistency}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public final class CheckCountsConsistency extends ContextAbstraction {

    /**
     * Entry point
     *
     * @param args The command line arguments
     */
    public static void main(final String args[]) {
        new CheckCountsConsistency().execute(args);
    }

    /**
     * Initializes a new {@link CheckCountsConsistency}.
     */
    private CheckCountsConsistency() {
        super();
    }

    /**
     * Executes the command
     *
     * @param args The command line arguments
     */
    private void execute(String[] args) {
        AdminParser parser = new AdminParser("checkcountsconsistency");
        setDefaultCommandLineOptionsWithoutContextID(parser);
        try {
            parser.ownparse(args);

            Credentials auth = new Credentials((String) parser.getOptionValue(this.adminUserOption), (String) parser.getOptionValue(this.adminPassOption));
            OXUtilInterface oxutil = OXUtilInterface.class.cast(Naming.lookup(RMI_HOSTNAME + OXUtilInterface.RMI_NAME));

            AtomicReference<Exception> errorRef = new AtomicReference<>();
            Runnable runnbable = () -> {
                try {
                    oxutil.checkCountsConsistency(true, true, auth);
                } catch (Exception e) {
                    errorRef.set(e);
                }
            };
            FutureTask<Void> ft = new FutureTask<>(runnbable, null);
            new Thread(ft, "Open-Xchange Counts Consistency Checker").start();

            // Await termination
            System.out.print("Checking consistency for counters. This may take a while");
            int c = 56;
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

            System.out.println("Counts successfully checked");
            sysexit(0);
        } catch (Exception e) {
            printErrors(null, null, e, parser);
        }
    }

    @Override
    protected String getObjectName() {
        return "counts consistency";
    }
}
