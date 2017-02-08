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

import static com.openexchange.groupware.calendar.TimeTools.D;
import static org.junit.Assert.assertEquals;
import java.util.Date;
import java.util.TimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.config.actions.GetRequest;
import com.openexchange.ajax.config.actions.GetResponse;
import com.openexchange.ajax.config.actions.SetRequest;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.test.CalendarTestManager;

/**
 * {@link Bug41995Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.1
 */
public class Bug41995Test extends AbstractAJAXSession {

    private CalendarTestManager ctm2;
    private Appointment appointment;
    private String origtz1;
    private String origtz2;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        catm.setFailOnError(true);
        ctm2 = new CalendarTestManager(getClient2());
        ctm2.setFailOnError(true);

        GetRequest getRequest = new GetRequest(Tree.TimeZone);
        GetResponse getResponse = getClient().execute(getRequest);
        origtz1 = getResponse.getString();
        getRequest = new GetRequest(Tree.TimeZone);
        getResponse = getClient().execute(getRequest);
        origtz2 = getResponse.getString();

        SetRequest setRequest = new SetRequest(Tree.TimeZone, "America/New_York");
        getClient().execute(setRequest);
        catm.setTimezone(TimeZone.getTimeZone("America/New_York"));
        setRequest = new SetRequest(Tree.TimeZone, "Europe/Berlin");
        getClient2().execute(setRequest);
        ctm2.setTimezone(TimeZone.getTimeZone("Europe/Berlin"));

        appointment = new Appointment();
        appointment.setTitle("Bug 41995 Test");
        appointment.setStartDate(D("01.11.2016 07:00"));
        appointment.setEndDate(D("01.11.2016 08:00"));
        appointment.setRecurrenceType(Appointment.WEEKLY);
        appointment.setInterval(1);
        appointment.setDays(Appointment.TUESDAY);
        appointment.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        appointment.setIgnoreConflicts(true);
        appointment.setParticipants(new Participant[] { new UserParticipant(getClient().getValues().getUserId()), new UserParticipant(getClient2().getValues().getUserId()) });

        catm.insert(appointment);
    }

    @Test
    public void testBug41995() throws Exception {
        Appointment update = new Appointment();
        update.setObjectID(appointment.getObjectID());
        update.setLastModified(new Date(Long.MAX_VALUE));
        update.setParentFolderID(getClient2().getValues().getPrivateAppointmentFolder());
        update.setAlarm(15);
        ctm2.update(update);

        Appointment loaded = catm.get(getClient().getValues().getPrivateAppointmentFolder(), appointment.getObjectID());
        assertEquals("Wrong start date.", appointment.getStartDate(), loaded.getStartDate());
        assertEquals("Wrong end date.", appointment.getEndDate(), loaded.getEndDate());
    }

    @After
    public void tearDown() throws Exception {
        try {
            SetRequest setRequest = new SetRequest(Tree.TimeZone, origtz1);
            getClient().execute(setRequest);
            setRequest = new SetRequest(Tree.TimeZone, origtz2);
            getClient2().execute(setRequest);
            ctm2.cleanUp();
        } finally {
            super.tearDown();
        }
    }

}
