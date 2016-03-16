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

package com.openexchange.groupware.tasks;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXException.Generic;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;
import com.openexchange.groupware.contact.ContactExceptionMessages;

/**
 * Error codes for task exceptions.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public enum TaskExceptionCode implements DisplayableOXExceptionCode {

    /** Error while inserting task: %s. */
    INSERT_FAILED("Error while inserting task: %s.", TaskExceptionMessage.INSERT_FAILED_MSG, Category.CATEGORY_ERROR, 1),

    /** Setting autoCommit to true failed. */
    AUTO_COMMIT("Problem setting auto commit to true.", Category.CATEGORY_SERVICE_DOWN, 2),

    /** A database connection cannot be obtained. */
    NO_CONNECTION("Cannot get database connection.", Category.CATEGORY_SERVICE_DOWN, 3),

    /** Unimplemented feature. */
    UNIMPLEMENTED("This method is not implemented.", Category.CATEGORY_ERROR, 4),

    /** SQL Problem. */
    SQL_ERROR("SQL Problem.", Category.CATEGORY_ERROR, 5),

    /** Folder %1$s (%2$d) is not a task folder. */
    NOT_TASK_FOLDER("Folder %1$s (%2$d) is not a task folder.", TaskExceptionMessage.NOT_TASK_FOLDER_MSG,
        Category.CATEGORY_PERMISSION_DENIED, 6),

    /**
     * Edit conflict. Your change cannot be completed because somebody else has
     * made a conflicting change to the same item. Please refresh or synchronize
     * and try again.
     */
    MODIFIED("Edit conflict. Your change cannot be completed because somebody else has made a conflicting change to the same item. "
        + "Please refresh or synchronize and try again.", TaskExceptionMessage.MODIFIED_MSG, Category.CATEGORY_CONFLICT, 7),

    /** Cannot create private task in public/shared folder %1$d. */
    PRIVATE_FLAG("Cannot create private task in public/shared folder %1$d.", TaskExceptionMessage.PRIVATE_FLAG_MSG,
        Category.CATEGORY_USER_INPUT, 8),

    /** SQL problem while updating task: %s. */
    UPDATE_FAILED("SQL problem while updating task: %s.", TaskExceptionMessage.UPDATE_FAILED_MSG, Category.CATEGORY_ERROR, 9),

    /** Counting tasks did not return a result. */
    NO_COUNT_RESULT("Counting tasks did not return a result.", Category.CATEGORY_ERROR, 10),

    /** SQL problem while deleting task: %s. */
    DELETE_FAILED("SQL problem while deleting task: %s.", TaskExceptionMessage.DELETE_FAILED_MSG, Category.CATEGORY_ERROR, 11),

    /** Cannot find folder for task %2$d and participant %1$d in context %3$d. */
    PARTICIPANT_FOLDER_INCONSISTENCY("Cannot find folder for task %2$d and participant %1$d in context %3$d.", Category.CATEGORY_ERROR, 12),

    /** SQL problem while listing tasks: %s. */
    SEARCH_FAILED("SQL problem while listing tasks: %s.", TaskExceptionMessage.SEARCH_FAILED_MSG, Category.CATEGORY_ERROR, 13),

    /** You are not allowed to delete the task %d. */
    NO_DELETE_PERMISSION("You are not allowed to delete the task.", TaskExceptionMessage.NO_DELETE_PERMISSION_MSG,
        Category.CATEGORY_PERMISSION_DENIED, 14),

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
    WRONG_DATE_RANGE("Date range in search must contain 2 and not %d values.", TaskExceptionMessage.WRONG_DATE_RANGE_MSG,
        Category.CATEGORY_USER_INPUT, 21),

    /** Cannot detach more attachments than actually available. */
    WRONG_ATTACHMENT_COUNT("Cannot detach more attachments than actually available.", Category.CATEGORY_ERROR, 22),

    /** You are not allowed to read the contents of folder %1$s (%2$d). */
    NO_READ_PERMISSION("You are not allowed to read the contents of folder %1$s (%2$d).", TaskExceptionMessage.NO_READ_PERMISSION_MSG,
        Category.CATEGORY_PERMISSION_DENIED, 23),

    /** Tasks are disable for you (%d). */
    NO_TASKS("Tasks are disabled for you (%d).", TaskExceptionMessage.NO_TASKS_MSG, Category.CATEGORY_PERMISSION_DENIED, 24),

    /** You are not allowed to create tasks in folder %1$s (%2$d). */
    NO_CREATE_PERMISSION("You are not allowed to create tasks in folder %1$s (%2$d).", TaskExceptionMessage.NO_CREATE_PERMISSION_MSG,
        Category.CATEGORY_PERMISSION_DENIED, 25),

    /** You are not allowed to delegate tasks. */
    NO_DELEGATE_PERMISSION("You are not allowed to delegate tasks.", TaskExceptionMessage.NO_DELEGATE_PERMISSION_MSG,
        Category.CATEGORY_PERMISSION_DENIED, 26),

    /** Missing folder id for creating task. */
    FOLDER_IS_MISSING("Missing folder id for creating task.", Category.CATEGORY_ERROR, 27),

    /** Private flagged tasks cannot be delegated. */
    NO_PRIVATE_DELEGATE("Tasks with private flag cannot be delegated.", TaskExceptionMessage.NO_PRIVATE_DELEGATE_MSG,
        Category.CATEGORY_USER_INPUT, 28),

    /** Percent is %d but must be between 0 and 100. */
    INVALID_PERCENTAGE("Percent is %d but must be between 0 and 100.", TaskExceptionMessage.INVALID_PERCENTAGE_MSG,
        Category.CATEGORY_USER_INPUT, 30),

    /**
     * For tasks which are not started the percentage done must be 0 and not %d.
     */
    PERCENTAGE_NOT_ZERO("Tasks that have not been started yet need to have a 'Done' percentage of 0 and not %d.",
        TaskExceptionMessage.PERCENTAGE_NOT_ZERO_MSG, Category.CATEGORY_USER_INPUT, 31),

    /** cannot send event to event system. */
    EVENT("Cannot send event to event system.", Category.CATEGORY_ERROR, 32),

    /** You are not allowed to edit tasks in folder %1$s (%2$d). */
    NO_WRITE_PERMISSION("You are not allowed to edit tasks in folder %1$s (%2$d).", TaskExceptionMessage.NO_WRITE_PERMISSION_MSG,
        Category.CATEGORY_PERMISSION_DENIED, 33),

    /** Moving items from or into shared folder %1$s (%2$d) is not allowed. */
    NO_SHARED_MOVE("Moving items from or into shared folder %1$s (%2$d) is not allowed.", TaskExceptionMessage.NO_SHARED_MOVE_MSG,
        Category.CATEGORY_PERMISSION_DENIED, 34),

    /** Missing folder mapping for task %1$d. */
    MISSING_FOLDER("Missing folder mapping for task %1$d.", Category.CATEGORY_ERROR, 35),

    /** Unknown recurrence type %d. */
    UNKNOWN_RECURRENCE("Unknown recurrence type %d.", TaskExceptionMessage.UNKNOWN_RECURRENCE_MSG, Category.CATEGORY_USER_INPUT, 36),

    /** Value for the recurrence is missing: %d. */
    MISSING_RECURRENCE_VALUE("Value for the recurrence is missing: %d.", TaskExceptionMessage.MISSING_RECURRENCE_VALUE_MSG,
        Category.CATEGORY_USER_INPUT, 37),

    /** For finished tasks the percentage must be 100 and not %d. */
    PERCENTAGE_NOT_FULL("Finished tasks need to have a percentage of 100 and not %d.", TaskExceptionMessage.PERCENTAGE_NOT_FULL_MSG,
        Category.CATEGORY_USER_INPUT, 38),

    /** Invalid task state %d. */
    INVALID_TASK_STATE("Invalid task state %d.", Category.CATEGORY_ERROR, 39),

    /** Start date %1$s must be before end date %2$s. */
    START_NOT_BEFORE_END("Start date %1$s must be before end date %2$s.", TaskExceptionMessage.START_NOT_BEFORE_END_MSG,
        Category.CATEGORY_USER_INPUT, 40),

    /**
     * The task could not be saved. Please shorten the %1$s and try again.
     * Current length %3$d is more than allowed length of %2$d characters.
     */
    TRUNCATED("The task could not be saved. Please shorten the %1$s and try again. Current length %3$d is more than allowed length"
        + " of %2$d characters.", TaskExceptionMessage.TRUNCATED_MSG, Category.CATEGORY_TRUNCATED, 41),

    /** Task with private flag cannot be moved to public folder %1$s (%2$d). */
    NO_PRIVATE_MOVE_TO_PUBLIC("Tasks with private flag cannot be moved to a public folder %1$s (%2$d).",
        TaskExceptionMessage.NO_PRIVATE_MOVE_TO_PUBLIC_MSG, Category.CATEGORY_PERMISSION_DENIED, 42),

    /** Only the task creator is allowed to set private flag. */
    ONLY_CREATOR_PRIVATE("Only the task creator is allowed to set the private flag.", TaskExceptionMessage.ONLY_CREATOR_PRIVATE_MSG,
        Category.CATEGORY_USER_INPUT, 43),

    /** Cannot add external participant without email address. */
    EXTERNAL_WITHOUT_MAIL("External participants without E-Mail address can not be added.", TaskExceptionMessage.EXTERNAL_WITHOUT_MAIL_MSG,
        Category.CATEGORY_USER_INPUT, 44),

    /** Problem with a thread. */
    THREAD_ISSUE("Unexpected error.", Category.CATEGORY_ERROR, 45),

    /** You are not allowed to see the task %1$d in folder %2$s (%3$d). */
    NO_PERMISSION("You are not allowed to see the task %1$d in folder %2$s (%3$d).", TaskExceptionMessage.NO_PERMISSION_MSG,
        Category.CATEGORY_PERMISSION_DENIED, 46),

    /** Task contains invalid data: "%1$s" */
    INVALID_DATA("Task contains invalid data: \"%1$s\"", TaskExceptionMessage.INVALID_DATA_MSG, Category.CATEGORY_USER_INPUT, 47),

    /** The task %1$d is not stored in folder %2$s (%3$d). */
    NOT_IN_FOLDER("The task %1$d is not stored in folder %2$s (%3$d).", TaskExceptionMessage.NOT_IN_FOLDER_MSG,
        Category.CATEGORY_PERMISSION_DENIED, 48),

    /** Unknown participant type %1$d. */
    UNKNOWN_PARTICIPANT("Unknown participant type %1$d.", Category.CATEGORY_ERROR, 49),

    /** In order to accomplish the search, %1$d or more characters are required. */
    PATTERN_TOO_SHORT("In order to accomplish the search, %1$d or more characters are required.",
        TaskExceptionMessage.PATTERN_TOO_SHORT_MSG, Category.CATEGORY_USER_INPUT, 51),

    /** Group %1$d is empty. You can't add an empty group to a task. */
    GROUP_IS_EMPTY("Group %1$d is empty. You can not add an empty group to a task.", TaskExceptionMessage.GROUP_IS_EMPTY_MSG,
        Category.CATEGORY_USER_INPUT, 52),

    /** UID of tasks can not be changed. */
    NO_UID_CHANGE("Tasks uids can not be changed.", Category.CATEGORY_ERROR, 53),

    /** SQL problem while deleting task: %s. */
    DELETE_FAILED_RETRY("SQL problem while deleting task: %s.", TaskExceptionMessage.DELETE_FAILED_MSG, Category.CATEGORY_TRY_AGAIN, 54),

    /** Value "%1$s" of attribute "%2$s" contains non digit characters. */
    CONTAINS_NON_DIGITS("Value \"%1$s\" of attribute \"%2$s\" contains non digit characters.", TaskExceptionMessage.CONTAINS_NON_DIGITS_MSG,
        Category.CATEGORY_USER_INPUT, 55),

    /** Can not determine delegator of task %1$d. */
    UNKNOWN_DELEGATOR("Can not determine delegator of task %1$d.", Category.CATEGORY_ERROR, 56),

    /** Priority is %d but mist be between 1 and 3. */
    INVALID_PRIORITY(TaskExceptionMessage.INVALID_PRIORITY_MSG, TaskExceptionMessage.INVALID_PRIORITY_MSG, Category.CATEGORY_USER_INPUT, 57),

    /** "The character \"%1$s\" in field \"%2$s\" can't be saved. Please remove the problematic character and try again." */
    INCORRECT_STRING("Field \"%2$s\" contains invalid character: \"%1$s\"", TaskExceptionMessage.INCORRECT_STRING_DISPLAY, Category.CATEGORY_USER_INPUT, 58);

    private String message;

    private String displayMessage;

    private Category category;

    private int number;

    private TaskExceptionCode(String message, String displayMessage, Category category, int number) {
        this.message = message;
        this.displayMessage = displayMessage != null ? displayMessage : OXExceptionStrings.MESSAGE;
        this.category = category;
        this.number = number;
    }

    private TaskExceptionCode(String message, Category category, int number) {
        this(message, null, category, number);
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
    public String getDisplayMessage() {
        return displayMessage;
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
    public boolean equals(OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     * @return The newly created {@link OXException} instance
     */
    public OXException create() {
        return specials(OXExceptionFactory.getInstance().create(this, new Object[0]));
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(Object... args) {
        return specials(OXExceptionFactory.getInstance().create(this, (Throwable) null, args));
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     * @param cause The optional initial cause
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(Throwable cause, Object... args) {
        return specials(OXExceptionFactory.getInstance().create(this, cause, args));
    }

    private OXException specials(OXException exc) {
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
        default:
        }
        return exc;
    }
}
