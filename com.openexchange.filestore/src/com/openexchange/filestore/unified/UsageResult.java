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
 *    trademarks of the OX Software GmbH. group of companies.
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
         */
        public Builder addUsageForService(long usage, String serviceId) {
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
