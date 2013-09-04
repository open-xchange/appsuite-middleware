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
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
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

/**
 * Implements the CLT showruntimestats.
 * 
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
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

    private static final char OPT_SHOWOPERATIONS_STATS_SHORT = 's';
    private static final String OPT_SHOWOPERATIONS_STATS_LONG = "showoperations";

    private static final char OPT_DOOPERATIONS_STATS_SHORT = 'd';
    private static final String OPT_DOOPERATIONS_STATS_LONG = "dooperation";

    private static final char OPT_MEMORY_THREADS_STATS_SHORT = 'm';
    private static final String OPT_MEMORY_THREADS_STATS_LONG = "memory";

    private static final char OPT_MEMORY_THREADS_FULL_STATS_SHORT = 'M';
    private static final String OPT_MEMORY_THREADS_FULL_STATS_LONG = "Memory";

    private CLIOption xchangestats = null;
    private CLIOption threadpoolstats = null;
    private CLIOption runtimestats = null;
    private CLIOption osstats = null;
    private CLIOption threadingstats = null;
    private CLIOption allstats = null;
    private CLIOption showoperation = null;
    private CLIOption dooperation = null;
    private CLIOption memorythreadstats = null;
    private CLIOption memorythreadstatsfull = null;
    private CLIOption sessionStats = null;
    private CLIOption usmSessionStats = null;
    private CLIOption clusterStats = null;
    private CLIOption grizzlyStats = null;

    public static void main(final String args[]) {
        final StatisticTools st = new StatisticTools();
        st.start(args, "showruntimestats");
    }

    @Override
    protected void furtherOptionsHandling(final AdminParser parser, final Map<String, String[]> env) throws JMException, InterruptedException, IOException, InstanceNotFoundException, AttributeNotFoundException, IntrospectionException, MBeanException, ReflectionException, MalformedObjectNameException, InvalidDataException {
        int count = 0;
        final MBeanServerConnection mbc = initConnection(env);
        if (null != parser.getOptionValue(this.xchangestats)) {
            System.out.print(showOXData(mbc));
            count++;
        }
        if (null != parser.getOptionValue(this.threadpoolstats) && 0 == count) {
            System.out.print(showThreadPoolData(mbc));
            count++;
        }
        if (null != parser.getOptionValue(this.runtimestats) && 0 == count) {
            System.out.print(getStats(mbc, "java.lang:type=Runtime"));
            System.out.print(showMemoryPoolData(mbc));
            count++;
        }
        if (null != parser.getOptionValue(this.osstats) && 0 == count) {
            System.out.print(getStats(mbc, "java.lang:type=OperatingSystem"));
            count++;
        }
        if (null != parser.getOptionValue(this.threadingstats) && 0 == count) {
            System.out.print(showSysThreadingData(mbc));
            count++;
        }
        if (null != parser.getOptionValue(this.sessionStats) && 0 == count) {
            System.out.print(getStats(mbc, "com.openexchange.sessiond", "name", "SessionD Toolkit"));
            count++;
        }
        if (null != parser.getOptionValue(this.usmSessionStats) && 0 == count) {
            System.out.print(getStats(mbc, "com.openexchange.usm.session", "name", "USMSessionInformation"));
            count++;
        }
        if (null != parser.getOptionValue(this.clusterStats) && 0 == count) {
            System.out.print(showClusterData(mbc));
            count++;
        }
        if (null != parser.getOptionValue(this.grizzlyStats) && 0 == count) {
            System.out.print(showGrizzlyData(mbc));
            count++;
        }
        if (null != parser.getOptionValue(this.allstats) && 0 == count) {
            System.out.print(showOXData(mbc));
            System.out.print(getStats(mbc, "com.openexchange.sessiond", "name", "SessionD Toolkit"));
            System.out.print(showThreadPoolData(mbc));
            System.out.print(getStats(mbc, "java.lang:type=OperatingSystem"));
            System.out.print(getStats(mbc, "java.lang:type=Runtime"));
            System.out.print(showMemoryPoolData(mbc));
            System.out.print(showSysThreadingData(mbc));
            System.out.print(showGrizzlyData(mbc));
            System.out.print(getStats(mbc, "com.openexchange.usm.session", "name", "com.openexchange.usm.session.impl.USMSessionInformation"));
            count++;
        }
        if (null != parser.getOptionValue(this.showoperation) && 0 == count) {
            System.out.print(showOperations(mbc));
            count++;
        }
        final String operation = (String) parser.getOptionValue(this.dooperation);
        if (null != operation && 0 == count) {
            final Object result = doOperation(mbc, operation);
            if (null != result) {
                System.out.println(result);
            }
            count++;
        }
        if (null != parser.getOptionValue(this.memorythreadstats) && 0 == count) {
            System.out.print(showThreadMemory(mbc, false));
            count++;
        }
        if (null != parser.getOptionValue(this.memorythreadstatsfull) && 0 == count) {
            System.out.print(showThreadMemory(mbc, true));
            count++;
        }
        if (0 == count) {
            System.err.println(new StringBuilder("No option selected (").append(OPT_STATS_LONG).append(", ").append(
                OPT_RUNTIME_STATS_LONG).append(", ").append(OPT_OS_STATS_LONG).append(", ").append(OPT_THREADING_STATS_LONG).append(
                ", ").append(OPT_ALL_STATS_LONG).append(", sessionstats)"));
            parser.printUsage();
        } else if (count > 1) {
            System.err.println("More than one of the stat options given. Using the first one only");
        }
        System.out.println("Done");
    }

    static String showThreadMemory(MBeanServerConnection mbc, boolean stacktrace) throws IOException, MalformedObjectNameException, InstanceNotFoundException, MBeanException {
        StringBuilder sb = new StringBuilder();
        final ThreadMXBean threadBean = ManagementFactory.newPlatformMXBeanProxy(mbc, ManagementFactory.THREAD_MXBEAN_NAME, ThreadMXBean.class);
        final long[] allThreadIds = threadBean.getAllThreadIds();
        final ObjectName srvThrdName = new ObjectName(ManagementFactory.THREAD_MXBEAN_NAME);
        long[] allocatedBytes = null;
        long[] cpuTime = null;
        long[] userTime = null;
        final ThreadInfo[] threadInfo;
        if (stacktrace) {
            threadInfo = threadBean.getThreadInfo(allThreadIds, Integer.MAX_VALUE);
        } else {
            threadInfo = threadBean.getThreadInfo(allThreadIds);
        }
        try {
            allocatedBytes = (long[]) mbc.invoke(
                srvThrdName,
                "getThreadAllocatedBytes",
                new Object[] { allThreadIds },
                new String[] { "[J" });
        } catch (final javax.management.ReflectionException e) {
            System.err.println("AllocatedBytes is not supported on this JVM");
            // Simple set to an array of 0
            allocatedBytes = new long[threadInfo.length];
            Arrays.fill(allocatedBytes, 0);
        }
        // First try the new method every time, if not available use the old iteration approach
        try {
            cpuTime = (long[]) mbc.invoke(srvThrdName, "getThreadCpuTime", new Object[] { allThreadIds }, new String[] { "[J" });
        } catch (final javax.management.ReflectionException e) {
            cpuTime = new long[threadInfo.length];
            for (int i = 0; i < allThreadIds.length; i++) {
                cpuTime[i] = threadBean.getThreadCpuTime(allThreadIds[i]);
            }
        }
        try {
            userTime = (long[]) mbc.invoke(
                srvThrdName,
                "getThreadUserTime",
                new Object[] { allThreadIds },
                new String[] { "[J" });
        } catch (final javax.management.ReflectionException e) {
            userTime = new long[threadInfo.length];
            for (int i = 0; i < allThreadIds.length; i++) {
                userTime[i] = threadBean.getThreadUserTime(allThreadIds[i]);
            }
        }
        if (allocatedBytes.length != cpuTime.length || cpuTime.length != userTime.length || userTime.length != threadInfo.length) {
            System.err.println("Different results returned");
            return sb.toString();
        }
        final ArrayList<ThreadOutputElem> arrayList = new ArrayList<ThreadOutputElem>();
        sb.append("ThreadID, Name, AllocatedBytes, CpuTime, UserTime");
        if (stacktrace) {
            sb.append(", StackTrace");
        }
        sb.append(LINE_SEPARATOR);
        for (int i = 0; i < allThreadIds.length; i++) {
            if (stacktrace) {
                arrayList.add(new ThreadOutputElem(
                    allThreadIds[i],
                    threadInfo[i].getThreadName(),
                    allocatedBytes[i],
                    cpuTime[i],
                    userTime[i],
                    threadInfo[i].getStackTrace()));
            } else {
                arrayList.add(new ThreadOutputElem(
                    allThreadIds[i],
                    threadInfo[i].getThreadName(),
                    allocatedBytes[i],
                    cpuTime[i],
                    userTime[i]));
            }
        }
        Collections.sort(arrayList, new Comparator<ThreadOutputElem>() {
            @Override
            public int compare(final ThreadOutputElem o1, final ThreadOutputElem o2) {
                if (o1.getAllocatedBytes() > o2.getAllocatedBytes()) {
                    return -1;
                } else if (o1.getAllocatedBytes() == o2.getAllocatedBytes()) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        for (final ThreadOutputElem elem : arrayList) {
            sb.append(elem.getThreadId());
            sb.append(", ");
            sb.append(elem.getThreadName());
            sb.append(", ");
            sb.append(elem.getAllocatedBytes());
            sb.append(", ");
            sb.append(elem.getCpuTime());
            sb.append(", ");
            sb.append(elem.getUserTime());
            if (stacktrace) {
                sb.append(", ");
                sb.append(Arrays.toString(elem.getStackTrace()));
            }
            sb.append(LINE_SEPARATOR);
        }
        return sb.toString();
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
        this.clusterStats = setShortLongOpt(parser, 'c', "clusterstats", "shows the cluster statistics", false, NeededQuadState.notneeded);
        this.grizzlyStats = setShortLongOpt(parser, 'g', "grizzlystats", "shows the grizzly statistics", false, NeededQuadState.notneeded);
        this.memorythreadstats = setShortLongOpt(
            parser,
            OPT_MEMORY_THREADS_STATS_SHORT,
            OPT_MEMORY_THREADS_STATS_LONG,
            "shows memory usage of threads",
            false,
            NeededQuadState.notneeded);
        this.memorythreadstatsfull = setShortLongOpt(
            parser,
            OPT_MEMORY_THREADS_FULL_STATS_SHORT,
            OPT_MEMORY_THREADS_FULL_STATS_LONG,
            "shows memory usage of threads including stack traces",
            false,
            NeededQuadState.notneeded);
    }

    static String showMemoryPoolData(MBeanServerConnection con) throws InstanceNotFoundException, AttributeNotFoundException, IntrospectionException, MBeanException, ReflectionException, IOException, MalformedObjectNameException, NullPointerException {
        return getStats(con, "java.lang:type=MemoryPool,name=*").toString();
    }

    static String showSysThreadingData(final MBeanServerConnection con) throws InstanceNotFoundException, AttributeNotFoundException, IntrospectionException, MBeanException, ReflectionException, IOException, MalformedObjectNameException, NullPointerException {
        return getStats(con, "java.lang:type=Threading").toString();
    }

    private static String showOXData(MBeanServerConnection con) throws InstanceNotFoundException, AttributeNotFoundException, IntrospectionException, MBeanException, ReflectionException, IOException, MalformedObjectNameException, NullPointerException {
        StringBuilder sb = new StringBuilder();
        sb.append(getStats(con, "com.openexchange.admin.monitor:name=CallMonitor"));
        sb.append(getStats(con, "com.openexchange.monitoring:name=AJPv13ServerThreadsMonitor"));
        sb.append(getStats(con, "com.openexchange.monitoring:name=AJPv13TaskMonitor"));
        sb.append(getStats(con, "com.openexchange.monitoring:name=GeneralMonitor"));
        sb.append(getStats(con, "com.openexchange.monitoring:name=MailInterfaceMonitor"));
        sb.append(getStats(con, "com.openexchange.pooling:name=*"));
        return sb.toString();
    }

    static String showThreadPoolData(final MBeanServerConnection mbc) throws InstanceNotFoundException, AttributeNotFoundException, IntrospectionException, MBeanException, ReflectionException, IOException, MalformedObjectNameException, NullPointerException {
        return getStats(mbc, "com.openexchange.threadpool:name=ThreadPoolInformation").toString();
    }

    static String showOperations(final MBeanServerConnection mbc) throws IOException, InstanceNotFoundException, IntrospectionException, ReflectionException {
        StringBuilder sb = new StringBuilder();
        final Set<ObjectName> queryNames = mbc.queryNames(null, null);
        for (final ObjectName objname : queryNames) {
            final MBeanInfo beanInfo = mbc.getMBeanInfo(objname);
            final MBeanOperationInfo[] operations = beanInfo.getOperations();
            for (final MBeanOperationInfo operation : operations) {
                sb.append(objname.getCanonicalName());
                sb.append(", operationname: ");
                sb.append(operation.getName());
                sb.append(", desciption: ");
                sb.append(operation.getDescription());
                sb.append(LINE_SEPARATOR);
            }
        }
        return sb.toString();
    }

    static String showClusterData(final MBeanServerConnection mbc) throws MalformedObjectNameException, NullPointerException, IOException, InstanceNotFoundException, IntrospectionException, ReflectionException, AttributeNotFoundException, MBeanException {
        // general info
        StringBuilder sb = new StringBuilder();
        for (final String type : new String[] { "Cluster", "Statistics", "Member" }) {
            for (final ObjectInstance mbean : mbc.queryMBeans(new ObjectName("com.hazelcast:type=" + type + ",*"), null)) {
                final ObjectName objectName = mbean.getObjectName();
                final MBeanInfo beanInfo = mbc.getMBeanInfo(objectName);
                for (final MBeanAttributeInfo attributeInfo : beanInfo.getAttributes()) {
                    if ("Cluster".equals(type) && "Config".equals(attributeInfo.getName())) {
                        final String value = mbc.getAttribute(mbean.getObjectName(), attributeInfo.getName()).toString();
                        for (final String keyword : new String[] { "groupConfig=", "properties=", "interfaces=", "tcpIpConfig=" }) {
                            final int startIdx = value.indexOf(keyword);
                            if (-1 < startIdx && startIdx + keyword.length() < value.length()) {
                                sb.append(objectName);
                                sb.append(',');
                                sb.append(keyword.substring(0, keyword.length() - 1));
                                sb.append(" = ");
                                sb.append(extractTextInBrackets(value, startIdx + keyword.length()));
                                sb.append(LINE_SEPARATOR);
                            }
                        }
                    } else {
                        sb.append(objectName);
                        sb.append(",");
                        sb.append(attributeInfo.getName());
                        sb.append(" = ");
                        try {
                            sb.append(mbc.getAttribute(objectName, attributeInfo.getName()));
                        } catch (final Exception e) {
                            sb.append('[');
                            sb.append(e.getMessage());
                            sb.append(']');
                        }
                        sb.append(LINE_SEPARATOR);
                    }
                }
            }
        }
        // maps
        for (final String type : new String[] { "Map", "MultiMap", "Topic", "Queue" }) {
            for (final ObjectInstance mbean : mbc.queryMBeans(new ObjectName("com.hazelcast:type=" + type + ",Cluster=*,name=*"), null)) {
                final ObjectName objectName = mbean.getObjectName();
                final MBeanInfo beanInfo = mbc.getMBeanInfo(objectName);
                for (final MBeanAttributeInfo attributeInfo : beanInfo.getAttributes()) {
                    sb.append(objectName);
                    sb.append(',');
                    sb.append(attributeInfo.getName());
                    sb.append(" = ");
                    try {
                        sb.append(mbc.getAttribute(objectName, attributeInfo.getName()));
                    } catch (final Exception e) {
                        sb.append('[');
                        sb.append(e.getMessage());
                        sb.append(']');
                    }
                    sb.append(LINE_SEPARATOR);
                }
            }
        }
        return sb.toString();
    }

    /**
     * Print Grizzly related management info to given PrintStream if Grizzly's MBeans can be found.
     * 
     * @param mbeanServerConnection The MBeanServerConnection to be used for querying MBeans.
     * @param out the {@link PrintStream} to write the output to.
     * @throws IOException
     * @throws MalformedObjectNameException
     * @throws NullPointerException
     */
    static String showGrizzlyData(final MBeanServerConnection mbeanServerConnection) throws MalformedObjectNameException, NullPointerException, IOException {
        StringBuilder sb = new StringBuilder();
        // Iterate over the MBeans we are interested in, query by objectName
        for (final GrizzlyMBean grizzlyMBean : GrizzlyMBean.values()) {
            final ObjectName objectName = new ObjectName(grizzlyMBean.getObjectName());
            final Set<ObjectInstance> mBeans = mbeanServerConnection.queryMBeans(objectName, null);
            // Iterate over the found MBeans and print the desired attributes for this MBean. If no MBeans are found (jmx disabled, ajp
            // backend in use) nothig will be printed to stdout
            for (final ObjectInstance mBean : mBeans) {
                for (final String attribute : grizzlyMBean.getAttributes()) {
                    sb.append(objectName);
                    sb.append(',');
                    sb.append(attribute);
                    sb.append(" = ");
                    try {
                        sb.append(mbeanServerConnection.getAttribute(objectName, attribute));
                    } catch (final Exception e) {
                        sb.append('[');
                        sb.append(e.getMessage());
                        sb.append(']');
                    }
                    sb.append(LINE_SEPARATOR);
                }
            }
        }
        return sb.toString();
    }

    private static String extractTextInBrackets(final String value, final int startIdx) {
        final StringBuilder stringBuilder = new StringBuilder();
        if (-1 < startIdx && startIdx < value.length()) {
            int i = startIdx;
            for (; i < value.length(); i++) {
                final char c = value.charAt(i);
                if ('[' == c || '{' == c) {
                    i++;
                    break;
                }
            }
            if (i < value.length()) {
                int brackets = 1;
                for (; i < value.length(); i++) {
                    final char c = value.charAt(i);
                    if ('[' == c || '{' == c) {
                        brackets++;
                    } else if (']' == c || '}' == c) {
                        brackets--;
                    }
                    if (0 == brackets) {
                        break;
                    }
                    stringBuilder.append(c);
                }
            }
        }
        return stringBuilder.toString();
    }
}
