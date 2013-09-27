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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link OXCalendarExceptionMessage}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class OXCalendarExceptionMessage implements LocalizableStrings {

    /**
     * Initializes a new {@link OXCalendarExceptionMessage}.
     */
    private OXCalendarExceptionMessage() {
        super();
    }

    public static final String CFO_NOT_INITIALIZIED_MSG = "CalendarFolderObject not initialized.";

    public static final String NOT_YET_SUPPORTED_MSG = "Not yet supported!";

    public static final String NO_SHARED_FOLDER_OWNER_MSG = "Shared folder owner not given.";

    public static final String FOLDER_TYPE_UNRESOLVEABLE_MSG = "Folder type unresolvable.";

    /**
     * Unexpected SQL Error!
     */
    public static final String CALENDAR_SQL_ERROR_MSG = "Unexpected SQL error.";

    /**
     * TODO remove this exception. The AJAX interface should already check for a missing last modified.
     */
    public static final String LAST_MODIFIED_IS_NULL_MSG = "clientLastModified IS NULL. Abort action.";

    /**
     * Unexpected exception %d!
     */
    public static final String UNEXPECTED_EXCEPTION_MSG = "Unexpected exception %d.";

    public static final String EXTERNAL_PARTICIPANTS_MANDATORY_FIELD_MSG = "Mandatory field mail address for external participants";

    public static final String UPDATE_WITHOUT_PARTICIPANTS_MSG = "Would create an object without participants";

    public static final String UPDATE_USER_SHARED_MISMATCH_MSG = "Folder type \"SHARED\" is not allowed in this situation.";

    public static final String RECURRING_UNEXPECTED_DELETE_STATE_MSG = "Unexpected state for deleting a virtual appointment (exception). uid:oid:position %d:%d:%d";

    public static final String ERROR_SESSIONOBJECT_IS_NULL_MSG = "SessionObject not initialized";

    public static final String NO_PERMISSION_MSG = "You do not have the necessary permissions for appointments in folder %1$d.";

    public static final String INSERT_WITH_OBJECT_ID_MSG = "Insert expected but the object id is already given. Aborting action...";

    public static final String UPDATE_WITHOUT_OBJECT_ID_MSG = "Update expected but no object id is given. Aborting action...";

    public static final String FOLDER_DELETE_INVALID_REQUEST_MSG = "Invalid request. Folder is shared.";

    public static final String FOLDER_FOREIGN_INVALID_REQUEST_MSG = "Invalid request. Folder is shared.";

    public static final String FOLDER_IS_EMPTY_INVALID_REQUEST_MSG = "Invalid request. Folder is shared.";

    public static final String FREE_BUSY_UNSUPPOTED_TYPE_MSG = "Unsupported type detected : %d";

    public static final String END_DATE_BEFORE_START_DATE_MSG = "End date is before start date";

    public static final String UNSUPPORTED_LABEL_MSG = "Unsupported label value %d";

    public static final String PRIVATE_FLAG_IN_PRIVATE_FOLDER_MSG = "Private flag is only allowed inside of a private folder.";

    public static final String PRIVATE_FLAG_AND_PARTICIPANTS_MSG = "Appointments marked as 'Private' can only be scheduled for the respective user (or owner of the calendar). Please remove additional participants or remove the \"Private\" mark.";

    public static final String UNSUPPORTED_PRIVATE_FLAG_MSG = "Unsupported private flag value %d";

    public static final String UNSUPPORTED_SHOWN_AS_MSG = "Unsupported \"shown as\"  value %d";

    public static final String MANDATORY_FIELD_START_DATE_MSG = "Required  value \"Start Date\" was not supplied.";

    public static final String MANDATORY_FIELD_END_DATE_MSG = "Required value \"End Date\" was not supplied.";

    public static final String MANDATORY_FIELD_TITLE_MSG = "Required value \"Title\" was not supplied.";

    public static final String UNABLE_TO_CALCULATE_RECURRING_POSITION_MSG = "Unable to create exception, recurring position can not be calculated.";

    public static final String INTERNAL_USER_PARTICIPANT_CHECK_1_MSG = "Got an UserParticipant object with an identifier < 1 Identifier:Folder_Type = %d:%d";

    public static final String INTERNAL_USER_PARTICIPANT_CHECK_2_MSG = "Got an UserParticipant object with a private folder id < 1 : Identifier = %d";

    public static final String INTERNAL_USER_PARTICIPANT_CHECK_3_MSG = "Got an UserParticipant object with a private folder id in a public folder : Identifier = %d";

    public static final String MOVE_NOT_SUPPORTED_MSG = "Move not supported: Cannot move an appointment from folder %d to folder %d";

    public static final String SHARED_FOLDER_MOVE_NOT_SUPPORTED_MSG = "Move not allowed from shared folders";

    public static final String CONTEXT_NOT_SET_MSG = "Calendar operation: Context not set.";

    public static final String NO_PERMISSIONS_TO_ATTACH_DETACH_MSG = "Insufficient rights to add/remove an attachment to/from this folder.";

    public static final String NO_PERMISSIONS_TO_READ_MSG = "Insufficient read rights for this folder.";

    /**
     * Can not resolve recurrence position because we got neither the recurring position nor a recurring date position
     */
    public static final String UNABLE_TO_CALCULATE_RECURRING_POSITION_NO_INPUT_MSG = "Cannot resolve recurrence position because neither the recurring position nor the recurring date position is known.";

    public static final String RECURRING_MISSING_START_DATE_MSG = "Missing start date, unable to calculate recurrence.";

    public static final String RECURRING_MISSING_DAILY_INTERVAL_MSG = "Missing or wrong interval value: %d";

    public static final String RECURRING_MISSING_WEEKLY_INTERVAL_MSG = "Missing or wrong interval value: %d";

    /**
     * Missing or wrong value DayInMonth : %d
     */
    public static final String RECURRING_MISSING_MONTLY_INTERVAL_MSG = "Missing or wrong DayInMonth value: %d";

    public static final String RECURRING_MISSING_MONTLY_INTERVAL_2_MSG = "Missing or wrong month value: %d";

    public static final String RECURRING_MISSING_MONTLY_DAY_MSG = "Missing or wrong day value: %d";

    /**
     * Missing or wrong DayInMonth value: %d
     */
    public static final String RECURRING_MISSING_MONTLY_DAY_2_MSG = "Missing or wrong DayInMonth value: %d";

    public static final String RECURRING_MISSING_YEARLY_INTERVAL_MSG = "Missing or wrong DayInMonth value: %d";

    public static final String RECURRING_MISSING_YEARLY_DAY_MSG = "Missing or wrong day value: %d";

    public static final String RECURRING_MISSING_YEARLY_TYPE_MSG = "Missing or wrong day_or_type : %d";

    public static final String RECURRING_MISSING_YEARLY_INTERVAL_2_MSG = "Missing or wrong interval value: %d";

    public static final String UNABLE_TO_REMOVE_PARTICIPANT_MSG = "Unable to remove participant %d";

    public static final String UNABLE_TO_REMOVE_PARTICIPANT_2_MSG = "Unable to remove participant because this participant is the last remaining";

    public static final String UNSUPPORTED_ACTION_TYPE_MSG = "Action type not supported : %d";

    public static final String SEARCH_ITERATOR_NULL_MSG = "SearchIterator NULL";

    public static final String NON_CALENDAR_FOLDER_MSG = "Folder is not of type Calendar";

    public static final String RECURRING_MISSING_OR_WRONG_VALUE_INTERVAL_MSG = "The required \"interval\" value is missing or wrong";

    public static final String RECURRING_MISSING_OR_WRONG_VALUE_DAYS_MSG = "The required \"days\"  value is missing or wrong: %d";

    public static final String PRIVATE_MOVE_TO_PUBLIC_MSG = "Moving appointment to a public folder flagged as private is not allowed.";

    public static final String LOAD_PERMISSION_EXCEPTION_1_MSG = "You do not have the appropriate permissions to modify this object.";

    public static final String LOAD_PERMISSION_EXCEPTION_2_MSG = "Got the wrong folder identification. You do not have the appropriate permissions to modify this object.";

    public static final String LOAD_PERMISSION_EXCEPTION_3_MSG = "Got the wrong shared folder identification. You do not have the appropriate permissions to modify this object.";

    public static final String LOAD_PERMISSION_EXCEPTION_4_MSG = "You do not have the appropriate permissions to move this object.";

    public static final String LOAD_PERMISSION_EXCEPTION_5_MSG = "You do not have the appropriate permissions to read this object %1$d.";

    public static final String LOAD_PERMISSION_EXCEPTION_6_MSG = "You do not have the appropriate permissions to create an object";

    public static final String RECURRING_MISSING_YEARLY_MONTH_MSG = "Missing or wrong month value: %d";

    public static final String RECURRING_ALREADY_EXCEPTION_MSG = "You are trying to create a new recurring appointment from an exception. This is not possible.";

    public static final String RECURRING_EXCEPTION_MOVE_EXCEPTION_MSG = "You cannot move one instance of a recurring appointment into another folder.";

    public static final String UPDATE_EXCEPTION_MSG = "A database update exception occurred.";

    public static final String MOVE_TO_SHARED_FOLDER_NOT_SUPPORTED_MSG = "Move to a shared folder not allowed if the private flag is set";

    public static final String RECURRING_EXCEPTION_PRIVATE_FLAG_MSG = "You can not use different private flags for one element of a recurring appointment";

    public static final String PIVATE_FLAG_ONLY_IN_PRIVATE_FOLDER_MSG = "You can not use the private flag in a non private folder";

    public static final String INVALID_CHARACTER_MSG = "Bad character in field %1$s. Error: %2$s";

    /**
     * Some data exceeds a field limit. Please shorten the input(s) for affected field(s).
     */
    public static final String TRUNCATED_SQL_ERROR_MSG = "Some data exceeds a field limit. Please shorten the input(s) for affected field(s).";

    /**
     * Calendar calculation requires a proper defined time zone.
     */
    public static final String TIMEZONE_MISSING_MSG = "Calendar calculation requires a properly defined time zone.";

    /**
     * Recurrence position %1$s does not exist
     */
    public static final String UNKNOWN_RECURRENCE_POSITION_MSG = "Recurrence position %1$s does not exist";

    /**
     * One or more exception dates are not contained in recurring appointment
     */
    public static final String FOREIGN_EXCEPTION_DATE_MSG = "One or more exception dates are not contained in recurring appointment";

    /**
     * Appointment's owner must not be removed from participants
     */
    public static final String OWNER_REMOVAL_EXCEPTION_MSG = "Appointment's owner must not be removed from participants";

    /**
     * An event error occurred: %1$s
     */
    public static final String EVENT_ERROR_MSG = "An event error occurred: %1$s";

    /**
     * Value %1$d exceeds max. supported value of %2$d.
     */
    public static final String RECURRING_VALUE_CONSTRAINT_MSG = "Value %1$d exceeds max. supported value of %2$d.";

    /**
     * Unable to calculate first occurrence.
     */
    public static final String UNABLE_TO_CALCULATE_FIRST_RECURRING_MSG = "Unable to calculate first occurrence.";

    /**
     * The recurrence pattern is too complex. Giving up.
     */
    public static final String RECURRENCE_PATTERN_TOO_COMPLEX_MSG = "The recurrence pattern is too complex. Giving up.";

    /**
     * Unknown name-value-pair in recurrence string: %1$s=%2$s
     */
    public static final String UNKNOWN_NVP_IN_REC_STR_MSG = "Unknown name-value-pair in recurrence string: %1$s=%2$s";

    /**
     * Changing recurrence type of a change exception denied
     */
    public static final String INVALID_RECURRENCE_TYPE_CHANGE_MSG = "Changing recurrence type of a change exception denied";

    /**
     * Changing recurrence position of a change exception denied
     */
    public static final String INVALID_RECURRENCE_POSITION_CHANGE_MSG = "Changing recurrence position of a change exception denied.";

    /**
     * User changing the appointment is missing.
     */
    public static final String MODIFIED_BY_MISSING_MSG = "User changing the appointment is missing.";

    /**
     * Callbacks threw exceptions
     */
    public static final String CALLBACK_EXCEPTIONS_MSG = "Some callbacks threw exceptions: %s";

    /**
     * Series end is before start date.
     */
    public static final String UNTIL_BEFORE_START_DATE_MSG = "Series end is before start date.";

    /**
     * Incomplete recurring information: Missing interval.
     */
    public static final String INCOMPLETE_REC_INFOS_INTERVAL_MSG = "Incomplete recurring information: missing interval.";

    /**
     * Incomplete recurring information: Missing series end date or number of occurrences.
     */
    public static final String INCOMPLETE_REC_INFOS_UNTIL_OR_OCCUR_MSG = "Incomplete recurring information: missing series end date or number of occurrences.";

    /**
     * Incomplete recurring information: Missing weekday.
     */
    public static final String INCOMPLETE_REC_INFOS_WEEKDAY_MSG = "Incomplete recurring information: missing weekday.";

    /**
     * Incomplete recurring information: Missing day in month.
     */
    public static final String INCOMPLETE_REC_INFOS_MONTHDAY_MSG = "Incomplete recurring information: missing day in month.";

    /**
     * Incomplete recurring information: Missing month.
     */
    public static final String INCOMPLETE_REC_INFOS_MONTH_MSG = "Incomplete recurring information: missing month.";

    /**
     * Incomplete recurring information: Missing recurrence type.
     */
    public static final String INCOMPLETE_REC_INFOS_TYPE_MSG = "Incomplete recurring information: missing recurrence type.";

    /**
     * Move of recurring appointments is not supported
     */
    public static final String RECURRING_FOLDER_MOVE_MSG = "Moving a recurring appointment to another folder is not supported.";

    /**
     * In order to accomplish the search, %1$d or more characters are required.
     */
    public static final String PATTERN_TOO_SHORT_MSG = "In order to accomplish the search, %1$d or more characters are required.";

    /**
     * Redundant information.
     */
    public static final String REDUNDANT_UNTIL_OCCURRENCES_MSG = "Redundant information for Until and occurrences.";

    /**
     * Unnecessary recurrence Information for Type "no".
     */
    public static final String UNNECESSARY_RECURRENCE_INFORMATION_NO_MSG = "Unnecessary recurrence information for recurrence type \"no recurrence\".";

    /**
     * Unnecessary recurrence information.
     */
    public static final String UNNECESSARY_RECURRENCE_INFORMATION_MSG = "Unnecessary recurrence information (%1$s) for type %2$s";

    public static final String UNABLE_TO_CALCULATE_POSITION_MSG = "The recurring appointment has been deleted or is outside of the range of the recurrence.";

    public static final String CHANGE_EXCEPTION_TO_RECURRENCE_MSG = "Changing an exception into a series is not supported.";

    public static final String UID_ALREDY_EXISTS_MSG = "Cannot insert appointment (%1$s). An appointment with the unique identifier (%2$s) already exists.";

    /** SQL Problem. */
    public static final String SQL_ERROR_MSG = "SQL Problem.";

    /** Wrong number of rows changed. Expected %1$d but was %2$d. */
    public static final String WRONG_ROW_COUNT_MSG = "Wrong number of rows changed. Expected %1$d but was %2$d.";

    /**
     * Unable to find a participant for a specified object.
     */
    public static final String COULD_NOT_FIND_PARTICIPANT_MSG = "Could not find participant for this object.";

    /** Was not able to calculate next upcoming reminder for series appointment %2$d in context %1$d. */
    public static final String NEXT_REMINDER_FAILED_MSG = "Was not able to calculate next upcoming reminder for series appointment %2$d in context %1$d.";

    /**
     * Invalid sequence value: %1$d
     */
    public static final String INVALID_SEQUENCE_MSG = "Invalid sequence value: %1$d";

    /**
     * An external participant with email address %1$s is already contained. Please remove duplicate participant and retry.
     */
    public static final String DUPLICATE_EXTERNAL_PARTICIPANT_MSG = "An external participant with the E-Mail address %1$s is already included. Please remove participant duplicate and retry.";

    // Appointment is not a recurring appointment.
    public static final String NO_RECCURENCE_MSG = "Appointment is not a recurring appointment.";

}
