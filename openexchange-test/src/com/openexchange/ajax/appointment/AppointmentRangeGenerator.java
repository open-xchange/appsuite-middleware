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

package com.openexchange.ajax.appointment;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

/**
 * Generates random date ranges spanning from am to pm on a workday. {@link AppointmentRangeGenerator}
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class AppointmentRangeGenerator {

    /**
     * A DateRange container consisting of a start- and endDate.
     * {@link AppointmentRange}
     *
     * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
     */
    public class AppointmentRange {

        public Date startDate;
        public Date endDate;

        public AppointmentRange(Date startDate, Date endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }

    private final Random random;

    private final Calendar calendar;

    /**
     * Initializes a new {@link AppointmentRangeGenerator}.
     * 
     * @param calendar Calendar of the user
     */
    public AppointmentRangeGenerator(Calendar calendar) {
        this.random = new Random();
        this.calendar = calendar;
    }

    /**
     * Returns a random date range spanning from am to pm on a workday.
     * 
     * @return a random date range spanning from am to pm on a workday.
     */
    public AppointmentRange getDateRange() {
        Date startDate, endDate;

        calendar.set(Calendar.DAY_OF_WEEK, getRandomWorkDay());

        calendar.set(Calendar.HOUR_OF_DAY, getRandomAMTime());
        startDate = calendar.getTime();

        calendar.set(Calendar.HOUR_OF_DAY, getRandomPMTime());
        endDate = calendar.getTime();

        return new AppointmentRange(startDate, endDate);
    }

    /**
     * Generate a random workday MO-FR
     * 
     * @return and int for the random workday
     */
    private int getRandomWorkDay() {
        return Calendar.MONDAY + random.nextInt(5);
    }

    /**
     * Generate a random full hour AM time between 0 and 11
     * 
     * @return a random full hour PM time between 0 and 11
     */
    private int getRandomAMTime() {
        return random.nextInt(12);
    }

    /**
     * Generate a random full hour PM time between 12 and 23
     * 
     * @return a random full hour PM time between 12 and 23
     */
    private int getRandomPMTime() {
        return 12 + random.nextInt(12);
    }
}
