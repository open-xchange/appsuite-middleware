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

package com.openexchange.groupware.tasks;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link TaskExceptionMessage}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class TaskExceptionMessage implements LocalizableStrings {

    private TaskExceptionMessage() {
        super();
    }

    // Error while inserting task: %s.
    public static final String INSERT_FAILED_MSG = "Error while inserting task: %s.";

    // Setting autoCommit to true failed.
    public static final String AUTO_COMMIT_MSG = "Problem setting auto commit to true.";

    // A database connection cannot be obtained.
    public static final String NO_CONNECTION_MSG = "Cannot get database connection.";

    // Unimplemented feature.
    public static final String UNIMPLEMENTED_MSG = "This method is not implemented.";

    // SQL Problem.
    public static final String SQL_ERROR_MSG = "SQL Problem.";

    // Folder %1$s (%2$d) is not a task folder.
    public static final String NOT_TASK_FOLDER_MSG = "Folder %1$s (%2$d) is not a task folder.";

    // Edit conflict. Your change cannot be completed because somebody else has made a conflicting change to the same item. Please refresh or synchronize and try again.
    public static final String MODIFIED_MSG = "Edit conflict. Your change cannot be completed because somebody else has made a conflicting change to the same item. Please refresh or synchronize and try again.";

    // Cannot create private task in public/shared folder %1$d.
    public static final String PRIVATE_FLAG_MSG = "Cannot create private task in public/shared folder %1$d.";

    // SQL problem while updating task: %s.
    public static final String UPDATE_FAILED_MSG = "SQL problem while updating task: %s.";

    // Counting tasks did not return a result.
    public static final String NO_COUNT_RESULT_MSG = "Counting tasks did not return a result.";

    // SQL problem while deleting task: %s.
    public static final String DELETE_FAILED_MSG = "SQL problem while deleting task: %s.";

    // Cannot find folder for task %2$d and participant %1$d in context %3$d.
    public static final String PARTICIPANT_FOLDER_INCONSISTENCY_MSG = "Cannot find folder for task %2$d and participant %1$d in context %3$d.";

    // SQL problem while listing tasks: %s.
    public static final String SEARCH_FAILED_MSG = "SQL problem while listing tasks: %s.";

    // You are not allowed to delete the task %d.
    public static final String NO_DELETE_PERMISSION_MSG = "You are not allowed to delete the task.";

    // Tried to delete %1$d folders but only %2$d were deleted.
    public static final String FOLDER_DELETE_WRONG_MSG = "Tried to delete %1$d folders but only %2$d were deleted.";

    // Tried to delete %1$d participants but only %2$d were deleted.
    public static final String PARTICIPANT_DELETE_WRONG_MSG = "Tried to delete %1$d participants but only %2$d were deleted.";

    // Participant %d for task %d is not found.
    public static final String PARTICIPANT_NOT_FOUND_MSG = "Participant %d for task %d is not found.";

    // Cannot find task %d in context %d.
    public static final String TASK_NOT_FOUND_MSG = "Cannot find task %d in context %d.";

    // Unknown task attribute %d.
    public static final String UNKNOWN_ATTRIBUTE_MSG = "Unknown task attribute %d.";

    // Date range in search must contain 2 and not %d values.
    public static final String WRONG_DATE_RANGE_MSG = "Date range in search must contain 2 and not %d values.";

    // Cannot detach more attachments than actually available.
    public static final String WRONG_ATTACHMENT_COUNT_MSG = "Cannot detach more attachments than actually available.";

    // You are not allowed to read the contents of folder %1$s (%2$d).
    public static final String NO_READ_PERMISSION_MSG = "You are not allowed to read the contents of folder %1$s (%2$d).";

    // Tasks are disable for you (%d).
    public static final String NO_TASKS_MSG = "Tasks are disabled for you (%d).";

    // You are not allowed to create tasks in folder %1$s (%2$d).
    public static final String NO_CREATE_PERMISSION_MSG = "You are not allowed to create tasks in folder %1$s (%2$d).";

    // You are not allowed to delegate tasks.
    public static final String NO_DELEGATE_PERMISSION_MSG = "You are not allowed to delegate tasks.";

    // Missing folder id for creating task.
    public static final String FOLDER_IS_MISSING_MSG = "Missing folder id for creating task.";

    // Private flagged tasks cannot be delegated.
    public static final String NO_PRIVATE_DELEGATE_MSG = "Tasks with private flag cannot be delegated.";

    // Percent is %d but must be between 0 and 100.
    public static final String INVALID_PERCENTAGE_MSG = "Percent is %d but must be between 0 and 100.";

    // For tasks which are not started the percentage done must be 0 and not %d.
    public static final String PERCENTAGE_NOT_ZERO_MSG = "Tasks that have not been started yet need to have a 'Done' percentage of 0 and not %d.";

    // cannot send event to event system.
    public static final String EVENT_MSG = "Cannot send event to event system.";

    // You are not allowed to edit tasks in folder %1$s (%2$d).
    public static final String NO_WRITE_PERMISSION_MSG = "You are not allowed to edit tasks in folder %1$s (%2$d).";

    // Moving items from or into shared folder %1$s (%2$d) is not allowed.
    public static final String NO_SHARED_MOVE_MSG = "Moving items from or into shared folder %1$s (%2$d) is not allowed.";

    // Missing folder mapping for task %1$d.
    public static final String MISSING_FOLDER_MSG = "Missing folder mapping for task %1$d.";

    // Unknown recurrence type %d.
    public static final String UNKNOWN_RECURRENCE_MSG = "Unknown recurrence type %d.";

    // Value for the recurrence is missing: %d.
    public static final String MISSING_RECURRENCE_VALUE_MSG = "Value for the recurrence is missing: %d.";

    // For finished tasks the percentage must be 100 and not %d.
    public static final String PERCENTAGE_NOT_FULL_MSG = "Finished tasks need to have a percentage of 100 and not %d.";

    // Invalid task state %d.
    public static final String INVALID_TASK_STATE_MSG = "Invalid task state %d.";

    // Start date %1$s must be before end date %2$s.
    public static final String START_NOT_BEFORE_END_MSG = "Start date %1$s must be before end date %2$s.";

    // The task could not be saved. Please shorten the %1$s and try again. Current length %3$d is more than allowed length of %2$d
    // characters.
    public static final String TRUNCATED_MSG = "The task could not be saved. Please shorten the %1$s and try again. Current length %3$d is more than allowed length of %2$d characters.";

    // Task with private flag cannot be moved to public folder %1$s (%2$d).
    public static final String NO_PRIVATE_MOVE_TO_PUBLIC_MSG = "Tasks with private flag cannot be moved to a public folder %1$s (%2$d).";

    // Only the task creator is allowed to set private flag.
    public static final String ONLY_CREATOR_PRIVATE_MSG = "Only the task creator is allowed to set the private flag.";

    // Cannot add external participant without email address.
    public static final String EXTERNAL_WITHOUT_MAIL_MSG = "External participants without E-Mail address can not be added.";

    // Problem with a thread.
    public static final String THREAD_ISSUE_MSG = "Unexpected error.";

    // You are not allowed to see the task %1$d in folder %2$s (%3$d).
    public static final String NO_PERMISSION_MSG = "You are not allowed to see the task %1$d in folder %2$s (%3$d).";

    // Task contains invalid data: "%1$s"
    public static final String INVALID_DATA_MSG = "Task contains invalid data: \"%1$s\"";

    // The task %1$d is not stored in folder %2$s (%3$d).
    public static final String NOT_IN_FOLDER_MSG = "The task %1$d is not stored in folder %2$s (%3$d).";

    // Unknown participant type %1$d.
    public static final String UNKNOWN_PARTICIPANT_MSG = "Unknown participant type %1$d.";

    // In order to accomplish the search, %1$d or more characters are required.
    public static final String PATTERN_TOO_SHORT_MSG = "In order to accomplish the search, %1$d or more characters are required.";

    // Group %1$d is empty. You can't add an empty group to a task.
    public static final String GROUP_IS_EMPTY_MSG = "Group %1$d is empty. You can not add an empty group to a task.";

    // UID of tasks can not be changed.
    public static final String NO_UID_CHANGE_MSG = "Tasks uids can not be changed.";

    // Into a numeric attribute something non numeric has been entered. %2$s will name the attribute that contains non digit characters.
    public static final String CONTAINS_NON_DIGITS_MSG = "Value \"%1$s\" of attribute \"%2$s\" contains non digit characters.";

    // A participant changed his confirmation state of a delegated task and we are not able to determine who delegated the task. %1$d is
    // replaced with the identifier of the task for further investigations.
    public static final String UNKNOWN_DELEGATOR_MSG = "Can not determine delegator of task %1$d.";

}
