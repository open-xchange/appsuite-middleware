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

package com.openexchange.gdpr.dataexport;

import java.util.Optional;
import com.openexchange.exception.OXException;

/**
 * {@link DataExportJob} - A job representation for processing a certain data export task's work items.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public interface DataExportJob {

    /**
     * Gets the data export task
     *
     * @return The task
     */
    DataExportTask getDataExportTask();

    /**
     * Gets the next work item of given data export task that needs to be processed.
     *
     * @return The optional next data export task work item to process
     * @throws OXException If data export task work item cannot be returned
     */
    Optional<DataExportWorkItem> getNextDataExportWorkItem() throws OXException;

}
