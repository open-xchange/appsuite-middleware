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

package com.openexchange.admin.console.user.configuration;

import static com.openexchange.java.Autoboxing.I;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionGroup;
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
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since 7.8.0
 */
public class GetUserConfigurationSource extends AbstractRmiCLI<Void> {

    private static final String NEWLINE = "\n";
    private static final String OPT_USER_SHORT = "i";
    private static final String OPT_USER_LONG = "userid";
    private static final String OPT_CONTEXT_SHORT = "c";
    private static final String OPT_CONTEXT_LONG = "context";
    private static final String OPT_CONFIGURATION_SHORT = "o";
    private static final String OPT_CONFIGURATION_LONG = "user-configuration";
    private static final String OPT_CAPABILITIES_SHORT = "a";
    private static final String OPT_CAPABILITIES_LONG = "user-capabilities";

    /**
     * Entry point
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        new GetUserConfigurationSource().execute(args);
    }

    // ------------------------------------------------------------------------------------------------------------------------------------

    private Integer contextId;

    /**
     * Prevent instantiation from outside.
     */
    private GetUserConfigurationSource() {
        super();
    }

    @Override
    protected boolean isAuthEnabled(RemoteAuthenticator authenticator) throws RemoteException {
        return contextId == null ? !authenticator.isMasterAuthenticationDisabled() : !authenticator.isContextAuthenticationDisabled();
    }

    @Override
    protected void administrativeAuth(String login, String password, CommandLine cmd, RemoteAuthenticator authenticator) throws RemoteException {
        if (contextId == null) {
            authenticator.doAuthentication(login, password);
        } else {
            authenticator.doAuthentication(login, password, contextId.intValue());
        }
    }

    @Override
    protected void addOptions(Options options) {
        OptionGroup optionGroup = new OptionGroup();
        optionGroup.addOption(createArgumentOption(OPT_CONFIGURATION_SHORT, OPT_CONFIGURATION_LONG, "userConfiguration", "Outputs the configuration associated with the given user. Filter by providing a pattern of the property.", true));
        optionGroup.addOption(createSwitch(OPT_CAPABILITIES_SHORT, OPT_CAPABILITIES_LONG, "Outputs the capabilities associated with the given user.", true));
        options.addOptionGroup(optionGroup);

        options.addOption(createArgumentOption(OPT_CONTEXT_SHORT, OPT_CONTEXT_LONG, "contextId", "A valid context identifier", true));
        options.addOption(createArgumentOption(OPT_USER_SHORT, OPT_USER_LONG, "userId", "A valid user identifier", true));
    }

    @Override
    protected void checkOptions(CommandLine cmd) {
        if (!cmd.hasOption(OPT_CONTEXT_SHORT)) {
            System.out.println("You must provide a context identifier.");
            printHelp();
            System.exit(1);
            return;
        }

        if (!cmd.hasOption(OPT_USER_SHORT)) {
            System.out.println("You must provide a user identifier.");
            printHelp();
            System.exit(1);
            return;
        }

        if (!cmd.hasOption(OPT_CAPABILITIES_SHORT) && !cmd.hasOption(OPT_CONFIGURATION_SHORT)) {
            System.out.println("Either user capabilities ('" + OPT_CAPABILITIES_SHORT + "') or user configuration ('" + OPT_CONFIGURATION_SHORT + "' <arg>) has to be added.");
            printHelp();
            System.exit(1);
            return;
        }

        String contextVal = cmd.getOptionValue(OPT_CONTEXT_SHORT);
        try {
            contextId = Integer.valueOf(contextVal.trim());
        } catch (@SuppressWarnings("unused") NumberFormatException e) {
            System.err.println("Cannot parse '" + contextVal + "' as a context id");
            printHelp();
            System.exit(1);
        }
    }

    @Override
    protected Void invoke(Options option, CommandLine cmd, String optRmiHostName) throws Exception {
        OXUserInterface oxUserInterface = getUserInterface();

        Context ctx = new Context(I(parseInt('c', 0, cmd, option)));
        User user = new User(parseInt('i', 0, cmd, option));
        Credentials credentials = new Credentials(cmd.getOptionValue('A'), cmd.getOptionValue('P'));

        if (cmd.hasOption(OPT_CONFIGURATION_SHORT)) {
            String searchPattern = cmd.getOptionValue(OPT_CONFIGURATION_LONG);
            handleConfigurationOption(oxUserInterface, ctx, user, credentials, searchPattern);
        }
        if (cmd.hasOption(OPT_CAPABILITIES_SHORT)) {
            handleCapabilitiesOption(oxUserInterface, ctx, user, credentials);
        }
        return null;
    }

    @Override
    protected Boolean requiresAdministrativePermission() {
        return null;
    }

    @Override
    protected String getFooter() {
        return "";
    }

    @Override
    protected String getName() {
        return "getuserconfigurationsource -c <contextId> -i <userId> [-a | -o <userConfiguration>] " + BASIC_CONTEXT_ONLY_ADMIN_USAGE;
    }

    ///////////////////////////////////////////////// HELPERS //////////////////////////////////////////////////

    /**
     * <code>"preferencePath"</code>
     */
    private static final String METADATA_PREFERENCE_PATH = "preferencePath";

    /**
     * Handles the <code>user-configuration</code> option
     *
     * @param oxUserInterface The {@link OXUserInterface}
     * @param ctx The context
     * @param user The user
     * @param credentials The credentials
     * @param searchPattern The search pattern
     * @throws Exception if an error is occurred
     */
    private void handleConfigurationOption(OXUserInterface oxUserInterface, Context ctx, User user, Credentials credentials, String searchPattern) throws Exception {
        List<UserProperty> userConfigurationSource = oxUserInterface.getUserConfigurationSource(ctx, user, searchPattern, credentials);
        if (userConfigurationSource.size() <= 0) {
            System.out.println("No property with pattern '" + searchPattern + "' found!");
            System.out.println();
            return;
        }

        StringBuilder builder = new StringBuilder(128);
        builder.append("Configuration found: ").append(NEWLINE);
        for (UserProperty property : userConfigurationSource) {
            builder.append(property.toString()).append(NEWLINE);
            if (property.getName() != null && property.getName().indexOf("//") > 0) {
                // Assume a UI property delivered via JSlob interface; e.g. "plugins/portal/myclient//linkTo/URL"
                if (property.getMetadata() == null || property.getMetadata().containsKey(METADATA_PREFERENCE_PATH) == false) {
                    builder.append("  (This configuration property has no effect when read from the App Suite UI, as there is no entry for it in `/opt/open-xchange/etc/settings/` directory.)").append(NEWLINE);
                }
            }
        }
        System.out.println(builder.toString());
    }

    /**
     * Handles the <code>user-capabilities</code> option
     *
     * @param oxUserInterface The {@link OXUserInterface}
     * @param ctx The context
     * @param user The user
     * @param credentials The credentials
     * @throws Exception if an error is occurred
     */
    private void handleCapabilitiesOption(OXUserInterface oxUserInterface, Context ctx, User user, Credentials credentials) throws Exception {
        Map<String, Map<String, Set<String>>> userCapabilitiesSource = oxUserInterface.getUserCapabilitiesSource(ctx, user, credentials);
        if (userCapabilitiesSource.size() <= 0) {
            System.out.println("Not able to retrieve capabilities.");
            System.out.println();
            return;
        }
        List<CapabilitySource> capabilitySources = new ArrayList<>(4);
        for (CapabilitySourceEnum cs : CapabilitySourceEnum.values()) {
            capabilitySources.add(new CapabilitySource(cs, userCapabilitiesSource.get(cs.getName()).get(CapabilitySource.GRANTED_KEY), userCapabilitiesSource.get(cs.getName()).get(CapabilitySource.DENIED_KEY)));
        }

        Collections.sort(capabilitySources, new CapabilitiesComparator());

        StringBuilder builder = new StringBuilder(128);
        Set<String> allowed = new TreeSet<>();
        for (CapabilitySource source : capabilitySources) {
            builder.append("Source: ").append(source.getSource().getName()).append(NEWLINE);

            builder.append("-- granted:").append(source.getGrantedCapabilities().toString()).append(NEWLINE);
            for (String granted : source.getGrantedCapabilities()) {
                allowed.add(granted);
            }
            builder.append("-- denied:").append(source.getDeniedCapabilities().toString()).append(NEWLINE);
            for (String denied : source.getDeniedCapabilities()) {
                allowed.remove(denied);
            }
            builder.append(NEWLINE);
        }
        System.out.println(builder.toString());
        System.out.println("Granted capabilities (lowest -> highest priority; permissions -> configuration -> provisioning -> programmatic): ");
        System.out.println(allowed.toString());
    }

    /**
     * Returns the {@link OXUserInterface}
     *
     * @return the {@link OXUserInterface}
     * @throws Exception if an error is occurred
     */
    private final OXUserInterface getUserInterface() throws Exception {
        return OXUserInterface.class.cast(getRmiStub(OXUserInterface.RMI_NAME));
    }
}
