/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
    public void setUp() {
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
