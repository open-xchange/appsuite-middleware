package com.openexchange.groupware.tasks;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXException.Generic;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

/**
 * Error codes for task exceptions.
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public enum TaskExceptionCode implements OXExceptionCode {
	/** Error while inserting task: %s. */
	INSERT_FAILED(TaskExceptionMessage.INSERT_FAILED_MSG,
			Category.CATEGORY_ERROR, 1),
	/** Setting autoCommit to true failed. */
	AUTO_COMMIT(TaskExceptionMessage.AUTO_COMMIT_MSG,
			Category.CATEGORY_SERVICE_DOWN, 2),
	/** A database connection cannot be obtained. */
	NO_CONNECTION(TaskExceptionMessage.NO_CONNECTION_MSG,
			Category.CATEGORY_SERVICE_DOWN, 3),
	/** Unimplemented feature. */
	UNIMPLEMENTED(TaskExceptionMessage.UNIMPLEMENTED_MSG,
			Category.CATEGORY_ERROR, 4),
	/** SQL Problem. */
	SQL_ERROR(TaskExceptionMessage.SQL_ERROR_MSG, Category.CATEGORY_ERROR, 5),
	/** Folder %1$s (%2$d) is not a task folder. */
	NOT_TASK_FOLDER(TaskExceptionMessage.NOT_TASK_FOLDER_MSG,
			Category.CATEGORY_PERMISSION_DENIED, 6),
	/**
	 * Edit conflict. Your change cannot be completed because somebody else has
	 * made a conflicting change to the same item. Please refresh or synchronize
	 * and try again.
	 */
	MODIFIED(TaskExceptionMessage.MODIFIED_MSG, Category.CATEGORY_CONFLICT, 7),
	/** Cannot create private task in public/shared folder %1$d. */
	PRIVATE_FLAG(TaskExceptionMessage.PRIVATE_FLAG_MSG,
			Category.CATEGORY_USER_INPUT, 8),
	/** SQL problem while updating task: %s. */
	UPDATE_FAILED(TaskExceptionMessage.UPDATE_FAILED_MSG,
			Category.CATEGORY_ERROR, 9),
	/** Counting tasks did not return a result. */
	NO_COUNT_RESULT(TaskExceptionMessage.NO_COUNT_RESULT_MSG,
			Category.CATEGORY_ERROR, 10),
	/** SQL problem while deleting task: %s. */
	DELETE_FAILED(TaskExceptionMessage.DELETE_FAILED_MSG,
			Category.CATEGORY_ERROR, 11),
	/** Cannot find folder for task %2$d and participant %1$d in context %3$d. */
	PARTICIPANT_FOLDER_INCONSISTENCY(
			TaskExceptionMessage.PARTICIPANT_FOLDER_INCONSISTENCY_MSG,
			Category.CATEGORY_ERROR, 12),
	/** SQL problem while listing tasks: %s. */
	SEARCH_FAILED(TaskExceptionMessage.SEARCH_FAILED_MSG,
			Category.CATEGORY_ERROR, 13),
	/** You are not allowed to delete the task %d. */
	NO_DELETE_PERMISSION(TaskExceptionMessage.NO_DELETE_PERMISSION_MSG,
			Category.CATEGORY_PERMISSION_DENIED, 14),
	/** Tried to delete %1$d folders but only %2$d were deleted. */
	FOLDER_DELETE_WRONG(TaskExceptionMessage.FOLDER_DELETE_WRONG_MSG,
			Category.CATEGORY_ERROR, 15),
	/** Tried to delete %1$d participants but only %2$d were deleted. */
	PARTICIPANT_DELETE_WRONG(TaskExceptionMessage.PARTICIPANT_DELETE_WRONG_MSG,
			Category.CATEGORY_ERROR, 16),
	/** Participant %d for task %d is not found. */
	PARTICIPANT_NOT_FOUND(TaskExceptionMessage.PARTICIPANT_NOT_FOUND_MSG,
			Category.CATEGORY_ERROR, 18),
	/** Cannot find task %d in context %d. */
	TASK_NOT_FOUND(TaskExceptionMessage.TASK_NOT_FOUND_MSG,
			Category.CATEGORY_ERROR, 19),
	/** Unknown task attribute %d. */
	UNKNOWN_ATTRIBUTE(TaskExceptionMessage.UNKNOWN_ATTRIBUTE_MSG,
			Category.CATEGORY_ERROR, 20),
	/** Date range in search must contain 2 and not %d values. */
	WRONG_DATE_RANGE(TaskExceptionMessage.WRONG_DATE_RANGE_MSG,
			Category.CATEGORY_ERROR, 21),
	/** Cannot decrease number of attachments below zero. */
	WRONG_ATTACHMENT_COUNT(TaskExceptionMessage.WRONG_ATTACHMENT_COUNT_MSG,
			Category.CATEGORY_ERROR, 22),
	/** You are not allowed to read the contents of folder %1$s (%2$d). */
	NO_READ_PERMISSION(TaskExceptionMessage.NO_READ_PERMISSION_MSG,
			Category.CATEGORY_PERMISSION_DENIED, 23),
	/** Tasks are disable for you (%d). */
	NO_TASKS(TaskExceptionMessage.NO_TASKS_MSG,
			Category.CATEGORY_PERMISSION_DENIED, 24),
	/** You are not allowed to create tasks in folder %1$s (%2$d). */
	NO_CREATE_PERMISSION(TaskExceptionMessage.NO_CREATE_PERMISSION_MSG,
			Category.CATEGORY_PERMISSION_DENIED, 25),
	/** You are not allowed to delegate tasks. */
	NO_DELEGATE_PERMISSION(TaskExceptionMessage.NO_DELEGATE_PERMISSION_MSG,
			Category.CATEGORY_PERMISSION_DENIED, 26),
	/** Missing folder id for creating task. */
	FOLDER_IS_MISSING(TaskExceptionMessage.FOLDER_IS_MISSING_MSG,
			Category.CATEGORY_ERROR, 27),
	/** Private flagged tasks cannot be delegated. */
	NO_PRIVATE_DELEGATE(TaskExceptionMessage.NO_PRIVATE_DELEGATE_MSG,
			Category.CATEGORY_USER_INPUT, 28),
	/** Percent is %d but must be between 0 and 100. */
	INVALID_PERCENTAGE(TaskExceptionMessage.INVALID_PERCENTAGE_MSG,
			Category.CATEGORY_USER_INPUT, 30),
	/**
	 * For tasks which are not started the percentage done must be 0 and not %d.
	 */
	PERCENTAGE_NOT_ZERO(TaskExceptionMessage.PERCENTAGE_NOT_ZERO_MSG,
			Category.CATEGORY_USER_INPUT, 31),
	/** cannot send event to event system. */
	EVENT(TaskExceptionMessage.EVENT_MSG, Category.CATEGORY_ERROR, 32),
	/** You are not allowed to edit tasks in folder %1$s (%2$d). */
	NO_WRITE_PERMISSION(TaskExceptionMessage.NO_WRITE_PERMISSION_MSG,
			Category.CATEGORY_PERMISSION_DENIED, 33),
	/** Moving items from or into shared folder %1$s (%2$d) is not allowed. */
	NO_SHARED_MOVE(TaskExceptionMessage.NO_SHARED_MOVE_MSG,
			Category.CATEGORY_PERMISSION_DENIED, 34),
	/** Missing folder mapping for task %1$d. */
	MISSING_FOLDER(TaskExceptionMessage.MISSING_FOLDER_MSG,
			Category.CATEGORY_ERROR, 35),
	/** Unknown recurrence type %d. */
	UNKNOWN_RECURRENCE(TaskExceptionMessage.UNKNOWN_RECURRENCE_MSG,
			Category.CATEGORY_USER_INPUT, 36),
	/** Value for the recurrence is missing: %d. */
	MISSING_RECURRENCE_VALUE(TaskExceptionMessage.MISSING_RECURRENCE_VALUE_MSG,
			Category.CATEGORY_USER_INPUT, 37),
	/** For finished tasks the percentage must be 100 and not %d. */
	PERCENTAGE_NOT_FULL(TaskExceptionMessage.PERCENTAGE_NOT_FULL_MSG,
			Category.CATEGORY_USER_INPUT, 38),
	/** Invalid task state %d. */
	INVALID_TASK_STATE(TaskExceptionMessage.INVALID_TASK_STATE_MSG,
			Category.CATEGORY_ERROR, 39),
	/** Start date %1$s must be before end date %2$s. */
	START_NOT_BEFORE_END(TaskExceptionMessage.START_NOT_BEFORE_END_MSG,
			Category.CATEGORY_USER_INPUT, 40),
	/**
	 * The task could not be saved. Please shorten the %1$s and try again.
	 * Current length %3$d is more than allowed length of %2$d characters.
	 */
	TRUNCATED(TaskExceptionMessage.TRUNCATED_MSG, Category.CATEGORY_TRUNCATED,
			41),
	/** Task with private flag cannot be moved to public folder %1$s (%2$d). */
	NO_PRIVATE_MOVE_TO_PUBLIC(
			TaskExceptionMessage.NO_PRIVATE_MOVE_TO_PUBLIC_MSG,
			Category.CATEGORY_PERMISSION_DENIED, 42),
	/** Only the task creator is allowed to set private flag. */
	ONLY_CREATOR_PRIVATE(TaskExceptionMessage.ONLY_CREATOR_PRIVATE_MSG,
			Category.CATEGORY_USER_INPUT, 43),
	/** Cannot add external participant without email address. */
	EXTERNAL_WITHOUT_MAIL(TaskExceptionMessage.EXTERNAL_WITHOUT_MAIL_MSG,
			Category.CATEGORY_USER_INPUT, 44),
	/** Problem with a thread. */
	THREAD_ISSUE(TaskExceptionMessage.THREAD_ISSUE_MSG,
			Category.CATEGORY_ERROR, 45),
	/** You are not allowed to see the task %1$d in folder %2$s (%3$d). */
	NO_PERMISSION(TaskExceptionMessage.NO_PERMISSION_MSG,
			Category.CATEGORY_PERMISSION_DENIED, 46),
	/** Task contains invalid data: "%1$s" */
	INVALID_DATA(TaskExceptionMessage.INVALID_DATA_MSG,
			Category.CATEGORY_USER_INPUT, 47),
	/** The task %1$d is not stored in folder %2$s (%3$d). */
	NOT_IN_FOLDER(TaskExceptionMessage.NOT_IN_FOLDER_MSG,
			Category.CATEGORY_PERMISSION_DENIED, 48),
	/** Unknown participant type %1$d. */
	UNKNOWN_PARTICIPANT(TaskExceptionMessage.UNKNOWN_PARTICIPANT_MSG,
			Category.CATEGORY_ERROR, 49),
	/**
	 * The entered value for costs is not within the allowed range. Please use a
	 * value from -130000.00 to 130000.00.
	 */
	COSTS_OFF_LIMIT(TaskExceptionMessage.COSTS_OFF_LIMIT_MSG,
			Category.CATEGORY_USER_INPUT, 50),
	/** In order to accomplish the search, %1$d or more characters are required. */
	PATTERN_TOO_SHORT(TaskExceptionMessage.PATTERN_TOO_SHORT_MSG,
			Category.CATEGORY_USER_INPUT, 51),
	/** Group %1$d is empty. You can't add an empty group to a task. */
	GROUP_IS_EMPTY(TaskExceptionMessage.GROUP_IS_EMPTY_MSG,
			Category.CATEGORY_USER_INPUT, 52),
	/** UID of tasks can not be changed. */
	NO_UID_CHANGE(TaskExceptionMessage.NO_UID_CHANGE_MSG,
			Category.CATEGORY_USER_INPUT, 53),
	/** SQL problem while deleting task: %s. */
    DELETE_FAILED_RETRY(TaskExceptionMessage.DELETE_FAILED_MSG,
            Category.CATEGORY_TRY_AGAIN, 54),
		            ;

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
	 * Creates a new {@link OXException} instance pre-filled with this code's
	 * attributes.
	 * 
	 * @return The newly created {@link OXException} instance
	 */
	public OXException create() {
		return specials(OXExceptionFactory.getInstance().create(this,
				new Object[0]));
	}

	/**
	 * Creates a new {@link OXException} instance pre-filled with this code's
	 * attributes.
	 * 
	 * @param args
	 *            The message arguments in case of printf-style message
	 * @return The newly created {@link OXException} instance
	 */
	public OXException create(final Object... args) {
		return specials(OXExceptionFactory.getInstance().create(this,
				(Throwable) null, args));
	}

	/**
	 * Creates a new {@link OXException} instance pre-filled with this code's
	 * attributes.
	 * 
	 * @param cause
	 *            The optional initial cause
	 * @param args
	 *            The message arguments in case of printf-style message
	 * @return The newly created {@link OXException} instance
	 */
	public OXException create(final Throwable cause, final Object... args) {
		return specials(OXExceptionFactory.getInstance().create(this, cause,
				args));
	}

	private OXException specials(final OXException exc) {
		switch(this) {
		case TASK_NOT_FOUND: 
			exc.setGeneric(Generic.NOT_FOUND);
			break;
		case NO_PERMISSION: case NO_WRITE_PERMISSION:
			exc.setGeneric(Generic.NO_PERMISSION);
			break;
		case MODIFIED:
			exc.setGeneric(Generic.CONFLICT);
			break;
		}
		return exc;
	}
}
