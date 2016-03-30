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

package com.openexchange.groupware.calendar;

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
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIteratorException;

/**
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 *
 */
@SingletonService
public interface CalendarCollectionService {

    // Stuff from CalendarCommonCollection
    public final int PRIVATE = 1;
    public final int PUBLIC = 2;
    public final int SHARED = 3;


    /**
     * Gets the max. until date for given infinite recurring appointment for calculation purpose.
     *
     * @param cdao The infinite recurring appointment (neither until nor occurrence set)
     * @return The max. until date for given infinite recurring appointment
     */
    public Date getMaxUntilDate(final CalendarDataObject cdao);

    /**
     * Adds a the given amount of years to the provided base time. The calculation is based on UTC time.
     *
     * @param base
     * @param years
     * @return
     */
    public long addYears(long base, int years);

    /**
     * Gets the name of specified field ID.
     *
     * @param fieldId The field ID.
     * @return The name of specified field ID or <code>null</code> if field ID is unknown.
     */
    public String getFieldName(final int fieldId);

    /**
     * Gets the names of specified field IDs.
     *
     * @param fieldIds The field IDs.
     * @return The names of specified field IDs, unknown IDs are set to <code>null</code>.
     */
    public String[] getFieldNames(final int[] fieldIds);

    /**
     * Gets the ID of specified field name.
     *
     * @param fieldName The field name.
     * @return The ID of specified field name or <code>-1</code> if field name
     *         is unknown.
     */
    public int getFieldId(final String fieldName);

    public boolean checkPermissions(final CalendarDataObject cdao, final Session so, final Context ctx, final Connection readcon, final int action, final int inFolder) throws OXException;

    public boolean getReadPermission(final int oid, final int fid, final Session so, final Context ctx) throws OXException;

    public boolean getWritePermission(final int oid, final int fid, final Session so, final Context ctx) throws OXException;

    public boolean checkIfUserIsParticipant(final CalendarDataObject cdao, final UserParticipant up);

    public void removeConfirmations(CalendarDataObject cdao, int uid);

    public void updateDefaultStatus(CalendarDataObject cdao, Context ctx, int uid, int inFolder) throws OXException;

    /**
     * Add editing user or shared folder owner to user participants. This ensures
     * the user itself is always on the participants list.
     */
    public void checkAndFillIfUserIsParticipant(final CalendarDataObject cdao, final UserParticipant up);

    public void checkAndConfirmIfUserUserIsParticipantInPublicFolder(final CalendarDataObject cdao, final UserParticipant up);

    public Set<UserParticipant> checkAndModifyAlarm(final CalendarDataObject cdao, Set<UserParticipant> check, final int uid,  final Set<UserParticipant> orig);

    public void simpleParticipantCheck(final CalendarDataObject cdao) throws OXException;

    /**
     * If user or shared folder owner is missing in participants it is added.
     * @throws OXException
     */
    public void checkAndFillIfUserIsUser(final CalendarDataObject cdao, final Participant p) throws OXException;

    public void removeUserParticipant(final CalendarDataObject cdao, final int uid) throws OXException;

    public void removeParticipant(final CalendarDataObject cdao, final int uid) throws OXException;

    public Date getNextReminderDate(final int oid, final int fid, final Session so) throws OXException, SQLException;

    public Date getNextReminderDate(final int oid, final int fid, final Session so, final long last) throws OXException, SQLException;

    public boolean existsReminder(final Context c, final int oid, final int uid);

    public void debugActiveDates(final long start, final long end, final boolean activeDates[]);

    public void debugRecurringResult(final RecurringResultInterface rr);

    public String getUniqueCalendarSessionName();

    /**
     * Checks if given columns contain fields
     * {@link Appointment#RECURRENCE_TYPE}, if so fields
     * {@link Appointment#CHANGE_EXCEPTIONS},
     * {@link Appointment#DELETE_EXCEPTIONS}, and
     * {@link Appointment#RECURRENCE_CALCULATOR} are added to specified
     * columns if not already present.
     *
     * @param cols The columns to check
     * @return The possibly enhanced columns
     */
    public int[] checkAndAlterCols(int cols[]);

    /**
     * Checks if given columns contain fields that are not present in the backup table - if so, they're removed.
     *
     * @param cols The columns to check
     * @return The possibly reduced columns
     */
    int[] checkAndAlterColsForDeleted(int cols[]);

    /**
     * Creates a newly allocated array containing first given array enhanced by
     * specified number of elements from second array.
     *
     * @param cols The first array
     * @param ara The second array
     * @param i The number of elements to copy from second array
     * @return A newly allocated array containing first given array enhanced by
     *         specified number of elements from second array.
     */
    public int[] enhanceCols(final int cols[], final int ara[], final int i);

    public void triggerEvent(final Session session, final int action, final Appointment appointmentobject) throws OXException;

    public void triggerModificationEvent(final Session session, final CalendarDataObject oldAppointment, final CalendarDataObject newAppointment) throws OXException;

    public String getSQLInStringForParticipants(final List<UserParticipant> userParticipant);

    public String getSQLInStringForParticipants(final Participant[] participant);

    public String getSQLInStringForResources(final Participant[] participant);

    /**
     * Checks if range specified by <code>check_start</code> and
     * <code>check_end</code> intersects/overlaps the range specified by
     * <code>range_start</code> and <code>range_end</code>.
     *
     * @param check_start
     *            The check start
     * @param check_end
     *            The check end
     * @param range_start
     *            The range start
     * @param range_end
     *            The range end
     * @return <code>true</code> if range specified by <code>check_start</code>
     *         and <code>check_end</code> intersects/overlaps the range
     *         specified by <code>range_start</code> and <code>range_end</code>;
     *         otherwise <code>false</code>
     */
    public boolean inBetween(final long check_start, final long check_end, final long range_start, final long range_end);

    /**
     * Converts given string of comma-separated <i>long</i>s to an array of
     * {@link Date} objects
     *
     * @param s
     *            The string of comma-separated <i>long</i>s
     * @return An array of {@link Date} objects
     */
    public Date[] convertString2Dates(final String s);

    /**
     * Converts given array of {@link Date} objects to a string of
     * comma-separated <i>long</i>s
     *
     * @param d
     *            The array of {@link Date} objects
     * @return A string of comma-separated <i>long</i>s
     */
    public String convertDates2String(final Date[] d);

    /**
     * Check if specified objects are different
     *
     * @param a The first object
     * @param b The second object
     * @return <code>true</code> if specified objects are different; otherwise <code>false</code>
     */
    public boolean check(final Object a, final Object b);

    /**
     * Checks if the two participant arrays are diffrent.
     * Two participant arrays are not different, if the contain the same Participants according their id,
     * indepent of the participant status.
     *
     * @param newParticipants
     * @param oldParticipants
     * @return true if the participant arrays are different, false otherwise.
     */
    public boolean checkParticipants(final Participant[] newParticipants, final Participant[] oldParticipants);

    public CalendarFolderObject getVisibleAndReadableFolderObject(final int uid, final int groups[], final Context c, final UserConfiguration uc, final Connection readcon) throws SQLException, SearchIteratorException, OXException;

    public CalendarFolderObject getAllVisibleAndReadableFolderObject(final int uid, final int groups[], final Context c, final UserConfiguration uc) throws SQLException, SearchIteratorException, OXException;

    public CalendarFolderObject getAllVisibleAndReadableFolderObject(final int uid, final int groups[], final Context c, final UserConfiguration uc, final Connection con) throws SQLException, SearchIteratorException, OXException;

    public void getVisibleFolderSQLInString(final StringBuilder sb, final int uid, final int groups[], final Context c, final UserConfiguration uc, final Connection readcon) throws SQLException, OXException, OXException;

    /**
     * Returns a {@link Date} array with all occurrences of <code>d</code> deleted from given date array
     *
     * @param dates The date array
     * @param d The date to check against
     * @return A {@link Date} array with all occurrences of <code>d</code> deleted from given date array
     */
    public Date[] removeException(final Date[] dates, final Date d);

    /**
     * Returns a {@link Date} array with all occurrences of <code>d</code> deleted from given date array
     *
     * @param dates The date array
     * @param dateTime The date time to check against
     * @return A {@link Date} array with all occurrences of <code>d</code> deleted from given date array
     */
    public Date[] removeException(final Date[] dates, final long dateTime);

    /**
     * Returns a {@link Date} array with <code>d</code> added to given date array
     * if not already contained.
     *
     * @param dates The date array
     * @param d The date to add
     * @return A {@link Date} array with <code>d</code> added to given date array
     */
    public Date[] addException(final Date[] dates, final Date d);

    public CalendarDataObject fillObject(final CalendarDataObject source, final CalendarDataObject destination);

    public void removeFieldsFromObject(final CalendarDataObject cdao);

    public void purgeExceptionFieldsFromObject(final CalendarDataObject cdao);

    public boolean isInThePast(final java.util.Date check);

    /**
     * Checks if given time millis are less than today (normalized current time
     * millis):
     *
     * <pre>
     * return check &lt; (CalendarRecurringCollection.normalizeLong(System.currentTimeMillis()));
     * </pre>
     *
     * @param check
     *            The time millis to check against today's millis
     * @return <code>true</code> if given time millis are less than normalized
     *         current time millis; otherwise <code>false</code>
     */
    public boolean checkMillisInThePast(final long check);

    public void removeRecurringType(final CalendarDataObject cdao);

    public void closeResultSet(final ResultSet rs);

    public void closePreparedStatement(final PreparedStatement prep);

    public void closeStatement(final Statement stmt);

    public void detectFolderMoveAction(final CalendarDataObject cdao, final CalendarDataObject edao) throws OXException;

    public void checkUserParticipantObject(final UserParticipant up, final int folder_type) throws OXException;

    public boolean detectTimeChange(final CalendarDataObject cdao, final CalendarDataObject edao) throws OXException;

    /**
     * Gets an appointment by specified ID
     *
     * @param id The appointment ID
     * @param session The session providing needed user data
     * @return The appointment belonging to specified ID or <code>null</code>
     * @throws OXException If appointment cannot be loaded
     */
    public CalendarDataObject getAppointmentByID(final int id, final Session session) throws OXException;

    /**
     * Gets the change exception of specified recurrence in given folder with given exception date.
     *
     * @param folderId The folder ID
     * @param recurrenceId The ID of parental recurrence
     * @param exDate The exception date
     * @param fields The fields to fill in returned calendar object
     * @param session The requesting user's session
     * @return The change exception of specified recurrence in given folder with given exception date or <code>null</code>.
     * @throws OXException If corresponding change exception cannot be loaded.
     */
    public CalendarDataObject getChangeExceptionByDate(final int folderId, final int recurrenceId, final Date exDate, final int[] fields, final Session session) throws OXException;

    /**
     * Gets dates of change exceptions belonging to specified recurrence.
     *
     * @param recurrenceId The recurrence's ID
     * @param session The session providing needed user data
     * @return The dates of change exceptions belonging to specified recurrence.
     * @throws OXException If change exceptions cannot be loaded
     */
    public long[] getChangeExceptionDatesByRecurrence(final int recurrenceId, final Session session) throws OXException;

    /**
     * Gets all change exceptions belonging to specified recurrence.
     *
     * @param recurrenceId The recurrence's ID
     * @param fields The fields to fill in returned calendar objects
     * @param session The session providing needed user data
     * @return All change exceptions belonging to specified recurrence.
     * @throws OXException If change exceptions cannot be loaded
     */
    public CalendarDataObject[] getChangeExceptionsByRecurrence(final int recurrenceId, final int[] fields, final Session session) throws OXException;

    /**
     * Loads calendar objects corresponding to specified IDs.
     *
     * @param folderId The folder ID
     * @param ids The IDs
     * @param fields The fields to fill in returned calendar objects
     * @param session The requesting user's session
     * @return The loaded calendar objects
     * @throws OXException If calendar objects cannot be loaded
     */
    public Appointment[] getAppointmentsByID(final int folderId, final int[] ids, final int[] fields, final Session session) throws OXException;

    /**
     * Determines appointment's valid folder ID for specified user
     *
     * @param oid The appointment ID
     * @param uid The suer ID
     * @param c The context
     * @return The appointment's valid folder ID for specified user
     * @throws OXException If appointment's valid folder ID for specified user cannot be determined
     */
    public int resolveFolderIDForUser(final int oid, final int uid, final Context c) throws OXException;

    public void fillEventInformation(final CalendarDataObject cdao, final CalendarDataObject edao, UserParticipant up_event[], final Set<UserParticipant> new_userparticipants, final Set<UserParticipant> deleted_userparticipants,final Set<UserParticipant> modified_userparticipants, Participant p_event[], final Set<Participant> new_participants, final Set<Participant> deleted_participants, final Participant[] modified_participants);

    /**
     * Gets the calendar data object from specified list whose ID matches given
     * ID.
     *
     * @param list
     *            The list of calendar data objects
     * @param oid
     *            The ID to search for
     * @return The calendar data object from specified list whose ID matches
     *         given ID or <code>null</code> if none matches
     */
    public CalendarDataObject getDAOFromList(final List<CalendarDataObject> list, final int oid);

    public boolean checkForSoloReminderUpdate(final CalendarDataObject cdao, final int[] ucols, final MBoolean cup);

    public void checkAndRemovePastReminders(final CalendarDataObject cdao, final CalendarDataObject edao);

    /**
     * Adds the time zone offset to given date's time millis and determines
     * corresponding date based on resulting time millis
     *
     * @param date
     *            The date whose UTC-based date shall be calculated
     * @param timezone
     *            The time zone identifier
     * @return The UTC-based date
     */
    public long getUserTimeUTCDate(final Date date, final String timezone);

    public boolean checkIfArrayKeyExistInArray(final Object a[], final Object b[]);

    /**
     * Checks if specified (exception) date occurs in given recurring appointment.
     *
     * @param date The normalized (exception) date
     * @param recurringAppointment The recurring appointment
     * @return <code>true</code> if date occurs in recurring appointment; otherwise <code>false</code>
     * @throws OXException If occurrences cannot be calculated
     */
    public boolean checkIfDateOccursInRecurrence(final Date date, final CalendarDataObject recurringAppointment) throws OXException;

    /**
     * Checks if specified (exception) dates occur in given recurring appointment.
     *
     * @param dates The (exception) dates
     * @param recurringAppointment The recurring appointment
     * @return <code>true</code> if every date occurs in recurring appointment; otherwise <code>false</code>
     * @throws OXException If occurrences cannot be calculated
     */
    public boolean checkIfDatesOccurInRecurrence(final Date[] dates, final CalendarDataObject recurringAppointment) throws OXException;

    /**
     * Gets the corresponding positions of specified (exception) dates in given recurring appointment.
     * <p>
     * If a date does not occur in given recurring appointment, its position is set to <code>-1</code>.
     *
     * @param dates The (exception) dates
     * @param recurringAppointment The recurring appointment
     * @return The corresponding positions of specified (exception) dates in given recurring appointment.
     * @throws OXException If occurrences cannot be calculated
     */
    public int[] getDatesPositions(final Date[] dates, final CalendarDataObject recurringAppointment) throws OXException;

    /**
     * Merges the specified (exception) dates
     *
     * @param ddates The first dates
     * @param cdates The second dates
     * @return The sorted and merged dates
     */
    public Date[] mergeExceptionDates(final Date[] ddates, final Date[] cdates);

    public void checkForInvalidCharacters(final CalendarDataObject cdao) throws OXException;

    public String getString(final CalendarDataObject cdao, final int fieldID);

    public void recoverForInvalidPattern(final CalendarDataObject cdao);

    // Stuff from CalendarRecurringCollection
    public final String NO_DS = null;

    /**
     * Constant to indicate that no recurring action was detected
     */
    public final int RECURRING_NO_ACTION = 0;

    /**
     * Constant to indicate that a delete exception shall be created through a <b>delete</b> operation
     */
    public final int RECURRING_VIRTUAL_ACTION = 1;

    /**
     * Constant to indicate that a formerly created change exception shall be deleted through a <b>delete</b> operation; meaning it's turned
     * into a delete exception
     */
    public final int RECURRING_EXCEPTION_ACTION = 2;

    /**
     * Constant to indicate that a former recurring appointment is turned to a normal non-recurring appointment through an <b>update</b>
     * operation
     */
    public final int RECURRING_EXCEPTION_DELETE = 3;

    /**
     * Constant to indicate that whole recurring appointment shall be deleted through a <b>delete</b> operation
     */
    public final int RECURRING_FULL_DELETE = 4;

    /**
     * Constant to indicate that a change exception shall be created through an <b>update</b> operation
     */
    public final int RECURRING_CREATE_EXCEPTION = 5;

    /**
     * Constant to indicate that the recurring pattern of a recurring appointment shall be changed through an <b>update</b> operation
     */
    public final int CHANGE_RECURRING_TYPE = 6;

    /**
     * Constant to indicate that a formerly created change exception shall be deleted through an <b>update</b> operation; meaning it's
     * turned into a delete exception
     */
    public final int RECURRING_EXCEPTION_DELETE_EXISTING = 7;

    /**
     * The maximum supported number of occurrences.
     */
    public final int MAX_OCCURRENCESE = 999;


    /**
     * <code>getMAX_END_YEARS</code> returns NO_END_YEARS. NO_END_YEARS means if no end date is given we calculate the start date PLUS
     * NO_END_YEARS to have an end date ...
     *
     * @return an <code>int</code> value
     */
    public int getMAX_END_YEARS();

    /**
     * <code>setMAX_END_YEARS</code> sets the max number of years a sequence can run if no end date is given
     *
     * @return an <code>int</code> value
     */
    public void setMAX_END_YEARS(final int MAX_END_YEARS);

    /**
     * Checks if given calendar data object denotes a recurring master.
     *
     * @param edao The calendar data object to check
     * @return <code>true</code> if given calendar data object denotes a recurring master; otherwise <code>false</code>
     */
    public boolean isRecurringMaster(final CalendarDataObject edao);

    /**
     * <code>getRecurringAppointmentDeleteAction</code> detects and returns the action type
     *
     * @param cdao a <code>CalendarDataObject</code> object (tranfered)
     * @param edao a <code>CalendarDataObject</code> object (loaded)
     * @return a <code>int</code> value
     */
    public int getRecurringAppointmentDeleteAction(final CalendarDataObject cdao, final CalendarDataObject edao);

    /**
     * <code>getRecurringAppoiontmentUpdateAction</code> detects and returns the action type
     *
     * @param cdao a <code>CalendarDataObject</code> object (transfered)
     * @param edao a <code>CalendarDataObject</code> object (loaded)
     * @return a <code>int</code> value
     */
    public int getRecurringAppoiontmentUpdateAction(final CalendarDataObject cdao, final CalendarDataObject edao);

    /**
     * <code>getLongByPosition</code> return the long value for the given CalendarDataObject and the given recurring position. The method
     * return 0 if the long can not be calculated.
     *
     * @param cdao a <code>CalendarDataObject</code>
     * @param pos a <code>int</code>
     * @return a <code>long</code> value
     */
    public long getLongByPosition(final CalendarDataObject cdao, final int pos) throws OXException;

    public void setRecurrencePositionOrDateInDAO(final CalendarDataObject cdao) throws OXException;

    /**
     * <code>getLongByPosition</code> return the long value for the given CalendarDataObject and the given recurring position. The method
     * return 0 if the long can not be calculated.
     *
     * @param cdao a <code>CalendarDataObject</code>
     * @param ignore_exceptions - Whether to ignore the holes left by delete exceptions
     * @return a <code>long</code> value
     */
    public void setRecurrencePositionOrDateInDAO(CalendarDataObject cdao, boolean ignore_exceptions) throws OXException;

    /**
     * Removes hours and minutes for the given date.
     *
     * @param millis milliseconds since January 1, 1970, 00:00:00 GMT not to exceed the milliseconds representation for the year 8099. A
     *            negative number indicates the number of milliseconds before January 1, 1970, 00:00:00 GMT.
     * @return The normalized <code>long</code> value
     */
    public long normalizeLong(final long millis);

    /**
     * Checks if specified UTC date increases day in month if adding given time zone's offset.
     *
     * @param millis The time millis
     * @param timeZoneID The time zone ID
     * @return <code>true</code> if specified date in increases day in month if adding given time zone's offset; otherwise
     *         <code>false</code>
     */
    public boolean exceedsHourOfDay(final long millis, final String timeZoneID);

    /**
     * Checks if specified UTC date increases day in month if adding given time zone's offset.
     *
     * @param millis The time millis
     * @param zone The time zone
     * @return <code>true</code> if specified date in increases day in month if adding given time zone's offset; otherwise
     *         <code>false</code>
     */
    public boolean exceedsHourOfDay(final long millis, final TimeZone zone);

    /**
     * Creates the recurring pattern for given (possibly recurring) appointment if needed and fills its recurring information according to
     * generated pattern.
     *
     * @param cdao The (possibly recurring) appointment
     * @return <code>true</code> if specified appointment denotes a proper recurring appointment whose recurring information could be
     *         successfully filled; otherwise <code>false</code> to indicate a failure
     */
    public boolean fillDAO(final CalendarDataObject cdao) throws OXException;

    /**
     * Creates the recurring string for specified recurring appointment
     *
     * @param cdao The recurring appointment whose recurring string shall be created
     * @return The recurring string for specified recurring appointment
     * @throws OXException If recurring appointment contains insufficient or invalid recurring information
     */
    public String createDSString(final CalendarDataObject cdao) throws OXException;

    /**
     * Gets the specified occurrence's end date within recurring appointment.
     *
     * @param cdao The recurring appointment
     * @return The first occurrence's end date
     * @throws OXException If calculating the first occurrence fails
     */
    public Date getOccurenceDate(final CalendarDataObject cdao) throws OXException;

    /**
     * Gets the given occurrence's end date within specified recurring appointment.
     *
     * @param cdao The recurring appointment
     * @param occurrence The occurrence
     * @return The first occurrence's end date
     * @throws OXException If calculating the first occurrence fails
     */
    public Date getOccurenceDate(final CalendarDataObject cdao, final int occurrence) throws OXException;

    /**
     * Checks if normalized date of given time millis is contained in either specified change exceptions or delete exceptions.
     *
     * @param t The time millis to check
     * @param ce The change exceptions
     * @param de The delete exceptions
     * @return <code>true</code>if normalized date of given time millis denotes an exception; otherwise <code>false</code>
     */
    public boolean isException(final long t, final Set<Long> ce, final Set<Long> de);

    /**
     * Tests if specified date is covered by any occurrence of given recurring appointment ignoring second specified date.
     * <p>
     * This method is useful when creating a new change exception within specified recurring appointment and checking that change
     * exception's destination date is not already occupied by either a regular recurrence's occurrence. or an existing change exception
     *
     * @param date The date to check
     * @param ignoreDate The date to ignore
     * @param cdao The recurring appointment to check against
     * @param changeExceptions The recurring appointment's change exception dates
     * @return <code>true</code> if specified time millis is covered by any occurrence; otherwise <code>false</code>
     * @throws OXException If calculating the occurrences fails
     */
    public boolean isOccurrenceDate(final long date, final long ignoreDate, final CalendarDataObject cdao, final long[] changeExceptions) throws OXException;

    /**
     * This method calculates the first occurrence and stores it within the returned {@link RecurringResultsInterface} collection.
     *
     * @param cdao The recurring appointment whose first occurrence shall be calculated
     * @return The calculated first occurrence kept in a {@link RecurringResultsInterface} collection
     * @throws OXException If calculating the first occurrence fails
     */
    public RecurringResultsInterface calculateFirstRecurring(final CalendarObject cdao) throws OXException;

    /**
     * This method calculates the recurring occurrences and stores them within the returned {@link RecurringResultsInterface} collection.
     * <p>
     * <b>! This method returns max. {@link #MAX_OCCURRENCESE} results AND ignores exceptions !</b>
     * <p>
     * A certain occurrence can be calculated by setting parameter {@code pos}.
     * <p>
     * A range query is performed when setting parameter {@code range_start} and {@code range_end}.
     *
     * @param cdao The recurring appointment whose occurrences shall be calculated
     * @param range_start The (optional) range start from which occurrences shall be calculated; leave to <code>0</code> to ignore
     * @param range_end The (optional) range end until occurrences shall be calculated; leave to <code>0</code> to ignore
     * @param pos The (optional) one-based occurrence position to calculate; leave to <code>0</code> to ignore
     * @return The calculated occurrences including change/delete exceptions kept in a {@link RecurringResultsInterface} collection
     * @throws OXException If calculating the occurrences fails
     */
    public RecurringResultsInterface calculateRecurringIgnoringExceptions(final CalendarObject cdao, final long range_start, final long range_end, final int pos) throws OXException;

    /**
     * This method calculates the recurring occurrences and stores them within the returned {@link RecurringResultsInterface} collection.
     * <p>
     * <b>! This method returns max. {@link #MAX_OCCURRENCESE} results !</b>
     * <p>
     * A certain occurrence can be calculated by setting parameter {@code pos}.
     * <p>
     * A range query is performed when setting parameter {@code range_start} and {@code range_end}.
     *
     * @param cdao The recurring appointment whose occurrences shall be calculated
     * @param range_start The (optional) range start from which occurrences shall be calculated; leave to <code>0</code> to ignore
     * @param range_end The (optional) range end until occurrences shall be calculated; leave to <code>0</code> to ignore
     * @param pos The (optional) one-based occurrence position to calculate; leave to <code>0</code> to ignore
     * @return The calculated occurrences without change/delete exceptions kept in a {@link RecurringResultsInterface} collection
     * @throws OXException If calculating the occurrences fails
     */
    public RecurringResultsInterface calculateRecurring(final CalendarObject cdao, final long range_start, final long range_end, final int pos) throws OXException;

    /**
     * This method calculates the recurring occurrences and stores them within the returned {@link RecurringResultsInterface} collection.
     * <p>
     * A certain occurrence can be calculated by setting parameter {@code pos}.
     * <p>
     * A range query is performed when setting parameter {@code range_start} and {@code range_end}.
     *
     * @param cdao The recurring appointment whose occurrences shall be calculated
     * @param range_start The (optional) range start from which occurrences shall be calculated; leave to <code>0</code> to ignore
     * @param range_end The (optional) range end until occurrences shall be calculated; leave to <code>0</code> to ignore
     * @param pos The (optional) one-based occurrence position to calculate; leave to <code>0</code> to ignore
     * @param PMAXTC The max. number of occurrences to calculate; mostly set to {@link #MAX_OCCURRENCESE}
     * @param ignore_exceptions <code>true</code> to ignore change and delete exceptions during calculation, meaning corresponding
     *            occurrences do not appear in returned {@link RecurringResultsInterface} collection; otherwise <code>false</code>
     * @return The calculated occurrences kept in a {@link RecurringResultsInterface} collection
     * @throws OXException If calculating the occurrences fails
     */
    public RecurringResultsInterface calculateRecurring(final CalendarObject cdao, final long range_start, final long range_end, final int pos, final int PMAXTC, final boolean ignore_exceptions) throws OXException;

    /**
     * This method calculates the recurring occurrences and stores them within the returned {@link RecurringResultsInterface} collection.
     * <p>
     * A certain occurrence can be calculated by setting parameter {@code pos}.
     * <p>
     * A range query is performed when setting parameter {@code range_start} and {@code range_end}.
     *
     * @param cdao The recurring appointment whose occurrences shall be calculated
     * @param range_start The (optional) range start from which occurrences shall be calculated; leave to <code>0</code> to ignore
     * @param range_end The (optional) range end until occurrences shall be calculated; leave to <code>0</code> to ignore
     * @param pos The (optional) one-based occurrence position to calculate; leave to <code>0</code> to ignore
     * @param PMAXTC The max. number of occurrences to calculate; mostly set to {@link #MAX_OCCURRENCESE}
     * @param ignore_exceptions <code>true</code> to ignore change and delete exceptions during calculation, meaning corresponding
     *            occurrences do not appear in returned {@link RecurringResultsInterface} collection; otherwise <code>false</code>
     * @param calc_until This parameter is not used, yet
     * @return The calculated occurrences kept in a {@link RecurringResultsInterface} collection
     * @throws OXException If calculating the occurrences fails
     */
    public RecurringResultsInterface calculateRecurring(final CalendarObject cdao, final long range_start, final long range_end, final int pos, final int PMAXTC, final boolean ignore_exceptions, final boolean calc_until) throws OXException;

    public void fillMap(final RecurringResultsInterface rss, final long s, final long diff, final int d, final int counter);

    public Date calculateRecurringDate(final long date, final long time, int timeZoneOffsetDiff);

    /**
     * Checks if recurring information provided in specified calendar object is complete.<br>
     * Fields <b>Until</b> and <b>Occurrence</b> may be ignored since an infinite recurring appointment may omit this information.<br>
     * This is the dependency table as defined by {@link #createDSString(CalendarDataObject)}:
     * <p>
     * <table border="1">
     * <tr>
     * <th>Recurrence type</th>
     * <th>Interval</th>
     * <th>Until or Occurrence</th>
     * <th>Weekday</th>
     * <th>Monthday</th>
     * <th>Month</th>
     * </tr>
     * <tr>
     * <td align="center">DAILY<br>
     * &nbsp;</td>
     * <td align="center">x</td>
     * <td align="center">x (dependent on argument <code>ignoreUntilAndOccurrence</code>)</td>
     * <td align="center">&nbsp;</td>
     * <td align="center">&nbsp;</td>
     * <td align="center">&nbsp;</td>
     * </tr>
     * <tr>
     * <td align="center">WEEKLY<br>
     * &nbsp;</td>
     * <td align="center">x</td>
     * <td align="center">x (dependent on argument <code>ignoreUntilAndOccurrence</code>)</td>
     * <td align="center">x</td>
     * <td align="center">&nbsp;</td>
     * <td align="center">&nbsp;</td>
     * </tr>
     * <tr>
     * <td align="center">MONTHLY 1<br>
     * (without weekday)</td>
     * <td align="center">x</td>
     * <td align="center">x (dependent on argument <code>ignoreUntilAndOccurrence</code>)</td>
     * <td align="center">&nbsp;</td>
     * <td align="center">x</td>
     * <td align="center">&nbsp;</td>
     * </tr>
     * <tr>
     * <td align="center">MONTHLY 2<br>
     * (with weekday)</td>
     * <td align="center">x</td>
     * <td align="center">x (dependent on argument <code>ignoreUntilAndOccurrence</code>)</td>
     * <td align="center">x</td>
     * <td align="center">x</td>
     * <td align="center">&nbsp;</td>
     * </tr>
     * <tr>
     * <td align="center">YEARLY 1<br>
     * (without weekday)</td>
     * <td align="center">x</td>
     * <td align="center">x (dependent on argument <code>ignoreUntilAndOccurrence</code>)</td>
     * <td align="center">&nbsp;</td>
     * <td align="center">x</td>
     * <td align="center">x</td>
     * </tr>
     * <tr>
     * <td align="center">YEARLY 2<br>
     * (with weekday)</td>
     * <td align="center">x</td>
     * <td align="center">x (dependent on argument <code>ignoreUntilAndOccurrence</code>)</td>
     * <td align="center">x</td>
     * <td align="center">x</td>
     * <td align="center">x</td>
     * </tr>
     * </table>
     *
     * @param cdao The calendar object to check
     * @param ignoreUntilAndOccurrence <code>true</code> to ignore whether until or occurrence is contained in specified calendar object;
     *            otherwise <code>false</code>
     * @throws OXException If check fails
     */
    public void checkRecurringCompleteness(final CalendarObject cdao, final boolean ignoreUntilAndOccurrence) throws OXException;

    public void checkRecurring(final CalendarObject cdao) throws OXException;

    /**
     * Creates a cloned version from given calendar object ready for being used to create the denoted change exception
     *
     * @param cdao The current calendar object denoting the change exception
     * @param edao The calendar object's storage version
     * @param ctx The context
     * @param session The session
     * @param inFolder The folder the action is performed in.
     * @return A cloned version ready for being used to create the denoted change exception
     * @throws OXException If cloned version cannot be created
     */
    public CalendarDataObject cloneObjectForRecurringException(final CalendarDataObject cdao, final CalendarDataObject edao, Context ctx, final Session session, int inFolder) throws OXException;

    /**
     * Replaces the start date and end date of specified recurring appointment with the start date and end date of its first occurrence.
     * <p>
     * <b>Note</b> that neither <i>recurrence position</i> nor <i>recurrence date position</i> is set.
     *
     * @param appointment The recurring appointment whose start date and end date shall be replaced
     * @throws OXException If calculating the first occurrence fails
     */
    public void replaceDatesWithFirstOccurence(final Appointment appointment) throws OXException;

    /**
     * Sets the start/end date of specified recurring appointment to its first occurrence. A possible exception is swallowed and recurring
     * information is removed.
     *
     * @param cdao The recurring appointment whose start/end date shall be set to its first occurrence
     */
    public void safelySetStartAndEndDateForRecurringAppointment(final CalendarDataObject cdao);

    // Stuff from Tools
    /**
     * Formats specified date's time millis into a date string.<br>
     * e.g.: <code>&quot;Jan 13, 2009&quot;</code>
     *
     * @param timeMillis The date's time millis to format
     * @return The date string.
     */
    public String getUTCDateFormat(final long timeMillis);

    /**
     * Formats specified date into a date string.<br>
     * e.g.: <code>&quot;Jan 13, 2009&quot;</code>
     *
     * @param date The date to format
     * @return The date string.
     */
    public String getUTCDateFormat(final Date date);

    public Context getContext(final Session so) throws OXException;

    public User getUser(final Session so, final Context ctx) throws OXException;

    public UserConfiguration getUserConfiguration(final Context ctx, final int userId) throws OXException;

    /**
     * Gets the <code>TimeZone</code> for the given ID.
     *
     * @param ID The ID for a <code>TimeZone</code>, either an abbreviation such as "PST", a full name such as "America/Los_Angeles", or a
     *            custom ID such as "GMT-8:00".
     * @return The specified <code>TimeZone</code>, or the GMT zone if the given ID cannot be understood.
     */
    public TimeZone getTimeZone(final String ID);

    /**
     * Gets the appointment's title associated with given object ID in given context.
     *
     * @param objectId The object ID
     * @param ctx The context
     * @return The appointment's title or <code>null</code>
     * @throws OXException If determining appointment's title fails
     */
    public String getAppointmentTitle(final int objectId, final Context ctx) throws OXException;

    /**
     * Gets the appointment's folder associated with given object ID in given context.
     *
     * @param objectId The object ID
     * @param userId The session user
     * @param ctx The context
     * @return The appointment's folder associated with given object ID in given context.
     * @throws OXException If determining appointment's folder fails
     */
    public int getAppointmentFolder(final int objectId, final int userId, final Context ctx) throws OXException;
}
