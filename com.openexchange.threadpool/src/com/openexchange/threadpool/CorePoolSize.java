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

package com.openexchange.threadpool;


/**
 * {@link CorePoolSize} - The core pool size to apply.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class CorePoolSize {

    /**
     * Specifies the behavior of the core pool size
     * <p>
     * whether minimum accepted core pool size is "Number of CPUs + 1" or simply accept any given value.
     */
    public static enum Behavior {
        /**
         * Minimum accepted core pool size is "Number of CPUs + 1"
         */
        ADJUST_IF_NEEDED,
        /**
         * Accept any given value
         */
        AS_IS
    }

    // --------------------------------------------------------------------------------------------------------------------------------

    private final int corePoolSize;
    private final Behavior behavior;

    /**
     * Initializes a new {@link CorePoolSize}.
     *
     * @param corePoolSize The number of threads to keep in the pool, even if they are idle
     * @param behavior The behavior whether given core pool size is accepted as-is or if adjusted according to "Number of CPUs + 1"
     */
    public CorePoolSize(int corePoolSize, Behavior behavior) {
        super();
        this.corePoolSize = corePoolSize;
        this.behavior = behavior;
    }

    /**
     * Gets the number of threads to keep in the pool, even if they are idle
     *
     * @return The core pool size
     */
    public int getCorePoolSize() {
        return corePoolSize;
    }

    /**
     * Gets the behavior
     * <p>
     * Whether core pool size is accepted as-is or if adjusted according to "Number of CPUs + 1"
     *
     * @return The behavior
     */
    public Behavior getBehavior() {
        return behavior;
    }

}
