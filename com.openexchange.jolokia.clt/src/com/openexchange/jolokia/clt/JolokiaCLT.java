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

package com.openexchange.jolokia.clt;

import static com.openexchange.jolokia.clt.JolokiaCLTHelper.createLongArgumentOption;
import static com.openexchange.jolokia.clt.JolokiaCLTHelper.createLongOption;
import static com.openexchange.jolokia.clt.JolokiaCLTHelper.createShortLongArgumentOption;
import static com.openexchange.jolokia.clt.JolokiaCLTHelper.createShortLongOption;
import static com.openexchange.jolokia.clt.JolokiaCLTHelper.getUsableOptionRepresentation;
import static com.openexchange.jolokia.clt.JolokiaCLTHelper.writeOperation;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeMBeanException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.jolokia.client.J4pClient;
import org.jolokia.client.exception.J4pConnectException;
import org.jolokia.client.exception.J4pException;
import org.jolokia.client.exception.J4pRemoteException;
import org.jolokia.client.request.J4pExecResponse;
import org.jolokia.client.request.J4pListRequest;
import org.jolokia.client.request.J4pListResponse;
import org.jolokia.client.request.J4pReadResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;

/**
 * {@link JolokiaCLT}
 *
 * @author <a href="mailto:felix.marx@open-xchange.com">Felix Marx</a>
 */
public class JolokiaCLT {

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

    private static final String OPT_WS_STATS_LONG = "websocketstats";

    private static final String OPT_PNS_STATS_LONG = "pnsstats";

    private static final String OPT_MAILINTERFACE_STATS_LONG = "mailinterfacestats";

    private static final String OPT_POOLING_STATS_LONG = "poolingstats";

    private static final String OPT_CALLMONITOR_STATS_LONG = "callmonitorstats";

    private static final String OPT_MISC_STATS_LONG = "misc";

    private static final String OPT_OVERVIEW_STATS_LONG = "overview";

    private static final String OPT_MEMORYPOOL_STATS_LONG = "memorypool";

    protected static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private Option xchangestats = null;

    private Option threadpoolstats = null;

    private Option runtimestats = null;

    private Option osstats = null;

    private Option threadingstats = null;

    private Option allstats = null;

    private Option showoperation = null;

    private Option dooperation = null;

    private Option memorythreadstats = null;

    private Option memorythreadstatsfull = null;

    private Option sessionStats = null;

    private Option cacheStats = null;

    private Option usmSessionStats = null;

    private Option clusterStats = null;

    private Option grizzlyStats = null;

    private Option documentconverterstats = null;

    private Option officestats = null;

    private Option eventadminstats = null;

    private Option generalstats = null;

    private Option wsstats = null;

    private Option pnsstats = null;

    private Option mailinterfacestats = null;

    private Option poolingstats = null;

    private Option callmonitorstats = null;

    private Option miscstats = null;

    private Option overviewstats = null;

    private Option memorypoolstats = null;

    private Option gcStats = null;

    private String serverUrl = "http://localhost:8009/monitoring/jolokia";

    private String user;

    private String password;

    private final Options options = new Options();;

    public static void main(String[] args) {
        final JolokiaCLT jolokiaCLT = new JolokiaCLT();
        System.exit(jolokiaCLT.runJolokiaCLT(args));
    }

    private int runJolokiaCLT(String[] args2) {

        setOptions(options);
        setFurtherOptions(options);
        CommandLineParser parser = new PosixParser();

        CommandLine cmd;
        try {
            cmd = parser.parse(options, args2, true);
            if (cmd.hasOption('h')) {
                printHelp();
                return 0;
            }
            if (cmd.hasOption("user")) {
                user = cmd.getOptionValue("user");
            }
            if (cmd.hasOption("password")) {
                password = cmd.getOptionValue("password");
            }

            // Set url if present, else use standard
            if (cmd.hasOption("jolokiaurl")) {
                serverUrl = cmd.getOptionValue("jolokiaurl");
            }

            // All values have to be set otherwise the tool won't start;
            J4pClient j4pClient = J4pClient.url(serverUrl).user(user).password(password).connectionTimeout(15000).build();

            try {
                furtherOptionsHandling(j4pClient, cmd);
            } catch (J4pConnectException j4pConE) {
                System.err.println(j4pConE);
                System.err.println("Server might be offline or not reachable");
                return 1;
            } catch (J4pRemoteException j4pRemE) {
                if (401 == j4pRemE.getStatus()) {
                    printHelp();
                    System.err.println("Check the authentification");
                } else {
                    System.err.println(j4pRemE);
                }
                return 1;
            } catch (MalformedObjectNameException e) {
                System.err.println(e);
                return 1;
            } catch (J4pException e) {
                System.err.println(e);
                return 1;
            } catch (InvalidDataException e) {
                System.err.println(e);
                return 1;
            }
            return 0;
        } catch (ParseException e1) {
            printHelp();
            return 1;
        }

    }

    protected void setOptions(Options options) {
        options.addOption(createShortLongOption('h', "help", "Prints a help text.", false));
        options.addOption(createLongArgumentOption("user", "User used for authentication", "user", true));
        options.addOption(createLongArgumentOption("password", "Password used for authentication", "password", true));
        options.addOption(createLongArgumentOption("jolokiaurl", "Url used to connect to the server", "url", false));
    }

    protected void setFurtherOptions(final Options options) {
        this.xchangestats = createShortLongOption(OPT_STATS_SHORT, OPT_STATS_LONG, "shows Open-Xchange stats", false);
        options.addOption(xchangestats);
        this.threadpoolstats = createShortLongOption(OPT_TPSTATS_SHORT, OPT_TPSTATS_LONG, "shows OX-Server threadpool stats", false);
        options.addOption(threadpoolstats);
        this.runtimestats = createShortLongOption(OPT_RUNTIME_STATS_SHORT, OPT_RUNTIME_STATS_LONG, "shows Java runtime stats", false);
        options.addOption(runtimestats);
        this.osstats = createShortLongOption(OPT_OS_STATS_SHORT, OPT_OS_STATS_LONG, "shows operating system stats", false);
        options.addOption(osstats);
        this.threadingstats = createShortLongOption(OPT_THREADING_STATS_SHORT, OPT_THREADING_STATS_LONG, "shows threading stats", false);
        options.addOption(threadingstats);
        this.allstats = createShortLongOption(OPT_ALL_STATS_SHORT, OPT_ALL_STATS_LONG, "shows all stats", false);
        options.addOption(allstats);
        this.showoperation = createShortLongOption(
            OPT_SHOWOPERATIONS_STATS_SHORT,
            OPT_SHOWOPERATIONS_STATS_LONG,
            "shows the operations for the registered beans",
            false);
        options.addOption(showoperation);
        this.dooperation = createShortLongArgumentOption(
            OPT_DOOPERATIONS_STATS_SHORT,
            OPT_DOOPERATIONS_STATS_LONG,
            "Syntax is <canonical object name (the first part from showoperatons)>!<operationname>",
            "operation",
            false);
        options.addOption(dooperation);
        this.sessionStats = createShortLongOption('i', "sessionstats", "shows the statistics of the session container", false);
        options.addOption(sessionStats);
        this.cacheStats = createShortLongOption(
            OPT_CACHE_STATS_SHORT,
            OPT_CACHE_STATS_LONG,
            "shows the statistics of the cache objects",
            false);
        options.addOption(cacheStats);
        this.usmSessionStats = createShortLongOption('u', "usmsessionstats", "shows the statistics of the USM session container", false);
        options.addOption(usmSessionStats);
        this.clusterStats = createShortLongOption('c', "clusterstats", "shows the cluster statistics", false);
        options.addOption(clusterStats);
        this.grizzlyStats = createShortLongOption('g', "grizzlystats", "shows the grizzly statistics", false);
        options.addOption(grizzlyStats);
        this.gcStats = createShortLongOption(OPT_GC_STATS_SHORT, OPT_GC_STATS_LONG, "shows the gc statistics", false);
        options.addOption(gcStats);
        this.memorythreadstats = createShortLongOption(
            OPT_MEMORY_THREADS_STATS_SHORT,
            OPT_MEMORY_THREADS_STATS_LONG,
            "shows memory usage of threads",
            false);
        options.addOption(memorythreadstats);
        this.memorythreadstatsfull = createShortLongOption(
            OPT_MEMORY_THREADS_FULL_STATS_SHORT,
            OPT_MEMORY_THREADS_FULL_STATS_LONG,
            "shows memory usage of threads including stack traces",
            false);
        options.addOption(memorythreadstatsfull);
        this.documentconverterstats = createShortLongOption(
            OPT_DOCUMENTCONVERTER_STATS_SHORT,
            OPT_DOCUMENTCONVERTER_STATS_LONG,
            "shows the documentconverter stats",
            false);
        options.addOption(documentconverterstats);
        this.officestats = createShortLongOption(OPT_OFFICE_STATS_SHORT, OPT_OFFICE_STATS_LONG, "shows the office stats", false);
        options.addOption(officestats);
        this.eventadminstats = createShortLongOption(
            OPT_EVENT_ADMIN_STATS_SHORT,
            OPT_EVENT_ADMIN_STATS_LONG,
            "shows the OSGi EventAdmin stats",
            false);
        options.addOption(eventadminstats);
        this.generalstats = createLongOption(OPT_GENERAL_STATS_LONG, "shows the open-xchange general stats", false);
        options.addOption(generalstats);
        this.pnsstats = createLongOption(OPT_PNS_STATS_LONG, "shows the push notification service statistics", false);
        options.addOption(pnsstats);
        this.wsstats = createLongOption(OPT_WS_STATS_LONG, "shows the web socket statistics", false);
        options.addOption(wsstats);
        this.mailinterfacestats = createLongOption(OPT_MAILINTERFACE_STATS_LONG, "shows the open-xchange mailinterface stats", false);
        options.addOption(mailinterfacestats);
        this.poolingstats = createLongOption(OPT_POOLING_STATS_LONG, "shows the open-xchange pooling stats", false);
        options.addOption(poolingstats);
        this.callmonitorstats = createLongOption(OPT_CALLMONITOR_STATS_LONG, "shows admin.monitor Call Monitor stats", false);
        options.addOption(callmonitorstats);
        this.miscstats = createLongOption(OPT_MISC_STATS_LONG, "shows stats for general and threading", false);
        options.addOption(miscstats);
        this.overviewstats = createLongOption(OPT_OVERVIEW_STATS_LONG, "shows stats for pooling and OperatingSystem", false);
        options.addOption(overviewstats);
        this.memorypoolstats = createLongOption(OPT_MEMORYPOOL_STATS_LONG, "shows stats for memory pool usage of the Java runtime", false);
        options.addOption(memorypoolstats);
    }

    protected void furtherOptionsHandling(final J4pClient j4pClient, CommandLine cmd) throws J4pException, MalformedObjectNameException, InvalidDataException {
        int count = 0;
        if (cmd.hasOption(getUsableOptionRepresentation(this.xchangestats))) {
            System.out.print(showOXData(j4pClient));
            count++;
        }
        if (cmd.hasOption(getUsableOptionRepresentation(this.threadpoolstats)) && 0 == count) {
            System.out.print(showThreadPoolData(j4pClient));
            count++;
        }
        if (cmd.hasOption(getUsableOptionRepresentation(this.runtimestats)) && 0 == count) {
            System.out.print(getStats(j4pClient, "java.lang:type=Runtime"));
            System.out.print(showMemoryPoolData(j4pClient));
            count++;
        }
        if (cmd.hasOption(getUsableOptionRepresentation(this.memorypoolstats)) && 0 == count) {
            System.out.print(showMemoryPoolData(j4pClient));
            count++;
        }
        if (cmd.hasOption(getUsableOptionRepresentation(this.osstats)) && 0 == count) {
            System.out.print(getStats(j4pClient, "java.lang:type=OperatingSystem"));
            count++;
        }
        if (cmd.hasOption(getUsableOptionRepresentation(this.threadingstats)) && 0 == count) {
            System.out.print(showSysThreadingData(j4pClient));
            count++;
        }
        if (cmd.hasOption(getUsableOptionRepresentation(this.sessionStats)) && 0 == count) {
            System.out.print(getStats(j4pClient, "com.openexchange.sessiond:name=SessionD Toolkit"));
            count++;
        }
        if (cmd.hasOption(getUsableOptionRepresentation(this.cacheStats)) && 0 == count) {
            System.out.print(showCacheData(j4pClient));
            count++;
        }
        if (cmd.hasOption(getUsableOptionRepresentation(this.usmSessionStats)) && 0 == count) {
            System.out.print(getStats(j4pClient, "com.openexchange.usm.session:name=USMSessionInformation"));
            count++;
        }
        if (cmd.hasOption(getUsableOptionRepresentation(this.clusterStats)) && 0 == count) {
            System.out.print(showClusterData(j4pClient));
            count++;
        }
        if (cmd.hasOption(getUsableOptionRepresentation(this.grizzlyStats)) && 0 == count) {
            System.out.print(showGrizzlyData(j4pClient));
            count++;
        }
        if (cmd.hasOption(getUsableOptionRepresentation(this.gcStats)) && 0 == count) {
            System.out.print(showGcData(j4pClient));
            count++;
        }
        if (cmd.hasOption(getUsableOptionRepresentation(this.eventadminstats)) && 0 == count) {
            System.out.print(showEventAdminData(j4pClient));
            count++;
        }
        if (cmd.hasOption(getUsableOptionRepresentation(this.wsstats)) && 0 == count) {
            System.out.print(showWebSocketData(j4pClient));
            count++;
        }
        if (cmd.hasOption(getUsableOptionRepresentation(this.pnsstats)) && 0 == count) {
            System.out.print(showPnsData(j4pClient));
            count++;
        }
        if (cmd.hasOption(getUsableOptionRepresentation(this.allstats)) && 0 == count) {
            System.out.print(showOXData(j4pClient));
            System.out.print(getStats(j4pClient, "com.openexchange.sessiond", "name", "SessionD Toolkit"));
            System.out.print(showCacheData(j4pClient));
            System.out.print(showThreadPoolData(j4pClient));
            System.out.print(getStats(j4pClient, "java.lang:type=OperatingSystem"));
            System.out.print(getStats(j4pClient, "java.lang:type=Runtime"));
            System.out.print(showMemoryPoolData(j4pClient));
            System.out.print(showSysThreadingData(j4pClient));
            System.out.print(showGrizzlyData(j4pClient));
            System.out.print(showGcData(j4pClient));
            System.out.print(getStats(
                j4pClient,
                "com.openexchange.usm.session",
                "name",
                "com.openexchange.usm.session.impl.USMSessionInformation"));
            System.out.println(showEventAdminData(j4pClient));
            count++;
        }
        if (cmd.hasOption(getUsableOptionRepresentation(this.showoperation)) && 0 == count) {
            System.out.print(showOperations(j4pClient));
            count++;
        }
        final String operation = cmd.getOptionValue(getUsableOptionRepresentation(this.dooperation));
        if (null != operation && 0 == count) {
            final Object result = doOperation(j4pClient, operation);
            if (null != result) {
                System.out.println(JolokiaCLTHelper.writeObject(result));
            }
            count++;
        }
        if (cmd.hasOption(getUsableOptionRepresentation(this.memorythreadstats)) && 0 == count) {
            System.out.print(showThreadMemory(j4pClient, false));
            count++;
        }
        if (cmd.hasOption(getUsableOptionRepresentation(this.memorythreadstatsfull)) && 0 == count) {
            System.out.print(showThreadMemory(j4pClient, true));
            count++;
        }
        if (cmd.hasOption(getUsableOptionRepresentation(this.documentconverterstats)) && 0 == count) {
            System.out.print(showDocumentConverterData(j4pClient));
            count++;
        }
        if (cmd.hasOption(getUsableOptionRepresentation(this.officestats)) && 0 == count) {
            System.out.print(showOfficeData(j4pClient));
            count++;
        }
        if (cmd.hasOption(getUsableOptionRepresentation(this.generalstats)) && 0 == count) {
            System.out.print(showGeneralMonitor(j4pClient));
            count++;
        }
        if (cmd.hasOption(getUsableOptionRepresentation(this.mailinterfacestats)) && 0 == count) {
            System.out.print(showMailInterfaceMonitor(j4pClient));
            count++;
        }
        if (cmd.hasOption(getUsableOptionRepresentation(this.poolingstats)) && 0 == count) {
            System.out.print(showPooling(j4pClient));
            count++;
        }
        if (cmd.hasOption(getUsableOptionRepresentation(this.callmonitorstats)) && 0 == count) {
            System.out.print(showCallMonitor(j4pClient));
            count++;
        }
        if (cmd.hasOption(getUsableOptionRepresentation(this.miscstats)) && 0 == count) {
            System.out.print(showGeneralMonitor(j4pClient));
            System.out.print(showSysThreadingData(j4pClient));
            count++;
        }
        if (cmd.hasOption(getUsableOptionRepresentation(this.overviewstats)) && 0 == count) {
            System.out.print(showGeneralMonitor(j4pClient));
            System.out.print(showPooling(j4pClient));
            System.out.print(getStats(j4pClient, "java.lang:type=OperatingSystem"));
            count++;
        }
        if (0 == count) {
            System.err.println(new StringBuilder("No option selected (").append(OPT_STATS_LONG).append(", ").append(OPT_RUNTIME_STATS_LONG).append(
                ", ").append(OPT_OS_STATS_LONG).append(", ").append(OPT_THREADING_STATS_LONG).append(", ").append(OPT_ALL_STATS_LONG).append(
                    ", sessionstats)"));
            printHelp();
        } else if (count > 1) {
            System.err.println("More than one of the stat options given. Using the first one only");
        }
        System.out.println("Done");
    }

    /**
     * @param j4pClient
     * @param string
     * @return
     * @throws MalformedObjectNameException
     * @throws J4pException
     */
    private static String getStats(J4pClient j4pClient, String pObjectName, String... pAttribute) throws MalformedObjectNameException, J4pException {
        J4pReadResponse resp = JolokiaCLTHelper.getReadResponse(j4pClient, pObjectName, pAttribute);

        final StringBuffer retval = new StringBuffer();
        if (null != resp) {
            for (final ObjectName obj : resp.getObjectNames()) {
                if (null == obj) {
                    continue;
                }
                final Collection<String> info = resp.getAttributes(obj);
                final String ocname = obj.getCanonicalName();
                if (info.size() > 0) {
                    for (final String element : info) {
                        try {
                            final Object o = resp.getValue(obj, element);
                            if (o != null) {
                                final StringBuilder sb = new StringBuilder(ocname).append(",").append(element).append(" = ");
                                retval.append(sb.append(JolokiaCLTHelper.writeObject(o)).append(LINE_SEPARATOR));
                            }
                        } catch (final RuntimeMBeanException e) {
                            // If there was an error getting the attribute we just omit that attribute
                        }
                    }
                }
            }
        }
        return retval.toString();
    }

    static String showThreadMemory(J4pClient j4pClient, boolean stacktrace) throws MalformedObjectNameException, J4pRemoteException, J4pException {
        StringBuilder sb = new StringBuilder();
        final J4pReadResponse allThreadIdsResponse = JolokiaCLTHelper.getReadResponse(j4pClient, ManagementFactory.THREAD_MXBEAN_NAME, "AllThreadIds");
        JSONArray allThreadIds = null;
        if (allThreadIdsResponse.getValue() instanceof JSONArray) {
            allThreadIds = allThreadIdsResponse.getValue();
        }
        if (null == allThreadIds) {
            System.err.println("No ThreadIds returned");
            return sb.toString();
        }
        // final ObjectName srvThrdName = new ObjectName(ManagementFactory.THREAD_MXBEAN_NAME);
        final J4pExecResponse allocatedBytesResponse;
        final J4pExecResponse cpuTimeResponse;
        final J4pExecResponse userTimeResponse;
        final J4pExecResponse threadInfoResponse;
        JSONArray threadInfo = null;

        long[] allocatedBytes = null;
        long[] cpuTime = null;
        long[] userTime = null;

        if (stacktrace) {
            threadInfoResponse = JolokiaCLTHelper.getExecResponse(
                j4pClient,
                ManagementFactory.THREAD_MXBEAN_NAME,
                "getThreadInfo" + "([J,int)",
                allThreadIds,
                Integer.MAX_VALUE);
        } else {
            threadInfoResponse = JolokiaCLTHelper.getExecResponse(j4pClient, ManagementFactory.THREAD_MXBEAN_NAME, "getThreadInfo" + "([J)", allThreadIds);
        }
        if (threadInfoResponse.getValue() instanceof JSONArray) {
            threadInfo = threadInfoResponse.getValue();
        } else {
            System.err.println("ThreadInfo did not return usable results");
            return sb.toString();
        }
        allocatedBytesResponse = JolokiaCLTHelper.getExecResponse(
            j4pClient,
            ManagementFactory.THREAD_MXBEAN_NAME,
            "getThreadAllocatedBytes" + "([J)",
            allThreadIds);
        allocatedBytes = extractLongFromJsonArray(allocatedBytesResponse, allThreadIds.size());
        cpuTimeResponse = JolokiaCLTHelper.getExecResponse(j4pClient, ManagementFactory.THREAD_MXBEAN_NAME, "getThreadCpuTime" + "([J)", allThreadIds);
        cpuTime = extractLongFromJsonArray(cpuTimeResponse, allThreadIds.size());
        userTimeResponse = JolokiaCLTHelper.getExecResponse(j4pClient, ManagementFactory.THREAD_MXBEAN_NAME, "getThreadUserTime" + "([J)", allThreadIds);
        userTime = extractLongFromJsonArray(userTimeResponse, allThreadIds.size());

        if (allocatedBytes.length != cpuTime.length || cpuTime.length != userTime.length || userTime.length != threadInfo.size()) {
            System.err.println("Different results returned");
            return sb.toString();
        }
        final ArrayList<ThreadOutputElem> arrayList = new ArrayList<ThreadOutputElem>();
        sb.append("ThreadID, Name, AllocatedBytes, CpuTime, UserTime");
        if (stacktrace) {
            sb.append(", StackTrace");
        }
        sb.append(LINE_SEPARATOR);
        for (int i = 0; i < allThreadIds.size(); i++) {
            if (stacktrace) {
                JSONArray stackTrace = null;
                if ((((JSONObject) (threadInfo.get(i))).get("stackTrace")) instanceof JSONArray) {
                    stackTrace = (JSONArray) (((JSONObject) (threadInfo.get(i))).get("stackTrace"));
                }
                arrayList.add(new ThreadOutputElem(
                    (Long) allThreadIds.get(i),
                    (String) (((JSONObject) (threadInfo.get(i))).get("threadName")),
                    allocatedBytes[i],
                    cpuTime[i],
                    userTime[i],
                    stackTrace));
            } else {
                arrayList.add(new ThreadOutputElem(
                    (Long) allThreadIds.get(i),
                    (String) (((JSONObject) (threadInfo.get(i))).get("threadName")),
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
                sb.append(Arrays.toString(parseStacktracJsonArrayToString(elem.getStackTrace())));
            }
            sb.append(LINE_SEPARATOR);
        }
        return sb.toString();
    }

    private static String[] parseStacktracJsonArrayToString(JSONArray jsonArray) {
        String[] returnString = new String[0];

        if (null != jsonArray) {
            returnString = new String[jsonArray.size()];
            for (int i = 0; i < jsonArray.size(); i++) {
                StringBuilder sb = new StringBuilder();
                JSONObject jo = (JSONObject) jsonArray.get(i);
                sb.append(jo.get("className")).append(jo.get("methodName"));
                sb.append("(");
                if ((Boolean) jo.get("nativeMethod")) {
                    sb.append("Native Method");
                } else {
                    sb.append(jo.get("fileName")).append(":").append(jo.get("lineNumber"));
                }
                sb.append(")");
                returnString[i] = sb.toString();
            }
        }
        return returnString;
    }

    private static long[] extractLongFromJsonArray(final J4pExecResponse execResponse, int size) {
        long[] longArray = null;
        if (execResponse.getValue() instanceof JSONArray) {
            JSONArray jsonArray = execResponse.getValue();
            longArray = new long[jsonArray.size()];
            for (int i = 0; i < jsonArray.size(); i++) {
                longArray[i] = (Long) jsonArray.get(i);
            }
        }
        if (null == longArray) {
            longArray = new long[size];
            Arrays.fill(longArray, 0);
        }
        return longArray;
    }

    static String showMemoryPoolData(J4pClient j4pClient) throws MalformedObjectNameException, J4pException {
        return getStats(j4pClient, "java.lang:type=MemoryPool,name=*").toString();
    }

    static String showSysThreadingData(final J4pClient j4pClient) throws MalformedObjectNameException, J4pException {
        return getStats(j4pClient, "java.lang:type=Threading").toString();
    }

    private static String showOXData(J4pClient j4pClient) throws MalformedObjectNameException, J4pException {
        StringBuilder sb = new StringBuilder();
        sb.append(showCallMonitor(j4pClient));
        sb.append(showGeneralMonitor(j4pClient));
        sb.append(showMailInterfaceMonitor(j4pClient));
        sb.append(showPooling(j4pClient));
        return sb.toString();
    }

    private static String showPooling(J4pClient j4pClient) throws MalformedObjectNameException, J4pException {
        return getStats(j4pClient, "com.openexchange.pooling:name=*");
    }

    private static String showMailInterfaceMonitor(J4pClient j4pClient) throws MalformedObjectNameException, J4pException {
        return getStats(j4pClient, "com.openexchange.monitoring:name=MailInterfaceMonitor");
    }

    private static String showGeneralMonitor(J4pClient j4pClient) throws MalformedObjectNameException, J4pException {
        return getStats(j4pClient, "com.openexchange.monitoring:name=GeneralMonitor");
    }

    private static String showCallMonitor(J4pClient j4pClient) throws MalformedObjectNameException, J4pException {
        return getStats(j4pClient, "com.openexchange.admin.monitor:name=CallMonitor");
    }

    private static String showCacheData(J4pClient j4pClient) throws MalformedObjectNameException, J4pRemoteException, InvalidDataException, J4pException {
        StringBuilder sb = new StringBuilder();
        sb.append(doOperationReturnString(j4pClient, "com.openexchange.caching:name=JCSCacheInformation!getMemoryCacheCount(java.lang.String)!Context"));
        sb.append(doOperationReturnString(j4pClient, "com.openexchange.caching:name=JCSCacheInformation!getMemoryCacheCount(java.lang.String)!Filestore"));
        sb.append(doOperationReturnString(j4pClient, "com.openexchange.caching:name=JCSCacheInformation!getMemoryCacheCount(java.lang.String)!OXDBPoolCache"));
        sb.append(doOperationReturnString(j4pClient, "com.openexchange.caching:name=JCSCacheInformation!getMemoryCacheCount(java.lang.String)!User"));
        sb.append(doOperationReturnString(
            j4pClient,
            "com.openexchange.caching:name=JCSCacheInformation!getMemoryCacheCount(java.lang.String)!UserConfiguration"));
        sb.append(doOperationReturnString(
            j4pClient,
            "com.openexchange.caching:name=JCSCacheInformation!getMemoryCacheCount(java.lang.String)!UserSettingMail"));
        sb.append(doOperationReturnString(j4pClient, "com.openexchange.caching:name=JCSCacheInformation!getMemoryCacheCount(java.lang.String)!OXFolderCache"));
        sb.append(doOperationReturnString(
            j4pClient,
            "com.openexchange.caching:name=JCSCacheInformation!getMemoryCacheCount(java.lang.String)!OXFolderQueryCache"));
        sb.append(doOperationReturnString(j4pClient, "com.openexchange.caching:name=JCSCacheInformation!getMemoryCacheCount(java.lang.String)!OXIMAPConCache"));
        sb.append(doOperationReturnString(j4pClient, "com.openexchange.caching:name=JCSCacheInformation!getMemoryCacheCount(java.lang.String)!OXMessageCache"));
        sb.append(doOperationReturnString(
            j4pClient,
            "com.openexchange.caching:name=JCSCacheInformation!getMemoryCacheCount(java.lang.String)!MailMessageCache"));
        sb.append(doOperationReturnString(
            j4pClient,
            "com.openexchange.caching:name=JCSCacheInformation!getMemoryCacheCount(java.lang.String)!MailConnectionCache"));
        sb.append(doOperationReturnString(j4pClient, "com.openexchange.caching:name=JCSCacheInformation!getMemoryCacheCount(java.lang.String)!CalendarCache"));
        sb.append(doOperationReturnString(j4pClient, "com.openexchange.caching:name=JCSCacheInformation!getMemoryCacheCount(java.lang.String)!SessionCache"));
        return sb.toString();
    }

    protected static String doOperationReturnString(J4pClient j4pClient, final String fullqualifiedoperationname) throws MalformedObjectNameException, J4pRemoteException, InvalidDataException, J4pException {
        Object opObject = doOperation(j4pClient, fullqualifiedoperationname);
        if (null != opObject) {
            final StringBuilder retval = new StringBuilder();
            retval.append(fullqualifiedoperationname).append(" = ");
            return retval.append(JolokiaCLTHelper.writeObject(opObject)).append(LINE_SEPARATOR).toString();
        } else {
            return "";
        }
    }

    static String showThreadPoolData(final J4pClient j4pClient) throws MalformedObjectNameException, J4pException {
        return getStats(j4pClient, "com.openexchange.threadpool:name=ThreadPoolInformation").toString();
    }

    static String showOperations(final J4pClient j4pClient) throws J4pException {
        StringBuilder sb = new StringBuilder();
        J4pListRequest listRequest = new J4pListRequest(null, (String) null);
        J4pListResponse listResponse = j4pClient.execute(listRequest);
        JSONObject list = listResponse.getValue();
        @SuppressWarnings("unchecked") Set<Map.Entry<String, JSONObject>> entrySet = list.entrySet();
        for (Entry<String, JSONObject> classEntry : entrySet) {
            String className = classEntry.getKey();
            JSONObject classJsonObject = classEntry.getValue();
            @SuppressWarnings("unchecked")
            Set<Map.Entry<String, JSONObject>> classEntrySet = classJsonObject.entrySet();
            for (Entry<String, JSONObject> innerE : classEntrySet) {
                String name = innerE.getKey();
                JSONObject innerJo = innerE.getValue();
                JSONObject operations = (JSONObject) innerJo.get("op");
                if (operations != null) {
                    @SuppressWarnings("unchecked")
                    final Set<Map.Entry<String, ?>> operationsEntrySet = operations.entrySet();
                    for (Entry<String, ?> operationE : operationsEntrySet) {
                        final String operationName = operationE.getKey();
                        sb.append(writeOperation(className, name, operationE, operationName));
                    }

                }
            }

        }
        return sb.toString();
    }

    static String showClusterData(J4pClient j4pClient) throws MalformedObjectNameException, J4pException {
        // general info
        StringBuilder sb = new StringBuilder();
        for (String type : new String[] {
            "HazelcastInstance", "HazelcastInstance.Node", "HazelcastInstance.ClientEngine", "HazelcastInstance.ConnectionManager" }) {
            sb.append(getStats(j4pClient, "com.hazelcast:type=" + type + ",*"));
        }
        for (String type : new String[] { "IMap", "IMultiMap", "ITopic", "IQueue" }) {
            sb.append(getStats(j4pClient, "com.hazelcast:type=" + type + ",instance=*,name=*"));
        }
        return sb.toString();
    }

    /**
     * Print Grizzly related management info to given PrintStream if Grizzly's MBeans can be found.
     *
     * @param j4pClient The MBeanServerConnection to be used for querying MBeans.
     * @param out the {@link PrintStream} to write the output to.
     * @throws J4pException
     * @throws IOException
     * @throws MalformedObjectNameException
     * @throws NullPointerException
     */
    static String showGrizzlyData(final J4pClient j4pClient) throws MalformedObjectNameException, J4pException {
        StringBuilder sb = new StringBuilder();
        // Iterate over the MBeans we are interested in, query by objectName
        for (final GrizzlyMBean grizzlyMBean : GrizzlyMBean.values()) {
            sb.append(getStats(j4pClient, grizzlyMBean.getObjectName()));
        }
        return sb.toString();
    }

    static String showDocumentConverterData(final J4pClient j4pClient) throws MalformedObjectNameException, J4pException {
        return getStats(j4pClient, "com.openexchange.documentconverter:name=DocumentConverterInformation").toString();
    }

    static String showOfficeData(final J4pClient j4pClient) throws MalformedObjectNameException, J4pException {
        return getStats(j4pClient, "com.openexchange.office:name=OfficeMonitoring").toString();
    }

    static String showEventAdminData(final J4pClient j4pClient) throws MalformedObjectNameException, J4pException {
        return getStats(j4pClient, "org.apache.felix.eventadmin.monitoring", "type", "EventAdminMBean").toString();
    }

    static String showWebSocketData(final J4pClient j4pClient) throws MalformedObjectNameException, J4pException {
        return getStats(j4pClient, "com.openexchange.websockets:name=WebSocketMBean").toString();
    }

    static String showPnsData(final J4pClient j4pClient) throws MalformedObjectNameException, J4pException {
        return getStats(j4pClient, "com.openexchange.pns:name=PushNotificationMBean").toString();
    }

    /**
     * Method to prepare and display the garbage collection information.
     *
     * @param j4pClient - The MBeanServerConnection to be used for querying MBeans.
     * @throws J4pException
     * @throws MalformedObjectNameException - thrown while creating {@link ObjectName}
     * @throws IOException - thrown while using the {@link MBeanServerConnection}
     * @throws ReflectionException- thrown while using the {@link MBeanServerConnection}
     * @throws IntrospectionException - thrown while getting {@link MBeanInfo}
     * @throws InstanceNotFoundException - thrown while getting {@link MBeanAttributeInfo} or {@link MBeanInfo}
     * @throws MBeanException - thrown while trying to get the attribute from {@link MBeanServerConnection}
     * @throws AttributeNotFoundException - thrown while trying to get the attribute from {@link MBeanServerConnection}
     */
    private static String showGcData(J4pClient j4pClient) throws MalformedObjectNameException, J4pException {
        final StringBuilder sb = new StringBuilder();

        double uptimeHours = getUptimeHours(j4pClient);
        sb.append(ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE + ",hoursUptime=" + uptimeHours + "\n");

        sb.append(getStats(j4pClient, ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE + ",*"));
        return sb.toString();
    }

    private static double getUptimeHours(J4pClient j4pClient) throws MalformedObjectNameException, J4pException {
        double uptimeHours = 0;
        try {
            J4pReadResponse resp = JolokiaCLTHelper.getReadResponse(j4pClient, ManagementFactory.RUNTIME_MXBEAN_NAME, "Uptime");
            if (null != resp) {
                Long value = resp.getValue();
                double valueAsDouble = value.doubleValue();
                uptimeHours = valueAsDouble / (1000 * 60 * 60);
            }

        } catch (NumberFormatException e) {
            // Do nothing
        }
        return uptimeHours;
    }

    private void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(
            "showruntimestatsoverhttp",
            "Prints out jmx information collected by jolokia client over http.",
            this.options,
            null,
            false);
    }

    protected static Object doOperation(final J4pClient j4pClient, final String fullqualifiedoperationname) throws InvalidDataException, MalformedObjectNameException, J4pRemoteException, J4pException {
        final String[] split = fullqualifiedoperationname.split("!");
        if (2 == split.length) {
            final String objectName = split[0];
            J4pExecResponse resp = JolokiaCLTHelper.getExecResponse(j4pClient, objectName, split[1]);
            if (resp != null) {
                return resp.getValue();
            }
        } else if (2 <= split.length) {
            final String objectName = split[0];
            final Object[] params = new Object[split.length - 2];
            System.arraycopy(split, 2, params, 0, split.length - 2);
            J4pExecResponse resp = JolokiaCLTHelper.getExecResponse(j4pClient, objectName, split[1], params);
            if (resp != null) {
                return resp.getValue();
            }
        } else {
            throw new InvalidDataException("The given operationname is not valid. It couldn't be split at \"!\"");
        }
        return null;
    }

}
