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
