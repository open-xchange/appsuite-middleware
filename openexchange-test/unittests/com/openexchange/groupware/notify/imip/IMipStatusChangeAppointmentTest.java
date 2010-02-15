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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.groupware.notify.imip;

import java.util.List;
import com.openexchange.data.conversion.ical.ITipMethod;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.session.Session;

/**
 * {@link IMipStatusChangeAppointmentTest}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class IMipStatusChangeAppointmentTest extends IMipTest {

    private Session so2;

    /*
     * (non-Javadoc)
     * @see com.openexchange.groupware.notify.imip.IMipTest#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        so2 = contextTools.getSessionForUser(secondUser, ctx);
        appointment = appointments.buildAppointmentWithUserParticipants(user, secondUser, thirdUser);
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.groupware.notify.ParticipantNotifyTest#tearDown()
     */
    @Override
    public void tearDown() throws Exception {
        super.tearDown();

    }
    
    public void testStatusChange() throws Exception {
        mailForOwner(CalendarObject.ACCEPT);
        mailForOwner(CalendarObject.DECLINE);
        mailForOwner(CalendarObject.TENTATIVE);
    }
    
    public void mailForOwner(int status) throws Exception {
        
        for (UserParticipant u : appointment.getUsers()) {
            if (u.getIdentifier() == secondUserId) {
                u.setConfirm(status);
                u.setConfirmMessage("Message");
            }
        }
        
        notify.appointmentAccepted(appointment, so2);
        List<Message> messages = notify.getMessages();
        
        boolean foundFirst = false;
        
        for (Message message : messages) {
            if (message.addresses.contains(userMail)) {
                checkState(message.message, ITipMethod.REPLY);
                foundFirst = true;
            } else if (message.addresses.contains(secondUserMail)) {
                fail("Sender should not get a mail");
            } else if (message.addresses.contains(thirdUserMail)) {
                fail("Other participants should not get reply mails.");
            }
        }
        
        assertTrue("missing user", foundFirst);
    }

}
