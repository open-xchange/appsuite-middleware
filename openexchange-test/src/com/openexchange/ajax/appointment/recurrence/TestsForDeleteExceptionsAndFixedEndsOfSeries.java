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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.ajax.appointment.recurrence;

import com.openexchange.groupware.container.Appointment;


/**
 * There are two ways to limit an apointment series: One is by date, one is by 
 * number of occurrences. There is supposed to be a difference handling deletes: 
 * In the occurrence case, the amount should be decreased. The limiting date 
 * ("until"), however, should not be changed.
 *  
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class TestsForDeleteExceptionsAndFixedEndsOfSeries extends ManagedAppointmentTest {

    public TestsForDeleteExceptionsAndFixedEndsOfSeries(String name) {
        super(name);
    }

    public void testShouldNotReduceNumberOfOccurrencesWhenDeletingOneInYearlySeries() throws Exception{
        calendarManager.setFailOnError(false);
        Appointment app = generateYearlyAppointment();
        app.setOccurrence(5);
        calendarManager.insertAppointmentOnServer(app);
        assertFalse("Should not fail during creation of series", calendarManager.hasLastException());
        
        Appointment actual = calendarManager.getAppointmentFromServer(app);
        assertFalse("Should not fail during first retrieval of series", calendarManager.hasLastException());
        assertEquals("Should have set amount of occurences" , 5 , actual.getOccurrence());

        calendarManager.createDeleteException(app, 5);
        assertFalse("Should not fail during creation of delete exception", calendarManager.hasLastException());
        
        actual = calendarManager.getAppointmentFromServer(app);
        assertFalse("Should not fail during second retrieval of series", calendarManager.hasLastException());
        assertEquals("Should have one less occurrence after delete exception" , 5 , actual.getOccurrence());
        assertFalse("Should not have an until value, only an occurrence value", actual.containsUntil());
    }
}
