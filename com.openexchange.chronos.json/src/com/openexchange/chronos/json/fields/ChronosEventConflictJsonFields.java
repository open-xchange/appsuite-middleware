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

package com.openexchange.chronos.json.fields;

/**
 * {@link ChronosEventConflictJsonFields} contains fields for event conflicts
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class ChronosEventConflictJsonFields {

    /**
     * An array of conflicts. See {@link EventConflict}
     */
    public static final String CONFLICTS = "conflicts";

    public static final class EventConflict {

        /**
         * Defines whether the conflict is a hard conflict. See {@link EventConflict#isHardConflict()}
         */
        public static final String HARD_CONFLICT = "hard_conflict";

        /**
         * The conflicting attendees. See {@link EventConflict#getConflictingAttendees()}
         */
        public static final String CONFLICTING_ATTENDEES = "conflicting_attendees";

        /**
         * The conflicting event. See {@link EventConflict#getConflictingEvent()}
         */
        public static final String EVENT = "event";

    }
}
