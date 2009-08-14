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

package com.openexchange.groupware.notify;

import java.util.List;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.tools.CommonAppointments;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.notify.ParticipantNotifyTest.Message;
import com.openexchange.session.Session;
import com.openexchange.setuptools.TestConfig;
import com.openexchange.setuptools.TestContextToolkit;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class Bug14309Test extends ParticipantNotifyTest {

    private Context ctx;

    private String user;

    private String secondUser;

    private int secondUserId;

    private String secondUserMail;

    private CommonAppointments appointments;

    private CalendarDataObject appointment;

    private Session so;

    public void setUp() throws Exception {
        super.setUp();
        
        final TestContextToolkit contextTools = new TestContextToolkit();
        ctx = contextTools.getDefaultContext();
        final TestConfig config = new TestConfig();
        user = config.getUser();
        secondUser = config.getSecondUser();
        secondUserId = contextTools.resolveUser(secondUser, ctx);
        secondUserMail = contextTools.loadUser(secondUserId, ctx).getMail();

        so = contextTools.getSessionForUser(user, ctx);

        appointments = new CommonAppointments(ctx, user);
        appointment = appointments.buildAppointmentWithUserParticipants(user, secondUser);

        notify.realUsers = true;
    }

    public void testBug14309() throws Exception {
        notify.appointmentCreated(appointment, so);
        List<Message> messages = notify.getMessages();
        assertEquals("Wrong amount of notification messages.", 1, messages.size());
        Message message = messages.get(0);
        assertTrue("Wrong recipient.", message.addresses.contains(secondUserMail));
        assertTrue("Message should contain a link to the apointment.", message.message.contains("http://")); // TODO: Make more
                                                                                                             // sophisticated.
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }
}
