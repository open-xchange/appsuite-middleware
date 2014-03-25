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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.calendar;

import java.sql.Date;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.session.Session;

/**
 * Unit tests for {@link CalendarSql}
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.0
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({})
public class CalendarSqlTest {

    @InjectMocks
    private CalendarSql calendarSql;

    @Mock
    private Session session;

    private int objectId = 111;

    private int folderId = 222;

    private String mail = "martin.schneider@open-xchange.com";

    private int occurrence = 333;

    private int userId = 444;

    private int confirm = Appointment.DECLINE;

    private String confirmMessage = "not yet";

    private Date returnDate = new Date(951782400000L);

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSetExternalConfirmation_occurrenceZero_callSetForSingleAppointment() throws OXException {

        CalendarSql calendarSqlSpy = Mockito.spy(calendarSql);
        Mockito.doReturn(returnDate).when(calendarSqlSpy).setExternalConfirmation(Matchers.anyInt(), Matchers.anyInt(), Matchers.anyString(), Matchers.anyInt(), Matchers.anyString());

        CalendarDataObject setExternalConfirmation = calendarSqlSpy.setExternalConfirmation(objectId, folderId, 0, mail, confirm, confirmMessage);

        Mockito.verify(calendarSqlSpy, Mockito.times(1)).setExternalConfirmation(objectId, folderId, mail, confirm, confirmMessage);
        Assert.assertEquals(returnDate, setExternalConfirmation.getLastModified());
    }

    @Test
    public void testSetUserConfirmation_occurrenceZero_callSetForSingleAppointment() throws OXException {

        CalendarSql calendarSqlSpy = Mockito.spy(calendarSql);
        Mockito.doReturn(returnDate).when(calendarSqlSpy).setUserConfirmation(Matchers.anyInt(), Matchers.anyInt(), Matchers.anyInt(), Matchers.anyInt(), Matchers.anyString());

        CalendarDataObject setUserConfirmation = calendarSqlSpy.setUserConfirmation(objectId, folderId, 0, userId, confirm, confirmMessage);

        Mockito.verify(calendarSqlSpy, Mockito.times(1)).setUserConfirmation(objectId, folderId, userId, confirm, confirmMessage);
        Assert.assertEquals(returnDate, setUserConfirmation.getLastModified());
    }

}
