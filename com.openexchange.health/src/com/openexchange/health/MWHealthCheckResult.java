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

package com.openexchange.health;

import java.util.List;

/**
 * {@link MWHealthCheckResult}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.1
 */
public interface MWHealthCheckResult {

    /**
     * The overall status of health checks
     * @return The overall status
     */
    MWHealthState getStatus();

    /**
     * Gets all responses of executed health checks
     * @return The health check responses
     */
    List<MWHealthCheckResponse> getChecks();

    /**
     * Gets a list of skipped checks
     * @return The skipped checks
     */
    List<String> getSkippedChecks();

    /**
     * Gets a list of ignored checks
     * @return The ignored checks
     */
    List<String> getIgnoredResponses();

}
