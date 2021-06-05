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

package com.openexchange.groupware.update;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link UpdateExceptionCodes}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public enum UpdateExceptionCodes implements DisplayableOXExceptionCode {

    /**
     * Current version number %1$s is already lower than or equal to desired version number %2$s.
     */
    ONLY_REDUCE("The current version number %1$s is already lower than or equal to the desired version number %2$s.",
        UpdateExceptionMessages.ONLY_REDUCE_DISPLAY, Category.CATEGORY_USER_INPUT, 13),

    /**
     * An SQL problem occurred: %1$s.
     */
    SQL_PROBLEM("An SQL problem occurred: %1$s.", OXExceptionStrings.SQL_ERROR_MSG, Category.CATEGORY_ERROR, 14),

    /**
     * Error loading update task "%1$s".
     */
    LOADING_TASK_FAILED("Error loading update task \"%1$s\".", UpdateExceptionMessages.LOADING_TASK_FAILED_DISPLAY,
        Category.CATEGORY_USER_INPUT, 15),

    /**
     * Unknown schema name: %1$s.
     */
    UNKNOWN_SCHEMA("Unknown schema name: %1$s.", UpdateExceptionMessages.UNKNOWN_SCHEMA_DISPLAY, Category.CATEGORY_USER_INPUT, 16),

    /**
     * Update task %1$s returned an unknown concurrency level. Running as blocking task.
     */
    UNKNOWN_CONCURRENCY("Update task %1$s returned an unknown concurrency level. Running as blocking task.",
        UpdateExceptionMessages.UNKNOWN_CONCURRENCY_DISPLAY, Category.CATEGORY_ERROR, 17),

    /**
     * Version can not be set back if update task handling has been migrated to remembered update tasks concept on schema %1$s.
     */
    RESET_FORBIDDEN("The version can not be set back if the update tasks handling has been migrated to the Remember Executed Update"
        + " Tasks concept on schema %1$s.", UpdateExceptionMessages.RESET_FORBIDDEN_DISPLAY, Category.CATEGORY_USER_INPUT, 18),

    /**
     * Unable to determine next update task to execute. Executed: %1$s. Enqueued: %2$s. Scheduled: %3$s.
     */
    UNRESOLVABLE_DEPENDENCIES("Unable to determine next update task to execute. Executed: %1$s. Enqueued: %2$s. Scheduled: %3$s.",
        UpdateExceptionMessages.UNRESOLVABLE_DEPENDENCIES_DISPLAY, Category.CATEGORY_ERROR, 19),

    /**
     * %1$s.
     */
    OTHER_PROBLEM("%1$s", OXExceptionStrings.MESSAGE, Category.CATEGORY_ERROR, 20),

    /**
     * Processed a wrong number of rows in database. Expected %1$d rows but worked on %2$d rows.
     */
    WRONG_ROW_COUNT("Processed a wrong number of rows in database. Expected %1$d rows but worked on %2$d rows.",
        UpdateExceptionMessages.WRONG_ROW_COUNT_DISPLAY, Category.CATEGORY_ERROR, 21),

    /**
     * Updating schema %1$s failed. Cause: %2$s.
     */
    UPDATE_FAILED("Updating schema %1$s failed. Cause: %2$s.", UpdateExceptionMessages.UPDATE_FAILED_DISPLAY, Category.CATEGORY_ERROR, 22),

    /**
     * Blocking tasks (%1$s) must be executed before background tasks can be executed (%2$s).
     */
    BLOCKING_FIRST("Blocking tasks (%1$s) must be executed before background tasks can be executed (%2$s).",
        UpdateExceptionMessages.BLOCKING_FIRST_DISPLAY, Category.CATEGORY_ERROR, 23),

    /** Unknown task: %1$s */
    UNKNOWN_TASK("Unknown task: %1$s.", UpdateExceptionMessages.UNKNOWN_TASK_DISPLAY, Category.CATEGORY_CONFIGURATION, 24),

    /** Column "%1$s" not found in table %2$s. */
    COLUMN_NOT_FOUND("Column \"%1$s\" not found in table %2$s.", UpdateExceptionMessages.COLUMN_NOT_FOUND_DISPLAY,
        Category.CATEGORY_ERROR, 25),

    /**
     * An error occurred: %1$s.
     */
    UNEXPECTED_ERROR("An error occurred: %1$s", OXExceptionStrings.MESSAGE, Category.CATEGORY_ERROR, 26),

    /**
     * Found multiple schemas for name "%1$s": %2$s
     */
    FOUND_MULTIPLE_SCHEMAS("Found multiple schemas for name \"%1$s\": %2$s", UpdateExceptionMessages.FOUND_MULTIPLE_SCHEMAS_DISPLAY, Category.CATEGORY_USER_INPUT, 16),

    /**
     * Unmet dependencies for update task \"%1$s\" (requires \"%2$s\").
     */
    UNMET_DEPENDENCY("Unmet dependencies for update task \"%1$s\" (requires \"%2$s\").", OXExceptionStrings.MESSAGE, Category.CATEGORY_ERROR, 27),

    ;

    final String message;

    final String displayMessage;

    final Category category;

    final int number;

    private UpdateExceptionCodes(final String message, final String displayMessage, final Category category, final int number) {
        this.message = message;
        this.displayMessage = displayMessage != null ? displayMessage : OXExceptionStrings.MESSAGE;
        this.category = category;
        this.number = number;
    }

    @Override
    public String getPrefix() {
        return "UPD";
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
