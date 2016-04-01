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

package com.openexchange.calendar;

import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
import com.openexchange.calendar.api.CalendarCollection;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.RecurringResultInterface;
import com.openexchange.groupware.calendar.RecurringResultsInterface;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.Participants;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.session.Session;
import com.openexchange.test.mock.MockUtils;

/**
 * Unit tests for {@link CalendarSql}
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.0
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ CalendarCollection.class })
public class CalendarSqlTest {

    @InjectMocks
    private CalendarSql calendarSql;

    @Mock
    private Session session;

    @Mock
    private CalendarCollection calendarCollection;

    private int objectId = 111;

    private int exceptionObjectId = 1111;

    private int folderId = 222;

    private String mail = "martin.schneider@open-xchange.com";

    private int occurrence = 333;

    private int userId = 444;

    private int confirm = Appointment.DECLINE;

    private String confirmMessage = "not yet";

    private Date returnDate = new Date(951782400000L);

    @Before
    public void setUp() throws OXException {
        MockitoAnnotations.initMocks(this);

        final List<CalendarDataObject> retval = new ArrayList<CalendarDataObject>();
        CalendarDataObject[] emptyArray = retval.toArray(new CalendarDataObject[retval.size()]);
        Mockito.when(calendarCollection.getChangeExceptionsByRecurrence(Matchers.anyInt(), Matchers.any(int[].class), (Session) Matchers.any())).thenReturn(emptyArray);
    }

    @Test
    public void testSetExternalConfirmation_occurrenceZero_callSetForSingleAppointment() throws OXException {

        CalendarSql calendarSqlSpy = Mockito.spy(calendarSql);
        Mockito.doReturn(returnDate).when(calendarSqlSpy).setExternalConfirmation(Matchers.anyInt(), Matchers.anyInt(), Matchers.anyString(), Matchers.anyInt(), Matchers.anyString());

        CalendarDataObject setExternalConfirmation = calendarSqlSpy.setExternalConfirmation(objectId, folderId, 0, mail, confirm, confirmMessage);

        Mockito.verify(calendarSqlSpy, Mockito.times(1)).setExternalConfirmation(objectId, folderId, mail, confirm, confirmMessage);
        Assert.assertEquals(returnDate, setExternalConfirmation.getLastModified());
    }

    @Test(expected = OXException.class)
    public void testSetExternalConfirmation_mailNotValid_throwException() throws OXException {

        CalendarSql calendarSqlSpy = Mockito.spy(calendarSql);

        calendarSqlSpy.setExternalConfirmation(objectId, folderId, occurrence, "", confirm, confirmMessage);
    }

    @Test(expected = OXException.class)
    public void testSetExternalConfirmation_mailNotExistend_throwException() throws OXException {

        CalendarSql calendarSqlSpy = Mockito.spy(calendarSql);

        calendarSqlSpy.setExternalConfirmation(objectId, folderId, occurrence, null, confirm, confirmMessage);
    }

    @Test
    public void testSetUserConfirmation_occurrenceZero_callSetForSingleAppointment() throws OXException {

        CalendarSql calendarSqlSpy = Mockito.spy(calendarSql);
        Mockito.doReturn(returnDate).when(calendarSqlSpy).setUserConfirmation(Matchers.anyInt(), Matchers.anyInt(), Matchers.anyInt(), Matchers.anyInt(), Matchers.anyString());

        CalendarDataObject setUserConfirmation = calendarSqlSpy.setUserConfirmation(objectId, folderId, 0, userId, confirm, confirmMessage);

        Mockito.verify(calendarSqlSpy, Mockito.times(1)).setUserConfirmation(objectId, folderId, userId, confirm, confirmMessage);
        Assert.assertEquals(returnDate, setUserConfirmation.getLastModified());
    }

    @Test
    public void testSetExternalConfirmation_recurringResultNull_updateSingleAppointment() throws OXException, SQLException {
        MockUtils.injectValueIntoPrivateField(calendarSql, "calendarCollection", this.calendarCollection);
        CalendarSql calendarSqlSpy = Mockito.spy(calendarSql);

        Mockito.when(calendarCollection.calculateRecurringIgnoringExceptions((CalendarObject) Matchers.any(), Matchers.anyLong(), Matchers.anyLong(), Matchers.anyInt())).thenReturn(null);
        Mockito.doReturn(returnDate).when(calendarSqlSpy).setExternalConfirmation(Matchers.anyInt(), Matchers.anyInt(), Matchers.anyString(), Matchers.anyInt(), Matchers.anyString());

        Mockito.doNothing().when(calendarSqlSpy).validateConfirmMessage(Matchers.anyString());
        CalendarDataObject cdo = new CalendarDataObject();
        Mockito.doReturn(cdo).when(calendarSqlSpy).getObjectById(Matchers.anyInt());

        CalendarDataObject setExternalConfirmation = calendarSqlSpy.setExternalConfirmation(objectId, folderId, occurrence, mail, confirm, confirmMessage);

        Mockito.verify(calendarSqlSpy, Mockito.times(1)).validateConfirmMessage(confirmMessage);
        Mockito.verify(calendarSqlSpy, Mockito.times(1)).setExternalConfirmation(objectId, folderId, mail, confirm, confirmMessage);
        Assert.assertEquals(returnDate, setExternalConfirmation.getLastModified());
    }

    @Test
    public void testSetUserConfirmation_recurringResultNull_updateSingleAppointment() throws OXException, SQLException {
        MockUtils.injectValueIntoPrivateField(calendarSql, "calendarCollection", this.calendarCollection);
        CalendarSql calendarSqlSpy = Mockito.spy(calendarSql);

        Mockito.when(calendarCollection.calculateRecurringIgnoringExceptions((CalendarObject) Matchers.any(), Matchers.anyLong(), Matchers.anyLong(), Matchers.anyInt())).thenReturn(null);
        Mockito.doReturn(returnDate).when(calendarSqlSpy).setUserConfirmation(Matchers.anyInt(), Matchers.anyInt(), Matchers.anyInt(), Matchers.anyInt(), Matchers.anyString());

        Mockito.doNothing().when(calendarSqlSpy).validateConfirmMessage(Matchers.anyString());
        CalendarDataObject cdo = new CalendarDataObject();
        Mockito.doReturn(cdo).when(calendarSqlSpy).getObjectById(Matchers.anyInt());

        CalendarDataObject setUserConfirmation = calendarSqlSpy.setUserConfirmation(objectId, folderId, occurrence, userId, confirm, confirmMessage);

        Mockito.verify(calendarSqlSpy, Mockito.times(1)).validateConfirmMessage(confirmMessage);
        Mockito.verify(calendarSqlSpy, Mockito.times(1)).setUserConfirmation(objectId, folderId, userId, confirm, confirmMessage);
        Assert.assertEquals(returnDate, setUserConfirmation.getLastModified());
    }

    @Test
    public void testSetExternalConfirmation_exceptionAvailable_updateException() throws OXException, SQLException {
        MockUtils.injectValueIntoPrivateField(calendarSql, "calendarCollection", this.calendarCollection);
        CalendarSql calendarSqlSpy = Mockito.spy(calendarSql);

        final List<CalendarDataObject> retval = new ArrayList<CalendarDataObject>();
        CalendarDataObject cdo = new CalendarDataObject();
        cdo.setRecurrencePosition(occurrence);
        cdo.setObjectID(exceptionObjectId);
        retval.add(cdo);
        Mockito.when(calendarCollection.getChangeExceptionsByRecurrence(Matchers.anyInt(), Matchers.any(int[].class), (Session) Matchers.any())).thenReturn(retval.toArray(new CalendarDataObject[retval.size()]));
        Mockito.doNothing().when(calendarSqlSpy).validateConfirmMessage(Matchers.anyString());
        Mockito.doReturn(new CalendarDataObject()).when(calendarSqlSpy).getObjectById(Matchers.anyInt());
        Mockito.doReturn(returnDate).when(calendarSqlSpy).setExternalConfirmation(Matchers.anyInt(), Matchers.anyInt(), Matchers.anyString(), Matchers.anyInt(), Matchers.anyString());

        CalendarDataObject setExternalConfirmation = calendarSqlSpy.setExternalConfirmation(objectId, folderId, occurrence, mail, confirm, confirmMessage);

        Mockito.verify(calendarSqlSpy, Mockito.times(1)).validateConfirmMessage(confirmMessage);
        Mockito.verify(calendarSqlSpy, Mockito.times(1)).setExternalConfirmation(exceptionObjectId, folderId, mail, confirm, confirmMessage);
        Assert.assertEquals(returnDate, setExternalConfirmation.getLastModified());
    }

    @Test
    public void testSetUserConfirmation_exceptionAvailable_updateException() throws OXException, SQLException {
        MockUtils.injectValueIntoPrivateField(calendarSql, "calendarCollection", this.calendarCollection);
        CalendarSql calendarSqlSpy = Mockito.spy(calendarSql);

        final List<CalendarDataObject> retval = new ArrayList<CalendarDataObject>();
        CalendarDataObject cdo = new CalendarDataObject();
        cdo.setRecurrencePosition(occurrence);
        cdo.setObjectID(exceptionObjectId);
        retval.add(cdo);
        Mockito.when(calendarCollection.getChangeExceptionsByRecurrence(Matchers.anyInt(), Matchers.any(int[].class), (Session) Matchers.any())).thenReturn(retval.toArray(new CalendarDataObject[retval.size()]));
        Mockito.doNothing().when(calendarSqlSpy).validateConfirmMessage(Matchers.anyString());
        Mockito.doReturn(new CalendarDataObject()).when(calendarSqlSpy).getObjectById(Matchers.anyInt());
        Mockito.doReturn(returnDate).when(calendarSqlSpy).setUserConfirmation(Matchers.anyInt(), Matchers.anyInt(), Matchers.anyInt(), Matchers.anyInt(), Matchers.anyString());

        CalendarDataObject setUserConfirmation = calendarSqlSpy.setUserConfirmation(objectId, folderId, occurrence, userId, confirm, confirmMessage);

        Mockito.verify(calendarSqlSpy, Mockito.times(1)).validateConfirmMessage(confirmMessage);
        Mockito.verify(calendarSqlSpy, Mockito.times(1)).setUserConfirmation(exceptionObjectId, folderId, userId, confirm, confirmMessage);
        Assert.assertEquals(returnDate, setUserConfirmation.getLastModified());
    }

    @Test
    public void testSetExternalConfirmation_recurringResultNullAndExceptionFound_updateException() throws OXException, SQLException {
        MockUtils.injectValueIntoPrivateField(calendarSql, "calendarCollection", this.calendarCollection);
        CalendarSql calendarSqlSpy = Mockito.spy(calendarSql);

        final List<CalendarDataObject> retval = new ArrayList<CalendarDataObject>();
        Mockito.when(calendarCollection.getChangeExceptionsByRecurrence(Matchers.anyInt(), Matchers.any(int[].class), (Session) Matchers.any())).thenReturn(retval.toArray(new CalendarDataObject[retval.size()]));

        RecurringResultsInterface recurringResultsInterface = Mockito.mock(RecurringResultsInterface.class);
        Mockito.when(recurringResultsInterface.getRecurringResult(0)).thenReturn(null);
        Mockito.when(calendarCollection.calculateRecurringIgnoringExceptions((CalendarObject) Matchers.any(), Matchers.anyLong(), Matchers.anyLong(), Matchers.anyInt())).thenReturn(recurringResultsInterface);

        Mockito.doNothing().when(calendarSqlSpy).validateConfirmMessage(Matchers.anyString());
        Mockito.doReturn(new CalendarDataObject()).when(calendarSqlSpy).getObjectById(Matchers.anyInt());
        Mockito.doReturn(returnDate).when(calendarSqlSpy).setExternalConfirmation(Matchers.anyInt(), Matchers.anyInt(), Matchers.anyString(), Matchers.anyInt(), Matchers.anyString());
        Mockito.doReturn(exceptionObjectId).when(calendarSqlSpy).getExceptionObjectIdForRecurrence((CalendarDataObject) Matchers.any(), Matchers.anyInt(), Matchers.anyInt());

        CalendarDataObject setExternalConfirmation = calendarSqlSpy.setExternalConfirmation(objectId, folderId, occurrence, mail, confirm, confirmMessage);

        Mockito.verify(calendarSqlSpy, Mockito.times(1)).validateConfirmMessage(confirmMessage);
        Mockito.verify(calendarSqlSpy, Mockito.times(1)).setExternalConfirmation(exceptionObjectId, folderId, mail, confirm, confirmMessage);
        Assert.assertEquals(returnDate, setExternalConfirmation.getLastModified());
    }

    @Test
    public void testSetExternalConfirmation_recurringResultNullAndExceptionNotFound_doNothing() throws OXException, SQLException {
        MockUtils.injectValueIntoPrivateField(calendarSql, "calendarCollection", this.calendarCollection);
        CalendarSql calendarSqlSpy = Mockito.spy(calendarSql);

        final List<CalendarDataObject> retval = new ArrayList<CalendarDataObject>();
        Mockito.when(calendarCollection.getChangeExceptionsByRecurrence(Matchers.anyInt(), Matchers.any(int[].class), (Session) Matchers.any())).thenReturn(retval.toArray(new CalendarDataObject[retval.size()]));

        RecurringResultsInterface recurringResultsInterface = Mockito.mock(RecurringResultsInterface.class);
        Mockito.when(recurringResultsInterface.getRecurringResult(0)).thenReturn(null);

        Mockito.when(calendarCollection.calculateRecurringIgnoringExceptions((CalendarObject) Matchers.any(), Matchers.anyLong(), Matchers.anyLong(), Matchers.anyInt())).thenReturn(recurringResultsInterface);
        Mockito.doNothing().when(calendarSqlSpy).validateConfirmMessage(Matchers.anyString());
        Mockito.doReturn(new CalendarDataObject()).when(calendarSqlSpy).getObjectById(Matchers.anyInt());
        Mockito.doReturn(returnDate).when(calendarSqlSpy).setExternalConfirmation(Matchers.anyInt(), Matchers.anyInt(), Matchers.anyString(), Matchers.anyInt(), Matchers.anyString());
        Mockito.doReturn(CalendarSql.EXCEPTION_NOT_FOUND).when(calendarSqlSpy).getExceptionObjectIdForRecurrence((CalendarDataObject) Matchers.any(), Matchers.anyInt(), Matchers.anyInt());

        calendarSqlSpy.setExternalConfirmation(objectId, folderId, occurrence, mail, confirm, confirmMessage);

        Mockito.verify(calendarSqlSpy, Mockito.times(1)).validateConfirmMessage(confirmMessage);
        Mockito.verify(calendarSqlSpy, Mockito.times(0)).setExternalConfirmation(exceptionObjectId, folderId, mail, confirm, confirmMessage);
    }

    @Test
    public void testSetUserConfirmation_recurringResultNullAndExceptionFound_updateException() throws OXException, SQLException {
        MockUtils.injectValueIntoPrivateField(calendarSql, "calendarCollection", this.calendarCollection);
        CalendarSql calendarSqlSpy = Mockito.spy(calendarSql);

        final List<CalendarDataObject> retval = new ArrayList<CalendarDataObject>();
        Mockito.when(calendarCollection.getChangeExceptionsByRecurrence(Matchers.anyInt(), Matchers.any(int[].class), (Session) Matchers.any())).thenReturn(retval.toArray(new CalendarDataObject[retval.size()]));

        RecurringResultsInterface recurringResultsInterface = Mockito.mock(RecurringResultsInterface.class);
        Mockito.when(recurringResultsInterface.getRecurringResult(0)).thenReturn(null);

        Mockito.when(calendarCollection.calculateRecurringIgnoringExceptions((CalendarObject) Matchers.any(), Matchers.anyLong(), Matchers.anyLong(), Matchers.anyInt())).thenReturn(recurringResultsInterface);
        Mockito.doNothing().when(calendarSqlSpy).validateConfirmMessage(Matchers.anyString());
        Mockito.doReturn(new CalendarDataObject()).when(calendarSqlSpy).getObjectById(Matchers.anyInt());
        Mockito.doReturn(returnDate).when(calendarSqlSpy).setUserConfirmation(Matchers.anyInt(), Matchers.anyInt(), Matchers.anyInt(), Matchers.anyInt(), Matchers.anyString());
        Mockito.doReturn(exceptionObjectId).when(calendarSqlSpy).getExceptionObjectIdForRecurrence((CalendarDataObject) Matchers.any(), Matchers.anyInt(), Matchers.anyInt());

        CalendarDataObject setUserConfirmation = calendarSqlSpy.setUserConfirmation(objectId, folderId, occurrence, userId, confirm, confirmMessage);

        Mockito.verify(calendarSqlSpy, Mockito.times(1)).validateConfirmMessage(confirmMessage);
        Mockito.verify(calendarSqlSpy, Mockito.times(1)).setUserConfirmation(exceptionObjectId, folderId, userId, confirm, confirmMessage);
        Assert.assertEquals(returnDate, setUserConfirmation.getLastModified());
    }

    @Test
    public void testSetUserConfirmation_recurringResultNullAndExceptionNotFound_doNothing() throws OXException, SQLException {
        MockUtils.injectValueIntoPrivateField(calendarSql, "calendarCollection", this.calendarCollection);
        CalendarSql calendarSqlSpy = Mockito.spy(calendarSql);

        final List<CalendarDataObject> retval = new ArrayList<CalendarDataObject>();
        Mockito.when(calendarCollection.getChangeExceptionsByRecurrence(Matchers.anyInt(), Matchers.any(int[].class), (Session) Matchers.any())).thenReturn(retval.toArray(new CalendarDataObject[retval.size()]));

        RecurringResultsInterface recurringResultsInterface = Mockito.mock(RecurringResultsInterface.class);
        Mockito.when(recurringResultsInterface.getRecurringResult(0)).thenReturn(null);

        Mockito.when(calendarCollection.calculateRecurringIgnoringExceptions((CalendarObject) Matchers.any(), Matchers.anyLong(), Matchers.anyLong(), Matchers.anyInt())).thenReturn(recurringResultsInterface);
        Mockito.doNothing().when(calendarSqlSpy).validateConfirmMessage(Matchers.anyString());
        Mockito.doReturn(new CalendarDataObject()).when(calendarSqlSpy).getObjectById(Matchers.anyInt());
        Mockito.doReturn(returnDate).when(calendarSqlSpy).setUserConfirmation(Matchers.anyInt(), Matchers.anyInt(), Matchers.anyInt(), Matchers.anyInt(), Matchers.anyString());
        Mockito.doReturn(CalendarSql.EXCEPTION_NOT_FOUND).when(calendarSqlSpy).getExceptionObjectIdForRecurrence((CalendarDataObject) Matchers.any(), Matchers.anyInt(), Matchers.anyInt());

        calendarSqlSpy.setUserConfirmation(objectId, folderId, occurrence, userId, confirm, confirmMessage);

        Mockito.verify(calendarSqlSpy, Mockito.times(1)).validateConfirmMessage(confirmMessage);
        Mockito.verify(calendarSqlSpy, Mockito.times(0)).setUserConfirmation(exceptionObjectId, folderId, userId, confirm, confirmMessage);
    }

    @Test
    public void testSetExternalConfirmation_newOccurrenceConfirmation_createException() throws OXException, SQLException, CloneNotSupportedException {
        MockUtils.injectValueIntoPrivateField(calendarSql, "calendarCollection", this.calendarCollection);
        CalendarSql calendarSqlSpy = Mockito.spy(calendarSql);
        Mockito.doNothing().when(calendarSqlSpy).validateConfirmMessage(Matchers.anyString());

        CalendarDataObject cdao = Mockito.mock(CalendarDataObject.class);
        final Participants participants = new Participants();

        final ExternalUserParticipant p1 = new ExternalUserParticipant(mail);
        participants.add(p1);
        final Participant p2 = new UserParticipant(userId);
        participants.add(p2);
        Mockito.when(cdao.getParticipants()).thenReturn(participants.getList());
        Mockito.when(cdao.clone()).thenReturn(new CalendarDataObject());
        Mockito.doReturn(cdao).when(calendarSqlSpy).getObjectById(Matchers.anyInt());

        final List<CalendarDataObject> retval = new ArrayList<CalendarDataObject>();
        Mockito.when(calendarCollection.getChangeExceptionsByRecurrence(Matchers.anyInt(), Matchers.any(int[].class), (Session) Matchers.any())).thenReturn(retval.toArray(new CalendarDataObject[retval.size()]));
        RecurringResultsInterface recurringResultsInterface = Mockito.mock(RecurringResultsInterface.class);
        Mockito.when(calendarCollection.calculateRecurringIgnoringExceptions((CalendarObject) Matchers.any(), Matchers.anyLong(), Matchers.anyLong(), Matchers.anyInt())).thenReturn(recurringResultsInterface);
        RecurringResultInterface recurringResultInterface = Mockito.mock(RecurringResultInterface.class);
        Mockito.when(recurringResultInterface.getEnd()).thenReturn(Date.UTC(2013, 12, 12, 0, 0, 0));
        Mockito.when(recurringResultInterface.getPosition()).thenReturn(occurrence);
        Mockito.when(recurringResultInterface.getStart()).thenReturn(Date.UTC(2013, 12, 11, 0, 0, 0));
        Mockito.when(recurringResultsInterface.getRecurringResult(0)).thenReturn(recurringResultInterface);

        Mockito.doReturn(null).when(calendarSqlSpy).updateAppointmentObject((CalendarDataObject) Matchers.any(), Matchers.anyInt(), (Date) Matchers.any());
        Mockito.doReturn(returnDate).when(calendarSqlSpy).setExternalConfirmation(Matchers.anyInt(), Matchers.anyInt(), Matchers.anyString(), Matchers.anyInt(), Matchers.anyString());

        CalendarDataObject setExternalConfirmation = calendarSqlSpy.setExternalConfirmation(objectId, folderId, occurrence, mail, confirm, confirmMessage);

        Participant[] participants2 = setExternalConfirmation.getParticipants();
        for (Participant participant : participants2) {
            if (participant instanceof UserParticipant) {
                UserParticipant userParticipant = (UserParticipant) participant;
                Assert.assertEquals(0, userParticipant.getConfirm());
            }
            if (participant instanceof ExternalUserParticipant) {
                ExternalUserParticipant externalUserParticipant = (ExternalUserParticipant) participant;
                Assert.assertEquals(confirm, externalUserParticipant.getConfirm());
                Assert.assertEquals(confirmMessage, externalUserParticipant.getMessage());
            }
        }
        Mockito.verify(calendarSqlSpy, Mockito.times(1)).validateConfirmMessage(confirmMessage);
        Mockito.verify(calendarSqlSpy, Mockito.times(1)).setExternalConfirmation(Matchers.anyInt(), Matchers.anyInt(), Matchers.anyString(), Matchers.anyInt(), Matchers.anyString());
    }

    @Test
    public void testSetUserConfirmation_newOccurrenceConfirmation_createException() throws OXException, SQLException, CloneNotSupportedException {
        MockUtils.injectValueIntoPrivateField(calendarSql, "calendarCollection", this.calendarCollection);
        CalendarSql calendarSqlSpy = Mockito.spy(calendarSql);
        Mockito.doNothing().when(calendarSqlSpy).validateConfirmMessage(Matchers.anyString());

        CalendarDataObject cdao = Mockito.mock(CalendarDataObject.class);
        final Participants participants = new Participants();

        final Participant p1 = new UserParticipant(userId);
        participants.add(p1);
        final Participant p2 = new UserParticipant(7777);
        participants.add(p2);
        Mockito.when(cdao.getUsers()).thenReturn(participants.getUsers());
        Mockito.when(cdao.clone()).thenReturn(new CalendarDataObject());
        Mockito.doReturn(cdao).when(calendarSqlSpy).getObjectById(Matchers.anyInt());

        final List<CalendarDataObject> retval = new ArrayList<CalendarDataObject>();
        Mockito.when(calendarCollection.getChangeExceptionsByRecurrence(Matchers.anyInt(), Matchers.any(int[].class), (Session) Matchers.any())).thenReturn(retval.toArray(new CalendarDataObject[retval.size()]));
        RecurringResultsInterface recurringResultsInterface = Mockito.mock(RecurringResultsInterface.class);
        Mockito.when(calendarCollection.calculateRecurringIgnoringExceptions((CalendarObject) Matchers.any(), Matchers.anyLong(), Matchers.anyLong(), Matchers.anyInt())).thenReturn(recurringResultsInterface);
        RecurringResultInterface recurringResultInterface = Mockito.mock(RecurringResultInterface.class);
        Mockito.when(recurringResultInterface.getEnd()).thenReturn(Date.UTC(2013, 12, 12, 0, 0, 0));
        Mockito.when(recurringResultInterface.getPosition()).thenReturn(occurrence);
        Mockito.when(recurringResultInterface.getStart()).thenReturn(Date.UTC(2013, 12, 11, 0, 0, 0));
        Mockito.when(recurringResultsInterface.getRecurringResult(0)).thenReturn(recurringResultInterface);

        Mockito.doReturn(null).when(calendarSqlSpy).updateAppointmentObject((CalendarDataObject) Matchers.any(), Matchers.anyInt(), (Date) Matchers.any());
        Mockito.doReturn(returnDate).when(calendarSqlSpy).setExternalConfirmation(Matchers.anyInt(), Matchers.anyInt(), Matchers.anyString(), Matchers.anyInt(), Matchers.anyString());

        CalendarDataObject setUserConfirmation = calendarSqlSpy.setUserConfirmation(objectId, folderId, occurrence, userId, confirm, confirmMessage);

        Participant[] participants2 = setUserConfirmation.getUsers();
        for (Participant participant : participants2) {
            if (participant instanceof UserParticipant) {
                UserParticipant userParticipant = (UserParticipant) participant;
                Assert.assertEquals(confirm, userParticipant.getConfirm());
                Assert.assertEquals(confirmMessage, userParticipant.getConfirmMessage());
            }
            if (participant instanceof ExternalUserParticipant) {
                ExternalUserParticipant externalUserParticipant = (ExternalUserParticipant) participant;
                Assert.assertEquals(0, externalUserParticipant.getConfirm());
            }
        }
        Mockito.verify(calendarSqlSpy, Mockito.times(1)).validateConfirmMessage(confirmMessage);
        Mockito.verify(calendarSqlSpy, Mockito.times(1)).updateAppointmentObject((CalendarDataObject) Matchers.any(), Matchers.anyInt(), (Date) Matchers.any());
    }

}
