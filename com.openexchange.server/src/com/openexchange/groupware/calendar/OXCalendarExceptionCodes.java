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

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

/**
 * The calendar error code enumeration.
 */
public enum OXCalendarExceptionCodes implements OXExceptionCode {
    CFO_NOT_INITIALIZIED(OXCalendarExceptionMessage.CFO_NOT_INITIALIZIED_MSG, 1, Category.CATEGORY_ERROR),
    NOT_YET_SUPPORTED(OXCalendarExceptionMessage.NOT_YET_SUPPORTED_MSG, 2, Category.CATEGORY_ERROR),
    NO_SHARED_FOLDER_OWNER(OXCalendarExceptionMessage.NO_SHARED_FOLDER_OWNER_MSG, 3, Category.CATEGORY_ERROR),
    FOLDER_TYPE_UNRESOLVEABLE(OXCalendarExceptionMessage.FOLDER_TYPE_UNRESOLVEABLE_MSG, 4, Category.CATEGORY_ERROR),
    /**
     * Unexpected SQL Error!
     */
    CALENDAR_SQL_ERROR(OXCalendarExceptionMessage.CALENDAR_SQL_ERROR_MSG, 5, Category.CATEGORY_ERROR),
    /**
     * TODO remove this exception. The AJAX interface should already check for a missing last modified.
     */
    LAST_MODIFIED_IS_NULL(OXCalendarExceptionMessage.LAST_MODIFIED_IS_NULL_MSG, 6, Category.CATEGORY_ERROR),
    /**
     * Unexpected exception %d!
     */
    UNEXPECTED_EXCEPTION(OXCalendarExceptionMessage.UNEXPECTED_EXCEPTION_MSG, 7, Category.CATEGORY_ERROR),
    EXTERNAL_PARTICIPANTS_MANDATORY_FIELD(OXCalendarExceptionMessage.EXTERNAL_PARTICIPANTS_MANDATORY_FIELD_MSG, 8, Category.CATEGORY_USER_INPUT),
    UPDATE_WITHOUT_PARTICIPANTS(OXCalendarExceptionMessage.UPDATE_WITHOUT_PARTICIPANTS_MSG, 9, Category.CATEGORY_ERROR),
    UPDATE_USER_SHARED_MISMATCH(OXCalendarExceptionMessage.UPDATE_USER_SHARED_MISMATCH_MSG, 10, Category.CATEGORY_USER_INPUT),
    RECURRING_UNEXPECTED_DELETE_STATE(OXCalendarExceptionMessage.RECURRING_UNEXPECTED_DELETE_STATE_MSG, 11, Category.CATEGORY_ERROR),
    ERROR_SESSIONOBJECT_IS_NULL(OXCalendarExceptionMessage.ERROR_SESSIONOBJECT_IS_NULL_MSG, 12, Category.CATEGORY_ERROR),
    NO_PERMISSION(OXCalendarExceptionMessage.NO_PERMISSION_MSG, 13, Category.CATEGORY_PERMISSION_DENIED),
    INSERT_WITH_OBJECT_ID(OXCalendarExceptionMessage.INSERT_WITH_OBJECT_ID_MSG, 14, Category.CATEGORY_ERROR),
    UPDATE_WITHOUT_OBJECT_ID(OXCalendarExceptionMessage.UPDATE_WITHOUT_OBJECT_ID_MSG, 15, Category.CATEGORY_ERROR),
    FOLDER_DELETE_INVALID_REQUEST(OXCalendarExceptionMessage.FOLDER_DELETE_INVALID_REQUEST_MSG, 16, Category.CATEGORY_ERROR),
    FOLDER_FOREIGN_INVALID_REQUEST(OXCalendarExceptionMessage.FOLDER_FOREIGN_INVALID_REQUEST_MSG, 17, Category.CATEGORY_ERROR),
    FOLDER_IS_EMPTY_INVALID_REQUEST(OXCalendarExceptionMessage.FOLDER_IS_EMPTY_INVALID_REQUEST_MSG, 18, Category.CATEGORY_ERROR),
    FREE_BUSY_UNSUPPOTED_TYPE(OXCalendarExceptionMessage.FREE_BUSY_UNSUPPOTED_TYPE_MSG, 19, Category.CATEGORY_ERROR),
    END_DATE_BEFORE_START_DATE(OXCalendarExceptionMessage.END_DATE_BEFORE_START_DATE_MSG, 20, Category.CATEGORY_USER_INPUT),
    UNSUPPORTED_LABEL(OXCalendarExceptionMessage.UNSUPPORTED_LABEL_MSG, 21, Category.CATEGORY_USER_INPUT),
    PRIVATE_FLAG_IN_PRIVATE_FOLDER(OXCalendarExceptionMessage.PRIVATE_FLAG_IN_PRIVATE_FOLDER_MSG, 22, Category.CATEGORY_USER_INPUT),
    PRIVATE_FLAG_AND_PARTICIPANTS(OXCalendarExceptionMessage.PRIVATE_FLAG_AND_PARTICIPANTS_MSG, 23, Category.CATEGORY_USER_INPUT),
    UNSUPPORTED_PRIVATE_FLAG(OXCalendarExceptionMessage.UNSUPPORTED_PRIVATE_FLAG_MSG, 24, Category.CATEGORY_USER_INPUT),
    UNSUPPORTED_SHOWN_AS(OXCalendarExceptionMessage.UNSUPPORTED_SHOWN_AS_MSG, 25, Category.CATEGORY_USER_INPUT),
    MANDATORY_FIELD_START_DATE(OXCalendarExceptionMessage.MANDATORY_FIELD_START_DATE_MSG, 26, Category.CATEGORY_USER_INPUT),
    MANDATORY_FIELD_END_DATE(OXCalendarExceptionMessage.MANDATORY_FIELD_END_DATE_MSG, 27, Category.CATEGORY_USER_INPUT),
    MANDATORY_FIELD_TITLE(OXCalendarExceptionMessage.MANDATORY_FIELD_TITLE_MSG, 28, Category.CATEGORY_USER_INPUT),
    UNABLE_TO_CALCULATE_RECURRING_POSITION(OXCalendarExceptionMessage.UNABLE_TO_CALCULATE_RECURRING_POSITION_MSG, 29, Category.CATEGORY_USER_INPUT),
    INTERNAL_USER_PARTICIPANT_CHECK_1(OXCalendarExceptionMessage.INTERNAL_USER_PARTICIPANT_CHECK_1_MSG, 30, Category.CATEGORY_ERROR),
    INTERNAL_USER_PARTICIPANT_CHECK_2(OXCalendarExceptionMessage.INTERNAL_USER_PARTICIPANT_CHECK_2_MSG, 31, Category.CATEGORY_USER_INPUT),
    INTERNAL_USER_PARTICIPANT_CHECK_3(OXCalendarExceptionMessage.INTERNAL_USER_PARTICIPANT_CHECK_3_MSG, 32, Category.CATEGORY_USER_INPUT),
    MOVE_NOT_SUPPORTED(OXCalendarExceptionMessage.MOVE_NOT_SUPPORTED_MSG, 33, Category.CATEGORY_ERROR),
    SHARED_FOLDER_MOVE_NOT_SUPPORTED(OXCalendarExceptionMessage.SHARED_FOLDER_MOVE_NOT_SUPPORTED_MSG, 34, Category.CATEGORY_ERROR),
    CONTEXT_NOT_SET(OXCalendarExceptionMessage.CONTEXT_NOT_SET_MSG, 35, Category.CATEGORY_ERROR),
    NO_PERMISSIONS_TO_ATTACH_DETACH(OXCalendarExceptionMessage.NO_PERMISSIONS_TO_ATTACH_DETACH_MSG, 36, Category.CATEGORY_PERMISSION_DENIED),
    NO_PERMISSIONS_TO_READ(OXCalendarExceptionMessage.NO_PERMISSIONS_TO_READ_MSG, 37, Category.CATEGORY_PERMISSION_DENIED),
    /**
     * Can not resolve recurrence position because we got neither the recurring position nor a recurring date position
     */
    UNABLE_TO_CALCULATE_RECURRING_POSITION_NO_INPUT(OXCalendarExceptionMessage.UNABLE_TO_CALCULATE_RECURRING_POSITION_NO_INPUT_MSG, 38, Category.CATEGORY_ERROR),
    /**
     * Missing start date, unable to calculate recurrence.
     */
    RECURRING_MISSING_START_DATE(OXCalendarExceptionMessage.RECURRING_MISSING_START_DATE_MSG, 39, Category.CATEGORY_ERROR),
    RECURRING_MISSING_DAILY_INTERVAL(OXCalendarExceptionMessage.RECURRING_MISSING_DAILY_INTERVAL_MSG, 40, Category.CATEGORY_USER_INPUT),
    RECURRING_MISSING_WEEKLY_INTERVAL(OXCalendarExceptionMessage.RECURRING_MISSING_WEEKLY_INTERVAL_MSG, 41, Category.CATEGORY_USER_INPUT),
    /**
     * Missing or wrong value DayInMonth : %d
     */
    RECURRING_MISSING_MONTLY_INTERVAL(OXCalendarExceptionMessage.RECURRING_MISSING_MONTLY_INTERVAL_MSG, 42, Category.CATEGORY_USER_INPUT),
    RECURRING_MISSING_MONTLY_INTERVAL_2(OXCalendarExceptionMessage.RECURRING_MISSING_MONTLY_INTERVAL_2_MSG, 43, Category.CATEGORY_USER_INPUT),
    RECURRING_MISSING_MONTLY_DAY(OXCalendarExceptionMessage.RECURRING_MISSING_MONTLY_DAY_MSG, 44, Category.CATEGORY_USER_INPUT),
    /**
     * Missing or wrong DayInMonth value: %d
     */
    RECURRING_MISSING_MONTLY_DAY_2(OXCalendarExceptionMessage.RECURRING_MISSING_MONTLY_DAY_2_MSG, 45, Category.CATEGORY_USER_INPUT),
    RECURRING_MISSING_YEARLY_INTERVAL(OXCalendarExceptionMessage.RECURRING_MISSING_YEARLY_INTERVAL_MSG, 46, Category.CATEGORY_USER_INPUT),
    RECURRING_MISSING_YEARLY_DAY(OXCalendarExceptionMessage.RECURRING_MISSING_YEARLY_DAY_MSG, 47, Category.CATEGORY_USER_INPUT),
    RECURRING_MISSING_YEARLY_TYPE(OXCalendarExceptionMessage.RECURRING_MISSING_YEARLY_TYPE_MSG, 48, Category.CATEGORY_USER_INPUT),
    RECURRING_MISSING_YEARLY_INTERVAL_2(OXCalendarExceptionMessage.RECURRING_MISSING_YEARLY_INTERVAL_2_MSG, 49, Category.CATEGORY_USER_INPUT),
    UNABLE_TO_REMOVE_PARTICIPANT(OXCalendarExceptionMessage.UNABLE_TO_REMOVE_PARTICIPANT_MSG, 50, Category.CATEGORY_ERROR),
    UNABLE_TO_REMOVE_PARTICIPANT_2(OXCalendarExceptionMessage.UNABLE_TO_REMOVE_PARTICIPANT_2_MSG, 51, Category.CATEGORY_USER_INPUT),
    UNSUPPORTED_ACTION_TYPE(OXCalendarExceptionMessage.UNSUPPORTED_ACTION_TYPE_MSG, 52, Category.CATEGORY_ERROR),
    SEARCH_ITERATOR_NULL(OXCalendarExceptionMessage.SEARCH_ITERATOR_NULL_MSG, 53, Category.CATEGORY_ERROR),
    NON_CALENDAR_FOLDER(OXCalendarExceptionMessage.NON_CALENDAR_FOLDER_MSG, 54, Category.CATEGORY_USER_INPUT),
    RECURRING_MISSING_OR_WRONG_VALUE_INTERVAL(OXCalendarExceptionMessage.RECURRING_MISSING_OR_WRONG_VALUE_INTERVAL_MSG, 55, Category.CATEGORY_USER_INPUT),
    RECURRING_MISSING_OR_WRONG_VALUE_DAYS(OXCalendarExceptionMessage.RECURRING_MISSING_OR_WRONG_VALUE_DAYS_MSG, 56, Category.CATEGORY_USER_INPUT),
    PRIVATE_MOVE_TO_PUBLIC(OXCalendarExceptionMessage.PRIVATE_MOVE_TO_PUBLIC_MSG, 57, Category.CATEGORY_USER_INPUT),
    LOAD_PERMISSION_EXCEPTION_1(OXCalendarExceptionMessage.LOAD_PERMISSION_EXCEPTION_1_MSG, 58, Category.CATEGORY_PERMISSION_DENIED),
    LOAD_PERMISSION_EXCEPTION_2(OXCalendarExceptionMessage.LOAD_PERMISSION_EXCEPTION_2_MSG, 59, Category.CATEGORY_PERMISSION_DENIED),
    LOAD_PERMISSION_EXCEPTION_3(OXCalendarExceptionMessage.LOAD_PERMISSION_EXCEPTION_3_MSG, 60, Category.CATEGORY_PERMISSION_DENIED),
    LOAD_PERMISSION_EXCEPTION_4(OXCalendarExceptionMessage.LOAD_PERMISSION_EXCEPTION_4_MSG, 61, Category.CATEGORY_PERMISSION_DENIED),
    LOAD_PERMISSION_EXCEPTION_5(OXCalendarExceptionMessage.LOAD_PERMISSION_EXCEPTION_5_MSG, 62, Category.CATEGORY_PERMISSION_DENIED),
    LOAD_PERMISSION_EXCEPTION_6(OXCalendarExceptionMessage.LOAD_PERMISSION_EXCEPTION_6_MSG, 63, Category.CATEGORY_PERMISSION_DENIED),
    RECURRING_MISSING_YEARLY_MONTH(OXCalendarExceptionMessage.RECURRING_MISSING_YEARLY_MONTH_MSG, 64, Category.CATEGORY_USER_INPUT),
    /**
     * You are trying to create a new recurring appointment from an exception. This is not possible.
     */
    RECURRING_ALREADY_EXCEPTION(OXCalendarExceptionMessage.RECURRING_ALREADY_EXCEPTION_MSG, 65, Category.CATEGORY_USER_INPUT),
    /**
     * You cannot move one instance of a recurring appointment into another folder.
     */
    RECURRING_EXCEPTION_MOVE_EXCEPTION(OXCalendarExceptionMessage.RECURRING_EXCEPTION_MOVE_EXCEPTION_MSG, 66, Category.CATEGORY_USER_INPUT),
    UPDATE_EXCEPTION(OXCalendarExceptionMessage.UPDATE_EXCEPTION_MSG, 67, Category.CATEGORY_ERROR),
    MOVE_TO_SHARED_FOLDER_NOT_SUPPORTED(OXCalendarExceptionMessage.MOVE_TO_SHARED_FOLDER_NOT_SUPPORTED_MSG, 68, Category.CATEGORY_USER_INPUT),
    RECURRING_EXCEPTION_PRIVATE_FLAG(OXCalendarExceptionMessage.RECURRING_EXCEPTION_PRIVATE_FLAG_MSG, 69, Category.CATEGORY_USER_INPUT),
    PIVATE_FLAG_ONLY_IN_PRIVATE_FOLDER(OXCalendarExceptionMessage.PIVATE_FLAG_ONLY_IN_PRIVATE_FOLDER_MSG, 70, Category.CATEGORY_USER_INPUT),
    INVALID_CHARACTER(OXCalendarExceptionMessage.INVALID_CHARACTER_MSG, 71, Category.CATEGORY_USER_INPUT),
    /**
     * Some data exceeds a field limit. Please shorten the input(s) for affected field(s).
     */
    TRUNCATED_SQL_ERROR(OXCalendarExceptionMessage.TRUNCATED_SQL_ERROR_MSG, 72, Category.CATEGORY_TRUNCATED),
    /**
     * Calendar calculation requires a proper defined time zone.
     */
    TIMEZONE_MISSING(OXCalendarExceptionMessage.TIMEZONE_MISSING_MSG, 73, Category.CATEGORY_ERROR),
    /**
     * Recurrence position %1$s does not exist
     */
    UNKNOWN_RECURRENCE_POSITION(OXCalendarExceptionMessage.UNKNOWN_RECURRENCE_POSITION_MSG, 74, Category.CATEGORY_USER_INPUT),
    /**
     * One or more exception dates are not contained in recurring appointment
     */
    FOREIGN_EXCEPTION_DATE(OXCalendarExceptionMessage.FOREIGN_EXCEPTION_DATE_MSG, 75, Category.CATEGORY_USER_INPUT),
    /**
     * Appointment's owner must not be removed from participants
     */
    OWNER_REMOVAL_EXCEPTION(OXCalendarExceptionMessage.OWNER_REMOVAL_EXCEPTION_MSG, 76, Category.CATEGORY_PERMISSION_DENIED),
    /**
     * An event error occurred: %1$s
     */
    EVENT_ERROR(OXCalendarExceptionMessage.EVENT_ERROR_MSG, 77, Category.CATEGORY_ERROR),
    /**
     * Value %1$d exceeds max. supported value of %2$d.
     */
    RECURRING_VALUE_CONSTRAINT(OXCalendarExceptionMessage.RECURRING_VALUE_CONSTRAINT_MSG, 78, Category.CATEGORY_USER_INPUT),
    /**
     * Unable to calculate first occurrence.
     */
    UNABLE_TO_CALCULATE_FIRST_RECURRING(OXCalendarExceptionMessage.UNABLE_TO_CALCULATE_FIRST_RECURRING_MSG, 79, Category.CATEGORY_ERROR),
    /**
     * The recurrence pattern is too complex. Giving up.
     */
    RECURRENCE_PATTERN_TOO_COMPLEX(OXCalendarExceptionMessage.RECURRENCE_PATTERN_TOO_COMPLEX_MSG, 80, Category.CATEGORY_ERROR),
    /**
     * Unknown name-value-pair in recurrence string: %1$s=%2$s
     */
    UNKNOWN_NVP_IN_REC_STR(OXCalendarExceptionMessage.UNKNOWN_NVP_IN_REC_STR_MSG, 81, Category.CATEGORY_ERROR),
    /**
     * Changing recurrence type of a change exception denied
     */
    INVALID_RECURRENCE_TYPE_CHANGE(OXCalendarExceptionMessage.INVALID_RECURRENCE_TYPE_CHANGE_MSG, 82, Category.CATEGORY_USER_INPUT),
    /**
     * Changing recurrence position of a change exception denied
     */
    INVALID_RECURRENCE_POSITION_CHANGE(OXCalendarExceptionMessage.INVALID_RECURRENCE_POSITION_CHANGE_MSG, 83, Category.CATEGORY_USER_INPUT),
    /**
     * User changing the appointment is missing.
     */
    MODIFIED_BY_MISSING(OXCalendarExceptionMessage.MODIFIED_BY_MISSING_MSG, 84, Category.CATEGORY_ERROR),
    /**
     * Callbacks threw exceptions
     */
    CALLBACK_EXCEPTIONS(OXCalendarExceptionMessage.CALLBACK_EXCEPTIONS_MSG, 85, Category.CATEGORY_ERROR),
    /**
     * Series end is before start date.
     */
    UNTIL_BEFORE_START_DATE(OXCalendarExceptionMessage.UNTIL_BEFORE_START_DATE_MSG, 86, Category.CATEGORY_USER_INPUT),
    /**
     * Incomplete recurring information: Missing interval.
     */
    INCOMPLETE_REC_INFOS_INTERVAL(OXCalendarExceptionMessage.INCOMPLETE_REC_INFOS_INTERVAL_MSG, 87, Category.CATEGORY_USER_INPUT),
    /**
     * Incomplete recurring information: Missing series end date or number of occurrences.
     */
    INCOMPLETE_REC_INFOS_UNTIL_OR_OCCUR(OXCalendarExceptionMessage.INCOMPLETE_REC_INFOS_UNTIL_OR_OCCUR_MSG, 88, Category.CATEGORY_USER_INPUT),
    /**
     * Incomplete recurring information: Missing weekday.
     */
    INCOMPLETE_REC_INFOS_WEEKDAY(OXCalendarExceptionMessage.INCOMPLETE_REC_INFOS_WEEKDAY_MSG, 89, Category.CATEGORY_USER_INPUT),
    /**
     * Incomplete recurring information: Missing day in month.
     */
    INCOMPLETE_REC_INFOS_MONTHDAY(OXCalendarExceptionMessage.INCOMPLETE_REC_INFOS_MONTHDAY_MSG, 90, Category.CATEGORY_USER_INPUT),
    /**
     * Incomplete recurring information: Missing month.
     */
    INCOMPLETE_REC_INFOS_MONTH(OXCalendarExceptionMessage.INCOMPLETE_REC_INFOS_MONTH_MSG, 91, Category.CATEGORY_USER_INPUT),
    /**
     * Incomplete recurring information: Missing recurrence type.
     */
    INCOMPLETE_REC_INFOS_TYPE(OXCalendarExceptionMessage.INCOMPLETE_REC_INFOS_TYPE_MSG, 92, Category.CATEGORY_USER_INPUT),
    /**
     * Move of recurring appointments is not supported
     */
    RECURRING_FOLDER_MOVE(OXCalendarExceptionMessage.RECURRING_FOLDER_MOVE_MSG, 93, Category.CATEGORY_USER_INPUT),
    /**
     * In order to accomplish the search, %1$d or more characters are required.
     */
    PATTERN_TOO_SHORT(OXCalendarExceptionMessage.PATTERN_TOO_SHORT_MSG, 94, Category.CATEGORY_USER_INPUT),
    /**
     * Redundant information.
     */
    REDUNDANT_UNTIL_OCCURRENCES(OXCalendarExceptionMessage.REDUNDANT_UNTIL_OCCURRENCES_MSG, 95, Category.CATEGORY_USER_INPUT),
    /**
     * Unnecessary recurrence Information for Type "no".
     */
    UNNECESSARY_RECURRENCE_INFORMATION_NO(OXCalendarExceptionMessage.UNNECESSARY_RECURRENCE_INFORMATION_NO_MSG, 96, Category.CATEGORY_USER_INPUT),
    /**
     * Unnecessary recurrence information.
     */
    UNNECESSARY_RECURRENCE_INFORMATION(OXCalendarExceptionMessage.UNNECESSARY_RECURRENCE_INFORMATION_MSG, 97, Category.CATEGORY_USER_INPUT),
    /**
     * The recurring appointment has been deleted or is outside of the range of the recurrence.
     */
    UNABLE_TO_CALCULATE_POSITION(OXCalendarExceptionMessage.UNABLE_TO_CALCULATE_POSITION_MSG, 98, Category.CATEGORY_USER_INPUT),
    CHANGE_EXCEPTION_TO_RECURRENCE(OXCalendarExceptionMessage.CHANGE_EXCEPTION_TO_RECURRENCE_MSG, 99, Category.CATEGORY_USER_INPUT),
    UID_ALREDY_EXISTS(OXCalendarExceptionMessage.UID_ALREDY_EXISTS_MSG, 100, Category.CATEGORY_USER_INPUT),
    /** SQL Problem. */
    SQL_ERROR(OXCalendarExceptionMessage.SQL_ERROR_MSG, 101, Category.CATEGORY_ERROR),
    /** Wrong number of rows changed. Expected %1$d but was %2$d. */
    WRONG_ROW_COUNT(OXCalendarExceptionMessage.WRONG_ROW_COUNT_MSG, 102, Category.CATEGORY_ERROR),
    /**
     * Unable to find a participant for a specified object.
     */
    COULD_NOT_FIND_PARTICIPANT(OXCalendarExceptionMessage.COULD_NOT_FIND_PARTICIPANT_MSG, 103, Category.CATEGORY_USER_INPUT),
    /** Was not able to calculate next upcoming reminder for series appointment %2$d in context %1$d. */
    NEXT_REMINDER_FAILED(OXCalendarExceptionMessage.NEXT_REMINDER_FAILED_MSG, 104, Category.CATEGORY_ERROR),
    /**
     * Invalid sequence value: %1$d
     */
    INVALID_SEQUENCE(OXCalendarExceptionMessage.INVALID_SEQUENCE_MSG, 105, Category.CATEGORY_USER_INPUT),
    /**
     * An external participant with email address %1$s is already contained. Please remove duplicate participant and retry.
     */
    DUPLICATE_EXTERNAL_PARTICIPANT(OXCalendarExceptionMessage.DUPLICATE_EXTERNAL_PARTICIPANT_MSG, 106, Category.CATEGORY_USER_INPUT),
    /**
     * Appointment is not a recurring appointment.
     */
    NO_RECCURENCE(OXCalendarExceptionMessage.NO_RECCURENCE_MSG, 107, Category.CATEGORY_USER_INPUT),
    /**
     * Unexpected SQL Error!
     */
    CALENDAR_SQL_ERROR_RETRY(OXCalendarExceptionMessage.CALENDAR_SQL_ERROR_MSG, 108, Category.CATEGORY_TRY_AGAIN),

    ;

    /**
     * Message of the exception.
     */
    private final String message;

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
        this.message = message;
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
    public boolean equals(final OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @return The newly created {@link OXException} instance
     */
    public OXException create() {
        return OXExceptionFactory.getInstance().create(this, new Object[0]);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Object... args) {
        return OXExceptionFactory.getInstance().create(this, (Throwable) null, args);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param cause The optional initial cause
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Throwable cause, final Object... args) {
        return OXExceptionFactory.getInstance().create(this, cause, args);
    }
}
