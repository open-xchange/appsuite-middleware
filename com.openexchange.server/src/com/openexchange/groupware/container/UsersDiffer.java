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
 * {@link UsersDiffer}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class UsersDiffer extends Differ<CalendarObject> {

    @SuppressWarnings("unchecked")
    @Override
    public Difference getDifference(CalendarObject original, CalendarObject update) {
        if (!update.containsUserParticipants()) {
            return null;
        }

        if (!original.containsUserParticipants() && update.containsUserParticipants()) {
            Difference difference = new Difference(CalendarObject.USERS);
            difference.getAdded().addAll(Arrays.asList(update.getUsers()));
            return difference;
        }

        if (original.getUsers() == update.getUsers()) {
            return null;
        }

        if (original.getUsers() == null) {
            Difference difference = new Difference(CalendarObject.USERS);
            difference.getAdded().addAll(Arrays.asList(update.getUsers()));
            return difference;
        }

        boolean isDifferent = false;
        Difference difference = new Difference(CalendarObject.USERS);

        if (original.getUsers() != null) {
            for (UserParticipant o : original.getUsers()) {
                boolean found = false;
                if (update.getUsers() != null) {
                    for (UserParticipant u : update.getUsers()) {
                        if (o.getIdentifier() == u.getIdentifier()) {
                            found = true;
                            Change change = getChange(o, u);
                            if (change != null) {
                                isDifferent = true;
                                difference.getChanged().add(change);
                            }
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

        if (update.getUsers() != null) {
            for (UserParticipant u : update.getUsers()) {
                boolean found = false;
                if (original.getUsers() != null) {
                    for (UserParticipant o : original.getUsers()) {
                        if (u.getIdentifier() == o.getIdentifier()) {
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

    private Change getChange(UserParticipant original, UserParticipant update) {
        boolean changed = false;

        ConfirmationChange change = new ConfirmationChange(Integer.toString(original.getIdentifier()));
        if (update.containsConfirm() && original.getConfirm() != update.getConfirm()) {
            changed = true;
            change.setStatus(original.getConfirm(), update.getConfirm());
        }

        if (update.containsConfirmMessage() && original.getConfirmMessage() != update.getConfirmMessage() && (original.getConfirmMessage() == null && update.getConfirmMessage() != null || !original.getConfirmMessage().equals(update.getConfirmMessage()))) {
            changed = true;
            change.setMessage(original.getConfirmMessage(), update.getConfirmMessage());
        }

        return changed ? change : null;
    }

    @Override
    public int getColumn() {
        return CalendarObject.USERS;
    }

}
