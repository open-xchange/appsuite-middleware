/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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

    public static final String INSERT_FAILED_MSG = "Error while inserting task.";

    public static final String FOLDER_NOT_FOUND_MSG = "Folder cannot be found.";

    public static final String MODIFIED_MSG = "Edit conflict. Your change cannot be completed because somebody else has made a conflicting change to the same item. Please refresh or synchronize and try again.";

    public static final String PRIVATE_FLAG_MSG = "Cannot create private task in public/shared folder.";

    public static final String UPDATE_FAILED_MSG = "Error while updating task.";

    public static final String DELETE_FAILED_MSG = "Error while deleting task.";

    public static final String SEARCH_FAILED_MSG = "Error while listing tasks.";

    public static final String NO_DELETE_PERMISSION_MSG = "You do not have the appropriate permissions to delete the task.";

    public static final String WRONG_DATE_RANGE_MSG = "Date range in search must contain 2 and not %d values.";

    public static final String NO_READ_PERMISSION_MSG = "You do not have the appropriate permissions to read the contents in that folder.";

    public static final String NO_TASKS_MSG = "Tasks are disabled for you.";

    public static final String NO_CREATE_PERMISSION_MSG = "You do not have the appropriate permissions to create tasks in that folder.";

    public static final String NO_DELEGATE_PERMISSION_MSG = "You do not have the appropriate permission to delegate tasks.";

    public static final String NO_PRIVATE_DELEGATE_MSG = "Tasks with private flag cannot be delegated.";

    public static final String INVALID_PERCENTAGE_MSG = "Percent is %d but must be between 0 and 100.";

    public static final String PERCENTAGE_NOT_ZERO_MSG = "Tasks that have not been started yet need to have a 'Done' percentage of 0 and not %d.";

    public static final String NO_WRITE_PERMISSION_MSG = "You do not have the appropriate permissions to edit tasks in that folder";

    public static final String NO_SHARED_MOVE_MSG = "Moving items from or into a shared folder is not allowed.";

    public static final String UNKNOWN_RECURRENCE_MSG = "Unknown recurrence type %d.";

    public static final String MISSING_RECURRENCE_VALUE_MSG = "Value for the recurrence is missing: %d.";

    public static final String PERCENTAGE_NOT_FULL_MSG = "Finished tasks need to have a percentage of 100 and not %d.";

    public static final String START_NOT_BEFORE_END_MSG = "Start date \"%1$s\" must be before end date \"%2$s\".";

    public static final String TRUNCATED_MSG = "The task could not be saved. Please shorten the \"%1$s\" and try again. Current length \"%3$d\" is more than allowed length"
        + " of \"%2$d\" characters.";

    public static final String NO_PRIVATE_MOVE_TO_PUBLIC_MSG = "Tasks with private flag cannot be moved to a public folder.";

    public static final String ONLY_CREATOR_PRIVATE_MSG = "Only the task creator is allowed to set the private flag.";

    public static final String EXTERNAL_WITHOUT_MAIL_MSG = "External participants without E-Mail address can not be added.";

    public static final String NO_PERMISSION_MSG = "You do not have the appropriate permisson to see the task.";

    public static final String INVALID_DATA_MSG = "Task contains invalid data: \"%1$s\".";

    public static final String PATTERN_TOO_SHORT_MSG = "In order to accomplish the search, %1$d or more characters are required.";

    public static final String GROUP_IS_EMPTY_MSG = "Group %1$d is empty. You can not add an empty group to a task.";

    public static final String CONTAINS_NON_DIGITS_MSG = "Value \"%1$s\" of attribute \"%2$s\" contains non digit characters.";

    public static final String INVALID_PRIORITY_MSG = "Priority is %d but must be between 1 and 3.";

    public final static String INCORRECT_STRING_DISPLAY = "The character \"%1$s\" in field \"%2$s\" can't be saved. Please remove the problematic character and try again.";

    public static final String NOT_VISIBLE = "You do not have the appropriate permisson to see the folder.";
}
