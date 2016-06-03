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

package com.openexchange.report.appsuite;

import java.util.Date;
import com.openexchange.exception.OXException;
import com.openexchange.report.appsuite.serialization.Report;

/**
 * The {@link ReportService} runs reports and manages pending and finished reports. This service is available via OSGi
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 */
public interface ReportService {

    /**
     * Same as calling {@link #run(String)} with the 'default' reportType
     */
    String run() throws OXException;

    /**
     * Run a report of the given reportType. Note that when a report of this type is already running, no new report is triggered and the
     * uuid of the running report is returned instead
     * 
     * @return The uuid of triggered or the already running report
     */
    String run(String reportType) throws OXException;

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

    /**
     * Run a report of the given reportType. Consider the given dates as timerange.
     * Note that when a report of this type is already running, no new report is
     * triggered and the uuid of the running report is returned instead.
     * 
     * @param reportType
     * @param startDate, start of the timerange
     * @param endDate, end of the timerange
     *            @return, uuid of the report
     * @throws Exception
     */
    String run(String reportType, Date startDate, Date endDate) throws OXException;

    /**
     * Run a report of the given report-type. The given parameters will be used to set the created
     * reports parameters. Note that when a report of this type is already running, no new report is
     * triggered and the uuid of the running report is returned instead.
     * 
     * @param reportType, the report-type to be run
     * @param startDate, start of the timerange
     * @param endDate, end of the timerange
     * @param isCustomTimerange, is the timerange relevant
     * @param isShowSingleTenant, is only one tenant relevant
     * @param singleTenantId, the tenants id
     * @param isIgnoreAdmin, should admins be ignored in the calculation
     * @param isShowDriveMetrics, are drive metrics relevant
     * @param isShowMailMetrics, are mail metrics relevant
     *            @return, uuid of the report
     * @throws Exception
     */
    String run(String reportType, Date startDate, Date endDate, Boolean isCustomTimerange, Boolean isShowSingleTenant, Long singleTenantId, Boolean isIgnoreAdmin, Boolean isShowDriveMetrics, Boolean isShowMailMetrics) throws OXException;
}
