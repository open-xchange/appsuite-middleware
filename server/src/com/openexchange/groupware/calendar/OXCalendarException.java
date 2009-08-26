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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import com.openexchange.api2.OXException;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.EnumComponent;

/**
 *  OXCalendarException
 *  @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
 */
public class OXCalendarException extends OXException {

    /**
     *
     */
    private static final long serialVersionUID = -33608409653330247L;

    /**
     * Initializes a new {@link OXCalendarException}
     *
     * @param cause The init cause
     */
    public OXCalendarException(final AbstractOXException cause) {
        super(cause);
    }

    public OXCalendarException(final Code code, final Object... messageArgs) {
        this(code, null, messageArgs);
    }

    public OXCalendarException(final Code code, final Throwable throwable, final Object... messageArgs) {
        super(EnumComponent.APPOINTMENT, code.getCategory(), code.getDetailNumber(), code.getMessage(), throwable);
        super.setMessageArgs(messageArgs);
    }

    /**
     * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
     *
     */
    public enum Code {
        CFO_NOT_INITIALIZIED("FATAL: CalendarFolderObject not initialized!", 1, AbstractOXException.Category.CODE_ERROR),
        NOT_YET_SUPPORTED("Not yet supported!", 2, AbstractOXException.Category.CODE_ERROR),
        NO_SHARED_FOLDER_OWNER("Shared folder owner not given !", 3, AbstractOXException.Category.CODE_ERROR),
        FOLDER_TYPE_UNRESOLVEABLE("Folder type unresolvable !", 4, AbstractOXException.Category.CODE_ERROR),
        /**
         * Unexpected SQL Error!
         */
        CALENDAR_SQL_ERROR("Unexpected SQL Error!", 5, AbstractOXException.Category.SUBSYSTEM_OR_SERVICE_DOWN),
        /**
         * TODO remove this exception. The AJAX interface should already check for a missing last modified.
         */
        LAST_MODIFIED_IS_NULL("clientLastModified IS NULL. Abort action!", 6, AbstractOXException.Category.CODE_ERROR),
        /**
         * Unexpected exception %d!
         */
        UNEXPECTED_EXCEPTION("Unexpected exception %d!", 7, AbstractOXException.Category.INTERNAL_ERROR),
        EXTERNAL_PARTICIPANTS_MANDATORY_FIELD("Mandatory field mail address for external participants", 8, AbstractOXException.Category.USER_INPUT),
        UPDATE_WITHOUT_PARTICIPANTS("FATAL: Would create an object without participants", 9, AbstractOXException.Category.CODE_ERROR),
        UPDATE_USER_SHARED_MISMATCH("Folder type \"SHARED\" is not allowed in this situation.", 10, AbstractOXException.Category.USER_INPUT),
        RECURRING_UNEXPECTED_DELETE_STATE("Unexpected state for deleting a virtual appointment (exception). uid:oid:position %d:%d:%d", 11, AbstractOXException.Category.INTERNAL_ERROR),
        ERROR_SESSIONOBJECT_IS_NULL("SessionObject not initialized", 12, AbstractOXException.Category.CODE_ERROR),
        NO_PERMISSION("You do not have the necessary permissions", 13, AbstractOXException.Category.PERMISSION),
        INSERT_WITH_OBJECT_ID("Insert expected but the object id is already given. Aborting action...", 14, AbstractOXException.Category.CODE_ERROR),
        UPDATE_WITHOUT_OBJECT_ID("Update expected but no object id is given. Aborting action...", 15, AbstractOXException.Category.CODE_ERROR),
        FOLDER_DELETE_INVALID_REQUEST("Invalid request. Folder is shared!", 16, AbstractOXException.Category.CODE_ERROR),
        FOLDER_FOREIGN_INVALID_REQUEST("Invalid request. Folder is shared!", 17, AbstractOXException.Category.CODE_ERROR),
        FOLDER_IS_EMPTY_INVALID_REQUEST("Invalid request. Folder is shared!", 18, AbstractOXException.Category.CODE_ERROR),
        FREE_BUSY_UNSUPPOTED_TYPE("Unsupported type detected : %d", 19, AbstractOXException.Category.CODE_ERROR),
        END_DATE_BEFORE_START_DATE("End date is before start date", 20, AbstractOXException.Category.USER_INPUT),
        UNSUPPORTED_LABEL("ERROR: Unsupported label value %d", 21, AbstractOXException.Category.USER_INPUT),
        PRIVATE_FLAG_IN_PRIVATE_FOLDER("ERROR: Private flag is only allowed inside of a private folder.", 22, AbstractOXException.Category.USER_INPUT),
        PRIVATE_FLAG_AND_PARTICIPANTS("ERROR: Private flag and participants are not supported.", 23, AbstractOXException.Category.USER_INPUT),
        UNSUPPORTED_PRIVATE_FLAG("ERROR: Unsupported private flag value %d", 24, AbstractOXException.Category.USER_INPUT),
        UNSUPPORTED_SHOWN_AS("ERROR:: Unsupported \"shown as\"  value %d", 25, AbstractOXException.Category.USER_INPUT),
        MANDATORY_FIELD_START_DATE("Required  value \"Start Date\" was not supplied.", 26, AbstractOXException.Category.USER_INPUT),
        MANDATORY_FIELD_END_DATE("Required value \"End Date\" was not supplied.", 27, AbstractOXException.Category.USER_INPUT),
        MANDATORY_FIELD_TITLE("Required value \"Title\" was not supplied.", 28, AbstractOXException.Category.USER_INPUT),
        UNABLE_TO_CALCULATE_RECURRING_POSITION("Unable to create exception, recurring position can not be calculated !", 29, AbstractOXException.Category.USER_INPUT),
        INTERNAL_USER_PARTICIPANT_CHECK_1("Error: Got an UserParticipant object with an identifier < 1 Identifier:Folder_Type = %d:%d", 30, AbstractOXException.Category.INTERNAL_ERROR),
        INTERNAL_USER_PARTICIPANT_CHECK_2("Error: Got an UserParticipant object with a private folder id < 1 : Identifier = %d", 31, AbstractOXException.Category.USER_INPUT),
        INTERNAL_USER_PARTICIPANT_CHECK_3("Error: Got an UserParticipant object with a private folder id in a public folder : Identifier = %d", 32, AbstractOXException.Category.USER_INPUT),
        MOVE_NOT_SUPPORTED("Move not supported: Cannot move an appointment from folder %d to folder %d", 33, AbstractOXException.Category.CODE_ERROR),
        SHARED_FOLDER_MOVE_NOT_SUPPORTED("Move not allowed from shared folders", 34, AbstractOXException.Category.CODE_ERROR),
        CONTEXT_NOT_SET("Calendar operation: Context not set.", 35, AbstractOXException.Category.CODE_ERROR),
        NO_PERMISSIONS_TO_ATTACH_DETACH("Insufficient rights to attach/detach an attachment to this folder!", 36, AbstractOXException.Category.PERMISSION),
        NO_PERMISSIONS_TO_READ("Insufficient read rights for this folder!", 37, AbstractOXException.Category.PERMISSION),
        /**
         * FATAL:: Can not resolve recurrence position because we got neither the recurring position nor a recurring date position
         */
        UNABLE_TO_CALCULATE_RECURRING_POSITION_NO_INPUT("FATAL:: Can not resolve recurrence position because we got neither the recurring position nor a recurring date position", 38, AbstractOXException.Category.INTERNAL_ERROR),
        RECURRING_MISSING_START_DATE("Missing start date, unable to calculate recurring!", 39, AbstractOXException.Category.CODE_ERROR),
        RECURRING_MISSING_DAILY_INTERVAL("Fatal error. (DAILY) Missing or wrong Interval value: %d", 40, AbstractOXException.Category.USER_INPUT),
        RECURRING_MISSING_WEEKLY_INTERVAL("Fatal error. (WEEKLY) Missing or wrong Interval value: %d", 41, AbstractOXException.Category.USER_INPUT),
        /**
         * Fatal error. (MONTHLY) Missing or wrong value DayInMonth : %d
         */
        RECURRING_MISSING_MONTLY_INTERVAL("Fatal error. (MONTHLY) Missing or wrong value DayInMonth : %d", 42, AbstractOXException.Category.USER_INPUT),
        RECURRING_MISSING_MONTLY_INTERVAL_2("Fatal error. (MONTHLY) Missing or wrong value Month : %d", 43, AbstractOXException.Category.USER_INPUT),
        RECURRING_MISSING_MONTLY_DAY("Fatal error. (MONTHLY2) Missing or wrong Day value: %d", 44, AbstractOXException.Category.USER_INPUT),
        /**
         * Fatal error. (MONTHLY2) Missing or wrong DayInMonth value: %d
         */
        RECURRING_MISSING_MONTLY_DAY_2("Fatal error. (MONTHLY2) Missing or wrong DayInMonth value: %d", 45, AbstractOXException.Category.USER_INPUT),
        RECURRING_MISSING_YEARLY_INTERVAL("Fatal error. (YEARLY) Missing or wrong value DayInMonth : %d", 46, AbstractOXException.Category.USER_INPUT),
        RECURRING_MISSING_YEARLY_DAY("Fatal error. (YEARLY2) Missing or wrong value day : %d", 47, AbstractOXException.Category.USER_INPUT),
        RECURRING_MISSING_YEARLY_TYPE("Fatal error. (YEARLY2) Missing or wrong day_or_type : %d", 48, AbstractOXException.Category.USER_INPUT),
        RECURRING_MISSING_YEARLY_INTERVAL_2("Fatal error. (YEARLY2) Missing or wrong Interval value: %d", 49, AbstractOXException.Category.USER_INPUT),
        UNABLE_TO_REMOVE_PARTICIPANT("Unable to remove participant %d", 50, AbstractOXException.Category.CODE_ERROR),
        UNABLE_TO_REMOVE_PARTICIPANT_2("Unable to remove participant because this participant is the last one", 51, AbstractOXException.Category.USER_INPUT),
        UNSUPPORTED_ACTION_TYPE("Action type not supported : %d", 52,  AbstractOXException.Category.CODE_ERROR),
        SEARCH_ITERATOR_NULL("SearchIterator NULL", 53, AbstractOXException.Category.CODE_ERROR),
        NON_CALENDAR_FOLDER("Folder is not of type Calendar", 54, AbstractOXException.Category.USER_INPUT),
        RECURRING_MISSING_OR_WRONG_VALUE_INTERVAL("The required value \"interval\" is missing or wrong", 55, AbstractOXException.Category.USER_INPUT),
        RECURRING_MISSING_OR_WRONG_VALUE_DAYS("The required  value \"days\" is missing or wrong", 56, AbstractOXException.Category.USER_INPUT),
        PRIVATE_MOVE_TO_PUBLIC("Moving appointment to a public folder flaged as private is not allowed!", 57, AbstractOXException.Category.USER_INPUT),
        LOAD_PERMISSION_EXCEPTION_1("You do not have the appropriate permissions to modify this object.", 58, AbstractOXException.Category.PERMISSION),
        LOAD_PERMISSION_EXCEPTION_2("Got the wrong folder identification. You do not have the appropriate permissions to modify this object.", 59, AbstractOXException.Category.PERMISSION),
        LOAD_PERMISSION_EXCEPTION_3("Got the wrong shared folder identification. You do not have the appropriate permissions to modify this object.", 60, AbstractOXException.Category.PERMISSION),
        LOAD_PERMISSION_EXCEPTION_4("You do not have the appropriate permissions to move this object.", 61, AbstractOXException.Category.PERMISSION),
        LOAD_PERMISSION_EXCEPTION_5("You do not have the appropriate permissions to read this object", 62, AbstractOXException.Category.PERMISSION),
        LOAD_PERMISSION_EXCEPTION_6("You do not have the appropriate permissions to create an object", 63, AbstractOXException.Category.PERMISSION),
        RECURRING_MISSING_YEARLY_MONTH("Fatal error. (YEARLY) Missing or wrong Month value: %d", 64, AbstractOXException.Category.USER_INPUT),
        RECURRING_ALREADY_EXCEPTION("Fatal error. You are trying to create a new recurring from an exception!", 65, AbstractOXException.Category.USER_INPUT),
        RECURRING_EXCEPTION_MOVE_EXCEPTION("You can not move one element of a recurring appointment into another folder.", 66, AbstractOXException.Category.USER_INPUT),
        UPDATE_EXCEPTION("Fatal error. An database update exception occurred.", 67, AbstractOXException.Category.INTERNAL_ERROR),
        MOVE_TO_SHARED_FOLDER_NOT_SUPPORTED("Move not allowed to a shared folder if the private flag is set", 68, AbstractOXException.Category.USER_INPUT),
        RECURRING_EXCEPTION_PRIVATE_FLAG("You can not use different private flags for one element of a recurring appointment", 69, AbstractOXException.Category.USER_INPUT),
        PIVATE_FLAG_ONLY_IN_PRIVATE_FOLDER("You can not use the private flags in a non private folder", 70, AbstractOXException.Category.USER_INPUT),
        INVALID_CHARACTER("Bad character in field %1$s. Error: %2$s", 71, AbstractOXException.Category.USER_INPUT),
        /**
         * Some data exceeds a field limit. Please shorten the input(s) for affected field(s).
         */
        TRUNCATED_SQL_ERROR("Some data exceeds a field limit. Please shorten the input(s) for affected field(s).", 72, AbstractOXException.Category.TRUNCATED),
        /**
         * Calendar calculation requires a proper defined time zone.
         */
        TIMEZONE_MISSING("Calendar calculation requires a proper defined time zone.", 73, Category.CODE_ERROR),
        /**
         * Recurrence position %1$s does not exist
         */
        UNKNOWN_RECURRENCE_POSITION("Recurrence position %1$s does not exist", 74, Category.USER_INPUT),
        /**
         * One or more exception dates are not contained in recurring appointment
         */
        FOREIGN_EXCEPTION_DATE("One or more exception dates are not contained in recurring appointment", 75, Category.USER_INPUT),
        /**
         * Appointment's owner must not be removed from participants
         */
        OWNER_REMOVAL_EXCEPTION("Appointment's owner must not be removed from participants", 76, Category.PERMISSION),
        /**
         * An event error occurred: %1$s
         */
        EVENT_ERROR("An event error occurred: %1$s", 77, Category.CODE_ERROR),
        /**
         * Value %1$d exceeds max. supported value of %2$d.
         */
        RECURRING_VALUE_CONSTRAINT("Value %1$d exceeds max. supported value of %2$d.", 78, Category.USER_INPUT),
        /**
         * Unable to calculate first occurrence.
         */
        UNABLE_TO_CALCULATE_FIRST_RECURRING("Unable to calculate first occurrence.", 79, Category.CODE_ERROR),
        /**
         * The recurrence pattern is too complex. Giving up.
         */
        RECURRENCE_PATTERN_TOO_COMPLEX("The recurrence pattern is too complex. Giving up.", 80, Category.INTERNAL_ERROR),
        /**
         * Unknown name-value-pair in recurrence string: %1$s=%2$s
         */
        UNKNOWN_NVP_IN_REC_STR("Unknown name-value-pair in recurrence string: %1$s=%2$s", 81, Category.CODE_ERROR),
        /**
         * Changing recurrence type of a change exception denied
         */
        INVALID_RECURRENCE_TYPE_CHANGE("Changing recurrence type of a change exception denied", 82, Category.USER_INPUT),
        /**
         * Changing recurrence position of a change exception denied
         */
        INVALID_RECURRENCE_POSITION_CHANGE("Changing recurrence position of a change exception denied.", 83, Category.USER_INPUT),
        /**
         * User changing the appointment is missing.
         */
        MODIFIED_BY_MISSING("User changing the appointment is missing.", 84, Category.CODE_ERROR),
        /**
         * Callbacks threw exceptions
         */
        CALLBACK_EXCEPTIONS("Some callbacks threw exceptions: %s", 85, Category.INTERNAL_ERROR),
        /**
         * Series end is before start date.
         */
        UNTIL_BEFORE_START_DATE("Series end is before start date.", 86, AbstractOXException.Category.USER_INPUT),
        /**
         * Incomplete recurring information: Missing interval.
         */
        INCOMPLETE_REC_INFOS_INTERVAL("Incomplete recurring information: Missing interval.", 87, Category.USER_INPUT),
        /**
         * Incomplete recurring information: Missing series end date or number of occurrences.
         */
        INCOMPLETE_REC_INFOS_UNTIL_OR_OCCUR("Incomplete recurring information: Missing series end date or number of occurrences.", 88, Category.USER_INPUT),
        /**
         * Incomplete recurring information: Missing weekday.
         */
        INCOMPLETE_REC_INFOS_WEEKDAY("Incomplete recurring information: Missing weekday.", 89, Category.USER_INPUT),
        /**
         * Incomplete recurring information: Missing day in month.
         */
        INCOMPLETE_REC_INFOS_MONTHDAY("Incomplete recurring information: Missing day in month.", 90, Category.USER_INPUT),
        /**
         * Incomplete recurring information: Missing month.
         */
        INCOMPLETE_REC_INFOS_MONTH("Incomplete recurring information: Missing month.", 91, Category.USER_INPUT),
        /**
         * Incomplete recurring information: Missing recurrence type.
         */
        INCOMPLETE_REC_INFOS_TYPE("Incomplete recurring information: Missing recurrence type.", 92, Category.USER_INPUT),
        /**
         * Move of recurring appointments is not supported
         */
        RECURRING_FOLDER_MOVE("Move of a recurring appointment to another folder is not supported.", 93, Category.USER_INPUT),
        /**
         * In order to accomplish the search, %1$d or more characters are required.
         */
        PATTERN_TOO_SHORT("In order to accomplish the search, %1$d or more characters are required.", 94, Category.USER_INPUT);

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
         * @param message message.
         * @param category category.
         * @param detailNumber detail number.
         */
        private Code(final String message,
                final int detailNumber,
                final Category category)  {
            this.message = message;
            this.category = category;
            this.detailNumber = detailNumber;
        }

        public Category getCategory() {
            return category;
        }

        public int getDetailNumber() {
            return detailNumber;
        }

        public String getMessage() {
            return message;
        }


    }



}
