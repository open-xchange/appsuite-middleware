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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.ReflectionException;
import javax.management.openmbean.TabularDataSupport;
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
import com.openexchange.tools.CSVWriter;
import com.openexchange.tools.console.TableWriter;
import com.openexchange.tools.console.TableWriter.ColumnFormat;
import com.openexchange.tools.console.TableWriter.ColumnFormat.Align;

/**
 * {@link ReportingTool}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class ReportingTool {

    private static final Options reportingOptions;

    /**
     * Prevent instantiation.
     */
    private ReportingTool() {
        super();
    }

    /**
     * Main method for starting from console.
     * @param args programm arguments
     */
    public static void main(String[] args) {
        CommandLineParser parser = new PosixParser();
        try {
            CommandLine cmd = parser.parse(reportingOptions, args);
            if (cmd.hasOption('h')) {
                printHelp();
                System.exit(0);
            }
            JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:9999/server");
            JMXConnector jmxConnector = JMXConnectorFactory.connect(url, null);
            try {
                MBeanServerConnection mbsc = jmxConnector.getMBeanServerConnection();
                final boolean csv = cmd.hasOption('c');
                if (cmd.hasOption('t')) {
                    writeTotal(mbsc, csv);
                } else if (cmd.hasOption('d')) {
                    writeDetail(mbsc, csv);
                } else {
                    writeTotal(mbsc, csv);
                }
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
        }
    }

    private static void writeTotal(MBeanServerConnection mbsc, boolean csv) throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException, IOException {
        TabularDataSupport data = (TabularDataSupport) mbsc.getAttribute(Constants.REPORTING_NAME, "Total");
        List<List<Object>> data2 = new ArrayList<List<Object>>();
        data2.add(Arrays.asList((Object)"contexts", "users"));
        for (Object tmp : data.keySet()) {
            data2.add((List<Object>) tmp);
        }
        if (csv) {
            new CSVWriter(System.out, data2).write();
        } else {
            new TableWriter(System.out, new ColumnFormat[] { new ColumnFormat(Align.RIGHT), new ColumnFormat(Align.RIGHT) }, data2).write();
        }
    }

    private static void writeDetail(MBeanServerConnection mbsc, boolean csv) throws AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException, IOException {
        TabularDataSupport data = (TabularDataSupport) mbsc.getAttribute(Constants.REPORTING_NAME, "Detail");
        List<List<Object>> data2 = new ArrayList<List<Object>>();
        data2.add(Arrays.asList((Object) "id", "users", "age", "created", "mappings"));
        TreeSet<List<Object>> sorted = new TreeSet<List<Object>>(new Comparator<List<Object>>() {
            public int compare(List<Object> o1, List<Object> o2) {
                Integer contextId1 = (Integer) o1.get(0);
                Integer contextId2 = (Integer) o2.get(0);
                return contextId1.compareTo(contextId2);
            }
        });
        sorted.addAll(data.keySet());
        for (List<Object> tmp : sorted) {
            data2.add(tmp);
        }
        if (csv) {
            new CSVWriter(System.out, data2).write();
        } else {
            new TableWriter(System.out, new ColumnFormat[] {
                new ColumnFormat(Align.RIGHT), new ColumnFormat(Align.RIGHT), new ColumnFormat(Align.RIGHT), new ColumnFormat(Align.LEFT),
                new ColumnFormat(Align.LEFT) }, data2).write();
        }
    }

    private static void printHelp() {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("oxreport", reportingOptions);
    }

    static {
        reportingOptions = new Options();
        reportingOptions.addOption("h", "help", false, "Prints a help text");
        reportingOptions.addOption("c", "csv", false, "Format output as CSV");
        reportingOptions.addOption("t", "total", false, "List total contexts/users");
        reportingOptions.addOption("d", "detail", false, "List detailed stats for every context");
    }
}
