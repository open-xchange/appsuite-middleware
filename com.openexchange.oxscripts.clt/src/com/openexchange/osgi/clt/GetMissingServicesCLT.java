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

package com.openexchange.osgi.clt;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import com.openexchange.auth.rmi.RemoteAuthenticator;
import com.openexchange.cli.AbstractRmiCLI;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.rmi.DeferredActivatorRMIService;

/**
 * {@link GetMissingServicesCLT} - Serves the <code>getmissingservices</code> command line tool
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class GetMissingServicesCLT extends AbstractRmiCLI<Void> {

    private static String SYNTAX = "getmissingservices [-n <bundleName>] -A <masterAdmin | contextAdmin> -P <masterAdminPassword | contextAdminPassword> [-p <RMI-Port>] [-s <RMI-Server>] [--responsetimeout <responseTimeout>] | [-h]";
    private static String FOOTER = "";

    private boolean testAll = true;
    private String bundleName;

    /**
     * Initializes a new {@link GetMissingServicesCLT}.
     */
    public GetMissingServicesCLT() {
        super();
    }

    /**
     * entry point
     *
     * @param args
     */
    public static void main(String[] args) {
        new GetMissingServicesCLT().execute(args);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.cli.AbstractRmiCLI#administrativeAuth(java.lang.String, java.lang.String, org.apache.commons.cli.CommandLine, com.openexchange.auth.rmi.RemoteAuthenticator)
     */
    @Override
    protected void administrativeAuth(String login, String password, CommandLine cmd, RemoteAuthenticator authenticator) throws RemoteException {
        authenticator.doAuthentication(login, password);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.cli.AbstractRmiCLI#addOptions(org.apache.commons.cli.Options)
     */
    @Override
    protected void addOptions(Options options) {
        options.addOption(createArgumentOption("n", "name", "bundleName", "The optional bundle's symbolic name", false));
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.cli.AbstractRmiCLI#invoke(org.apache.commons.cli.Options, org.apache.commons.cli.CommandLine, java.lang.String)
     */
    @Override
    protected Void invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {
        try {
            if (testAll) {
                listAllMissingServices(optRmiHostName);
            } else {
                listMissingServicesForBundle(optRmiHostName);
            }
        } catch (MalformedURLException e) {
            System.err.println("URL to connect to server is invalid: " + e.getMessage());
            System.exit(3);
        } catch (RemoteException e) {
            Throwable t = e.getCause();
            String message;
            if (null == t) {
                message = e.getMessage();
            } else {
                if ((t instanceof OXException)) {
                    OXException oxe = (OXException) t;
                    if ("CTX".equals(oxe.getPrefix())) {
                        message = "Cannot find bundle " + bundleName;
                    } else {
                        message = t.getMessage();
                    }
                } else {
                    message = t.getMessage();
                }
            }
            System.err.println(null == message ? "Unexpected error." : "Unexpected error: " + message);
            System.exit(3);
        } catch (RuntimeException e) {
            System.err.println("Problem in runtime: " + e.getMessage());
            printHelp();
            System.exit(3);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractCLI#checkOptions(org.apache.commons.cli.CommandLine)
     */
    @Override
    protected void checkOptions(CommandLine cmd) {
        if (cmd.hasOption('n')) {
            bundleName = cmd.getOptionValue('n');
            testAll = false;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.cli.AbstractCLI#getFooter()
     */
    @Override
    protected String getFooter() {
        return FOOTER;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.cli.AbstractCLI#getName()
     */
    @Override
    protected String getName() {
        return SYNTAX;
    }

    /**
     * Lists all missing services
     *
     * @param rmiHostName The optional remote hostname
     */
    private void listAllMissingServices(String rmiHostName) throws MalformedURLException, RemoteException, NotBoundException {
        DeferredActivatorRMIService rmiService = getRmiStub(rmiHostName, DeferredActivatorRMIService.RMI_NAME);
        Map<String, List<String>> allServices = rmiService.listAllMissingServices();
        if (allServices.isEmpty()) {
            System.out.println("No services missing");
            System.exit(0);
        }
        for (Entry<String, List<String>> bundle : allServices.entrySet()) {
            printMissingServicesForBundle(bundle.getKey(), bundle.getValue());
            System.out.println();
        }
        System.exit(2);
    }

    /**
     * Lists all missing services for the specified bundle
     *
     * @param optRmiHostName The optional remote hostname
     */
    private void listMissingServicesForBundle(String rmiHostName) throws MalformedURLException, RemoteException, NotBoundException {
        DeferredActivatorRMIService rmiService = getRmiStub(rmiHostName, DeferredActivatorRMIService.RMI_NAME);
        List<String> missingServices = rmiService.listMissingServices(bundleName);
        if (missingServices.isEmpty()) {
            System.out.println("No services missing for bundle " + bundleName);
            System.exit(0);
        }
        printMissingServicesForBundle(bundleName, missingServices);
        System.exit(2);
    }

    /**
     * Prints the missing services for the specified bundle
     *
     * @param bundleName The bundle's name
     * @param missingServices the missing services
     */
    private void printMissingServicesForBundle(String bundleName, List<String> missingServices) {
        System.out.println("Services missing for bundle " + bundleName + ":");
        StringBuilder sb = new StringBuilder();
        for (String o : missingServices) {
            sb.append(o.toString()).append(',');
        }
        sb.deleteCharAt(sb.length() - 1);
        System.out.println(sb.toString());
    }
}
