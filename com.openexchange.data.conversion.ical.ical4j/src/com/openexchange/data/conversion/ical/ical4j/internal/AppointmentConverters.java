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
package com.openexchange.data.conversion.ical.ical4j.internal;

import net.fortuna.ical4j.model.component.VEvent;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.*;
import com.openexchange.data.conversion.ical.ical4j.internal.appointment.RequireStartDate;
import com.openexchange.data.conversion.ical.ical4j.internal.appointment.RequireEndDate;
import com.openexchange.data.conversion.ical.ical4j.internal.appointment.Location;
import com.openexchange.data.conversion.ical.ical4j.internal.appointment.Transparency;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class AppointmentConverters {
    public static final AttributeConverter<VEvent, AppointmentObject>[] ALL;

    /**
     * Prevent instantiation.
     */
    private AppointmentConverters() {
        super();
    }

    static {
        final List<AttributeConverter<VEvent, AppointmentObject>> tmp = new ArrayList<AttributeConverter<VEvent, AppointmentObject>>();
        tmp.add(new Title<VEvent, AppointmentObject>());
        tmp.add(new Note<VEvent, AppointmentObject>());

        Start<VEvent, AppointmentObject> start = new Start<VEvent, AppointmentObject>();
        start.setVerifier(new RequireStartDate());
        tmp.add(start);

        tmp.add(new End<VEvent, AppointmentObject>());

        Duration<VEvent, AppointmentObject> duration = new Duration<VEvent, AppointmentObject>();
        duration.setVerifier(new RequireEndDate());
        tmp.add(duration);

        tmp.add(new Klass<VEvent, AppointmentObject>());

        tmp.add(new Location());
        tmp.add(new Transparency());

        tmp.add(new Participants<VEvent, AppointmentObject>());

        tmp.add(new Categories<VEvent, AppointmentObject>());

        tmp.add(new Recurrence<VEvent, AppointmentObject>());

        tmp.add(new DeleteExceptions<VEvent, AppointmentObject>());

        tmp.add(new Alarm<VEvent, AppointmentObject>());
        tmp.add(new Uid<VEvent, AppointmentObject>());
        ALL = (AttributeConverter<VEvent, AppointmentObject>[]) tmp.toArray(new AttributeConverter[tmp.size()]);
    }
}
