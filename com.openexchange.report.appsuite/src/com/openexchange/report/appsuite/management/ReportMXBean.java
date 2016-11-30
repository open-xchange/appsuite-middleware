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
