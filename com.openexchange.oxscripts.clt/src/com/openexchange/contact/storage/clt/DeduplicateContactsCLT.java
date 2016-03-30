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

package com.openexchange.contact.storage.clt;

import java.util.Arrays;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import com.openexchange.auth.mbean.AuthenticatorMBean;
import com.openexchange.cli.AbstractMBeanCLI;

/**
 * {@link DeduplicateContactsCLT}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DeduplicateContactsCLT extends AbstractMBeanCLI<Void> {

    public static void main(String[] args) {
        new DeduplicateContactsCLT().execute(args);
    }

    // ------------------------------------------------------------------------------------ //

    /**
     * Initializes a new {@link DeduplicateContactsCLT}.
     */
    private DeduplicateContactsCLT() {
        super();
    }

    @Override
    protected void checkOptions(CommandLine cmd) {
        // No more options to check
    }

    @Override
    protected void addOptions(Options options) {
        options.addOption("c", "context", true, "A valid context identifier");
        options.addOption("f", "folder", true, "A valid contact folder identifier");
        options.addOption("m", "max", true, "The maximum number of contacts to process, or 0 for no limit, defaults to 1000000");
        options.addOption("e", "execute", false, "Actually performs the de-duplication of contacts - by default, identifiers " +
            "of duplicated contacts are printed out only.");
    }

    @Override
    protected Void invoke(Options options, CommandLine cmd, MBeanServerConnection mbsc) throws Exception {
        /*
         * parse arguments
         */
        if (false == cmd.hasOption('c')) {
            System.err.println("Missing context identifier.");
            printHelp(options);
            System.exit(1);
            return null;
        }
        int contextID;
        {
            String optionValue = cmd.getOptionValue('c');
            try {
                contextID = Integer.parseInt(optionValue.trim());
            } catch (NumberFormatException e) {
                System.err.println("Context identifier parameter is not a number: " + optionValue);
                printHelp(options);
                System.exit(1);
                return null;
            }
        }
        if (false == cmd.hasOption('f')) {
            System.err.println("Missing contact folder identifier.");
            printHelp(options);
            System.exit(1);
            return null;
        }
        int folderID;
        {
            String optionValue = cmd.getOptionValue('f');
            try {
                folderID = Integer.parseInt(optionValue.trim());
            } catch (NumberFormatException e) {
                System.err.println("Folder identifier parameter is not a number: " + optionValue);
                printHelp(options);
                System.exit(1);
                return null;
            }
        }
        if (false == cmd.hasOption('c')) {
            System.err.println("Missing context identifier.");
            printHelp(options);
            System.exit(1);
            return null;
        }
        long limit = 1000000;
        if (cmd.hasOption('m')) {
            String optionValue = cmd.getOptionValue('m');
            try {
                limit = Integer.parseInt(optionValue.trim());
            } catch (NumberFormatException e) {
                System.err.println("Limit parameter is not a number: " + optionValue);
                printHelp(options);
                System.exit(1);
                return null;
            }
        }
        boolean dryRun = false == cmd.hasOption('e');
        /*
         * Invoke MBean method
         */
        String[] signature = { int.class.getName(), int.class.getName(), long.class.getName(), boolean.class.getName() };
        Object[] params = { Integer.valueOf(contextID), Integer.valueOf(folderID), Long.valueOf(limit), Boolean.valueOf(dryRun) };
        Object result = mbsc.invoke(
            getObjectName("RDB Contact Storage Toolkit", "com.openexchange.contact"), "deduplicateContacts", params, signature);
        if (null == result || false == int[].class.isInstance(result)) {
            System.out.println("Unexpected result: " + result);
            System.exit(1);
            return null;
        }
        int[] objectIDs = (int[])result;
        if (0 == objectIDs.length) {
            System.out.println("No duplicate contacts in folder " + folderID + " of context " + contextID + " found.");
        } else {
            System.out.println("Found " + (dryRun ? "" : "and deleted ") + objectIDs.length + " duplicate contact" +
                (1 == objectIDs.length ? "" : "s") + " in folder " + folderID + " of context " + contextID + ": " +
                System.getProperty("line.separator") + Arrays.toString(objectIDs));
        }
        return null;
    }

    @Override
    protected boolean requiresAdministrativePermission() {
        return true;
    }

    @Override
    protected void administrativeAuth(String login, String password, CommandLine cmd, AuthenticatorMBean authenticator) throws MBeanException {
        int contextID;
        {
            String optionValue = cmd.getOptionValue('c');
            try {
                contextID = Integer.parseInt(optionValue.trim());
            } catch (NumberFormatException e) {
                System.err.println("Context identifier parameter is not a number: " + optionValue);
                System.exit(1);
                return;
            }
        }
        authenticator.doAuthentication(login, password, contextID);
    }

    @Override
    protected String getFooter() {
        return "Handle with care and review the found duplicates before executing the de-duplication. " +
            "Detected duplicates are deleted permanently, with no recovery option.";
    }

    @Override
    protected String getName() {
        return "deduplicatecontacts";
    }

}
