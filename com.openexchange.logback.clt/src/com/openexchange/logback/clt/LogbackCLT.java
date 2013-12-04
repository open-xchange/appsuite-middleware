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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

package com.openexchange.logback.clt;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import ch.qos.logback.classic.Level;
import com.openexchange.logging.mbean.LogbackConfigurationMBean;

/**
 * {@link LogbackCLT}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class LogbackCLT {
    
    private static final String serviceURL = "service:jmx:rmi:///jndi/rmi://localhost:9999/server";
    
    private static final Options options = new Options();
    static {
        Option add = createOption("a", "add", false, false, "Flag to add the filter", true);
        Option del = createOption("d", "delete", false, false, "Flag to delete the filter", true);
        
        OptionGroup og = new OptionGroup();
        og.addOption(add).addOption(del);
        
        options.addOption(createOption("u", "user", true, false, "The user id for which to enable logging", false));
        options.addOption(createOption("c", "context", true, false, "The context id for which to enable logging", false));
        options.addOption(createOption("s", "session", true, false, "The session id for which to enable logging", false));
        options.addOption(createOption("l", "level", true, false, "Define the log level", false));
        options.addOption(createOption("h", "help", false, false, "Print usage of the command line tool", false));
        options.addOption(createOption("ll", "list-loggers", false, true, "Get a list with all loggers of the system\nCan optionally have a list with loggers as arguments, i.e. -ll <logger1> <logger2> OR the keyword 'dynamic' that instructs the command line tool to fetch all dynamically modified loggers. Any other keyword is then ignored, and a full list will be retrieved.", false));
        options.addOption(createOption("lf", "list-filters", false, false, "Get a list with all logging filters of the system", false));
        options.addOptionGroup(og);
    }
    
    /**
     * Create an {@link Option} with the {@link OptionBuilder}
     * 
     * @param shortName short name of the option 
     * @param longName long name of the option
     * @param hasArgs whether it has arguments
     * @param hasOptArgs whether it has optional arguments
     * @param description short description
     * @param mandatory whether it is mandatory
     * @return
     */
    @SuppressWarnings("static-access")
    private static final Option createOption(String shortName, String longName, boolean hasArgs, boolean hasOptArgs, String description, boolean mandatory) {
        return OptionBuilder.withLongOpt(longName).hasArg(hasArgs).withDescription(description).isRequired(mandatory).create(shortName);
    }

    /**
     * @param args
     * @throws ParseException 
     */
    public static void main(String[] args) {
        CommandLineParser parser = new PosixParser();
        
        try {
            CommandLine cl = parser.parse(options, args);
            String method = null;
            Object[] params = null;
            
            String sessionID = null;
            int contextID = 0;
            int userID = 0;
            
            if (cl.hasOption("s")) {
                sessionID = cl.getOptionValue("s");
                method = cl.hasOption("a") ? "filterSession" : "removeSessionFilter";
                params = new Object[] {sessionID};
            } else if (cl.hasOption("c")) {
                contextID = getIntValue(cl.getOptionValue("c"));
                if (cl.hasOption("u")) {
                    userID = getIntValue(cl.getOptionValue("u"));
                    method = cl.hasOption("a") ? "filterUser" : "removeUserFilter";
                    params = new Object[]{userID, contextID};
                } else {
                    method = cl.hasOption("a") ? "filterContext" : "removeContextFilter";
                    params = new Object[]{contextID};
                }
            } else if (cl.hasOption("l")) {
                String level = cl.getOptionValue("l");
                String[] logLevelValues = cl.getArgs(); 
                if (isValidLogLevel(level)) {
                    params = new Object[] {level, logLevelValues};
                    method = "setLogLevel";
                }
            } else if (cl.hasOption("lf")) {
                method = "listFilters";
                params = null;
            } else if (cl.hasOption("ll")) {
                String[] llargs = cl.getArgs();
                if (llargs.length > 1) {
                    method = "getLevelForLoggers";
                    params = new Object[]{llargs};
                } else if (llargs.length == 1 && llargs[0].equals("dynamic")) {
                    method = "listDynamicallyModifiedLoggers";
                    params = null;
                } else {
                    method = "listAllLoggers";
                    params = null;
                }
            } else if (cl.hasOption("h")) {
                printUsage();
                System.exit(0);
            } else {
                printUsage();
                System.exit(-1);
            }
            
            invokeMBeanMethod(method, params, getSignatureOf(method));
            System.exit(1);
            
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            printUsage();
            System.exit(-1);
        }
    }

    /**
     * Invoke the specified MBean method with the specified signature and specified parameters
     * @param methodName
     * @param params
     * @param signature
     */
    @SuppressWarnings("unchecked")
    private static final void invokeMBeanMethod(String methodName, Object[] params, String[] signature) {
        try {
            ObjectName logbackConfObjName = new ObjectName(LogbackConfigurationMBean.DOMAIN, LogbackConfigurationMBean.KEY, LogbackConfigurationMBean.VALUE);
            JMXServiceURL jmxServiceURL = new JMXServiceURL(serviceURL);
            JMXConnector jmxConnector = JMXConnectorFactory.connect(jmxServiceURL);
            MBeanServerConnection mbeanServerConnection = jmxConnector.getMBeanServerConnection();
            
            Object o = mbeanServerConnection.invoke(logbackConfObjName, methodName, params, signature);
            if (o instanceof Set) {
                Set<String> set = (Set<String>) o;
                Iterator<String> i = set.iterator();
                while(i.hasNext()) {
                    System.out.println(i.next());
                }
            } else {
                StringBuilder builder = new StringBuilder();
                builder.append("Operation '").append(methodName).append("' with parameters: {");
                for(Object p : params) {
                    if (p instanceof String[]) {
                        String[] s = (String[]) p;
                        for(String str : s) {
                            builder.append(str).append(", ");
                        }
                    } else {
                        builder.append(p).append(", ");
                    }
                }
                builder.setCharAt(builder.length() - 2, '}'); //replace last comma "," with a curly bracket "}"
                builder.append("succeeded.\n");
                System.out.println(builder.toString());
                return;
            }
        } catch (InstanceNotFoundException e) {
            e.printStackTrace();
        } catch (MBeanException e) {
            e.printStackTrace();
        } catch (ReflectionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        System.exit(-1);
    }
    
    /**
     * Validate whether the specified log level is in a recognized logback {@link Level} 
     * @param value loglevel
     * @return true/false
     */
    private static final boolean isValidLogLevel(String value) {
        Level l = Level.toLevel(value, null);
        if (l != null)
            return true;
        
        System.out.println("Error: Unknown log level: \"" + value + "\".");
        System.out.println("Requires a valid log level: {OFF, ERROR, WARN, INFO, DEBUG, TRACE, ALL}\n");
        printUsage();
        System.exit(-1);
        
        return false;
    }
    
    /**
     * Get the int value
     * @param value
     * @return
     */
    private static final int getIntValue(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            System.out.println("Error: Requires an integer value.\n");
            printUsage();
            System.exit(-1);
        }
        return -1;
    }
    
    /**
     * Print usage
     */
    private static final void printUsage() {
        HelpFormatter hf = new HelpFormatter();
        hf.printHelp("logback [-a | -d] [ [-u <userid> -c <contextid>] \n | [-s <sessionid>] \n  | [-c <contextid>] ] \n -l <loglevel> <logger name 1> ... <logger name n> \n -lf \n -ll \n -h", 
            null, 
            options, 
            "\n\nThe flags -a and -d are mutually exclusive.");
    }
    
    /**
     * Get the signature of the specified method as an array of Strings
     * @param methodName
     * @return
     */
    private static final String[] getSignatureOf(String methodName) {
        try {
            String[] signature = null;
            Class<?>[] types = null;
            Class<?> clazz = Class.forName(LogbackConfigurationMBean.class.getCanonicalName());
            for (Method m : clazz.getMethods()) {
                if (m.getName().equals(methodName)) {
                    types = m.getParameterTypes();
                    break;
                }
            }
            
            if (types != null && types.length > 0) {
                signature = new String[types.length];
                int s = 0;
                for(Class<?> c : types) {
                    signature[s++] = c.getName();
                }
                return signature;
            }
            
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}