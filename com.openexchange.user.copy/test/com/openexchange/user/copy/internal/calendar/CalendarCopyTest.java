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

package com.openexchange.user.copy.internal.calendar;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.user.copy.internal.AbstractUserCopyTest;
import com.openexchange.user.copy.internal.IntegerMapping;



/**
 * {@link CalendarCopyTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class CalendarCopyTest extends AbstractUserCopyTest {
    
    private int srcUsrId;
    
    private int srcCtxId;
    
    private int dstCtxId;

    private Connection srcCon;

    private Connection dstCon;
    
    /**
     * Initializes a new {@link CalendarCopyTest}.
     * @param name
     */
    public CalendarCopyTest(final String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        srcUsrId = getSourceUserId();
        srcCon = getSourceConnection();
        dstCon = getDestinationConnection();
        srcCtxId = getSourceContext().getContextId();
        dstCtxId = getDestinationContext().getContextId(); 
    }
    
    public void testCalendarCopy() throws Exception {
        final CalendarCopyTask copyTask = new CalendarCopyTask();
        final List<Integer> originFolderIds = loadFolderIdsFromDB(srcCon, srcCtxId, srcUsrId);        
        final List<Integer> originAppointmentsIds = copyTask.loadAppointmentIdsFromDB(new HashSet<Integer>(originFolderIds), srcUsrId, srcCtxId, srcCon);
        final Map<Integer, CalendarDataObject> originAppointments = copyTask.loadAppointmentsFromDB(originAppointmentsIds, srcCtxId, srcCon);
        final Map<Integer, ExternalDate> sourceExternalDates = copyTask.loadExternalDatesFromDB(srcCon, srcCtxId, originAppointmentsIds);        
        
        DBUtils.startTransaction(dstCon);
        IntegerMapping mapping = null;
        try {
            mapping = copyTask.copyUser(getObjectMappingWithFolders());
        } catch (final OXException e) {
            DBUtils.rollback(dstCon);
            e.printStackTrace();
            fail("A UserCopyException occurred.");
        }        
        dstCon.commit();
        
        final List<Integer> targetAppointmentIds = new ArrayList<Integer>();
        for (final Integer origin : mapping.getSourceKeys()) {
            final Integer destination = mapping.getDestination(origin);
            targetAppointmentIds.add(destination);
        }
        
        final Map<Integer, CalendarDataObject> targetAppointments = copyTask.loadAppointmentsFromDB(targetAppointmentIds, dstCtxId, dstCon);
        checkAppointments(originAppointments, targetAppointments, mapping);
        
        final Map<Integer, ExternalDate> targetExternalDates = copyTask.loadExternalDatesFromDB(dstCon, dstCtxId, targetAppointmentIds);
        compareExternalDates(sourceExternalDates, targetExternalDates);
    }
    
    private void compareExternalDates(final Map<Integer, ExternalDate> sourceExternalDates, final Map<Integer, ExternalDate> targetExternalDates) {
        checkAndGetMatchingObjects(sourceExternalDates.values(), targetExternalDates.values());        
    }
    
    private void checkAppointments(final Map<Integer, CalendarDataObject> originAppointments, final Map<Integer, CalendarDataObject> targetAppointments, final IntegerMapping mapping) {
        final Collection<CalendarDataObject> originValues = originAppointments.values();
        final Collection<CalendarDataObject> targetValues = targetAppointments.values();
        for (final CalendarDataObject originAppointment : originValues) {
            final int originId = originAppointment.getObjectID();
            final Integer targetId = mapping.getDestination(originId);
            if (targetId == null) {
                final int originMasterId = originAppointment.getRecurrenceID();
                if (originMasterId == -1 || originMasterId == originId || mapping.getSourceKeys().contains(originMasterId)) {
                    fail("Mapping did not contain appointment.");
                }
            } else {
                findAndCompareAppointments(targetValues, targetId, originAppointment, mapping);
            }            
        }
    }
    
    private void findAndCompareAppointments(final Collection<CalendarDataObject> targetAppointments, final int targetId, final CalendarDataObject originAppointment, final IntegerMapping mapping) {
        CalendarDataObject targetAppointment = null;
        for (final CalendarDataObject appointment : targetAppointments) {
            if (appointment.getObjectID() == targetId) {
                targetAppointment = appointment;
                break;
            }
        }
        
        if (targetAppointment == null) {
            fail("Did not find target for source " + originAppointment.getObjectID());
        }
        
        if (originAppointment.getRecurrenceID() != -1) {
            checkRecurrenceRelations(originAppointment, targetAppointment, mapping);
        }        
        compareAppointments(originAppointment, targetAppointment);
        if (originAppointment.getParticipants() != null) {
            checkAndGetMatchingObjects(Arrays.asList(originAppointment.getParticipants()), Arrays.asList(targetAppointment.getParticipants()), new ParticipantComparator());
        }
    }
   
    private void checkRecurrenceRelations(final CalendarDataObject origin, final CalendarDataObject target, final IntegerMapping mapping) {
        final int originMaster = origin.getRecurrenceID();
        final int targetMaster = target.getRecurrenceID();
        
        final Integer toCompare = mapping.getDestination(originMaster);
        if (targetMaster != toCompare) {
            fail("Recurrence master relation is wrong.");
        }
    }
    
    private void compareAppointments(final CalendarDataObject origin, final CalendarDataObject target) {
        assertEquals(origin.getPrivateFlag(), target.getPrivateFlag());
        assertEquals(origin.getStartDate(), target.getStartDate());
        assertEquals(origin.getEndDate(), target.getEndDate());
        assertEquals(origin.getTimezone(), target.getTimezone());
        assertEquals(origin.getLabel(), target.getLabel());
        assertEquals(origin.getDays(), target.getDays());
        assertEquals(origin.getRecurrencePosition(), target.getRecurrencePosition());
        assertEquals(origin.getShownAs(), target.getShownAs());
        assertEquals(origin.getFullTime(), target.getFullTime());
        assertEquals(origin.getNumberOfAttachments(), target.getNumberOfAttachments());
        assertEquals(origin.getTitle(), target.getTitle());
        assertEquals(origin.getLocation(), target.getLocation());
        assertEquals(origin.getNote(), target.getNote());
        assertEquals(origin.getRecurrence(), target.getRecurrence());
        assertEquals(origin.getDelExceptions(), target.getDelExceptions());
        assertEquals(origin.getExceptions(), target.getExceptions());
        assertEquals(origin.getCategories(), target.getCategories());
        assertEquals(origin.getUid(), target.getUid());
        assertEquals(origin.getOrganizer(), target.getOrganizer());
        assertEquals(origin.getSequence(), target.getSequence());
    }
    
    @Override
    protected void tearDown() throws Exception {
        DBUtils.autocommit(dstCon);
        deleteAllFromTablesForCid(dstCtxId, "cid", dstCon, "prg_dates", "prg_dates_members", "prg_date_rights", "dateExternal");
        super.tearDown();
    }

    /**
     * @see com.openexchange.user.copy.internal.AbstractUserCopyTest#getSequenceTables()
     */
    @Override
    protected String[] getSequenceTables() {
        return new String[] {"sequence_calendar"};
    }
    
    private static final class ParticipantComparator implements Comparator<Participant> {
        
        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(final Participant o1, final Participant o2) {
            boolean isEqual = false;
            if (o1 instanceof UserParticipant) {
                if (o2 instanceof UserParticipant) {
                    final UserParticipant user1 = (UserParticipant) o1;
                    final UserParticipant user2 = (UserParticipant) o2;
                    
                    isEqual = checkNullOrEquals(user1.getEmailAddress(), user2.getEmailAddress()) &&
                    checkNullOrEquals(user1.getAlarmDate(), user2.getAlarmDate()) &&
                    checkNullOrEquals(user1.getAlarmMinutes(), user2.getAlarmMinutes()) &&
                    checkNullOrEquals(user1.getConfirm(), user2.getConfirm()) &&
                    checkNullOrEquals(user1.getConfirmMessage(), user2.getConfirmMessage()) &&
                    checkNullOrEquals(user1.getDisplayName(), user2.getDisplayName());
                }
            } else {
                if (o2 instanceof ExternalUserParticipant) {
                    final ExternalUserParticipant user1 = (ExternalUserParticipant) o1;
                    final ExternalUserParticipant user2 = (ExternalUserParticipant) o2;
                    isEqual = checkNullOrEquals(user1.getEmailAddress(), user2.getEmailAddress()) &&
                    checkNullOrEquals(user1.getDisplayName(), user2.getDisplayName());
                }
            }
            
            return isEqual ? 0 : -1;
        }
        
    }
}
