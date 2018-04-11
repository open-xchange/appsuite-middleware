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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.diagnostic;

import java.rmi.RemoteException;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import com.openexchange.auth.rmi.RemoteAuthenticator;
import com.openexchange.cli.AbstractRmiCLI;
import com.openexchange.diagnostics.rmi.RemoteDiagnosticService;

/**
 * {@link DiagnosticsCLT}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class DiagnosticsCLT extends AbstractRmiCLI<Void> {

    private static final String CLT_NAME = "diagnostics [-c] | [-v] | [-r] | [-a]";

    /**
     * Entry point
     * 
     * @param args The arguments of the command line tool
     */
    public static void main(String[] args) {
        new DiagnosticsCLT().execute(args);
    }

    /**
     * Initialises a new {@link DiagnosticsCLT}.
     */
    public DiagnosticsCLT() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractRmiCLI#administrativeAuth(java.lang.String, java.lang.String, org.apache.commons.cli.CommandLine, com.openexchange.auth.rmi.RemoteAuthenticator)
     */
    @Override
    protected void administrativeAuth(String login, String password, CommandLine cmd, RemoteAuthenticator authenticator) throws RemoteException {
        // no-op
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractRmiCLI#addOptions(org.apache.commons.cli.Options)
     */
    @Override
    protected void addOptions(Options options) {
        options.addOption(new Option("c", "cipher-suites", false, "A list with all supported cipher suites of this JVM."));
        options.addOption(new Option("v", "version", false, "The server's version."));
        options.addOption(new Option("r", "protocols", false, "A list with all supported SSL protocols of this JVM."));
        options.addOption(new Option("a", "charsets", false, "A list with all supported charsets of this JVM."));
        options.addOption(new Option("al", "charsets-long", false, "A long list with all supported charsets of this JVM. Along the charsets their aliases will also be listed as a comma separated list. The name of each charset will always be first."));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractRmiCLI#invoke(org.apache.commons.cli.Options, org.apache.commons.cli.CommandLine, java.lang.String)
     */
    @Override
    protected Void invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {
        RemoteDiagnosticService diagnosticService = getRmiStub(optRmiHostName, RemoteDiagnosticService.RMI_NAME);
        if (cmd.hasOption("c")) {
            System.out.println("Available Cipher Suites");
            System.out.println("-----------------------");
            printList(diagnosticService.getCipherSuites());
        } else if (cmd.hasOption("v")) {
            System.out.println("Server version: " + diagnosticService.getVersion());
        } else if (cmd.hasOption("r")) {
            System.out.println("Available SSL Protocols");
            System.out.println("-----------------------");
            printList(diagnosticService.getProtocols());
        } else if (cmd.hasOption("a")) {
            System.out.println("Available Charsets");
            System.out.println("------------------");
            printList(diagnosticService.getCharsets(false));
        } else if (cmd.hasOption("al")) {
            System.out.println("Available Charsets");
            System.out.println("------------------");
            printList(diagnosticService.getCharsets(true));
        } else {
            System.err.println("Missing parameter");
            printHelp();
        }
        return null;
    }

    /**
     * Prints the specified {@link List} to the standard output
     * 
     * @param list The {@link List} to print
     */
    private void printList(List<String> list) {
        for (String element : list) {
            System.out.println(element);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractAdministrativeCLI#requiresAdministrativePermission()
     */
    @Override
    protected boolean requiresAdministrativePermission() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractCLI#checkOptions(org.apache.commons.cli.CommandLine)
     */
    @Override
    protected void checkOptions(CommandLine cmd) {
        // no-op
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractCLI#getFooter()
     */
    @Override
    protected String getFooter() {
        return "Prints out diagnostic information about the JVM and the middleware";
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractCLI#getName()
     */
    @Override
    protected String getName() {
        return CLT_NAME;
    }
}
