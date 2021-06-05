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

package com.openexchange.chronos.impl;

import java.util.List;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.service.EventConflict;

/**
 * {@link EventConflictImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class EventConflictImpl implements EventConflict {

    private final Event conflictingEvent;
    private final List<Attendee> conflictingAttendees;
    private final boolean hardConflict;

    /**
     * Initializes a new {@link EventConflictImpl}.
     *
     * @param conflictingEvent The conflicting event
     * @param conflictingAttendees The conflicting attendees
     * @param hardConflict <code>true</code> for a <i>hard</i>, i.e. non-ignorable conflict, <code>false</code>, otherwise
     */
    public EventConflictImpl(Event conflictingEvent, List<Attendee> conflictingAttendees, boolean hardConflict) {
        super();
        this.conflictingEvent = conflictingEvent;
        this.conflictingAttendees = conflictingAttendees;
        this.hardConflict = hardConflict;
    }

    @Override
    public Event getConflictingEvent() {
        return conflictingEvent;
    }

    @Override
    public List<Attendee> getConflictingAttendees() {
        return conflictingAttendees;
    }

    @Override
    public boolean isHardConflict() {
        return hardConflict;
    }

}
