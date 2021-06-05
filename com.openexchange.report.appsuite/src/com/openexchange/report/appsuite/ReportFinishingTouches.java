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

/**
 * The {@link ReportFinishingTouches} run after all analyzers and cumulators and can be used to reformat the report.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public interface ReportFinishingTouches {
    /**
     * Declare whether to run as part of this reportType
     */
    boolean appliesTo(String reportType);

    /**
     * Modify the report, as needed
     * @throws OXException 
     */
    void finish(Report report) throws OXException;
}
