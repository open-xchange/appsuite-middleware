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

    private static final String SYNTAX = "includestacktrace [-e | -d] [-u <userid>] [-c <contextid>] -A <masterAdmin> -P <masterAdminPassword> [-p <RMI-Port>] [-s <RMI-Server] | [-h]";
    private static final String FOOTER = "\n\nThe flags -e and -d are mutually exclusive.";

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

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractRmiCLI#addOptions(org.apache.commons.cli.Options)
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractRmiCLI#invoke(org.apache.commons.cli.Options, org.apache.commons.cli.CommandLine, java.lang.String)
     */
    @Override
    protected Void invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {
        boolean enable = cmd.hasOption("e") ? true : false;
        int contextId = parseInt('c', -1, cmd, options);
        int userId = parseInt('u', -1, cmd, options);
        if (contextId <= 0 || userId <= 0) {
            System.err.println("Invalid context and/or user identifier specified.");
            printHelp();
        }

        LogbackConfigurationRMIService rmiService = getRmiStub(optRmiHostName, LogbackConfigurationRMIService.RMI_NAME);
        rmiService.includeStackTraceForUser(contextId, userId, enable);
        System.out.println("Including stack trace information successfully " + (enable ? "enabled" : "disabled") + " for user " + userId + " in context " + contextId);
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractCLI#checkOptions(org.apache.commons.cli.CommandLine)
     */
    @Override
    protected void checkOptions(CommandLine cmd) {
        // nothing to check
    }
}
