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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.groupware.importexport.importers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import com.openexchange.api2.OXException;
import com.openexchange.database.DBPoolingException;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.CalendarFolderObject;
import com.openexchange.groupware.calendar.MBoolean;
import com.openexchange.groupware.calendar.OXCalendarException;
import com.openexchange.groupware.calendar.RecurringResultInterface;
import com.openexchange.groupware.calendar.RecurringResultsInterface;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIteratorException;


/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class MockCalendarCollectionService implements CalendarCollectionService{

    public Date[] addException(Date[] dates, Date d) {
        return null;
    }
    public long addYears(long base, int years) {
        return 0;
    }

    public RecurringResultsInterface calculateFirstRecurring(CalendarObject cdao) throws OXException {
        return null;
    }

    public RecurringResultsInterface calculateRecurring(CalendarObject cdao, long range_start, long range_end, int pos) throws OXException {
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#calculateRecurring(com.openexchange.groupware.container.CalendarObject, long, long, int, int, boolean)
     */
    public RecurringResultsInterface calculateRecurring(CalendarObject cdao, long range_start, long range_end, int pos, int PMAXTC, boolean ignore_exceptions) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#calculateRecurring(com.openexchange.groupware.container.CalendarObject, long, long, int, int, boolean, boolean)
     */
    public RecurringResultsInterface calculateRecurring(CalendarObject cdao, long range_start, long range_end, int pos, int PMAXTC, boolean ignore_exceptions, boolean calc_until) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#calculateRecurringDate(long, long, int)
     */
    public Date calculateRecurringDate(long date, long time, int timeZoneOffsetDiff) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#calculateRecurringIgnoringExceptions(com.openexchange.groupware.container.CalendarObject, long, long, int)
     */
    public RecurringResultsInterface calculateRecurringIgnoringExceptions(CalendarObject cdao, long range_start, long range_end, int pos) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#check(java.lang.Object, java.lang.Object)
     */
    public boolean check(Object a, Object b) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#checkAndAlterCols(int[])
     */
    public int[] checkAndAlterCols(int[] cols) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#checkAndConfirmIfUserUserIsParticipantInPublicFolder(com.openexchange.groupware.calendar.CalendarDataObject, com.openexchange.groupware.container.UserParticipant)
     */
    public void checkAndConfirmIfUserUserIsParticipantInPublicFolder(CalendarDataObject cdao, UserParticipant up) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#checkAndFillIfUserIsParticipant(com.openexchange.groupware.calendar.CalendarDataObject, com.openexchange.groupware.container.UserParticipant)
     */
    public void checkAndFillIfUserIsParticipant(CalendarDataObject cdao, UserParticipant up) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#checkAndFillIfUserIsUser(com.openexchange.groupware.calendar.CalendarDataObject, com.openexchange.groupware.container.Participant)
     */
    public void checkAndFillIfUserIsUser(CalendarDataObject cdao, Participant p) throws OXException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#checkAndModifyAlarm(com.openexchange.groupware.calendar.CalendarDataObject, com.openexchange.groupware.container.UserParticipant[], int, com.openexchange.groupware.container.UserParticipant[])
     */
    public UserParticipant[] checkAndModifyAlarm(CalendarDataObject cdao, UserParticipant[] check, int uid, UserParticipant[] orig) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#checkAndRemovePastReminders(com.openexchange.groupware.calendar.CalendarDataObject, com.openexchange.groupware.calendar.CalendarDataObject)
     */
    public void checkAndRemovePastReminders(CalendarDataObject cdao, CalendarDataObject edao) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#checkForInvalidCharacters(com.openexchange.groupware.calendar.CalendarDataObject)
     */
    public void checkForInvalidCharacters(CalendarDataObject cdao) throws OXException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#checkForSoloReminderUpdate(com.openexchange.groupware.calendar.CalendarDataObject, int[], com.openexchange.groupware.calendar.MBoolean)
     */
    public boolean checkForSoloReminderUpdate(CalendarDataObject cdao, int[] ucols, MBoolean cup) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#checkIfArrayKeyExistInArray(java.lang.Object[], java.lang.Object[])
     */
    public boolean checkIfArrayKeyExistInArray(Object[] a, Object[] b) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#checkIfDateOccursInRecurrence(java.util.Date, com.openexchange.groupware.calendar.CalendarDataObject)
     */
    public boolean checkIfDateOccursInRecurrence(Date date, CalendarDataObject recurringAppointment) throws OXException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#checkIfDatesOccurInRecurrence(java.util.Date[], com.openexchange.groupware.calendar.CalendarDataObject)
     */
    public boolean checkIfDatesOccurInRecurrence(Date[] dates, CalendarDataObject recurringAppointment) throws OXException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#checkIfUserIsParticipant(com.openexchange.groupware.calendar.CalendarDataObject, com.openexchange.groupware.container.UserParticipant)
     */
    public boolean checkIfUserIsParticipant(CalendarDataObject cdao, UserParticipant up) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#checkMillisInThePast(long)
     */
    public boolean checkMillisInThePast(long check) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#checkParticipants(com.openexchange.groupware.container.Participant[], com.openexchange.groupware.container.Participant[])
     */
    public boolean checkParticipants(Participant[] newParticipants, Participant[] oldParticipants) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#checkPermissions(com.openexchange.groupware.calendar.CalendarDataObject, com.openexchange.session.Session, com.openexchange.groupware.contexts.Context, java.sql.Connection, int, int)
     */
    public boolean checkPermissions(CalendarDataObject cdao, Session so, Context ctx, Connection readcon, int action, int inFolder) throws OXException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#checkRecurring(com.openexchange.groupware.container.CalendarObject)
     */
    public void checkRecurring(CalendarObject cdao) throws OXException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#checkRecurringCompleteness(com.openexchange.groupware.container.CalendarObject, boolean)
     */
    public void checkRecurringCompleteness(CalendarObject cdao, boolean ignoreUntilAndOccurrence) throws OXCalendarException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#checkUserParticipantObject(com.openexchange.groupware.container.UserParticipant, int)
     */
    public void checkUserParticipantObject(UserParticipant up, int folder_type) throws OXException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#cloneObjectForRecurringException(com.openexchange.groupware.calendar.CalendarDataObject, com.openexchange.groupware.calendar.CalendarDataObject, int)
     */
    public CalendarDataObject cloneObjectForRecurringException(CalendarDataObject cdao, CalendarDataObject edao, int sessionUser) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#closePreparedStatement(java.sql.PreparedStatement)
     */
    public void closePreparedStatement(PreparedStatement prep) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#closeResultSet(java.sql.ResultSet)
     */
    public void closeResultSet(ResultSet rs) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#closeStatement(java.sql.Statement)
     */
    public void closeStatement(Statement stmt) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#convertDates2String(java.util.Date[])
     */
    public String convertDates2String(Date[] d) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#convertString2Dates(java.lang.String)
     */
    public Date[] convertString2Dates(String s) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#createDSString(com.openexchange.groupware.calendar.CalendarDataObject)
     */
    public String createDSString(CalendarDataObject cdao) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#debugActiveDates(long, long, boolean[])
     */
    public void debugActiveDates(long start, long end, boolean[] activeDates) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#debugRecurringResult(com.openexchange.groupware.calendar.RecurringResultInterface)
     */
    public void debugRecurringResult(RecurringResultInterface rr) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#detectFolderMoveAction(com.openexchange.groupware.calendar.CalendarDataObject, com.openexchange.groupware.calendar.CalendarDataObject)
     */
    public void detectFolderMoveAction(CalendarDataObject cdao, CalendarDataObject edao) throws OXException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#detectTimeChange(com.openexchange.groupware.calendar.CalendarDataObject, com.openexchange.groupware.calendar.CalendarDataObject)
     */
    public boolean detectTimeChange(CalendarDataObject cdao, CalendarDataObject edao) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#enhanceCols(int[], int[], int)
     */
    public int[] enhanceCols(int[] cols, int[] ara, int i) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#exceedsHourOfDay(long, java.lang.String)
     */
    public boolean exceedsHourOfDay(long millis, String timeZoneID) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#exceedsHourOfDay(long, java.util.TimeZone)
     */
    public boolean exceedsHourOfDay(long millis, TimeZone zone) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#existsReminder(com.openexchange.groupware.contexts.Context, int, int)
     */
    public boolean existsReminder(Context c, int oid, int uid) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#fillDAO(com.openexchange.groupware.calendar.CalendarDataObject)
     */
    public boolean fillDAO(CalendarDataObject cdao) throws OXException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#fillEventInformation(com.openexchange.groupware.calendar.CalendarDataObject, com.openexchange.groupware.calendar.CalendarDataObject, com.openexchange.groupware.container.UserParticipant[], com.openexchange.groupware.container.UserParticipant[], com.openexchange.groupware.container.UserParticipant[], com.openexchange.groupware.container.Participant[], com.openexchange.groupware.container.Participant[], com.openexchange.groupware.container.Participant[])
     */
    public void fillEventInformation(CalendarDataObject cdao, CalendarDataObject edao, UserParticipant[] up_event, UserParticipant[] new_userparticipants, UserParticipant[] deleted_userparticipants, Participant[] p_event, Participant[] new_participants, Participant[] deleted_participants) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#fillMap(com.openexchange.groupware.calendar.RecurringResultsInterface, long, long, int, int)
     */
    public void fillMap(RecurringResultsInterface rss, long s, long diff, int d, int counter) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#fillObject(com.openexchange.groupware.calendar.CalendarDataObject, com.openexchange.groupware.calendar.CalendarDataObject)
     */
    public CalendarDataObject fillObject(CalendarDataObject source, CalendarDataObject destination) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getAllVisibleAndReadableFolderObject(int, int[], com.openexchange.groupware.contexts.Context, com.openexchange.groupware.userconfiguration.UserConfiguration, java.sql.Connection)
     */
    public CalendarFolderObject getAllVisibleAndReadableFolderObject(int uid, int[] groups, Context c, UserConfiguration uc) throws SQLException, DBPoolingException, SearchIteratorException, OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getAppointmentByID(int, com.openexchange.session.Session)
     */
    public CalendarDataObject getAppointmentByID(int id, Session session) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getAppointmentFolder(int, int, com.openexchange.groupware.contexts.Context)
     */
    public int getAppointmentFolder(int objectId, int userId, Context ctx) throws OXException {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getAppointmentTitle(int, com.openexchange.groupware.contexts.Context)
     */
    public String getAppointmentTitle(int objectId, Context ctx) throws OXCalendarException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getAppointmentsByID(int, int[], int[], com.openexchange.session.Session)
     */
    public Appointment[] getAppointmentsByID(int folderId, int[] ids, int[] fields, Session session) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getChangeExceptionByDate(int, int, java.util.Date, int[], com.openexchange.session.Session)
     */
    public CalendarDataObject getChangeExceptionByDate(int folderId, int recurrenceId, Date exDate, int[] fields, Session session) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getChangeExceptionDatesByRecurrence(int, com.openexchange.session.Session)
     */
    public long[] getChangeExceptionDatesByRecurrence(int recurrenceId, Session session) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getChangeExceptionsByRecurrence(int, int[], com.openexchange.session.Session)
     */
    public CalendarDataObject[] getChangeExceptionsByRecurrence(int recurrenceId, int[] fields, Session session) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getContext(com.openexchange.session.Session)
     */
    public Context getContext(Session so) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getDAOFromList(java.util.List, int)
     */
    public CalendarDataObject getDAOFromList(List<CalendarDataObject> list, int oid) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getDatesPositions(java.util.Date[], com.openexchange.groupware.calendar.CalendarDataObject)
     */
    public int[] getDatesPositions(Date[] dates, CalendarDataObject recurringAppointment) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getFieldId(java.lang.String)
     */
    public int getFieldId(String fieldName) {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getFieldName(int)
     */
    public String getFieldName(int fieldId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getFieldNames(int[])
     */
    public String[] getFieldNames(int[] fieldIds) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getLongByPosition(com.openexchange.groupware.calendar.CalendarDataObject, int)
     */
    public long getLongByPosition(CalendarDataObject cdao, int pos) throws OXException {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getMAX_END_YEARS()
     */
    public int getMAX_END_YEARS() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getMaxUntilDate(com.openexchange.groupware.calendar.CalendarDataObject)
     */
    public Date getMaxUntilDate(CalendarDataObject cdao) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getNextReminderDate(int, int, com.openexchange.session.Session)
     */
    public Date getNextReminderDate(int oid, int fid, Session so) throws OXException, SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getNextReminderDate(int, int, com.openexchange.session.Session, long)
     */
    public Date getNextReminderDate(int oid, int fid, Session so, long last) throws OXException, SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getOccurenceDate(com.openexchange.groupware.calendar.CalendarDataObject)
     */
    public Date getOccurenceDate(CalendarDataObject cdao) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getOccurenceDate(com.openexchange.groupware.calendar.CalendarDataObject, int)
     */
    public Date getOccurenceDate(CalendarDataObject cdao, int occurrence) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getReadPermission(int, int, com.openexchange.session.Session, com.openexchange.groupware.contexts.Context)
     */
    public boolean getReadPermission(int oid, int fid, Session so, Context ctx) throws OXException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getRecurringAppointmentDeleteAction(com.openexchange.groupware.calendar.CalendarDataObject, com.openexchange.groupware.calendar.CalendarDataObject)
     */
    public int getRecurringAppointmentDeleteAction(CalendarDataObject cdao, CalendarDataObject edao) {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getRecurringAppoiontmentUpdateAction(com.openexchange.groupware.calendar.CalendarDataObject, com.openexchange.groupware.calendar.CalendarDataObject)
     */
    public int getRecurringAppoiontmentUpdateAction(CalendarDataObject cdao, CalendarDataObject edao) {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getSQLInStringForParticipants(java.util.List)
     */
    public String getSQLInStringForParticipants(List<UserParticipant> userParticipant) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getSQLInStringForParticipants(com.openexchange.groupware.container.Participant[])
     */
    public String getSQLInStringForParticipants(Participant[] participant) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getSQLInStringForResources(com.openexchange.groupware.container.Participant[])
     */
    public String getSQLInStringForResources(Participant[] participant) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getString(com.openexchange.groupware.calendar.CalendarDataObject, int)
     */
    public String getString(CalendarDataObject cdao, int fieldID) {
        // TODO Auto-generated method stub
        return null;
    }

    public TimeZone getTimeZone(String ID) {
        return TimeZone.getTimeZone(ID);
    }

    public String getUTCDateFormat(long timeMillis) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getUTCDateFormat(Date date) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getUniqueCalendarSessionName() {
        // TODO Auto-generated method stub
        return null;
    }

    public User getUser(Session so, Context ctx) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getUserConfiguration(com.openexchange.groupware.contexts.Context, int)
     */
    public UserConfiguration getUserConfiguration(Context ctx, int userId) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getUserTimeUTCDate(java.util.Date, java.lang.String)
     */
    public long getUserTimeUTCDate(Date date, String timezone) {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getVisibleAndReadableFolderObject(int, int[], com.openexchange.groupware.contexts.Context, com.openexchange.groupware.userconfiguration.UserConfiguration, java.sql.Connection)
     */
    public CalendarFolderObject getVisibleAndReadableFolderObject(int uid, int[] groups, Context c, UserConfiguration uc, Connection readcon) throws SQLException, DBPoolingException, SearchIteratorException, OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getVisibleFolderSQLInString(java.lang.StringBuilder, int, int[], com.openexchange.groupware.contexts.Context, com.openexchange.groupware.userconfiguration.UserConfiguration, java.sql.Connection)
     */
    public void getVisibleFolderSQLInString(StringBuilder sb, int uid, int[] groups, Context c, UserConfiguration uc, Connection readcon) throws SQLException, OXException, OXCalendarException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getWritePermission(int, int, com.openexchange.session.Session, com.openexchange.groupware.contexts.Context)
     */
    public boolean getWritePermission(int oid, int fid, Session so, Context ctx) throws OXException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#inBetween(long, long, long, long)
     */
    public boolean inBetween(long check_start, long check_end, long range_start, long range_end) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#isException(long, java.util.Set, java.util.Set)
     */
    public boolean isException(long t, Set<Long> ce, Set<Long> de) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#isInThePast(java.sql.Date)
     */
    public boolean isInThePast(java.sql.Date check) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#isInThePast(java.util.Date)
     */
    public boolean isInThePast(Date check) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#isOccurrenceDate(long, long, com.openexchange.groupware.calendar.CalendarDataObject, long[])
     */
    public boolean isOccurrenceDate(long date, long ignoreDate, CalendarDataObject cdao, long[] changeExceptions) throws OXException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#isRecurringMaster(com.openexchange.groupware.calendar.CalendarDataObject)
     */
    public boolean isRecurringMaster(CalendarDataObject edao) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#mergeExceptionDates(java.util.Date[], java.util.Date[])
     */
    public Date[] mergeExceptionDates(Date[] ddates, Date[] cdates) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#normalizeLong(long)
     */
    public long normalizeLong(long millis) {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#purgeExceptionFieldsFromObject(com.openexchange.groupware.calendar.CalendarDataObject)
     */
    public void purgeExceptionFieldsFromObject(CalendarDataObject cdao) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#recoverForInvalidPattern(com.openexchange.groupware.calendar.CalendarDataObject)
     */
    public void recoverForInvalidPattern(CalendarDataObject cdao) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#removeException(java.util.Date[], java.util.Date)
     */
    public Date[] removeException(Date[] dates, Date d) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#removeException(java.util.Date[], long)
     */
    public Date[] removeException(Date[] dates, long dateTime) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#removeFieldsFromObject(com.openexchange.groupware.calendar.CalendarDataObject)
     */
    public void removeFieldsFromObject(CalendarDataObject cdao) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#removeParticipant(com.openexchange.groupware.calendar.CalendarDataObject, int)
     */
    public void removeParticipant(CalendarDataObject cdao, int uid) throws OXException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#removeRecurringType(com.openexchange.groupware.calendar.CalendarDataObject)
     */
    public void removeRecurringType(CalendarDataObject cdao) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#removeUserParticipant(com.openexchange.groupware.calendar.CalendarDataObject, int)
     */
    public void removeUserParticipant(CalendarDataObject cdao, int uid) throws OXException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#replaceDatesWithFirstOccurence(com.openexchange.groupware.container.Appointment)
     */
    public void replaceDatesWithFirstOccurence(Appointment appointment) throws OXException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#resolveFolderIDForUser(int, int, com.openexchange.groupware.contexts.Context)
     */
    public int resolveFolderIDForUser(int oid, int uid, Context c) throws OXException {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#safelySetStartAndEndDateForRecurringAppointment(com.openexchange.groupware.calendar.CalendarDataObject)
     */
    public void safelySetStartAndEndDateForRecurringAppointment(CalendarDataObject cdao) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#setMAX_END_YEARS(int)
     */
    public void setMAX_END_YEARS(int MAX_END_YEARS) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#setRecurrencePositionOrDateInDAO(com.openexchange.groupware.calendar.CalendarDataObject)
     */
    public void setRecurrencePositionOrDateInDAO(CalendarDataObject cdao) throws OXException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#simpleParticipantCheck(com.openexchange.groupware.calendar.CalendarDataObject)
     */
    public void simpleParticipantCheck(CalendarDataObject cdao) throws OXException {
        // TODO Auto-generated method stub
        
    }

    public void triggerEvent(Session session, int action, Appointment appointmentobject) throws OXException {
        // TODO Auto-generated method stub
        
    }

    public void triggerModificationEvent(Session session, CalendarDataObject oldAppointment, CalendarDataObject newAppointment) throws OXCalendarException {
        // TODO Auto-generated method stub
        
    }

    public void updateDefaultStatus(CalendarDataObject cdao, Context ctx, int uid, int inFolder) throws OXException {
        // TODO Auto-generated method stub
        
    }
    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#fillEventInformation(com.openexchange.groupware.calendar.CalendarDataObject, com.openexchange.groupware.calendar.CalendarDataObject, com.openexchange.groupware.container.UserParticipant[], com.openexchange.groupware.container.UserParticipant[], com.openexchange.groupware.container.UserParticipant[], com.openexchange.groupware.container.UserParticipant[], com.openexchange.groupware.container.Participant[], com.openexchange.groupware.container.Participant[], com.openexchange.groupware.container.Participant[], com.openexchange.groupware.container.Participant[])
     */
    public void fillEventInformation(CalendarDataObject cdao, CalendarDataObject edao, UserParticipant[] up_event, UserParticipant[] new_userparticipants, UserParticipant[] deleted_userparticipants, UserParticipant[] modified_userparticipants, Participant[] p_event, Participant[] new_participants, Participant[] deleted_participants, Participant[] modified_participants) {
        // TODO Auto-generated method stub
        
    }
    public CalendarFolderObject getAllVisibleAndReadableFolderObject(int uid, int[] groups, Context c, UserConfiguration uc, Connection con) throws SQLException, DBPoolingException, SearchIteratorException, OXException {
        // TODO Auto-generated method stub
        return null;
    }
    
}