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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.admin.console.user.configuration;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserProperty;
import com.openexchange.auth.rmi.RemoteAuthenticator;
import com.openexchange.cli.AbstractRmiCLI;

/**
 * CLT to request the user configuration and permissions per RMI calls.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class GetUserConfigurationSource extends AbstractRmiCLI<Void> {

    private static final String OPT_USER_SHORT = "u";
    private static final String OPT_USER_LONG = "user";
    private static final String OPT_CONTEXT_SHORT = "c";
    private static final String OPT_CONTEXT_LONG = "context";
    private static final String OPT_CONFIGURATION_SHORT = "conf";
    private static final String OPT_CONFIGURATION_LONG = "user-configuration";
    private static final String OPT_CAPABILITIES_SHORT = "cap";
    private static final String OPT_CAPABILITIES_LONG = "user-capabilities";

    public static void main(String[] args) {
        new GetUserConfigurationSource().execute(args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void administrativeAuth(String login, String password, CommandLine cmd, RemoteAuthenticator authenticator) {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addOptions(Options options) {
        options.addOption(new Option(OPT_CONTEXT_SHORT, OPT_CONTEXT_LONG, true, "A valid context identifier"));
        options.addOption(new Option(OPT_USER_SHORT, OPT_USER_LONG, true, "A valid user identifier"));
        options.addOption(new Option(OPT_CONFIGURATION_SHORT, OPT_CONFIGURATION_LONG, true, "Outputs the configuration associated with the given user. Filter by providing a pattern of the property."));
        options.addOption(new Option(OPT_CAPABILITIES_SHORT, OPT_CAPABILITIES_LONG, false, "Outputs the capabilities associated with the given user."));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void checkOptions(CommandLine cmd) {
        if (!cmd.hasOption(OPT_CONTEXT_SHORT)) {
            System.out.println("You must provide a context identifier.");
            System.exit(-1);
            return;
        }

        if (!cmd.hasOption(OPT_USER_SHORT)) {
            System.out.println("You must provide a user identifier.");
            System.exit(-1);
            return;
        }

        if (!cmd.hasOption(OPT_CAPABILITIES_SHORT) && !cmd.hasOption(OPT_CONFIGURATION_SHORT)) {
            System.out.println("Either user capabilities ('" + OPT_CAPABILITIES_SHORT + "') or user configuration ('" + OPT_CONFIGURATION_SHORT + "' <arg>) has to be added.");
            System.exit(-1);
            return;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Void invoke(Options option, CommandLine cmd, String optRmiHostName) throws Exception {
        OXUserInterface oxUserInterface = getUserInterface();

        final Context ctx = new Context(parseInt('c', 0, cmd, option));
        final User user = new User(parseInt('u', 0, cmd, option));
        Credentials credentials = new Credentials(cmd.getOptionValue('A'), cmd.getOptionValue('P'));

        if (cmd.hasOption(OPT_CONFIGURATION_SHORT)) {
            String searchPattern = cmd.getOptionValue(OPT_CONFIGURATION_LONG);
            List<UserProperty> userConfigurationSource = oxUserInterface.getUserConfigurationSource(ctx, user, searchPattern, credentials);
            if (userConfigurationSource.size() <= 0) {
                System.out.println("No property with pattern '" + searchPattern + "' found!");
                return null;
            }

            System.out.println("Configuration found: ");
            for (UserProperty property : userConfigurationSource) {
                System.out.println(property.toString());
            }
            System.out.println();
        }
        if (cmd.hasOption(OPT_CAPABILITIES_SHORT)) {
            Map<String, Map<String, Set<String>>> userCapabilitiesSource = oxUserInterface.getUserCapabilitiesSource(ctx, user, credentials);
            if (userCapabilitiesSource.size() <= 0) {
                System.out.println("Not able to retrieve capabilities.");
                return null;
            }

            System.out.println("Capabilities sources found: ");
            for (Entry<String, Map<String, Set<String>>> capabilitiesSource : userCapabilitiesSource.entrySet()) {
                System.out.println("Source: " + capabilitiesSource.getKey());
                for (Entry<String, Set<String>> value : capabilitiesSource.getValue().entrySet()) {
                    System.out.println("-- " + value.getKey() + ": " + value.getValue());
                }
            }
            System.out.println();
        }
        return null;
    }

    private final OXUserInterface getUserInterface() throws NotBoundException, MalformedURLException, RemoteException {
        return (OXUserInterface) getRmiStub(OXUserInterface.RMI_NAME);
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
    protected String getFooter() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getName() {
        return "getuserconfigurationsource";
    }
}
