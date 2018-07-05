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

package com.openexchange.groupware.tasks.mapping;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.tasks.Task;

/**
 * {@link ParticipantsTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class ParticipantsTest {

    @InjectMocks
    private Participants participants;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testEquals_noParticipants_returnTrue() {
        boolean equals = participants.equals(new Task(), new Task());

        assertTrue(equals);
    }

    @Test
    public void testEquals_sameParticipants_returnTrue() {
        Task task1 = new Task();
        task1.addParticipant(new UserParticipant(1));

        Task task2 = new Task();
        task2.addParticipant(new UserParticipant(1));

        boolean equals = participants.equals(task1, task2);

        assertTrue(equals);
    }

    @Test
    public void testEquals_differentParticipants_returnFalse() {
        Task task1 = new Task();
        task1.addParticipant(new UserParticipant(1));

        Task task2 = new Task();
        task2.addParticipant(new UserParticipant(2));

        boolean equals = participants.equals(task1, task2);

        assertFalse(equals);
    }

    @Test
    public void testEquals_task1ContainsParticipantsTask2Not_returnFalse() {
        Task task1 = new Task();
        task1.addParticipant(new UserParticipant(1));

        Task task2 = new Task();

        boolean equals = participants.equals(task1, task2);

        assertFalse(equals);
    }

    @Test
    public void testEquals_task2ContainsParticipantsTask1Not_returnFalse() {
        Task task1 = new Task();

        Task task2 = new Task();
        task2.addParticipant(new UserParticipant(1));

        boolean equals = participants.equals(task1, task2);

        assertFalse(equals);
    }

    @Test
    public void testEquals_task1HasMoreParticipants_returnFalse() {
        Task task1 = new Task();
        task1.addParticipant(new UserParticipant(1));
        task1.addParticipant(new UserParticipant(2));
        task1.addParticipant(new UserParticipant(3));

        Task task2 = new Task();
        task2.addParticipant(new UserParticipant(1));

        boolean equals = participants.equals(task1, task2);

        assertFalse(equals);
    }

    @Test
    public void testEquals_sameParticipantButStatusChanged_returnFalse() {
        Task task1 = new Task();
        UserParticipant participant3 = new UserParticipant(3);
        participant3.setConfirm(1);
        task1.addParticipant(participant3);

        Task task2 = new Task();
        UserParticipant participant3_2 = new UserParticipant(3);
        participant3_2.setConfirm(2);
        task2.addParticipant(participant3_2);

        boolean equals = participants.equals(task1, task2);

        assertFalse(equals);
    }

    @Test
    public void testEquals_sameParticipantAndSameStatus_returnTrue() {
        Task task1 = new Task();
        UserParticipant participant3 = new UserParticipant(3);
        participant3.setConfirm(1);
        task1.addParticipant(participant3);

        Task task2 = new Task();
        UserParticipant participant3_2 = new UserParticipant(3);
        participant3_2.setConfirm(1);
        task2.addParticipant(participant3_2);

        boolean equals = participants.equals(task1, task2);

        assertTrue(equals);
    }

    @Test
    public void testEquals_sameParticipantAndSameStatusButDifferentMessage_returnTrue() {
        Task task1 = new Task();
        UserParticipant participant3 = new UserParticipant(3);
        participant3.setConfirm(1);
        participant3.setConfirmMessage("you");
        task1.addParticipant(participant3);

        Task task2 = new Task();
        UserParticipant participant3_2 = new UserParticipant(3);
        participant3_2.setConfirm(1);
        participant3_2.setConfirmMessage("jawoll");
        task2.addParticipant(participant3_2);

        boolean equals = participants.equals(task1, task2);

        assertTrue(equals);
    }

    @Test
    public void testEquals_sameParticipantsButStatusChanged_returnFalse() {
        Task task1 = new Task();
        UserParticipant participant1 = new UserParticipant(1);
        participant1.setConfirm(1);
        UserParticipant participant2 = new UserParticipant(1);
        participant2.setConfirm(1);
        UserParticipant participant3 = new UserParticipant(1);
        participant3.setConfirm(3);
        task1.addParticipant(participant1);
        task1.addParticipant(participant2);
        task1.addParticipant(participant3);

        Task task2 = new Task();
        UserParticipant participant1_2 = new UserParticipant(1);
        participant1_2.setConfirm(2);
        UserParticipant participant2_2 = new UserParticipant(1);
        participant2_2.setConfirm(2);
        UserParticipant participant3_2 = new UserParticipant(1);
        participant3_2.setConfirm(3);
        task2.addParticipant(participant1_2);
        task2.addParticipant(participant2_2);
        task2.addParticipant(participant3_2);

        boolean equals = participants.equals(task1, task2);

        assertFalse(equals);
    }

    @Test
    public void testEquals_sameParticipantsButOneStatusChanged_returnFalse() {
        Task task1 = new Task();
        UserParticipant participant1 = new UserParticipant(1);
        participant1.setConfirm(1);
        UserParticipant participant2 = new UserParticipant(2);
        participant2.setConfirm(1);
        UserParticipant participant3 = new UserParticipant(3);
        participant3.setConfirm(3);
        task1.addParticipant(participant1);
        task1.addParticipant(participant2);
        task1.addParticipant(participant3);

        Task task2 = new Task();
        UserParticipant participant1_2 = new UserParticipant(1);
        participant1_2.setConfirm(2);
        UserParticipant participant2_2 = new UserParticipant(2);
        participant2_2.setConfirm(1);
        UserParticipant participant3_2 = new UserParticipant(3);
        participant3_2.setConfirm(3);
        task2.addParticipant(participant1_2);
        task2.addParticipant(participant2_2);
        task2.addParticipant(participant3_2);

        boolean equals = participants.equals(task1, task2);

        assertFalse(equals);
    }

    @Test
    public void testEquals_sameParticipantsButOnlyMessageChanged_returnTrue() {
        Task task1 = new Task();
        UserParticipant participant1 = new UserParticipant(1);
        participant1.setConfirm(1);
        participant1.setConfirmMessage("sure");
        UserParticipant participant2 = new UserParticipant(2);
        participant2.setConfirm(1);
        participant2.setConfirmMessage("me too");
        UserParticipant participant3 = new UserParticipant(3);
        participant3.setConfirm(3);
        task1.addParticipant(participant1);
        task1.addParticipant(participant2);
        task1.addParticipant(participant3);

        Task task2 = new Task();
        UserParticipant participant1_2 = new UserParticipant(1);
        participant1_2.setConfirm(1);
        participant1_2.setConfirmMessage("YEEEAAH");
        UserParticipant participant2_2 = new UserParticipant(2);
        participant2_2.setConfirm(1);
        UserParticipant participant3_2 = new UserParticipant(3);
        participant3_2.setConfirm(3);
        participant3_2.setConfirmMessage("I'm out");
        task2.addParticipant(participant1_2);
        task2.addParticipant(participant2_2);
        task2.addParticipant(participant3_2);

        boolean equals = participants.equals(task1, task2);

        assertTrue(equals);
    }
}
