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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.dataretention.csv.tasks;

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
                    LOG.warn("Renaming to CSV file \"{}\" failed. Retry #{}", dest.getPath(), counter);
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
