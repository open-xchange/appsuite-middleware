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

package com.openexchange.groupware.calendar;

import com.openexchange.chronos.service.RecurrenceService;

/**
 * 
 * {@link RecurringResultsInterface}
 *
 * @deprecated Use {@link RecurrenceService}
 */
@Deprecated
public interface RecurringResultsInterface {

    /**
     * Adds specified recurring result to this recurring results collection
     *
     * @param rr The recurring result to add
     */
    public abstract void add(final RecurringResultInterface rr);

    /**
     * Gets the corresponding result by specified one-based recurrence position
     *
     * @param recurrencePosition The one-based recurrence position
     * @return The corresponding result by specified one-based recurrence
     *         position or <code>null</code>
     */
    public abstract RecurringResultInterface getRecurringResultByPosition(final int recurrencePosition);

    /**
     * Gets the corresponding result by specified zero-based internal position
     *
     * @param position The zero-based internal position
     * @return The corresponding result by specified zero-based internal
     *         position or <code>null</code>
     */
    public abstract RecurringResultInterface getRecurringResult(final int position);

    /**
     * Gets this recurring results collection's size
     *
     * @return The recurring results collection's size
     */
    public abstract int size();

    /**
     * Gets the one-based internal position in recurring results by specified
     * normalized time milliseconds
     *
     * @param normalizedTime The normalized time milliseconds whose position
     *            shall be determined
     * @return The time's zero-based internal position in recurring results or
     *         <code>-1</code> if time milliseconds a re not covered by this
     *         recurring results
     */
    public abstract int getPositionByLong(final long normalizedTime);

}
