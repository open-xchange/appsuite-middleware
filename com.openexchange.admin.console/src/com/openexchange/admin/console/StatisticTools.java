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
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
import javax.management.openmbean.CompositeDataSupport;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;

public class StatisticTools extends AbstractJMXTools {

    public class ThreadOutputElem {

        private final long threadId;

        private final String threadName;

        private final long allocatedBytes;

        private final long cpuTime;

        private final long userTime;

        private StackTraceElement[] stackTrace;

        public ThreadOutputElem(long threadId, String threadName, long allocatedBytes, long cpuTime, long userTime) {
            this.threadId = threadId;
            this.threadName = threadName;
            this.allocatedBytes = allocatedBytes;
            this.cpuTime = cpuTime;
            this.userTime = userTime;
        }

        public ThreadOutputElem(long threadId, String threadName, long allocatedBytes, long cpuTime, long userTime, StackTraceElement[] stackTrace) {
            this.threadId = threadId;
            this.threadName = threadName;
            this.allocatedBytes = allocatedBytes;
            this.cpuTime = cpuTime;
            this.userTime = userTime;
            this.stackTrace = stackTrace;
        }

        public long getThreadId() {
            return threadId;
        }

        public String getThreadName() {
            return threadName;
        }

        public long getAllocatedBytes() {
            return allocatedBytes;
        }

        public long getCpuTime() {
            return cpuTime;
        }

        public long getUserTime() {
            return userTime;
        }

        public StackTraceElement[] getStackTrace() {
            return stackTrace;
        }

        public void setStackTrace(StackTraceElement[] stackTrace) {
            this.stackTrace = stackTrace;
        }

    }

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

    private static final char OPT_MEMORY_THREADS_STATS_SHORT = 'm';

    private static final String OPT_MEMORY_THREADS_STATS_LONG = "memory";

    private static final char OPT_GC_STATS_SHORT = 'z';

    private static final String OPT_GC_STATS_LONG = "gcstats";

    private static final char OPT_MEMORY_THREADS_FULL_STATS_SHORT = 'M';

    private static final String OPT_MEMORY_THREADS_FULL_STATS_LONG = "Memory";

    private CLIOption xchangestats = null;

    private CLIOption threadpoolstats = null;

    private CLIOption runtimestats = null;

    private CLIOption osstats = null;

    private CLIOption threadingstats = null;

    private CLIOption allstats = null;

    private CLIOption admindaemonstats = null;

    private CLIOption showoperation = null;

    private CLIOption dooperation = null;

    private CLIOption memorythreadstats = null;

    private CLIOption memorythreadstatsfull = null;

    private CLIOption sessionStats = null;

    private CLIOption usmSessionStats = null;

    private CLIOption jsonStats = null;

    private CLIOption clusterStats = null;

    private CLIOption grizzlyStats = null;

    /**
     * Option for garbage collection statistics
     */
    private CLIOption gcStats = null;

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
                count++;
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
        if (null != parser.getOptionValue(this.gcStats)) {
            if (0 == count) {
                final MBeanServerConnection initConnection = initConnection(admin, env);
                showGcData(initConnection);
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
                showGcData(initConnection);
                try {
                    System.out.print(getStats(
                        initConnection,
                        "com.openexchange.usm.session",
                        "name",
                        "com.openexchange.usm.session.impl.USMSessionInformation"));
                } catch (final IllegalStateException e) {
                    // Skip it
                }
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
        if (null != parser.getOptionValue(this.memorythreadstats)) {
            showThreadMemory(env, admin, false);
            count++;
        }

        if (null != parser.getOptionValue(this.memorythreadstatsfull)) {
            showThreadMemory(env, admin, true);
            count++;
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

    private void showThreadMemory(final HashMap<String, String[]> env, boolean admin, boolean stacktrace) throws InterruptedException, IOException, MalformedObjectNameException, InstanceNotFoundException, MBeanException, ReflectionException {
        final MBeanServerConnection initConnection = initConnection(admin, env);
        ThreadMXBean threadBean = ManagementFactory.newPlatformMXBeanProxy(
            initConnection,
            ManagementFactory.THREAD_MXBEAN_NAME,
            ThreadMXBean.class);
        final long[] allThreadIds = threadBean.getAllThreadIds();
        ObjectName srvThrdName = new ObjectName(ManagementFactory.THREAD_MXBEAN_NAME);
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
            allocatedBytes = (long[]) initConnection.invoke(
                srvThrdName,
                "getThreadAllocatedBytes",
                new Object[] { allThreadIds },
                new String[] { "[J" });
        } catch (javax.management.ReflectionException e) {
            System.err.println("AllocatedBytes is not supported on this JVM");
            // Simple set to an array of 0
            allocatedBytes = new long[threadInfo.length];
            Arrays.fill(allocatedBytes, 0);
        }
        // First try the new method everytime, if not available use the old iteration approach
        try {
            cpuTime = (long[]) initConnection.invoke(srvThrdName, "getThreadCpuTime", new Object[] { allThreadIds }, new String[] { "[J" });
        } catch (javax.management.ReflectionException e) {
            cpuTime = new long[threadInfo.length];
            for (int i = 0; i < allThreadIds.length; i++) {
                cpuTime[i] = threadBean.getThreadCpuTime(allThreadIds[i]);
            }
        }
        try {
            userTime = (long[]) initConnection.invoke(
                srvThrdName,
                "getThreadUserTime",
                new Object[] { allThreadIds },
                new String[] { "[J" });
        } catch (javax.management.ReflectionException e) {
            userTime = new long[threadInfo.length];
            for (int i = 0; i < allThreadIds.length; i++) {
                userTime[i] = threadBean.getThreadUserTime(allThreadIds[i]);
            }
        }
        if (allocatedBytes.length != cpuTime.length || cpuTime.length != userTime.length || userTime.length != threadInfo.length) {
            System.err.println("Different results returned");
            return;
        }
        final ArrayList<ThreadOutputElem> arrayList = new ArrayList<ThreadOutputElem>();
        if (stacktrace) {
            System.out.println("ThreadID, Name, AllocatedBytes, CpuTime, UserTime, StackTrace");
            for (int i = 0; i < allThreadIds.length; i++) {
                arrayList.add(new ThreadOutputElem(
                    allThreadIds[i],
                    threadInfo[i].getThreadName(),
                    allocatedBytes[i],
                    cpuTime[i],
                    userTime[i],
                    threadInfo[i].getStackTrace()));
            }
        } else {
            System.out.println("ThreadID, Name, AllocatedBytes, CpuTime, UserTime");
            for (int i = 0; i < allThreadIds.length; i++) {
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
            public int compare(ThreadOutputElem o1, ThreadOutputElem o2) {
                if (o1.getAllocatedBytes() > o2.getAllocatedBytes()) {
                    return -1;
                } else if (o1.getAllocatedBytes() == o2.getAllocatedBytes()) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        if (stacktrace) {
            for (final ThreadOutputElem elem : arrayList) {
                System.out.println(elem.getThreadId() + ", " + elem.getThreadName() + ", " + elem.getAllocatedBytes() + ", " + elem.getCpuTime() + ", " + elem.getUserTime() + ", " + Arrays.toString(elem.getStackTrace()));
            }
        } else {
            for (final ThreadOutputElem elem : arrayList) {
                System.out.println(elem.getThreadId() + ", " + elem.getThreadName() + ", " + elem.getAllocatedBytes() + ", " + elem.getCpuTime() + ", " + elem.getUserTime());
            }
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
        this.gcStats = setShortLongOpt(
            parser,
            OPT_GC_STATS_SHORT,
            OPT_GC_STATS_LONG,
            "shows the gc statistics",
            false,
            NeededQuadState.notneeded);
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
        for (String type : new String[] { "Map", "MultiMap", "Topic", "Queue" }) {
            for (ObjectInstance mbean : mbc.queryMBeans(new ObjectName("com.hazelcast:type=" + type + ",Cluster=*,name=*"), null)) {
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
        for (GrizzlyMBean grizzlyMBean : GrizzlyMBean.values()) {
            ObjectName objectName = new ObjectName(grizzlyMBean.getObjectName());
            mbeanServerConnection.queryMBeans(objectName, null);

            for (String attribute : grizzlyMBean.getAttributes()) {
                try {
                    System.out.println(objectName + "," + attribute + " = " + mbeanServerConnection.getAttribute(objectName, attribute));
                } catch (Exception e) {
                    System.out.println(objectName + "," + attribute + " = [" + e.getMessage() + "]");
                }
            }
        }
    }

    /**
     * Method to prepare and display the garbage collection information.
     * 
     * @param mbeanServerConnection - The MBeanServerConnection to be used for querying MBeans.
     * @throws MalformedObjectNameException - thrown while creating {@link ObjectName}
     * @throws IOException - thrown while using the {@link MBeanServerConnection}
     * @throws ReflectionException- thrown while using the {@link MBeanServerConnection}
     * @throws IntrospectionException - thrown while getting {@link MBeanInfo}
     * @throws InstanceNotFoundException - thrown while getting {@link MBeanAttributeInfo} or {@link MBeanInfo}
     * @throws MBeanException - thrown while trying to get the attribute from {@link MBeanServerConnection}
     * @throws AttributeNotFoundException - thrown while trying to get the attribute from {@link MBeanServerConnection}
     */
    private void showGcData(MBeanServerConnection mbeanServerConnection) throws MalformedObjectNameException, IOException, InstanceNotFoundException, IntrospectionException, AttributeNotFoundException, MBeanException, ReflectionException {
        final StringBuilder stringBuilder = new StringBuilder();

        int uptimeHours = getUptimeHours(mbeanServerConnection);
        stringBuilder.append(ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE + ",hoursUptime=" + uptimeHours + "\n");

        ObjectName domainType = new ObjectName(ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE + ",*");
        Set<ObjectInstance> mbeans = mbeanServerConnection.queryMBeans(domainType, null);

        for (ObjectInstance mbean : mbeans) {
            ObjectName objectName = mbean.getObjectName();
            MBeanInfo beanInfo = mbeanServerConnection.getMBeanInfo(objectName);

            for (MBeanAttributeInfo attributeInfo : beanInfo.getAttributes()) {
                Object attribute = mbeanServerConnection.getAttribute(objectName, attributeInfo.getName());

                if (attribute != null) {
                    stringBuilder.append(objectName.getCanonicalName()).append(",").append(attributeInfo.getName()).append(" = ");

                    if (attribute instanceof CompositeDataSupport) {
                        final CompositeDataSupport compositeDataSupport = (CompositeDataSupport) attribute;
                        stringBuilder.append("[startTime=").append(compositeDataSupport.get("startTime")).append(", endTime=").append(
                            compositeDataSupport.get("endTime")).append(", GcThreadCount=").append(
                                compositeDataSupport.get("GcThreadCount")).append(", duration=").append(compositeDataSupport.get("duration")).append(
                                    "]\n");
                    } else if (attribute instanceof String[]) {
                        final String[] stringArray = (String[]) attribute;
                        stringBuilder.append(Arrays.toString(stringArray) + "\n");
                    } else if (attribute instanceof long[]) {
                        final long[] longArray = (long[]) attribute;
                        stringBuilder.append(Arrays.toString(longArray) + "\n");
                    } else {
                        if (attributeInfo.getName().equalsIgnoreCase("collectioncount") || attributeInfo.getName().equalsIgnoreCase(
                            "collectiontime")) {
                            if (attribute instanceof Long) {
                                Long attributeValue = (Long) attribute;
                                if (uptimeHours > 0) {
                                    stringBuilder.append((attributeValue / uptimeHours) + "\n");
                                } else {
                                    stringBuilder.append(0 + "\n");
                                }
                            }
                        } else {
                            stringBuilder.append(attribute.toString() + "\n");
                        }
                    }
                }
            }
        }
        System.out.println(stringBuilder.toString());
    }

    /**
     * @param mbeanServerConnection
     * @param uptimeHours
     * @return
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws InstanceNotFoundException
     * @throws IntrospectionException
     * @throws ReflectionException
     * @throws MBeanException
     * @throws AttributeNotFoundException
     */
    private int getUptimeHours(MBeanServerConnection mbeanServerConnection) throws MalformedObjectNameException, IOException, InstanceNotFoundException, IntrospectionException, ReflectionException, MBeanException, AttributeNotFoundException {
        int uptimeHours = 0;

        ObjectName domainTypeRuntime = new ObjectName(ManagementFactory.RUNTIME_MXBEAN_NAME + ",*");
        Set<ObjectInstance> mbeansRuntime = mbeanServerConnection.queryMBeans(domainTypeRuntime, null);
        for (ObjectInstance mbean : mbeansRuntime) {
            ObjectName objectName = mbean.getObjectName();
            MBeanInfo beanInfo = mbeanServerConnection.getMBeanInfo(objectName);
            for (MBeanAttributeInfo attributeInfo : beanInfo.getAttributes()) {
                if (attributeInfo.getName().equalsIgnoreCase("uptime")) {
                    Object attribute = mbeanServerConnection.getAttribute(objectName, attributeInfo.getName());
                    if (attribute instanceof Long) {
                        Long value = (Long) attribute;
                        uptimeHours = (int) (value / (1000 * 60 * 60));
                    }
                }
            }
        }
        return uptimeHours;
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
