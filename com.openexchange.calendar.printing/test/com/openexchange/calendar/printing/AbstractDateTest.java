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

package com.openexchange.calendar.printing;

import java.util.Calendar;
import java.util.Date;
import junit.framework.TestCase;

/**
 * {@link AbstractDateTest}
 *
 * @author <a href="mailto:firstname.lastname@open-xchange.com">Firstname Lastname</a>
 */
public abstract class AbstractDateTest extends TestCase {

    /**
     * Initializes a new {@link AbstractDateTest}.
     */
    public AbstractDateTest() {
        super();
    }

    /**
     * Initializes a new {@link AbstractDateTest}.
     * @param name
     */
    public AbstractDateTest(String name) {
        super(name);
    }

    public Date plusOneHour(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.HOUR_OF_DAY, 1);
        return cal.getTime();
    }

    /**
     * Gets you four dates, starting one day and two hours before the given calendar point
     */
    protected Date[] getFourDates(Calendar cal) {
        Date date11 = cal.getTime();
        cal.add(Calendar.HOUR_OF_DAY, -1);
        Date date10 = cal.getTime();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        Date date01 = cal.getTime();
        cal.add(Calendar.HOUR_OF_DAY, -1);
        Date date00 = cal.getTime();

        cal.add(Calendar.DAY_OF_YEAR, 1);
        cal.add(Calendar.HOUR_OF_DAY, 2);
        return new Date[]{date00,date01,date10,date11};
    }

    /**
     * @return 7.1.2009 was a wednesday
     */
    protected Date WEDNESDAY() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_YEAR, 7);
        cal.set(Calendar.YEAR, 2009);
        cal.set(Calendar.HOUR_OF_DAY, 12);
        return cal.getTime();
    }


    /**
     * @return 14.1.2009 was a Wednesday
     */
    protected Date WEDNESDAY_NEXT_WEEK() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_YEAR, 14);
        cal.set(Calendar.YEAR, 2009);
        cal.set(Calendar.HOUR_OF_DAY, 12);
        return cal.getTime();
    }

    /**
     * @return 4.2.2009 was a Wednesday
     */
    protected Date WEDNESDAY_NEXT_MONTH() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2009);
        cal.set(Calendar.MONTH, 2);
        cal.set(Calendar.DAY_OF_MONTH, 4);
        cal.set(Calendar.HOUR_OF_DAY, 12);
        return cal.getTime();
    }

    /**
     * @return 8.1.2009 was a Thursday
     */
    protected Date THURSDAY() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_YEAR, 8);
        cal.set(Calendar.YEAR, 2009);
        cal.set(Calendar.HOUR_OF_DAY, 12);
        return cal.getTime();
    }
    /**
     * @return 10.1.2009 was a Saturday
     */
    protected Date SATURDAY() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_YEAR, 10);
        cal.set(Calendar.YEAR, 2009);
        cal.set(Calendar.HOUR_OF_DAY, 12);
        return cal.getTime();
    }
    /**
     * @return 11.1.2009 was a Sunday
     */
    protected Date SUNDAY() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_YEAR, 11);
        cal.set(Calendar.YEAR, 2009);
        cal.set(Calendar.HOUR_OF_DAY, 12);
        return cal.getTime();
    }

    /**
     * @return 12.1.2009 was a Monday
     */
    protected Date MONDAY_NEXT_WEEK() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_YEAR, 12);
        cal.set(Calendar.YEAR, 2009);
        cal.set(Calendar.HOUR_OF_DAY, 12);
        return cal.getTime();
    }

    protected Calendar getCalendar(){
        return Calendar.getInstance();
    }

}
