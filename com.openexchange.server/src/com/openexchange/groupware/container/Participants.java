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

package com.openexchange.groupware.container;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Participants
 *
 * @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
 */
public class Participants {

    private List<Participant> participants = new ArrayList<Participant>();

    private Set<UserParticipant> h_users = new HashSet<UserParticipant>();

    public Participants() {
        super();
    }

    public Participants(final UserParticipant[] users) {
        if (h_users == null) {
            h_users = new HashSet<UserParticipant>();
        }

        if (users != null) {
            for (int a = 0; a < users.length; a++) {
                h_users.add(users[a]);
            }
        }
    }

    public Participants(final UserParticipant[] users, final Participant[] participants) {
        if (h_users == null) {
            h_users = new HashSet<UserParticipant>();
        }

        for (int a = 0; a < users.length; a++) {
            h_users.add(users[a]);
        }

        this.participants = Arrays.asList(participants);
    }

    public Participant[] getList() {
        if (participants != null) {
            return participants.toArray(new Participant[participants.size()]);
        }
        return null;
    }

    public UserParticipant[] getUsers() {
        if (h_users != null) {
            return h_users.toArray(new UserParticipant[h_users.size()]);
        }
        return null;
    }

    public void add(final Participant p) {
        if (participants == null) {
            participants = new ArrayList<Participant>();
        }
        if (!participants.contains(p)) {
            participants.add(p);
        }
    }

    public void add(final UserParticipant p) {
        if (h_users == null) {
            h_users = new HashSet<UserParticipant>();
        }
        h_users.add(p);

        if (participants == null) {
            participants = new ArrayList<Participant>();
        }
        if (!participants.contains(p)) {
            participants.add(p);
        }
    }

    /**
     * This method adds the given UserParticipant or the corresponding UserParticipant of the given array if present.
     * (This is needed to override the Confirmation Information of a User)
     * @param p
     * @param users
     */
    public void add(UserParticipant p, UserParticipant[] users) {
        if (users != null) {
            for (UserParticipant user : users) {
                if (user.getIdentifier() == p.getIdentifier()) {
                    add(user);
                    return;
                }
            }
        }
        add(p);
    }

    public boolean containsUserParticipant(final UserParticipant up) {
        if (participants != null) {
            final int participantsSize = participants.size();
            final Iterator<Participant> i = participants.iterator();
            for (int k = 0; k < participantsSize; k++) {
                final Participant p = i.next();
                if (p instanceof UserParticipant) {
                    final UserParticipant cup = (UserParticipant) p;
                    if (cup.getIdentifier() == up.getIdentifier()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
