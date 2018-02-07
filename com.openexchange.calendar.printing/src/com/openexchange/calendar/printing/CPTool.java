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

package com.openexchange.calendar.printing;

import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.calendar.printing.blocks.WeekAndDayCalculator;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.ServiceLookup;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class CPTool extends WeekAndDayCalculator {

    /**
     * Based on the selected template, this method determines new start and end dates to present exactly the block that the template needs.
     * 
     * @param params The {@link CPParameters}
     */
    public void calculateNewStartAndEnd(final CPParameters params) {
        if (!isBlockTemplate(params)) {
            return;
            // TODO this calls for a strategy pattern later on when there is more than one
        }

        final Calendar cal = getCalendar();
        cal.setTime(params.getStart());
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        params.setStart(cal.getTime());

        cal.setTime(params.getEnd());
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.MILLISECOND, 999);
        params.setEnd(cal.getTime());
    }

    /**
     * Checks whether the template given is one that prints a specific timeframe as a block, which might be different from the given start
     * and end date.
     * 
     * @param params The {@link CPParameters}
     * @return <code>true</code> if the template given is one that prints a specific timeframe as a block, <code>false</code> otherwise
     */
    public boolean isBlockTemplate(final CPParameters params) {
        final String basic = "/[^/]+$";
        final String template = (params.hasUserTemplate()) ? params.getUserTemplate() : params.getTemplate();
        final Matcher m1 = Pattern.compile(CPType.WORKWEEKVIEW.getName() + basic).matcher(template);
        final Matcher m2 = Pattern.compile(CPType.WORKWEEKVIEW.getNumber() + basic).matcher(template);
        return m1.find() || m2.find();
    }

    /**
     * Sort a list of events by start date.
     * 
     * @param events To sort
     */
    public void sort(final List<CPEvent> events) {
        Collections.sort(events, new StartDateComparator());
    }

    /**
     * Check if the specific user declined the event
     * 
     * @param event The {@link Event}
     * @param userId The user to check
     * @return <code>true</code> if the user declined the event, <code>false</code> otherwise
     */
    public static boolean hasDeclined(Event event, int userId) {
        for (Attendee attendee : event.getAttendees()) {
            if (attendee.getEntity() == userId && ParticipationStatus.DECLINED.getValue().equals(attendee.getPartStat().getValue())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Converts {@link Event}s into {@link CPEvent}s
     * 
     * @param services The {@link ServiceLookup}
     * @param events To convert
     * @param cal The {@link CPCalendar}
     * @param context The {@link Context}
     * @return The {@link Event}s as {@link CPEvent}s
     */
    public List<CPEvent> toCPEvent(ServiceLookup services, List<Event> events, CPCalendar cal, Context context) {
        List<CPEvent> retval = new LinkedList<>();
        for (Event event : events) {
            retval.add(new CPEvent(services, event, cal, context));
        }
        return retval;
    }
}
