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

package com.openexchange.dataretention.csv;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import com.openexchange.dataretention.RetentionData;
import com.openexchange.dataretention.csv.tasks.AbstractWriteTask;
import com.openexchange.dataretention.csv.tasks.MailboxAccessWriteTask;
import com.openexchange.dataretention.csv.tasks.OutboundMailWriteTask;

/**
 * {@link CSVWriter} - The CSV writer creating a write tasks for each call to its <tt>write()</tt> methods, which sequentially processes
 * those tasks in a separate {@link Executor executor}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CSVWriter {

    private static volatile CSVWriter instance;

    /**
     * Gets the singleton instance of {@link CSVWriter}.
     *
     * @return The singleton instance of {@link CSVWriter}.
     */
    public static CSVWriter getInstance() {
        CSVWriter tmp = instance;
        if (null == tmp) {
            synchronized (CSVWriter.class) {
                if (null == (tmp = instance)) {
                    tmp = instance = new CSVWriter(CSVDataRetentionConfig.getInstance().getDirectory());
                }
            }
        }
        return tmp;
    }

    /**
     * Releases the singleton instance of {@link CSVWriter}.
     */
    public static void releaseInstance() {
        if (null != instance) {
            synchronized (CSVWriter.class) {
                if (null != instance) {
                    instance = null;
                }
            }
        }
    }

    /**
     * Helper interface for creating instances of {@link AbstractWriteTask}.
     */
    private static interface InstanceCreator {

        /**
         * Creates a new instance of {@link AbstractWriteTask}.
         *
         * @param retentionData The retention data passed to created instance
         * @param versionNumber The version number; e.g. <code>1</code>
         * @param sequenceNumber The task's unique sequence number
         * @param csvFile The CSV file the created task shall write to
         * @return A new instance of {@link AbstractWriteTask}.
         */
        public AbstractWriteTask newInstance(RetentionData retentionData, int versionNumber, int sequenceNumber, CSVFile csvFile);
    }

    /**
     * The supported transaction types.
     */
    public static enum TransactionType {

        /**
         * Outbound mail
         */
        OUTBOUND('O', new InstanceCreator() {

            @Override
            public AbstractWriteTask newInstance(final RetentionData retentionData, final int versionNumber, final int sequenceNumber, final CSVFile csvFile) {
                return new OutboundMailWriteTask(retentionData, versionNumber, sequenceNumber, csvFile);
            }
        }),
        /**
         * Mailbox access
         */
        ACCESS('A', new InstanceCreator() {

            @Override
            public AbstractWriteTask newInstance(final RetentionData retentionData, final int versionNumber, final int sequenceNumber, final CSVFile csvFile) {
                return new MailboxAccessWriteTask(retentionData, versionNumber, sequenceNumber, csvFile);
            }
        });

        private final char c;

        private final InstanceCreator creator;

        private TransactionType(final char c, final InstanceCreator creator) {
            this.c = c;
            this.creator = creator;
        }

        /**
         * Gets this transaction type's character.
         *
         * @return The character.
         */
        public char getChar() {
            return c;
        }

        /**
         * Creates appropriate write task.
         *
         * @param retentionData The retention data
         * @param versionNumber The version number; e.g. <code>1</code>
         * @param sequenceNumber The unique task's sequence number
         */
        AbstractWriteTask createWriteTask(final RetentionData retentionData, final int versionNumber, final int sequenceNumber, final CSVFile csvFile) {
            return creator.newInstance(retentionData, versionNumber, sequenceNumber, csvFile);
        }
    }

    private final ExecutorService executor;

    private final AtomicInteger sequenceNumber;

    private final CSVFile csvFile;

    /**
     * Initializes a new {@link CSVWriter}.
     *
     * @param directory The parent directory keeping the CSV file
     */
    private CSVWriter(final File directory) {
        super();
        // According to singleThreadExecutor but with priority queue
        executor = new ThreadPoolExecutor(
            1,
            1,
            0L,
            TimeUnit.MILLISECONDS,
            new PriorityBlockingQueue<Runnable>(),
            new CSVWriterThreadFactory("CSVWriter-"));
        sequenceNumber = new AtomicInteger();
        csvFile = new CSVFile(directory);
    }

    /**
     * Writes specified retention data as a CSV line with version number set to configured value.
     *
     * @param retentionData The retention data
     * @param transactionType The transaction type
     */
    public void write(final RetentionData retentionData, final TransactionType transactionType) {
        write(retentionData, CSVDataRetentionConfig.getInstance().getVersionNumber(), transactionType);
    }

    /**
     * Writes specified retention data as a CSV line.
     *
     * @param retentionData The retention data
     * @param versionNumber The version number; e.g. <code>1</code>
     * @param transactionType The transaction type
     */
    public void write(final RetentionData retentionData, final int versionNumber, final TransactionType transactionType) {
        executor.execute(transactionType.createWriteTask(retentionData, versionNumber, sequenceNumber.incrementAndGet(), csvFile));
    }

    /**
     * Stops this CSV writer.
     *
     * @throws InterruptedException If awaiting CSV writer's termination is interrupted
     */
    public void stop() throws InterruptedException {
        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
    }

    /*-
     * #####################################################################
     */

    private static final class CSVWriterThreadFactory implements ThreadFactory {

        private final AtomicInteger threadNumber;

        private final String namePrefix;

        public CSVWriterThreadFactory(final String namePrefix) {
            super();
            threadNumber = new AtomicInteger(1);
            this.namePrefix = namePrefix;
        }

        @Override
        public Thread newThread(final Runnable r) {
            return new Thread(r, getThreadName(
                threadNumber.getAndIncrement(),
                new StringBuilder(namePrefix.length() + 5).append(namePrefix)));
        }

        private static String getThreadName(final int threadNumber, final StringBuilder sb) {
            for (int i = threadNumber; i < 10000; i *= 10) {
                sb.append('0');
            }
            return sb.append(threadNumber).toString();
        }
    }
}
