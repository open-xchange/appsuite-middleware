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

package com.openexchange.gdpr.dataexport;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link DataExportExceptionMessage} - The The GDPR data export error messages.
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class DataExportExceptionMessage implements LocalizableStrings {

    /**
     * Initializes a new {@link DataExportExceptionMessage}.
     */
    private DataExportExceptionMessage() {
        super();
    }

    // There is already a running data export.
    public final static String TASK_ALREADY_RUNNING_MSG = "There is already a running data export";

    // There is no data export.
    public static final String NO_SUCH_TASK_MSG = "There is no data export";

    // The data export is not yet finished. Please try again later.
    public static final String TASK_NOT_COMPLETED_MSG = "The data export is not yet finished. Please try again later.";

    // The data export failed.
    public static final String TASK_FAILED_MSG = "The data export failed";

    // A data export has already been requested
    public static final String DUPLICATE_TASK_MSG = "A data export has already been requested";

    // There is no data export or it has already been completed
    public static final String CANCEL_TASK_FAILED_MSG = "There is no data export or it has already been completed";

    // The data export has been aborted, but he/she tries to retrieve its results
    public static final String TASK_ABORTED_MSG = "The data export has been aborted";

    // User requested a package number that does not exist
    public static final String NO_SUCH_RESULT_FILE_MSG = "No such package";

    // User requested to delete a completed data export task, but task is not yet completed
    public static final String DELETE_TASK_FAILED_MSG = "There is no data export or it is not yet completed";;

}
