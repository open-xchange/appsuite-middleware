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
            } else {
                RecurrenceSet recurrenceSet = new RecurrenceSet();
                recurrenceSet.addInstances(new RecurrenceRuleAdapter(rule));
                if (null != recurrenceDates && 0 < recurrenceDates.length) {
                    recurrenceSet.addInstances(new RecurrenceList(recurrenceDates));
                }
                return recurrenceSet.iterator(seriesStart.getTimeZone(), seriesStart.getTimestamp());
            }
        } catch (IllegalArgumentException e) {
            throw CalendarExceptionCodes.INVALID_RRULE.create(e, rule);
        }
    }

}
