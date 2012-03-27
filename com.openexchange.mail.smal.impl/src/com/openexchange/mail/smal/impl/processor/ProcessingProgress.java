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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.mail.smal.impl.processor;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailFields;
import com.openexchange.threadpool.ThreadPools;

/**
 * {@link ProcessingProgress} - The status of an in-progress folder.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ProcessingProgress {

    /**
     * The empty processing result.
     */
    public static final ProcessingProgress EMPTY_RESULT = new ProcessingProgress();

    private static final MailFields FIELDS_BODY_OR_FULL = new MailFields(MailField.FULL, MailField.BODY);

    private volatile boolean firstTime;

    private volatile boolean hasHighAttention;

    private volatile ProcessType processType;

    private volatile Future<Object> future;

    /**
     * The count-down latch indicating proper initialization of this process.
     */
    final CountDownLatch latch;

    /**
     * Whether associated latch has already been counted-down.
     */
    volatile boolean countDown;

    /**
     * Initializes a new {@link ProcessingProgress}.
     */
    public ProcessingProgress() {
        super();
        processType = ProcessType.NONE;
        latch = new CountDownLatch(1);
        countDown = true;
    }

    /**
     * Sets the future
     * 
     * @param future The future to set
     * @return This progress with argument applied
     */
    ProcessingProgress setFuture(final Future<Object> future) {
        this.future = future;
        return this;
    }

    /**
     * Sets the first-time flag
     * 
     * @param firstTime The first-time flag to set
     * @return This progress with argument applied
     */
    public ProcessingProgress setFirstTime(final boolean firstTime) {
        this.firstTime = firstTime;
        return this;
    }

    /**
     * Sets the high-attention flag
     * 
     * @param hasHighAttention The high-attention flag to set
     * @return This progress with argument applied
     */
    public ProcessingProgress setHasHighAttention(final boolean hasHighAttention) {
        this.hasHighAttention = hasHighAttention;
        return this;
    }

    /**
     * Sets the process type
     * 
     * @param processType The process type to set
     * @return This progress with argument applied
     */
    public ProcessingProgress setProcessType(final ProcessType processType) {
        this.processType = processType;
        latch.countDown();
        countDown = false;
        return this;
    }

    /**
     * Awaits completion of associated processor's run.
     * 
     * @throws OXException If processor run failed
     * @throws InterruptedException If waiting is aborted
     */
    public void awaitCompletion() throws OXException, InterruptedException {
        final Future<Object> future = this.future;
        if (null == future) {
            return;
        }
        try {
            future.get();
        } catch (final ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof InterruptedException) {
                throw (InterruptedException) cause;
            }
            try {
                throw ThreadPools.launderThrowable(e, OXException.class);
            } catch (final RuntimeException rte) {
                throw MailExceptionCode.UNEXPECTED_ERROR.create(rte, rte.getMessage());
            }
        }
    }

    /**
     * Cancels further processing.
     */
    public void cancel() {
        final Future<Object> future = this.future;
        if (null == future) {
            return;
        }
        future.cancel(true);
    }

    /**
     * Checks if associated processing task is already completed.
     * 
     * @return <code>true</code> if done; otherwise <code>false</code>
     */
    public boolean isDone() throws OXException, InterruptedException {
        final Future<Object> future = this.future;
        if (null == future || !future.isDone()) {
            return false;
        }
        try {
            future.get();
            return true;
        } catch (final ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof InterruptedException) {
                throw (InterruptedException) cause;
            }
            try {
                throw ThreadPools.launderThrowable(e, OXException.class);
            } catch (final RuntimeException rte) {
                throw MailExceptionCode.UNEXPECTED_ERROR.create(rte, rte.getMessage());
            }
        }
    }

    /**
     * Checks if specified fields are covered by this processing result.
     * 
     * @param fields The fields to check against
     * @return <code>true</code> if specified fields are covered by this processing result; otherwise <code>false</code>
     */
    public boolean covers(final MailField[] fields) {
        if (ProcessType.FULL.equals(processType)) {
            return true;
        }
        if (ProcessType.HEADERS_AND_CONTENT.equals(processType)) {
            return true;
        }
        // If requested fields contains one of FULL or BODY, request cannot be served by index.
        return !new MailFields(fields).containsAny(FIELDS_BODY_OR_FULL);
    }

    /**
     * Checks if this processing result was yielded from submitting a job.
     * 
     * @return <code>true</code> if associated with a job; otherwise <code>false</code> if immediate processing was performed
     */
    public boolean asJob() {
        return ProcessType.JOB.equals(processType);
    }

    /**
     * Checks whether processing took place the first time for associated folder.
     * 
     * @return <code>true</code> for first time processing; otherwise <code>false</code>
     */
    public boolean isFirstTime() {
        return firstTime;
    }

    /**
     * Checks whether processed folder has high attention.
     * 
     * @return <code>true</code> if processed folder has high attention; otherwise <code>false</code>
     */
    public boolean isHasHighAttention() {
        return hasHighAttention;
    }

    /**
     * Gets the process type
     * 
     * @return The process type
     */
    public ProcessType getProcessType() {
        return processType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (hasHighAttention ? 1231 : 1237);
        result = prime * result + ((processType == null) ? 0 : processType.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ProcessingProgress)) {
            return false;
        }
        final ProcessingProgress other = (ProcessingProgress) obj;
        if (hasHighAttention != other.hasHighAttention) {
            return false;
        }
        if (processType != other.processType) {
            return false;
        }
        return true;
    }

}
