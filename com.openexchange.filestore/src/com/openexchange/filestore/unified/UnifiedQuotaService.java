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

package com.openexchange.filestore.unified;

import com.openexchange.exception.OXException;
import com.openexchange.filestore.QuotaMode;

/**
 * {@link UnifiedQuotaService} - Provides methods to query and increment/decrement a service-associated usage as well as to query its limit that contribute to a unified quota.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public interface UnifiedQuotaService extends QuotaMode {

    /** The mode for Unified Quota */
    public static final String MODE = "unified";

    /**
     * Gets the current limit for specified user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The current limit for specified user
     * @throws OXException If current limit cannot be returned
     */
    long getLimit(int userId, int contextId) throws OXException;

    /**
     * Gets the current usage for specified user including all contributing services.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The current usage for specified user
     * @throws OXException If current usage cannot be returned
     */
    UsageResult getUsage(int userId, int contextId) throws OXException;

    /**
     * Gets the current usage for specified user, except for specified services.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param toExclude The identifier of such service contributors to exclude
     * @return The current usage for specified user
     * @throws OXException If current usage cannot be returned
     */
    UsageResult getUsageExceptFor(int userId, int contextId, String... toExclude) throws OXException;

    /**
     * Sets the usage for specified service contributor to the given value for specified user
     *
     * @param usage The usage bytes to set
     * @param serviceId The identifier of the service contributor
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if new usage has been successfully applied; otherwise <code>false</code> if there was a concurrent modification
     * @throws OXException If usage cannot be updated
     */
    boolean setUsage(long usage, String serviceId, int userId, int contextId) throws OXException;

    /**
     * Increments the usage by the given value for specified user
     *
     * @param required The required bytes to increment by
     * @param serviceId The identifier of the service contributor
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if usage has been successfully incremented; otherwise <code>false</code> if there was a concurrent modification
     * @throws OXException If usage cannot be updated
     */
    boolean incrementUsage(long required, String serviceId, int userId, int contextId) throws OXException;

    /**
     * Decrements the usage by the given value for specified user
     *
     * @param released The released bytes to decrement by
     * @param serviceId The identifier of the service contributor
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if usage has been successfully decremented; otherwise <code>false</code> if there was a concurrent modification
     * @throws OXException If usage cannot be updated
     */
    boolean decrementUsage(long released, String serviceId, int userId, int contextId) throws OXException;

    /**
     * Checks if this usage service is applicable for given user
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if applicable; otherwise <code>false</code>
     * @throws OXException If applicability cannot be checked
     */
    boolean isApplicableFor(int userId, int contextId) throws OXException;

    // ---------------------------------------------------------------------------------------------------------

    /**
     * Deletes the usage entries for specified context.
     *
     * @param contextId The context identifier
     * @throws OXException If delete attempt fails
     */
    void deleteEntryFor(int contextId) throws OXException;

    /**
     * Deletes the usage entries for specified user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws OXException If delete attempt fails
     */
    void deleteEntryFor(int userId, int contextId) throws OXException;

}
