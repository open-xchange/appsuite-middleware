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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.monitoring.internal.memory;

import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.slf4j.Logger;
import com.openexchange.java.Strings;

/**
 * {@link MemoryMonitoring} - Heavily driven By <a href="http://www.torsten-horn.de/techdocs/jmx-gc.htm">Torsten Horn's article about GC monitoring and more</a>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class MemoryMonitoring implements Runnable {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(MemoryMonitoring.class);

    private final DecimalFormat decimalFormatPercent;
    private final DecimalFormat decimalFormatCount;
    private final Map<String, Measurement> lastMeasurement;
    private final MBeanServer server;
    private final int periodMinutes;
    private final double threshold;

    /**
     * Initializes a new {@link MemoryMonitoring}.
     */
    public MemoryMonitoring(int periodMinutes, double threshold) {
        this(periodMinutes, threshold, ManagementFactory.getPlatformMBeanServer());
    }

    /**
     * Initializes a new {@link MemoryMonitoring}.
     */
    public MemoryMonitoring(int periodMinutes, double threshold, MBeanServer server) {
        super();
        this.threshold = threshold;
        if (periodMinutes <= 0) {
            throw new IllegalArgumentException("periodMinutes must be greater than zero");
        }
        if (threshold <= 0) {
            throw new IllegalArgumentException("threshold must be greater than zero");
        }
        this.periodMinutes = periodMinutes;
        this.server = server;
        lastMeasurement = new HashMap<>(4);
        decimalFormatPercent = new DecimalFormat("0.0");
        decimalFormatCount = new DecimalFormat("#,##0");
    }

    @Override
    public void run() {
        try {
            GarbageCollectionInfos infos = getGarbageCollectionInfos(false);
            double gcTimePercentSum = infos.gcTimePercentSum;
            if (gcTimePercentSum > threshold) {
                /*-
                 * Example:
                 *
                 *    High GC activity detected!!!
                 *    Garbage collection consumed 12.5% of uptime within 5 minutes. Thereof:
                 *        ParNewGc consumed 8.5% (4,533 collections)
                 *        ConcuurentMarkSweep consumed 4.0% (524 collections)
                 *
                 */
                StringBuilder msg = new StringBuilder(256);
                msg.append(Strings.getLineSeparator()).append("\tHigh GC activity detected!!!");
                msg.append(Strings.getLineSeparator()).append("\tGarbage collection consumed ").append(decimalFormatPercent.format(gcTimePercentSum)).append("% of uptime within ").append(getTimeInfo()).append(". Thereof:");
                for (GarbageCollectionInfo gcInfo : infos.gcSingles) {
                    msg.append(Strings.getLineSeparator()).append("\t\t").append(gcInfo.gcName).append(" consumed ").append(decimalFormatPercent.format(gcInfo.gcTimePercent)).append(" (").append(decimalFormatCount.format(gcInfo.gcCountPerPeriod)).append(" collections)");
                }
                msg.append(Strings.getLineSeparator());
                LOG.warn(msg.toString());
            } else {
                LOG.info("{}\tGarbage collection consumed {}% of uptime within {}. All fine.{}", Strings.getLineSeparator(), decimalFormatPercent.format(gcTimePercentSum), getTimeInfo(), Strings.getLineSeparator());
            }
        } catch (Exception e) {
            LOG.warn("Failed to collect Garbage Collection information", e);
        }
    }

    private String getTimeInfo() {
        if (1 == periodMinutes) {
            return "1 minute";
        }
        return new StringBuilder(10).append(periodMinutes).append(" minutes").toString();
    }

    private synchronized GarbageCollectionInfos getGarbageCollectionInfos(boolean withCpuTime) throws Exception {
        // Determine current runtime from this JVM
        long rtUptimeMs = getRuntimeMXBean().getUptime();

        // Grab available GC MBeans
        List<GarbageCollectorMXBean> gcMXBeans = getGarbageCollectorMXBeans();

        // Per GC type
        GarbageCollectionInfos gcInfos = new GarbageCollectionInfos();
        for (GarbageCollectorMXBean gc : gcMXBeans) {
            GarbageCollectionInfo gcInfo = new GarbageCollectionInfo();

            gcInfo.gcName = gc.getName();

            Measurement gcLast = lastMeasurement.get(gcInfo.gcName);
            if (gcLast == null) {
                // First run...
                gcInfo.gcCountPerPeriod = gc.getCollectionCount() * periodMinutes * 60 * 1000 / rtUptimeMs;
                gcInfo.gcTimePercent = (gc.getCollectionTime() * 1000 / rtUptimeMs) / 10.;
            } else {
                gcInfo.gcCountPerPeriod = gc.getCollectionCount() - gcLast.first;
                gcInfo.gcTimePercent = ((gc.getCollectionTime() - gcLast.second) / (periodMinutes * 60)) / 10.;

                if (gcInfo.gcCountPerPeriod < 0 || gcInfo.gcTimePercent < 0) {
                    gcInfo.gcCountPerPeriod = gc.getCollectionCount() * periodMinutes * 60 * 1000 / rtUptimeMs;
                    gcInfo.gcTimePercent = (gc.getCollectionTime() * 1000 / rtUptimeMs) / 10.;
                }
            }
            lastMeasurement.put(gcInfo.gcName, new Measurement(gc.getCollectionCount(), gc.getCollectionTime()));

            gcInfos.gcSingles.add(gcInfo);
            gcInfos.gcTimePercentSum += gcInfo.gcTimePercent;
        }

        // CPU time
        if (withCpuTime) {
            gcInfos.cpuTimePercent = calculateCpuTimePercent(rtUptimeMs);
        }

        // Done
        return gcInfos;
    }

    private int calculateCpuTimePercent(long rtUptimeMs) {
        try {
            Long cpuTime = (Long) server.getAttribute(new ObjectName("java.lang:type=OperatingSystem"), "ProcessCpuTime");
            if (cpuTime == null) {
                return -1;
            }

            Measurement lastCpuTimeVals = lastMeasurement.get("ProcessCpuTime");
            lastMeasurement.put("ProcessCpuTime", new Measurement(rtUptimeMs, cpuTime.longValue()));
            OperatingSystemMXBean op = getOperatingSystemMXBean();
            long cpuCount = Math.max(1, op.getAvailableProcessors());
            long lastRtUptimeMs = 0;
            long lastCpuTime = 0;
            if (lastCpuTimeVals != null) {
                lastRtUptimeMs = lastCpuTimeVals.first;
                lastCpuTime = lastCpuTimeVals.second;
            }
            return (int) Math.min(99, (cpuTime.longValue() - lastCpuTime) / ((rtUptimeMs - lastRtUptimeMs) * cpuCount * 10000));
        } catch (Exception x) {
            return -1;
        }
    }

    // -------------------------------- Utility methods to obtain/access MBeans -------------------------------

    private RuntimeMXBean getRuntimeMXBean() throws IOException {
        return ManagementFactory.newPlatformMXBeanProxy(server, ManagementFactory.RUNTIME_MXBEAN_NAME, RuntimeMXBean.class);
    }

    // Lies OperatingSystem-MXBean von Remote
    private OperatingSystemMXBean getOperatingSystemMXBean() throws IOException {
        return ManagementFactory.newPlatformMXBeanProxy(server, ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, OperatingSystemMXBean.class);
    }

    private List<GarbageCollectorMXBean> getGarbageCollectorMXBeans() throws MalformedObjectNameException, NullPointerException, IOException {
        ObjectName gcAllObjectName = new ObjectName(ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE + ",*");
        Set<ObjectName> gcMXBeanObjectNames = server.queryNames(gcAllObjectName, null);

        List<GarbageCollectorMXBean> gcMXBeans = new ArrayList<GarbageCollectorMXBean>(gcMXBeanObjectNames.size());
        for (ObjectName on : gcMXBeanObjectNames) {
            GarbageCollectorMXBean gc = ManagementFactory.newPlatformMXBeanProxy(server, on.getCanonicalName(), GarbageCollectorMXBean.class);
            gcMXBeans.add(gc);
        }
        return gcMXBeans;
    }

    // --------------------------------- Helper classes ----------------------------------------------------------

    /** Information for all available GC types and CPU time */
    private static final class GarbageCollectionInfos {

        List<GarbageCollectionInfo> gcSingles = new ArrayList<GarbageCollectionInfo>(4);
        double gcTimePercentSum;
        long cpuTimePercent;

        GarbageCollectionInfos() {
            super();
        }
    }

    /** Information for a single GC type */
    private static final class GarbageCollectionInfo {

        String gcName;
        long gcCountPerPeriod;
        double gcTimePercent;

        GarbageCollectionInfo() {
            super();
        }
    }

    /** A measurement of two long values */
    private static final class Measurement {

        final long first;
        final long second;

        Measurement(long first, long second) {
            super();
            this.first = first;
            this.second = second;
        }
    }

}
