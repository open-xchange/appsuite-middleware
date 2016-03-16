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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.calendar;

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
        if (DEFAULT_SIZE < 1) {
            recurringResults = new RecurringResult[DEFAULT_SIZE];
        } else {
            recurringResults = new RecurringResult[DEFAULT_SIZE];
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.calendar.RecurringResultsInterface#add(com.openexchange.calendar.RecurringResult)
     */
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

    /* (non-Javadoc)
     * @see com.openexchange.calendar.RecurringResultsInterface#getRecurringResultByPosition(int)
     */
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

    /* (non-Javadoc)
     * @see com.openexchange.calendar.RecurringResultsInterface#getRecurringResult(int)
     */
    @Override
    public RecurringResultInterface getRecurringResult(final int position) {
        if (position <= counter && position >= 0) {
            return recurringResults[position];
        }
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.calendar.RecurringResultsInterface#size()
     */
    @Override
    public int size() {
        return counter;
    }

    /* (non-Javadoc)
     * @see com.openexchange.calendar.RecurringResultsInterface#getPositionByLong(long)
     */
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
