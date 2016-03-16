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

package com.openexchange.cli;

import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import com.openexchange.auth.mbean.AuthenticatorMBean;


/**
 * {@link AbstractContextMBeanCLI} - The {@link AbstractMBeanCLI} for a context.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.4.2
 */
public abstract class AbstractContextMBeanCLI<R> extends AbstractMBeanCLI<R> {

    /**
     * Initializes a new {@link AbstractContextMBeanCLI}.
     */
    protected AbstractContextMBeanCLI() {
        super();
    }

    @Override
    protected void addOptions(Options options) {
        options.addOption("c", "context", true, "A valid context identifier");
        addMoreOptions(options);
    }

    @Override
    protected R invoke(Options options, CommandLine cmd, MBeanServerConnection mbsc) throws Exception {
        // Parse context identifier
        if (!cmd.hasOption('c')) {
            System.err.println("Missing context identifier.");
            printHelp(options);
            System.exit(1);
            return null;
        }
        final int contextId;
        {
            final String optionValue = cmd.getOptionValue('c');
            try {
                contextId = Integer.parseInt(optionValue.trim());
            } catch (final NumberFormatException e) {
                System.err.println("Context identifier parameter is not a number: " + optionValue);
                printHelp(options);
                System.exit(1);
                return null;
            }
        }

        // Invoke MBean method
        return invoke(contextId, options, cmd, mbsc);
    }

    @Override
    protected boolean requiresAdministrativePermission() {
        return true;
    }

    /**
     * Checks if authentication is enabled.
     * <p>
     * Property <code>"CONTEXT_AUTHENTICATION_DISABLED"</code> gets examined.
     *
     * @param authenticator The authenticator MBean
     * @throws MBeanException If operation fails
     */
    @Override
    protected boolean isAuthEnabled(AuthenticatorMBean authenticator) throws MBeanException {
        return !authenticator.isContextAuthenticationDisabled();
    }

    @Override
    protected void administrativeAuth(String login, String password, CommandLine cmd, AuthenticatorMBean authenticator) throws MBeanException {
        final String optionValue = cmd.getOptionValue('c');
        try {
            authenticator.doAuthentication(login, password, Integer.parseInt(optionValue.trim()));
        } catch (final NumberFormatException e) {
            System.err.println("Context identifier parameter is not a number: ''" + optionValue + "''");
            System.exit(1);
        }
    }

    /**
     * Adds this command-line tool's options.
     * <p>
     * Note following options are reserved:
     * <ul>
     * <li>-h / --help
     * <li>-t / --host
     * <li>-p / --port
     * <li>-l / --login
     * <li>-s / --password
     * <li>-A / --adminuser
     * <li>-P / --adminpass
     * <li>-c / --context
     * </ul>
     *
     * @param options The options
     */
    protected abstract void addMoreOptions(Options options);

    /**
     * Invokes the MBean's method.
     *
     * @param contextId The context identifier
     * @param option The options
     * @param cmd The command line providing parameters/options
     * @param mbsc The MBean server connection
     * @return The return value
     * @throws Exception If invocation fails
     */
    protected abstract R invoke(int contextId, Options options, CommandLine cmd, MBeanServerConnection mbsc) throws Exception;

}
