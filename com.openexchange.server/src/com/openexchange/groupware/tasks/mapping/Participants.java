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
