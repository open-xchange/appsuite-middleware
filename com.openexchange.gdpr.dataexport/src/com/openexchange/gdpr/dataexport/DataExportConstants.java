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


/**
 * {@link DataExportConstants} - Provides constants for data export.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class DataExportConstants {

    /**
     * Initializes a new {@link DataExportConstants}.
     */
    private DataExportConstants() {
        super();
    }

    /**
     * The minimum file size for generated result files (512 MB).
     */
    public static final long MINIMUM_FILE_SIZE = 536870912L;

    /**
     * The default value for the max. file size for generated result files (512 MB).
     */
    public static final long DFAULT_MAX_FILE_SIZE = 1073741824L;

    /**
     * The default value for max. time to live for a completed data export (<code>1209600000</code> two weeks in milliseconds).
     */
    public static final long DEFAULT_MAX_TIME_TO_LIVE = 1209600000L; // Two weeks in milliseconds

    /**
     * The default value for the frequency to check for available data export tasks (<code>300000</code> five minutes in milliseconds).
     */
    public static final long DEFAULT_CHECK_FOR_TASKS_FREQUENCY = 300000L; // 5 minutes in milliseconds

    /**
     * The default value for the frequency to check for aborted data export tasks (<code>120000</code> 2 minutes in milliseconds).
     */
    public static final long DEFAULT_CHECK_FOR_ABORTED_TASKS_FREQUENCY = 120000L; // 2 minutes in milliseconds

    /**
     * The default value for the expiration time for an in-processing data export task (<code>600000</code> 10 minutes in milliseconds).
     */
    public static final long DEFAULT_EXPIRATION_TIME = 600000L; // 10 minutes in milliseconds

    /**
     * The default value for allowed number of concurrent data export tasks (<code>1</code>).
     */
    public static final int DEFAULT_NUMBER_OF_CONCURRENT_TASKS = 1;

    /**
     * The default value for max. allowed fail count for a certain work item associated with a data export task (<code>4</code>).
     */
    public static final int DEFAULT_MAX_FAIL_COUNT_FOR_WORK_ITEM = 4;

}
