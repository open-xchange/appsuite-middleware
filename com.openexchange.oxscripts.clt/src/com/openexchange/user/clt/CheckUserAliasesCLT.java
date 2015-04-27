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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.user.clt;

import java.util.List;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import com.openexchange.auth.mbean.AuthenticatorMBean;
import com.openexchange.cli.AbstractMBeanCLI;
import com.openexchange.contact.storage.rdb.mbean.ContactStorageMBean;

/**
 * {@link CheckUserAliasesCLT}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CheckUserAliasesCLT extends AbstractMBeanCLI<Void> {

    /**
     * The main method
     *
     * @param args The arguments
     */
    public static void main(String[] args) {
        new CheckUserAliasesCLT().execute(args);
    }

    // ------------------------------------------------------------------------------------ //

    /**
     * Initializes a new {@link CheckUserAliasesCLT}.
     */
    private CheckUserAliasesCLT() {
        super();
    }

    @Override
    protected void administrativeAuth(String login, String password, CommandLine cmd, AuthenticatorMBean authenticator) throws MBeanException {
        authenticator.doAuthentication(login, password);
    }

    @Override
    protected Void invoke(Options option, CommandLine cmd, MBeanServerConnection mbsc) throws Exception {
        int contextId = -1;
        if (cmd.hasOption('c')) {
            String optionValue = cmd.getOptionValue('c');
            try {
                contextId = Integer.parseInt(optionValue.trim());
            } catch (NumberFormatException e) {
                System.err.println("Context identifier parameter is not a number: " + optionValue);
                printHelp(options);
                System.exit(1);
                return null;
            }
        }

        boolean dryRun = cmd.hasOption('d');

        ContactStorageMBean mBean = getMBean(mbsc, ContactStorageMBean.class, "com.openexchange.contact");
        List<List<Integer>> userIdsPerContext = mBean.checkUserAliases(contextId, dryRun);
        if (userIdsPerContext.isEmpty()) {
            System.out.println("No incomplete aliases found" + (contextId > 0 ? " in context " + contextId : ""));
        } else {
            System.out.println("Found " + (dryRun ? "" : "and corrected ") + "the incomplete aliases of " + userIdsPerContext.size() + " users" + (contextId > 0 ? " in context " + contextId : ""));
            System.out.println("Affected users (per context):");
            for (List<Integer> userIds : userIdsPerContext) {
                // First position is always the context identifier
                System.out.print(userIds.get(0) + ": ");
                System.out.print(userIds.get(1)); // First user identifier
                int size = userIds.size();
                if (size > 1) {
                    for (int i = 2; i < size; i++) {
                        System.out.print(", ");
                        System.out.print(userIds.get(i));
                    }
                }
                System.out.println();
            }
        }
        return null;
    }

    @Override
    protected boolean requiresAdministrativePermission() {
        return true;
    }

    @Override
    protected void checkOptions(CommandLine cmd) {
        // No more options to check
    }

    @Override
    protected void addOptions(Options options) {
        options.addOption("c", "context", true, "An optional context identifier to only process the users of that context");
        options.addOption("d", "dryrun", false, "Signals if aliases should be actually processed or not.");
    }

    @Override
    protected String getFooter() {
        return "Checks for all users if the set of aliases contain primaryMail, Email1 and defaultSenderAddress.";
    }

    @Override
    protected String getName() {
        return "checkuseraliases";
    }

}
