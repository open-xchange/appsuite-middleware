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
import java.util.Date;
import java.util.TimeZone;
import com.openexchange.ajax.config.actions.GetRequest;
import com.openexchange.ajax.config.actions.GetResponse;
import com.openexchange.ajax.config.actions.SetRequest;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
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

    private AJAXClient client2;
    private CalendarTestManager ctm1;
    private CalendarTestManager ctm2;
    private Appointment appointment;
    private String origtz1;
    private String origtz2;

    public Bug41995Test(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        client2 = new AJAXClient(User.User2);
        ctm1 = new CalendarTestManager(client);
        ctm1.setFailOnError(true);
        ctm2 = new CalendarTestManager(client2);
        ctm2.setFailOnError(true);

        GetRequest getRequest = new GetRequest(Tree.TimeZone);
        GetResponse getResponse = client.execute(getRequest);
        origtz1 = getResponse.getString();
        getRequest = new GetRequest(Tree.TimeZone);
        getResponse = client.execute(getRequest);
        origtz2 = getResponse.getString();

        SetRequest setRequest = new SetRequest(Tree.TimeZone, "America/New_York");
        client.execute(setRequest);
        setRequest = new SetRequest(Tree.TimeZone, "Europe/Berlin");
        client2.execute(setRequest);

        appointment = new Appointment();
        appointment.setTitle("Bug 41995 Test");
        appointment.setStartDate(D("01.11.2016 07:00"));
        appointment.setEndDate(D("01.11.2016 08:00"));
        appointment.setRecurrenceType(Appointment.WEEKLY);
        appointment.setInterval(1);
        appointment.setDays(Appointment.TUESDAY);
        appointment.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
        appointment.setIgnoreConflicts(true);
        appointment.setParticipants(new Participant[] { new UserParticipant(client.getValues().getUserId()), new UserParticipant(client2.getValues().getUserId()) });
        
        ctm1.insert(appointment);
    }

    public void testBug41995() throws Exception {
        Appointment update = new Appointment();
        update.setObjectID(appointment.getObjectID());
        update.setLastModified(new Date(Long.MAX_VALUE));
        update.setParentFolderID(client2.getValues().getPrivateAppointmentFolder());
        update.setAlarm(15);
        ctm2.update(update);
        
        Appointment loaded = ctm1.get(client.getValues().getPrivateAppointmentFolder(), appointment.getObjectID());
        assertEquals("Wrong start date.", appointment.getStartDate(), loaded.getStartDate());
        assertEquals("Wrong end date.", appointment.getEndDate(), loaded.getEndDate());
    }

    @Override
    public void tearDown() throws Exception {
        SetRequest setRequest = new SetRequest(Tree.TimeZone, origtz1);
        client.execute(setRequest);
        setRequest = new SetRequest(Tree.TimeZone, origtz2);
        client2.execute(setRequest);
        ctm1.cleanUp();
        ctm2.cleanUp();
        super.tearDown();
    }

}
