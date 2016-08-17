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

package com.openexchange.caldav;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.container.participants.ConfirmStatus;
import com.openexchange.groupware.container.participants.ConfirmableParticipant;

/**
 * {@link ParticipantTools}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ParticipantTools {

    /**
     * Checks whether two arrays contain different participants or not,
     * ignoring ID differences for external participants, optionally also
     * comparing the confirmation status.
     *
     * @param participants1 the first list of participants
     * @param participants2 the second list of participants
     * @param compareStatus
     * @return <code>true</code>, if there are different participants, <code>false</code>, otherwise
     */
    public static boolean equals(Participant[] participants1, Participant[] participants2, boolean compareStatus) {
        if (null == participants1) {
            return null == participants2;
        } else if (null == participants2) {
            return false;
        } else {
            return participants1.length == participants2.length && containsAll(participants1, participants2, compareStatus);
        }
    }

    /**
     * Checks whether two sets contain different participants or not,
     * ignoring ID differences for external participants, optionally also
     * comparing the confirmation status.
     *
     * @param participants1 the first list of participants
     * @param participants2 the second list of participants
     * @param compareStatus
     * @return <code>true</code>, if there are different participants, <code>false</code>, otherwise
     */
    public static boolean equals(Set<Participant> participants1, Set<Participant> participants2, boolean compareStatus) {
        if (null == participants1) {
            return null == participants2;
        } else if (null == participants2) {
            return false;
        } else  {
            return participants1.size() == participants2.size() && containsAll(participants1, participants2, compareStatus);
        }
    }

    /**
     * Determines whether a set of participants contains a collection of
     * participants, ignoring ID differences for external participants,
     * optionally also comparing the confirmation status.
     *
     * @param participants1
     * @param participants2
     * @param compareStatus
     * @return
     */
    public static boolean containsAll(Set<Participant> participants1, Collection<Participant> participants2, boolean compareStatus) {
        for (Participant participant2 : participants2) {
            if (false == contains(participants1, participant2, compareStatus)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Determines whether an array of participants contains a collection of
     * participants, ignoring ID differences for external participants,
     * optionally also comparing the confirmation status.
     *
     * @param participants1
     * @param participants2
     * @param compareStatus
     * @return
     */
    public static boolean containsAll(Participant[] participants1, Participant[] participants2, boolean compareStatus) {
        for (Participant participant2 : participants2) {
            if (false == contains(Arrays.asList(participants1), participant2, compareStatus)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Determines whether a collection of participants contains a participant,
     * ignoring ID differences for external participants, optionally also
     * comparing the confirmation status.
     *
     * @param participants
     * @param participant
     * @param compareStatus
     * @return
     */
    public static boolean contains(Collection<Participant> participants, Participant participant, boolean compareStatus) {
        if (null == participant || null == participants) {
            return false;
        } else {
            for (Participant p : participants) {
                if (null != p && equals(p, participant, compareStatus)) {
                    return true;
                }
            }
            return false; // not found
        }
//
//
//            if (Participant.EXTERNAL_USER == participant.getType()) {
//            for (Participant p : participants) {
//                if (null != p && equals(p, participant, compareStatus)) {
//                    return true;
//                }
//            }
//            return false; // not found
//        } else {
//            return participants.contains(participant);
//        }
    }

    /**
     * Gets a set of individual participants of an appointment, i.e. unique participants that are either internal or external users /
     * resource. Group-participants are excluded from the result.
     *
     * @param appointment The appointment to extract the participants from
     * @return A set of individual participants, that may be empty, but never <code>null</code>
     */
    public static Set<Participant> getIndividualParticipants(Appointment appointment) {
        Set<Participant> individualParticipants = new HashSet<Participant>();
        if (null != appointment) {
            Set<Integer> userIDs = new HashSet<Integer>();
            Participant[] participants = appointment.getParticipants();
            if (null != participants) {
                for (Participant participant : participants) {
                    if (Participant.USER == participant.getType()) {
                        if (userIDs.add(Integer.valueOf(participant.getIdentifier()))) {
                            individualParticipants.add(participant);
                        }
                    } else if (Participant.EXTERNAL_USER == participant.getType() || Participant.RESOURCE == participant.getType()) {
                        individualParticipants.add(participant);
                    }
                }
            }
            UserParticipant[] userParticipants = appointment.getUsers();
            if (null != userParticipants) {
                for (UserParticipant userParticipant : userParticipants) {
                    if (userIDs.add(Integer.valueOf(userParticipant.getIdentifier()))) {
                        individualParticipants.add(userParticipant);
                    }
                }
            }
        }
        return individualParticipants;
    }

    /**
     * Compares on participant with another, ignoring ID differences for
     * external participants.
     *
     * @param participant1
     * @param participant2
     * @return
     */
    public static boolean equals(Participant participant1, Participant participant2) {
        return equals(participant1, participant2, false);
    }

    /**
     * Compares on participant with another, ignoring ID differences for
     * external participants, optionally also comparing the confirmation
     * status.
     *
     * @param participant1
     * @param participant2
     * @param compareStatus
     * @return
     */
    public static boolean equals(Participant participant1, Participant participant2, boolean compareStatus) {
        if (Participant.EXTERNAL_USER == participant1.getType() && null != participant1.getEmailAddress() &&
                participant1.getEmailAddress().equals(participant2.getEmailAddress()) || participant1.equals(participant2)) {
            return compareStatus ? getStatus(participant1).equals(getStatus(participant2)) : true;
        } else {
            return false;
        }
    }

    /**
     * Gets the confirmation status for a participant.
     *
     * @param participant
     * @return
     */
    public static ConfirmStatus getStatus(Participant participant) {
        if (ConfirmableParticipant.class.isInstance(participant)) {
            return ((ConfirmableParticipant)participant).getStatus();
        } else if (UserParticipant.class.isInstance(participant)) {
            return ConfirmStatus.byId(((UserParticipant)participant).getConfirm());
        } else {
            return ConfirmStatus.NONE;
        }
    }

    /**
     * Gets the personal alarm setting from an appointment.
     *
     * @param appointment The appointment
     * @param userID The ID of the user to get the reminder time for
     * @return The reminder minutes, or <code>-1</code> if not set
     */
    public static int getReminderMinutes(Appointment appointment, int userID) {
        UserParticipant user = findUser(appointment, userID);
        return null != user && user.containsAlarm() ? user.getAlarmMinutes() : -1;
    }

    /**
     * Gets a specific user participant from an appointment.
     *
     * @param appointment The appointment
     * @param userID The ID of the user to get
     * @return The user particpant, or <code>null</code> if not found
     */
    public static UserParticipant findUser(Appointment appointment, int userID) {
        if (null != appointment && null != appointment.getUsers()) {
            for (UserParticipant user : appointment.getUsers()) {
                if (user.getIdentifier() == userID) {
                    return user;
                }
            }
        }
        return null;
    }

    private ParticipantTools() {
        super();
    }

}
