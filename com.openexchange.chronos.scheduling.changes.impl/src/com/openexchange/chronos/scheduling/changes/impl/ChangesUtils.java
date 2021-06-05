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

package com.openexchange.chronos.scheduling.changes.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.java.Strings;

/**
 * {@link ChangesUtils}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public class ChangesUtils {

    private ChangesUtils() {}

    public static List<Attendee> sortAttendees(List<Attendee> attendees) {
        return new ArrayList<>(attendees).stream().sorted(new AttendeeComperator()).collect(Collectors.toList());
    }

    public static List<Attendee> sortAttendees(List<Attendee> attendees, CalendarUserType cuType) {
        return new ArrayList<>(attendees).stream().filter(a -> cuType.matches(a.getCuType())).sorted(new AttendeeComperator(cuType)).collect(Collectors.toList());
    }

    private static class AttendeeComperator implements Comparator<Attendee> {

        private final CalendarUserType cuType;

        /**
         * Initializes a new {@link AttendeeComperator}.
         * 
         */
        public AttendeeComperator() {
            this(null);
        }

        /**
         * Initializes a new {@link AttendeeComperator}.
         * 
         * @param cuType The {@link CalendarUserType}
         */
        public AttendeeComperator(CalendarUserType cuType) {
            super();
            this.cuType = null == cuType ? CalendarUserType.INDIVIDUAL : cuType;
        }

        @Override
        public int compare(Attendee a1, Attendee a2) {
            if (CalendarUtils.isInternal(a1, cuType)) {
                if (CalendarUtils.isInternal(a2, cuType)) {
                    // Both internal users
                    return a1.getCn().compareTo(a2.getCn());
                }
                return -1;
            }
            if (CalendarUtils.isInternal(a2, cuType)) {
                // a1 is not, a2 is internal
                return 1;
            }
            // Check URI of externals
            if (Strings.isNotEmpty(a1.getUri())) {
                if (Strings.isNotEmpty(a2.getUri())) {
                    return a1.getUri().compareTo(a2.getUri());
                }
                return -1;
            }
            if (Strings.isNotEmpty(a2.getUri())) {
                return 1;
            }
            // Last fallback, use mail
            if (Strings.isNotEmpty(a1.getEMail())) {
                if (Strings.isNotEmpty(a2.getEMail())) {
                    return a1.getEMail().compareTo(a2.getEMail());
                }
                return -1;
            }
            if (Strings.isNotEmpty(a2.getEMail())) {
                return 1;
            }
            return 0;
        };

    }

}
