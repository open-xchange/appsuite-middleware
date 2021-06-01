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

import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.osgi.annotation.SingletonService;


/**
 * {@link MWHealthCheckService}- The health check service
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.1
 */
@SingletonService
public interface MWHealthCheckService {

    static final Logger LOG = LoggerFactory.getLogger(MWHealthCheckService.class);

    /**
     * Gets a collection containing all registered health checks.
     *
     * @return The collection of health checks
     */
    Collection<MWHealthCheck> getAllChecks();

    /**
     * Gets a single health check by its name
     *
     * @param name The name
     * @return The denoted check or <code>null</code>
     */
    MWHealthCheck getCheck(String name);

    /**
     * Executes all registered and not blacklisted health checks.
     *
     * @return The health check result
     */
    MWHealthCheckResult check();

}
