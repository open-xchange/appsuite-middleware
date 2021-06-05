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

package com.openexchange.ajax.requesthandler.jobqueue;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link JobQueueMessages} - Translatable messages for {@link JobQueueExceptionCodes}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public final class JobQueueMessages implements LocalizableStrings {

    // No user-associated job found
    public static final String NO_SUCH_JOB_MSG = "No such job %1$s";

    // A long-running operation is being executed
    public static final String LONG_RUNNING_OPERATION_MSG = "A long-running operation is being executed";

    // Such a long-running operation is already in progress
    public static final String ALREADY_RUNNING_MSG = "Such a long-running operation is already in progress";

    /**
     * Prevent instantiation.
     */
    private JobQueueMessages() {
        super();
    }

}
