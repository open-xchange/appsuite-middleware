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

package com.openexchange.data.conversion.ical.ical4j.internal;

import java.util.ArrayList;
import java.util.List;
import net.fortuna.ical4j.model.component.VEvent;
import com.openexchange.data.conversion.ical.ical4j.internal.appointment.ChangeExceptions;
import com.openexchange.data.conversion.ical.ical4j.internal.appointment.DeleteExceptions;
import com.openexchange.data.conversion.ical.ical4j.internal.appointment.IgnoreConflicts;
import com.openexchange.data.conversion.ical.ical4j.internal.appointment.Location;
import com.openexchange.data.conversion.ical.ical4j.internal.appointment.RequireEndDate;
import com.openexchange.data.conversion.ical.ical4j.internal.appointment.RequireStartDate;
import com.openexchange.data.conversion.ical.ical4j.internal.appointment.Transparency;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.Alarm;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.Attach;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.Categories;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.CreatedAndDTStamp;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.CreatedBy;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.Duration;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.End;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.Klass;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.LastModified;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.Note;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.Participants;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.Recurrence;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.Sequence;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.Start;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.Title;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.Uid;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.XMicrosoftCdoAlldayEvent;
import com.openexchange.data.conversion.ical.itip.ITipMethod;
import com.openexchange.groupware.container.Appointment;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public final class AppointmentConverters {

    public static final List<AttributeConverter<VEvent, Appointment>> ALL;

    public static final List<AttributeConverter<VEvent, Appointment>> ALL_ITIP;

    private static AttributeConverter<VEvent, Appointment> title = new Title<VEvent, Appointment>();

    private static AttributeConverter<VEvent, Appointment> note = new Note<VEvent, Appointment>();

    private static AbstractVerifyingAttributeConverter<VEvent, Appointment> verifyingStart = new Start<VEvent, Appointment>();

    private static AttributeConverter<VEvent, Appointment> end = new End<VEvent, Appointment>();

    private static AttributeConverter<VEvent, Appointment> xMicrosoftCdoAlldayEvent = new XMicrosoftCdoAlldayEvent<VEvent, Appointment>();

    private static AbstractVerifyingAttributeConverter<VEvent, Appointment> verifyingDuration = new Duration<VEvent, Appointment>();

    private static AttributeConverter<VEvent, Appointment> klass = new Klass<VEvent, Appointment>();

    private static AttributeConverter<VEvent, Appointment> location = new Location();

    private static AttributeConverter<VEvent, Appointment> transparency = new Transparency();

    private static AttributeConverter<VEvent, Appointment> categories = new Categories<VEvent, Appointment>();

    private static AttributeConverter<VEvent, Appointment> recurrence = new Recurrence<VEvent, Appointment>();

    private static AttributeConverter<VEvent, Appointment> deleteExcetions = new DeleteExceptions();

    private static AttributeConverter<VEvent, Appointment> changeExceptions = new ChangeExceptions();

    private static AttributeConverter<VEvent, Appointment> alarm = new Alarm<VEvent, Appointment>();

    private static AttributeConverter<VEvent, Appointment> ignoreConflicts = new IgnoreConflicts();

    private static AttributeConverter<VEvent, Appointment> uid = new Uid<VEvent, Appointment>();

    private static AttributeConverter<VEvent, Appointment> createdAndDTStamp = new CreatedAndDTStamp<VEvent, Appointment>();

    private static AttributeConverter<VEvent, Appointment> lastModified = new LastModified<VEvent, Appointment>();

    private static AttributeConverter<VEvent, Appointment> createdBy = new CreatedBy<VEvent, Appointment>();

    private static AttributeConverter<VEvent, Appointment> sequence = new Sequence<VEvent, Appointment>();

    private static AbstractVerifyingAttributeConverter<VEvent, Appointment> participants = new Participants<VEvent, Appointment>();

    private static AbstractVerifyingAttributeConverter<VEvent, Appointment> attach = new Attach<VEvent, Appointment>();

    /**
     * Prevent instantiation.
     */
    private AppointmentConverters() {
        super();
    }

    static {
        verifyingStart.setVerifier(new RequireStartDate());
        verifyingDuration.setVerifier(new RequireEndDate());

        ALL = getAll();
        ALL_ITIP = getAllItip();
    }

    private static List<AttributeConverter<VEvent, Appointment>> getAll() {
        List<AttributeConverter<VEvent, Appointment>> tmp = new ArrayList<AttributeConverter<VEvent, Appointment>>();
        tmp.add(title);
        tmp.add(note);
        tmp.add(verifyingStart);
        tmp.add(end);
        tmp.add(verifyingDuration);
        tmp.add(klass);
        tmp.add(location);
        tmp.add(transparency);
        tmp.add(categories);
        tmp.add(recurrence);
        tmp.add(deleteExcetions);
        tmp.add(changeExceptions);
        tmp.add(alarm);
        tmp.add(ignoreConflicts);
        tmp.add(uid);
        tmp.add(createdAndDTStamp);
        tmp.add(lastModified);
        tmp.add(createdBy);
        tmp.add(sequence);
        tmp.add(participants);
        tmp.add(xMicrosoftCdoAlldayEvent);
        tmp.add(attach);
        return tmp;
    }

    private static List<AttributeConverter<VEvent, Appointment>> getAllItip() {
        List<AttributeConverter<VEvent, Appointment>> tmp = new ArrayList<AttributeConverter<VEvent, Appointment>>();
        tmp.add(title);
        tmp.add(note);
        tmp.add(verifyingStart);
        tmp.add(end);
        tmp.add(verifyingDuration);
        tmp.add(klass);
        tmp.add(location);
        tmp.add(categories);
        tmp.add(recurrence);
        tmp.add(deleteExcetions);
        tmp.add(changeExceptions);
        tmp.add(alarm);
        tmp.add(ignoreConflicts);
        tmp.add(uid);
        tmp.add(createdAndDTStamp);
        tmp.add(lastModified);
        tmp.add(createdBy);
        tmp.add(sequence);
        tmp.add(participants);
        tmp.add(xMicrosoftCdoAlldayEvent);
        tmp.add(attach);

        return tmp;
    }

    public static List<AttributeConverter<VEvent, Appointment>> getConverters(ITipMethod method) {
        return method == null ? ALL : ALL_ITIP;
    }

}
