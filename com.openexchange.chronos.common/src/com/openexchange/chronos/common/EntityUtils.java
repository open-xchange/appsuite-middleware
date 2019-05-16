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
 *    trademarks of the OX Software GmbH. group of companies.
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
