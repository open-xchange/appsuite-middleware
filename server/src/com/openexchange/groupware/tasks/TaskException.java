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

package com.openexchange.groupware.tasks;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.EnumComponent;

/**
 * This exception will be thrown if exceptions in the tasks occur.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class TaskException extends AbstractOXException {

    /**
     * More detailed information about the exception.
     */
    private final Detail detail;

    /**
     * For serialization.
     */
    private static final long serialVersionUID = -3629038373770929623L;

    /**
     * Initialize e new exception using the information from the nested abstract
     * OX exception.
     * @param cause the cause.
     */
    public TaskException(final AbstractOXException cause) {
        super(cause);
        detail = Detail.OTHER;
    }

    /**
     * Initializes a new exception using the information provides by the code.
     * @param code code for the exception.
     * @param messageArgs arguments that will be formatted into the message.
     */
    public TaskException(final Code code, final Object... messageArgs) {
        this(code, null, messageArgs);
    }

    /**
     * Initializes a new exception using the information provides by the code.
     * @param code code for the exception.
     * @param cause the cause of the exception.
     * @param messageArgs arguments that will be formatted into the message.
     */
    public TaskException(final Code code, final Throwable cause,
        final Object... messageArgs) {
        super(EnumComponent.TASK, code.category, code.number, code.message, cause);
        detail = code.detail;
        setMessageArgs(messageArgs);
    }

    /**
     * @return Returns the detail.
     */
    public Detail getDetail() {
        return detail;
    }

    /**
     * Values for the detail attribute of this task exception.
     */
    public static enum Detail {
        /**
         * Detail information stating that a permission problem occurred.
         */
        PERMISSION,
        /**
         * Detail information stating that the task doesn't exist.
         */
        NOT_FOUND,
        /**
         * Detail information stating that a mandatory field is not set.
         */
        MANDATORY_FIELD,
        /**
         * Detail information stating that a conflict occurred.
         */
        CONFLICT,
        /**
         * Detail information stating that the task has been changed in the
         * between time.
         */
        CONCURRENT_MODIFICATION,
        /**
         * No detail information available.
         */
        OTHER,
        /**
         * Data has been truncated storing them in the database.
         */
        TRUNCATED
    }

    /**
     * Error codes for task exceptions.
     * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
     */
    public enum Code {
        /**
         * Error while inserting task: %s.
         */
        INSERT_FAILED("Error while inserting task: %s.",
            Category.CODE_ERROR, Detail.OTHER, 1),
        /**
         * Setting autoCommit to true failed.
         */
        AUTO_COMMIT("Problem setting auto commit to true.",
            Category.SUBSYSTEM_OR_SERVICE_DOWN, Detail.OTHER, 2),
        /**
         * A database connection cannot be obtained.
         */
        NO_CONNECTION("Cannot get database connection.",
            Category.SUBSYSTEM_OR_SERVICE_DOWN, Detail.OTHER, 3),
        /**
         * Unimplemented feature.
         */
        UNIMPLEMENTED("This method is not implemented.",
            Category.CODE_ERROR, Detail.OTHER, 4),
        /**
         * SQL Problem: "%s".
         */
        SQL_ERROR("SQL Problem: \"%s\".", Category.CODE_ERROR,
            Detail.OTHER, 5),
        /**
         * Folder %1$s (%2$d) is not a task folder.
         */
        NOT_TASK_FOLDER("Folder %1$s (%2$d) is not a task folder.",
            Category.PERMISSION, Detail.PERMISSION, 6),
        /**
         * Edit Conflict. Your change cannot be completed because somebody else
         * has made a conflicting change to the same item. Please refresh or
         * synchronize and try again.
         */
        MODIFIED("Edit Conflict. Your change cannot be completed because "
            + "somebody else has made a conflicting change to the same item. "
            + "Please refresh or synchronize and try again.", Category
            .CONCURRENT_MODIFICATION, Detail.CONCURRENT_MODIFICATION, 7),
        /**
         * Cannot create private task in public/shared folder %1$d.
         */
        PRIVATE_FLAG("Cannot create private task in public/shared folder %1$d.",
            Category.USER_INPUT, Detail.OTHER, 8),
        /**
         * SQL problem while updating task: %s.
         */
        UPDATE_FAILED("SQL problem while updating task: %s.",
            Category.CODE_ERROR, Detail.OTHER, 9),
        /**
         * Counting tasks did not return a result.
         */
        NO_COUNT_RESULT("Counting tasks did not return a result.",
            Category.CODE_ERROR, Detail.OTHER, 10),
        /**
         * SQL problem while deleting task: %s.
         */
        DELETE_FAILED("SQL problem while deleting task: %s.",
            Category.CODE_ERROR, Detail.OTHER, 11),
        /**
         * Cannot find folder of task participant.
         */
        PARTICIPANT_FOLDER_INCONSISTENCY("Cannot find folder of task "
            + "participant.", Category.CODE_ERROR, Detail.OTHER, 12),
        /**
         * SQL problem while listing tasks: %s.
         */
        SEARCH_FAILED("SQL problem while listing tasks: %s.",
            Category.CODE_ERROR, Detail.OTHER, 13),
        /**
         * You are not allowed to delete the task %d.
         */
        NO_DELETE_PERMISSION("You are not allowed to delete the task.",
            Category.PERMISSION, Detail.PERMISSION, 14),
        /**
         * Tried to delete %1$d folders but only %2$d were deleted.
         */
        FOLDER_DELETE_WRONG("Tried to delete %1$d folders but only %2$d were "
            + "deleted.", Category.CODE_ERROR, Detail.OTHER, 15),
        /**
         * Tried to delete %1$d participants but only %2$d were deleted.
         */
        PARTICIPANT_DELETE_WRONG("Tried to delete %1$d participants but only "
            + "%2$d were deleted.", Category.CODE_ERROR, Detail.OTHER,
            16),
        /**
         * Participant %d for task %d is not found.
         */
        PARTICIPANT_NOT_FOUND("Participant %d for task %d is not found.",
            Category.CODE_ERROR, Detail.NOT_FOUND, 18),
        /**
         * Cannot find task %d in context %d.
         */
        TASK_NOT_FOUND("Cannot find task %d in context %d.",
            Category.CODE_ERROR, Detail.NOT_FOUND, 19),
        /**
         * Unknown task attribute %d.
         */
        UNKNOWN_ATTRIBUTE("Unknown task attribute %d.",
            Category.CODE_ERROR, Detail.OTHER, 20),
        /**
         * Date range in search must contain 2 and not %d values.
         */
        WRONG_DATE_RANGE("Date range in search must contain 2 and not %d "
            + "values.", Category.CODE_ERROR, Detail.MANDATORY_FIELD,
            21),
        /**
         * Cannot decrease number of attachments below zero.
         */
        WRONG_ATTACHMENT_COUNT("Cannot decrease number of attachments below "
            + "zero.", Category.CODE_ERROR, Detail.OTHER, 22),
        /**
         * You are not allowed to read the contents of folder %1$s (%2$d).
         */
        NO_READ_PERMISSION("You are not allowed to read the contents of folder "
            + "%1$s (%2$d).", Category.PERMISSION, Detail.PERMISSION, 23),
        /**
         * Tasks are disable for you (%d).
         */
        NO_TASKS("Tasks are disable for you (%d).",
            Category.PERMISSION, Detail.PERMISSION, 24),
        /**
         * You are not allowed to create tasks in folder %1$s (%2$d).
         */
        NO_CREATE_PERMISSION("You are not allowed to create tasks in folder "
            + "%1$s (%2$d).", Category.PERMISSION, Detail.PERMISSION, 25),
        /**
         * You are not allowed to delegate tasks.
         */
        NO_DELEGATE_PERMISSION("You are not allowed to delegate tasks.",
            Category.PERMISSION, Detail.PERMISSION, 26),
        /**
         * Missing folder id for creating task.
         */
        FOLDER_IS_MISSING("Missing folder id for creating task.",
            Category.CODE_ERROR, Detail.MANDATORY_FIELD, 27),
        /**
         * Private flagged tasks cannot be delegated.
         */
        NO_PRIVATE_DELEGATE("Private flagged tasks cannot be delegated.",
            Category.USER_INPUT, Detail.OTHER, 28),
        /**
         * Percent is %d but must be between 0 and 100.
         */
        INVALID_PERCENTAGE("Percent is %d but must be between 0 and 100.",
            Category.USER_INPUT, Detail.MANDATORY_FIELD, 30),
        /**
         * For tasks which are not started the percentage done must be 0 and not
         * %d.
         */
        PERCENTAGE_NOT_ZERO("For tasks which are not started the percentage "
            + "done must be 0 and not %d.", Category.USER_INPUT,
            Detail.MANDATORY_FIELD, 31),
        /**
         * cannot send event to event system.
         */
        EVENT("Cannot send event to event system.", Category.CODE_ERROR,
            Detail.OTHER, 32),
        /**
         * You are not allowed to edit tasks in folder %1$s (%2$d).
         */
        NO_WRITE_PERMISSION("You are not allowed to edit tasks in folder "
            + "%1$s (%2$d).", Category.PERMISSION, Detail.PERMISSION, 33),
        /**
         * Moving items from or into shared folder %1$s (%2$d) is not allowed.
         */
        NO_SHARED_MOVE("Moving items from or into shared folder %1$s (%2$d) is "
            + "not allowed.", Category.PERMISSION, Detail.PERMISSION, 34),
        /**
         * Unknown recurrence type %d.
         */
        UNKNOWN_RECURRENCE("Unknown recurrence type %d.", Category.USER_INPUT,
            Detail.MANDATORY_FIELD, 36),
        /**
         * Value for the recurrence is missing: %d.
         */
        MISSING_RECURRENCE_VALUE("Value for the recurrence is missing: %d.",
            Category.USER_INPUT, Detail.MANDATORY_FIELD, 37),
        /**
         * For finished tasks the percentage must be 100 and not %d.
         */
        PERCENTAGE_NOT_FULL("For finished tasks the percentage must be 100 and "
            + "not %d.", Category.USER_INPUT, Detail.MANDATORY_FIELD, 38),
        /**
         * Invalid task state %d.
         */
        INVALID_TASK_STATE("Invalid task state %d.",
            Category.CODE_ERROR, Detail.OTHER, 39),
        /**
         * Start date %1$s must be before end date %2$s.
         */
        START_NOT_BEFORE_END("Start date %1$s must be before end date %2$s.",
            Category.USER_INPUT, Detail.CONFLICT, 40),
        /**
         * The attribute %1$s has been truncated.
         */
        TRUNCATED("The attribute %1$s has been truncated.",
            Category.TRUNCATED, Detail.TRUNCATED, 41),
        /**
         * Task with private flag cannot be moved to public folder %1$s (%2$d).
         */
        NO_PRIVATE_MOVE_TO_PUBLIC("Task with private flags cannot be moved to "
            + "public folder %1$s (%2$d).", Category.PERMISSION,
            Detail.PERMISSION, 42),
        /**
         * Only the task creator is allowed to set private flag.
         */
        ONLY_CREATOR_PRIVATE("Only the task creator is allowed to set private "
            + "flag.", Category.USER_INPUT, Detail.PERMISSION, 43),
        /**
         * Cannot add external participant without email address.
         */
        EXTERNAL_WITHOUT_MAIL("Cannot add external participant without email "
            + "address.", Category.USER_INPUT, Detail.MANDATORY_FIELD, 44),
        /**
         * Problem with a thread.
         */
        THREAD_ISSUE("Problem with a thread.", Category.CODE_ERROR,
            Detail.OTHER, 45),
        /**
         * You are not allowed to see the task %1$d in folder %2$s (%3$d).
         */
        NO_PERMISSION("You are not allowed to see the task %1$d in folder %2$s "
            + "(%3$d).", Category.PERMISSION, Detail.PERMISSION, 46),
        /**
         * Task contains invalid data: "%1$s"
         */
        INVALID_DATA("Task contains invalid data: \"%1$s\"", Category
            .USER_INPUT, Detail.OTHER, 47);

        /**
         * Message of the exception.
         */
        private final String message;

        /**
         * Category of the exception.
         */
        private final Category category;

        /**
         * Detail of the exception.
         */
        private final Detail detail;

        /**
         * Detail number of the exception.
         */
        private final int number;

        /**
         * Default constructor.
         * @param message message.
         * @param category category.
         * @param detail Detail.
         * @param number detail number.
         */
        private Code(final String message, final Category category,
            final Detail detail, final int number) {
            this.message = message;
            this.category = category;
            this.detail = detail;
            this.number = number;
        }

        /**
         * @return the message
         */
        public String getMessage() {
            return message;
        }

        /**
         * @return the category
         */
        public Category getCategory() {
            return category;
        }

        /**
         * @return the number
         */
        public int getNumber() {
            return number;
        }
    }
}
