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

package com.openexchange.push.impl.balancing.reschedulerpolicy;

/**
 * {@link ReschedulePlan} - A rescheduling plan.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.2
 */
public class ReschedulePlan {

    private static final ReschedulePlan INSTANCE_W_REMOTE = new ReschedulePlan(true);
    private static final ReschedulePlan INSTANCE_WO_REMOTE = new ReschedulePlan(false);

    /**
     * Gets the instance w/ or w/o remote plan
     *
     * @param remotePlan <code>true</code> for remote rescheduling; otherwise <code>false</code>
     * @return The instance w/ or w/o remote plan
     */
    public static ReschedulePlan getInstance(boolean remotePlan) {
        return remotePlan ? INSTANCE_W_REMOTE : INSTANCE_WO_REMOTE;
    }

    // -----------------------------------------------------------------------------------------------------------------------

    private final boolean remotePlan;
    private final int hash;

    /**
     * Initializes a new {@link ReschedulePlan}.
     */
    private ReschedulePlan(boolean remotePlan) {
        super();
        this.remotePlan = remotePlan;
        this.hash = 31; // Always the same hash code
    }

    /**
     * Signals if this plan also reschedules on remote nodes.
     *
     * @return <code>true</code> for remote rescheduling; otherwise <code>false</code>
     */
    public boolean isRemotePlan() {
        return remotePlan;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ReschedulePlan)) {
            return false;
        }

        // Always equal to other reschedule plans
        return true;
    }

}
