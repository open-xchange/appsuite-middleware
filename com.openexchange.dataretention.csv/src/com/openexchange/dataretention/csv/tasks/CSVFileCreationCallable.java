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

package com.openexchange.dataretention.csv.tasks;

import static com.openexchange.java.Autoboxing.I;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import com.openexchange.dataretention.DataRetentionExceptionCodes;
import com.openexchange.dataretention.csv.CSVDataRetentionConfig;

/**
 * {@link CSVFileCreationCallable} - Task for proper CSV file creation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class CSVFileCreationCallable implements Callable<Boolean> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CSVFileCreationCallable.class);

    /**
     * Atomic counter for file creations.
     */
    private static final AtomicInteger CREATE_COUNTER = new AtomicInteger();

    /**
     * The invoking write task.
     */
    private final AbstractWriteTask writeTask;

    /**
     * Initializes a new {@link CSVFileCreationCallable}.
     *
     * @param writeTask The invoking write task.
     */
    CSVFileCreationCallable(final AbstractWriteTask writeTask) {
        super();
        this.writeTask = writeTask;
    }

    /**
     * Checks for the existence of the CSV file and creates the CSV file within a single, atomic operation and writes the starting header
     * line.
     */
    @Override
    public Boolean call() throws Exception {
        /*
         * From JavaDoc: Atomically creates a new, empty file named by this abstract pathname if and only if a file with this name does not
         * yet exist. The check for the existence of the file and the creation of the file if it does not exist are a single operation that
         * is atomic with respect to all other filesystem activities that might affect the file.
         */
        writeTask.csvFile.reset2Unique();
        final File file = writeTask.csvFile.getFile();
        if (file.createNewFile()) {
            /*
             * As per JavaDoc the CSV file has been checked for existence and created in a single, atomic operation at this location.
             */
            final long lastModified = file.lastModified();
            final CSVDataRetentionConfig config = CSVDataRetentionConfig.getInstance();
            // Create headers
            final StringBuilder sb = new StringBuilder(128);
            sb.append(AbstractWriteTask.RECORD_TYPE_HEADER).append(writeTask.versionNumber).append(';');
            sb.append(writeTask.versionNumber).append(';');
            sb.append(AbstractWriteTask.escape(config.getClientId())).append(';');
            sb.append(AbstractWriteTask.escape(config.getSourceId())).append(';');
            sb.append(AbstractWriteTask.escape(config.getLocation())).append(';');
            sb.append(AbstractWriteTask.msec2sec(lastModified)).append(';');
            sb.append(AbstractWriteTask.escape(config.getTimeZone().getID())).append(';');
            // Time zone offset in minutes
            sb.append(((config.getTimeZone().getRawOffset() / 1000) / 60)).append(';');
            sb.append('\n');
            // Write to file
            writeTask.writeCSVLine(sb.toString());
            boolean success = false;
            int counter = 0;
            // Try 5 times
            while (!success && counter++ < 5) {
                sb.setLength(0);
                // <sourceId>_<localtime>_<nnnnn>.<postfix>
                sb.append(config.getSourceId()).append('_');
                {
                    final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
                    sdf.setTimeZone(config.getTimeZone());
                    sb.append(sdf.format(new Date(lastModified))).append('_');
                }
                appendFileNumber(CREATE_COUNTER.incrementAndGet(), sb);
                sb.append('.').append("mail");
                final File dest = new File(config.getDirectory(), sb.toString());
                success = file.renameTo(dest);
                if (success) {
                    writeTask.csvFile.setFile(dest);
                    LOG.info("Successfully created CSV file \"{}\" and added starting header line", writeTask.csvFile.getFile().getPath());
                } else {
                    LOG.warn("Renaming to CSV file \"{}\" failed. Retry #{}", dest.getPath(), I(counter));
                }
            }
            if (!success) {
                throw DataRetentionExceptionCodes.ERROR.create("CSV file could not be created.");
            }
        }
        // Return dummy object
        return Boolean.TRUE;
    }

    /**
     * Appends formatted file number to specified string builder.
     *
     * @param fileNumber The file number to format
     * @param sb The string builder to append to
     */
    private static void appendFileNumber(final int fileNumber, final StringBuilder sb) {
        for (int i = fileNumber; i < 10000; i *= 10) {
            sb.append('0');
        }
        sb.append(fileNumber);
    }
}
