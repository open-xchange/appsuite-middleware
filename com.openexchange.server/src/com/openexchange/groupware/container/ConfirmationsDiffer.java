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
import com.openexchange.groupware.container.participants.ConfirmableParticipant;

/**
 * {@link ConfirmationsDiffer}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class ConfirmationsDiffer extends Differ<CalendarObject> {

    @SuppressWarnings("unchecked")
    @Override
    public Difference getDifference(CalendarObject original, CalendarObject update) {
        if (!update.containsConfirmations()) {
            return null;
        }

        if (!original.containsConfirmations() && update.containsConfirmations()) {
            Difference difference = new Difference(CalendarObject.CONFIRMATIONS);
            difference.getAdded().addAll(Arrays.asList(update.getConfirmations()));
            return difference;
        }

        if (original.getConfirmations() == update.getConfirmations()) {
            return null;
        }

        if (original.getConfirmations() == null) {
            Difference difference = new Difference(CalendarObject.CONFIRMATIONS);
            difference.getAdded().addAll(Arrays.asList(update.getConfirmations()));
            return difference;
        }

        boolean isDifferent = false;
        Difference difference = new Difference(CalendarObject.CONFIRMATIONS);

        for (ConfirmableParticipant o : original.getConfirmations()) {
            boolean found = false;
            for (ConfirmableParticipant u : update.getConfirmations()) {
                if (o.getEmailAddress() != null && o.getEmailAddress().equalsIgnoreCase(u.getEmailAddress())) {
                    Change change = getChange(o, u);
                    found = true;
                    if (change != null) {
                        isDifferent = true;
                        difference.getChanged().add(change);
                    }
                    break;
                }
            }
            if (!found) {
                difference.getRemoved().add(o);
                isDifferent = true;
            }
        }

        for (ConfirmableParticipant u : update.getConfirmations()) {
            boolean found = false;
            for (ConfirmableParticipant o : original.getConfirmations()) {
                if (u.getEmailAddress() != null && u.getEmailAddress().equalsIgnoreCase(o.getEmailAddress())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                difference.getAdded().add(u);
                isDifferent = true;
            }
        }

        return isDifferent ? difference : null;
    }

    private Change getChange(ConfirmableParticipant original, ConfirmableParticipant update) {
        boolean changed = false;

        ConfirmationChange change = new ConfirmationChange(original.getEmailAddress());
        if (original.getConfirm() != update.getConfirm()) {
            changed = true;
            change.setStatus(original.getConfirm(), update.getConfirm());
        }

        if (original.getMessage() != update.getMessage() && (original.getMessage() == null && update.getMessage() != null || !original.getMessage().equals(update.getMessage()))) {
            changed = true;
            change.setMessage(original.getMessage(), update.getMessage());
            change.setStatus(original.getConfirm(), update.getConfirm()); // Also set status on change.
        }

        return changed ? change : null;
    }

    @Override
    public int getColumn() {
        return CalendarObject.CONFIRMATIONS;
    }

}
