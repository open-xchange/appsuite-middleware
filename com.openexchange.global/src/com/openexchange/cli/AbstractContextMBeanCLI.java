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
            } catch (NumberFormatException e) {
                System.err.println("Context identifier parameter is not a number: " + optionValue);
                printHelp(options);
                System.exit(1);
                return null;
            }
        }

        // Invoke MBean method
        return invoke(contextId, options, cmd, mbsc);
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
        } catch (NumberFormatException e) {
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
