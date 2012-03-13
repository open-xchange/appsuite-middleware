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

import java.util.LinkedList;
import java.util.List;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.tasks.Task;

/**
 * Ensures that every added/removed/remained participant and the task owner are informed about the changed task.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug9950Test extends ParticipantNotifyTest {

    public Bug9950Test() {
        super();
    }

    public void testShouldNotifyOldAndNewParticipants() throws Exception{
        NotificationPool.getInstance().clear();
        final Participant[] oldParticipants = getParticipants(U(2,4,10),G(),S(), R());
        final Task oldTask = getTask(oldParticipants);

        final Participant[] newParticipants = getParticipants(U(4, 8, 10), G(), S(), R());
        final Task newTask = getTask(newParticipants);

        notify.taskModified(oldTask, newTask, session);

        final List<Message> messages = notify.getMessages();

        final List<String> mailAddresses = new LinkedList<String>();
        for(final Message message : messages) { mailAddresses.addAll(message.addresses); }

        final List<PooledNotification> pooledNotifications = NotificationPool.getInstance().getNotifications();
        for (final PooledNotification pooledNotification : pooledNotifications) {
            mailAddresses.add(pooledNotification.getParticipant().email);
        }

        assertNames( mailAddresses, "user1@test.invalid", "user3@test.invalid", "user7@test.invalid", "user9@test.invalid", "primary@test" );
    }
}
