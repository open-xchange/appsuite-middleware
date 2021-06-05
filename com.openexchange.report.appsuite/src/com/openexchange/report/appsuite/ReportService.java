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

package com.openexchange.report.appsuite;

import com.openexchange.exception.OXException;
import com.openexchange.report.appsuite.serialization.Report;
import com.openexchange.report.appsuite.serialization.ReportConfigs;

/**
 * The {@link ReportService} runs reports and manages pending and finished reports. This service is available via OSGi
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 */
public interface ReportService {

    /**
     * Same as calling {@link #getLastReport(String)} with the 'default' reportType
     */
    Report getLastReport();

    /**
     * Get the last finished report of the given reportType or null if during the uptime of this cluster, no report has been produced.
     */
    Report getLastReport(String reportType);

    /**
     * Get a list of currently running reports. You can check the progress of these reports by examining the startTime, pendingTasks and numberOfTasks of the running reports
     */
    Report[] getPendingReports(String reportType);

    /**
     * Same as calling {@link #getPendingReports(String)} with the 'default' reportType
     */
    Report[] getPendingReports();

    /**
     * Remove the markers for the pending report with the given uuid, to make way to start a new report. Useful to cancel crashed reports.
     */
    void flushPending(String uuid, String reportType);

    /**
     * Same as calling {@link #flushPending(String, String)} with the 'default' reportType
     */
    void flushPending(String uuid);

    /**
     * Called if the report generation for related context (provided within {@link ContextReport}) has been finished.
     * 
     * @param contextReport Context information encapsulated within the {@link ContextReport}
     * @throws OXException
     */
    void finishContext(ContextReport contextReport) throws OXException;

    /**
     * Called if the report for related context cannot be created
     * 
     * @param uuid The generated UUID of the complete Report
     * @param reportType The type of report that should be generated
     */
    void abortContextReport(String uuid, String reportType);

    /**
     * Aborts report generation based on the provided reason
     * 
     * @param uuid The generated UUID of the complete Report
     * @param reportType The type of report that should be generated
     * @param reason Reason why the report becomes canceled
     */
    void abortGeneration(String uuid, String reportType, String reason);

    /**
     * Returns a report that contains information about failed report generation. The report will be existent after com.openexchange.report.appsuite.ReportService.abortGeneration(String, String, String) has been called.
     * 
     * @param reportType The type of report that the error report should be returned for.
     * @return {@link Report} that contains the status while abortion and the detailed error description.
     */
    Report getLastErrorReport(String reportType);

    String run(ReportConfigs reportConfig) throws OXException;
}
