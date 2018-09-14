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

package com.openexchange.file.storage.clt;

import java.rmi.RemoteException;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import com.openexchange.auth.rmi.RemoteAuthenticator;
import com.openexchange.cli.AbstractRmiCLI;
import com.openexchange.groupware.infostore.rmi.FileChecksumsRMIService;

/**
 * {@link FileChecksumsCLT}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.0
 */
public class FileChecksumsCLT extends AbstractRmiCLI<Void> {

    private static final String SYNTAX = "calculatefilechecksums [-d <databaseId> | -c <contextId>] [-C] [-A <masterAdmin> -P <masterAdminPassword> [-p <RMI-Port>] [-s <RMI-Server]] | [-h]";
    private static final String FOOTER = "Command-line tool to calculate missing file checksums";

    public static void main(String[] args) {
        new FileChecksumsCLT().execute(args);
    }

    /**
     * Initializes a new {@link FileChecksumsCLT}.
     */
    public FileChecksumsCLT() {
        super();
    }

    @Override
    protected void addOptions(Options options) {
        options.addOption("c", "context", true, "The identifier of the context to determine/calculate missing checksums in");
        options.addOption("d", "database", true, "The database pool identifier to determine/calculate missing checksums in");
        options.addOption("C", "calculate", false, "Calculate and store missing checksums (if not specified, files with missing checksums are printed out only)");
    }

    @Override
    protected void checkOptions(CommandLine cmd) {
        checkOptions(cmd, options);
    }

    @Override
    protected void checkOptions(CommandLine cmd, Options options) {
        if ((false == cmd.hasOption('c') && false == cmd.hasOption('d')) || (cmd.hasOption('c') && cmd.hasOption('d'))) {
            System.out.println("You must either provide a context or database identifier.");
            if (null != options) {
                printHelp(options);
            }
            System.exit(-1);
            return;
        }
    }

    @Override
    protected boolean requiresAdministrativePermission() {
        return true;
    }

    @Override
    protected String getFooter() {
        return FOOTER;
    }

    @Override
    protected String getName() {
        return SYNTAX;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractRmiCLI#administrativeAuth(java.lang.String, java.lang.String, org.apache.commons.cli.CommandLine, com.openexchange.auth.rmi.RemoteAuthenticator)
     */
    @Override
    protected void administrativeAuth(String login, String password, CommandLine cmd, RemoteAuthenticator authenticator) throws RemoteException {
        if (cmd.hasOption('c')) {
            authenticator.doAuthentication(login, password, parseInt('c', 0, cmd, options));
        } else {
            authenticator.doAuthentication(login, password);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractRmiCLI#invoke(org.apache.commons.cli.Options, org.apache.commons.cli.CommandLine, java.lang.String)
     */
    @Override
    protected Void invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {
        FileChecksumsRMIService rmiService = getRmiStub(optRmiHostName, FileChecksumsRMIService.RMI_NAME);
        int contextId = parseInt('c', 0, cmd, options);
        int databaseId = parseInt('d', 0, cmd, options);
        boolean calculate = cmd.hasOption('C');
        List<String> result;
        if (calculate) {
            if (0 < contextId) {
                result = rmiService.calculateMissingChecksumsInContext(contextId);
            } else if (0 < databaseId) {
                result = rmiService.calculateMissingChecksumsInDatabase(databaseId);
            } else {
                checkOptions(cmd, options);
                return null;
            }
        } else {
            if (0 < contextId) {
                result = rmiService.listFilesWithoutChecksumInContext(contextId);
            } else if (0 < databaseId) {
                result = rmiService.listFilesWithoutChecksumInDatabase(databaseId);
            } else {
                checkOptions(cmd, options);
                return null;
            }
        }
        if (result.isEmpty()) {
            System.out.println("No files with missing checksums found.");
        } else {
            if (calculate) {
                System.out.println("Missing file checksums calculated for " + result.size() + " files:" + System.lineSeparator());
            } else {
                System.out.println("Missing file checksums detected for " + result.size() + " files:" + System.lineSeparator());
            }
            for (String item : result) {
                System.out.println("  " + item);
            }
        }
        return null;
    }
}
