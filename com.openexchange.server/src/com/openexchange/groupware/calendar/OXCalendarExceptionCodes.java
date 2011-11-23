package com.openexchange.groupware.calendar;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

/**
 * The calendar error code enumeration.
 */
public enum OXCalendarExceptionCodes implements OXExceptionCode {
    CFO_NOT_INITIALIZIED("FATAL: CalendarFolderObject not initialized!", 1, Category.CATEGORY_ERROR),
    NOT_YET_SUPPORTED("Not yet supported!", 2, Category.CATEGORY_ERROR),
    NO_SHARED_FOLDER_OWNER("Shared folder owner not given !", 3, Category.CATEGORY_ERROR),
    FOLDER_TYPE_UNRESOLVEABLE("Folder type unresolvable !", 4, Category.CATEGORY_ERROR),
    /**
     * Unexpected SQL Error!
     */
    CALENDAR_SQL_ERROR("Unexpected SQL Error!", 5, Category.CATEGORY_SERVICE_DOWN),
    /**
     * TODO remove this exception. The AJAX interface should already check for a missing last modified.
     */
    LAST_MODIFIED_IS_NULL("clientLastModified IS NULL. Abort action!", 6, Category.CATEGORY_ERROR),
    /**
     * Unexpected exception %d!
     */
    UNEXPECTED_EXCEPTION("Unexpected exception %d!", 7, Category.CATEGORY_ERROR),
    EXTERNAL_PARTICIPANTS_MANDATORY_FIELD("Mandatory field mail address for external participants", 8, Category.CATEGORY_USER_INPUT),
    UPDATE_WITHOUT_PARTICIPANTS("FATAL: Would create an object without participants", 9, Category.CATEGORY_ERROR),
    UPDATE_USER_SHARED_MISMATCH("Folder type \"SHARED\" is not allowed in this situation.", 10, Category.CATEGORY_USER_INPUT),
    RECURRING_UNEXPECTED_DELETE_STATE("Unexpected state for deleting a virtual appointment (exception). uid:oid:position %d:%d:%d", 11, Category.CATEGORY_ERROR),
    ERROR_SESSIONOBJECT_IS_NULL("SessionObject not initialized", 12, Category.CATEGORY_ERROR),
    NO_PERMISSION("You do not have the necessary permissions for appointments in folder %1$d.", 13, Category.CATEGORY_PERMISSION_DENIED),
    INSERT_WITH_OBJECT_ID("Insert expected but the object id is already given. Aborting action...", 14, Category.CATEGORY_ERROR),
    UPDATE_WITHOUT_OBJECT_ID("Update expected but no object id is given. Aborting action...", 15, Category.CATEGORY_ERROR),
    FOLDER_DELETE_INVALID_REQUEST("Invalid request. Folder is shared!", 16, Category.CATEGORY_ERROR),
    FOLDER_FOREIGN_INVALID_REQUEST("Invalid request. Folder is shared!", 17, Category.CATEGORY_ERROR),
    FOLDER_IS_EMPTY_INVALID_REQUEST("Invalid request. Folder is shared!", 18, Category.CATEGORY_ERROR),
    FREE_BUSY_UNSUPPOTED_TYPE("Unsupported type detected : %d", 19, Category.CATEGORY_ERROR),
    END_DATE_BEFORE_START_DATE("End date is before start date", 20, Category.CATEGORY_USER_INPUT),
    UNSUPPORTED_LABEL("ERROR: Unsupported label value %d", 21, Category.CATEGORY_USER_INPUT),
    PRIVATE_FLAG_IN_PRIVATE_FOLDER("ERROR: Private flag is only allowed inside of a private folder.", 22, Category.CATEGORY_USER_INPUT),
    PRIVATE_FLAG_AND_PARTICIPANTS("Error: Appointments marked as 'Private' can only be scheduled for the respective user (or owner of the calendar). Please remove additional participants or remove the \"Private\" mark.", 23, Category.CATEGORY_USER_INPUT),
    UNSUPPORTED_PRIVATE_FLAG("ERROR: Unsupported private flag value %d", 24, Category.CATEGORY_USER_INPUT),
    UNSUPPORTED_SHOWN_AS("ERROR:: Unsupported \"shown as\"  value %d", 25, Category.CATEGORY_USER_INPUT),
    MANDATORY_FIELD_START_DATE("Required  value \"Start Date\" was not supplied.", 26, Category.CATEGORY_USER_INPUT),
    MANDATORY_FIELD_END_DATE("Required value \"End Date\" was not supplied.", 27, Category.CATEGORY_USER_INPUT),
    MANDATORY_FIELD_TITLE("Required value \"Title\" was not supplied.", 28, Category.CATEGORY_USER_INPUT),
    UNABLE_TO_CALCULATE_RECURRING_POSITION("Unable to create exception, recurring position can not be calculated !", 29, Category.CATEGORY_USER_INPUT),
    INTERNAL_USER_PARTICIPANT_CHECK_1("Error: Got an UserParticipant object with an identifier < 1 Identifier:Folder_Type = %d:%d", 30, Category.CATEGORY_ERROR),
    INTERNAL_USER_PARTICIPANT_CHECK_2("Error: Got an UserParticipant object with a private folder id < 1 : Identifier = %d", 31, Category.CATEGORY_USER_INPUT),
    INTERNAL_USER_PARTICIPANT_CHECK_3("Error: Got an UserParticipant object with a private folder id in a public folder : Identifier = %d", 32, Category.CATEGORY_USER_INPUT),
    MOVE_NOT_SUPPORTED("Move not supported: Cannot move an appointment from folder %d to folder %d", 33, Category.CATEGORY_ERROR),
    SHARED_FOLDER_MOVE_NOT_SUPPORTED("Move not allowed from shared folders", 34, Category.CATEGORY_ERROR),
    CONTEXT_NOT_SET("Calendar operation: Context not set.", 35, Category.CATEGORY_ERROR),
    NO_PERMISSIONS_TO_ATTACH_DETACH("Insufficient rights to attach/detach an attachment to this folder!", 36, Category.CATEGORY_PERMISSION_DENIED),
    NO_PERMISSIONS_TO_READ("Insufficient read rights for this folder!", 37, Category.CATEGORY_PERMISSION_DENIED),
    /**
     * FATAL:: Can not resolve recurrence position because we got neither the recurring position nor a recurring date position
     */
    UNABLE_TO_CALCULATE_RECURRING_POSITION_NO_INPUT("FATAL:: Can not resolve recurrence position because we got neither the recurring position nor a recurring date position", 38, Category.CATEGORY_ERROR),
    RECURRING_MISSING_START_DATE("Missing start date, unable to calculate recurring!", 39, Category.CATEGORY_ERROR),
    RECURRING_MISSING_DAILY_INTERVAL("Fatal error. (DAILY) Missing or wrong Interval value: %d", 40, Category.CATEGORY_USER_INPUT),
    RECURRING_MISSING_WEEKLY_INTERVAL("Fatal error. (WEEKLY) Missing or wrong Interval value: %d", 41, Category.CATEGORY_USER_INPUT),
    /**
     * Fatal error. (MONTHLY) Missing or wrong value DayInMonth : %d
     */
    RECURRING_MISSING_MONTLY_INTERVAL("Fatal error. (MONTHLY) Missing or wrong value DayInMonth : %d", 42, Category.CATEGORY_USER_INPUT),
    RECURRING_MISSING_MONTLY_INTERVAL_2("Fatal error. (MONTHLY) Missing or wrong value Month : %d", 43, Category.CATEGORY_USER_INPUT),
    RECURRING_MISSING_MONTLY_DAY("Fatal error. (MONTHLY2) Missing or wrong Day value: %d", 44, Category.CATEGORY_USER_INPUT),
    /**
     * Fatal error. (MONTHLY2) Missing or wrong DayInMonth value: %d
     */
    RECURRING_MISSING_MONTLY_DAY_2("Fatal error. (MONTHLY2) Missing or wrong DayInMonth value: %d", 45, Category.CATEGORY_USER_INPUT),
    RECURRING_MISSING_YEARLY_INTERVAL("Fatal error. (YEARLY) Missing or wrong value DayInMonth : %d", 46, Category.CATEGORY_USER_INPUT),
    RECURRING_MISSING_YEARLY_DAY("Fatal error. (YEARLY2) Missing or wrong value day : %d", 47, Category.CATEGORY_USER_INPUT),
    RECURRING_MISSING_YEARLY_TYPE("Fatal error. (YEARLY2) Missing or wrong day_or_type : %d", 48, Category.CATEGORY_USER_INPUT),
    RECURRING_MISSING_YEARLY_INTERVAL_2("Fatal error. (YEARLY2) Missing or wrong Interval value: %d", 49, Category.CATEGORY_USER_INPUT),
    UNABLE_TO_REMOVE_PARTICIPANT("Unable to remove participant %d", 50, Category.CATEGORY_ERROR),
    UNABLE_TO_REMOVE_PARTICIPANT_2("Unable to remove participant because this participant is the last one", 51, Category.CATEGORY_USER_INPUT),
    UNSUPPORTED_ACTION_TYPE("Action type not supported : %d", 52,  Category.CATEGORY_ERROR),
    SEARCH_ITERATOR_NULL("SearchIterator NULL", 53, Category.CATEGORY_ERROR),
    NON_CALENDAR_FOLDER("Folder is not of type Calendar", 54, Category.CATEGORY_USER_INPUT),
    RECURRING_MISSING_OR_WRONG_VALUE_INTERVAL("The required value \"interval\" is missing or wrong", 55, Category.CATEGORY_USER_INPUT),
    RECURRING_MISSING_OR_WRONG_VALUE_DAYS("The required  value \"days\" is missing or wrong : %d", 56, Category.CATEGORY_USER_INPUT),
    PRIVATE_MOVE_TO_PUBLIC("Moving appointment to a public folder flaged as private is not allowed!", 57, Category.CATEGORY_USER_INPUT),
    LOAD_PERMISSION_EXCEPTION_1("You do not have the appropriate permissions to modify this object.", 58, Category.CATEGORY_PERMISSION_DENIED),
    LOAD_PERMISSION_EXCEPTION_2("Got the wrong folder identification. You do not have the appropriate permissions to modify this object.", 59, Category.CATEGORY_PERMISSION_DENIED),
    LOAD_PERMISSION_EXCEPTION_3("Got the wrong shared folder identification. You do not have the appropriate permissions to modify this object.", 60, Category.CATEGORY_PERMISSION_DENIED),
    LOAD_PERMISSION_EXCEPTION_4("You do not have the appropriate permissions to move this object.", 61, Category.CATEGORY_PERMISSION_DENIED),
    LOAD_PERMISSION_EXCEPTION_5("You do not have the appropriate permissions to read this object %1$d.", 62, Category.CATEGORY_PERMISSION_DENIED),
    LOAD_PERMISSION_EXCEPTION_6("You do not have the appropriate permissions to create an object", 63, Category.CATEGORY_PERMISSION_DENIED),
    RECURRING_MISSING_YEARLY_MONTH("Fatal error. (YEARLY) Missing or wrong Month value: %d", 64, Category.CATEGORY_USER_INPUT),
    RECURRING_ALREADY_EXCEPTION("Fatal error. You are trying to create a new recurring from an exception!", 65, Category.CATEGORY_USER_INPUT),
    RECURRING_EXCEPTION_MOVE_EXCEPTION("You can not move one element of a recurring appointment into another folder.", 66, Category.CATEGORY_USER_INPUT),
    UPDATE_EXCEPTION("Fatal error. An database update exception occurred.", 67, Category.CATEGORY_ERROR),
    MOVE_TO_SHARED_FOLDER_NOT_SUPPORTED("Move not allowed to a shared folder if the private flag is set", 68, Category.CATEGORY_USER_INPUT),
    RECURRING_EXCEPTION_PRIVATE_FLAG("You can not use different private flags for one element of a recurring appointment", 69, Category.CATEGORY_USER_INPUT),
    PIVATE_FLAG_ONLY_IN_PRIVATE_FOLDER("You can not use the private flags in a non private folder", 70, Category.CATEGORY_USER_INPUT),
    INVALID_CHARACTER("Bad character in field %1$s. Error: %2$s", 71, Category.CATEGORY_USER_INPUT),
    /**
     * Some data exceeds a field limit. Please shorten the input(s) for affected field(s).
     */
    TRUNCATED_SQL_ERROR("Some data exceeds a field limit. Please shorten the input(s) for affected field(s).", 72, Category.CATEGORY_TRUNCATED),
    /**
     * Calendar calculation requires a proper defined time zone.
     */
    TIMEZONE_MISSING("Calendar calculation requires a proper defined time zone.", 73, Category.CATEGORY_ERROR),
    /**
     * Recurrence position %1$s does not exist
     */
    UNKNOWN_RECURRENCE_POSITION("Recurrence position %1$s does not exist", 74, Category.CATEGORY_USER_INPUT),
    /**
     * One or more exception dates are not contained in recurring appointment
     */
    FOREIGN_EXCEPTION_DATE("One or more exception dates are not contained in recurring appointment", 75, Category.CATEGORY_USER_INPUT),
    /**
     * Appointment's owner must not be removed from participants
     */
    OWNER_REMOVAL_EXCEPTION("Appointment's owner must not be removed from participants", 76, Category.CATEGORY_PERMISSION_DENIED),
    /**
     * An event error occurred: %1$s
     */
    EVENT_ERROR("An event error occurred: %1$s", 77, Category.CATEGORY_ERROR),
    /**
     * Value %1$d exceeds max. supported value of %2$d.
     */
    RECURRING_VALUE_CONSTRAINT("Value %1$d exceeds max. supported value of %2$d.", 78, Category.CATEGORY_USER_INPUT),
    /**
     * Unable to calculate first occurrence.
     */
    UNABLE_TO_CALCULATE_FIRST_RECURRING("Unable to calculate first occurrence.", 79, Category.CATEGORY_ERROR),
    /**
     * The recurrence pattern is too complex. Giving up.
     */
    RECURRENCE_PATTERN_TOO_COMPLEX("The recurrence pattern is too complex. Giving up.", 80, Category.CATEGORY_ERROR),
    /**
     * Unknown name-value-pair in recurrence string: %1$s=%2$s
     */
    UNKNOWN_NVP_IN_REC_STR("Unknown name-value-pair in recurrence string: %1$s=%2$s", 81, Category.CATEGORY_ERROR),
    /**
     * Changing recurrence type of a change exception denied
     */
    INVALID_RECURRENCE_TYPE_CHANGE("Changing recurrence type of a change exception denied", 82, Category.CATEGORY_USER_INPUT),
    /**
     * Changing recurrence position of a change exception denied
     */
    INVALID_RECURRENCE_POSITION_CHANGE("Changing recurrence position of a change exception denied.", 83, Category.CATEGORY_USER_INPUT),
    /**
     * User changing the appointment is missing.
     */
    MODIFIED_BY_MISSING("User changing the appointment is missing.", 84, Category.CATEGORY_ERROR),
    /**
     * Callbacks threw exceptions
     */
    CALLBACK_EXCEPTIONS("Some callbacks threw exceptions: %s", 85, Category.CATEGORY_ERROR),
    /**
     * Series end is before start date.
     */
    UNTIL_BEFORE_START_DATE("Series end is before start date.", 86, Category.CATEGORY_USER_INPUT),
    /**
     * Incomplete recurring information: Missing interval.
     */
    INCOMPLETE_REC_INFOS_INTERVAL("Incomplete recurring information: Missing interval.", 87, Category.CATEGORY_USER_INPUT),
    /**
     * Incomplete recurring information: Missing series end date or number of occurrences.
     */
    INCOMPLETE_REC_INFOS_UNTIL_OR_OCCUR("Incomplete recurring information: Missing series end date or number of occurrences.", 88, Category.CATEGORY_USER_INPUT),
    /**
     * Incomplete recurring information: Missing weekday.
     */
    INCOMPLETE_REC_INFOS_WEEKDAY("Incomplete recurring information: Missing weekday.", 89, Category.CATEGORY_USER_INPUT),
    /**
     * Incomplete recurring information: Missing day in month.
     */
    INCOMPLETE_REC_INFOS_MONTHDAY("Incomplete recurring information: Missing day in month.", 90, Category.CATEGORY_USER_INPUT),
    /**
     * Incomplete recurring information: Missing month.
     */
    INCOMPLETE_REC_INFOS_MONTH("Incomplete recurring information: Missing month.", 91, Category.CATEGORY_USER_INPUT),
    /**
     * Incomplete recurring information: Missing recurrence type.
     */
    INCOMPLETE_REC_INFOS_TYPE("Incomplete recurring information: Missing recurrence type.", 92, Category.CATEGORY_USER_INPUT),
    /**
     * Move of recurring appointments is not supported
     */
    RECURRING_FOLDER_MOVE("Move of a recurring appointment to another folder is not supported.", 93, Category.CATEGORY_USER_INPUT),
    /**
     * In order to accomplish the search, %1$d or more characters are required.
     */
    PATTERN_TOO_SHORT("In order to accomplish the search, %1$d or more characters are required.", 94, Category.CATEGORY_USER_INPUT),
    /**
     * Redundant information.
     */
    REDUNDANT_UNTIL_OCCURRENCES("Redundant information for Until and occurrences.", 95, Category.CATEGORY_USER_INPUT),
    /**
     * Unnecessary recurrence Information for Type "no".
     */
    UNNECESSARY_RECURRENCE_INFORMATION_NO("Unnecessary recurrence information for recurrence type \"no recurrence\".", 96, Category.CATEGORY_USER_INPUT),
    /**
     * Unnecessary recurrence information.
     */
    UNNECESSARY_RECURRENCE_INFORMATION("Unnecessary recurrence information (%1$s) for type %2$s", 97, Category.CATEGORY_USER_INPUT),
    UNABLE_TO_CALCULATE_POSITION("Unable to calculate given position. Seems to be a delete exception or outside range", 98, Category.CATEGORY_USER_INPUT),
    CHANGE_EXCEPTION_TO_RECURRENCE("Changing an exception into a series is not supported.", 99, Category.CATEGORY_USER_INPUT),
    UID_ALREDY_EXISTS("Cannot insert appointment (%1$s). An appointment with the unique identifier (%2$s) already exists.", 100, Category.CATEGORY_USER_INPUT),
    /** SQL Problem. */
    SQL_ERROR("SQL Problem.", 101, Category.CATEGORY_ERROR),
    /** Wrong number of rows changed. Expected %1$d but was %2$d. */
    WRONG_ROW_COUNT("Wrong number of rows changed. Expected %1$d but was %2$d.", 102, Category.CATEGORY_ERROR),
    /**
     * Unable to find a participant for a specified object.
     */
    COULD_NOT_FIND_PARTICIPANT("Could not find participant for this object.", 103, Category.CATEGORY_USER_INPUT),
    /** Was not able to calculate next upcoming reminder for series appointment %2$d in context %1$d. */
    NEXT_REMINDER_FAILED("Was not able to calculate next upcoming reminder for series appointment %2$d in context %1$d.", 104, Category.CATEGORY_ERROR),
    /**
     * Invalid sequence value: %1$d
     */
    INVALID_SEQUENCE("Invalid sequence value: %1$d", 105, Category.CATEGORY_USER_INPUT),
    /**
     * An external participant with email address %1$s is already contained. Please remove duplicate participant and retry.
     */
    DUPLICATE_EXTERNAL_PARTICIPANT("An external participant with email address %1$s is already contained. Please remove duplicate participant and retry.", 106, Category.CATEGORY_USER_INPUT),

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