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

package com.openexchange.chronos.recurrence.service;

import static com.openexchange.chronos.common.CalendarUtils.initRecurrenceRule;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.dmfs.rfc5545.recurrenceset.ForwardingRecurrenceSet;
import org.dmfs.rfc5545.recurrenceset.RecurrenceList;
import org.dmfs.rfc5545.recurrenceset.RecurrenceRuleAdapter;
import org.dmfs.rfc5545.recurrenceset.RecurrenceSet;
import org.dmfs.rfc5545.recurrenceset.RecurrenceSetIterator;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.service.RecurrenceData;
import com.openexchange.exception.OXException;

/**
 * {@link RecurrenceUtils}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class RecurrenceUtils {

    /**
     * Initializes a new recurrence iterator for a specific recurrence rule.
     *
     * @param recurrenceData The recurrence data
     * @return The recurrence rule iterator
     * @throws OXException {@link CalendarExceptionCodes#INVALID_RRULE}
     */
    public static RecurrenceSetIterator getRecurrenceIterator(RecurrenceData recurrenceData) throws OXException {
        return getRecurrenceIterator(recurrenceData, false);
    }

    /**
     * Initializes a new recurrence iterator for a specific recurrence rule, optionally advancing to the first occurrence. The latter
     * option ensures that the first date delivered by the iterator matches the start-date of the first occurrence.
     *
     * @param recurrenceData The recurrence data
     * @param forwardToOccurrence <code>true</code> to fast-forward the iterator to the first occurrence if the recurrence data's start
     *            does not fall into the pattern, <code>false</code> otherwise
     * @return The recurrence rule iterator
     * @throws OXException {@link CalendarExceptionCodes#INVALID_RRULE}
     */
    public static RecurrenceSetIterator getRecurrenceIterator(RecurrenceData recurrenceData, boolean forwardToOccurrence) throws OXException {
        RecurrenceRule rule = initRecurrenceRule(recurrenceData.getRecurrenceRule());
        return getRecurrenceIterator(rule, recurrenceData.getSeriesStart(), recurrenceData.getRecurrenceDates(), forwardToOccurrence);
    }

    /**
     * Initializes a new recurrence iterator for a specific recurrence rule, optionally advancing to the first occurrence. The latter
     * option ensures that the first date delivered by the iterator matches the start-date of the first occurrence.
     *
     * @param rule The recurrence rule
     * @param seriesStart The series start date, usually the date of the first occurrence
     * @param recurrenceDates The list of recurrence dates to include in the recurrence set, or <code>null</code> if there are none
     * @param forwardToOccurrence <code>true</code> to fast-forward the iterator to the first occurrence if the recurrence data's start
     *            does not fall into the pattern, <code>false</code> otherwise
     * @return The recurrence rule iterator
     * @throws OXException {@link CalendarExceptionCodes#INVALID_RRULE}
     */
    private static RecurrenceSetIterator getRecurrenceIterator(RecurrenceRule rule, DateTime seriesStart, long[] recurrenceDates, boolean forwardToOccurrence) throws OXException {
        try {
            if (forwardToOccurrence) {
                ForwardingRecurrenceSet recurrenceSet = new ForwardingRecurrenceSet();
                recurrenceSet.addInstances(new RecurrenceRuleAdapter(rule));
                if (null != recurrenceDates && 0 < recurrenceDates.length) {
                    recurrenceSet.addInstances(new RecurrenceList(recurrenceDates));
                }
                return recurrenceSet.iterator(seriesStart.getTimeZone(), seriesStart.getTimestamp());
            }
            RecurrenceSet recurrenceSet = new RecurrenceSet();
            recurrenceSet.addInstances(new RecurrenceRuleAdapter(rule));
            if (null != recurrenceDates && 0 < recurrenceDates.length) {
                recurrenceSet.addInstances(new RecurrenceList(recurrenceDates));
            }
            return recurrenceSet.iterator(seriesStart.getTimeZone(), seriesStart.getTimestamp());
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw CalendarExceptionCodes.INVALID_RRULE.create(e, rule);
        }
    }

}
