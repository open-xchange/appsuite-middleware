/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.report.appsuite.jobs;

import static com.openexchange.java.Autoboxing.I;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextExceptionCodes;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.report.appsuite.ContextReport;
import com.openexchange.report.appsuite.ReportContextHandler;
import com.openexchange.report.appsuite.ReportExceptionCodes;
import com.openexchange.report.appsuite.ReportService;
import com.openexchange.report.appsuite.ReportUserHandler;
import com.openexchange.report.appsuite.UserReport;
import com.openexchange.report.appsuite.UserReportCumulator;
import com.openexchange.report.appsuite.internal.ReportProperties;
import com.openexchange.report.appsuite.internal.Services;
import com.openexchange.report.appsuite.serialization.Report;
import com.openexchange.user.User;

/**
 * The {@link AnalyzeContextBatch} class is the workhorse of the reporting system. It runs the reports on a batch of
 * context ids.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 */
public class AnalyzeContextBatch implements Callable<Integer>, Serializable {

    private static final long serialVersionUID = -578253218760102061L;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AnalyzeContextBatch.class);

    private final String uuid;
    private String reportType;
    private List<Integer> contextIds;
    private final Report report;

    /**
     *
     * Initializes a new {@link AnalyzeContextBatch} with a report instead of just a report-type.
     * This means, additional options are selected and stored in the report object.
     *
     * @param uuid The uuid of the report we're running
     * @param reportType The type of report that is being run
     * @param chunk a list of context IDs to analyze
     */
    public AnalyzeContextBatch(String uuid, Report report, List<Integer> chunk) {
        super();
        this.uuid = uuid;
        this.report = report;
        this.reportType = report.getType();
        this.contextIds = chunk;
    }

    @Override
    public Integer call() throws Exception {
        LOG.debug("Starting context processing ot one schema, for report with uuid: {} and context amount: {}", this.uuid, I(this.contextIds.size()));
        Thread currentThread = Thread.currentThread();
        int previousPriority = currentThread.getPriority();
        currentThread.setPriority(ReportProperties.getThreadPriority());

        try {
            if (reportType == null) {
                reportType = "default";
            }

            for (int i = contextIds.size(); (!currentThread.isInterrupted()) && (i-- > 0);) {
                Integer ctxId = contextIds.get(i);
                ReportService reportService = Services.getService(ReportService.class);
                ContextReport contextReport = null;
                try {
                    Context ctx = loadContext(ctxId.intValue());
                    contextReport = new ContextReport(uuid, reportType, ctx);

                    handleContext(contextReport);
                    handleUsersGuestsLinks(ctx, contextReport);

                    reportService.finishContext(contextReport);
                } catch (OXException oxException) {
                    if (ContextExceptionCodes.UPDATE.equals(oxException)) {
                        reportService.abortGeneration(uuid, reportType, "Not all schemas are up to date! Please ensure schema up-to-dateness (e. g. by calling 'runupdate' CLT).");
                        break;
                    }
                    if (ReportExceptionCodes.REPORT_GENERATION_CANCELED.equals(oxException)) {
                        LOG.info("Stop execution of report generation due to an user instruction!", oxException);
                        contextIds = Collections.emptyList();
                        reportService.abortGeneration(uuid, reportType, "Cancelled report generation based on user interaction.");
                        break;
                    }
                    LOG.error("Exception thrown while loading context. Skip report for context {}. Move to next context", ctxId, oxException);
                    reportService.abortContextReport(uuid, reportType);
                    this.report.addError(oxException);
                    continue;
                } catch (Exception e) {
                    LOG.error("Unexpected error while context report generation!", e);
                    reportService.abortContextReport(uuid, reportType);
                }
            }
        } finally {
            currentThread.setPriority(previousPriority);
        }
        return I(contextIds.size());
    }

    /**
     * Run the report for the given context, to get/set all relevant data.
     *
     * @param contextReport
     * @throws OXException
     */
    private void handleContext(ContextReport contextReport) {
        // Run all Context Analyzers that apply to this reportType
        for (ReportContextHandler contextHandler : Services.getContextHandlers()) {
            if (contextHandler.appliesTo(reportType)) {
                contextHandler.runContextReport(contextReport);
            }
        }
    }

    /**
     * Handles users for the given context
     *
     * @param ctx
     * @param contextReport
     * @throws OXException
     */
    private void handleUsersGuestsLinks(Context ctx, ContextReport contextReport) throws OXException {
        // Next, let's look at all the users in this context
        User[] loadUsers = loadUsers(ctx);

        for (User user : loadUsers) {
            UserReport userReport = new UserReport(uuid, reportType, ctx, user, contextReport);
            // Are extended options available?
            if (this.report != null) {
                userReport.setReportConfig(this.report.getReportConfig());
                //Add user to context
                contextReport.getUserList().add(I(user.getId()));
                contextReport.setReportConfig(this.report.getReportConfig());
            }
            if (runUserAnalyzers(contextReport, user, userReport)) {
                continue;
            }
            // Compact User Analysis and add to context report
            for (UserReportCumulator cumulator : Services.getUserReportCumulators()) {
                if (cumulator.appliesTo(reportType)) {
                    cumulator.merge(userReport, contextReport);
                }
            }
        }

    }

    private boolean runUserAnalyzers(ContextReport contextReport, User user, UserReport userReport) {
        boolean skip = false;
        for (ReportUserHandler userHandler : Services.getUserHandlers()) {
            if (userHandler.appliesTo(reportType)) {
                try {
                    userHandler.runUserReport(userReport);
                } catch (OXException e) {
                    LOG.error("", e);
                    contextReport.getUserList().remove(I(user.getId()));
                    skip = true;
                    if (this.report != null) {
                        this.report.addError(e);
                    }
                }
            }
        }
        return skip;
    }

    protected User[] loadUsers(Context ctx) throws OXException {
        return UserStorage.getInstance().getUser(ctx, true, false);
    }

    protected Context loadContext(int contextId) throws OXException {
        return Services.getService(ContextService.class).getContext(contextId);
    }
}
