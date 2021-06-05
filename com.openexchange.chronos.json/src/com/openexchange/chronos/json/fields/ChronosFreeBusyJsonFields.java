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

import com.openexchange.chronos.service.FreeBusyResult;

/**
 * {@link ChronosFreeBusyJsonFields} contains all fields which are used by the freeBusy action
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class ChronosFreeBusyJsonFields {

    /**
     * The attendee of the {@link FreeBusyResult}
     */
    public static final String ATTENDEE = "attendee";
    /**
     * The free busy times of the attendee. See {@link FreeBusyResult#getFreeBusyTimes()}.
     */
    public static final String FREE_BUSY_TIME = "freeBusyTime";

    /**
     * The warnings of the attendee. See {@link FreeBusyResult#getWarnings()}.
     */
    public static final String WARNINGS = "warnings";

    public static final class FreeBusyTime {
        /**
         * The start time of the free-busy time-slot. See {@link com.openexchange.chronos.FreeBusyTime#getStartTime()}.
         */
        public static final String START_TIME = "startTime";
        /**
         * The end time of the free-busy time-slot. See {@link com.openexchange.chronos.FreeBusyTime#getEndTime()}.
         */
        public static final String END_TIME = "endTime";
        /**
         * The type of the free-busy time-slot. See {@link com.openexchange.chronos.FreeBusyTime#getFbType()}.
         */
        public static final String FB_TYPE = "fbType";
        /**
         * The event the free-busy time-slot corresponds to. See {@link com.openexchange.chronos.FreeBusyTime#getEvent()}.
         */
        public static final String EVENT = "event";
    }
}

