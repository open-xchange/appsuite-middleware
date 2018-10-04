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

package com.openexchange.database.migration.clt;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import com.openexchange.auth.rmi.RemoteAuthenticator;
import com.openexchange.cli.AbstractRmiCLI;
import com.openexchange.database.migration.rmi.DBMigrationRMIService;
import com.openexchange.java.Strings;

/**
 * Command line tool to control database migrations.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since 7.6.1
 */
public class DBMigrationCLT extends AbstractRmiCLI<Void> {

    private static final String FOOTER = "Prints the current migration status if no option is set.";
    private static final String SYNTAX = "dbmigrations -n <schemaName> [[-f] | [-ll] [-u]] -A <masterAdmin> -P <masterAdminPassword> [-p <RMI-Port>] [-s <RMI-Server] | [-h]";

    private static final String OPT_SCHEMA_NAME_SHORT = "n";
    private static final String OPT_SCHEMA_NAME_LONG = "name";
    private static final String OPT_UNLOCK_SHORT = "u";
    private static final String OPT_UNLOCK_LONG = "force-unlock";
    private static final String OPT_LIST_LOCKS_SHORT = "ll";
    private static final String OPT_LIST_LOCKS_LONG = "list-locks";
    private static final String OPT_RUN_SHORT = "r";
    private static final String OPT_RUN_LONG = "run";

    public static void main(String[] args) {
        new DBMigrationCLT().execute(args);
    }

    // ------------------------------------------------------------------------------------ //

    /**
     * Initializes a new {@link DBMigrationCLT}.
     */
    private DBMigrationCLT() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addOptions(Options options) {
        OptionGroup requiredOptions = new OptionGroup();
        requiredOptions.setRequired(true);
        requiredOptions.addOption(createArgumentOption(OPT_SCHEMA_NAME_SHORT, OPT_SCHEMA_NAME_LONG, "schemaName", "The database schema name to use", true));
        options.addOptionGroup(requiredOptions);
        OptionGroup ops = new OptionGroup();
        ops.setRequired(false);
        ops.addOption(createSwitch(OPT_RUN_SHORT, OPT_RUN_LONG, "Forces a run of the current core changelog.", false));
        ops.addOption(createSwitch(OPT_LIST_LOCKS_SHORT, OPT_LIST_LOCKS_LONG, "Lists all currently acquired locks.", false));
        ops.addOption(createSwitch(OPT_UNLOCK_SHORT, OPT_UNLOCK_LONG, "Forces a release of all locks.", false));
        options.addOptionGroup(ops);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void checkOptions(CommandLine cmd) {
        // No more options to check
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean requiresAdministrativePermission() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractRmiCLI#administrativeAuth(java.lang.String, java.lang.String, org.apache.commons.cli.CommandLine, com.openexchange.auth.rmi.RemoteAuthenticator)
     */
    @Override
    protected void administrativeAuth(String login, String password, CommandLine cmd, RemoteAuthenticator authenticator) throws RemoteException {
        authenticator.doAuthentication(login, password);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getFooter() {
        return FOOTER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getName() {
        return SYNTAX;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractRmiCLI#invoke(org.apache.commons.cli.Options, org.apache.commons.cli.CommandLine, java.lang.String)
     */
    @Override
    protected Void invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {
        String schemaName = cmd.getOptionValue(OPT_SCHEMA_NAME_SHORT);
        if (Strings.isEmpty(schemaName)) {
            throw new MissingOptionException("Database schema name missing");
        }

        DBMigrationRMIService rmiService;
        try {
            rmiService = getRmiStub(optRmiHostName, DBMigrationRMIService.RMI_NAME);
        } catch (NotBoundException e) {
            System.err.println("No migration RMI service found for schema name \"" + schemaName + "\"");
            System.exit(1);
            return null;
        }

        if (cmd.hasOption(OPT_RUN_SHORT)) {
            rmiService.forceMigration(schemaName);
            System.out.println("Done.\nCurrent status: " + rmiService.getLockStatus(schemaName));
        } else if (cmd.hasOption(OPT_LIST_LOCKS_SHORT)) {
            System.out.println(rmiService.getLockStatus(schemaName));
        } else if (cmd.hasOption(OPT_UNLOCK_SHORT)) {
            rmiService.releaseLocks(schemaName);
            System.out.println("Done.\n" + rmiService.getLockStatus(schemaName));
        } else {
            System.out.println(rmiService.getMigrationStatus(schemaName));
        }

        return null;
    }
}
