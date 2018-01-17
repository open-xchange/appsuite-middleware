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

package com.openexchange.admin.multifactor;

import java.util.Arrays;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.auth.rmi.RemoteAuthenticator;
import com.openexchange.cli.AbstractRmiCLI;
import com.openexchange.multifactor.rmi.MultifactorDeviceResult;
import com.openexchange.multifactor.rmi.MultifactorManagementRemoteService;

/**
 * {@link ListMultifactorDevices} - a command line tool for listing multifacor devices
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public class ListMultifactorDevices extends AbstractRmiCLI<Void> {

    private static final String OPT_USER_SHORT = "i";
    private static final String OPT_USER_LONG = "userid";
    private static final String OPT_CONTEXT_SHORT = "c";
    private static final String OPT_CONTEXT_LONG = "context";
    private final static String ADMIN_USER_SHORT_NAME = "A";
    private final static String ADMIN_PASSWORD_SHORT_NAME = "P";

    public static void main(String[] args){
        new ListMultifactorDevices().execute(args);
    }

    private void printDevices(MultifactorDeviceResult...deviceResults) {
        Arrays.stream(deviceResults).forEach(d -> printDevice(d));
    }

    private void printDevice(MultifactorDeviceResult deviceResult) {
        System.out.println(String.format("ID: %s\tProvider: %s\tName: %s\t(%s%s)",
            deviceResult.getId(),
            deviceResult.getProviderName(),
            deviceResult.getName(),
            deviceResult.isEnabled() ? "enabled" : "disabled",
            deviceResult.isBackupDevice() ? ", backup device" : ""
            ));
    }

    @Override
    protected void administrativeAuth(String login, String password, CommandLine cmd, RemoteAuthenticator authenticator) {
        // no-op
    }

    @Override
    protected void addOptions(Options options) {
        options.addOption(OPT_CONTEXT_SHORT, OPT_CONTEXT_LONG, true, "A valid context identifier");
        options.addOption(OPT_USER_SHORT, OPT_USER_LONG, true, "A valid user identifier");
    }

    @Override
    protected Void invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {

        final int contextId = parseInt(OPT_CONTEXT_LONG, 0, cmd, options);
        final int userId = parseInt(OPT_USER_LONG, 0, cmd, options);
        String username = cmd.getOptionValue(ADMIN_USER_SHORT_NAME);
        String password = cmd.getOptionValue(ADMIN_PASSWORD_SHORT_NAME);

        MultifactorManagementRemoteService rmiService = getRmiStub(optRmiHostName, MultifactorManagementRemoteService.RMI_NAME);
        MultifactorDeviceResult[] multifactorDevices = rmiService.getMultifactorDevices(contextId, userId, new Credentials(username, password));
        printDevices(multifactorDevices);
        return null;
    }

    @Override
    protected boolean requiresAdministrativePermission() {
        return true;
    }

    @Override
    protected void checkOptions(CommandLine cmd) {
        if (!cmd.hasOption(OPT_CONTEXT_SHORT)) {
            System.out.println("You must provide a context identifier.");
            printHelp();
            System.exit(-1);
        }

        if (!cmd.hasOption(OPT_USER_SHORT)) {
            System.out.println("You must provide a user identifier.");
            printHelp();
            System.exit(-1);
        }
    }

    @Override
    protected String getFooter() {
        return "The command-line tool for listing multi-factor devices.";
    }

    @Override
    protected String getName() {
        return "listmultifactordevices";
    }
}