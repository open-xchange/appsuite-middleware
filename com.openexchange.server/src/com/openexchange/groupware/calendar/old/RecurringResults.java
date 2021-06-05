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

package com.openexchange.groupware.calendar.old;

import com.openexchange.groupware.calendar.RecurringResultInterface;
import com.openexchange.groupware.calendar.RecurringResultsInterface;

/**
 * {@link RecurringResults} - Collection for recurring results
 *
 * @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
 */
public final class RecurringResults implements RecurringResultsInterface {

    private static final int DEFAULT_SIZE = 4;

    private RecurringResultInterface recurringResults[];

    private int counter;

    /**
     * Initializes a new {@link RecurringResults}
     */
    public RecurringResults() {
        super();
        recurringResults = new RecurringResult[DEFAULT_SIZE];
    }

    @Override
    public void add(final RecurringResultInterface rr) {
        // CalendarCommonCollection.debugRecurringResult(rr); // uncomment this
        // in runtime edition
        if (counter == recurringResults.length) {
            final RecurringResult new_recurring_result[] = new RecurringResult[recurringResults.length << 1];
            System.arraycopy(recurringResults, 0, new_recurring_result, 0, counter);
            recurringResults = new_recurring_result;
        }
        recurringResults[counter++] = rr;
    }

    @Override
    public RecurringResultInterface getRecurringResultByPosition(final int recurrencePosition) {
        final int internalPosition = recurrencePosition - 1;
        if (internalPosition < counter) {
            if (recurringResults[internalPosition].getPosition() == recurrencePosition) {
                return recurringResults[internalPosition];
            }
        }
        for (int a = 0; a < counter; a++) {
            if (recurringResults[a].getPosition() == recurrencePosition) {
                return recurringResults[a];
            }
        }
        return null;
    }

    @Override
    public RecurringResultInterface getRecurringResult(final int position) {
        if (position <= counter && position >= 0) {
            return recurringResults[position];
        }
        return null;
    }

    @Override
    public int size() {
        return counter;
    }

    @Override
    public int getPositionByLong(final long normalizedTime) {
        for (int a = 0; a < counter; a++) {
            if (recurringResults[a].getNormalized() == normalizedTime) {
                return recurringResults[a].getPosition();
            }
        }
        return -1;
    }

}
