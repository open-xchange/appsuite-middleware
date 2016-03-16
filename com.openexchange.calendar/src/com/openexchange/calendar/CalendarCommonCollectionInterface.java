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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.CalendarFolderObject;
import com.openexchange.groupware.calendar.MBoolean;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIteratorException;

public interface CalendarCommonCollectionInterface {

    public static final int SHARED = 3;

    /**
     * Gets the max. until date for given infinite recurring appointment for calculation purpose.
     *
     * @param cdao The infinite recurring appointment (neither until nor occurrence set)
     * @return The max. until date for given infinite recurring appointment
     */
    public Date getMaxUntilDate(final CalendarDataObject cdao);

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

    /**
     * Add editing user or shared folder owner to user participants. This ensures
     * the user itself is always on the participants list.
     */
    public void checkAndFillIfUserIsParticipant(final CalendarDataObject cdao, final UserParticipant up);

    public void checkAndConfirmIfUserUserIsParticipant(final CalendarDataObject cdao, final UserParticipant up);

    public UserParticipant[] checkAndModifyAlarm(final CalendarDataObject cdao, UserParticipant check[], final int uid, final UserParticipant orig[]);

    public void simpleParticipantCheck(final CalendarDataObject cdao) throws OXException;

    /**
     * If user or shared folder owner is missing in participants it is added.
     */
    public void checkAndFillIfUserIsUser(final CalendarDataObject cdao, final Participant p);

    public void removeParticipant(final CalendarDataObject cdao, final int uid) throws OXException;

    public void removeUserParticipant(final CalendarDataObject cdao, final int uid) throws OXException;

    public Date getNextReminderDate(final int oid, final int fid, final Session so) throws OXException, SQLException;

    public Date getNextReminderDate(final int oid, final int fid, final Session so, final long last) throws OXException, SQLException;

    public boolean existsReminder(final Context c, final int oid, final int uid);

    public void debugActiveDates(final long start, final long end, final boolean activeDates[]);

    public void debugRecurringResult(final RecurringResult rr);

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

    public String getSQLInStringForParticipants(final UserParticipant[] userParticipant);

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

    public CalendarFolderObject getAllVisibleAndReadableFolderObject(final int uid, final int groups[], final Context c, final UserConfiguration uc, final Connection readcon) throws SQLException, SearchIteratorException, OXException;

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

    public boolean isInThePast(final java.sql.Date check);

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

    public boolean detectTimeChange(final CalendarDataObject cdao, final CalendarDataObject edao);

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

    public void fillEventInformation(final CalendarDataObject cdao, final CalendarDataObject edao, UserParticipant up_event[], final UserParticipant[] new_userparticipants, final UserParticipant[] deleted_userparticipants, Participant p_event[], final Participant new_participants[], final Participant deleted_participants[]);

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

}
