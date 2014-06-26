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

package com.openexchange.solr.monitoring.console;

import java.io.IOException;
import java.net.MalformedURLException;
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
import com.openexchange.solr.SolrMBean;

/**
 * {@link SolrMonitoringCLT}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SolrMonitoringCLT {

    private static final String JMX_URL = "service:jmx:rmi:///jndi/rmi://localhost:9999/server";

    public static void main(String[] args) {
        System.exit(listCores(args));
    }

    private static int listCores(String[] args) {
        Options options = new Options();
        options.addOption(createOption("h", "help", false, "Prints a help text.", false));
        options.addOption(createOption("d", "details", false, "Lists also the names of all active Solr Cores.", false));
        CommandLineParser parser = new PosixParser();
        JMXConnector jmxConnector = null;
        try {
            CommandLine cmd = parser.parse(options, args, true);
            if (cmd.hasOption('h')) {
                printHelp(options);
                return 0;
            }

            JMXServiceURL url = new JMXServiceURL(JMX_URL);
            jmxConnector = JMXConnectorFactory.connect(url, null);
            MBeanServerConnection mbsc = jmxConnector.getMBeanServerConnection();
            SolrMBean solrMBean = solrMBeanProxy(mbsc);
            List<String> activeCores = solrMBean.getActiveCores();
            System.out.println("Number of active Solr Cores: " + activeCores.size());

            if (cmd.hasOption('d')) {
                System.out.println();
                for (String core : activeCores) {
                    System.out.println("    " + core);
                }
            }

            return 0;
        } catch (MBeanException e) {
            System.out.println(e.getMessage());
            return 1;
        } catch (MalformedURLException e) {
            System.out.println("An internal error occurred: " + e.getMessage());
            return 1;
        } catch (IOException e) {
            System.out.println("An internal error occurred: " + e.getMessage());
            return 1;
        } catch (MalformedObjectNameException e) {
            System.out.println("An internal error occurred: " + e.getMessage());
            return 1;
        } catch (ParseException e) {
            printHelp(options);
            return 1;
        } finally {
            if (jmxConnector != null) {
                try {
                    jmxConnector.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("listactivesolrcores", "Lists the number of Solr Cores that are active on this node.", options, null, false);
    }

    private static Option createOption(String shortArg, String longArg, boolean hasArg, String description, boolean required) {
        Option option = new Option(shortArg, longArg, hasArg, description);
        option.setRequired(required);
        return option;
    }

    private static SolrMBean solrMBeanProxy(MBeanServerConnection mbsc) throws MalformedObjectNameException {
        SolrMBean solrMBean = MBeanServerInvocationHandler.newProxyInstance(mbsc, new ObjectName(
            SolrMBean.DOMAIN,
            SolrMBean.KEY,
            SolrMBean.VALUE), SolrMBean.class, false);

        return solrMBean;
    }

}
