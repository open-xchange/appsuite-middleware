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

package com.openexchange.admin.index.console;

import java.io.IOException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import com.openexchange.index.IndexManagementMBean;


/**
 * {@link IndexCLI}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class IndexCLI {

    private static final String JMX_URL = "service:jmx:rmi:///jndi/rmi://localhost:9999/server";

    private static final String ACTION_LOCK = "lockindex";

    private static final String ACTION_UNLOCK = "unlockindex";


    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            System.out.println("Missing argument 'action'.");
            System.exit(1);
        }

        String[] actionArgs;
        if (args.length > 1) {
            actionArgs = new String[args.length - 1];
            System.arraycopy(args, 1, actionArgs, 0, actionArgs.length);
        } else {
            actionArgs = new String[0];
        }

        try {
            String action = args[0];
            int pos;
            if ((pos = action.lastIndexOf('/')) >= 0) {
                action = action.substring(pos + 1);
            }

            int retval = 0;
            if (action.equals(ACTION_LOCK)) {
                retval = lockIndex(actionArgs);
            } else if (action.equals(ACTION_UNLOCK)) {
                retval = unlockIndex(actionArgs);
            } else {
                System.out.println("Unknown action '" + action + "'.");
                System.exit(1);
            }

            System.exit(retval);
        } catch (MalformedObjectNameException e) {
            System.out.println("An internal error occurred: " + e.getMessage());
            System.exit(1);
        } catch (MBeanException e) {
            System.out.println("An internal error occurred: " + e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.out.println("An internal error occurred: " + e.getMessage());
            System.exit(1);
        }
    }

    private static int unlockIndex(String args[]) throws IOException, MalformedObjectNameException, NullPointerException, MBeanException {
        Options options = new Options();
        options.addOption(createOption("h", "help", false, "Prints a help text.", false));
        options.addOption(createOption("c", "context", true, "The context id.", true));
        options.addOption(createOption("u", "user", true, "The user id.", true));
        options.addOption(createOption("m", "module", true, "The module id.", true));
        if (args == null) {
            printHelp(ACTION_UNLOCK, options);
            return 1;
        }

        CommandLineParser parser = new PosixParser();
        try {
            CommandLine cmd = parser.parse(options, args, true);
            if (cmd.hasOption('h')) {
                printHelp(ACTION_UNLOCK, options);
                return 0;
            }

            JMXServiceURL url = new JMXServiceURL(JMX_URL);
            JMXConnector jmxConnector = JMXConnectorFactory.connect(url, null);
            try {
                MBeanServerConnection mbsc = jmxConnector.getMBeanServerConnection();
                IndexManagementMBean indexMBean = indexMBean(mbsc);
                int contextId = Integer.parseInt(cmd.getOptionValue('c'));
                int userId = Integer.parseInt(cmd.getOptionValue('u'));
                int module = Integer.parseInt(cmd.getOptionValue('m'));
                indexMBean.unlockIndex(contextId, userId, module);

                System.out.println("Index was unlocked.");
                return 0;
            } finally {
                try {
                    jmxConnector.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        } catch (NumberFormatException e) {
            printHelp(ACTION_UNLOCK, options);
            return 1;
        } catch (ParseException e) {
            printHelp(ACTION_UNLOCK, options);
            return 1;
        }
    }

    private static int lockIndex(String args[]) throws IOException, MalformedObjectNameException, NullPointerException, MBeanException {
        Options options = new Options();
        options.addOption(createOption("h", "help", false, "Prints a help text.", false));
        options.addOption(createOption("c", "context", true, "The context id.", true));
        options.addOption(createOption("u", "user", true, "The user id.", true));
        options.addOption(createOption("m", "module", true, "The module id.", true));
        if (args == null) {
            printHelp(ACTION_LOCK, options);
            return 1;
        }

        CommandLineParser parser = new PosixParser();
        try {
            CommandLine cmd = parser.parse(options, args, true);
            if (cmd.hasOption('h')) {
                printHelp(ACTION_LOCK, options);
                return 0;
            }

            JMXServiceURL url = new JMXServiceURL(JMX_URL);
            JMXConnector jmxConnector = JMXConnectorFactory.connect(url, null);
            try {
                MBeanServerConnection mbsc = jmxConnector.getMBeanServerConnection();
                IndexManagementMBean indexMBean = indexMBean(mbsc);
                int contextId = Integer.parseInt(cmd.getOptionValue('c'));
                int userId = Integer.parseInt(cmd.getOptionValue('u'));
                int module = Integer.parseInt(cmd.getOptionValue('m'));
                indexMBean.lockIndex(contextId, userId, module);

                System.out.println("Index was locked.");
                return 0;
            } finally {
                try {
                    jmxConnector.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        } catch (NumberFormatException e) {
            printHelp(ACTION_LOCK, options);
            return 1;
        } catch (ParseException e) {
            printHelp(ACTION_LOCK, options);
            return 1;
        }
    }

    private static IndexManagementMBean indexMBean(MBeanServerConnection mbsc) throws MalformedObjectNameException {
        IndexManagementMBean proxyInstance = MBeanServerInvocationHandler.newProxyInstance(
            mbsc,
            new ObjectName(IndexManagementMBean.DOMAIN, IndexManagementMBean.KEY, IndexManagementMBean.VALUE),
            IndexManagementMBean.class,
            false);
        return proxyInstance;
    }

    private static void printHelp(String action, Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(action, options, false);
    }

    private static Option createOption(String shortArg, String longArg, boolean hasArg, String description, boolean required) {
        Option option = new Option(shortArg, longArg, hasArg, description);
        option.setRequired(required);
        return option;
    }

}
