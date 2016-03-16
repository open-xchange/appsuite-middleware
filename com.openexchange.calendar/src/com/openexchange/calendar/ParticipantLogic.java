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

package com.openexchange.calendar;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.participants.ConfirmableParticipant;

/**
 * Some operations to deal with participant arrays and sets.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ParticipantLogic {

    private ParticipantLogic() {
        super();
    }

    /**
     * Merges the new external user participants into the known participant array. This method ensures that the external participants can be
     * removed from the normal calendar participant storage. They must only be stored in the external participant table.
     * @param participants participant array.
     * @param externals loaded external participants.
     * @return an array containing the participants extended with the external participants.
     */
    public static Participant[] mergeFallback(Participant[] participants, ExternalUserParticipant[] externals) {
        // Sets and contains may not work because old external user participants get an identifier.
        List<Participant> retval = new ArrayList<Participant>();
        if (null != participants) {
            for (Participant participant : participants) {
                retval.add(participant);
            }
        }
        if (null != externals) {
            for (ExternalUserParticipant participant : externals) {
                Participant contained = get(retval, participant);
                if (null == contained) {
                    retval.add(participant);
                } else {
                    extendData(participant, contained);
                }
            }
        }
        return retval.toArray(new Participant[retval.size()]);
    }

    public static ConfirmableParticipant[] mergeConfirmations(ExternalUserParticipant[] externals, Participant[] participants) {
        List<ConfirmableParticipant> retval = new ArrayList<ConfirmableParticipant>();
        if (null != externals) {
            for (ExternalUserParticipant external : externals) {
                retval.add(external);
            }
        }
        if (null != participants) {
            for (Participant participant : participants) {
                if (!(participant instanceof ConfirmableParticipant)) {
                    continue;
                }
                ConfirmableParticipant confirmable = (ConfirmableParticipant) participant;
                if (!contains(retval, confirmable)) {
                    retval.add(confirmable);
                }
            }
        }
        return retval.toArray(new ConfirmableParticipant[retval.size()]);
    }

    /**
     * Checks only by mailAddress if a participant is contained.
     */
    private static boolean contains(List<? extends Participant> retval, Participant participant) {
        return null != get(retval, participant);
    }

    /**
     * Checks only by mailAddress if an external participant is contained in the list.
     */
    private static Participant get(List<? extends Participant> participantList, Participant participant) {
        for (Participant fromList : participantList) {
            if (participant.getEmailAddress().equals(fromList.getEmailAddress())) {
                return fromList;
            }
        }
        return null;
    }

    private static void extendData(ExternalUserParticipant from, Participant other) {
        if (!(other instanceof ExternalUserParticipant)) {
            return;
        }
        ExternalUserParticipant to = (ExternalUserParticipant) other;
        if (null == to.getDisplayName() && null != to.getDisplayName()) {
            to.setDisplayName(from.getDisplayName());
        }
        if (!to.containsStatus() && from.containsStatus()) {
            to.setStatus(from.getStatus());
        }
        if (!to.containsMessage() && from.containsMessage()) {
            to.setMessage(from.getMessage());
        }
    }
}
