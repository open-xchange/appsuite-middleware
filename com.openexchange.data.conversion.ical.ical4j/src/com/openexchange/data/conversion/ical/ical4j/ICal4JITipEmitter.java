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

package com.openexchange.data.conversion.ical.ical4j;

import java.util.List;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Comment;
import net.fortuna.ical4j.model.property.Method;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.Mode;
import com.openexchange.data.conversion.ical.SimpleMode;
import com.openexchange.data.conversion.ical.ZoneInfo;
import com.openexchange.data.conversion.ical.itip.ITipContainer;
import com.openexchange.data.conversion.ical.itip.ITipEmitter;
import com.openexchange.data.conversion.ical.itip.ITipMessage;
import com.openexchange.data.conversion.ical.itip.ITipMethod;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.contexts.Context;

/**
 * {@link ICal4JITipEmitter}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class ICal4JITipEmitter extends ICal4JEmitter implements ITipEmitter {

    @Override
    public String writeMessage(ITipMessage message, Context ctx, List<ConversionError> errors, List<ConversionWarning> warnings) {
        Calendar calendar = new Calendar();
        initCalendar(calendar);
        Mode mode = new SimpleMode(ZoneInfo.OUTLOOK);
        boolean consumedComment = false;

        Appointment appt = null;
        VEvent event = null;
        if (message.getAppointment() != null) {
        	appt = message.getAppointment();
        	event = createEvent(mode, 0, message.getAppointment(), ctx, errors, warnings, new ITipContainer());
            if (message.getComment() != null && !message.getComment().trim().equals("")) {
                event.getProperties().add(new Comment(message.getComment()));
                consumedComment = true;
            }
            calendar.getComponents().add(event);
        }
        /*XProperty xprop = new XProperty("X-MICROSOFT-DISALLOW-COUNTER", "TRUE");
        event.getProperties().add(xprop); */ // Test whether we may support these counters


        for (CalendarDataObject exception : message.exceptions()) {
        	if (appt == null) {
        		appt = exception;
        	}
            event = createEvent(mode, 0, exception, ctx, errors, warnings);
            if (message.getComment() != null && !message.getComment().trim().equals("")) {
                event.getProperties().add(new Comment(message.getComment()));
            }
            if (!consumedComment && message.getComment() != null && !message.getComment().trim().equals("")) {
                event.getProperties().add(new Comment(message.getComment()));
                consumedComment = true;
            }

            calendar.getComponents().add(event);
        }
        addVTimeZone(mode.getZoneInfo(), calendar, appt);

        Method method = getICalMethod(message.getMethod());
        replaceMethod(calendar, method);

        return calendar.toString();
    }

    public Method getICalMethod(ITipMethod m) {
        switch (m) {
        case ADD:
            return Method.ADD;
        case CANCEL:
            return Method.CANCEL;
        case COUNTER:
            return Method.COUNTER;
        case DECLINECOUNTER:
            return Method.DECLINE_COUNTER;
        case PUBLISH:
            return Method.PUBLISH;
        case REFRESH:
            return Method.REFRESH;
        case REPLY:
            return Method.REPLY;
        case REQUEST:
            return Method.REQUEST;
        case NO_METHOD:
        default:
            return null;
        }
    }

    @Override
    protected void initCalendar(final Calendar calendar) {
        super.initCalendar(calendar);
        replaceMethod(calendar, Method.REQUEST); // default to REQUEST for iTIP
    }

}
