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
import java.util.UUID;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link DataExportStatusChecker} - Provides method to acquire a task's current status.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
@SingletonService
public interface DataExportStatusChecker {

    /**
     * Gets the optional status for the data export task associated with specified identifier.
     *
     * @param taskId The identifier for the data export task
     * @return The optional data export status
     * @throws OXException If data export status cannot be returned
     */
    Optional<DataExportStatus> getDataExportStatus(UUID taskId) throws OXException;

    /**
     * Gets the optional status for the data export task associated with specified user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The optional data export status
     * @throws OXException If data export status cannot be returned
     */
    Optional<DataExportStatus> getDataExportStatus(int userId, int contextId) throws OXException;

    /**
     * Touches specified task.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws OXException If task's last-accessed time stamp could not be updated
     */
    void touch(int userId, int contextId) throws OXException;
}
