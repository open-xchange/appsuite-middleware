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

import com.openexchange.exception.OXException;
import com.openexchange.i18n.LocalizableStrings;

/**
 * Exception message texts for the {@link OXException}.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class UpdateExceptionMessages implements LocalizableStrings {

    public static final String ONLY_REDUCE_DISPLAY = "The current version number %1$s is already lower than or equal to the desired version number %2$s.";

    public static final String LOADING_TASK_FAILED_DISPLAY = "Error loading update task \"%1$s\".";

    public static final String UNKNOWN_SCHEMA_DISPLAY = "Unknown schema name: \"%1$s\".";

    public static final String UNKNOWN_CONCURRENCY_DISPLAY = "Update task \"%1$s\" returned an unknown concurrency level. Running as blocking task.";

    public static final String RESET_FORBIDDEN_DISPLAY = "The version can not be set back if the update tasks handling has been migrated to the Remember Executed Update Tasks concept on schema \"%1$s\".";

    public static final String UNRESOLVABLE_DEPENDENCIES_DISPLAY = "Unable to determine next update task to execute. Executed: \"%1$s\". Enqueued: \"%2$s\". Scheduled: \"%3$s\".";

    public static final String WRONG_ROW_COUNT_DISPLAY = "Processed a wrong number of rows in database. Expected %1$d rows but worked on %2$d rows.";

    public static final String UPDATE_FAILED_DISPLAY = "Updating schema \"%1$s\" failed. Cause: \"%2$s\".";

    public static final String BLOCKING_FIRST_DISPLAY = "Blocking tasks (\"%1$s\") must be executed before background tasks can be executed (\"%2$s\").";

    public static final String UNKNOWN_TASK_DISPLAY = "Unknown task: \"%1$s\".";

    public static final String COLUMN_NOT_FOUND_DISPLAY = "Column \"%1$s\" not found in table \"%2$s\".";

    public static final String FOUND_MULTIPLE_SCHEMAS_DISPLAY = "Found multiple schemas for name: \"%1$s\".";

    private UpdateExceptionMessages() {
        super();
    }
}
