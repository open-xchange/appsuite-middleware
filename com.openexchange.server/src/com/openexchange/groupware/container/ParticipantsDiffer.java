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

import java.util.Arrays;

/**
 * {@link ParticipantsDiffer}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class ParticipantsDiffer extends Differ<CalendarObject> {

    @SuppressWarnings("unchecked")
    @Override
    public Difference getDifference(CalendarObject original, CalendarObject update) {
        if (!update.containsParticipants()) {
            return null;
        }

        if (!original.containsParticipants() && update.containsParticipants()) {
            Difference difference = new Difference(CalendarObject.PARTICIPANTS);
            difference.getAdded().addAll(Arrays.asList(update.getParticipants()));
            return difference;
        }

        if (original.getParticipants() == update.getParticipants()) {
            return null;
        }

        if (original.getParticipants() == null) {
            Difference difference = new Difference(CalendarObject.PARTICIPANTS);
            difference.getAdded().addAll(Arrays.asList(update.getParticipants()));
            return difference;
        }

        boolean isDifferent = false;

        Difference difference = new Difference(CalendarObject.PARTICIPANTS);

        if (original.getParticipants() != null) {
            for (Participant o : original.getParticipants()) {
                boolean found = false;
                if (update.getParticipants() != null) {
                    for (Participant u : update.getParticipants()) {
                        if (o.getIdentifier() == u.getIdentifier() && o.getIdentifier() != -1 || o.getEmailAddress() != null && o.getEmailAddress().equalsIgnoreCase(u.getEmailAddress())) {
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                    difference.getRemoved().add(o);
                    isDifferent = true;
                }
            }
        }

        if (update.getParticipants() != null) {
            for (Participant u : update.getParticipants()) {
                boolean found = false;
                if (original.getParticipants() != null) {
                    for (Participant o : original.getParticipants()) {
                        if (u.getIdentifier() == o.getIdentifier() && o.getIdentifier() != -1 || u.getEmailAddress() != null && u.getEmailAddress().equalsIgnoreCase(o.getEmailAddress())) {
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                    difference.getAdded().add(u);
                    isDifferent = true;
                }
            }
        }

        return isDifferent ? difference : null;
    }

    public ConfirmationChange getConfirmChange(UserParticipant original, UserParticipant update) {
        boolean changed = false;

        ConfirmationChange change = new ConfirmationChange(Integer.toString(original.getIdentifier()));
        if (original.getConfirm() != update.getConfirm()) {
            changed = true;
            change.setStatus(original.getConfirm(), update.getConfirm());
        }

        return changed ? change : null;
    }

    @Override
    public int getColumn() {
        return CalendarObject.PARTICIPANTS;
    }

}
