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

package com.openexchange.find.tasks;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link TasksStrings}
 *
 * @author <a href="mailto:felix.marx@open-xchange.com">Felix Marx</a>
 */
public class TasksStrings implements LocalizableStrings {

    // ------------------------- i18n strings for facet types -------------------------------------- //

    // Context: Searching in tasks.
    // Displayed as: [Search for] 'user input' in title.
    // The 'user input' part is always prepended, please heed this in translations.
    public static final String FACET_TASK_TITLE = "in title";

    // Search in folders.
    public static final String FACET_TASK_FOLDERS = "Task folders";

    // Search in persons.
    public static final String FACET_TASK_PARTICIPANTS = "Task participants";

    // Search in task type.
    public static final String FACET_TASK_TYPE = "Task type";

    // Search in task status
    public static final String FACET_TASK_STATUS = "Task status";

    // Context: Searching in tasks.
    // Displayed as: [Search for] 'user input' in description.
    // The 'user input' part is always prepended, please heed this in translations.
    public static final String FACET_TASK_DESCRIPTION = "in description";

    // Context: Searching in tasks.
    // Displayed as: [Search for] 'user input' in attachment name.
    // The 'user input' part is always prepended, please heed this in translations.
    public static final String FACET_TASK_ATTACHMENT_NAME = "in attachment name";

    // ------------------------- i18n strings for folder types ------------------------------------- //

    public static final String TASK_STATUS_NOT_STARTED = "Not Started";

    public static final String TASK_STATUS_IN_PROGRESS = "In Progress";

    public static final String TASK_STATUS_DONE = "Done";

    public static final String TASK_STATUS_WAITING = "Waiting";

    public static final String TASK_STATUS_DEFERRED = "Deferred";

    public static final String TASK_TYPE_SINGLE_TASK = "Single Task";

    public static final String TASK_TYPE_SERIES = "Series";

}
