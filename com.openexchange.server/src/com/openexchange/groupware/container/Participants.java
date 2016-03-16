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
