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

import com.openexchange.exception.OXException;
import com.openexchange.report.appsuite.ReportService;
import com.openexchange.report.appsuite.internal.LocalReportService;
import com.openexchange.report.appsuite.internal.Services;
import com.openexchange.report.appsuite.serialization.Report;
import com.openexchange.report.appsuite.serialization.ReportConfigs;

/**
 * The {@link ReportMXBeanImpl} implements the MXBean interface by delegating all calls to
 * the {@link LocalReportService} singleton and wrapping reports as {@link JMXReport} instances
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 */
public class ReportMXBeanImpl implements ReportMXBean {

    @Override
    public JMXReport retrieveLastReport(String reportType) throws Exception {
        Report lastReport = Services.getService(ReportService.class).getLastReport(reportType);
        return null == lastReport ? null : new JMXReport(lastReport);
    }

    @Override
    public JMXReport retrieveLastReport() throws Exception {
        Report lastReport = Services.getService(ReportService.class).getLastReport();
        return null == lastReport ? null : new JMXReport(lastReport);
    }

    @Override
    public JMXReport[] retrievePendingReports(String reportType) throws Exception {
        return JMXReport.wrap(Services.getService(ReportService.class).getPendingReports(reportType));
    }

    @Override
    public JMXReport[] retrievePendingReports() throws Exception {
        return JMXReport.wrap(Services.getService(ReportService.class).getPendingReports());
    }

    @Override
    public void flushPending(String uuid, String reportType) {
        Services.getService(ReportService.class).flushPending(uuid, reportType);
    }

    @Override
    public void flushPending(String uuid) {
        Services.getService(ReportService.class).flushPending(uuid);
    }

    @Override
    public JMXReport retrieveLastErrorReport(String reportType) throws Exception {
        Report lastReport = Services.getService(ReportService.class).getLastErrorReport(reportType);
        return null == lastReport ? null : new JMXReport(lastReport);
    }

    @Override
    public String run(String reportType, Boolean isSingleDeployment, Boolean isConfigureTimerange, Long timeframeStart, Long timeframeEnd) throws Exception {
        try {
            ReportConfigs reportConfig = new ReportConfigs.ReportConfigsBuilder(reportType)
                .isSingleDeployment(isSingleDeployment.booleanValue())
                .isConfigTimerange(isConfigureTimerange.booleanValue())
                .consideredTimeframeStart(timeframeStart.longValue())
                .consideredTimeframeEnd(timeframeEnd.longValue())
                .build();
            return Services.getService(ReportService.class).run(reportConfig);
        } catch (OXException e) {
            throw new Exception(e.getMessage());
        }
    }
}
