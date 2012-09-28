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

package com.openexchange.report.console;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import com.openexchange.report.Constants;

/**
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public final class LoginCounterTool {

    private static final Options countingOptions;

    /**
     * Prevent instantiation.
     */
    private LoginCounterTool() {
        super();
    }

    /**
     * Main method for starting from console.
     * @param args program arguments
     */
    public static void main(String[] args) {
        CommandLineParser parser = new PosixParser();
        Date startDate = null;
        Date endDate = null;

        try {
            final CommandLine cmd = parser.parse(countingOptions, args);
            if (cmd.hasOption('h')) {
                printHelp();
                System.exit(0);
                return;
            }

            if (!cmd.hasOption('s') || !cmd.hasOption('e')) {
                System.out.println("Parameters 'start' and 'end' are required.");
                printHelp();
                System.exit(0);
                return;
            }
            // Parse dates
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String source = unquote(cmd.getOptionValue('s'));
            try {
                startDate = sdf.parse(source);
            } catch (java.text.ParseException e) {
                System.out.println("Wrong format for parameter 'start': " + source);
                printHelp();
                System.exit(0);
            }
            source = unquote(cmd.getOptionValue('e'));
            try {
                endDate = sdf.parse(source);
            } catch (java.text.ParseException e) {
                System.out.println("Wrong format for parameter 'end': " + source);
                printHelp();
                System.exit(0);
            }
            // Invoke MBean
            JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:9999/server");
            JMXConnector jmxConnector = JMXConnectorFactory.connect(url, null);
            try {
                MBeanServerConnection mbsc = jmxConnector.getMBeanServerConnection();
                String regex = null;
                if (cmd.hasOption('r')) {
                    regex = cmd.getOptionValue('r');
                }

                writeNumberOfLogins(mbsc, startDate, endDate, regex);
            } finally {
                jmxConnector.close();
            }
        } catch (ParseException e) {
            System.err.println("Unable to parse command line: " + e.getMessage());
            printHelp();
        } catch (MalformedURLException e) {
            System.err.println("URL to connect to server is invalid: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Unable to communicate with the server: " + e.getMessage());
        } catch (AttributeNotFoundException e) {
            System.err.println("Attributes for reporting are not available: " + e.getMessage());
        } catch (InstanceNotFoundException e) {
            System.err.println("Instance for reporting is not available: " + e.getMessage());
        } catch (MBeanException e) {
            System.err.println("Problem on MBean connection: " + e.getMessage());
        } catch (ReflectionException e) {
            System.err.println("Problem with reflective type handling: " + e.getMessage());
        } catch (InvalidAttributeValueException e) {
            System.err.println("Problem with JMX attribute: " + e.getMessage());
        }
    }

    private static void writeNumberOfLogins(MBeanServerConnection mbsc, Date startDate, Date endDate, String regex) throws InstanceNotFoundException, AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException, IOException {
        String withRegex = "";
        if (regex != null) {
            withRegex += "\nfor expression\n    '" + regex + "'";
        }

        int count = 0;
        boolean err = false;
        String errMsg = null;
        try {
        	mbsc.setAttribute(Constants.OXTENDER_MONITOR_NAME, new Attribute("DeviceWildcard", regex));
        	count = (Integer) mbsc.invoke(Constants.OXTENDER_MONITOR_NAME, "getNumberOfLogins", new Object[] {startDate, endDate}, new String[] {Date.class.getName(), Date.class.getName()});
        } catch (Exception e) {
        	err = true;
        	errMsg = e.getMessage();
        }

        String output;
        if (err) {
        	output = errMsg != null ? errMsg : "An error occurred.";
        } else {
        	output= "Number of logins between\n    " + startDate.toString() + "\nand\n    " + endDate.toString() + withRegex + "\n\n    :    " + count;
        }

        System.out.println(output);
    }

    private static void printHelp() {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("logincounter", countingOptions);
    }

    private static String unquote(final String s) {
        if (isEmpty(s) || s.length() <= 1) {
            return s;
        }
        String retval = s;
        char c;
        if ((c = retval.charAt(0)) == '"' || c == '\'') {
            retval = retval.substring(1);
        }
        final int mlen = retval.length()-1;
        if ((c = retval.charAt(mlen)) == '"' || c == '\'') {
            retval = retval.substring(0, mlen);
        }
        return retval;
    }

    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = Character.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

    static {
        countingOptions = new Options();
        countingOptions.addOption("h", "help", false, "Prints a help text");
        countingOptions.addOption("s", "start", true, "Required. Sets the start date for the detecting range. Example: 2009-12-31 00:00:00");
        countingOptions.addOption("e", "end", true, "Required. Sets the end date for the detecting range. Example: 2010-01-1 23:59:59");
        countingOptions.addOption("r", "regex", true, "Optional. Limits the counter to login devices that match regex.");
    }
}
