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

package com.openexchange.sessiond.impl.clt;

import java.rmi.RemoteException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import com.openexchange.auth.rmi.RemoteAuthenticator;
import com.openexchange.cli.AbstractRmiCLI;
import com.openexchange.sessiond.rmi.SessiondRMIService;

/**
 * {@link CloseSessionsCLT} - Command-Line access clear all sessions belonging to a given context.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
// TODO: Maybe enhance the command line tool over functionality that 
//       clears global context sessions as well as clearing the session storage...
public final class CloseSessionsCLT extends AbstractRmiCLI<Void> {

    private static final String SYNTAX = "closesessions [-c <contextId>] [-u <userId>] [-A <masterAdmin | contextAdmin> -P <masterAdminPassword | contextAdminPassword> [-p <RMI-Port>] [-s <RMI-Server]] | [-h]";

    private int contextId;
    private int userId;

    /**
     * Entry point
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new CloseSessionsCLT().execute(args);
    }

    // ------------------------------------------------------------------------------------ //

    /**
     * Initializes a new {@link CloseSessionsCLT}.
     */
    private CloseSessionsCLT() {
        super();
    }

    @Override
    protected void checkOptions(CommandLine cmd) {
        if (false == cmd.hasOption('c')) {
            System.err.println("Missing context identifier.");
            printHelp(options);
            System.exit(1);
        }

        contextId = parseInt('c', -1, cmd, options);
        if (cmd.hasOption('u')) {
            userId = parseInt('u', -1, cmd, options);
        }
    }

    @Override
    protected void addOptions(Options options) {
        options.addOption(createOption("c", "context", true, "A valid context identifier", true));
        options.addOption(createOption("u", "user", true, "A valid user identifier", false));
    }

    @Override
    protected boolean requiresAdministrativePermission() {
        return true;
    }

    @Override
    protected String getFooter() {
        return null;
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
        authenticator.doAuthentication(login, password);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractRmiCLI#invoke(org.apache.commons.cli.Options, org.apache.commons.cli.CommandLine, java.lang.String)
     */
    @Override
    protected Void invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {
        SessiondRMIService rmiService = getRmiStub(optRmiHostName, SessiondRMIService.RMI_NAME);
        if (userId > 0) {
            rmiService.clearUserSessions(Integer.valueOf(contextId), Integer.valueOf(userId));
            System.out.println("Cleared sessions for user " + userId + " in context " + contextId);
            return null;
        }
        rmiService.clearContextSessions(Integer.valueOf(contextId));
        System.out.println("Cleared sessions for context " + contextId);
        return null;
    }
}
