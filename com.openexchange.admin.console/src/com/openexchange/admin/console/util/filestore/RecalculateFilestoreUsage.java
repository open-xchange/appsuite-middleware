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
package com.openexchange.admin.console.util.filestore;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.RecalculationScope;
import com.openexchange.auth.rmi.RemoteAuthenticator;
import com.openexchange.cli.AbstractRmiCLI;

/**
 *
 * {@link RecalculateFilestoreUsage}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class RecalculateFilestoreUsage extends AbstractRmiCLI<Void> {

    private static final String OPT_USER_SHORT = "u";
    private static final String OPT_USER_LONG = "user";
    private static final String OPT_CONTEXT_SHORT = "c";
    private static final String OPT_CONTEXT_LONG = "context";
    private static final String OPT_SCOPE_LONG = "scope";

    public static void main(final String args[]) {
        new RecalculateFilestoreUsage().execute(args);
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------

    private Credentials credentials;

    /**
     * Initializes a new {@link RecalculateFilestoreUsage}.
     */
    public RecalculateFilestoreUsage() {
        super();
    }

    @Override
    protected void administrativeAuth(String login, String password, CommandLine cmd, RemoteAuthenticator authenticator) throws RemoteException {
        credentials = new Credentials(login, password);
    }

    @Override
    protected void addOptions(Options options) {
        options.addOption(new Option(OPT_CONTEXT_SHORT, OPT_CONTEXT_LONG, true, "The identifier of the context for which the file storage usage shall be recalculated. "+
                                                                                "If a user identifier is also specified, only the user-associated file storage is considered."));
        options.addOption(new Option(OPT_USER_SHORT, OPT_USER_LONG, true, "The identifier of the user for which the file storage usage shall be recalculated or "+
                                                                          "'all' to recalculates the usages for all user-associated file storage in the given context."));

        Option allOption = new Option(null, OPT_SCOPE_LONG, true, "Scope can be either set to either 'all', 'context' or 'user'. If set "+
                                                                 "to 'all', all usages of all context and user file stores are recalculated. "+
                                                                 "Otherwise the usages of either context- or user-associated file storages are recalculated. "+
                                                                 "Cannot be used in conjunction with the '--" + OPT_CONTEXT_LONG + "' or '--" + OPT_USER_LONG + "'.");
        allOption.setArgName("scope");
        options.addOption(allOption);
    }

    @Override
    protected Void invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {
        try {
            OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(RMI_HOSTNAME + OXUtilInterface.RMI_NAME);

            boolean hasScopeOption = cmd.hasOption(OPT_SCOPE_LONG);
            if (hasScopeOption) {
                String scope = hasScopeOption ? cmd.getOptionValue(OPT_SCOPE_LONG) : null;
                recalculateUsingScope(scope, oxutil, null);
            } else {
                recalculateDedicated(options, cmd, oxutil);
            }
        } catch (Exception e) {
            System.err.println("Failed to recalculate usage: " + e.getMessage());
            System.exit(-1);
        }
        return null;
    }

    private void recalculateDedicated(Options options, CommandLine cmd, OXUtilInterface oxutil) throws Exception {
        // Require 'context' option
        if (false == cmd.hasOption(OPT_CONTEXT_LONG)) {
            System.err.println("If \"" + OPT_SCOPE_LONG + "\" option is not set, the \"" + OPT_CONTEXT_LONG + "\" is required.");
            System.exit(-1);
        }
        int contextId = parseInt(OPT_CONTEXT_LONG, 0, cmd, options);
        if (contextId <= 0) {
            System.err.println("\"" + OPT_CONTEXT_LONG + "\" does not specify a valid context identifier.");
            System.exit(-1);
        }

        // Check optional 'user' option
        Integer userId = null;
        if (cmd.hasOption(OPT_USER_LONG)) {
            if ("all".equals(cmd.getOptionValue(OPT_USER_LONG).toLowerCase())) {
                recalculateUsingScope(RecalculationScope.USER.name(), oxutil, Integer.valueOf(contextId));
                return;
            }

            userId = Integer.valueOf(parseInt(OPT_USER_LONG, 0, cmd, options));
            if (userId.intValue() <= 0) {
                System.err.println("\"" + OPT_USER_LONG + "\" does not specify a valid user identifier.");
                System.exit(-1);
            }
        }

        // Trigger recalculation
        if (userId != null) {
            oxutil.recalculateFilestoreUsage(Integer.valueOf(contextId), userId, credentials);
            System.out.println("Usage has been successfully recalculated for user " + userId + " in context " + contextId);
        } else {
            oxutil.recalculateFilestoreUsage(Integer.valueOf(contextId), null, credentials);
            System.out.println("Usage has been successfully recalculated for context " + contextId);
        }
    }

    private void recalculateUsingScope(String scopeName, final OXUtilInterface oxutil, final Integer ctxId) throws Exception {
        // Create & execute task to recalculates usages for given scope
        final RecalculationScope scope = RecalculationScope.getScopeByName(scopeName);
        final AtomicReference<Exception> errorRef = new AtomicReference<Exception>();
        final Credentials credentials = this.credentials;
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    oxutil.recalculateFilestoreUsage(scope, ctxId, credentials);
                } catch (Exception e) {
                    errorRef.set(e);
                }
            }
        };
        FutureTask<Void> ft = new FutureTask<Void>(runnable, null);
        new Thread(ft, RecalculateFilestoreUsage.class.getSimpleName()).start();

        // Await task termination
        System.out.print("Recalculating usages of file storages");
        int c = 34;
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

        System.out.println("Usages have been successfully recalculated");
    }

    @Override
    protected boolean requiresAdministrativePermission() {
        return true;
    }

    @Override
    protected void checkOptions(CommandLine cmd) {
        if (!cmd.hasOption(OPT_CONTEXT_SHORT) && !cmd.hasOption(OPT_SCOPE_LONG)) {
            System.out.println("You must provide either the \"" + OPT_CONTEXT_LONG + "\" or \"" + OPT_SCOPE_LONG + "\" option.");
            printHelp();
            System.exit(-1);
            return;
        }
    }

    @Override
    protected String getFooter() {
        return "The command-line tool to recalculate the usage of file storages";
    }

    @Override
    protected String getName() {
        return "recalculatefilestoreusage [-c <context-id> [-u <user-id>] | --scope <scope>]";
    }
}
