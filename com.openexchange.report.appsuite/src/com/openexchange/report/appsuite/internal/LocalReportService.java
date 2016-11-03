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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import org.slf4j.Logger;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;
import com.openexchange.report.appsuite.ContextReport;
import com.openexchange.report.appsuite.ContextReportCumulator;
import com.openexchange.report.appsuite.ReportExceptionCodes;
import com.openexchange.report.appsuite.ReportFinishingTouches;
import com.openexchange.report.appsuite.ReportSystemHandler;
import com.openexchange.report.appsuite.jobs.AnalyzeContextBatch;
import com.openexchange.report.appsuite.serialization.Report;
import com.openexchange.report.appsuite.serialization.ReportConfigs;
import com.openexchange.report.appsuite.storage.ChunkingUtilities;
import com.openexchange.report.appsuite.storage.ContextLoader;

/**
 * {@link LocalReportService}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.8.2
 */
public class LocalReportService extends AbstractReportService {

    //--------------------Class Attributes--------------------
    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(LocalReportService.class);

    /**
     * Cache has concurrency level 20 as 20 threads will be able to update the cache concurrently.
     * 
     * 'Implementations of this interface are expected to be thread-safe, and can be safely accessed by multiple concurrent threads.' from http://docs.guava-libraries.googlecode.com/git/javadoc/com/google/common/cache/Cache.html
     */
    private static final Cache<String, Map<String, Report>> reportCache = CacheBuilder.newBuilder().concurrencyLevel(ReportProperties.getMaxThreadPoolSize()).expireAfterWrite(180, TimeUnit.MINUTES).<String, Map<String, Report>> build();

    private static final Cache<String, Map<String, Report>> failedReportCache = CacheBuilder.newBuilder().concurrencyLevel(ReportProperties.getMaxThreadPoolSize()).expireAfterWrite(180, TimeUnit.MINUTES).<String, Map<String, Report>> build();

    private static AtomicReference<ExecutorService> EXECUTOR_SERVICE_REF = new AtomicReference<ExecutorService>();

    private final AtomicInteger threadNumber = new AtomicInteger();

    //--------------------Public override methods--------------------
    /**
     * {@inheritDoc}
     */
    @Override
    public Report getLastReport(String reportType) {
        Map<String, Report> finishedReports = reportCache.asMap().get(REPORTS_KEY);
        if (finishedReports == null) {
            return null;
        }
        return finishedReports.get(reportType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Report[] getPendingReports(String reportType) {
        Map<String, Report> pendingReports = reportCache.asMap().get(PENDING_REPORTS_PRE_KEY + reportType);
        if (pendingReports == null) {
            return null;
        }
        return pendingReports.values().toArray(new Report[pendingReports.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flushPending(String uuid, String reportType) {
        Map<String, Report> pendingReports = reportCache.asMap().get(PENDING_REPORTS_PRE_KEY + reportType);
        pendingReports.remove(uuid);
        List<Runnable> shutdownNow = EXECUTOR_SERVICE_REF.get().shutdownNow();
        LOG.info("Report generation for report type {} with UUID {} canceled. Canceled {} planned threads.", reportType, uuid, shutdownNow.size());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void finishContext(ContextReport contextReport) throws OXException {
        String reportType = contextReport.getType();
        Map<String, Report> pendingReports = reportCache.asMap().get(PENDING_REPORTS_PRE_KEY + reportType);

        if ((pendingReports == null) || (pendingReports.isEmpty())) {
            //stopped (and removed) in the meanwhile
            return;
        }

        Report report = pendingReports.get(contextReport.getUUID());
        if (report == null) {
            // Somebody cancelled the report, so just discard the result
            throw ReportExceptionCodes.REPORT_GENERATION_CANCELED.create();
        }
        // Run all applicable cumulators to add the context report results to the global report
        for (ContextReportCumulator cumulator : Services.getContextReportCumulators()) {
            if (cumulator.appliesTo(reportType)) {
                Collection<Object> reportValues = ((Map<String, Object>) report.getNamespace(Report.MACDETAIL)).values();
                if (reportValues.size() >= ReportProperties.getMaxChunkSize()) {
                    synchronized (report) {
                        cumulator.merge(contextReport, report);
                    }
                } else {
                    cumulator.merge(contextReport, report);
                }

            }
        }
        pendingReports.put(report.getUUID(), report);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void abortContextReport(String uuid, String reportType) {
        Map<String, Report> pendingReports = reportCache.asMap().get(PENDING_REPORTS_PRE_KEY + reportType);
        if ((pendingReports == null) || (pendingReports.isEmpty())) {
            //stopped (and removed) in the meanwhile
            return;
        }

        Report report = pendingReports.get(uuid);
        if (report == null) {
            return;
        }
        pendingReports.put(report.getUUID(), report);
    }

    @Override
    public void abortGeneration(String uuid, String reportType, String reason) {
        Map<String, Report> pendingReports = reportCache.asMap().get(PENDING_REPORTS_PRE_KEY + reportType);
        Report stoppedReport = pendingReports.get(uuid);
        if (stoppedReport == null) { // already removed from pending reports
            return;
        }
        stoppedReport.set("error", reportType, reason);

        Map<String, Report> stoppedPendingReports = failedReportCache.asMap().get(REPORTS_ERROR_KEY + reportType);
        if (stoppedPendingReports == null) {
            stoppedPendingReports = new HashMap<>();
        }
        stoppedPendingReports.put(reportType, stoppedReport);
        failedReportCache.asMap().put(REPORTS_ERROR_KEY + reportType, stoppedPendingReports);

        if (!EXECUTOR_SERVICE_REF.get().isShutdown()) {
            EXECUTOR_SERVICE_REF.get().shutdownNow();
        }

        pendingReports.remove(uuid);
        ChunkingUtilities.removeAllReportParts(uuid);
        LOG.info("Report generation stopped due to an error. Solve the following error and start report again: {}", reason);
    }

    @Override
    public Report getLastErrorReport(String reportType) {
        Map<String, Report> errorReports = failedReportCache.asMap().get(REPORTS_ERROR_KEY + reportType);
        if (errorReports == null) {
            return null;
        }
        return errorReports.get(reportType);
    }

    @Override
    public String run(ReportConfigs reportConfig) throws OXException {
        Map<String, Report> pendingReports = reportCache.asMap().get(PENDING_REPORTS_PRE_KEY + reportConfig.getType());
        if (pendingReports == null) {
            pendingReports = new HashMap<>();
        }

        if (!pendingReports.isEmpty()) {
            // Yes, there is a report running, so retrieve its UUID and return it.
            return pendingReports.keySet().iterator().next();
        }

        // No, we have to set up a  new report
        String uuid = UUIDs.getUnformattedString(UUID.randomUUID());

        // Load all contextIds
        List<Integer> allContextIds = new ArrayList<Integer>();

        // Set up an AnalyzeContextBatch instance for every chunk of contextIds
        allContextIds = Services.getService(ContextService.class).getAllContextIds();
        // Set up the report instance
        Report report = new Report(uuid, System.currentTimeMillis(), reportConfig);
        report.setStorageFolderPath(ReportProperties.getStoragePath());
        report.setNumberOfTasks(allContextIds.size());
        pendingReports.put(uuid, report);
        reportCache.asMap().put(PENDING_REPORTS_PRE_KEY + reportConfig.getType(), pendingReports);

        setUpContextAnalyzer(uuid, allContextIds, report);
        return uuid;
    }

    //--------------------Private helper methods--------------------
    private void setUpContextAnalyzer(String uuid, final List<Integer> allContextIds, final Report report) throws OXException {
        new Thread() {

            public void run() {
                List<List<Integer>> contextsInSameSchema;
                try {
                    contextsInSameSchema = getContextsInSameSchemas(allContextIds);
                    processAllContexts(report, contextsInSameSchema);
                } catch (OXException e) {
                    LOG.error("Unable to strat multithreaded context processing, abort report generation ", e);
                    abortGeneration(report.getUUID(), report.getType(), "Unable to distribute context processing on multiple threads");
                }
            };
        }.start();
    }

    private List<List<Integer>> getContextsInSameSchemas(List<Integer> allContextIds) throws OXException {
        List<List<Integer>> contextsInSchemas = new ArrayList<>();
        ContextLoader dataloaderMySQL = new ContextLoader();
        try {
            while (!allContextIds.isEmpty()) {
                List<Integer> currentSchemaIds;

                currentSchemaIds = dataloaderMySQL.getAllContextIdsInSameSchema(allContextIds.get(0).intValue());
                contextsInSchemas.add(currentSchemaIds);
                allContextIds.removeAll(currentSchemaIds);
            }
        } catch (SQLException e) {
            throw ReportExceptionCodes.UNABLE_TO_RETRIEVE_ALL_CONTEXT_IDS.create(e);
        }
        return contextsInSchemas;
    }

    private void processAllContexts(Report report, List<List<Integer>> contextsInSchemas) throws OXException {
        ExecutorService reportSchemaThreadPool = Executors.newFixedThreadPool(ReportProperties.getMaxThreadPoolSize(), new ThreadFactory() {

            @Override
            public Thread newThread(Runnable r) {
                int threadNum;
                while ((threadNum = threadNumber.incrementAndGet()) <= 0) {
                    if (threadNumber.compareAndSet(threadNum, 1)) {
                        threadNum = 1;
                    } else {
                        threadNum = threadNumber.incrementAndGet();
                    }
                }
                return new Thread(r, getThreadName(threadNum));
            }
        });

        CompletionService<Integer> schemaProcessor = new ExecutorCompletionService<>(reportSchemaThreadPool);

        EXECUTOR_SERVICE_REF.compareAndSet(null, reportSchemaThreadPool);
        if (EXECUTOR_SERVICE_REF.get().isShutdown()) {
            EXECUTOR_SERVICE_REF.set(reportSchemaThreadPool);
        }
        for (List<Integer> singleSchemaContexts : contextsInSchemas) {
            schemaProcessor.submit(new AnalyzeContextBatch(report.getUUID(), report, singleSchemaContexts));
        }
        for (int i = 0; i < contextsInSchemas.size(); i++) {
            try {
                Future<Integer> finishedContexts = schemaProcessor.take();
                report.setTaskState(report.getNumberOfTasks(), report.getNumberOfPendingTasks() - finishedContexts.get());
                if (report.getNumberOfPendingTasks() <= 0) {
                    finishUpReport(report);
                }
            } catch (InterruptedException e) {
                throw ReportExceptionCodes.THREAD_WAS_INTERRUPTED.create(e);
            } catch (ExecutionException e) {
                throw ReportExceptionCodes.UABLE_TO_RETRIEVE_THREAD_RESULT.create(e);
            }
        }
    }

    private static String getThreadName(int threadNumber) {
        return LocalReportService.class.getSimpleName() + "-" + threadNumber;
    }

    private void finishUpReport(Report report) {
        for (ReportSystemHandler handler : Services.getSystemHandlers()) {
            if (handler.appliesTo(report.getType())) {
                handler.runSystemReport(report);
            }
        }

        // And perform the finishing touches
        for (ReportFinishingTouches handler : Services.getFinishingTouches()) {
            if (handler.appliesTo(report.getType())) {
                handler.finish(report);
            }
        }

        // We are done. Dump Report
        report.setStopTime(System.currentTimeMillis());
        report.getNamespace("configs").put("com.openexchange.report.appsuite.ReportService", this.getClass().getSimpleName());

        Map<String, Report> finishedReports = reportCache.asMap().get(REPORTS_KEY);
        if (finishedReports == null) {
            finishedReports = new HashMap<>();
        }
        finishedReports.put(report.getType(), report);
        reportCache.asMap().put(REPORTS_KEY, finishedReports);

        // Clean up resources
        reportCache.asMap().get(PENDING_REPORTS_PRE_KEY + report.getType()).remove(report.getUUID());
    }
}
