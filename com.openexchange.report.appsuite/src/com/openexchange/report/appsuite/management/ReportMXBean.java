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

package com.openexchange.report.appsuite.management;

import com.openexchange.report.appsuite.ReportService;

/**
 * The {@link ReportMXBean} defines the JMX operations for running reports according to the MXBean conventions. These
 * methods are the equivalents of the methods in the {@link ReportService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 */
public interface ReportMXBean {

    /**
     * Same as calling {@link #getLastReport(String)} with the 'default' reportType
     */
    public abstract JMXReport retrieveLastReport(String reportType) throws Exception;

    /**
     * Get the last finished report of the given reportType or null if during the uptime of this cluster, no report has been produced.
     */
    public abstract JMXReport retrieveLastReport() throws Exception;

    /**
     * Get a list of currently running reports. You can check the progress of these reports by examining the startTime, pendingTasks and numberOfTasks of the running reports
     */
    public abstract JMXReport[] retrievePendingReports(String reportType) throws Exception;

    /**
     * Same as calling {@link #getPendingReports(String)} with the 'default' reportType
     */
    public abstract JMXReport[] retrievePendingReports() throws Exception;

    /**
     * Remove the markers for the pending report with the given uuid, to make way to start a new report. Useful to cancel crashed reports.
     */
    public abstract void flushPending(String uuid, String reportType) throws Exception;

    /**
     * Same as calling {@link #flushPending(String, String)} with the 'default' reportType
     */
    public abstract void flushPending(String uuid) throws Exception;

    public abstract JMXReport retrieveLastErrorReport(String reportType) throws Exception;

    public abstract String run(String reportType, Boolean isSingleDeployment, Boolean isConfigureTimerange, Long timeframeStart, Long timeframeEnd) throws Exception;
}
