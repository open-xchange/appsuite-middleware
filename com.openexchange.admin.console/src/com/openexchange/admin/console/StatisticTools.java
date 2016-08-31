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
import java.util.Map.Entry;
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

    private static final char OPT_EVENT_ADMIN_STATS_SHORT = 'e';
    private static final String OPT_EVENT_ADMIN_STATS_LONG = "eventstats";

    private static final char OPT_GC_STATS_SHORT = 'z';

    private static final String OPT_GC_STATS_LONG = "gcstats";

    private static final char OPT_MEMORY_THREADS_FULL_STATS_SHORT = 'M';
    private static final String OPT_MEMORY_THREADS_FULL_STATS_LONG = "Memory";

    private static final char OPT_DOCUMENTCONVERTER_STATS_SHORT = 'y';
    private static final String OPT_DOCUMENTCONVERTER_STATS_LONG = "documentconverterstats";

    private static final char OPT_OFFICE_STATS_SHORT = 'f';
    private static final String OPT_OFFICE_STATS_LONG = "officestats";

    private static final char OPT_CACHE_STATS_SHORT = 'j';
    private static final String OPT_CACHE_STATS_LONG = "cachestats";

    private static final String OPT_GENERAL_STATS_LONG = "generalstats";

    private static final String OPT_MAILINTERFACE_STATS_LONG = "mailinterfacestats";

    private static final String OPT_POOLING_STATS_LONG = "poolingstats";

    private static final String OPT_CALLMONITOR_STATS_LONG = "callmonitorstats";

    private static final String OPT_MISC_STATS_LONG = "misc";

    private static final String OPT_OVERVIEW_STATS_LONG = "overview";

    private static final String OPT_MEMORYPOOL_STATS_LONG = "memorypool";

    private static final char OPT_NIO_BUFFER_STATS_SHORT = 'n';
    private static final String OPT_NIO_BUFFER_STATS_LONG = "niobufferstats";

    private static final char OPT_RT_BUFFER_STATS_SHORT= 'R';
    private static final String OPT_RT_BUFFER_STATS_LONG = "rtstats";

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
    private CLIOption cacheStats = null;
    private CLIOption usmSessionStats = null;
    private CLIOption clusterStats = null;
    private CLIOption grizzlyStats = null;
    private CLIOption pnsStats = null;
    private CLIOption webSocketStats = null;
    private CLIOption documentconverterstats = null;
    private CLIOption officestats = null;
    private CLIOption eventadminstats = null;
    private CLIOption generalstats = null;
    private CLIOption mailinterfacestats = null;
    private CLIOption poolingstats = null;
    private CLIOption callmonitorstats = null;
    private CLIOption miscstats = null;
    private CLIOption overviewstats = null;
    private CLIOption memorypoolstats = null;
    private CLIOption niobufferstats = null;
    private CLIOption rtstats = null;

    /**
     * Option for garbage collection statistics
     */
    private CLIOption gcStats = null;

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
        if (null != parser.getOptionValue(this.memorypoolstats) && 0 == count) {
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
        if (null != parser.getOptionValue(this.cacheStats) && 0 == count) {
            System.out.print(showCacheData(mbc));
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
        if (null != parser.getOptionValue(this.pnsStats) && 0 == count) {
            System.out.print(showPnsData(mbc));
            count++;
        }
        if (null != parser.getOptionValue(this.webSocketStats) && 0 == count) {
            System.out.print(showWebSocketData(mbc));
            count++;
        }
        if (null != parser.getOptionValue(this.gcStats) && 0 == count) {
            System.out.print(showGcData(mbc));
            count++;
        }
        if (null != parser.getOptionValue(this.eventadminstats) && 0 == count) {
            System.out.print(showEventAdminData(mbc));
            count++;
        }
        if (null != parser.getOptionValue(this.allstats) && 0 == count) {
            System.out.print(showOXData(mbc));
            System.out.print(getStats(mbc, "com.openexchange.sessiond", "name", "SessionD Toolkit"));
            System.out.print(showCacheData(mbc));
            System.out.print(showThreadPoolData(mbc));
            System.out.print(getStats(mbc, "java.lang:type=OperatingSystem"));
            System.out.print(getStats(mbc, "java.lang:type=Runtime"));
            System.out.print(showMemoryPoolData(mbc));
            System.out.print(showSysThreadingData(mbc));
            System.out.print(showGrizzlyData(mbc));
            System.out.print(showGcData(mbc));
            System.out.print(getStats(mbc, "com.openexchange.usm.session", "name", "com.openexchange.usm.session.impl.USMSessionInformation"));
            System.out.print(showEventAdminData(mbc));
            System.out.print(showNioBufferData(mbc));
            System.out.println(showRtData(mbc));
            System.out.println(showPnsData(mbc));
            System.out.println(showWebSocketData(mbc));
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
        if (null != parser.getOptionValue(this.documentconverterstats) && 0 == count) {
            System.out.print(showDocumentConverterData(mbc));
            count++;
        }
        if (null != parser.getOptionValue(this.officestats) && 0 == count) {
            System.out.print(showOfficeData(mbc));
            count++;
        }
        if (null != parser.getOptionValue(this.generalstats) && 0 == count) {
            System.out.print(showGeneralMonitor(mbc));
            count++;
        }
        if (null != parser.getOptionValue(this.mailinterfacestats) && 0 == count) {
            System.out.print(showMailInterfaceMonitor(mbc));
            count++;
        }
        if (null != parser.getOptionValue(this.poolingstats) && 0 == count) {
            System.out.print(showPooling(mbc));
            count++;
        }
        if (null != parser.getOptionValue(this.callmonitorstats) && 0 == count) {
            System.out.print(showCallMonitor(mbc));
            count++;
        }
        if (null != parser.getOptionValue(this.miscstats) && 0 == count) {
            System.out.print(showGeneralMonitor(mbc));
            System.out.print(showSysThreadingData(mbc));
            count++;
        }
        if (null != parser.getOptionValue(this.overviewstats) && 0 == count) {
            System.out.print(showGeneralMonitor(mbc));
            System.out.print(showPooling(mbc));
            System.out.print(getStats(mbc, "java.lang:type=OperatingSystem"));
            count++;
        }
        if (null != parser.getOptionValue(this.niobufferstats) && 0 == count) {
            System.out.print(showNioBufferData(mbc));
            count++;
        }
        if (null != parser.getOptionValue(this.rtstats) && 0 == count) {
            System.out.print(showRtData(mbc));
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
        this.cacheStats = setShortLongOpt(
            parser,
            OPT_CACHE_STATS_SHORT,
            OPT_CACHE_STATS_LONG,
            "shows the statistics of the cache objects",
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
        this.pnsStats = setLongOpt(parser, "pnsstats", "shows the push notification service statistics", false, false);
        this.webSocketStats = setLongOpt(parser, "websocketstats", "shows the web socket statistics", false, false);
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
        this.documentconverterstats = setShortLongOpt(
            parser,
            OPT_DOCUMENTCONVERTER_STATS_SHORT,
            OPT_DOCUMENTCONVERTER_STATS_LONG,
            "shows the documentconverter stats",
            false,
            NeededQuadState.notneeded);
        this.officestats = setShortLongOpt(
            parser,
            OPT_OFFICE_STATS_SHORT,
            OPT_OFFICE_STATS_LONG,
            "shows the office stats",
            false,
            NeededQuadState.notneeded);
        this.eventadminstats = setShortLongOpt(
            parser,
            OPT_EVENT_ADMIN_STATS_SHORT,
            OPT_EVENT_ADMIN_STATS_LONG,
            "shows the OSGi EventAdmin stats",
            false,
            NeededQuadState.notneeded);
        this.generalstats = setLongOpt(parser, OPT_GENERAL_STATS_LONG, "shows the open-xchange general stats", false, false);
        this.mailinterfacestats = setLongOpt(parser, OPT_MAILINTERFACE_STATS_LONG, "shows the open-xchange mailinterface stats", false, false);
        this.poolingstats = setLongOpt(parser, OPT_POOLING_STATS_LONG, "shows the open-xchange pooling stats", false, false);
        this.callmonitorstats = setLongOpt(parser, OPT_CALLMONITOR_STATS_LONG, "shows admin.monitor Call Monitor stats", false, false);
        this.miscstats = setLongOpt(parser, OPT_MISC_STATS_LONG, "shows stats for general and threading", false, false);
        this.overviewstats = setLongOpt(parser, OPT_OVERVIEW_STATS_LONG, "shows stats for pooling and OperatingSystem", false, false);
        this.memorypoolstats = setLongOpt(parser, OPT_MEMORYPOOL_STATS_LONG, "shows stats for memory pool usage of the Java runtime", false, false);
        this.niobufferstats = setShortLongOpt(
            parser,
            OPT_NIO_BUFFER_STATS_SHORT,
            OPT_NIO_BUFFER_STATS_LONG,
            "shows the NIO buffer stats",
            false,
            NeededQuadState.notneeded);
        this.rtstats = setShortLongOpt(parser, OPT_RT_BUFFER_STATS_SHORT, OPT_RT_BUFFER_STATS_LONG, "shows stats for the realtime component", false, NeededQuadState.notneeded);


    }

    static String showMemoryPoolData(MBeanServerConnection con) throws InstanceNotFoundException, AttributeNotFoundException, IntrospectionException, MBeanException, ReflectionException, IOException, MalformedObjectNameException, NullPointerException {
        return getStats(con, "java.lang:type=MemoryPool,name=*").toString();
    }

    static String showSysThreadingData(final MBeanServerConnection con) throws InstanceNotFoundException, AttributeNotFoundException, IntrospectionException, MBeanException, ReflectionException, IOException, MalformedObjectNameException, NullPointerException {
        return getStats(con, "java.lang:type=Threading").toString();
    }

    private static String showOXData(MBeanServerConnection con) throws InstanceNotFoundException, AttributeNotFoundException, IntrospectionException, MBeanException, ReflectionException, IOException, MalformedObjectNameException, NullPointerException {
        StringBuilder sb = new StringBuilder();
        sb.append(showCallMonitor(con));
        sb.append(showGeneralMonitor(con));
        sb.append(showMailInterfaceMonitor(con));
        sb.append(showPooling(con));
        sb.append(showRateLimiterMonitor(con));
        return sb.toString();
    }

    private static StringBuffer showPooling(MBeanServerConnection con) throws IOException, InstanceNotFoundException, MBeanException, AttributeNotFoundException, ReflectionException, IntrospectionException, MalformedObjectNameException {
        return getStats(con, "com.openexchange.pooling:name=*");
    }

    private static StringBuffer showMailInterfaceMonitor(MBeanServerConnection con) throws IOException, InstanceNotFoundException, MBeanException, AttributeNotFoundException, ReflectionException, IntrospectionException, MalformedObjectNameException {
        return getStats(con, "com.openexchange.monitoring:name=MailInterfaceMonitor");
    }

    private static StringBuffer showGeneralMonitor(MBeanServerConnection con) throws IOException, InstanceNotFoundException, MBeanException, AttributeNotFoundException, ReflectionException, IntrospectionException, MalformedObjectNameException {
        return getStats(con, "com.openexchange.monitoring:name=GeneralMonitor");
    }

    private static StringBuffer showRateLimiterMonitor(MBeanServerConnection con) throws IOException, InstanceNotFoundException, MBeanException, AttributeNotFoundException, ReflectionException, IntrospectionException, MalformedObjectNameException {
        return getStats(con, "com.openexchange.monitoring:name=RateLimiterMonitor");
    }

    private static StringBuffer showCallMonitor(MBeanServerConnection con) throws IOException, InstanceNotFoundException, MBeanException, AttributeNotFoundException, ReflectionException, IntrospectionException, MalformedObjectNameException {
        return getStats(con, "com.openexchange.admin.monitor:name=CallMonitor");
    }

    private static String showCacheData(MBeanServerConnection mbc) throws NullPointerException, IOException, InvalidDataException, IllegalStateException, JMException {
        StringBuilder sb = new StringBuilder();
        sb.append(doOperationReturnString(mbc, "com.openexchange.caching:name=JCSCacheInformation!getMemoryCacheCount!Context"));
        sb.append(doOperationReturnString(mbc, "com.openexchange.caching:name=JCSCacheInformation!getMemoryCacheCount!Filestore"));
        sb.append(doOperationReturnString(mbc, "com.openexchange.caching:name=JCSCacheInformation!getMemoryCacheCount!OXDBPoolCache"));
        sb.append(doOperationReturnString(mbc, "com.openexchange.caching:name=JCSCacheInformation!getMemoryCacheCount!User"));
        sb.append(doOperationReturnString(mbc, "com.openexchange.caching:name=JCSCacheInformation!getMemoryCacheCount!UserConfiguration"));
        sb.append(doOperationReturnString(mbc, "com.openexchange.caching:name=JCSCacheInformation!getMemoryCacheCount!UserSettingMail"));
        sb.append(doOperationReturnString(mbc, "com.openexchange.caching:name=JCSCacheInformation!getMemoryCacheCount!OXFolderCache"));
        sb.append(doOperationReturnString(mbc, "com.openexchange.caching:name=JCSCacheInformation!getMemoryCacheCount!OXFolderQueryCache"));
        sb.append(doOperationReturnString(mbc, "com.openexchange.caching:name=JCSCacheInformation!getMemoryCacheCount!OXIMAPConCache"));
        sb.append(doOperationReturnString(mbc, "com.openexchange.caching:name=JCSCacheInformation!getMemoryCacheCount!OXMessageCache"));
        sb.append(doOperationReturnString(mbc, "com.openexchange.caching:name=JCSCacheInformation!getMemoryCacheCount!MailMessageCache"));
        sb.append(doOperationReturnString(mbc, "com.openexchange.caching:name=JCSCacheInformation!getMemoryCacheCount!MailConnectionCache"));
        sb.append(doOperationReturnString(mbc, "com.openexchange.caching:name=JCSCacheInformation!getMemoryCacheCount!CalendarCache"));
        sb.append(doOperationReturnString(mbc, "com.openexchange.caching:name=JCSCacheInformation!getMemoryCacheCount!SessionCache"));
        sb.append(getStats(mbc, "com.openexchange.caching", "name", "CacheEventInformation"));
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

    static String showClusterData(MBeanServerConnection mbc) throws MalformedObjectNameException, NullPointerException, IOException, InstanceNotFoundException, IntrospectionException, ReflectionException, AttributeNotFoundException, MBeanException {
        /*
         * query general information
         */
        StringBuilder sb = new StringBuilder();
        for (String type : new String[] { "HazelcastInstance", "HazelcastInstance.Node", "HazelcastInstance.EventService",
            "HazelcastInstance.ClientEngine", "HazelcastInstance.ConnectionManager" }) {
            for (ObjectInstance mbean : mbc.queryMBeans(new ObjectName("com.hazelcast:type=" + type + ",*"), null)) {
                ObjectName objectName = mbean.getObjectName();
                MBeanInfo beanInfo = mbc.getMBeanInfo(objectName);
                for (MBeanAttributeInfo attributeInfo : beanInfo.getAttributes()) {
                    if ("HazelcastInstance".equals(type) && "config".equals(attributeInfo.getName())) {
                        String value = mbc.getAttribute(mbean.getObjectName(), attributeInfo.getName()).toString();
                        for (String keyword : new String[] {
                            "groupConfig=", "properties=", "interfaces=", "tcpIpConfig=", "multicastConfig=" }) {
                            int startIdx = value.indexOf(keyword);
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
        /*
         * probe if detailed map statistics are available
         */
        ObjectName toolkitName = new ObjectName("com.openexchange.hazelcast", "name", "Hazelcast Toolkit");
        Set<ObjectInstance> mBeans = mbc.queryMBeans(toolkitName, null);
        if (null == mBeans || 0 == mBeans.size()) {
            return sb.append("[Hazelcast Toolkit MBean not available, unable to retrieve map statistics]").append(LINE_SEPARATOR).toString();
        }
        if (null == mbc.invoke(toolkitName, "getPartitionOwner", new Object[] { "probe" }, new String[] { String.class.getName() })) {
            return sb.append("[No partition owners detected, unable to retrieve map statistics]").append(LINE_SEPARATOR).toString();
        }
        Object result = mbc.invoke(toolkitName, "usesCustomPartitioning", new Object[0], new String[0]);
        if (null == result || false == Boolean.class.isInstance(result) || Boolean.TRUE.equals(result)) {
            result = mbc.invoke(toolkitName, "supportsPartitionReplicas", new Object[0], new String[0]);
            if (null == result || false == Boolean.class.isInstance(result) || Boolean.FALSE.equals(result)) {
                return sb.append("[No owner for all configured partition replicas detected, unable to retrieve map statistics]")
                    .append(LINE_SEPARATOR).toString();
            }
        }
        /*
         * query map statistics
         */
        for (String type : new String[] { "IMap", "IMultiMap", "ITopic", "IQueue" }) {
            for (ObjectInstance mbean : mbc.queryMBeans(new ObjectName("com.hazelcast:type=" + type + ",instance=*,name=*"), null)) {
                ObjectName objectName = mbean.getObjectName();
                MBeanInfo beanInfo = mbc.getMBeanInfo(objectName);
                for (MBeanAttributeInfo attributeInfo : beanInfo.getAttributes()) {
                    String name = attributeInfo.getName();
                    if (null != name && ("config".equals(name) || name.startsWith("local"))) {
                        sb.append(objectName).append(',').append(name).append(" = ");
                        try {
                            sb.append(mbc.getAttribute(objectName, name));
                        } catch (Exception e) {
                            sb.append('[').append(e.getMessage()).append(']');
                        }
                        sb.append(LINE_SEPARATOR);
                    }
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
            // Iterate over the found MBeans and print the desired attributes for this MBean. If no MBeans are found (jmx disabled) nothig
            // will be printed to stdout
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

    /**
     * Prints PNS related management info to given PrintStream if PNS's MBeans can be found.
     *
     * @param mbeanServerConnection The MBeanServerConnection to be used for querying MBeans.
     */
    static String showPnsData(final MBeanServerConnection mbeanServerConnection) throws InstanceNotFoundException, AttributeNotFoundException, IntrospectionException, MBeanException, ReflectionException, IOException, MalformedObjectNameException, NullPointerException {
        return getStats(mbeanServerConnection, "com.openexchange.pns:name=PushNotificationMBean").toString();
    }

    /**
     * Prints Web Socket related management info to given PrintStream if Web Socket's MBeans can be found.
     *
     * @param mbeanServerConnection The MBeanServerConnection to be used for querying MBeans.
     */
    static String showWebSocketData(final MBeanServerConnection mbeanServerConnection) throws InstanceNotFoundException, AttributeNotFoundException, IntrospectionException, MBeanException, ReflectionException, IOException, MalformedObjectNameException, NullPointerException {
        return getStats(mbeanServerConnection, "com.openexchange.websockets:name=WebSocketMBean").toString();
    }

    static String showDocumentConverterData(final MBeanServerConnection mbeanServerConnection) throws InstanceNotFoundException, AttributeNotFoundException, IntrospectionException, MBeanException, ReflectionException, IOException, MalformedObjectNameException, NullPointerException {
        return getStats(mbeanServerConnection, "com.openexchange.documentconverter:name=DocumentConverterInformation").toString();
    }

    static String showOfficeData(final MBeanServerConnection mbeanServerConnection) throws InstanceNotFoundException, AttributeNotFoundException, IntrospectionException, MBeanException, ReflectionException, IOException, MalformedObjectNameException, NullPointerException {
        return getStats(mbeanServerConnection, "com.openexchange.office:name=OfficeMonitoring").toString();
    }

    static String showEventAdminData(final MBeanServerConnection mbeanServerConnection) throws IOException, NullPointerException, IllegalStateException, JMException {
        return getStats(mbeanServerConnection, "org.apache.felix.eventadmin.monitoring", "type", "EventAdminMBean").toString();
    }

    /**
     * Method to prepare and display the garbage collection information.
     *
     * @param con - The MBeanServerConnection to be used for querying MBeans.
     * @throws MalformedObjectNameException - thrown while creating {@link ObjectName}
     * @throws IOException - thrown while using the {@link MBeanServerConnection}
     * @throws ReflectionException- thrown while using the {@link MBeanServerConnection}
     * @throws IntrospectionException - thrown while getting {@link MBeanInfo}
     * @throws InstanceNotFoundException - thrown while getting {@link MBeanAttributeInfo} or {@link MBeanInfo}
     * @throws MBeanException - thrown while trying to get the attribute from {@link MBeanServerConnection}
     * @throws AttributeNotFoundException - thrown while trying to get the attribute from {@link MBeanServerConnection}
     */
    private static String showGcData(MBeanServerConnection con) throws MalformedObjectNameException, IOException, InstanceNotFoundException, IntrospectionException, AttributeNotFoundException, MBeanException, ReflectionException {
        final StringBuilder sb = new StringBuilder();

        double uptimeHours = getUptimeHours(con);
        sb.append(ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE + ",hoursUptime=" + uptimeHours + "\n");

        ObjectName domainType = new ObjectName(ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE + ",*");
        Set<ObjectInstance> mbeans = con.queryMBeans(domainType, null);

        for (ObjectInstance mbean : mbeans) {
            ObjectName objectName = mbean.getObjectName();
            MBeanInfo beanInfo = con.getMBeanInfo(objectName);

            for (MBeanAttributeInfo attributeInfo : beanInfo.getAttributes()) {
                Object attribute = con.getAttribute(objectName, attributeInfo.getName());

                if (attribute != null) {
                    sb.append(objectName.getCanonicalName()).append(",").append(attributeInfo.getName()).append(" = ");

                    if (attribute instanceof CompositeDataSupport) {
                        final CompositeDataSupport compositeDataSupport = (CompositeDataSupport) attribute;
                        sb.append("[startTime=").append(compositeDataSupport.get("startTime")).append(", endTime=").append(
                            compositeDataSupport.get("endTime")).append(", GcThreadCount=").append(
                                compositeDataSupport.get("GcThreadCount")).append(", duration=").append(compositeDataSupport.get("duration")).append(
                                    "]\n");
                    } else if (attribute instanceof String[]) {
                        final String[] stringArray = (String[]) attribute;
                        sb.append(Arrays.toString(stringArray) + "\n");
                    } else if (attribute instanceof long[]) {
                        final long[] longArray = (long[]) attribute;
                        sb.append(Arrays.toString(longArray) + "\n");
                    } else {
                        sb.append(attribute.toString() + "\n");
                    }
                }
            }
        }
        return sb.toString();
    }

    /**
     * Returns the number of uptime hours of the JVM
     *
     * @param mbeanServerConnection - The MBeanServerConnection to be used for querying MBeans.
     * @throws MalformedObjectNameException - thrown while creating {@link ObjectName}
     * @throws IOException - thrown while using the {@link MBeanServerConnection}
     * @throws ReflectionException- thrown while using the {@link MBeanServerConnection}
     * @throws IntrospectionException - thrown while getting {@link MBeanInfo}
     * @throws InstanceNotFoundException - thrown while getting {@link MBeanAttributeInfo} or {@link MBeanInfo}
     * @throws MBeanException - thrown while trying to get the attribute from {@link MBeanServerConnection}
     * @throws AttributeNotFoundException - thrown while trying to get the attribute from {@link MBeanServerConnection}
     * @return double - number of uptime hours of the JVM
     */
    private static double getUptimeHours(MBeanServerConnection mbeanServerConnection) throws MalformedObjectNameException, IOException, InstanceNotFoundException, IntrospectionException, ReflectionException, MBeanException, AttributeNotFoundException {
        double uptimeHours = 0;

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
                        double valueAsDouble = value.doubleValue();
                        uptimeHours = valueAsDouble / (1000 * 60 * 60);
                    }
                }
            }
        }
        return uptimeHours;
    }


    /**
     * Show NIO buffer stats
     * @param mbeanServerConnection
     * @return
     * @throws InstanceNotFoundException
     * @throws AttributeNotFoundException
     * @throws IntrospectionException
     * @throws MalformedObjectNameException
     * @throws MBeanException
     * @throws ReflectionException
     * @throws IOException
     */
    static String showNioBufferData(final MBeanServerConnection mbeanServerConnection) throws InstanceNotFoundException, AttributeNotFoundException, IntrospectionException, MalformedObjectNameException, MBeanException, ReflectionException, IOException {
        return getStats(mbeanServerConnection, "java.nio:type=BufferPool,name=direct")
        .append(getStats(mbeanServerConnection, "java.nio:type=BufferPool,name=mapped"))
        .toString();
    }

    static String showRtData(final MBeanServerConnection mbeanServerConnection) {
        String runLoopFillSum = "RunLoopFillSum";
        StringBuilder sb = new StringBuilder();
        try {
            final ObjectName objectName = new ObjectName("com.openexchange.realtime:name=RunLoopManager");
            Object fillSum = mbeanServerConnection.getAttribute(objectName, runLoopFillSum);
            if (fillSum instanceof Map) {
                Map<?, ?> fillSums = Map.class.cast(fillSum);
                for (Entry<?, ?> entry : fillSums.entrySet()) {
                    sb.append(objectName);
                    sb.append(',');
                    sb.append(runLoopFillSum);
                    sb.append(',');
                    sb.append(entry.getKey());
                    sb.append(" = ");
                    sb.append(entry.getValue());
                    sb.append(LINE_SEPARATOR);
                }
            }
        } catch (final Exception e) {
            sb.append("com.openexchange.realtime");
            sb.append(" = ");
            sb.append('[');
            sb.append(e.toString());
            sb.append(']');
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
