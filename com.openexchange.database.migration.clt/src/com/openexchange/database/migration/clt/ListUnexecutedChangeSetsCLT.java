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

import java.util.ArrayList;
import java.util.List;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import com.openexchange.auth.mbean.AuthenticatorMBean;
import com.openexchange.cli.AbstractMBeanCLI;
import com.openexchange.database.migration.mbean.DBMigrationMBean;

/**
 * Command line tool to list the current status of database migrations.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class ListUnexecutedChangeSetsCLT extends AbstractMBeanCLI<List<String>> {

    public static void main(String[] args) {
        List<String> unexcecutedChangeSets = new ListUnexecutedChangeSetsCLT().execute(args);

        if (unexcecutedChangeSets.size() == 0) {
            System.out.println("All changesets successfully executed!");
        }
        else {
            System.out.println("The following changesets have currently not been executed!");
            for (String changeSetNotExecuted : unexcecutedChangeSets) {
                System.out.println(changeSetNotExecuted);
            }
        }
    }

    // ------------------------------------------------------------------------------------ //

    /**
     * Initializes a new {@link CloseSessionsCLT}.
     */
    private ListUnexecutedChangeSetsCLT() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void checkOptions(CommandLine cmd) {
        // TODO Auto-generated method stub

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
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getName() {
        return "listUnexecutedChangeSets";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addOptions(Options options) {
        options.addOption("f", "filename", true, "Name of the file the database migration status should be displayed for");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<String> invoke(Options option, CommandLine cmd, MBeanServerConnection mbsc) throws Exception {
        if (!cmd.hasOption('f')) {
            System.err.println("Missing file name the unexecuted changesets should be listed for.");
            printHelp(option);
            System.exit(1);
        }
        final String fileName = cmd.getOptionValue('f');
        final String[] signature = new String[] { String.class.getName() };
        final Object[] params = new Object[] { fileName };
        Object invoke = mbsc.invoke(getObjectName(DBMigrationMBean.class.getName(), DBMigrationMBean.DOMAIN), getName(), params, signature);

        List<String> unexecutedChangeSets = new ArrayList<String>();

        if (invoke instanceof List<?>) {
            unexecutedChangeSets = (List<String>) invoke;
        } else {
            System.out.println("Unexpected result from calling '" + getName() + "'. Neither 'true' nor 'false' received from the call.");
        }
        return unexecutedChangeSets;
    }
}
