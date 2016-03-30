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
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.CalendarFolderObject;
import com.openexchange.groupware.calendar.MBoolean;
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


/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class MockCalendarCollectionService implements CalendarCollectionService{

    @Override
    public Date[] addException(Date[] dates, Date d) {
        return null;
    }
    @Override
    public long addYears(long base, int years) {
        return 0;
    }

    @Override
    public RecurringResultsInterface calculateFirstRecurring(CalendarObject cdao) throws OXException {
        return null;
    }

    @Override
    public RecurringResultsInterface calculateRecurring(CalendarObject cdao, long range_start, long range_end, int pos) throws OXException {
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#calculateRecurring(com.openexchange.groupware.container.CalendarObject, long, long, int, int, boolean)
     */
    @Override
    public RecurringResultsInterface calculateRecurring(CalendarObject cdao, long range_start, long range_end, int pos, int PMAXTC, boolean ignore_exceptions) throws OXException {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#calculateRecurring(com.openexchange.groupware.container.CalendarObject, long, long, int, int, boolean, boolean)
     */
    @Override
    public RecurringResultsInterface calculateRecurring(CalendarObject cdao, long range_start, long range_end, int pos, int PMAXTC, boolean ignore_exceptions, boolean calc_until) throws OXException {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#calculateRecurringDate(long, long, int)
     */
    @Override
    public Date calculateRecurringDate(long date, long time, int timeZoneOffsetDiff) {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#calculateRecurringIgnoringExceptions(com.openexchange.groupware.container.CalendarObject, long, long, int)
     */
    @Override
    public RecurringResultsInterface calculateRecurringIgnoringExceptions(CalendarObject cdao, long range_start, long range_end, int pos) throws OXException {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#check(java.lang.Object, java.lang.Object)
     */
    @Override
    public boolean check(Object a, Object b) {
        // Nothing to do
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#checkAndAlterCols(int[])
     */
    @Override
    public int[] checkAndAlterCols(int[] cols) {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#checkAndAlterColsForDeleted(int[])
     */
    @Override
    public int[] checkAndAlterColsForDeleted(int[] cols) {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#checkAndConfirmIfUserUserIsParticipantInPublicFolder(com.openexchange.groupware.calendar.CalendarDataObject, com.openexchange.groupware.container.UserParticipant)
     */
    @Override
    public void checkAndConfirmIfUserUserIsParticipantInPublicFolder(CalendarDataObject cdao, UserParticipant up) {
        // Nothing to do

    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#checkAndFillIfUserIsParticipant(com.openexchange.groupware.calendar.CalendarDataObject, com.openexchange.groupware.container.UserParticipant)
     */
    @Override
    public void checkAndFillIfUserIsParticipant(CalendarDataObject cdao, UserParticipant up) {
        // Nothing to do

    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#checkAndFillIfUserIsUser(com.openexchange.groupware.calendar.CalendarDataObject, com.openexchange.groupware.container.Participant)
     */
    @Override
    public void checkAndFillIfUserIsUser(CalendarDataObject cdao, Participant p) throws OXException {
        // Nothing to do

    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#checkAndModifyAlarm(com.openexchange.groupware.calendar.CalendarDataObject, com.openexchange.groupware.container.UserParticipant[], int, com.openexchange.groupware.container.UserParticipant[])
     */
    @Override
    public Set<UserParticipant> checkAndModifyAlarm(CalendarDataObject cdao, Set<UserParticipant> check, int uid, Set<UserParticipant> orig) {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#checkAndRemovePastReminders(com.openexchange.groupware.calendar.CalendarDataObject, com.openexchange.groupware.calendar.CalendarDataObject)
     */
    @Override
    public void checkAndRemovePastReminders(CalendarDataObject cdao, CalendarDataObject edao) {
        // Nothing to do

    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#checkForInvalidCharacters(com.openexchange.groupware.calendar.CalendarDataObject)
     */
    @Override
    public void checkForInvalidCharacters(CalendarDataObject cdao) throws OXException {
        // Nothing to do

    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#checkForSoloReminderUpdate(com.openexchange.groupware.calendar.CalendarDataObject, int[], com.openexchange.groupware.calendar.MBoolean)
     */
    @Override
    public boolean checkForSoloReminderUpdate(CalendarDataObject cdao, int[] ucols, MBoolean cup) {
        // Nothing to do
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#checkIfArrayKeyExistInArray(java.lang.Object[], java.lang.Object[])
     */
    @Override
    public boolean checkIfArrayKeyExistInArray(Object[] a, Object[] b) {
        // Nothing to do
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#checkIfDateOccursInRecurrence(java.util.Date, com.openexchange.groupware.calendar.CalendarDataObject)
     */
    @Override
    public boolean checkIfDateOccursInRecurrence(Date date, CalendarDataObject recurringAppointment) throws OXException {
        // Nothing to do
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#checkIfDatesOccurInRecurrence(java.util.Date[], com.openexchange.groupware.calendar.CalendarDataObject)
     */
    @Override
    public boolean checkIfDatesOccurInRecurrence(Date[] dates, CalendarDataObject recurringAppointment) throws OXException {
        // Nothing to do
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#checkIfUserIsParticipant(com.openexchange.groupware.calendar.CalendarDataObject, com.openexchange.groupware.container.UserParticipant)
     */
    @Override
    public boolean checkIfUserIsParticipant(CalendarDataObject cdao, UserParticipant up) {
        // Nothing to do
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#checkMillisInThePast(long)
     */
    @Override
    public boolean checkMillisInThePast(long check) {
        // Nothing to do
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#checkParticipants(com.openexchange.groupware.container.Participant[], com.openexchange.groupware.container.Participant[])
     */
    @Override
    public boolean checkParticipants(Participant[] newParticipants, Participant[] oldParticipants) {
        // Nothing to do
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#checkPermissions(com.openexchange.groupware.calendar.CalendarDataObject, com.openexchange.session.Session, com.openexchange.groupware.contexts.Context, java.sql.Connection, int, int)
     */
    @Override
    public boolean checkPermissions(CalendarDataObject cdao, Session so, Context ctx, Connection readcon, int action, int inFolder) throws OXException {
        // Nothing to do
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#checkRecurring(com.openexchange.groupware.container.CalendarObject)
     */
    @Override
    public void checkRecurring(CalendarObject cdao) throws OXException {
        // Nothing to do

    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#checkRecurringCompleteness(com.openexchange.groupware.container.CalendarObject, boolean)
     */
    @Override
    public void checkRecurringCompleteness(CalendarObject cdao, boolean ignoreUntilAndOccurrence) throws OXException {
        // Nothing to do

    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#checkUserParticipantObject(com.openexchange.groupware.container.UserParticipant, int)
     */
    @Override
    public void checkUserParticipantObject(UserParticipant up, int folder_type) throws OXException {
        // Nothing to do

    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#closePreparedStatement(java.sql.PreparedStatement)
     */
    @Override
    public void closePreparedStatement(PreparedStatement prep) {
        // Nothing to do

    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#closeResultSet(java.sql.ResultSet)
     */
    @Override
    public void closeResultSet(ResultSet rs) {
        // Nothing to do

    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#closeStatement(java.sql.Statement)
     */
    @Override
    public void closeStatement(Statement stmt) {
        // Nothing to do

    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#convertDates2String(java.util.Date[])
     */
    @Override
    public String convertDates2String(Date[] d) {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#convertString2Dates(java.lang.String)
     */
    @Override
    public Date[] convertString2Dates(String s) {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#createDSString(com.openexchange.groupware.calendar.CalendarDataObject)
     */
    @Override
    public String createDSString(CalendarDataObject cdao) throws OXException {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#debugActiveDates(long, long, boolean[])
     */
    @Override
    public void debugActiveDates(long start, long end, boolean[] activeDates) {
        // Nothing to do

    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#debugRecurringResult(com.openexchange.groupware.calendar.RecurringResultInterface)
     */
    @Override
    public void debugRecurringResult(RecurringResultInterface rr) {
        // Nothing to do

    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#detectFolderMoveAction(com.openexchange.groupware.calendar.CalendarDataObject, com.openexchange.groupware.calendar.CalendarDataObject)
     */
    @Override
    public void detectFolderMoveAction(CalendarDataObject cdao, CalendarDataObject edao) throws OXException {
        // Nothing to do

    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#detectTimeChange(com.openexchange.groupware.calendar.CalendarDataObject, com.openexchange.groupware.calendar.CalendarDataObject)
     */
    @Override
    public boolean detectTimeChange(CalendarDataObject cdao, CalendarDataObject edao) {
        // Nothing to do
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#enhanceCols(int[], int[], int)
     */
    @Override
    public int[] enhanceCols(int[] cols, int[] ara, int i) {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#exceedsHourOfDay(long, java.lang.String)
     */
    @Override
    public boolean exceedsHourOfDay(long millis, String timeZoneID) {
        // Nothing to do
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#exceedsHourOfDay(long, java.util.TimeZone)
     */
    @Override
    public boolean exceedsHourOfDay(long millis, TimeZone zone) {
        // Nothing to do
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#existsReminder(com.openexchange.groupware.contexts.Context, int, int)
     */
    @Override
    public boolean existsReminder(Context c, int oid, int uid) {
        // Nothing to do
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#fillDAO(com.openexchange.groupware.calendar.CalendarDataObject)
     */
    @Override
    public boolean fillDAO(CalendarDataObject cdao) throws OXException {
        // Nothing to do
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#fillEventInformation(com.openexchange.groupware.calendar.CalendarDataObject, com.openexchange.groupware.calendar.CalendarDataObject, com.openexchange.groupware.container.UserParticipant[], com.openexchange.groupware.container.UserParticipant[], com.openexchange.groupware.container.UserParticipant[], com.openexchange.groupware.container.Participant[], com.openexchange.groupware.container.Participant[], com.openexchange.groupware.container.Participant[])
     */
    public void fillEventInformation(CalendarDataObject cdao, CalendarDataObject edao, UserParticipant[] up_event, UserParticipant[] new_userparticipants, UserParticipant[] deleted_userparticipants, Participant[] p_event, Participant[] new_participants, Participant[] deleted_participants) {
        // Nothing to do

    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#fillMap(com.openexchange.groupware.calendar.RecurringResultsInterface, long, long, int, int)
     */
    @Override
    public void fillMap(RecurringResultsInterface rss, long s, long diff, int d, int counter) {
        // Nothing to do

    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#fillObject(com.openexchange.groupware.calendar.CalendarDataObject, com.openexchange.groupware.calendar.CalendarDataObject)
     */
    @Override
    public CalendarDataObject fillObject(CalendarDataObject source, CalendarDataObject destination) {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getAllVisibleAndReadableFolderObject(int, int[], com.openexchange.groupware.contexts.Context, com.openexchange.groupware.userconfiguration.UserConfiguration, java.sql.Connection)
     */
    @Override
    public CalendarFolderObject getAllVisibleAndReadableFolderObject(int uid, int[] groups, Context c, UserConfiguration uc) throws SQLException, OXException, OXException, OXException {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getAppointmentByID(int, com.openexchange.session.Session)
     */
    @Override
    public CalendarDataObject getAppointmentByID(int id, Session session) throws OXException {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getAppointmentFolder(int, int, com.openexchange.groupware.contexts.Context)
     */
    @Override
    public int getAppointmentFolder(int objectId, int userId, Context ctx) throws OXException {
        // Nothing to do
        return 0;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getAppointmentTitle(int, com.openexchange.groupware.contexts.Context)
     */
    @Override
    public String getAppointmentTitle(int objectId, Context ctx) throws OXException {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getAppointmentsByID(int, int[], int[], com.openexchange.session.Session)
     */
    @Override
    public Appointment[] getAppointmentsByID(int folderId, int[] ids, int[] fields, Session session) throws OXException {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getChangeExceptionByDate(int, int, java.util.Date, int[], com.openexchange.session.Session)
     */
    @Override
    public CalendarDataObject getChangeExceptionByDate(int folderId, int recurrenceId, Date exDate, int[] fields, Session session) throws OXException {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getChangeExceptionDatesByRecurrence(int, com.openexchange.session.Session)
     */
    @Override
    public long[] getChangeExceptionDatesByRecurrence(int recurrenceId, Session session) throws OXException {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getChangeExceptionsByRecurrence(int, int[], com.openexchange.session.Session)
     */
    @Override
    public CalendarDataObject[] getChangeExceptionsByRecurrence(int recurrenceId, int[] fields, Session session) throws OXException {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getContext(com.openexchange.session.Session)
     */
    @Override
    public Context getContext(Session so) throws OXException {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getDAOFromList(java.util.List, int)
     */
    @Override
    public CalendarDataObject getDAOFromList(List<CalendarDataObject> list, int oid) {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getDatesPositions(java.util.Date[], com.openexchange.groupware.calendar.CalendarDataObject)
     */
    @Override
    public int[] getDatesPositions(Date[] dates, CalendarDataObject recurringAppointment) throws OXException {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getFieldId(java.lang.String)
     */
    @Override
    public int getFieldId(String fieldName) {
        // Nothing to do
        return 0;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getFieldName(int)
     */
    @Override
    public String getFieldName(int fieldId) {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getFieldNames(int[])
     */
    @Override
    public String[] getFieldNames(int[] fieldIds) {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getLongByPosition(com.openexchange.groupware.calendar.CalendarDataObject, int)
     */
    @Override
    public long getLongByPosition(CalendarDataObject cdao, int pos) throws OXException {
        // Nothing to do
        return 0;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getMAX_END_YEARS()
     */
    @Override
    public int getMAX_END_YEARS() {
        // Nothing to do
        return 0;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getMaxUntilDate(com.openexchange.groupware.calendar.CalendarDataObject)
     */
    @Override
    public Date getMaxUntilDate(CalendarDataObject cdao) {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getNextReminderDate(int, int, com.openexchange.session.Session)
     */
    @Override
    public Date getNextReminderDate(int oid, int fid, Session so) throws OXException, SQLException {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getNextReminderDate(int, int, com.openexchange.session.Session, long)
     */
    @Override
    public Date getNextReminderDate(int oid, int fid, Session so, long last) throws OXException, SQLException {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getOccurenceDate(com.openexchange.groupware.calendar.CalendarDataObject)
     */
    @Override
    public Date getOccurenceDate(CalendarDataObject cdao) throws OXException {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getOccurenceDate(com.openexchange.groupware.calendar.CalendarDataObject, int)
     */
    @Override
    public Date getOccurenceDate(CalendarDataObject cdao, int occurrence) throws OXException {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getReadPermission(int, int, com.openexchange.session.Session, com.openexchange.groupware.contexts.Context)
     */
    @Override
    public boolean getReadPermission(int oid, int fid, Session so, Context ctx) throws OXException {
        // Nothing to do
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getRecurringAppointmentDeleteAction(com.openexchange.groupware.calendar.CalendarDataObject, com.openexchange.groupware.calendar.CalendarDataObject)
     */
    @Override
    public int getRecurringAppointmentDeleteAction(CalendarDataObject cdao, CalendarDataObject edao) {
        // Nothing to do
        return 0;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getRecurringAppoiontmentUpdateAction(com.openexchange.groupware.calendar.CalendarDataObject, com.openexchange.groupware.calendar.CalendarDataObject)
     */
    @Override
    public int getRecurringAppoiontmentUpdateAction(CalendarDataObject cdao, CalendarDataObject edao) {
        // Nothing to do
        return 0;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getSQLInStringForParticipants(java.util.List)
     */
    @Override
    public String getSQLInStringForParticipants(List<UserParticipant> userParticipant) {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getSQLInStringForParticipants(com.openexchange.groupware.container.Participant[])
     */
    @Override
    public String getSQLInStringForParticipants(Participant[] participant) {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getSQLInStringForResources(com.openexchange.groupware.container.Participant[])
     */
    @Override
    public String getSQLInStringForResources(Participant[] participant) {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getString(com.openexchange.groupware.calendar.CalendarDataObject, int)
     */
    @Override
    public String getString(CalendarDataObject cdao, int fieldID) {
        // Nothing to do
        return null;
    }

    @Override
    public TimeZone getTimeZone(String ID) {
        return TimeZone.getTimeZone(ID);
    }

    @Override
    public String getUTCDateFormat(long timeMillis) {
        // Nothing to do
        return null;
    }

    @Override
    public String getUTCDateFormat(Date date) {
        // Nothing to do
        return null;
    }

    @Override
    public String getUniqueCalendarSessionName() {
        // Nothing to do
        return null;
    }

    @Override
    public User getUser(Session so, Context ctx) throws OXException {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getUserConfiguration(com.openexchange.groupware.contexts.Context, int)
     */
    @Override
    public UserConfiguration getUserConfiguration(Context ctx, int userId) throws OXException {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getUserTimeUTCDate(java.util.Date, java.lang.String)
     */
    @Override
    public long getUserTimeUTCDate(Date date, String timezone) {
        // Nothing to do
        return 0;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getVisibleAndReadableFolderObject(int, int[], com.openexchange.groupware.contexts.Context, com.openexchange.groupware.userconfiguration.UserConfiguration, java.sql.Connection)
     */
    @Override
    public CalendarFolderObject getVisibleAndReadableFolderObject(int uid, int[] groups, Context c, UserConfiguration uc, Connection readcon) throws SQLException, OXException, OXException, OXException {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getVisibleFolderSQLInString(java.lang.StringBuilder, int, int[], com.openexchange.groupware.contexts.Context, com.openexchange.groupware.userconfiguration.UserConfiguration, java.sql.Connection)
     */
    @Override
    public void getVisibleFolderSQLInString(StringBuilder sb, int uid, int[] groups, Context c, UserConfiguration uc, Connection readcon) throws SQLException, OXException, OXException {
        // Nothing to do

    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#getWritePermission(int, int, com.openexchange.session.Session, com.openexchange.groupware.contexts.Context)
     */
    @Override
    public boolean getWritePermission(int oid, int fid, Session so, Context ctx) throws OXException {
        // Nothing to do
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#inBetween(long, long, long, long)
     */
    @Override
    public boolean inBetween(long check_start, long check_end, long range_start, long range_end) {
        // Nothing to do
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#isException(long, java.util.Set, java.util.Set)
     */
    @Override
    public boolean isException(long t, Set<Long> ce, Set<Long> de) {
        // Nothing to do
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#isInThePast(java.sql.Date)
     */
    public boolean isInThePast(java.sql.Date check) {
        // Nothing to do
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#isInThePast(java.util.Date)
     */
    @Override
    public boolean isInThePast(Date check) {
        // Nothing to do
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#isOccurrenceDate(long, long, com.openexchange.groupware.calendar.CalendarDataObject, long[])
     */
    @Override
    public boolean isOccurrenceDate(long date, long ignoreDate, CalendarDataObject cdao, long[] changeExceptions) throws OXException {
        // Nothing to do
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#isRecurringMaster(com.openexchange.groupware.calendar.CalendarDataObject)
     */
    @Override
    public boolean isRecurringMaster(CalendarDataObject edao) {
        // Nothing to do
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#mergeExceptionDates(java.util.Date[], java.util.Date[])
     */
    @Override
    public Date[] mergeExceptionDates(Date[] ddates, Date[] cdates) {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#normalizeLong(long)
     */
    @Override
    public long normalizeLong(long millis) {
        // Nothing to do
        return 0;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#purgeExceptionFieldsFromObject(com.openexchange.groupware.calendar.CalendarDataObject)
     */
    @Override
    public void purgeExceptionFieldsFromObject(CalendarDataObject cdao) {
        // Nothing to do

    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#recoverForInvalidPattern(com.openexchange.groupware.calendar.CalendarDataObject)
     */
    @Override
    public void recoverForInvalidPattern(CalendarDataObject cdao) {
        // Nothing to do

    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#removeException(java.util.Date[], java.util.Date)
     */
    @Override
    public Date[] removeException(Date[] dates, Date d) {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#removeException(java.util.Date[], long)
     */
    @Override
    public Date[] removeException(Date[] dates, long dateTime) {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#removeFieldsFromObject(com.openexchange.groupware.calendar.CalendarDataObject)
     */
    @Override
    public void removeFieldsFromObject(CalendarDataObject cdao) {
        // Nothing to do

    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#removeParticipant(com.openexchange.groupware.calendar.CalendarDataObject, int)
     */
    @Override
    public void removeUserParticipant(CalendarDataObject cdao, int uid) throws OXException {
        // Nothing to do

    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#removeRecurringType(com.openexchange.groupware.calendar.CalendarDataObject)
     */
    @Override
    public void removeRecurringType(CalendarDataObject cdao) {
        // Nothing to do

    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#removeUserParticipant(com.openexchange.groupware.calendar.CalendarDataObject, int)
     */
    @Override
    public void removeParticipant(CalendarDataObject cdao, int uid) throws OXException {
        // Nothing to do

    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#replaceDatesWithFirstOccurence(com.openexchange.groupware.container.Appointment)
     */
    @Override
    public void replaceDatesWithFirstOccurence(Appointment appointment) throws OXException {
        // Nothing to do

    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#resolveFolderIDForUser(int, int, com.openexchange.groupware.contexts.Context)
     */
    @Override
    public int resolveFolderIDForUser(int oid, int uid, Context c) throws OXException {
        // Nothing to do
        return 0;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#safelySetStartAndEndDateForRecurringAppointment(com.openexchange.groupware.calendar.CalendarDataObject)
     */
    @Override
    public void safelySetStartAndEndDateForRecurringAppointment(CalendarDataObject cdao) {
        // Nothing to do

    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#setMAX_END_YEARS(int)
     */
    @Override
    public void setMAX_END_YEARS(int MAX_END_YEARS) {
        // Nothing to do

    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#setRecurrencePositionOrDateInDAO(com.openexchange.groupware.calendar.CalendarDataObject)
     */
    @Override
    public void setRecurrencePositionOrDateInDAO(CalendarDataObject cdao) throws OXException {
        // Nothing to do

    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#simpleParticipantCheck(com.openexchange.groupware.calendar.CalendarDataObject)
     */
    @Override
    public void simpleParticipantCheck(CalendarDataObject cdao) throws OXException {
        // Nothing to do

    }

    @Override
    public void triggerEvent(Session session, int action, Appointment appointmentobject) throws OXException {
        // Nothing to do

    }

    @Override
    public void triggerModificationEvent(Session session, CalendarDataObject oldAppointment, CalendarDataObject newAppointment) throws OXException {
        // Nothing to do

    }

    @Override
    public void updateDefaultStatus(CalendarDataObject cdao, Context ctx, int uid, int inFolder) throws OXException {
        // Nothing to do

    }
    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#fillEventInformation(com.openexchange.groupware.calendar.CalendarDataObject, com.openexchange.groupware.calendar.CalendarDataObject, com.openexchange.groupware.container.UserParticipant[], com.openexchange.groupware.container.UserParticipant[], com.openexchange.groupware.container.UserParticipant[], com.openexchange.groupware.container.UserParticipant[], com.openexchange.groupware.container.Participant[], com.openexchange.groupware.container.Participant[], com.openexchange.groupware.container.Participant[], com.openexchange.groupware.container.Participant[])
     */
    @Override
    public void fillEventInformation(final CalendarDataObject cdao, final CalendarDataObject edao, UserParticipant up_event[], final Set<UserParticipant> new_userparticipants, final Set<UserParticipant> deleted_userparticipants,final Set<UserParticipant> modified_userparticipants, Participant p_event[], final Set<Participant> new_participants, final Set<Participant> deleted_participants, final Participant[] modified_participants) {
        // Nothing to do

    }
    @Override
    public CalendarFolderObject getAllVisibleAndReadableFolderObject(int uid, int[] groups, Context c, UserConfiguration uc, Connection con) throws SQLException, OXException, OXException, OXException {
        // Nothing to do
        return null;
    }
	@Override
	public void setRecurrencePositionOrDateInDAO(CalendarDataObject cdao,
			boolean ignore_exceptions) throws OXException {
		// Nothing to do

	}

    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#removeConfirmations(com.openexchange.groupware.calendar.CalendarDataObject, int)
     */
    @Override
    public void removeConfirmations(CalendarDataObject cdao, int uid) {
        // TODO Auto-generated method stub

    }
    /* (non-Javadoc)
     * @see com.openexchange.groupware.calendar.CalendarCollectionService#cloneObjectForRecurringException(com.openexchange.groupware.calendar.CalendarDataObject, com.openexchange.groupware.calendar.CalendarDataObject, com.openexchange.groupware.contexts.Context, com.openexchange.session.Session, int)
     */
    @Override
    public CalendarDataObject cloneObjectForRecurringException(CalendarDataObject cdao, CalendarDataObject edao, Context ctx, Session session, int inFolder) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

}
