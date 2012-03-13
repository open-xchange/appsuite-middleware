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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.groupware.notify;

import java.util.Calendar;
import java.util.Date;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Participant;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.test.Asserts;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class AttachedIcalFileTests extends ParticipantNotifyTest {

    /*
     * Bug 15558
     */
    public void testDTENDShouldBeEndOfFirstOccurence() throws Throwable {
        final AppointmentState state = new AppointmentState(null, null, null);
        final TestMailObject mailObject = new TestMailObject();
        final Participant[] participants = getParticipants(U(), G(2), S(), R());

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 8);
        Date startDate = cal.getTime();

        cal.add(Calendar.HOUR, 1);
        Date endDate = cal.getTime();

        Appointment app = new Appointment();
        app.setParticipants(participants);
        app.setStartDate(startDate);
        app.setEndDate(endDate);
        app.setRecurrenceType(Appointment.DAILY);
        app.setOccurrence(4);
        app.setInterval(1);
        app.setDays(127);


        state.modifyExternal(mailObject, app, session);

        final ContentType ct = mailObject.getTheContentType();

        assertEquals(ct.getCharsetParameter(), "utf-8");
        assertEquals(ct.getPrimaryType(), "text");
        assertEquals(ct.getSubType(), "calendar");

        assertEquals("appointment.ics", mailObject.getTheFilename());

        try {

            final Appointment app2 = convertFromICal(mailObject.getTheInputStream());
            int precision = Calendar.MINUTE;
            Asserts.assertEquals("Start date should match", app.getStartDate(), app2.getStartDate(), precision);
            Asserts.assertEquals("End date should match", app.getEndDate(), app2.getEndDate(), precision);
        } catch (final Exception x) {
            x.printStackTrace();
            fail(x.getMessage());
        }
    }
}
