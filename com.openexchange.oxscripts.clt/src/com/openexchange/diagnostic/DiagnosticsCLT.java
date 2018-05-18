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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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

    private static final String CLT_NAME = "diagnostics [-c][-v][-r][-a|-al]";

    /**
     * {@link CommandLineOption} - Sums up all available command line options
     * and executes the relevant remote methods via the specified {@link RemoteDiagnosticService}
     */
    private enum CommandLineOption {
        c("Available Cipher Suites") {

            @Override
            List<String> executeWith(RemoteDiagnosticService service) throws RemoteException {
                return service.getCipherSuites();
            }
        },
        a("Available Charsets") {

            @Override
            List<String> executeWith(RemoteDiagnosticService service) throws RemoteException {
                return service.getCharsets(false);
            }
        },
        r("Available SSL Protocols") {

            @Override
            List<String> executeWith(RemoteDiagnosticService service) throws RemoteException {
                return service.getProtocols();
            }

        },
        v("Server version") {

            @Override
            List<String> executeWith(RemoteDiagnosticService service) throws RemoteException {
                return Collections.singletonList(service.getVersion());
            }

        },
        al("Available Charsets") {

            @Override
            List<String> executeWith(RemoteDiagnosticService service) throws RemoteException {
                return service.getCharsets(true);
            }

        };

        private final String header;

        /**
         * 
         * Initialises a new {@link CommandLineOption}.
         * 
         * @param header
         */
        private CommandLineOption(String header) {
            this.header = header;
        }

        /**
         * Executes this {@link CommandLineOption} with the specified {@link RemoteDiagnosticService}
         * 
         * @param service The {@link RemoteDiagnosticService} to use for execution
         * @return A {@link List} of {@link String}s
         * @throws RemoteException if an error is occurred
         */
        abstract List<String> executeWith(RemoteDiagnosticService service) throws RemoteException;

        /**
         * Gets the header
         *
         * @return The header
         */
        public String getHeader() {
            return header;
        }
    }

    private final Set<CommandLineOption> toExecute;

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
        toExecute = new HashSet<>(8);
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
        options.addOption(new Option("a", "charsets", false, "A list with all supported charsets of this JVM. This switch is mutually-exclusive with it's counter-part '-al'"));
        options.addOption(new Option("al", "charsets-long", false, "A long list with all supported charsets of this JVM. Along the charsets their aliases will also be listed as a comma separated list. The name of each charset will always be first. This switch is mutually-exclusive with it's counter-part '-a'"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractRmiCLI#invoke(org.apache.commons.cli.Options, org.apache.commons.cli.CommandLine, java.lang.String)
     */
    @Override
    protected Void invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {
        RemoteDiagnosticService diagnosticService = getRmiStub(optRmiHostName, RemoteDiagnosticService.RMI_NAME);
        if (toExecute.isEmpty()) {
            System.err.println("Missing parameter");
            printHelp();
        }

        Map<String, List<String>> lists = new HashMap<>(4);
        for (CommandLineOption clo : toExecute) {
            lists.put(clo.getHeader(), clo.executeWith(diagnosticService));
        }
        printLists(lists);

        return null;
    }

    /**
     * @param lists
     */
    private void printLists(Map<String, List<String>> lists) {
        for (Entry<String, List<String>> entry : lists.entrySet()) {
            printList(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Prints the specified {@link List} to the standard output
     * 
     * @param header The header of the list
     * @param list The {@link List} to print
     */
    private void printList(String header, List<String> list) {
        System.out.println(header);
        StringBuilder b = new StringBuilder(header.length());
        for (int c = 0; c < header.length(); c++) {
            b.append('-');
        }
        System.out.println(b.toString());
        for (String element : list) {
            System.out.println(element);
        }
        System.out.println();
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
        for (CommandLineOption clo : CommandLineOption.values()) {
            if (cmd.hasOption(clo.name())) {
                toExecute.add(clo);
            }
        }
        if (toExecute.contains(CommandLineOption.a) && toExecute.contains(CommandLineOption.al)) {
            System.err.println("The command line options '-a' and '-al' are mutually-exclusive. The option '-al' takes preference");
            toExecute.remove(CommandLineOption.a);
        }
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
