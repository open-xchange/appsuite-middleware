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

package com.openexchange.report.appsuite.internal;

import com.openexchange.report.appsuite.ReportService;
import com.openexchange.report.appsuite.serialization.Report;

/**
 * {@link AbstractReportService}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.1
 */
public abstract class AbstractReportService implements ReportService {

    protected static final String REPORT_TYPE_DEFAULT = "default";

    protected static final String REPORTS_MERGE_PRE_KEY = "com.openexchange.report.Reports.Merge.";

    protected static final String PENDING_REPORTS_PRE_KEY = "com.openexchange.report.PendingReports.";

    protected static final String REPORTS_ERROR_KEY = "com.openexchange.report.Reports.Error.";

    protected static final String REPORTS_KEY = "com.openexchange.report.Reports";

    /**
     * {@inheritDoc}
     */
    @Override
    public Report getLastReport() {
        return getLastReport(REPORT_TYPE_DEFAULT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Report[] getPendingReports() {
        return getPendingReports(REPORT_TYPE_DEFAULT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flushPending(String uuid) {
        flushPending(uuid, REPORT_TYPE_DEFAULT);
    }
}
