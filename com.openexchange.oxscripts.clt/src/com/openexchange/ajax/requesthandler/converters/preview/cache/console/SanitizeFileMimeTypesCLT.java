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

package com.openexchange.ajax.requesthandler.converters.preview.cache.console;

import java.rmi.RemoteException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import com.openexchange.ajax.requesthandler.converters.preview.cache.rmi.ResourceCacheRMIService;
import com.openexchange.auth.rmi.RemoteAuthenticator;
import com.openexchange.cli.AbstractRmiCLI;

/**
 * {@link SanitizeFileMimeTypesCLT} - Serves <code>sanitizefilemimetypes</code> command-line tool.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public final class SanitizeFileMimeTypesCLT extends AbstractRmiCLI<Void> {

    private static final String SYNTAX = "sanitizefilemimetypes [[[-c <contextId>] | [-a]] [-i <invalidIds>] " + BASIC_CONTEXT_ADMIN_USAGE;
    private static final String FOOTER = "The options -c/--context and -a/--all are mutually exclusive.";

    private Integer contextId;
    private String invalids;

    /**
     * Prevent instantiation.
     */
    private SanitizeFileMimeTypesCLT() {
        super();
    }

    /**
     * Main method for starting from console.
     *
     * @param args program arguments
     */
    public static void main(String[] args) {
        new SanitizeFileMimeTypesCLT().execute(args);
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
        OptionGroup group = new OptionGroup();
        group.addOption(createArgumentOption("c", "context", "contextId", "Required. The context identifier", true));
        group.addOption(createSwitch("a", "all", "Required. The flag to signal that contexts shall be processed. Hence option -c/--context is then obsolete.", true));
        options.addOptionGroup(group);
        options.addOption(createArgumentOption("i", "invalids", "mimetype_1,mimetype_2,...,mimetype_n", "An optional comma-separated list of those MIME types that should be considered as broken/corrupt. Default is \"application/force-download, application/x-download, application/$suffix\"", false));
    }

    @Override
    protected Void invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {
        boolean error = true;
        try {
            ResourceCacheRMIService rmiService = getRmiStub(optRmiHostName, ResourceCacheRMIService.RMI_NAME);
            if (null == contextId) {
                System.out.println(rmiService.sanitizeMimeTypesInDatabaseFor(-1, invalids));
            } else {
                System.out.println(rmiService.sanitizeMimeTypesInDatabaseFor(contextId.intValue(), invalids));
            }
            error = false;
        } catch (RemoteException e) {
            final String errMsg = e.getMessage();
            System.out.println(errMsg == null ? "An error occurred." : errMsg);
        } catch (Exception e) {
            final String errMsg = e.getMessage();
            System.out.println(errMsg == null ? "An error occurred." : errMsg);
        } finally {
            if (error) {
                System.exit(1);
            }
        }
        return null;
    }

    @Override
    protected Boolean requiresAdministrativePermission() {
        return Boolean.TRUE;
    }

    @Override
    protected void checkOptions(CommandLine cmd) {
        if (cmd.hasOption('a')) {
            contextId = null;
            return;
        }

        if (cmd.hasOption('c')) {
            String contextVal = cmd.getOptionValue('c');
            try {
                contextId = Integer.valueOf(contextVal.trim());
            } catch (NumberFormatException e) {
                System.err.println("Cannot parse '" + contextVal + "' as a context id");
                printHelp();
                System.exit(1);
            }
            return;
        }

        if (cmd.hasOption('i')) {
            invalids = cmd.getOptionValue('i');
            invalids = invalids.trim();
            if (invalids.startsWith("\"") && invalids.endsWith("\"")) {
                invalids = invalids.substring(1, invalids.length() - 1);
                invalids = invalids.trim();
            }
        }

        System.out.println("Either parameter 'context' or parameter 'all' is required.");
        printHelp();
        System.exit(1);
    }

    @Override
    protected String getFooter() {
        return FOOTER;
    }

    @Override
    protected String getName() {
        return SYNTAX;
    }

}
