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
 *     Copyright (C) 2004-2016 Open-Xchange, Inc.
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;
import com.openexchange.report.appsuite.ContextReport;
import com.openexchange.report.appsuite.ContextReportCumulator;
import com.openexchange.report.appsuite.ReportExceptionCodes;
import com.openexchange.report.appsuite.ReportFinishingTouches;
import com.openexchange.report.appsuite.ReportSystemHandler;
import com.openexchange.report.appsuite.jobs.AnalyzeContextBatch;
import com.openexchange.report.appsuite.serialization.PortableReport;
import com.openexchange.report.appsuite.serialization.Report;

/**
 * {@link LocalReportService}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.2
 */
public class LocalReportService extends AbstractReportService {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(LocalReportService.class);

    /**
     * Cache has concurrency level 20 as 20 threads will be able to update the cache concurrently.
     * 
     * 'Implementations of this interface are expected to be thread-safe, and can be safely accessed by multiple concurrent threads.' from http://docs.guava-libraries.googlecode.com/git/javadoc/com/google/common/cache/Cache.html
     */
    private static final Cache<String, Map<String, PortableReport>> cache = CacheBuilder.newBuilder().concurrencyLevel(20).expireAfterWrite(180, TimeUnit.MINUTES).<String, Map<String, PortableReport>> build();

    /**
     * {@inheritDoc}
     */
    @Override
    public String run(String reportType) throws OXException {
        Map<String, PortableReport> pendingReports = cache.asMap().get(PENDING_REPORTS_PRE_KEY + reportType);
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
        List<Integer> allContextIds = Services.getService(ContextService.class).getAllContextIds();

        // Set up the report instance
        Report report = new Report(uuid, reportType, System.currentTimeMillis());
        report.setNumberOfTasks(allContextIds.size());
        pendingReports.put(uuid, PortableReport.wrap(report));
        cache.asMap().put(PENDING_REPORTS_PRE_KEY + reportType, pendingReports);

        // Set up an AnalyzeContextBatch instance for every chunk of contextIds
        DatabaseService databaseService = Services.getService(DatabaseService.class);
        ExecutorService executorService = Executors.newFixedThreadPool(20);

        List<Integer> contextsToProcess = Collections.synchronizedList(new ArrayList<>(allContextIds));

        LOG.info("{} contexts in total will get processed!", contextsToProcess.size());
        while (!contextsToProcess.isEmpty()) {
            Integer firstRemainingContext = contextsToProcess.get(0);
            Integer[] contextsInSameSchema = ArrayUtils.toObject(databaseService.getContextsInSameSchema(firstRemainingContext.intValue()));

            LOG.info("For {} context will be spawned a new thread.", contextsInSameSchema.length);

            executorService.submit(new AnalyzeContextBatch(uuid, reportType, Arrays.asList(contextsInSameSchema)));

            for (int i = 0; i < contextsInSameSchema.length; i++) {
                contextsToProcess.remove(Integer.valueOf(contextsInSameSchema[i]));
            }
            LOG.info("{} contexts still to assign.", contextsToProcess.size());
        }
        return uuid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Report getLastReport(String reportType) {
        Map<String, PortableReport> finishedReports = cache.asMap().get(REPORTS_KEY);
        if (finishedReports == null) {
            return null;
        }
        PortableReport portableReport = finishedReports.get(reportType);
        return PortableReport.unwrap(portableReport);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Report[] getPendingReports(String reportType) {
        Map<String, PortableReport> pendingReports = cache.asMap().get(PENDING_REPORTS_PRE_KEY + reportType);
        if (pendingReports == null) {
            return null;
        }
        Collection<PortableReport> reportCol = pendingReports.values();
        return PortableReport.unwrap(reportCol);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flushPending(String uuid, String reportType) {
        Map<String, PortableReport> pendingReports = cache.asMap().get(PENDING_REPORTS_PRE_KEY + reportType);
        pendingReports.remove(uuid);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void done(ContextReport contextReport) throws OXException {
        String reportType = contextReport.getType();
        Map<String, PortableReport> pendingReports = cache.asMap().get(PENDING_REPORTS_PRE_KEY + reportType);

        if ((pendingReports == null) || (pendingReports.isEmpty())) {
            //stopped (and removed) in the meanwhile
            return;
        }

        Report report = PortableReport.unwrap(pendingReports.get(contextReport.getUUID()));
        if (report == null) {
            // Somebody cancelled the report, so just discard the result
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

        pendingReports.put(contextReport.getUUID(), PortableReport.wrap(report));

        if (report.getNumberOfPendingTasks() == 0) {
            finishUpReport(reportType, pendingReports, report);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void abort(String uuid, String reportType) {
        Map<String, PortableReport> pendingReports = cache.asMap().get(PENDING_REPORTS_PRE_KEY + reportType);
        if (pendingReports.isEmpty()) {
            //stopped (and removed) in the meanwhile
            return;
        }

        Report report = PortableReport.unwrap(pendingReports.get(uuid));
        if (report == null) {
            return;
        }
        // Mark context as done
        report.markTaskAsDone();
        pendingReports.put(report.getUUID(), PortableReport.wrap(report));

        if (report.getNumberOfPendingTasks() == 0) {
            finishUpReport(reportType, pendingReports, report);
        }
    }

    private void finishUpReport(String reportType, Map<String, PortableReport> pendingReports, Report report) {
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

        Map<String, PortableReport> finishedReports = cache.asMap().get(REPORTS_KEY);
        if (finishedReports == null) {
            finishedReports = new HashMap<>();
        }
        finishedReports.put(report.getType(), PortableReport.wrap(report));
        cache.asMap().put(REPORTS_KEY, finishedReports);

        // Clean up resources
        pendingReports.remove(report.getUUID());
        cache.asMap().get(PENDING_REPORTS_PRE_KEY + reportType).remove(report.getUUID());
    }
}
