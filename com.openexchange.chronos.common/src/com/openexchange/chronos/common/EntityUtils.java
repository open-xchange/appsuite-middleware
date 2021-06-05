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

package com.openexchange.chronos.common;

import static com.openexchange.chronos.common.CalendarUtils.isInternal;
import static com.openexchange.java.Autoboxing.I;
import java.util.Set;
import com.openexchange.chronos.Attendee;

/**
 * {@link EntityUtils}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class EntityUtils {

    /**
     * Determines the next unique entity identifier to use when inserting an entry into the <code>calendar_attendee</code> table. For
     * <i>internal</i> attendees, this is always the (already unique) entity identifier itself. For <i>external</i> attendees, the
     * identifier is always negative and based on the hash code of the URI.
     *
     * @param attendee The attendee to determine the entity for
     * @param usedEntities The so far used entities to avoid hash collisions
     * @param entitySalt Random to enrich the hash calculation for the entity identifiers of external attendees
     * @return The entity
     */
    public static int determineEntity(Attendee attendee, Set<Integer> usedEntities, int entitySalt) {
        if (isInternal(attendee)) {
            usedEntities.add(I(attendee.getEntity()));
            return attendee.getEntity();
        }
        String uri = attendee.getUri();
        int entity = 31 * 1 + entitySalt;
        if (uri != null) {
            entity = 31 * entity + uri.hashCode();
        }
        if (entity > 0) {
            entity = -1 * entity;
        }
        while (false == usedEntities.add(I(entity))) {
            entity--;
        }
        return entity;
    }

}
