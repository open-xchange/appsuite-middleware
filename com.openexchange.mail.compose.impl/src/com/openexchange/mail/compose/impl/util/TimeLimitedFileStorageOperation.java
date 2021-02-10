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

package com.openexchange.mail.compose.impl.util;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.mail.compose.CompositionSpaceErrorCode;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;

/**
 * {@link TimeLimitedFileStorageOperation}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class TimeLimitedFileStorageOperation {

    private static final long DEFAULT_TEMPORARY_DOWN_THRESHOLD_MILLIS = 10000L;

    private static final int DEFAULT_WAIT_TIMEOUT_SECONDS = 20;

    /** The default map to remember timed-out file storages */
    static final Map<URI, Long> DEFAULT_TIMED_OUT_FILE_STORAGES = new NonBlockingHashMap<>();

    /**
     * Initializes a new {@link TimeLimitedFileStorageOperation}.
     */
    private TimeLimitedFileStorageOperation() {
        super();
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Creates a new builder for an instance of <code>TimeLimitedOperation</code>
     *
     * @param <V> The builder's type
     * @param task The task
     * @param fileStorage The file storage
     * @return The newly created builder
     */
    public static <V> Builder<V> createBuilder(Task<V> task, FileStorage fileStorage) {
        return new Builder<V>(task, fileStorage);
    }

    /** A builder for an instance of <code>TimeLimitedOperation</code> */
    public static class Builder<V> {

        private final Task<V> task;
        private final FileStorage fileStorage;
        private AtomicBoolean taskFlag;
        private Map<URI, Long> timedOutFileStorages;
        private long temporaryDownThresholdMillis;
        private int waitTimeoutSeconds;
        private Supplier<OXException> onTimeOutHandler;

        Builder(Task<V> task, FileStorage fileStorage) {
            super();
            this.task = task;
            this.fileStorage = fileStorage;
            timedOutFileStorages = DEFAULT_TIMED_OUT_FILE_STORAGES;
            waitTimeoutSeconds = DEFAULT_WAIT_TIMEOUT_SECONDS;
            temporaryDownThresholdMillis = DEFAULT_TEMPORARY_DOWN_THRESHOLD_MILLIS;
        }

        /**
         * Sets the handler to call when timed-out.
         *
         * @param onTimeOutHandler The handler to call when timed-out
         * @return This instance
         */
        public Builder<V> withOnTimeOutHandler(Supplier<OXException> onTimeOutHandler) {
            this.onTimeOutHandler = onTimeOutHandler;
            return this;
        }

        /**
         * Sets the task flag.
         *
         * @param taskFlag The task flag to set
         * @return This instance
         */
        public Builder<V> withTaskFlag(AtomicBoolean taskFlag) {
            this.taskFlag = taskFlag;
            return this;
        }

        /**
         * Sets the timed-out file storages.
         *
         * @param timedOutFileStorages The timed-out file storages to set
         * @return This instance
         */
        public Builder<V> withTimedOutFileStorages(Map<URI, Long> timedOutFileStorages) {
            this.timedOutFileStorages = timedOutFileStorages;
            return this;
        }

        /**
         * Sets the temporary-down threshold milliseconds.
         *
         * @param temporaryDownThresholdMillis The temporary-down threshold milliseconds to set
         * @return This instance
         */
        public Builder<V> withTemporaryDownThresholdMillis(long temporaryDownThresholdMillis) {
            if (temporaryDownThresholdMillis <= 0) {
                throw new IllegalArgumentException("Temporary-down threshold must not be 0 (zero) or negative");
            }
            this.temporaryDownThresholdMillis = temporaryDownThresholdMillis;
            return this;
        }

        /**
         * Sets the wait time-out in seconds.
         *
         * @param waitTimeoutSeconds The wait time-out in seconds to set
         * @return This instance
         */
        public Builder<V> withWaitTimeoutSeconds(int waitTimeoutSeconds) {
            if (waitTimeoutSeconds <= 0) {
                throw new IllegalArgumentException("Wait time must not be 0 (zero) or negative");
            }
            this.waitTimeoutSeconds = waitTimeoutSeconds;
            return this;
        }

        /**
         * Creates the instance of <code>TimeLimitedOperation</code> from this builder's arguments.
         *
         * @return The instance of <code>TimeLimitedOperation</code>
         */
        public TimeLimitedOperation<V> build() {
            return new TimeLimitedOperation<>(task, fileStorage, taskFlag, timedOutFileStorages, waitTimeoutSeconds, temporaryDownThresholdMillis, onTimeOutHandler);
        }

        /**
         * Creates the instance of <code>TimeLimitedOperation</code> from this builder's arguments and submits it for execution.
         * <p>
         * Convenience method for:
         * <pre>
         * TimeLimitedOperation<Value> operation = builder.build();
         * return TimeLimitedFileStorageOperation.timeLimitedOperation(operation);
         * </pre>
         *
         * @return The result
         * @throws OXException If submitting operation for execution fails
         */
        public Result<V> buildAndSubmit() throws OXException {
            return TimeLimitedFileStorageOperation.timeLimitedOperation(build());
        }
    }

    /** A time-limited operation */
    public static class TimeLimitedOperation<V> {

        private final Task<V> task;
        private final FileStorage fileStorage;
        private final AtomicBoolean optionalTaskFlag;
        private final Map<URI, Long> optionalTimedOutFileStorages;
        private final int waitTimeoutSeconds;
        private final long temporaryDownThresholdMillis;
        private final Supplier<OXException> optionalOnTimeOutHandler;

        /**
         * Initializes a new {@link TimeLimitedOperation}.
         *
         * @param task The task
         * @param fileStorage The file storage
         * @param optionalTaskFlag The task flag or <code>null</code>
         * @param optionalTimedOutFileStorages The timed-out file storages or <code>null</code>
         * @param waitTimeoutSeconds The wait time-out in seconds
         * @param temporaryDownThresholdMillis The temporary-down threshold milliseconds
         */
        TimeLimitedOperation(Task<V> task, FileStorage fileStorage, AtomicBoolean optionalTaskFlag, Map<URI, Long> optionalTimedOutFileStorages, int waitTimeoutSeconds, long temporaryDownThresholdMillis, Supplier<OXException> optionalOnTimeOutHandler) {
            super();
            this.task = task;
            this.optionalTaskFlag = optionalTaskFlag;
            this.fileStorage = fileStorage;
            this.optionalTimedOutFileStorages = optionalTimedOutFileStorages;
            this.waitTimeoutSeconds = waitTimeoutSeconds;
            this.temporaryDownThresholdMillis = temporaryDownThresholdMillis;
            this.optionalOnTimeOutHandler = optionalOnTimeOutHandler;
        }

        /**
         * Gets the handler to call when timed-out.
         *
         * @return The handler to call when timed-out or <code>null</code>
         */
        public Supplier<OXException> getOptionalOnTimeOutHandler() {
            return optionalOnTimeOutHandler;
        }

        /**
         * Gets the task
         *
         * @return The task
         */
        public Task<V> getTask() {
            return task;
        }

        /**
         * Gets the file storage
         *
         * @return The file storage
         */
        public FileStorage getFileStorage() {
            return fileStorage;
        }

        /**
         * Gets the optional task flag
         *
         * @return The task flag or <code>null</code>
         */
        public AtomicBoolean getOptionalTaskFlag() {
            return optionalTaskFlag;
        }

        /**
         * Gets the optional timed-out file storages
         *
         * @return The timed-out file storages or <code>null</code>
         */
        public Map<URI, Long> getOptionalTimedOutFileStorages() {
            return optionalTimedOutFileStorages;
        }

        /**
         * Gets the The temporary-down threshold milliseconds
         *
         * @return The The temporary-down threshold milliseconds
         */
        public long getTemporaryDownThresholdMillis() {
            return temporaryDownThresholdMillis;
        }

        /**
         * Gets the wait time-out in seconds
         *
         * @return The wait time-out in seconds
         */
        public int getWaitTimeoutSeconds() {
            return waitTimeoutSeconds;
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Submits given time-limited operation for execution.
     *
     * @param <V> The operation's type
     * @param operation The operation to execute
     * @return The result
     * @throws OXException If operation cannot be submitted
     */
    public static <V> Result<V> timeLimitedOperation(TimeLimitedOperation<V> operation) throws OXException {
        FileStorage fileStorage = operation.getFileStorage();
        Map<URI, Long> optionalTimedOutFileStorages = operation.getOptionalTimedOutFileStorages();
        // Examine file storage
        if (optionalTimedOutFileStorages != null) {
            Long stamp = optionalTimedOutFileStorages.get(fileStorage.getUri());
            if (stamp != null) {
                if (System.currentTimeMillis() - stamp.longValue() <= operation.getTemporaryDownThresholdMillis()) {
                    /*
                     * Still considered as being temporary broken
                     */
                    throw CompositionSpaceErrorCode.ERROR.create("File storage (" + fileStorage.getUri() + ") is still considered as inaccessible.");
                }
                optionalTimedOutFileStorages.remove(fileStorage.getUri());
            }
        }

        // Submit task
        ThreadPoolService threadPool = ThreadPools.getThreadPool();
        Future<V> future = threadPool.submit(operation.getTask());

        // Return result, which awaits completion or time-out
        return new Result<>(future, operation);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /** The result of a time-limited file storage operation */
    public static class Result<V> {

        private final Future<V> future;
        private final TimeLimitedOperation<V> operation;

        Result(Future<V> future, TimeLimitedOperation<V> operation) {
            super();
            this.future = future;
            this.operation = operation;
        }

        /**
         * Gets the result object.
         *
         * @return The result object
         * @throws OXException If execution fails, gets interrupted or wait time is exceeded
         */
        public V getResult() throws OXException {
            // Await completion or time-out
            try {
                return future.get(operation.getWaitTimeoutSeconds(), TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                // Thread interrupted
                AtomicBoolean taskFlag = operation.getOptionalTaskFlag();
                if (taskFlag != null) {
                    taskFlag.set(true);
                }
                Thread.currentThread().interrupt();
                future.cancel(true);
                throw CompositionSpaceErrorCode.ERROR.create(e, "Interrupted while awaiting task completion");
            } catch (ExecutionException e) {
                AtomicBoolean taskFlag = operation.getOptionalTaskFlag();
                if (taskFlag != null) {
                    taskFlag.set(true);
                }
                future.cancel(true);
                Throwable cause = e.getCause();
                if (cause instanceof OXException) {
                    throw (OXException) cause;
                }
                throw CompositionSpaceErrorCode.ERROR.create(cause, "Failed to execute task.");
            } catch (TimeoutException e) {
                AtomicBoolean taskFlag = operation.getOptionalTaskFlag();
                if (taskFlag != null) {
                    taskFlag.set(true);
                }
                future.cancel(true);
                Map<URI, Long> timedOutFileStorages = operation.getOptionalTimedOutFileStorages();
                if (timedOutFileStorages != null) {
                    timedOutFileStorages.put(operation.getFileStorage().getUri(), Long.valueOf(System.currentTimeMillis()));
                }
                Supplier<OXException> onTimeOutHandler = operation.getOptionalOnTimeOutHandler();
                if (onTimeOutHandler != null) {
                    throw onTimeOutHandler.get();
                }
                throw CompositionSpaceErrorCode.ERROR.create("Task could not be completed in time.");
            }
        }
    }

}
