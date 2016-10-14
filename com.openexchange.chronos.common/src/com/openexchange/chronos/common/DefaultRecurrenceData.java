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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.chronos.common;

import static com.openexchange.chronos.common.CalendarUtils.isFloating;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.service.RecurrenceData;

/**
 * {@link DefaultRecurrenceData}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class DefaultRecurrenceData implements RecurrenceData {

    private final String rrule;
    private final boolean allDay;
    private final String timeZoneID;
    private final long seriesStart;

    /**
     * Initializes a new {@link DefaultRecurrenceData}.
     *
     * @param rrule The underlying recurrence rule
     * @param allDay <code>true</code> if the recurrence is <i>all-day</i>, <code>false</code>, otherwise
     * @param timeZoneID The timezone identifier, or <code>null</code> for <i>all-day</i> or <i>floating</i> event series
     * @param seriesStart The series start date, usually the date of the first occurrence, as milliseconds since epoch
     */
    public DefaultRecurrenceData(String rrule, boolean allDay, String timeZoneID, long seriesStart) {
        super();
        this.rrule = rrule;
        this.allDay = allDay;
        this.timeZoneID = timeZoneID;
        this.seriesStart = seriesStart;
    }

    /**
     * Initializes a new {@link DefaultRecurrenceData} based on the series master event.
     *
     * @param seriesMaster The series master event
     */
    public DefaultRecurrenceData(Event seriesMaster) {
        this(seriesMaster.getRecurrenceRule(), seriesMaster.isAllDay(), isFloating(seriesMaster) ? null : seriesMaster.getStartTimeZone(), seriesMaster.getStartDate().getTime());
    }

    @Override
    public String getRecurrenceRule() {
        return rrule;
    }

    @Override
    public boolean isAllDay() {
        return allDay;
    }

    @Override
    public String getTimeZoneID() {
        return timeZoneID;
    }

    @Override
    public long getSeriesStart() {
        return seriesStart;
    }

}
