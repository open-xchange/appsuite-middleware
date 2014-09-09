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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import com.openexchange.auth.mbean.AuthenticatorMBean;
import com.openexchange.cli.AbstractMBeanCLI;
import com.openexchange.database.migration.mbean.ConfigDBMigrationMBean;

/**
 * Command line tool to control config database migrations.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since 7.6.1
 */
public class ConfigDBMigrationCLT extends AbstractMBeanCLI<Void> {

    private static final String OPT_UNLOCK_SHORT = "u";
    private static final String OPT_UNLOCK_LONG = "force-unlock";
    private static final String OPT_LIST_LOCKS_SHORT = "ll";
    private static final String OPT_LIST_LOCKS_LONG = "list-locks";
    private static final String OPT_RUN_SHORT = "r";
    private static final String OPT_RUN_LONG = "run";

    public static void main(String[] args) {
        new ConfigDBMigrationCLT().execute(args);
    }

    // ------------------------------------------------------------------------------------ //

    /**
     * Initializes a new {@link CloseSessionsCLT}.
     */
    private ConfigDBMigrationCLT() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void checkOptions(CommandLine cmd) {
        //
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
        return "configdbmigrations";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addOptions(Options options) {
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
        ConfigDBMigrationMBean migrationMBean = MBeanServerInvocationHandler.newProxyInstance(
            mbsc,
            getObjectName(ConfigDBMigrationMBean.class.getName(), ConfigDBMigrationMBean.DOMAIN),
            ConfigDBMigrationMBean.class,
            false);

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
