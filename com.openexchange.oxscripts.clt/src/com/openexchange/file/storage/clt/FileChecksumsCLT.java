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

import java.util.List;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import com.openexchange.auth.mbean.AuthenticatorMBean;
import com.openexchange.cli.AbstractMBeanCLI;
import com.openexchange.groupware.infostore.mbean.FileChecksumsMBean;

/**
 * {@link FileChecksumsCLT}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class FileChecksumsCLT extends AbstractMBeanCLI<Void> {

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
    protected void administrativeAuth(String login, String password, CommandLine cmd, AuthenticatorMBean authenticator) throws MBeanException {
        if (cmd.hasOption('c')) {
            authenticator.doAuthentication(login, password, parseInt('c', 0, cmd, options));
        } else {
            authenticator.doAuthentication(login, password);
        }
    }

    @Override
    protected void addOptions(Options options) {
        options.addOption("c", "context", true, "The identifier of the context to determine/calculate missing checksums in");
        options.addOption("d", "database", true, "The database pool identifier to determine/calculate missing checksums in");
        options.addOption("C", "calculate", false, "Calculate and store missing checksums (if not specified, files with missing checksums are printed out only)");
    }

    @Override
    protected Void invoke(Options option, CommandLine cmd, MBeanServerConnection mbsc) throws Exception {
        FileChecksumsMBean fileChecksumsMBean = getMBean(mbsc, FileChecksumsMBean.class, FileChecksumsMBean.DOMAIN);
        int contextId = parseInt('c', 0, cmd, option);
        int databaseId = parseInt('d', 0, cmd, option);
        boolean calculate = cmd.hasOption('C');
        List<String> result;
        if (calculate) {
            if (0 < contextId) {
                result = fileChecksumsMBean.calculateMissingChecksumsInContext(contextId);
            } else if (0 < databaseId) {
                result = fileChecksumsMBean.calculateMissingChecksumsInDatabase(databaseId);
            } else {
                checkOptions(cmd, option);
                return null;
            }
        } else {
            if (0 < contextId) {
                result = fileChecksumsMBean.listFilesWithoutChecksumInContext(contextId);
            } else if (0 < databaseId) {
                result = fileChecksumsMBean.listFilesWithoutChecksumInDatabase(databaseId);
            } else {
                checkOptions(cmd, option);
                return null;
            }
        }
        if (result.isEmpty()) {
            System.out.println("No files with missing checksums found.");
        } else {
            if (calculate) {
                System.out.println("Missing file checksums calculated for " +  result.size() + " files:" + System.lineSeparator());
            } else {
                System.out.println("Missing file checksums detected for " +  result.size() + " files:" + System.lineSeparator());
            }
            for (String item : result) {
                System.out.println("  " + item);
            }
        }
        return null;
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
        return "Command-line tool to calculate missing file checksums";
    }

    @Override
    protected String getName() {
        return "calculatefilechecksums";
    }

}
