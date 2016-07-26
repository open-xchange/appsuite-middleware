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

package com.openexchange.report.appsuite.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.ILock;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;
import com.openexchange.logging.LogLevelService;
import com.openexchange.report.appsuite.ContextReport;
import com.openexchange.report.appsuite.ContextReportCumulator;
import com.openexchange.report.appsuite.ReportExceptionCodes;
import com.openexchange.report.appsuite.ReportFinishingTouches;
import com.openexchange.report.appsuite.ReportService;
import com.openexchange.report.appsuite.ReportSystemHandler;
import com.openexchange.report.appsuite.jobs.AnalyzeContextBatch;
import com.openexchange.report.appsuite.serialization.PortableReport;
import com.openexchange.report.appsuite.serialization.Report;
import com.openexchange.report.appsuite.serialization.ReportConfigs;

/**
 * The {@link HazelcastReportService} class uses hazelcast to coordinate the clusters efforts in producing reports. It maintains the following resources via hazelcast:
 *
 * A Map "com.openexchange.report.Reports" that contains an entry per reportType of the last successful report run for that type
 * A Map "com.openexchange.report.PendingReports.[reportType] per reportType that contains currently running reports.
 *
 *
 * A Lock com.openexchange.report.Reports.[reportType] that acts as a cluster-wide lock for the given resourceType to coordinate
 * when to set up a new report
 * A Lock com.openexchange.report.Reports.Merge.[reportType] that protects the merge operations for the global report
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 */
public class HazelcastReportService extends AbstractReportService {

    //--------------------Class Attributes--------------------
    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(HazelcastReportService.class);

    private final ReportService delegate;

    //--------------------Constructor--------------------
    public HazelcastReportService(ReportService delegate) {
        this.delegate = delegate;
    }

    //--------------------Public override methods--------------------
    /**
     * {@inheritDoc}
     */
    @Override
    public Report getLastReport(String reportType) {
        if (useLocal()) {
            return delegate.getLastReport(reportType);
        }

        IMap<String, PortableReport> map = Services.getService(HazelcastInstance.class).getMap(REPORTS_KEY);
        PortableReport portableReport = map.get(reportType);
        return PortableReport.unwrap(portableReport);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Report[] getPendingReports(String reportType) {
        if (useLocal()) {
            return delegate.getPendingReports(reportType);
        }
        // Simply look up the pending reports for this type in a hazelcast map
        HazelcastInstance hazelcast = Services.getService(HazelcastInstance.class);

        IMap<String, PortableReport> pendingReports = hazelcast.getMap(PENDING_REPORTS_PRE_KEY + reportType);
        Collection<PortableReport> reportCol = pendingReports.values();

        return PortableReport.unwrap(reportCol);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flushPending(String uuid, String reportType) {
        if (useLocal()) {
            delegate.flushPending(uuid, reportType);
            return;
        }

        // Remove entries from hazelcasts pending maps, so a report can be started again
        HazelcastInstance hazelcast = Services.getService(HazelcastInstance.class);
        IMap<String, PortableReport> pendingReports = hazelcast.getMap(PENDING_REPORTS_PRE_KEY + reportType);

        ILock lock = hazelcast.getLock(REPORTS_MERGE_PRE_KEY + reportType);

        pendingReports.remove(uuid);
        lock.destroy();
    }

    // Called by the AnalyzeContextBatch for every context so the context specific entries can be
    // added to the global report. Adds context to general report and mark context as done
    @Override
    public void finishContext(ContextReport contextReport) throws OXException {
        if (useLocal()) {
            delegate.finishContext(contextReport);
            return;
        }
        HazelcastInstance hazelcast = Services.getService(HazelcastInstance.class);

        String reportType = contextReport.getType();

        // Retrieve general report and merge in the contextReport
        IMap<String, PortableReport> pendingReports = hazelcast.getMap(PENDING_REPORTS_PRE_KEY + reportType);

        // Reports are not threadsafe, plus we have to prevent other nodes from modifying the report
        // Until we've merged in the results of this context analysis
        ILock lock = hazelcast.getLock(REPORTS_MERGE_PRE_KEY + reportType);
        Report report;
        try {
            if (!lock.tryLock(60, TimeUnit.MINUTES)) {
                // Abort report
                flushPending(contextReport.getUUID(), reportType);
                LOG.error("Could not acquire merge lock! Aborting {} for type: {}", contextReport.getUUID(), reportType);
            }
        } catch (InterruptedException e) {
            throw ReportExceptionCodes.UNABLE_TO_RETRIEVE_LOCK.create();
        }
        try {
            report = PortableReport.unwrap(pendingReports.get(contextReport.getUUID()));
            if (report == null) {
                // Somebody cancelled the report, so just discard the result
                lock.unlock();
                lock.destroy();
                lock = null;
                throw ReportExceptionCodes.REPORT_GENERATION_CANCELED.create();
            }
            // Run all applicable cumulators to add the context report results to the global report
            for (ContextReportCumulator cumulator : Services.getContextReportCumulators()) {
                if (cumulator.appliesTo(reportType)) {
                    cumulator.merge(contextReport, report);
                }
            }
            // Mark context as done, thereby decreasing the number of pending tasks
            report.markTaskAsDone();

            // Save it back to hazelcast
            pendingReports.put(contextReport.getUUID(), PortableReport.wrap(report));
        } finally {
            if (lock != null) {
                lock.unlock();
            }
        }

        if (report.getNumberOfPendingTasks() == 0) {
            finishUpReport(reportType, hazelcast, pendingReports, lock, report);

            resetLogLevels();
        }
    }

    @Override
    public void abortContextReport(String uuid, String reportType) {
        // This contextReport failed, so at least decrease the number of pending tasks
        if (useLocal()) {
            delegate.abortContextReport(uuid, reportType);
            return;
        }
        HazelcastInstance hazelcast = Services.getService(HazelcastInstance.class);

        IMap<String, PortableReport> pendingReports = hazelcast.getMap(PENDING_REPORTS_PRE_KEY + reportType);

        ILock lock = hazelcast.getLock(REPORTS_MERGE_PRE_KEY + reportType);
        Report report;
        try {
            if (!lock.tryLock(10, TimeUnit.MINUTES)) {
                // Abort report
                lock = null;// Don't care about locking then
            }
        } catch (InterruptedException e) {
            return;
        }
        try {
            report = PortableReport.unwrap(pendingReports.get(uuid));
            if (report == null) {
                lock.unlock();
                lock.destroy();
                lock = null;
                return;
            }
            // Mark context as done
            report.markTaskAsDone();
            // Save it back to hazelcast
            pendingReports.put(report.getUUID(), PortableReport.wrap(report));
        } finally {
            if (lock != null) {
                lock.unlock();
            }
        }

        if (report.getNumberOfPendingTasks() == 0) {
            finishUpReport(reportType, hazelcast, pendingReports, lock, report);
        }
    }

    @Override
    public void abortGeneration(String uuid, String reportType, String reason) {
        if (useLocal()) {
            delegate.abortGeneration(uuid, reportType, reason);
            return;
        }

        HazelcastInstance hazelcast = Services.getService(HazelcastInstance.class);
        IMap<String, PortableReport> pendingReports = hazelcast.getMap(PENDING_REPORTS_PRE_KEY + reportType);
        Report stoppedReport = PortableReport.unwrap(pendingReports.get(uuid));
        if (stoppedReport == null) { // already removed from pending reports
            return;
        }
        stoppedReport.set("error", reportType, reason);

        Map<String, PortableReport> stoppedPendingReports = hazelcast.getMap(REPORTS_ERROR_KEY + reportType);
        if (stoppedPendingReports == null) {
            stoppedPendingReports = new HashMap<>();
        }
        stoppedPendingReports.put(reportType, PortableReport.wrap(stoppedReport));

        pendingReports.remove(uuid);
        resetLogLevels();

        LOG.info("Report abortion triggered by a cluster member with reason: {}. Trying to stop generation and cancel remote threads.", reason);
    }

    @Override
    public Report getLastErrorReport(String reportType) {
        if (useLocal()) {
            return delegate.getLastErrorReport(reportType);
        }
        Map<String, PortableReport> errorReports = Services.getService(HazelcastInstance.class).getMap(REPORTS_ERROR_KEY + reportType);
        if (errorReports == null) {
            return null;
        }
        return PortableReport.unwrap(errorReports.get(reportType));
    }

    
    @Override
    public String run(ReportConfigs reportConfig) throws OXException {
        // Start a new report run or retrieve the UUID of an already running report
        if (useLocal()) {
            return delegate.run(reportConfig);
        }
        
        LogLevelService logLevelService = Services.getService(LogLevelService.class);
        if (logLevelService != null) {
            logLevelService.set("com.hazelcast.spi.impl.operationservice.impl.Invocation", Level.SEVERE);
            logLevelService.set("com.hazelcast.spi.impl.operationservice.impl.IsStillRunningService.InvokeIsStillRunningOperationRunnable", Level.SEVERE);
        }

        HazelcastInstance hazelcast = Services.getService(HazelcastInstance.class);
        final String uuid = UUIDs.getUnformattedString(UUID.randomUUID());
        List<Integer> allContextIds;
        Report report = null;
        // Firstly retrieve the global lock per this report type to make sure, we are the only one coordinating a report run of this type for now.
        ILock lock = hazelcast.getLock("com.openexchange.report.Reports." + reportConfig.getType());
        lock.lock();
        try {
            // Is a report pending?
            IMap<String, PortableReport> pendingReports = hazelcast.getMap(PENDING_REPORTS_PRE_KEY + reportConfig.getType());

            if ((pendingReports != null) && !pendingReports.isEmpty()) {
                // Yes, there is a report running, so retrieve its UUID and return it.
                return pendingReports.keySet().iterator().next();
            }

            // Load all contextIds
            allContextIds = Services.getService(ContextService.class).getAllContextIds();

            // Set up the report instance
            report = new Report(uuid, System.currentTimeMillis(), reportConfig);
            report.setNumberOfTasks(allContextIds.size());
            // Put it into hazelcast for others to discover
            pendingReports.put(uuid, PortableReport.wrap(report));

        } finally {
            if (lock != null) {
                lock.forceUnlock();
            }
        }

        // Set up an AnalyzeContextBatch instance for every chunk of contextIds
        setUpContextAnalyzer(hazelcast, uuid, reportConfig.getType(), allContextIds, report);
        return uuid;
    }

    //--------------------Private helper methods--------------------
    /**
     * Trigger the analyzing process for all contexts. Each schema is handled separately.
     * 
     * @param hazelcast, the hazelcast instance to use
     * @param uuid, the uuid of the report
     * @param reportType, the report-type
     * @param allContextIds, all relevant context ids, despite the schema
     * @throws OXException
     */
    private void setUpContextAnalyzer(HazelcastInstance hazelcast, String uuid, String reportType, List<Integer> allContextIds, Report report) throws OXException {
        // Set up an AnalyzeContextBatch instance for every chunk of contextIds
        IExecutorService executorService = hazelcast.getExecutorService(REPORT_TYPE_DEFAULT);
        DatabaseService databaseService = Services.getService(DatabaseService.class);

        ArrayList<Integer> contextsToProcess = new ArrayList<>(allContextIds);

        LOG.debug("{} contexts in total will get processed!", contextsToProcess.size());
        while (!contextsToProcess.isEmpty()) {
            Integer firstRemainingContext = contextsToProcess.get(0);
            Integer[] contextsInSameSchema = ArrayUtils.toObject(databaseService.getContextsInSameSchema(firstRemainingContext.intValue()));

            Member member = getRandomMember(hazelcast);
            LOG.debug("{} will get assigned to new thread on hazelcast member {}", contextsInSameSchema.length, member.getSocketAddress().toString());

            if (report != null) {
                executorService.submitToMember(new AnalyzeContextBatch(uuid, report, Arrays.asList(contextsInSameSchema)), member);
            } else
                executorService.submitToMember(new AnalyzeContextBatch(uuid, reportType, Arrays.asList(contextsInSameSchema)), member);

            for (int i = 0; i < contextsInSameSchema.length; i++) {
                contextsToProcess.remove(Integer.valueOf(contextsInSameSchema[i]));
            }
            LOG.debug("{} contexts still to assign.", contextsToProcess.size());
        }
    }

    /**
     * Determine whether to use the {@link LocalReportService} or this (Hazelcast) Service.
     * 
     * @return, true if no hazelcast instance is available
     */
    private boolean useLocal() {
        if (delegate == null) {
            return false;
        }
        HazelcastInstance hazelcast = Services.getService(HazelcastInstance.class);
        if ((hazelcast == null) || (hazelcast.getCluster().getMembers().size() <= 1) || Services.getService(ConfigurationService.class).getBoolProperty("com.openexchange.report.runLocal", false)) {
            return true;
        }
        return false;
    }

    private Member getRandomMember(HazelcastInstance hazelcast) {
        Set<Member> members = hazelcast.getCluster().getMembers();
        int i = 0;
        int max = new Random().nextInt(members.size());
        Iterator<Member> iterator = members.iterator();
        Member member = iterator.next();
        while (iterator.hasNext() && (i < max)) {
            member = iterator.next();
            i++;
        }
        return member;
    }

    private void finishUpReport(String reportType, HazelcastInstance hazelcast, IMap<String, PortableReport> pendingReports, ILock lock, Report report) {
        // Looks like this was the last context result we were waiting for
        // So finish up the report
        // First run the global system handlers
        for (ReportSystemHandler handler : Services.getSystemHandlers()) {
            if (handler.appliesTo(reportType)) {
                handler.runSystemReport(report);
            }
        }

        // And perform the finishing touches
        for (ReportFinishingTouches handler : Services.getFinishingTouches()) {
            if (handler.appliesTo(reportType)) {
                handler.finish(report);
            }
        }

        // We are done. Dump Report
        report.setStopTime(System.currentTimeMillis());
        report.getNamespace("configs").put("com.openexchange.report.appsuite.ReportService", this.getClass().getSimpleName());
        hazelcast.getMap(REPORTS_KEY).put(report.getType(), PortableReport.wrap(report));

        // Clean up resources
        pendingReports.remove(report.getUUID());
        lock.destroy();
    }

    private void resetLogLevels() {
        LogLevelService logLevelService = Services.getService(LogLevelService.class);
        if (logLevelService != null) {
            logLevelService.reset("com.hazelcast.spi.impl.operationservice.impl.Invocation");
            logLevelService.reset("com.hazelcast.spi.impl.operationservice.impl.IsStillRunningService.InvokeIsStillRunningOperationRunnable");
        }
    }
}
