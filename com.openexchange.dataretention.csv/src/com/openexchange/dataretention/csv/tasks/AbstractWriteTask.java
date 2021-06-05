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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.dataretention.DataRetentionExceptionCodes;
import com.openexchange.dataretention.RetentionData;
import com.openexchange.dataretention.csv.CSVFile;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;

/**
 * {@link AbstractWriteTask} - Abstract write task containing triggered {@link #run()} method, but delegating concrete CSV line creation to
 * its subclasses by {@link #getCSVLine()}.
 * <p>
 * Moreover this class contains the mechanism to ensure that checking for CSV file's existence, creating the CSV file and finally writing
 * starting header line are performed as a single atomic operation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractWriteTask implements Comparable<AbstractWriteTask>, Runnable {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractWriteTask.class);

    /**
     * The character indicating a header record.
     */
    protected static final char RECORD_TYPE_HEADER = 'H';

    /**
     * The retention data.
     */
    protected final RetentionData retentionData;

    /**
     * The record type.
     */
    protected final char recordType;

    /**
     * The version number.
     */
    protected final int versionNumber;

    /**
     * The unique sequence number.
     */
    protected final long sequenceNumber;

    /**
     * The CSV file.
     */
    protected final CSVFile csvFile;

    /**
     * Used to ensure that creating the CSV file and writing starting header line is atomic.
     */
    private static final AtomicReference<Future<Boolean>> REFERENCE = new AtomicReference<Future<Boolean>>();

    /**
     * The last future task kept in atomic reference; used for exclusively setting to <code>null</code>.
     */
    private static volatile Future<Boolean> last;

    /**
     * Initializes a new {@link AbstractWriteTask}.
     *
     * @param retentionData The retention data to write as a CSV line
     * @param recordType The record type; e.g. <code>&quot;M&quot;</code>
     * @param versionNumber The version number
     * @param sequenceNumber The record's sequence number
     * @param csvFile The CSV file to write to
     */
    protected AbstractWriteTask(final RetentionData retentionData, final char recordType, final int versionNumber, final long sequenceNumber, final CSVFile csvFile) {
        super();
        if (null == csvFile) {
            throw new IllegalArgumentException("CSV file is null.");
        }
        this.retentionData = retentionData;
        this.recordType = recordType;
        this.versionNumber = versionNumber;
        this.sequenceNumber = sequenceNumber;
        this.csvFile = csvFile;
    }

    /**
     * Compares this write task with the specified write task by their sequence numbers. Returns a negative integer, zero, or a positive
     * integer as this write task's sequence number is less than, equal to, or greater than the specified write task's sequence number.
     */
    @Override
    public final int compareTo(final AbstractWriteTask o) {
        final long thisVal = sequenceNumber;
        final long anotherVal = o.sequenceNumber;
        return (thisVal < anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
    }

    /**
     * Ensures existence of specified CSV file. As per contract of {@link File#createNewFile()} the check for existence and creation of the
     * file are performed in a single <b>atomic</b> operation. Therefore the file denoted by this writer task's file reference is only
     * created once.
     *
     * @throws OXException If an error occurs
     */
    private void ensureExistence() throws OXException {
        if (csvFile.exists()) {
            return;
        }
        /*
         * Exclusively set reference to null to avoid releasing a previously created future task
         */
        final Future<Boolean> l = last;
        if (l != null && REFERENCE.compareAndSet(l, null)) {
            last = null;
        }
        /*
         * Exclusively create file and write starting header line by a Future
         */
        Future<Boolean> future = REFERENCE.get();
        if (future == null) {
            final FutureTask<Boolean> futureTask = new FutureTask<Boolean>(new CSVFileCreationCallable(this));
            if (REFERENCE.compareAndSet(null, futureTask)) {
                future = futureTask;
                futureTask.run();
                last = future;
            } else {
                future = REFERENCE.get();
            }
        }
        try {
            /*
             * Wait for file being created and its starting header line written
             */
            future.get();
        } catch (InterruptedException e) {
            // Keep interrupted status
            Thread.currentThread().interrupt();
        } catch (CancellationException e) {
            REFERENCE.set(null);
            throw DataRetentionExceptionCodes.ERROR.create(e, e.getMessage());
        } catch (ExecutionException e) {
            REFERENCE.set(null);
            final Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw DataRetentionExceptionCodes.IO.create(cause, cause.getMessage());
            }
            if (cause instanceof RuntimeException) {
                throw DataRetentionExceptionCodes.ERROR.create(cause, cause.getMessage());
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new IllegalStateException("Not unchecked", cause);
        }
    }

    @Override
    public final void run() {
        try {
            ensureExistence();
            // Write CSV line to file
            writeCSVLine(getCSVLine());
        } catch (OXException e) {
            LOG.error("", e);
        } catch (FileNotFoundException e) {
            LOG.error("", e);
        } catch (UnsupportedEncodingException e) {
            LOG.error("", e);
        } catch (IOException e) {
            LOG.error("", e);
        }
    }

    /**
     * Gets the CSV line to write.
     *
     * @return The CSV line to write.
     * @throws OXException If a data retention exception occurs while generating the CSV line
     */
    protected abstract String getCSVLine() throws OXException;

    /**
     * Writes specified CSV line to this task's CSV file.
     * <p>
     * This routine acts a central write method to easily change the way a CSV line is written to the CSV file. The default implementation
     * uses a newly created {@link FileOutputStream file output stream} for each write access. Overwrite it when needed.
     *
     * @param csvLine The CSV line to write
     * @throws IOException If an I/O error occurs
     */
    protected void writeCSVLine(final String csvLine) throws IOException {
        // Write CSV line to file
        final FileOutputStream fos = new FileOutputStream(csvFile.getFile(), true);
        try {
            LOG.debug("Writing CSV line: {}", csvLine);
            fos.write(Charsets.toAsciiBytes(csvLine));
            fos.flush();
        } finally {
            Streams.close(fos);
        }
    }

    /**
     * Escapes specified string. Any control characters (<code>,;"\</code>) are prefixed with <code>'\'</code> and characters &lt; <code>0x20</code> are
     * replaced with <code>'#'</code>.
     *
     * @param string The string to escape
     * @return The escaped string
     */
    protected static final String escape(final String string) {
        final int length = string.length();
        final StringBuilder sb = new StringBuilder(length + 8);
        for (int i = 0; i < length; i++) {
            final char c = string.charAt(i);
            if (',' == c || ';' == c || '"' == c || '\\' == c) {
                sb.append('\\').append(c);
            } else if (c < 0x20) {
                sb.append('#');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Converts specified time millis to seconds.
     *
     * @param millis The time millis; the number of milliseconds since January 1, 1970, 00:00:00 GMT
     * @return The number of seconds since January 1, 1970, 00:00:00 GMT
     */
    protected static final long msec2sec(final long millis) {
        return (millis / 1000);
    }
}
