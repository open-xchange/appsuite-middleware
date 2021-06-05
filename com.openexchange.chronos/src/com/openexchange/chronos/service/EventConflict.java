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

package com.openexchange.chronos.service;

import java.util.List;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.exception.OXException.ProblematicAttribute;

/**
 * {@link EventConflict}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public interface EventConflict extends ProblematicAttribute {

    /**
     * Gets the underlying conflicting event.
     *
     * @return The underlying conflicting event
     */
    Event getConflictingEvent();

    /**
     * Gets a list of conflicting attendees.
     *
     * @return The conflicting attendees
     */
    List<Attendee> getConflictingAttendees();

    /**
     * Gets a value indicating whether this conflict is <i>hard</i>, i.e. cannot be ignored.
     *
     * @return <code>true</code> if this is a <i>hard</i> conflict, <code>false</code>, otherwise
     */
    boolean isHardConflict();

}
