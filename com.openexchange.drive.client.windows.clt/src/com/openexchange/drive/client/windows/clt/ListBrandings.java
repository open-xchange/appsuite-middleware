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

package com.openexchange.drive.client.windows.clt;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import com.openexchange.cli.AbstractRmiCLI;
import com.openexchange.drive.client.windows.service.rmi.BrandingConfigurationRemote;
import com.openexchange.auth.rmi.RemoteAuthenticator;

/**
 * {@link ListBrandings}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.0
 */
public class ListBrandings extends AbstractRmiCLI<Void> {

    private final static String VALIDATE_LONG = "validate";
    private final static String VALIDATE_SHORT = "v";

    private final static String INVALID_ONLY_LONG = "invalid_only";
    private final static String INVALID_ONLY_SHORT = "o";

    /**
     * @param args
     */
    public static void main(String[] args) {
        new ListBrandings().execute(args);
    }

    @Override
    protected void administrativeAuth(String login, String password, CommandLine cmd, RemoteAuthenticator authenticator) throws RemoteException {
        authenticator.doAuthentication(login, password);
    }

    @Override
    protected void addOptions(Options options) {
        options.addOption(VALIDATE_SHORT, VALIDATE_LONG, false, "If defined, the brandings will be validated and only valid brandings will be returned. This validation verifies if all required files are present.");
        options.addOption(INVALID_ONLY_SHORT, INVALID_ONLY_LONG, false, "Retrieves only invalid brandings.");
    }

    @Override
    protected Void invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {
        boolean validate = cmd.hasOption(VALIDATE_LONG);
        boolean invalid_only = cmd.hasOption(INVALID_ONLY_LONG);
        validate = invalid_only ? true : validate;
        BrandingConfigurationRemote remote = (BrandingConfigurationRemote) Naming.lookup(RMI_HOSTNAME + BrandingConfigurationRemote.RMI_NAME);
        List<String> brands = remote.getBrandings(validate, invalid_only);

        if (invalid_only) {
            System.out.println("The following brands are invalid:");
        } else {
            if (validate) {
                System.out.println("The following brands are available and valid:");
            } else {
                System.out.println("The following brands are available:");
            }

        }
        for (String brand : brands) {
            System.out.println("\t -" + brand);
        }
        return null;
    }

    @Override
    protected boolean requiresAdministrativePermission() {
        return true;
    }

    @Override
    protected void checkOptions(CommandLine cmd) {
        // Nothing to check
    }

    @Override
    protected String getFooter() {
        return "The command-line tool to list the available windows drive clients";
    }

    @Override
    protected String getName() {
        return "listdriveclients";
    }
}
