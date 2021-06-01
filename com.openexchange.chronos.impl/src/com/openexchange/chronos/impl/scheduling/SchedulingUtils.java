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

package com.openexchange.chronos.impl.scheduling;

import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.common.CalendarUtils;

/**
 * {@link SchedulingUtils}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.6
 */
public class SchedulingUtils {

    private SchedulingUtils() {
        super();
    }

    /**
     * Check if originator is allowed to perform any action,
     * either by perfect match comparing to the organizer
     * or by comparing to the sent-by field of the organizer
     *
     * @param originalEvent The original event to get the organizer from
     * @param originator The originator of a scheduling action
     * @return <code>true</code> if the originator matches the organizer, <code>false</code> otherwise
     */
    public static boolean originatorMatches(Event originalEvent, CalendarUser originator) {
        Organizer organizer = originalEvent.getOrganizer();
        return CalendarUtils.matches(originator, organizer) //perfect match 
            || (null != organizer.getSentBy() && CalendarUtils.matches(originator, organizer.getSentBy()));
    }

}
