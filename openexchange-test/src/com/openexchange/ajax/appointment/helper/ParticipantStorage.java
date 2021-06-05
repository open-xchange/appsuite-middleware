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

package com.openexchange.ajax.appointment.helper;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.participants.ConfirmableParticipant;

/**
 * Interface to the storage of external participants.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ParticipantStorage {

    public static final ExternalUserParticipant[] extractExternal(Participant[] participants) {
        return extractExternal(participants, null);
    }
    public static final ExternalUserParticipant[] extractExternal(Participant[] participants, ConfirmableParticipant[] confirmations) {
        List<ExternalUserParticipant> retval = new ArrayList<ExternalUserParticipant>();
        if (null != participants) {
            for (Participant participant : participants) {
                if (participant instanceof ExternalUserParticipant) {
                    ExternalUserParticipant external = (ExternalUserParticipant) participant;
                    if (confirmations != null) {
                        for (ConfirmableParticipant confirmation : confirmations) {
                            if (external.getEmailAddress().equals(confirmation.getEmailAddress())) {
                                external.setConfirm(confirmation.getConfirm());
                                external.setMessage(confirmation.getMessage());
                            }
                        }
                    }
                    retval.add((ExternalUserParticipant) participant);
                }
            }
        }
        return retval.toArray(new ExternalUserParticipant[retval.size()]);
    }
}
