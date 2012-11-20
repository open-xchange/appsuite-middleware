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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.admin.solr.console;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Formatter;
import java.util.List;
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
import com.openexchange.solr.SolrCoreStore;
import com.openexchange.solr.SolrMBean;


/**
 * {@link CoreStoreCLI}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class CoreStoreCLI {
    
    private static final String JMX_URL = "service:jmx:rmi:///jndi/rmi://localhost:9999/server";
    
    private static final String ACTION_REGISTER = "registercorestore";
    
    private static final String ACTION_UNREGISTER = "unregistercorestore";
    
    private static final String ACTION_LIST = "listcorestore";
    
    private static final String ACTION_CHANGE = "changecorestore";
    
    
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
            if (action.contains("/")) {
                action = action.substring(action.lastIndexOf('/') + 1);
            }
            
            int retval = 0;
            if (action.equals(ACTION_REGISTER)) {
                retval = register(actionArgs);
            } else if (action.equals(ACTION_UNREGISTER)) {
                retval = unregister(actionArgs);
            } else if (action.equals(ACTION_LIST)) {
                retval = list(actionArgs);
            } else if (action.equals(ACTION_CHANGE)) {
                retval = change(actionArgs);
            } else {
                System.out.println("Unknown action '" + action + "'.");
                System.exit(1);
            }
            
            System.exit(retval);
        } catch (MalformedObjectNameException e) {
            System.out.println("An internal error occurred: " + e.getMessage());
            System.exit(1);
        } catch (MBeanException e) {
            System.out.println("An internal error occurred. " + e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.out.println("An internal error occurred. " + e.getMessage());
            System.exit(1);
        }
    }
    
    private static int change(String[] args) throws IOException, MalformedObjectNameException, MBeanException {
        Options options = new Options();
        options.addOption(createOption("h", "help", false, "Prints a help text.", false));
        options.addOption(createOption("i", "id", true, "The core store id.", true));
        options.addOption(createOption("c", "cores", true, "The maximal number of cores that this core store can handle.", false));
        options.addOption(createOption("u", "uri", true, "An absolute URI (rfc2396 compliant) that points to the directory where the core store is mounted. E.g. file:/path/to/mount/point", false));
        
        if (args == null) {
            printHelp(ACTION_CHANGE, options);
            return 1;
        }
        
        CommandLineParser parser = new PosixParser();
        try {
            CommandLine cmd = parser.parse(options, args, true);
            if (cmd.hasOption('h')) {
                printHelp(ACTION_CHANGE, options);
                return 0;
            }
            
            int id = Integer.parseInt(cmd.getOptionValue('i'));
            String cmdUri = cmd.getOptionValue('u');
            URI uri = null;
            if (cmdUri != null) {
                uri = new URI(cmdUri);
            }
            String cmdCores = cmd.getOptionValue('c');
            int maxCores = -1;
            if (cmdCores != null) {
                maxCores = Integer.parseInt(cmdCores);
            }
            JMXServiceURL url = new JMXServiceURL(JMX_URL);
            JMXConnector jmxConnector = JMXConnectorFactory.connect(url, null);
            try {
                MBeanServerConnection mbsc = jmxConnector.getMBeanServerConnection();
                SolrMBean solrMBean = solrMBeanProxy(mbsc);
                SolrCoreStore coreStore = solrMBean.getCoreStore(id);
                SolrCoreStore modified = new SolrCoreStore();
                modified.setUri(uri == null ? coreStore.getUri() : uri);
                modified.setMaxCores(maxCores < 0 ? coreStore.getMaxCores() : maxCores);
                modified.setNumCores(coreStore.getNumCores());
                
                solrMBean.modifyCoreStore(id, modified.getUri(), modified.getMaxCores());
                System.out.println("Core store '" + id + "' was successfully modified.");
                
                return 0;
            } finally {
                try {
                    jmxConnector.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        } catch (NumberFormatException e) {
            System.out.println(e.getMessage());
            return 1;
        } catch (ParseException e) {
            printHelp(ACTION_CHANGE, options);
            return 1;
        } catch (URISyntaxException e) {
            System.out.println("Wrong syntax for 'uri'. " + e.getMessage());
            return 1;
        }
    }

    private static int unregister(String[] args) throws IOException, MalformedObjectNameException, MBeanException {
        Options options = new Options();
        options.addOption(createOption("h", "help", false, "Prints a help text.", false));
        options.addOption(createOption("i", "id", true, "The core store id.", true));
        if (args == null) {
            printHelp(ACTION_UNREGISTER, options);
            return 1;
        }
        
        CommandLineParser parser = new PosixParser();
        try {
            CommandLine cmd = parser.parse(options, args, true);
            if (cmd.hasOption('h')) {
                printHelp(ACTION_UNREGISTER, options);
                return 0;
            }
            
            int id = Integer.parseInt(cmd.getOptionValue('i'));
            JMXServiceURL url = new JMXServiceURL(JMX_URL);
            JMXConnector jmxConnector = JMXConnectorFactory.connect(url);
            try {
                MBeanServerConnection mbsc = jmxConnector.getMBeanServerConnection();
                SolrMBean solrMBean = solrMBeanProxy(mbsc);
                solrMBean.unregisterCoreStore(id);
                
                System.out.println("Core store '" + id + "' was successfully unregistered.");
                return 0;
            } finally {
                try {
                    jmxConnector.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Parameter 'id' must be a valid number.");
            return 1;
        } catch (ParseException e) {
            printHelp(ACTION_UNREGISTER, options);
            return 1;
        }
    }
    
    private static int list(String[] args) throws IOException, MalformedObjectNameException, MBeanException {
        Options options = new Options();
        options.addOption(createOption("h", "help", false, "Prints a help text.", false));
        CommandLineParser parser = new PosixParser();
        try {
            CommandLine cmd = parser.parse(options, args, true);
            if (cmd.hasOption('h')) {
                printHelp(ACTION_LIST, options);
                return 0;
            }
        } catch (ParseException e) {
            printHelp(ACTION_LIST, options);
            System.exit(1);
        }
        
        JMXServiceURL url = new JMXServiceURL(JMX_URL);
        JMXConnector jmxConnector = JMXConnectorFactory.connect(url, null);
        try {
            MBeanServerConnection mbsc = jmxConnector.getMBeanServerConnection();
            SolrMBean solrMBean = solrMBeanProxy(mbsc);
            List<SolrCoreStore> stores = solrMBean.getAllStores();
            Formatter formatter = new Formatter();
            formatter.format("%1$8s | %2$64s | %3$10s | %4$10s", "id", "uri", "max. cores", "cores");
            formatter.flush();
            System.out.println(formatter.toString());
            System.out.println("-----------------------------------------------------------------------------------------------------");
            for (SolrCoreStore store : stores) {
                int id = store.getId();
                int maxCores = store.getMaxCores();
                int numCores = store.getNumCores();
                String uri = store.getUri().toString();
                
                formatter = new Formatter();
                formatter.format("%1$8d | %2$64s | %3$10d | %4$10d", id, uri, maxCores, numCores);
                formatter.flush();
                System.out.println(formatter.toString());
            }
            
            return 0;
        } finally {
            try {
                jmxConnector.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }
    
    private static int register(String[] args) throws IOException, MalformedObjectNameException, MBeanException {
        Options options = new Options();
        options.addOption(createOption("h", "help", false, "Prints a help text.", false));
        options.addOption(createOption("u", "uri", true, "An absolute URI (rfc2396 compliant) that points to the directory where the core store is mounted. E.g. file:/path/to/mount/point", true));
        options.addOption(createOption("c", "cores", true, "The maximal number of cores that this core store can handle.", true));
        if (args == null) {
            printHelp(ACTION_REGISTER, options);
            return 1;
        }
        
        CommandLineParser parser = new PosixParser();
        try {
            CommandLine cmd = parser.parse(options, args, true);
            if (cmd.hasOption('h')) {
                printHelp(ACTION_REGISTER, options);
                return 0;
            }
            
            String cmdUri = cmd.getOptionValue('u');
            URI uri = new URI(cmdUri);
            int maxCores = Integer.parseInt(cmd.getOptionValue('c'));
            JMXServiceURL url = new JMXServiceURL(JMX_URL);
            JMXConnector jmxConnector = JMXConnectorFactory.connect(url, null);
            try {
                MBeanServerConnection mbsc = jmxConnector.getMBeanServerConnection();
                SolrMBean solrMBean = solrMBeanProxy(mbsc);
                int id = solrMBean.registerCoreStore(uri, maxCores);
                System.out.println("Core store '" + id + "' was successfully registered.");
                
                return 0;
            } finally {
                try {
                    jmxConnector.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Parameter 'cores' must be a valid number.");
            return 1;
        } catch (ParseException e) {
            printHelp(ACTION_REGISTER, options);
            return 1;
        } catch (URISyntaxException e) {
            System.out.println("Wrong syntax for 'uri'. " + e.getMessage());
            return 1;
        }
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
    
    private static SolrMBean solrMBeanProxy(MBeanServerConnection mbsc) throws MalformedObjectNameException {
        SolrMBean solrMBean = MBeanServerInvocationHandler.newProxyInstance(
            mbsc, 
            new ObjectName(SolrMBean.DOMAIN, SolrMBean.KEY, SolrMBean.VALUE), 
            SolrMBean.class, 
            false);

        return solrMBean;
    }

}
