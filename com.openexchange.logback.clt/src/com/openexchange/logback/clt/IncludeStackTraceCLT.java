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

package com.openexchange.logback.clt;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import com.openexchange.logging.rmi.LogbackConfigurationRMIService;

/**
 * {@link IncludeStackTraceCLT}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class IncludeStackTraceCLT extends AbstractLogbackConfigurationAdministrativeCLI<Void> {

    private static final String SYNTAX = "includestacktrace [-e | -d] [-u <userid>] [-c <contextid>] " + BASIC_MASTER_ADMIN_USAGE;
    private static final String FOOTER = "The flags -e and -d are mutually exclusive.";

    /**
     * Entry point
     * 
     * @param args The command line arguments
     */
    public static void main(String[] args) {
        new IncludeStackTraceCLT().execute(args);
    }

    /**
     * Initialises a new {@link IncludeStackTraceCLT}.
     */
    private IncludeStackTraceCLT() {
        super(SYNTAX, FOOTER);
    }

    @Override
    protected void addOptions(Options options) {
        Option enable = createSwitch("e", "enable", "Flag to enable to include stack traces in HTTP-API JSON responses", true);
        Option disbale = createSwitch("d", "disable", "Flag to disable to include stack traces in HTTP-API JSON responses", true);
        options.addOption(createArgumentOption("c", "context", "contextId", "The context identifier", true));
        options.addOption(createArgumentOption("u", "user", "userId", "The user identifier", true));

        OptionGroup og = new OptionGroup();
        og.addOption(enable).addOption(disbale);

        options.addOptionGroup(og);
    }

    @Override
    protected Void invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {
        boolean enable = cmd.hasOption("e");
        int contextId = parseInt('c', -1, cmd, options);
        int userId = parseInt('u', -1, cmd, options);
        if (contextId <= 0 || userId <= 0) {
            System.err.println("Invalid context and/or user identifier specified.");
            printHelp();
            System.exit(1);
        }

        LogbackConfigurationRMIService rmiService = getRmiStub(optRmiHostName, LogbackConfigurationRMIService.RMI_NAME);
        rmiService.includeStackTraceForUser(contextId, userId, enable);
        System.out.println("Including stack trace information successfully " + (enable ? "enabled" : "disabled") + " for user " + userId + " in context " + contextId);
        return null;
    }

    @Override
    protected void checkOptions(CommandLine cmd) {
        // nothing to check
    }
}
