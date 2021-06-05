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

import java.io.Serializable;
import java.util.Comparator;
import com.openexchange.chronos.Availability;
import com.openexchange.chronos.Available;
import com.openexchange.chronos.FreeBusyTime;
import com.openexchange.chronos.common.DateTimeComparator;
import com.openexchange.chronos.impl.availability.performer.GetPerformer;

/**
 * {@link Comparators}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class Comparators {

    public static final Comparator<Available> AVAILABLE_DATE_TIME_COMPARATOR = new FreeSlotDateTimeComparator();
    public static final Comparator<FreeBusyTime> FREE_BUSY_DATE_TIME_COMPARATOR = new FreeBusyTimeDateTimeComparator();

    /**
     * {@link DateTimeComparator} - DateTime comparator. Orders {@link Available} items
     * by start date (ascending)
     */
    public static class FreeSlotDateTimeComparator implements Comparator<Available>, Serializable {

        private static final long serialVersionUID = -5782820265507447924L;

        /**
         * Initialises a new {@link GetPerformer.DateTimeComparator}.
         */
        public FreeSlotDateTimeComparator() {
            super();
        }

        @Override
        public int compare(Available o1, Available o2) {
            if (o1.getStartTime().before(o2.getStartTime())) {
                return -1;
            } else if (o1.getStartTime().after(o2.getStartTime())) {
                return 1;
            }
            return 0;
        }
    }

    /**
     * {@link DateTimeComparator} - DateTime comparator. Orders {@link Availability} items
     * by start date (ascending)
     */
    public static class AvailabilityDateTimeComparator implements Comparator<Availability>, Serializable {

        private static final long serialVersionUID = 2953299600953128028L;

        /**
         * Initialises a new {@link GetPerformer.DateTimeComparator}.
         */
        public AvailabilityDateTimeComparator() {
            super();
        }

        @Override
        public int compare(Availability o1, Availability o2) {
            if (o1.getStartTime().before(o2.getStartTime())) {
                return -1;
            } else if (o1.getStartTime().after(o2.getStartTime())) {
                return 1;
            }
            return 0;
        }
    }

    /**
     * {@link PriorityComparator} - Priority comparator. Orders {@link Availability} items
     * by priority (descending). We want elements with higher priority (in this context '1' > '9' > '0')
     * to be on the top of the list.
     */
    public static class PriorityComparator implements Comparator<Availability>, Serializable {

        private static final long serialVersionUID = 1746812827168863381L;

        /**
         * Initialises a new {@link GetPerformer.PriorityComparator}.
         */
        public PriorityComparator() {
            super();
        }

        @Override
        public int compare(Availability o1, Availability o2) {
            // Use '10' for '0' as '0' has a lower priority than '9'
            int o1Priority = o1.getPriority() == 0 ? 10 : o1.getPriority();
            int o2Priority = o2.getPriority() == 0 ? 10 : o2.getPriority();

            //We want elements with higher priority (in this context '1' > '9' > '0') to be on the top of the list
            if (o1Priority > o2Priority) {
                return 1;
            } else if (o1Priority < o2Priority) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    /**
     * {@link FreeBusyTime} - DateTime comparator. Orders {@link FreeBusyTime} items
     * by start date (ascending)
     */
    public static class FreeBusyTimeDateTimeComparator implements Comparator<FreeBusyTime>, Serializable {

        private static final long serialVersionUID = 8548997917793298868L;

        /**
         * Initialises a new {@link GetPerformer.DateTimeComparator}.
         */
        public FreeBusyTimeDateTimeComparator() {
            super();
        }

        @Override
        public int compare(FreeBusyTime o1, FreeBusyTime o2) {
            if (o1.getStartTime().before(o2.getStartTime())) {
                return -1;
            } else if (o1.getStartTime().after(o2.getStartTime())) {
                return 1;
            }
            return 0;
        }
    }
}
