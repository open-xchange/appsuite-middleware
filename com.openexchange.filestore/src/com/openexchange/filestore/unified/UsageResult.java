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

import java.util.Map;
import com.google.common.collect.ImmutableMap;

/**
 * {@link UsageResult} - A Unified Quota usage result.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class UsageResult {

    /**
     * Creates a new builder.
     *
     * @return The new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** A builder for a usage result */
    public static class Builder {

        private final ImmutableMap.Builder<String, Long> usagePerService;
        private long total;

        /**
         * Initializes a new {@link UsageResult.Builder}.
         */
        Builder() {
            super();
            usagePerService = ImmutableMap.builder();
        }

        /**
         * Adds specified service's usage.
         *
         * @param usage The usage to add
         * @param serviceId The identifier of the service contributor
         * @return This builder
         * @throws IllegalArgumentException If given usage is less than <code>0</code> (zero) or given service identifier is <code>null</code>
         */
        public Builder addUsageForService(long usage, String serviceId) {
            if (usage < 0) {
                throw new IllegalArgumentException("usage must not be less than zero");
            }
            if (null == serviceId) {
                throw new IllegalArgumentException("serviceId must not be null");
            }
            usagePerService.put(serviceId, Long.valueOf(usage));
            total += usage;
            return this;
        }

        /**
         * Builds the result from this builder's attributes.
         *
         * @return The usage result
         */
        public UsageResult build() {
            return new UsageResult(usagePerService.build(), total);
        }
    }

    // -----------------------------------------------------------------------------------

    private final ImmutableMap<String, Long> usagePerService;
    private final long total;

    /**
     * Initializes a new {@link UsageResult}.
     */
    UsageResult(ImmutableMap<String, Long> usagePerService, long total) {
        super();
        this.usagePerService = usagePerService;
        this.total = total;
    }

    /**
     * Gets the total usage for all considered services.
     *
     * @return The total usage
     */
    public long getTotal() {
        return total;
    }

    /**
     * Gets the (immutable) mapping for usage per service
     *
     * @return The mapping
     */
    public Map<String, Long> getUsagePerService() {
        return usagePerService;
    }

    /**
     * Checks if this usage result contains a usage entry for specified service.
     *
     * @param serviceId The service identifier
     * @return <code>true</code> if such an entry is contained; otherwise <code>false</code>
     */
    public boolean hasUsageFor(String serviceId) {
        return null == serviceId ? false : usagePerService.containsKey(serviceId);
    }

}
