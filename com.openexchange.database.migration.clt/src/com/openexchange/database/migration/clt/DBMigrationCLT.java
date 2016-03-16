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

import java.util.Set;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import com.openexchange.auth.mbean.AuthenticatorMBean;
import com.openexchange.cli.AbstractMBeanCLI;
import com.openexchange.database.migration.mbean.DBMigrationMBean;
import com.openexchange.java.Strings;

/**
 * Command line tool to control database migrations.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since 7.6.1
 */
public class DBMigrationCLT extends AbstractMBeanCLI<Void> {

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
     * Initializes a new {@link CloseSessionsCLT}.
     */
    private DBMigrationCLT() {
        super();
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

    /**
     * {@inheritDoc}
     */
    @Override
    protected void administrativeAuth(String login, String password, CommandLine cmd, AuthenticatorMBean authenticator) throws MBeanException {
        authenticator.doAuthentication(login, password);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getFooter() {
        return "Prints the current migration status if no option is set.";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getName() {
        return "dbmigrations";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addOptions(Options options) {
        OptionGroup requiredOptions = new OptionGroup();
        requiredOptions.setRequired(true);
        requiredOptions.addOption(new Option(OPT_SCHEMA_NAME_SHORT, OPT_SCHEMA_NAME_LONG, true, "The database schema name to use"));
        options.addOptionGroup(requiredOptions);
        OptionGroup ops = new OptionGroup();
        ops.setRequired(false);
        ops.addOption(new Option(OPT_RUN_SHORT, OPT_RUN_LONG, false, "Forces a run of the current core changelog."));
        ops.addOption(new Option(OPT_LIST_LOCKS_SHORT, OPT_LIST_LOCKS_LONG, false, "Lists all currently acquired locks."));
        ops.addOption(new Option(OPT_UNLOCK_SHORT, OPT_UNLOCK_LONG, false, "Forces a release of all locks."));
        options.addOptionGroup(ops);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Void invoke(Options options, CommandLine cmd, MBeanServerConnection mbsc) throws Exception {
        String schemaName = cmd.getOptionValue(OPT_SCHEMA_NAME_SHORT);
        if (Strings.isEmpty(schemaName)) {
            throw new MissingOptionException("Database schema name missing");
        }
        /*
         * search matching migration MBean
         */
        Set<ObjectName> objectNames = mbsc.queryNames(new ObjectName(DBMigrationMBean.DOMAIN + ":*,name=" + schemaName), null);
        if (null == objectNames || 0 == objectNames.size()) {
            System.err.println("No migration MBean found for schema name \"" + schemaName + "\"");
            System.exit(1);
            return null;
        }
        if (1 < objectNames.size()) {
            System.err.println("More than one matching migration MBeans found for schema name \"" + schemaName + "\":");
            for (ObjectName objectName : objectNames) {
                System.err.println(" - " + objectName);
            }
            System.exit(1);
            return null;
        }
        /*
         * instantiate proxy & invoke requested operation
         */
        ObjectName objectName = objectNames.iterator().next();
        DBMigrationMBean migrationMBean = MBeanServerInvocationHandler.newProxyInstance(mbsc, objectName, DBMigrationMBean.class, false);
        if (cmd.hasOption(OPT_RUN_SHORT)) {
            migrationMBean.forceMigration();
            System.out.println("Done.\nCurrent status: " + migrationMBean.getMigrationStatus());
        } else if (cmd.hasOption(OPT_LIST_LOCKS_SHORT)) {
            System.out.println(migrationMBean.getLockStatus());
        } else if (cmd.hasOption(OPT_UNLOCK_SHORT)) {
            migrationMBean.releaseLocks();
            System.out.println("Done.\n" + migrationMBean.getLockStatus());
        } else {
            System.out.println(migrationMBean.getMigrationStatus());
        }

        return null;
    }
}
