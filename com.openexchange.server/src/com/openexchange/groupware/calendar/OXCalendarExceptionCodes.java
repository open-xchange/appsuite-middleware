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

import com.openexchange.database.IncorrectStringSQLException;
import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;
import com.openexchange.exception.OXException.Generic;
import com.openexchange.tools.exceptions.SimpleIncorrectStringAttribute;

/**
 * The calendar error code enumeration.
 */
public enum OXCalendarExceptionCodes implements DisplayableOXExceptionCode {
    /**
     * CalendarFolderObject not initialized.
     */
    CFO_NOT_INITIALIZIED(OXCalendarExceptionCodes.CFO_NOT_INITIALIZIED_MSG, 1, Category.CATEGORY_ERROR),
    /**
     * Not yet supported!
     */
    NOT_YET_SUPPORTED(OXCalendarExceptionCodes.NOT_YET_SUPPORTED_MSG, 2, Category.CATEGORY_ERROR),
    /**
     * Shared folder owner not given.
     */
    NO_SHARED_FOLDER_OWNER(OXCalendarExceptionCodes.NO_SHARED_FOLDER_OWNER_MSG, 3, Category.CATEGORY_ERROR),
    /**
     * Folder type unresolvable.
     */
    FOLDER_TYPE_UNRESOLVEABLE(OXCalendarExceptionCodes.FOLDER_TYPE_UNRESOLVEABLE_MSG, 4, Category.CATEGORY_ERROR),
    /**
     * Unexpected SQL Error!
     */
    CALENDAR_SQL_ERROR(OXCalendarExceptionCodes.CALENDAR_SQL_ERROR_MSG, OXExceptionStrings.SQL_ERROR_MSG, 5, Category.CATEGORY_ERROR) {
        @Override
        public OXException create(final Throwable cause, final Object... args) {
            if (IncorrectStringSQLException.class.isInstance(cause)) {
                IncorrectStringSQLException isse = (IncorrectStringSQLException) cause;
                CalendarField field = CalendarField.getByDbField(isse.getColumn());
                OXException e = OXCalendarExceptionCodes.INVALID_CHARACTER.create(cause, field.getLocalizable(), isse.getIncorrectString());
                e.addProblematic(new SimpleIncorrectStringAttribute(field.getAppointmentObjectID(), isse.getIncorrectString()));
                return e;
            }
            return OXExceptionFactory.getInstance().create(this, cause, args);
        }
    },
    /**
     * TODO remove this exception. The AJAX interface should already check for a missing last modified.
     */
    LAST_MODIFIED_IS_NULL(OXCalendarExceptionCodes.LAST_MODIFIED_IS_NULL_MSG, 6, Category.CATEGORY_ERROR),
    /**
     * Unexpected exception %d!
     */
    UNEXPECTED_EXCEPTION(OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION_MSG, 7, Category.CATEGORY_ERROR),
    /**
     * Mandatory field mail address for external participants
     */
    EXTERNAL_PARTICIPANTS_MANDATORY_FIELD(OXCalendarExceptionCodes.EXTERNAL_PARTICIPANTS_MANDATORY_FIELD_MSG, 8, Category.CATEGORY_USER_INPUT),
    /**
     * Would create an object without participants
     */
    UPDATE_WITHOUT_PARTICIPANTS(OXCalendarExceptionCodes.UPDATE_WITHOUT_PARTICIPANTS_MSG, OXCalendarExceptionMessage.UPDATE_WITHOUT_PARTICIPANTS_DISPLAY, 9, Category.CATEGORY_ERROR),
    /**
     * Folder type \"SHARED\" is not allowed in this situation.
     */
    UPDATE_USER_SHARED_MISMATCH(OXCalendarExceptionCodes.UPDATE_USER_SHARED_MISMATCH_MSG, 10, Category.CATEGORY_USER_INPUT),
    /**
     * Unexpected state for deleting a virtual appointment (exception). uid:oid:position %d:%d:%d
     */
    RECURRING_UNEXPECTED_DELETE_STATE(OXCalendarExceptionCodes.RECURRING_UNEXPECTED_DELETE_STATE_MSG, 11, Category.CATEGORY_ERROR),
    /**
     * SessionObject not initialized
     */
    ERROR_SESSIONOBJECT_IS_NULL(OXCalendarExceptionCodes.ERROR_SESSIONOBJECT_IS_NULL_MSG, 12, Category.CATEGORY_ERROR),
    /**
     * You do not have the appropriate permissions to read appointments in folder %1$d.
     */
    NO_PERMISSION(OXCalendarExceptionCodes.NO_PERMISSION_MSG, OXCalendarExceptionMessage.NO_PERMISSION_MSG, 13, Category.CATEGORY_PERMISSION_DENIED),
    /**
     * Insert expected but the object id is already given. Aborting action...
     */
    INSERT_WITH_OBJECT_ID(OXCalendarExceptionCodes.INSERT_WITH_OBJECT_ID_MSG, 14, Category.CATEGORY_ERROR),
    /**
     * Update expected but no object id is given. Aborting action...
     */
    UPDATE_WITHOUT_OBJECT_ID(OXCalendarExceptionCodes.UPDATE_WITHOUT_OBJECT_ID_MSG, 15, Category.CATEGORY_ERROR),
    /**
     * Invalid request. Folder is shared.
     */
    FOLDER_DELETE_INVALID_REQUEST(OXCalendarExceptionCodes.FOLDER_DELETE_INVALID_REQUEST_MSG, 16, Category.CATEGORY_ERROR),
    /**
     * Invalid request. Folder is shared.
     */
    FOLDER_FOREIGN_INVALID_REQUEST(OXCalendarExceptionCodes.FOLDER_FOREIGN_INVALID_REQUEST_MSG, 17, Category.CATEGORY_ERROR),
    /**
     * Invalid request. Folder is shared.
     */
    FOLDER_IS_EMPTY_INVALID_REQUEST(OXCalendarExceptionCodes.FOLDER_IS_EMPTY_INVALID_REQUEST_MSG, 18, Category.CATEGORY_ERROR),
    /**
     * Unsupported type detected : %d
     */
    FREE_BUSY_UNSUPPOTED_TYPE(OXCalendarExceptionCodes.FREE_BUSY_UNSUPPOTED_TYPE_MSG, 19, Category.CATEGORY_ERROR),
    /**
     * End date is before start date.
     */
    END_DATE_BEFORE_START_DATE(OXCalendarExceptionCodes.END_DATE_BEFORE_START_DATE_MSG, OXCalendarExceptionMessage.END_DATE_BEFORE_START_DATE_DISPLAY, 20, Category.CATEGORY_USER_INPUT),
    /**
     * Unsupported label value %d
     */
    UNSUPPORTED_LABEL(OXCalendarExceptionCodes.UNSUPPORTED_LABEL_MSG, OXCalendarExceptionMessage.UNSUPPORTED_LABEL_DISPLAY, 21, Category.CATEGORY_USER_INPUT),
    /**
     * Private flag is only allowed inside of a private folder.
     */
    PRIVATE_FLAG_IN_PRIVATE_FOLDER(OXCalendarExceptionCodes.PRIVATE_FLAG_IN_PRIVATE_FOLDER_MSG, OXCalendarExceptionMessage.PRIVATE_FLAG_IN_PRIVATE_FOLDER_MSG, 22, Category.CATEGORY_USER_INPUT),
    /**
     * Appointments marked as 'Private' can only be scheduled for the respective user (or owner of the calendar). Please remove additional
     * participants or remove the \"Private\" mark.
     */
    PRIVATE_FLAG_AND_PARTICIPANTS(OXCalendarExceptionCodes.PRIVATE_FLAG_AND_PARTICIPANTS_MSG, OXCalendarExceptionMessage.PRIVATE_FLAG_AND_PARTICIPANTS_MSG, 23, Category.CATEGORY_USER_INPUT),
    /**
     * Unsupported private flag value %d
     */
    UNSUPPORTED_PRIVATE_FLAG(OXCalendarExceptionCodes.UNSUPPORTED_PRIVATE_FLAG_MSG, 24, Category.CATEGORY_USER_INPUT),
    /**
     * Unsupported \"shown as\" value %d.
     */
    UNSUPPORTED_SHOWN_AS(OXCalendarExceptionCodes.UNSUPPORTED_SHOWN_AS_MSG, OXCalendarExceptionMessage.UNSUPPORTED_SHOWN_AS_MSG, 25, Category.CATEGORY_USER_INPUT),
    /**
     * Required value \"Start Date\" was not supplied.
     */
    MANDATORY_FIELD_START_DATE(OXCalendarExceptionCodes.MANDATORY_FIELD_START_DATE_MSG, OXCalendarExceptionMessage.MANDATORY_FIELD_START_DATE_MSG, 26, Category.CATEGORY_USER_INPUT),
    /**
     * Required value \"End Date\" was not supplied.
     */
    MANDATORY_FIELD_END_DATE(OXCalendarExceptionCodes.MANDATORY_FIELD_END_DATE_MSG, OXCalendarExceptionMessage.MANDATORY_FIELD_END_DATE_MSG, 27, Category.CATEGORY_USER_INPUT),
    /**
     * Required value \"Title\" was not supplied.
     */
    MANDATORY_FIELD_TITLE(OXCalendarExceptionCodes.MANDATORY_FIELD_TITLE_MSG, OXCalendarExceptionMessage.MANDATORY_FIELD_TITLE_MSG, 28, Category.CATEGORY_USER_INPUT),
    /**
     * Unable to create exception, recurring position (%d) can not be calculated.
     */
    UNABLE_TO_CALCULATE_RECURRING_POSITION(OXCalendarExceptionCodes.UNABLE_TO_CALCULATE_RECURRING_POSITION_MSG, 29, Category.CATEGORY_USER_INPUT),
    /**
     * Got an UserParticipant object with an identifier < 1 Identifier:Folder_Type = %d:%d
     */
    INTERNAL_USER_PARTICIPANT_CHECK_1(OXCalendarExceptionCodes.INTERNAL_USER_PARTICIPANT_CHECK_1_MSG, 30, Category.CATEGORY_ERROR),
    /**
     * Got an UserParticipant object with a private folder id < 1 : Identifier = %d
     */
    INTERNAL_USER_PARTICIPANT_CHECK_2(OXCalendarExceptionCodes.INTERNAL_USER_PARTICIPANT_CHECK_2_MSG, 31, Category.CATEGORY_USER_INPUT),
    /**
     * Got an UserParticipant object with a private folder id in a public folder : Identifier = %d
     */
    INTERNAL_USER_PARTICIPANT_CHECK_3(OXCalendarExceptionCodes.INTERNAL_USER_PARTICIPANT_CHECK_3_MSG, 32, Category.CATEGORY_USER_INPUT),
    /**
     * Move not supported: Cannot move an appointment from folder %d to folder %d.
     */
    MOVE_NOT_SUPPORTED(OXCalendarExceptionCodes.MOVE_NOT_SUPPORTED_MSG, OXCalendarExceptionMessage.MOVE_NOT_SUPPORTED_MSG, 33, Category.CATEGORY_ERROR),
    /**
     * Move not allowed from shared folders.
     */
    SHARED_FOLDER_MOVE_NOT_SUPPORTED(OXCalendarExceptionCodes.SHARED_FOLDER_MOVE_NOT_SUPPORTED_MSG, OXCalendarExceptionMessage.SHARED_FOLDER_MOVE_NOT_SUPPORTED_MSG, 34, Category.CATEGORY_ERROR),
    /**
     * Calendar operation: Context not set.
     */
    CONTEXT_NOT_SET(OXCalendarExceptionCodes.CONTEXT_NOT_SET_MSG, 35, Category.CATEGORY_ERROR),
    /**
     * Insufficient rights to add/remove an attachment to/from this folder.
     */
    NO_PERMISSIONS_TO_ATTACH_DETACH(OXCalendarExceptionCodes.NO_PERMISSIONS_TO_ATTACH_DETACH_MSG, OXCalendarExceptionMessage.NO_PERMISSIONS_TO_ATTACH_DETACH_DISPLAY, 36, Category.CATEGORY_PERMISSION_DENIED),
    /**
     * Insufficient read rights for this folder.
     */
    NO_PERMISSIONS_TO_READ(OXCalendarExceptionCodes.NO_PERMISSIONS_TO_READ_MSG, OXCalendarExceptionMessage.NO_PERMISSIONS_TO_READ_MSG, 37, Category.CATEGORY_PERMISSION_DENIED),
    /**
     * Can not resolve recurrence position because we got neither the recurring position nor a recurring date position
     */
    UNABLE_TO_CALCULATE_RECURRING_POSITION_NO_INPUT(OXCalendarExceptionCodes.UNABLE_TO_CALCULATE_RECURRING_POSITION_NO_INPUT_MSG, 38, Category.CATEGORY_ERROR),
    /**
     * Missing start date, unable to calculate recurrence.
     */
    RECURRING_MISSING_START_DATE(OXCalendarExceptionCodes.RECURRING_MISSING_START_DATE_MSG, 39, Category.CATEGORY_ERROR),
    /**
     * Missing or wrong interval value: %d
     */
    RECURRING_MISSING_DAILY_INTERVAL(OXCalendarExceptionCodes.RECURRING_MISSING_DAILY_INTERVAL_MSG, 40, Category.CATEGORY_USER_INPUT),
    /**
     * Missing or wrong interval value: %d
     */
    RECURRING_MISSING_WEEKLY_INTERVAL(OXCalendarExceptionCodes.RECURRING_MISSING_WEEKLY_INTERVAL_MSG, 41, Category.CATEGORY_USER_INPUT),
    /**
     * Missing or wrong value DayInMonth : %d
     */
    RECURRING_MISSING_MONTLY_INTERVAL(OXCalendarExceptionCodes.RECURRING_MISSING_MONTLY_INTERVAL_MSG, 42, Category.CATEGORY_USER_INPUT),
    /**
     * Missing or wrong month value: %d
     */
    RECURRING_MISSING_MONTLY_INTERVAL_2(OXCalendarExceptionCodes.RECURRING_MISSING_MONTLY_INTERVAL_2_MSG, 43, Category.CATEGORY_USER_INPUT),
    /**
     * Missing or wrong day value: %d
     */
    RECURRING_MISSING_MONTLY_DAY(OXCalendarExceptionCodes.RECURRING_MISSING_MONTLY_DAY_MSG, 44, Category.CATEGORY_USER_INPUT),
    /**
     * Missing or wrong DayInMonth value: %d
     */
    RECURRING_MISSING_MONTLY_DAY_2(OXCalendarExceptionCodes.RECURRING_MISSING_MONTLY_DAY_2_MSG, 45, Category.CATEGORY_USER_INPUT),
    /**
     * Missing or wrong DayInMonth value: %d
     */
    RECURRING_MISSING_YEARLY_INTERVAL(OXCalendarExceptionCodes.RECURRING_MISSING_YEARLY_INTERVAL_MSG, 46, Category.CATEGORY_USER_INPUT),
    /**
     * Missing or wrong day value: %d
     */
    RECURRING_MISSING_YEARLY_DAY(OXCalendarExceptionCodes.RECURRING_MISSING_YEARLY_DAY_MSG, 47, Category.CATEGORY_USER_INPUT),
    /**
     * "Missing or wrong day_or_type : %d
     */
    RECURRING_MISSING_YEARLY_TYPE(OXCalendarExceptionCodes.RECURRING_MISSING_YEARLY_TYPE_MSG, 48, Category.CATEGORY_USER_INPUT),
    /**
     * Missing or wrong interval value: %d
     */
    RECURRING_MISSING_YEARLY_INTERVAL_2(OXCalendarExceptionCodes.RECURRING_MISSING_YEARLY_INTERVAL_2_MSG, 49, Category.CATEGORY_USER_INPUT),
    /**
     * Unable to remove participant %d
     */
    UNABLE_TO_REMOVE_PARTICIPANT(OXCalendarExceptionCodes.UNABLE_TO_REMOVE_PARTICIPANT_MSG, 50, Category.CATEGORY_ERROR),
    /**
     * Unable to remove participant because this participant is the last remaining
     */
    UNABLE_TO_REMOVE_PARTICIPANT_2(OXCalendarExceptionCodes.UNABLE_TO_REMOVE_PARTICIPANT_2_MSG, 51, Category.CATEGORY_USER_INPUT),
    /**
     * Action type not supported : %d
     */
    UNSUPPORTED_ACTION_TYPE(OXCalendarExceptionCodes.UNSUPPORTED_ACTION_TYPE_MSG, 52, Category.CATEGORY_ERROR),
    /**
     * SearchIterator NULL
     */
    SEARCH_ITERATOR_NULL(OXCalendarExceptionCodes.SEARCH_ITERATOR_NULL_MSG, 53, Category.CATEGORY_ERROR),
    /**
     * Folder is not of type Calendar.
     */
    NON_CALENDAR_FOLDER(OXCalendarExceptionCodes.NON_CALENDAR_FOLDER_MSG, OXCalendarExceptionMessage.NON_CALENDAR_FOLDER_MSG, 54, Category.CATEGORY_USER_INPUT),
    /**
     * The required \"interval\" value is missing or wrong
     */
    RECURRING_MISSING_OR_WRONG_VALUE_INTERVAL(OXCalendarExceptionCodes.RECURRING_MISSING_OR_WRONG_VALUE_INTERVAL_MSG, 55, Category.CATEGORY_USER_INPUT),
    /**
     * The required \"days\" value is missing or wrong: %d
     */
    RECURRING_MISSING_OR_WRONG_VALUE_DAYS(OXCalendarExceptionCodes.RECURRING_MISSING_OR_WRONG_VALUE_DAYS_MSG, 56, Category.CATEGORY_USER_INPUT),
    /**
     * Moving an appointment to a public folder flagged as private is not allowed.
     */
    PRIVATE_MOVE_TO_PUBLIC(OXCalendarExceptionCodes.PRIVATE_MOVE_TO_PUBLIC_MSG, OXCalendarExceptionMessage.PRIVATE_MOVE_TO_PUBLIC_MSG, 57, Category.CATEGORY_USER_INPUT),
    /**
     * You do not have the appropriate permissions to modify this object.
     */
    LOAD_PERMISSION_EXCEPTION_1(OXCalendarExceptionCodes.LOAD_PERMISSION_EXCEPTION_1_MSG, OXCalendarExceptionMessage.LOAD_PERMISSION_EXCEPTION_1_MSG, 58, Category.CATEGORY_PERMISSION_DENIED),
    /**
     * Got the wrong folder identification. You do not have the appropriate permissions to modify this object.
     */
    LOAD_PERMISSION_EXCEPTION_2(OXCalendarExceptionCodes.LOAD_PERMISSION_EXCEPTION_2_MSG, OXCalendarExceptionMessage.LOAD_PERMISSION_EXCEPTION_1_MSG, 59, Category.CATEGORY_PERMISSION_DENIED),
    /**
     * Got the wrong shared folder identification. You do not have the appropriate permissions to modify this object.
     */
    LOAD_PERMISSION_EXCEPTION_3(OXCalendarExceptionCodes.LOAD_PERMISSION_EXCEPTION_3_MSG, OXCalendarExceptionMessage.LOAD_PERMISSION_EXCEPTION_1_MSG, 60, Category.CATEGORY_PERMISSION_DENIED),
    /**
     * You do not have the appropriate permissions to move this object.
     */
    LOAD_PERMISSION_EXCEPTION_4(OXCalendarExceptionCodes.LOAD_PERMISSION_EXCEPTION_4_MSG, OXCalendarExceptionMessage.LOAD_PERMISSION_EXCEPTION_4_MSG, 61, Category.CATEGORY_PERMISSION_DENIED),
    /**
     * You do not have the appropriate permissions to read this object %1$d.
     */
    LOAD_PERMISSION_EXCEPTION_5(OXCalendarExceptionCodes.LOAD_PERMISSION_EXCEPTION_5_MSG, OXCalendarExceptionMessage.LOAD_PERMISSION_EXCEPTION_5_DISPLAY, 62, Category.CATEGORY_PERMISSION_DENIED),
    /**
     * You do not have the appropriate permissions to create an object.
     */
    LOAD_PERMISSION_EXCEPTION_6(OXCalendarExceptionCodes.LOAD_PERMISSION_EXCEPTION_6_MSG, OXCalendarExceptionMessage.LOAD_PERMISSION_EXCEPTION_6_MSG, 63, Category.CATEGORY_PERMISSION_DENIED),
    /**
     * Missing or wrong month value: %d
     */
    RECURRING_MISSING_YEARLY_MONTH(OXCalendarExceptionCodes.RECURRING_MISSING_YEARLY_MONTH_MSG, 64, Category.CATEGORY_USER_INPUT),
    /**
     * You are trying to create a new recurring appointment from an exception. This is not possible.
     */
    RECURRING_ALREADY_EXCEPTION(OXCalendarExceptionCodes.RECURRING_ALREADY_EXCEPTION_MSG, OXCalendarExceptionMessage.RECURRING_ALREADY_EXCEPTION_MSG, 65, Category.CATEGORY_USER_INPUT),
    /**
     * You cannot move one instance of a recurring appointment into another folder.
     */
    RECURRING_EXCEPTION_MOVE_EXCEPTION(OXCalendarExceptionCodes.RECURRING_EXCEPTION_MOVE_EXCEPTION_MSG, OXCalendarExceptionMessage.RECURRING_EXCEPTION_MOVE_EXCEPTION_MSG, 66, Category.CATEGORY_USER_INPUT),
    /**
     * Moving an instance of a recurring appointment into another folder is not allowed.
     */
    UPDATE_EXCEPTION(OXCalendarExceptionCodes.UPDATE_EXCEPTION_MSG, OXExceptionStrings.SQL_ERROR_MSG, 67, Category.CATEGORY_ERROR),
    /**
     * Move to a shared folder not allowed if the private flag is set.
     */
    MOVE_TO_SHARED_FOLDER_NOT_SUPPORTED(OXCalendarExceptionCodes.MOVE_TO_SHARED_FOLDER_NOT_SUPPORTED_MSG, OXCalendarExceptionMessage.MOVE_TO_SHARED_FOLDER_NOT_SUPPORTED_DISPLAY, 68, Category.CATEGORY_USER_INPUT),
    /**
     * You can not use different private flags for one element of a recurring appointment.
     */
    RECURRING_EXCEPTION_PRIVATE_FLAG(OXCalendarExceptionCodes.RECURRING_EXCEPTION_PRIVATE_FLAG_MSG, OXCalendarExceptionMessage.RECURRING_EXCEPTION_PRIVATE_FLAG_MSG, 69, Category.CATEGORY_USER_INPUT),
    /**
     * You can not use the private flag in a non private folder.
     */
    PIVATE_FLAG_ONLY_IN_PRIVATE_FOLDER(OXCalendarExceptionCodes.PIVATE_FLAG_ONLY_IN_PRIVATE_FOLDER_MSG, OXCalendarExceptionMessage.PIVATE_FLAG_ONLY_IN_PRIVATE_FOLDER_MSG, 70, Category.CATEGORY_USER_INPUT),
    /**
     * Bad character in field %1$s. Error: %2$s
     */
    INVALID_CHARACTER(OXCalendarExceptionCodes.INVALID_CHARACTER_MSG, OXCalendarExceptionMessage.INVALID_CHARACTER_DISPLAY, 71, Category.CATEGORY_USER_INPUT),
    /**
     * Some data exceeds a field limit. Please shorten the input(s) for affected field(s).
     */
    TRUNCATED_SQL_ERROR(OXCalendarExceptionCodes.TRUNCATED_SQL_ERROR_MSG, OXExceptionStrings.SQL_ERROR_MSG, 72, Category.CATEGORY_TRUNCATED),
    /**
     * Calendar calculation requires a proper defined time zone.
     */
    TIMEZONE_MISSING(OXCalendarExceptionCodes.TIMEZONE_MISSING_MSG, 73, Category.CATEGORY_ERROR),
    /**
     * Recurrence position %1$s does not exist
     */
    UNKNOWN_RECURRENCE_POSITION(OXCalendarExceptionCodes.UNKNOWN_RECURRENCE_POSITION_MSG, 74, Category.CATEGORY_USER_INPUT),
    /**
     * One or more exception dates are not contained in recurring appointment
     */
    FOREIGN_EXCEPTION_DATE(OXCalendarExceptionCodes.FOREIGN_EXCEPTION_DATE_MSG, 75, Category.CATEGORY_USER_INPUT),
    /**
     * Appointment's owner must not be removed from participants
     */
    OWNER_REMOVAL_EXCEPTION(OXCalendarExceptionCodes.OWNER_REMOVAL_EXCEPTION_MSG, 76, Category.CATEGORY_PERMISSION_DENIED),
    /**
     * An event error occurred: %1$s
     */
    EVENT_ERROR(OXCalendarExceptionCodes.EVENT_ERROR_MSG, 77, Category.CATEGORY_ERROR),
    /**
     * Value %1$d exceeds max. supported value of %2$d.
     */
    RECURRING_VALUE_CONSTRAINT(OXCalendarExceptionCodes.RECURRING_VALUE_CONSTRAINT_MSG, 78, Category.CATEGORY_USER_INPUT),
    /**
     * Unable to calculate first occurrence of appointment %1$d.
     */
    UNABLE_TO_CALCULATE_FIRST_RECURRING(OXCalendarExceptionCodes.UNABLE_TO_CALCULATE_FIRST_RECURRING_MSG, 79, Category.CATEGORY_ERROR),
    /**
     * The recurrence pattern is too complex. Giving up.
     */
    RECURRENCE_PATTERN_TOO_COMPLEX(OXCalendarExceptionCodes.RECURRENCE_PATTERN_TOO_COMPLEX_MSG, 80, Category.CATEGORY_ERROR),
    /**
     * Unknown name-value-pair in recurrence string: %1$s=%2$s
     */
    UNKNOWN_NVP_IN_REC_STR(OXCalendarExceptionCodes.UNKNOWN_NVP_IN_REC_STR_MSG, 81, Category.CATEGORY_ERROR),
    /**
     * Changing recurrence type of a change exception denied
     */
    INVALID_RECURRENCE_TYPE_CHANGE(OXCalendarExceptionCodes.INVALID_RECURRENCE_TYPE_CHANGE_MSG, 82, Category.CATEGORY_USER_INPUT),
    /**
     * Changing recurrence position of a change exception denied
     */
    INVALID_RECURRENCE_POSITION_CHANGE(OXCalendarExceptionCodes.INVALID_RECURRENCE_POSITION_CHANGE_MSG, 83, Category.CATEGORY_USER_INPUT),
    /**
     * User changing the appointment is missing.
     */
    MODIFIED_BY_MISSING(OXCalendarExceptionCodes.MODIFIED_BY_MISSING_MSG, 84, Category.CATEGORY_ERROR),
    /**
     * Callbacks threw exceptions
     */
    CALLBACK_EXCEPTIONS(OXCalendarExceptionCodes.CALLBACK_EXCEPTIONS_MSG, 85, Category.CATEGORY_ERROR),
    /**
     * Series end is before start date.
     */
    UNTIL_BEFORE_START_DATE(OXCalendarExceptionCodes.UNTIL_BEFORE_START_DATE_MSG, OXCalendarExceptionMessage.UNTIL_BEFORE_START_DATE_MSG, 86, Category.CATEGORY_USER_INPUT),
    /**
     * Incomplete recurring information: Missing interval.
     */
    INCOMPLETE_REC_INFOS_INTERVAL(OXCalendarExceptionCodes.INCOMPLETE_REC_INFOS_INTERVAL_MSG, 87, Category.CATEGORY_USER_INPUT),
    /**
     * Incomplete recurring information: Missing series end date or number of occurrences.
     */
    INCOMPLETE_REC_INFOS_UNTIL_OR_OCCUR(OXCalendarExceptionCodes.INCOMPLETE_REC_INFOS_UNTIL_OR_OCCUR_MSG, 88, Category.CATEGORY_USER_INPUT),
    /**
     * Incomplete recurring information: Missing weekday.
     */
    INCOMPLETE_REC_INFOS_WEEKDAY(OXCalendarExceptionCodes.INCOMPLETE_REC_INFOS_WEEKDAY_MSG, 89, Category.CATEGORY_USER_INPUT),
    /**
     * Incomplete recurring information: Missing day in month.
     */
    INCOMPLETE_REC_INFOS_MONTHDAY(OXCalendarExceptionCodes.INCOMPLETE_REC_INFOS_MONTHDAY_MSG, 90, Category.CATEGORY_USER_INPUT),
    /**
     * Incomplete recurring information: Missing month.
     */
    INCOMPLETE_REC_INFOS_MONTH(OXCalendarExceptionCodes.INCOMPLETE_REC_INFOS_MONTH_MSG, 91, Category.CATEGORY_USER_INPUT),
    /**
     * Incomplete recurring information: Missing recurrence type.
     */
    INCOMPLETE_REC_INFOS_TYPE(OXCalendarExceptionCodes.INCOMPLETE_REC_INFOS_TYPE_MSG, 92, Category.CATEGORY_USER_INPUT),
    /**
     * Move of recurring appointments is not supported
     */
    RECURRING_FOLDER_MOVE(OXCalendarExceptionCodes.RECURRING_FOLDER_MOVE_MSG, OXCalendarExceptionMessage.RECURRING_FOLDER_MOVE_MSG, 93, Category.CATEGORY_USER_INPUT),
    /**
     * In order to accomplish the search, %1$d or more characters are required.
     */
    PATTERN_TOO_SHORT(OXCalendarExceptionCodes.PATTERN_TOO_SHORT_MSG, 94, Category.CATEGORY_USER_INPUT),
    /**
     * Redundant information.
     */
    REDUNDANT_UNTIL_OCCURRENCES(OXCalendarExceptionCodes.REDUNDANT_UNTIL_OCCURRENCES_MSG, 95, Category.CATEGORY_USER_INPUT),
    /**
     * Unnecessary recurrence Information for Type "no".
     */
    UNNECESSARY_RECURRENCE_INFORMATION_NO(OXCalendarExceptionCodes.UNNECESSARY_RECURRENCE_INFORMATION_NO_MSG, 96, Category.CATEGORY_USER_INPUT),
    /**
     * Unnecessary recurrence information.
     */
    UNNECESSARY_RECURRENCE_INFORMATION(OXCalendarExceptionCodes.UNNECESSARY_RECURRENCE_INFORMATION_MSG, 97, Category.CATEGORY_USER_INPUT),
    /**
     * The recurring appointment has been deleted or is outside of the range of the recurrence.
     */
    UNABLE_TO_CALCULATE_POSITION(OXCalendarExceptionCodes.UNABLE_TO_CALCULATE_POSITION_MSG, 98, Category.CATEGORY_USER_INPUT),
    /**
     * Changing an exception into a series is not supported.
     */
    CHANGE_EXCEPTION_TO_RECURRENCE(OXCalendarExceptionCodes.CHANGE_EXCEPTION_TO_RECURRENCE_MSG, 99, Category.CATEGORY_USER_INPUT),
    /**
     * Cannot insert appointment (%1$s). An appointment with the unique identifier (%2$s) already exists.
     */
    APPOINTMENT_UID_ALREDY_EXISTS(OXCalendarExceptionCodes.APPOINTMENT_UID_ALREDY_EXISTS_MSG, OXCalendarExceptionMessage.UID_ALREDY_EXISTS_MSG, 100, Category.CATEGORY_USER_INPUT),
    /**
     * Cannot insert task (%1$s). A task with the unique identifier (%2$s) already exists.
     */
    TASK_UID_ALREDY_EXISTS(OXCalendarExceptionCodes.TASK_UID_ALREDY_EXISTS_MSG, OXCalendarExceptionMessage.TASK_UID_ALREDY_EXISTS_MSG, 100, Category.CATEGORY_USER_INPUT),
    /**
     * SQL Problem.
     */
    SQL_ERROR(OXCalendarExceptionCodes.SQL_ERROR_MSG, OXExceptionStrings.SQL_ERROR_MSG, 101, Category.CATEGORY_ERROR),
    /**
     * Wrong number of rows changed. Expected %1$d but was %2$d.
     */
    WRONG_ROW_COUNT(OXCalendarExceptionCodes.WRONG_ROW_COUNT_MSG, OXExceptionStrings.SQL_ERROR_MSG, 102, Category.CATEGORY_WARNING),
    /**
     * Unable to find a participant for a specified object.
     */
    COULD_NOT_FIND_PARTICIPANT(OXCalendarExceptionCodes.COULD_NOT_FIND_PARTICIPANT_MSG, 103, Category.CATEGORY_USER_INPUT),
    /**
     * Was not able to calculate next upcoming reminder for series appointment %2$d in context %1$d.
     */
    NEXT_REMINDER_FAILED(OXCalendarExceptionCodes.NEXT_REMINDER_FAILED_MSG, 104, Category.CATEGORY_ERROR),
    /**
     * Invalid sequence value: %1$d
     */
    INVALID_SEQUENCE(OXCalendarExceptionCodes.INVALID_SEQUENCE_MSG, 105, Category.CATEGORY_USER_INPUT),
    /**
     * An external participant with email address %1$s is already contained. Please remove duplicate participant and retry.
     */
    DUPLICATE_EXTERNAL_PARTICIPANT(OXCalendarExceptionCodes.DUPLICATE_EXTERNAL_PARTICIPANT_MSG, OXCalendarExceptionMessage.DUPLICATE_EXTERNAL_PARTICIPANT_MSG, 106, Category.CATEGORY_USER_INPUT),
    /**
     * Appointment is not a recurring appointment.
     */
    NO_RECCURENCE(OXCalendarExceptionCodes.NO_RECCURENCE_MSG, 107, Category.CATEGORY_USER_INPUT),
    /**
     * Unexpected SQL Error!
     */
    CALENDAR_SQL_ERROR_RETRY(OXCalendarExceptionCodes.CALENDAR_SQL_ERROR_MSG, 108, Category.CATEGORY_TRY_AGAIN),
    /**
     * Unknown recurrence type: %1$d
     */
    UNKNOWN_RECURRENCE_TYPE(OXCalendarExceptionCodes.UNKNOWN_RECURRENCE_TYPE_MSG, 109, Category.CATEGORY_USER_INPUT),
    /**
     * The value 'null' is not allowed for the field \"%s\".
     */
    FIELD_NULL_VALUE(OXCalendarExceptionCodes.FIELD_NULL_VALUE_MSG, 110, Category.CATEGORY_ERROR),
    /**
     * Sequence number is outdated.
     */
    OUTDATED_SEQUENCE(OXCalendarExceptionMessage.OUTDATED_SEQUENCE, OXCalendarExceptionMessage.OUTDATED_SEQUENCE, 111, Category.CATEGORY_USER_INPUT),

    ;

    private static final String CFO_NOT_INITIALIZIED_MSG = "CalendarFolderObject not initialized.";

    private static final String NOT_YET_SUPPORTED_MSG = "Not yet supported!";

    private static final String NO_SHARED_FOLDER_OWNER_MSG = "Shared folder owner not given.";

    private static final String FOLDER_TYPE_UNRESOLVEABLE_MSG = "Folder type unresolvable.";

    private static final String CALENDAR_SQL_ERROR_MSG = "Unexpected SQL error.";

    private static final String LAST_MODIFIED_IS_NULL_MSG = "clientLastModified IS NULL. Abort action.";

    private static final String UNEXPECTED_EXCEPTION_MSG = "Unexpected exception %d.";

    private static final String EXTERNAL_PARTICIPANTS_MANDATORY_FIELD_MSG = "Mandatory field mail address for external participants";

    private static final String UPDATE_WITHOUT_PARTICIPANTS_MSG = "Would create an object without participants";

    private static final String UPDATE_USER_SHARED_MISMATCH_MSG = "Folder type \"SHARED\" is not allowed in this situation.";

    private static final String RECURRING_UNEXPECTED_DELETE_STATE_MSG = "Unexpected state for deleting a virtual appointment (exception). uid:oid:position %d:%d:%d";

    private static final String ERROR_SESSIONOBJECT_IS_NULL_MSG = "SessionObject not initialized";

    private static final String NO_PERMISSION_MSG = "You do not have the appropriate permissions to read appointments in folder %1$d.";

    private static final String INSERT_WITH_OBJECT_ID_MSG = "Insert expected but the object id is already given. Aborting action...";

    private static final String UPDATE_WITHOUT_OBJECT_ID_MSG = "Update expected but no object id is given. Aborting action...";

    private static final String FOLDER_DELETE_INVALID_REQUEST_MSG = "Invalid request. Folder is shared.";

    private static final String FOLDER_FOREIGN_INVALID_REQUEST_MSG = "Invalid request. Folder is shared.";

    private static final String FOLDER_IS_EMPTY_INVALID_REQUEST_MSG = "Invalid request. Folder is shared.";

    private static final String FREE_BUSY_UNSUPPOTED_TYPE_MSG = "Unsupported type detected : %d";

    private static final String END_DATE_BEFORE_START_DATE_MSG = "End date is before start date.";

    private static final String UNSUPPORTED_LABEL_MSG = "Unsupported label value %d";

    private static final String PRIVATE_FLAG_IN_PRIVATE_FOLDER_MSG = "Private flag is only allowed inside of a private folder.";

    private static final String PRIVATE_FLAG_AND_PARTICIPANTS_MSG = "Appointments marked as 'Private' can only be scheduled for the respective user (or owner of the calendar). Please remove additional participants or remove the \"Private\" mark.";

    private static final String UNSUPPORTED_PRIVATE_FLAG_MSG = "Unsupported private flag value %d";

    private static final String UNSUPPORTED_SHOWN_AS_MSG = "Unsupported \"shown as\" value %d.";

    private static final String FIELD_NULL_VALUE_MSG = "The value 'null' is not allowed for the field \"%s\".";

    private static final String MANDATORY_FIELD_START_DATE_MSG = "Required  value \"Start Date\" was not supplied.";

    private static final String MANDATORY_FIELD_END_DATE_MSG = "Required value \"End Date\" was not supplied.";

    private static final String MANDATORY_FIELD_TITLE_MSG = "Required value \"Title\" was not supplied.";

    private static final String UNABLE_TO_CALCULATE_RECURRING_POSITION_MSG = "Unable to create exception, recurring position (%d) can not be calculated.";

    private static final String INTERNAL_USER_PARTICIPANT_CHECK_1_MSG = "Got an UserParticipant object with an identifier < 1 Identifier:Folder_Type = %d:%d";

    private static final String INTERNAL_USER_PARTICIPANT_CHECK_2_MSG = "Got an UserParticipant object with a private folder id < 1 : Identifier = %d";

    private static final String INTERNAL_USER_PARTICIPANT_CHECK_3_MSG = "Got an UserParticipant object with a private folder id in a public folder : Identifier = %d";

    private static final String MOVE_NOT_SUPPORTED_MSG = "Move not supported: Cannot move an appointment from folder %d to folder %d.";

    private static final String SHARED_FOLDER_MOVE_NOT_SUPPORTED_MSG = "Move not allowed from shared folders.";

    private static final String CONTEXT_NOT_SET_MSG = "Calendar operation: Context not set.";

    private static final String NO_PERMISSIONS_TO_ATTACH_DETACH_MSG = "Insufficient rights to add/remove an attachment to/from this folder.";

    private static final String NO_PERMISSIONS_TO_READ_MSG = "Insufficient read rights for this folder.";

    private static final String UNABLE_TO_CALCULATE_RECURRING_POSITION_NO_INPUT_MSG = "Cannot resolve recurrence position because neither the recurring position nor the recurring date position is known.";

    private static final String RECURRING_MISSING_START_DATE_MSG = "Missing start date, unable to calculate recurrence.";

    private static final String RECURRING_MISSING_DAILY_INTERVAL_MSG = "Missing or wrong interval value: %d";

    private static final String RECURRING_MISSING_WEEKLY_INTERVAL_MSG = "Missing or wrong interval value: %d";

    private static final String RECURRING_MISSING_MONTLY_INTERVAL_MSG = "Missing or wrong DayInMonth value: %d";

    private static final String RECURRING_MISSING_MONTLY_INTERVAL_2_MSG = "Missing or wrong month value: %d";

    private static final String RECURRING_MISSING_MONTLY_DAY_MSG = "Missing or wrong day value: %d";

    private static final String RECURRING_MISSING_MONTLY_DAY_2_MSG = "Missing or wrong DayInMonth value: %d";

    private static final String RECURRING_MISSING_YEARLY_INTERVAL_MSG = "Missing or wrong DayInMonth value: %d";

    private static final String RECURRING_MISSING_YEARLY_DAY_MSG = "Missing or wrong day value: %d";

    private static final String RECURRING_MISSING_YEARLY_TYPE_MSG = "Missing or wrong day_or_type : %d";

    private static final String RECURRING_MISSING_YEARLY_INTERVAL_2_MSG = "Missing or wrong interval value: %d";

    private static final String UNABLE_TO_REMOVE_PARTICIPANT_MSG = "Unable to remove participant %d";

    private static final String UNABLE_TO_REMOVE_PARTICIPANT_2_MSG = "Unable to remove participant because this participant is the last remaining";

    private static final String UNSUPPORTED_ACTION_TYPE_MSG = "Action type not supported : %d";

    private static final String SEARCH_ITERATOR_NULL_MSG = "SearchIterator NULL";

    private static final String NON_CALENDAR_FOLDER_MSG = "Folder is not of type Calendar.";

    private static final String RECURRING_MISSING_OR_WRONG_VALUE_INTERVAL_MSG = "The required \"interval\" value is missing or wrong";

    private static final String RECURRING_MISSING_OR_WRONG_VALUE_DAYS_MSG = "The required \"days\"  value is missing or wrong: %d";

    private static final String PRIVATE_MOVE_TO_PUBLIC_MSG = "Moving an appointment to a public folder flagged as private is not allowed.";

    private static final String LOAD_PERMISSION_EXCEPTION_1_MSG = "You do not have the appropriate permissions to modify this object.";

    private static final String LOAD_PERMISSION_EXCEPTION_2_MSG = "Got the wrong folder identification. You do not have the appropriate permissions to modify this object.";

    private static final String LOAD_PERMISSION_EXCEPTION_3_MSG = "Got the wrong shared folder identification. You do not have the appropriate permissions to modify this object.";

    private static final String LOAD_PERMISSION_EXCEPTION_4_MSG = "You do not have the appropriate permissions to move this object.";

    private static final String LOAD_PERMISSION_EXCEPTION_5_MSG = "You do not have the appropriate permissions to read this object %1$d.";

    private static final String LOAD_PERMISSION_EXCEPTION_6_MSG = "You do not have the appropriate permissions to create an object.";

    private static final String RECURRING_MISSING_YEARLY_MONTH_MSG = "Missing or wrong month value: %d";

    private static final String RECURRING_ALREADY_EXCEPTION_MSG = "You are trying to create a new recurring appointment from an exception. This is not possible.";

    private static final String RECURRING_EXCEPTION_MOVE_EXCEPTION_MSG = "Moving an instance of a recurring appointment into another folder is not allowed.";

    private static final String UPDATE_EXCEPTION_MSG = "A database update exception occurred.";

    private static final String MOVE_TO_SHARED_FOLDER_NOT_SUPPORTED_MSG = "Move to a shared folder not allowed if the private flag is set.";

    private static final String RECURRING_EXCEPTION_PRIVATE_FLAG_MSG = "You can not use different private flags for one element of a recurring appointment.";

    private static final String PIVATE_FLAG_ONLY_IN_PRIVATE_FOLDER_MSG = "You can not use the private flag in a non private folder.";

    private static final String INVALID_CHARACTER_MSG = "Bad character in field %1$s. Error: %2$s";

    private static final String TRUNCATED_SQL_ERROR_MSG = "Some data exceeds a field limit. Please shorten the input(s) for affected field(s).";

    private static final String TIMEZONE_MISSING_MSG = "Calendar calculation requires a properly defined time zone.";

    private static final String UNKNOWN_RECURRENCE_POSITION_MSG = "Recurrence position %1$s does not exist";

    private static final String FOREIGN_EXCEPTION_DATE_MSG = "One or more exception dates are not contained in recurring appointment";

    private static final String OWNER_REMOVAL_EXCEPTION_MSG = "Appointment's owner must not be removed from participants";

    private static final String EVENT_ERROR_MSG = "An event error occurred: %1$s";

    private static final String RECURRING_VALUE_CONSTRAINT_MSG = "Value %1$d exceeds max. supported value of %2$d.";

    private static final String UNABLE_TO_CALCULATE_FIRST_RECURRING_MSG = "Unable to calculate first occurrence of appointment %1$d.";

    private static final String RECURRENCE_PATTERN_TOO_COMPLEX_MSG = "The recurrence pattern is too complex. Giving up.";

    private static final String UNKNOWN_NVP_IN_REC_STR_MSG = "Unknown name-value-pair in recurrence string: %1$s=%2$s";

    private static final String INVALID_RECURRENCE_TYPE_CHANGE_MSG = "Changing recurrence type of a change exception denied";

    private static final String INVALID_RECURRENCE_POSITION_CHANGE_MSG = "Changing recurrence position of a change exception denied.";

    private static final String MODIFIED_BY_MISSING_MSG = "User changing the appointment is missing.";

    private static final String CALLBACK_EXCEPTIONS_MSG = "Some callbacks threw exceptions: %s";

    private static final String UNTIL_BEFORE_START_DATE_MSG = "Series end is before start date.";

    private static final String INCOMPLETE_REC_INFOS_INTERVAL_MSG = "Incomplete recurring information: missing interval.";

    private static final String INCOMPLETE_REC_INFOS_UNTIL_OR_OCCUR_MSG = "Incomplete recurring information: missing series end date or number of occurrences.";

    private static final String INCOMPLETE_REC_INFOS_WEEKDAY_MSG = "Incomplete recurring information: missing weekday.";

    private static final String INCOMPLETE_REC_INFOS_MONTHDAY_MSG = "Incomplete recurring information: missing day in month.";

    private static final String INCOMPLETE_REC_INFOS_MONTH_MSG = "Incomplete recurring information: missing month.";

    private static final String INCOMPLETE_REC_INFOS_TYPE_MSG = "Incomplete recurring information: missing recurrence type.";

    private static final String RECURRING_FOLDER_MOVE_MSG = "Moving a recurring appointment to another folder is not supported.";

    private static final String PATTERN_TOO_SHORT_MSG = "In order to accomplish the search, %1$d or more characters are required.";

    private static final String REDUNDANT_UNTIL_OCCURRENCES_MSG = "Redundant information for Until and occurrences.";

    private static final String UNNECESSARY_RECURRENCE_INFORMATION_NO_MSG = "Unnecessary recurrence information for recurrence type \"no recurrence\".";

    private static final String UNNECESSARY_RECURRENCE_INFORMATION_MSG = "Unnecessary recurrence information (%1$s) for type %2$s";

    private static final String UNABLE_TO_CALCULATE_POSITION_MSG = "The recurring appointment has been deleted or is outside of the range of the recurrence.";

    private static final String CHANGE_EXCEPTION_TO_RECURRENCE_MSG = "Changing an exception into a series is not supported.";

    private static final String APPOINTMENT_UID_ALREDY_EXISTS_MSG = "Cannot insert appointment (%1$s). An appointment with the unique identifier (%2$s) already exists.";

    private static final String TASK_UID_ALREDY_EXISTS_MSG = "Cannot insert task (%1$s). A task with the unique identifier (%2$s) already exists.";

    private static final String SQL_ERROR_MSG = "SQL Problem.";

    private static final String WRONG_ROW_COUNT_MSG = "Wrong number of rows changed. Expected %1$d but was %2$d.";

    private static final String COULD_NOT_FIND_PARTICIPANT_MSG = "Could not find participant for this object.";

    private static final String NEXT_REMINDER_FAILED_MSG = "Was not able to calculate next upcoming reminder for series appointment %2$d in context %1$d.";

    private static final String INVALID_SEQUENCE_MSG = "Invalid sequence value: %1$d";

    private static final String DUPLICATE_EXTERNAL_PARTICIPANT_MSG = "An external participant with the E-Mail address %1$s is already included. Please remove participant duplicate and retry.";

    private static final String NO_RECCURENCE_MSG = "Appointment is not a recurring appointment.";

    private static final String UNKNOWN_RECURRENCE_TYPE_MSG = "Unknown recurrence type: %1$d";

    /**
     * Message of the exception.
     */
    private final String message;

    /**
     * The message intended for being displayed to the user.
     */
    private String displayMessage;

    /**
     * Category of the exception.
     */
    private final Category category;

    /**
     * Detail number of the exception.
     */
    private final int detailNumber;

    /**
     * Default constructor.
     *
     * @param message message.
     * @param category category.
     * @param detailNumber detail number.
     */
    private OXCalendarExceptionCodes(final String message, final int detailNumber, final Category category) {
        this(message, OXExceptionStrings.MESSAGE, detailNumber, category);
        if (category == Category.CATEGORY_USER_INPUT) {
            this.displayMessage = message;
        }
    }

    /**
     * Default constructor.
     *
     * @param message message.
     * @param displayMessage display message
     * @param category category.
     * @param detailNumber detail number.
     */
    private OXCalendarExceptionCodes(final String message, String displayMessage, final int detailNumber, final Category category) {
        this.message = message;
        this.displayMessage = displayMessage == null ? OXExceptionStrings.MESSAGE : displayMessage;
        this.category = category;
        this.detailNumber = detailNumber;
    }

    @Override
    public String getPrefix() {
        return "APP";
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public int getNumber() {
        return detailNumber;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getDisplayMessage() {
        return displayMessage;
    }

    @Override
    public boolean equals(final OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @return The newly created {@link OXException} instance
     */
    public OXException create() {
        return specials(OXExceptionFactory.getInstance().create(this, new Object[0]));
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Object... args) {
        return specials(OXExceptionFactory.getInstance().create(this, (Throwable) null, args));
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param cause The optional initial cause
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Throwable cause, final Object... args) {
        return specials(OXExceptionFactory.getInstance().create(this, cause, args));
    }

    private OXException specials(OXException exc) {
        if (exc.getCategories().contains(Category.CATEGORY_PERMISSION_DENIED)) {
            exc.setGeneric(Generic.NO_PERMISSION);
        }
        return exc;
    }
}
