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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.groupware.calendar;

/**
 * {@link RecurringResults} - Collection for recurring results
 * 
 * @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
 */
public final class RecurringResults {

    private static final int DEFAULT_SIZE = 4;

    private RecurringResult recurringResults[];

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

    /**
     * Adds specified recurring result to this recurring results collection
     * 
     * @param rr The recurring result to add
     */
    public void add(final RecurringResult rr) {
        // CalendarCommonCollection.debugRecurringResult(rr); // uncomment this
        // in runtime edition
        if (counter == recurringResults.length) {
            final RecurringResult new_recurring_result[] = new RecurringResult[recurringResults.length * 2];
            System.arraycopy(recurringResults, 0, new_recurring_result, 0, counter);
            recurringResults = new_recurring_result;
        }
        recurringResults[counter++] = rr;
    }

    /**
     * Gets the corresponding result by specified one-based recurrence position
     * 
     * @param recurrencePosition The one-based recurrence position
     * @return The corresponding result by specified one-based recurrence
     *         position or <code>null</code>
     */
    public RecurringResult getRecurringResultByPosition(final int recurrencePosition) {
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

    /**
     * Gets the corresponding result by specified zero-based internal position
     * 
     * @param position The zero-based internal position
     * @return The corresponding result by specified zero-based internal
     *         position or <code>null</code>
     */
    public RecurringResult getRecurringResult(final int position) {
        if (position <= counter && position >= 0) {
            return recurringResults[position];
        }
        return null;
    }

    /**
     * Gets this recurring results collection's size
     * 
     * @return The recurring results collection's size
     */
    public int size() {
        return counter;
    }

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
    public int getPositionByLong(final long normalizedTime) {
        for (int a = 0; a < counter; a++) {
            if (recurringResults[a].getNormalized() == normalizedTime) {
                return recurringResults[a].getPosition();
            }
        }
        return -1;
    }

}
