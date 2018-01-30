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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.StringEscapeUtils;
import com.openexchange.calendar.printing.days.CalendarTools;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.exception.OXException;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.user.UserService;

/**
 * The view of an appointment. A dumbed-down version without recurrence pattern, day spanning and so on.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class CPEvent {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CPEvent.class);

    private String title, description, location;

    private Date startDate, endDate;

    private Event original;

    private final CPCalendar cal;

    private final Context context;
    
    private String colorLabel;

    private final ServiceLookup services;

    public CPEvent(ServiceLookup services, final Event mother, final CPCalendar cal, final Context context) {
        super();
        this.cal = cal;
        this.context = context;
        this.services = services;
        setTitle(mother.getSummary());
        setDescription(mother.getDescription());
        setLocation(mother.getLocation());
        setStartDate(new Date(mother.getStartDate().getTimestamp()));
        setEndDate(new Date(mother.getEndDate().getTimestamp()));
        setColorLabel(mother.getColor());
        setOriginal(mother);
    }
    
    private void setColorLabel(String label) {
        this.colorLabel = label;
    }

    public String getColorLabel() {
        return StringEscapeUtils.escapeHtml(colorLabel);
    }

    public String getTitle() {
        final String retval;
        if (null == title) {
            retval = "";
        } else {
            retval = StringEscapeUtils.escapeHtml(title);
        }
        return retval;
    }

    public String getDescription() {
        return StringEscapeUtils.escapeHtml(description);
    }

    public String getLocation() {
        return StringEscapeUtils.escapeHtml(location);
    }

    public Date getStartDate() {
        return startDate;
    }

    public String format(final String pattern, final Date date) {
        return cal.format(pattern, date);
    }

    public long getStartMinutes() {
        return TimeUnit.MILLISECONDS.toMinutes(startDate.getTime() - CalendarTools.getDayStart(cal, startDate).getTime());
    }

    public Date getEndDate() {
        return endDate;
    }

    public long getDurationInMinutes() {
        return TimeUnit.MILLISECONDS.toMinutes(endDate.getTime() - startDate.getTime());
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setLocation(final String location) {
        this.location = location;
    }

    public void setStartDate(final Date start) {
        this.startDate = start;
    }

    public void setEndDate(final Date end) {
        this.endDate = end;
    }

    public List<String> getParticipants() {
        final List<String> retval = new ArrayList<String>();
        for (final Attendee attendee : original.getAttendees()) {
            CalendarUserType userType = attendee.getCuType();
            if (CalendarUserType.INDIVIDUAL.equals(userType)) {
                try {
                    final UserService userService =services.getService(UserService.class);
                    if(null == userService ) {
                        throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(UserService.class.getName());
                    }
                    retval.add(userService.getUser(attendee.getEntity(), context).getDisplayName());
                } catch (final OXException e) {
                    LOG.error("", e);
                }
            } else if (CalendarUserType.GROUP.equals(userType)) {
                try {
                    final GroupService service = services.getService(GroupService.class);
                    if(null == service) {
                        throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(GroupService.class.getName());
                    }
                    retval.add(service.getGroup(context, attendee.getEntity()).getDisplayName());
                } catch (final OXException e) {
                    LOG.error("", e);
                }
            }
            // Don't log resources, rooms or unknown types
        }
        return retval;
    }

    public void setOriginal(final Event original) {
        this.original = original;
    }

    public Event getOriginal() {
        return original;
    }

    @Override
    public String toString() {
        return getStartDate() + " - " + getEndDate() + ": " + getTitle();
    }

}
