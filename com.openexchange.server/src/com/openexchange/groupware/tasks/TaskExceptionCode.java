package com.openexchange.groupware.tasks;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

/**
 * Error codes for task exceptions.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public enum TaskExceptionCode implements OXExceptionCode {
    /** Error while inserting task: %s. */
    INSERT_FAILED("Error while inserting task: %s.", Category.CATEGORY_ERROR, 1),
    /** Setting autoCommit to true failed. */
    AUTO_COMMIT("Problem setting auto commit to true.", Category.CATEGORY_SERVICE_DOWN, 2),
    /** A database connection cannot be obtained. */
    NO_CONNECTION("Cannot get database connection.", Category.CATEGORY_SERVICE_DOWN, 3),
    /** Unimplemented feature. */
    UNIMPLEMENTED("This method is not implemented.", Category.CATEGORY_ERROR, 4),
    /** SQL Problem. */
    SQL_ERROR("SQL Problem.", Category.CATEGORY_ERROR, 5),
    /** Folder %1$s (%2$d) is not a task folder. */
    NOT_TASK_FOLDER("Folder %1$s (%2$d) is not a task folder.", Category.CATEGORY_PERMISSION_DENIED, 6),
    /** Edit Conflict. Your change cannot be completed because somebody else has made a conflicting change to the same item. Please
     * refresh or synchronize and try again. */
    MODIFIED("Edit Conflict. Your change cannot be completed because somebody else has made a conflicting change to the same item. Please refresh or synchronize and try again.", Category.CATEGORY_CONFLICT, 7),
    /** Cannot create private task in public/shared folder %1$d. */
    PRIVATE_FLAG("Cannot create private task in public/shared folder %1$d.", Category.CATEGORY_USER_INPUT, 8),
    /** SQL problem while updating task: %s. */
    UPDATE_FAILED("SQL problem while updating task: %s.", Category.CATEGORY_ERROR, 9),
    /** Counting tasks did not return a result. */
    NO_COUNT_RESULT("Counting tasks did not return a result.", Category.CATEGORY_ERROR, 10),
    /** SQL problem while deleting task: %s. */
    DELETE_FAILED("SQL problem while deleting task: %s.", Category.CATEGORY_ERROR, 11),
    /** Cannot find folder for task %2$d and participant %1$d in context %3$d. */
    PARTICIPANT_FOLDER_INCONSISTENCY("Cannot find folder for task %2$d and participant %1$d in context %3$d.", Category.CATEGORY_ERROR, 12),
    /** SQL problem while listing tasks: %s. */
    SEARCH_FAILED("SQL problem while listing tasks: %s.", Category.CATEGORY_ERROR, 13),
    /** You are not allowed to delete the task %d. */
    NO_DELETE_PERMISSION("You are not allowed to delete the task.", Category.CATEGORY_PERMISSION_DENIED, 14),
    /** Tried to delete %1$d folders but only %2$d were deleted. */
    FOLDER_DELETE_WRONG("Tried to delete %1$d folders but only %2$d were deleted.", Category.CATEGORY_ERROR, 15),
    /** Tried to delete %1$d participants but only %2$d were deleted. */
    PARTICIPANT_DELETE_WRONG("Tried to delete %1$d participants but only %2$d were deleted.", Category.CATEGORY_ERROR, 16),
    /** Participant %d for task %d is not found. */
    PARTICIPANT_NOT_FOUND("Participant %d for task %d is not found.", Category.CATEGORY_ERROR, 18),
    /** Cannot find task %d in context %d. */
    TASK_NOT_FOUND("Cannot find task %d in context %d.", Category.CATEGORY_ERROR, 19),
    /** Unknown task attribute %d. */
    UNKNOWN_ATTRIBUTE("Unknown task attribute %d.", Category.CATEGORY_ERROR, 20),
    /** Date range in search must contain 2 and not %d values. */
    WRONG_DATE_RANGE("Date range in search must contain 2 and not %d values.", Category.CATEGORY_ERROR, 21),
    /** Cannot decrease number of attachments below zero. */
    WRONG_ATTACHMENT_COUNT("Cannot decrease number of attachments below zero.", Category.CATEGORY_ERROR, 22),
    /** You are not allowed to read the contents of folder %1$s (%2$d). */
    NO_READ_PERMISSION("You are not allowed to read the contents of folder %1$s (%2$d).", Category.CATEGORY_PERMISSION_DENIED, 23),
    /** Tasks are disable for you (%d). */
    NO_TASKS("Tasks are disable for you (%d).", Category.CATEGORY_PERMISSION_DENIED, 24),
    /** You are not allowed to create tasks in folder %1$s (%2$d). */
    NO_CREATE_PERMISSION("You are not allowed to create tasks in folder %1$s (%2$d).", Category.CATEGORY_PERMISSION_DENIED, 25),
    /** You are not allowed to delegate tasks. */
    NO_DELEGATE_PERMISSION("You are not allowed to delegate tasks.", Category.CATEGORY_PERMISSION_DENIED, 26),
    /** Missing folder id for creating task. */
    FOLDER_IS_MISSING("Missing folder id for creating task.", Category.CATEGORY_ERROR, 27),
    /** Private flagged tasks cannot be delegated. */
    NO_PRIVATE_DELEGATE("Private flagged tasks cannot be delegated.", Category.CATEGORY_USER_INPUT, 28),
    /** Percent is %d but must be between 0 and 100. */
    INVALID_PERCENTAGE("Percent is %d but must be between 0 and 100.", Category.CATEGORY_USER_INPUT, 30),
    /** For tasks which are not started the percentage done must be 0 and not %d. */
    PERCENTAGE_NOT_ZERO("For tasks which are not started the percentage done must be 0 and not %d.", Category.CATEGORY_USER_INPUT, 31),
    /** cannot send event to event system. */
    EVENT("Cannot send event to event system.", Category.CATEGORY_ERROR, 32),
    /** You are not allowed to edit tasks in folder %1$s (%2$d). */
    NO_WRITE_PERMISSION("You are not allowed to edit tasks in folder %1$s (%2$d).", Category.CATEGORY_PERMISSION_DENIED, 33),
    /** Moving items from or into shared folder %1$s (%2$d) is not allowed. */
    NO_SHARED_MOVE("Moving items from or into shared folder %1$s (%2$d) is not allowed.", Category.CATEGORY_PERMISSION_DENIED, 34),
    /** Missing folder mapping for task %1$d. */
    MISSING_FOLDER("Missing folder mapping for task %1$d.", Category.CATEGORY_ERROR, 35),
    /** Unknown recurrence type %d. */
    UNKNOWN_RECURRENCE("Unknown recurrence type %d.", Category.CATEGORY_USER_INPUT, 36),
    /** Value for the recurrence is missing: %d. */
    MISSING_RECURRENCE_VALUE("Value for the recurrence is missing: %d.", Category.CATEGORY_USER_INPUT, 37),
    /** For finished tasks the percentage must be 100 and not %d. */
    PERCENTAGE_NOT_FULL("For finished tasks the percentage must be 100 and not %d.", Category.CATEGORY_USER_INPUT, 38),
    /** Invalid task state %d. */
    INVALID_TASK_STATE("Invalid task state %d.", Category.CATEGORY_ERROR, 39),
    /** Start date %1$s must be before end date %2$s. */
    START_NOT_BEFORE_END("Start date %1$s must be before end date %2$s.", Category.CATEGORY_USER_INPUT, 40),
    /** The task could not be saved. Please shorten the %1$s and try again. Current length %3$d is more than allowed length of %2$d
     * characters. */
    TRUNCATED("The task could not be saved. Please shorten the %1$s and try again. Current length %3$d is more than allowed length of %2$d characters.", Category.CATEGORY_TRUNCATED, 41),
    /** Task with private flag cannot be moved to public folder %1$s (%2$d). */
    NO_PRIVATE_MOVE_TO_PUBLIC("Task with private flags cannot be moved to public folder %1$s (%2$d).", Category.CATEGORY_PERMISSION_DENIED, 42),
    /** Only the task creator is allowed to set private flag. */
    ONLY_CREATOR_PRIVATE("Only the task creator is allowed to set private flag.", Category.CATEGORY_USER_INPUT, 43),
    /** Cannot add external participant without email address. */
    EXTERNAL_WITHOUT_MAIL("Cannot add external participant without email address.", Category.CATEGORY_USER_INPUT, 44),
    /** Problem with a thread. */
    THREAD_ISSUE("Problem with a thread.", Category.CATEGORY_ERROR, 45),
    /** You are not allowed to see the task %1$d in folder %2$s (%3$d). */
    NO_PERMISSION("You are not allowed to see the task %1$d in folder %2$s (%3$d).", Category.CATEGORY_PERMISSION_DENIED, 46),
    /** Task contains invalid data: "%1$s" */
    INVALID_DATA("Task contains invalid data: \"%1$s\"", Category.CATEGORY_USER_INPUT, 47),
    /** The task %1$d is not stored in folder %2$s (%3$d). */
    NOT_IN_FOLDER("The task %1$d is not stored in folder %2$s (%3$d).", Category.CATEGORY_PERMISSION_DENIED, 48),
    /** Unknown participant type %1$d. */
    UNKNOWN_PARTICIPANT("Unknown participant type %1$d.", Category.CATEGORY_ERROR, 49),
    /** The entered value for costs is not within the allowed range. Please use a value from -130000.00 to 130000.00. */
    COSTS_OFF_LIMIT("The entered value for costs is not within the allowed range. Please use a value from -130000.00 to 130000.00.", Category.CATEGORY_USER_INPUT, 50),
    /** In order to accomplish the search, %1$d or more characters are required. */
    PATTERN_TOO_SHORT("In order to accomplish the search, %1$d or more characters are required.", Category.CATEGORY_USER_INPUT, 51),
    /** Group %1$d is empty. You can't add an empty group to a task. */
    GROUP_IS_EMPTY("Group %1$d is empty. You can't add an empty group to a task.", Category.CATEGORY_USER_INPUT, 52),
    /** UID of tasks can not be changed. */
    NO_UID_CHANGE("UID of tasks can not be changed.", Category.CATEGORY_USER_INPUT, 53);

    private final String message;
    private final Category category;
    private final int number;

    private TaskExceptionCode(final String message, final Category category,
        final int number) {
        this.message = message;
        this.category = category;
        this.number = number;
    }

    @Override
    public String getPrefix() {
        return "TSK";
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public int getNumber() {
        return number;
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