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

package com.openexchange.client.onboarding;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * {@link AvailabilityResult} - The availability result.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class AvailabilityResult {

    private static final AvailabilityResult AVAILABLE = new AvailabilityResult(true, Collections.<String> emptyList());

    /**
     * Gets the result that signals availability.
     *
     * @return The result that signals availability
     */
    public static AvailabilityResult available() {
        return AVAILABLE;
    }

    // --------------------------------------------------------------------------------------------------------------

    private final boolean available;
    private final List<String> missingCapabilities;

    /**
     * Initializes a new {@link AvailabilityResult}.
     *
     * @param available The availability flag
     * @param missingCapabilities The missing capabilities
     */
    public AvailabilityResult(boolean available, List<String> missingCapabilities) {
        super();
        this.available = available;
        this.missingCapabilities = missingCapabilities;
    }

    /**
     * Initializes a new {@link AvailabilityResult}.
     *
     * @param available The availability flag
     * @param missingCapabilities The missing capabilities
     */
    public AvailabilityResult(boolean available, String... missingCapabilities) {
        super();
        this.available = available;
        this.missingCapabilities = null == missingCapabilities || missingCapabilities.length == 0 ? Collections.<String> emptyList() : Arrays.asList(missingCapabilities);
    }

    /**
     * Gets the availability flag
     *
     * @return <code>true</code> if available (permission-/capability-wise); otherwise <code>false</code>
     */
    public boolean isAvailable() {
        return available;
    }

    /**
     * Gets the missing capabilities
     *
     * @return The missing capabilities
     */
    public List<String> getMissingCapabilities() {
        return null == missingCapabilities ? Collections.<String> emptyList() : missingCapabilities;
    }

}
