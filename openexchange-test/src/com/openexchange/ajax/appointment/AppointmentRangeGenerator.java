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
 *    trademarks of the OX Software GmbH group of companies.
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
    public  class AppointmentRange {
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