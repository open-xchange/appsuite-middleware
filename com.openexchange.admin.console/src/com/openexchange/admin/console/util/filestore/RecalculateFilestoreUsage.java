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
package com.openexchange.admin.console.util.filestore;

import java.net.URI;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.RecalculationScope;
import com.openexchange.auth.rmi.RemoteAuthenticator;
import com.openexchange.cli.AbstractRmiCLI;

/**
 *
 * {@link RecalculateFilestoreUsage}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class RecalculateFilestoreUsage extends AbstractRmiCLI<List<URI>> {

    private static final String OPT_USER_SHORT = "u";
    private static final String OPT_USER_LONG = "userid";
    private static final String OPT_CONTEXT_SHORT = "c";
    private static final String OPT_CONTEXT_LONG = "context";
    private static final String OPT_ALL_LONG = "all";

    public static void main(final String args[]) {
        List<URI> results = new RecalculateFilestoreUsage().execute(args);
        // display results
        System.out.println("The following filestores have been recalculated:");
        for(URI uri:results){
            System.out.println(uri.toASCIIString());
        }
    }

    @Override
    protected void administrativeAuth(String login, String password, CommandLine cmd, RemoteAuthenticator authenticator) throws RemoteException {
        authenticator.doAuthentication(login, password);
    }

    @Override
    protected void addOptions(Options options) {
        options.addOption(new Option(OPT_CONTEXT_SHORT, OPT_CONTEXT_LONG, true, "The context ID for that the file store usage shall be recalculated. "+
                                                                                "If a user ID is also set, the according user file store is "+
                                                                                "affected."));
        options.addOption(new Option(OPT_USER_SHORT, OPT_USER_LONG, true, "The user ID for which the user file store usage shall be recalculated. "+
                                                                          "A value of 'all' recalculates the usages for all user file stores in "+
                                                                          "the given context."));
        options.addOption("z", OPT_ALL_LONG, true,  "If all file store usages for the given scope shall be recalculated. "+
                                                     "Scope can be either set to 'all', 'context' or 'user'. If set "+
                                                     "to 'all', all usages of all context and user file stores are recalculated. "+
                                                     "Otherwise only context or user file store usages are recalculated. "+
                                                     "Cannot be used in conjunction with '-c' or '-u'.");
    }

    @Override
    protected List<URI> invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {
        try {

            boolean hasAllOption = false;
            String contextId = cmd.getOptionValue(OPT_CONTEXT_SHORT);
            String userId = cmd.getOptionValue(OPT_USER_SHORT);
            if(cmd.hasOption(OPT_ALL_LONG)){
                hasAllOption=true;
            }
            String scope = cmd.getOptionValue(OPT_ALL_LONG);
            final OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(RMI_HOSTNAME +OXUtilInterface.RMI_NAME);
            if(hasAllOption){
                return oxutil.recalculateFilestoreUsage(RecalculationScope.getScopeByName(scope));
            } else {
                return Collections.singletonList(oxutil.recalculateFilestoreUsage(Integer.valueOf(contextId), Integer.valueOf(userId)));
            }
        } catch (final Exception e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        }
        return null;
    }

    @Override
    protected boolean requiresAdministrativePermission() {
        return true;
    }

    @Override
    protected void checkOptions(CommandLine cmd) {
        if (!cmd.hasOption(OPT_CONTEXT_SHORT) && !cmd.hasOption(OPT_ALL_LONG)) {
            System.out.println("You must provide either a context identifier or the all parameter.");
            printHelp();
            System.exit(-1);
            return;
        }

    }

    @Override
    protected String getFooter() {
        return "The command-line tool to recalculate the usage of filestores";
    }

    @Override
    protected String getName() {
        return "recalculatefilestoreusage [-c <context-id> [-u <user-id>] | --all <scope>]";
    }
}
