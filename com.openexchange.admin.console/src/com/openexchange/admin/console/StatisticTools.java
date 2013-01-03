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

package com.openexchange.admin.console;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Set;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;

public class StatisticTools extends AbstractJMXTools {

    private static final char OPT_STATS_SHORT = 'x';

    private static final String OPT_STATS_LONG = "xchangestats";

    private static final char OPT_TPSTATS_SHORT = 'p';

    private static final String OPT_TPSTATS_LONG = "threadpoolstats";

    private static final char OPT_RUNTIME_STATS_SHORT = 'r';

    private static final String OPT_RUNTIME_STATS_LONG = "runtimestats";

    private static final char OPT_OS_STATS_SHORT = 'o';

    private static final String OPT_OS_STATS_LONG = "osstats";

    private static final char OPT_THREADING_STATS_SHORT = 't';

    private static final String OPT_THREADING_STATS_LONG = "threadingstats";

    private static final char OPT_ALL_STATS_SHORT = 'a';

    private static final String OPT_ALL_STATS_LONG = "allstats";

    private static final char OPT_ADMINDAEMON_STATS_SHORT = 'A';

    private static final String OPT_ADMINDAEMON_STATS_LONG = "admindaemonstats";

    private static final char OPT_SHOWOPERATIONS_STATS_SHORT = 's';

    private static final String OPT_SHOWOPERATIONS_STATS_LONG = "showoperations";

    private static final char OPT_DOOPERATIONS_STATS_SHORT = 'd';

    private static final String OPT_DOOPERATIONS_STATS_LONG = "dooperation";

    private CLIOption xchangestats = null;

    private CLIOption threadpoolstats = null;

    private CLIOption runtimestats = null;

    private CLIOption osstats = null;

    private CLIOption threadingstats = null;

    private CLIOption allstats = null;

    private CLIOption admindaemonstats = null;

    private CLIOption showoperation = null;

    private CLIOption dooperation = null;

    private CLIOption sessionStats = null;

    private CLIOption usmSessionStats = null;

    private CLIOption jsonStats = null;

    private CLIOption clusterStats = null;

    private CLIOption grizzlyStats = null;

    public static void main(final String args[]) {
        final StatisticTools st = new StatisticTools();
        st.start(args, "showruntimestats");
    }

    @Override
    protected void furtherOptionsHandling(final AdminParser parser, final HashMap<String, String[]> env) throws JMException, InterruptedException, IOException, InstanceNotFoundException, AttributeNotFoundException, IntrospectionException, MBeanException, ReflectionException, MalformedObjectNameException, InvalidDataException {
        boolean admin = false;
        if (null != parser.getOptionValue(this.admindaemonstats)) {
            admin = true;
        }
        int count = 0;
        if (null != parser.getOptionValue(this.xchangestats)) {
            final MBeanServerConnection initConnection = initConnection(admin, env);
            showOXData(initConnection, admin);
            count++;
        }
        if (null != parser.getOptionValue(this.threadpoolstats)) {
            final MBeanServerConnection initConnection = initConnection(admin, env);
            showThreadPoolData(initConnection);
            count++;
        }
        if (null != parser.getOptionValue(this.runtimestats)) {
            if (0 == count) {
                final MBeanServerConnection initConnection = initConnection(admin, env);
                System.out.print(getStats(initConnection, "sun.management.RuntimeImpl"));
                showMemoryPoolData(initConnection);
            }
            count++;
        }
        if (null != parser.getOptionValue(this.osstats)) {
            if (0 == count) {
                final MBeanServerConnection initConnection = initConnection(admin, env);
                System.out.print(getStats(initConnection, "com.sun.management.UnixOperatingSystem"));
            }
            count++;

        }
        if (null != parser.getOptionValue(this.threadingstats)) {
            if (0 == count) {
                final MBeanServerConnection initConnection = initConnection(admin, env);
                showSysThreadingData(initConnection);
            }
            count++;

        }
        if (null != parser.getOptionValue(this.sessionStats)) {
            if (0 == count) {
                final MBeanServerConnection initConnection = initConnection(admin, env);
                System.out.print(getStats(initConnection, "com.openexchange.sessiond", "name", "SessionD Toolkit"));
                count++;
            }
        }
        if (null != parser.getOptionValue(this.usmSessionStats)) {
            if (0 == count) {
                final MBeanServerConnection initConnection = initConnection(admin, env);
                System.out.print(getStats(initConnection, "com.openexchange.usm.session", "name", "USMSessionInformation"));
                count++;
            }
        }
        if (null != parser.getOptionValue(this.jsonStats)) {
            if (0 == count) {
                final MBeanServerConnection initConnection = initConnection(admin, env);
                System.out.print(getStats(initConnection, "org.json", "name", "JSONMBean"));
            }
        }
        if (null != parser.getOptionValue(this.clusterStats)) {
            if (0 == count) {
                final MBeanServerConnection initConnection = initConnection(admin, env);
                showClusterData(initConnection);
                count++;
            }
        }
        if (null != parser.getOptionValue(this.grizzlyStats)) {
            if (0 == count) {
                final MBeanServerConnection initConnection = initConnection(admin, env);
                showGrizzlyData(initConnection);
                count++;
            }
        }
        if (null != parser.getOptionValue(this.allstats)) {
            if (0 == count) {
                final MBeanServerConnection initConnection = initConnection(admin, env);
                showOXData(initConnection, admin);
                System.out.print(getStats(initConnection, "com.openexchange.sessiond", "name", "SessionD Toolkit"));
                showThreadPoolData(initConnection);
                System.out.print(getStats(initConnection, "com.sun.management.UnixOperatingSystem"));
                System.out.print(getStats(initConnection, "sun.management.RuntimeImpl"));
                showMemoryPoolData(initConnection);
                showSysThreadingData(initConnection);
                System.out.print(getStats(initConnection, "org.json", "name", "JSONMBean"));
                showGrizzlyData(initConnection);
                System.out.print(getStats(
                    initConnection,
                    "com.openexchange.usm.session",
                    "name",
                    "com.openexchange.usm.session.impl.USMSessionInformation"));
            }
            count++;

        }
        if (null != parser.getOptionValue(this.showoperation)) {
            if (0 == count) {
                final MBeanServerConnection initConnection = initConnection(admin, env);
                showOperations(initConnection);
            }
            count++;

        }
        final String operation = (String) parser.getOptionValue(this.dooperation);
        if (null != operation) {
            if (0 == count) {
                final MBeanServerConnection initConnection = initConnection(admin, env);
                final Object result = doOperation(initConnection, operation);
                if (null != result) {
                    System.out.println(result);
                }
            }
            count++;
            System.out.println("Done");
        }
        if (0 == count) {
            System.err.println(new StringBuilder("No option selected (").append(OPT_STATS_LONG).append(", ").append(OPT_RUNTIME_STATS_LONG).append(
                ", ").append(OPT_OS_STATS_LONG).append(", ").append(OPT_THREADING_STATS_LONG).append(", ").append(OPT_ALL_STATS_LONG).append(
                ", sessionstats)"));
            parser.printUsage();
        } else if (count > 1) {
            System.err.println("More than one of the stat options given. Using the first one only");
        }
    }

    @Override
    protected void setFurtherOptions(final AdminParser parser) {
        this.xchangestats = setShortLongOpt(
            parser,
            OPT_STATS_SHORT,
            OPT_STATS_LONG,
            "shows Open-Xchange stats",
            false,
            NeededQuadState.notneeded);
        this.threadpoolstats = setShortLongOpt(
            parser,
            OPT_TPSTATS_SHORT,
            OPT_TPSTATS_LONG,
            "shows OX-Server threadpool stats",
            false,
            NeededQuadState.notneeded);
        this.runtimestats = setShortLongOpt(
            parser,
            OPT_RUNTIME_STATS_SHORT,
            OPT_RUNTIME_STATS_LONG,
            "shows Java runtime stats",
            false,
            NeededQuadState.notneeded);
        this.osstats = setShortLongOpt(
            parser,
            OPT_OS_STATS_SHORT,
            OPT_OS_STATS_LONG,
            "shows operating system stats",
            false,
            NeededQuadState.notneeded);
        this.threadingstats = setShortLongOpt(
            parser,
            OPT_THREADING_STATS_SHORT,
            OPT_THREADING_STATS_LONG,
            "shows threading stats",
            false,
            NeededQuadState.notneeded);
        this.allstats = setShortLongOpt(
            parser,
            OPT_ALL_STATS_SHORT,
            OPT_ALL_STATS_LONG,
            "shows all stats",
            false,
            NeededQuadState.notneeded);
        this.admindaemonstats = setShortLongOpt(
            parser,
            OPT_ADMINDAEMON_STATS_SHORT,
            OPT_ADMINDAEMON_STATS_LONG,
            "shows stats for the admin instead of the groupware",
            false,
            NeededQuadState.notneeded);
        this.showoperation = setShortLongOpt(
            parser,
            OPT_SHOWOPERATIONS_STATS_SHORT,
            OPT_SHOWOPERATIONS_STATS_LONG,
            "shows the operations for the registered beans",
            false,
            NeededQuadState.notneeded);
        this.dooperation = setShortLongOpt(
            parser,
            OPT_DOOPERATIONS_STATS_SHORT,
            OPT_DOOPERATIONS_STATS_LONG,
            "operation",
            "Syntax is <canonical object name (the first part from showoperatons)>!<operationname>",
            false);
        this.sessionStats = setShortLongOpt(
            parser,
            'i',
            "sessionstats",
            "shows the statistics of the session container",
            false,
            NeededQuadState.notneeded);
        this.usmSessionStats = setShortLongOpt(
            parser,
            'u',
            "usmsessionstats",
            "shows the statistics of the USM session container",
            false,
            NeededQuadState.notneeded);
        this.jsonStats = setShortLongOpt(parser, 'j', "jsonstats", "shows the JSON statistics", false, NeededQuadState.notneeded);
        this.clusterStats = setShortLongOpt(parser, 'c', "clusterstats", "shows the cluster statistics", false, NeededQuadState.notneeded);
        this.grizzlyStats = setShortLongOpt(parser, 'g', "grizzlystats", "shows the grizzly statistics", false, NeededQuadState.notneeded);
    }

    private void showMemoryPoolData(final MBeanServerConnection mbc) throws InstanceNotFoundException, AttributeNotFoundException, IntrospectionException, MBeanException, ReflectionException, IOException {
        System.out.print(getStats(mbc, ManagementFactory.getMemoryPoolMXBeans().get(0).getClass().getName()));
    }

    private void showSysThreadingData(final MBeanServerConnection mbc) throws InstanceNotFoundException, AttributeNotFoundException, IntrospectionException, MBeanException, ReflectionException, IOException {
        System.out.print(getStats(mbc, ManagementFactory.getThreadMXBean().getClass().getName()));
    }

    private void showOXData(final MBeanServerConnection mbc, final boolean admin) throws InstanceNotFoundException, AttributeNotFoundException, IntrospectionException, MBeanException, ReflectionException, IOException {
        if (admin) {
            System.out.print(getStats(mbc, "com.openexchange.admin.tools.monitoring.Monitor"));
        } else {
            System.out.print(getStats(mbc, "com.openexchange.ajp13.monitoring.AJPv13ServerThreadsMonitor"));
            System.out.print(getStats(mbc, "com.openexchange.ajp13.watcher.AJPv13TaskMonitor"));
            System.out.print(getStats(mbc, "com.openexchange.monitoring.internal.GeneralMonitor"));
            System.out.print(getStats(mbc, "com.openexchange.api2.MailInterfaceMonitor"));
            System.out.print(getStats(mbc, "com.openexchange.database.internal.ConnectionPool"));
            System.out.print(getStats(mbc, "com.openexchange.database.internal.Overview"));
        }
    }

    private void showThreadPoolData(MBeanServerConnection mbc) throws InstanceNotFoundException, AttributeNotFoundException, IntrospectionException, MBeanException, ReflectionException, IOException {
        System.out.print(getStats(mbc, "com.openexchange.threadpool.internal.ThreadPoolInformation"));
    }

    @SuppressWarnings("unchecked")
    private void showOperations(final MBeanServerConnection mbc) throws IOException, InstanceNotFoundException, IntrospectionException, ReflectionException {
        final Set<ObjectName> queryNames = mbc.queryNames(null, null);
        for (final ObjectName objname : queryNames) {
            final MBeanInfo beanInfo = mbc.getMBeanInfo(objname);
            final MBeanOperationInfo[] operations = beanInfo.getOperations();
            for (final MBeanOperationInfo operation : operations) {
                System.out.println(new StringBuilder(objname.getCanonicalName()).append(", operationname: ").append(operation.getName()).append(
                    ", desciption: ").append(operation.getDescription()));
            }
        }
    }

    private void showClusterData(MBeanServerConnection mbc) throws MalformedObjectNameException, NullPointerException, IOException, InstanceNotFoundException, IntrospectionException, ReflectionException, AttributeNotFoundException, MBeanException {
        /*
         * general info
         */
        for (String type : new String[] { "Cluster", "Statistics", "Member" }) {
            for (ObjectInstance mbean : mbc.queryMBeans(new ObjectName("com.hazelcast:type=" + type + ",*"), null)) {
                ObjectName objectName = mbean.getObjectName();
                MBeanInfo beanInfo = mbc.getMBeanInfo(objectName);
                for (MBeanAttributeInfo attributeInfo : beanInfo.getAttributes()) {
                    if ("Cluster".equals(type) && "Config".equals(attributeInfo.getName())) {
                        String value = mbc.getAttribute(mbean.getObjectName(), attributeInfo.getName()).toString();
                        for (String keyword : new String[] { "groupConfig=", "properties=", "interfaces=", "tcpIpConfig=" }) {
                            int startIdx = value.indexOf(keyword);
                            if (-1 < startIdx && startIdx + keyword.length() < value.length()) {
                                System.out.println(objectName + "," + keyword.substring(0, keyword.length() - 1) + " = " + extractTextInBrackets(
                                    value,
                                    startIdx + keyword.length()));
                            }
                        }
                    } else {
                        try {
                            System.out.println(objectName + "," + attributeInfo.getName() + " = " + mbc.getAttribute(
                                objectName,
                                attributeInfo.getName()));
                        } catch (Exception e) {
                            System.out.println(objectName + "," + attributeInfo.getName() + " = [" + e.getMessage() + "]");
                        }
                    }
                }
            }
        }
        /*
         * maps
         */
        for (ObjectInstance mbean : mbc.queryMBeans(new ObjectName("com.hazelcast:type=Map,Cluster=*,name=*"), null)) {
            ObjectName objectName = mbean.getObjectName();
            MBeanInfo beanInfo = mbc.getMBeanInfo(objectName);
            for (MBeanAttributeInfo attributeInfo : beanInfo.getAttributes()) {
                try {
                    System.out.println(objectName + "," + attributeInfo.getName() + " = " + mbc.getAttribute(
                        objectName,
                        attributeInfo.getName()));
                } catch (Exception e) {
                    System.out.println(objectName + "," + attributeInfo.getName() + " = [" + e.getMessage() + "]");
                }
            }
        }
    }

    /**
     * Print Grizzly related management info to stdout if Grizzly's MBeans can be found.
     * 
     * @param mbeanServerConnection The MBeanServerConnection to be used for querying MBeans.
     * @throws IOException
     * @throws MalformedObjectNameException
     * @throws NullPointerException
     */
    private void showGrizzlyData(MBeanServerConnection mbeanServerConnection) throws MalformedObjectNameException, NullPointerException, IOException {
        // Iterate over the MBeans we are interested in, query by objectName
        for (GrizzlyMBean grizzlyMBean : GrizzlyMBean.values()) {
            ObjectName objectName = new ObjectName(grizzlyMBean.getObjectName());
            Set<ObjectInstance> mBeans = mbeanServerConnection.queryMBeans(objectName, null);
            // Iterate over the found MBeans and print the desired attributes for this MBean. If no MBeans are found (jmx disabled, ajp
            // backend in use) nothig will be printed to stdout
            for (ObjectInstance mBean : mBeans) {
                for (String attribute : grizzlyMBean.getAttributes()) {
                    try {
                        System.out.println(objectName + "," + attribute + " = " + mbeanServerConnection.getAttribute(objectName, attribute));
                    } catch (Exception e) {
                        System.out.println(objectName + "," + attribute + " = [" + e.getMessage() + "]");
                    }
                }
            }
        }
    }

    private static String extractTextInBrackets(String value, int startIdx) {
        StringBuilder stringBuilder = new StringBuilder();
        if (-1 < startIdx && startIdx < value.length()) {
            int i = startIdx;
            for (; i < value.length(); i++) {
                char c = value.charAt(i);
                if ('[' == c || '{' == c) {
                    i++;
                    break;
                }
            }
            if (i < value.length()) {
                int brackets = 1;
                for (; i < value.length(); i++) {
                    char c = value.charAt(i);
                    if ('[' == c || '{' == c) {
                        brackets++;
                    } else if (']' == c || '}' == c) {
                        brackets--;
                    }
                    if (0 == brackets) {
                        break;
                    } else {
                        stringBuilder.append(c);
                    }
                }
            }
        }
        return stringBuilder.toString();
    }

    /**
     * {@link GrizzlyMBean} Enum of MBeans we are interested in. Each containing the ObjectName and the attributes to query.
     * 
     * @author <a href="mailto:marc	.arens@open-xchange.com">Marc Arens</a>
     */
    private enum GrizzlyMBean {
        HTTPCODECFILTER("org.glassfish.grizzly:pp=/gmbal-root/HttpServer[HttpServer]/NetworkListener[NetworkListener[http-listener]],type=HttpCodecFilter,name=HttpCodecFilter", new String[] {
            "total-bytes-written", "total-bytes-received", "http-codec-error-count" }),
        HTTPSERVERFILTER("org.glassfish.grizzly:pp=/gmbal-root/HttpServer[HttpServer]/NetworkListener[NetworkListener[http-listener]],type=HttpServerFilter,name=HttpServerFilter", new String[] {
            "current-suspended-request-count", "requests-cancelled-count", "requests-completed-count", "requests-received-count",
            "requests-timed-out-count" }),
        KEEPALIVE("org.glassfish.grizzly:pp=/gmbal-root/HttpServer[HttpServer]/NetworkListener[NetworkListener[http-listener]],type=KeepAlive,name=Keep-Alive", new String[] {
            "hits-count", "idle-timeout-seconds", "live-connections-count", "max-requests-count", "refuses-count", "timeouts-count" }),
        NETWORKLISTENER("org.glassfish.grizzly:pp=/gmbal-root/HttpServer[HttpServer],type=NetworkListener,name=NetworkListener[http-listener]", new String[] {
            "host", "port", "secure", "started", "paused", "chunking-enabled", "max-http-header-size", "max-pending-bytes" }),
        TCPNIOTRANSPORT("org.glassfish.grizzly:pp=/gmbal-root/HttpServer[HttpServer]/NetworkListener[NetworkListener[http-listener]],type=TCPNIOTransport,name=Transport", new String[] {
            "bound-addresses", "bytes-read", "bytes-written", "client-connect-timeout-millis", "client-socket-so-timeout", "last-error",
            "open-connections-count", "total-connections-count", "read-buffer-size", "selector-threads-count", "thread-pool-type",
            "server-socket-so-timeout", "socket-keep-alive", "socket-linger", "state", "write-buffer-size" });

        private final String objectName;

        private final String[] attributes;

        /**
         * Initializes a new {@link GrizzlyMBean}.
         * 
         * @param objectName The object name needed to query for this MBean
         * @param attributes The attributes of the MBean we are interested in.
         */
        GrizzlyMBean(String objectName, String[] attributes) {
            this.objectName = objectName;
            this.attributes = attributes;
        }

        /**
         * Gets the object name of the MBean we are interested in.
         * 
         * @return The object name
         */
        public String getObjectName() {
            return objectName;
        }

        /**
         * Gets the attributes of the MBean we are interested in.
         * 
         * @return The attributes
         */
        public String[] getAttributes() {
            return attributes;
        }

    }
}
