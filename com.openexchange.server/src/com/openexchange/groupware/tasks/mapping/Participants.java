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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.Change;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.ParticipantsDiffer;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.tasks.Mapper;
import com.openexchange.groupware.tasks.Task;

/**
 * 
 * {@link Participants} Mapper to identify and map if participants of a Tasks are equal
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class Participants implements Mapper<Participant[]> {

    /**
     * {@inheritDoc}
     * 
     * Returns <code>false</code> if at least one participant status differs. All other participant information (like confirmation message) will not be considered
     */
    @Override
    public boolean equals(final Task task1, final Task task2) {
        Participant[] participants = task1.getParticipants();
        Participant[] participants2 = task2.getParticipants();
        if ((participants == null) && (participants2 == null)) {
            return true;
        }
        if (((participants != null) && (participants2 == null)) || ((participants == null) && (participants2 != null))) {
            return false;
        }
        if (participants.length != participants2.length) {
            return false;
        }
        boolean equal = true;
        for (int i = 0; i < participants.length; i++) {
            Participant participant = participants[i];

            if (participant instanceof UserParticipant) {
                UserParticipant userParticipant = (UserParticipant) participant;
                boolean foundParticipantInBothTasks = false;
                UserParticipant userParticipant2 = null;

                for (int j = 0; j < participants2.length; j++) {
                    Participant participant2 = participants2[j];

                    if (participant2 instanceof UserParticipant) {
                        userParticipant2 = (UserParticipant) participant2;
                        if (userParticipant.getIdentifier() == userParticipant2.getIdentifier()) {
                            foundParticipantInBothTasks = true;
                            break;
                        }
                    }
                }
                if (!foundParticipantInBothTasks) {
                    equal = false;
                    break;
                }
                Change change = new ParticipantsDiffer().getConfirmChange(userParticipant, userParticipant2);
                if (change != null) {
                    equal = false;
                    break;
                }
            }
        }
        return equal;
    }

    @Override
    public void fromDB(final ResultSet result, final int pos, final Task task) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Participant[] get(final Task task) {
        return task.getParticipants();
    }

    @Override
    public String getDBColumnName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDisplayName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getId() {
        return CalendarObject.PARTICIPANTS;
    }

    @Override
    public boolean isSet(final Task task) {
        return task.containsParticipants();
    }

    @Override
    public void set(final Task task, final Participant[] value) {
        task.setParticipants(value);
    }

    @Override
    public void toDB(final PreparedStatement stmt, final int pos, final Task task) {
        throw new UnsupportedOperationException();
    }
}
