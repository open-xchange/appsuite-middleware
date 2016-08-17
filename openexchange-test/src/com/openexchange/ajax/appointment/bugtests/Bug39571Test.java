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

package com.openexchange.ajax.appointment.bugtests;

import java.util.Calendar;
import java.util.Date;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.resource.ResourceTools;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.test.CalendarTestManager;

/**
 * {@link Bug39571Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.0
 */
public class Bug39571Test extends AbstractAJAXSession {

    private CalendarTestManager ctm;
    private Appointment series;
    private int nextYear;
    private Appointment single;

    public Bug39571Test(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        
        nextYear = Calendar.getInstance().get(Calendar.YEAR) + 1;

        UserParticipant up1 = new UserParticipant(client.getValues().getUserId());
        ResourceParticipant resourceParticipant = new ResourceParticipant(ResourceTools.getSomeResource(client));
        
        ctm = new CalendarTestManager(client);
        series = new Appointment();
        series.setTitle("Bug 39571 Series");
        series.setStartDate(TimeTools.D("01.08." + nextYear + " 08:00"));
        series.setEndDate(TimeTools.D("01.08." + nextYear + " 08:30"));
        series.setRecurrenceType(Appointment.DAILY);
        series.setInterval(1);
        series.setOccurrence(3);
        series.setParticipants(new Participant[] { up1, resourceParticipant });
        series.setIgnoreConflicts(true);
        series.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
        ctm.insert(series);
        
        single = new Appointment();
        single.setTitle("Bug 39571 Single");
        single.setStartDate(TimeTools.D("02.08." + nextYear + " 09:00"));
        single.setEndDate(TimeTools.D("02.08." + nextYear + " 09:30"));
        single.setParticipants(new Participant[] { up1, resourceParticipant });
        single.setIgnoreConflicts(true);
        single.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
        ctm.insert(single);
    }

    public void testBug39571() throws Exception {
        Appointment exception = ctm.createIdentifyingCopy(series);
        exception.setStartDate(TimeTools.D("02.08." + nextYear + " 06:00"));
        exception.setEndDate(TimeTools.D("02.08." + nextYear + " 06:30"));
        exception.setRecurrencePosition(2);
        ctm.update(exception);

        series.setStartDate(TimeTools.D("01.08." + nextYear + " 09:00"));
        series.setEndDate(TimeTools.D("01.08." + nextYear + " 09:30"));
        series.setLastModified(new Date(Long.MAX_VALUE));
        ctm.update(series);
        assertTrue("Excpected conflicting ressource.", ctm.getLastResponse().hasConflicts());
    }

    @Override
    public void tearDown() throws Exception {
        ctm.cleanUp();
        super.tearDown();
    }

}
