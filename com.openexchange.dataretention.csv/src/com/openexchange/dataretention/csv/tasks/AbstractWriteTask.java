
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
import com.openexchange.dataretention.DataRetentionException;
import com.openexchange.dataretention.DataRetentionExceptionMessages;
import com.openexchange.dataretention.RetentionData;
import com.openexchange.dataretention.csv.CSVFile;

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

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(AbstractWriteTask.class);

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
    public final int compareTo(final AbstractWriteTask o) {
        final long thisVal = this.sequenceNumber;
        final long anotherVal = o.sequenceNumber;
        return (thisVal < anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
    }

    /**
     * Ensures existence of specified CSV file. As per contract of {@link File#createNewFile()} the check for existence and creation of the
     * file are performed in a single <b>atomic</b> operation. Therefore the file denoted by this writer task's file reference is only
     * created once.
     * 
     * @throws DataRetentionException If an I/O error occurs
     */
    private void ensureExistence() throws DataRetentionException {
        if (csvFile.getFile().exists()) {
            return;
        }
        /*
         * Exclusively set reference to null
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
        } catch (final InterruptedException e) {
            // Keep interrupted status
            Thread.currentThread().interrupt();
        } catch (final CancellationException e) {
            REFERENCE.set(null);
            throw DataRetentionExceptionMessages.ERROR.create(e, e.getMessage());
        } catch (final ExecutionException e) {
            REFERENCE.set(null);
            final Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw DataRetentionExceptionMessages.IO.create(cause, cause.getMessage());
            }
            if (cause instanceof RuntimeException) {
                throw DataRetentionExceptionMessages.ERROR.create(cause, cause.getMessage());
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new IllegalStateException("Not unchecked", cause);
        }
    }

    public final void run() {
        try {
            ensureExistence();
            // Write CSV line to file
            writeCSVLine(getCSVLine());
        } catch (final DataRetentionException e) {
            LOG.error(e.getMessage(), e);
        } catch (final FileNotFoundException e) {
            LOG.error(e.getMessage(), e);
        } catch (final UnsupportedEncodingException e) {
            LOG.error(e.getMessage(), e);
        } catch (final IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * Gets the CSV line to write.
     * 
     * @return The CSV line to write.
     * @throws DataRetentionException If a data retention exception occurs while generating the CSV line
     */
    protected abstract String getCSVLine() throws DataRetentionException;

    /**
     * Writes specified CSV line to this task's CSV file.
     * <p>
     * This routine acts a central write method to easily change in which way a CSV line is written to the CSV file. The default
     * implementation uses a newly created {@link FileOutputStream file output stream} for each write access. Overwrite it when needed.
     * 
     * @param csvLine The CSV line to write
     * @throws IOException If an I/O error occurs
     */
    protected void writeCSVLine(final String csvLine) throws IOException {
        // Write CSV line to file
        final FileOutputStream fos = new FileOutputStream(csvFile.getFile(), true);
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug(new StringBuilder("Composed CSV line: ").append(csvLine).toString());
            }
            fos.write(csvLine.getBytes("US-ASCII"));
            fos.flush();
        } finally {
            try {
                fos.close();
            } catch (final IOException e) {
                LOG.error(e.getMessage(), e);
            }
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
        final StringBuilder sb = new StringBuilder(string.length() + 8);
        for (final char c : string.toCharArray()) {
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
