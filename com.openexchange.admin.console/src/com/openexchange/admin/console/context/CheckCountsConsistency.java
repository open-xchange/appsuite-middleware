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

package com.openexchange.admin.console.context;

import java.rmi.Naming;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.rmi.OXContextInterface;
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
        new CheckCountsConsistency(args);
    }

    /**
     * Initialises a new {@link CheckCountsConsistency}.
     *
     * @param args The command line arguments
     */
    public CheckCountsConsistency(final String[] args) {

        final AdminParser parser = new AdminParser("checkcountsconsistency");

        setDefaultCommandLineOptionsWithoutContextID(parser);
        try {
            parser.ownparse(args);

            final Credentials auth = new Credentials((String) parser.getOptionValue(this.adminUserOption), (String) parser.getOptionValue(this.adminPassOption));
            final OXContextInterface oxres = (OXContextInterface) Naming.lookup(RMI_HOSTNAME + OXContextInterface.RMI_NAME);

            final AtomicReference<Exception> errorRef = new AtomicReference<Exception>();
            Runnable runnbable = new Runnable() {

                @Override
                public void run() {
                    try {
                        oxres.checkCountsConsistency(true, true, auth);
                    } catch (Exception e) {
                        errorRef.set(e);
                    }
                }
            };
            FutureTask<Void> ft = new FutureTask<Void>(runnbable, null);
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
        } catch (final Exception e) {
            printErrors(null, null, e, parser);
        }
    }

    @Override
    protected String getObjectName() {
        return "counts consistency";
    }
}
