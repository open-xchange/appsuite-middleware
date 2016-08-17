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

package com.openexchange.push.console;

import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import com.openexchange.auth.mbean.AuthenticatorMBean;
import com.openexchange.cli.AbstractMBeanCLI;
import com.openexchange.push.mbean.PushMBean;


/**
 * {@link DeleteRegisteredPushUser} - The command-line tool to delete a registered push user.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.2
 */
public class DeleteRegisteredPushUser extends AbstractMBeanCLI<Void> {

    public static void main(String[] args) {
        new DeleteRegisteredPushUser().execute(args);
    }

    /**
     * Initializes a new {@link DeleteRegisteredPushUser}.
     */
    public DeleteRegisteredPushUser() {
        super();
    }

    @Override
    protected void administrativeAuth(String login, String password, CommandLine cmd, AuthenticatorMBean authenticator) throws MBeanException {
        authenticator.doAuthentication(login, password);
    }

    @Override
    protected void addOptions(Options options) {
        options.addOption("c", "context", true, "A valid context identifier");
        options.addOption("u", "user", true, "A valid user identifier");
        options.addOption("i", "client", true, "The client identifier");
    }

    @Override
    protected Void invoke(Options option, CommandLine cmd, MBeanServerConnection mbsc) throws Exception {
        PushMBean pushMBean = getMBean(mbsc, PushMBean.class, com.openexchange.push.mbean.PushMBean.DOMAIN);

        int contextId = parseInt('c', 0, cmd, option);
        int userId = parseInt('u', 0, cmd, option);
        String clientId = cmd.getOptionValue('i');

        boolean deleted = pushMBean.unregisterPermanentListenerFor(userId, contextId, clientId);
        if (deleted) {
            System.out.println("Push registration successfully deleted for user " + userId + " in context " + contextId + " for client '" + clientId + "'");
        } else {
            System.out.println("No such push registration for user " + userId + " in context " + contextId + " for client '" + clientId + "'");
        }

        return null;
    }

    @Override
    protected void checkOptions(CommandLine cmd) {
        checkOptions(cmd, options);
    }

    @Override
    protected void checkOptions(CommandLine cmd, Options options) {
        if (!cmd.hasOption('c')) {
            System.out.println("You must provide a context identifier.");
            if (null != options) {
                printHelp(options);
            }
            System.exit(-1);
            return;
        }
        if (!cmd.hasOption('u')) {
            System.out.println("You must provide a user identifier.");
            if (null != options) {
                printHelp(options);
            }
            System.exit(-1);
            return;
        }
        if (!cmd.hasOption('i')) {
            System.out.println("You must provide a client identifier.");
            if (null != options) {
                printHelp(options);
            }
            System.exit(-1);
            return;
        }
    }

    @Override
    protected boolean requiresAdministrativePermission() {
        return true;
    }

    @Override
    protected String getFooter() {
        return "Command-line tool to delete a registered push user";
    }

    @Override
    protected String getName() {
        return "deleteregisteredpushuser";
    }

}
