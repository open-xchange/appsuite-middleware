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

package com.openexchange.pooling;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class stores data about a pooled object.
 * @param <T> type of object.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class PooledData<T> {

    /**
     * Counter for referencing uniquely all pooled objects.
     */
    private static final AtomicInteger counter = new AtomicInteger();

    /**
     * Unique identifier of the pooled object.
     */
    private final int identifier;

    /**
     * Time when this pooled object was created.
     */
    private final long createTime;

    private long timestamp;

    private final T pooled;

    private Thread thread;

    private StackTraceElement[] trace;

    /**
     * Default constructor.
     * @param pooled Pooled object.
     */
    PooledData(final T pooled) {
        super();
        this.createTime = System.currentTimeMillis();
        touch();
        this.identifier = counter.incrementAndGet();
        this.pooled = pooled;
    }

    void setThread(final Thread user) {
        this.thread = user;
    }

    final void touch() {
        timestamp = System.currentTimeMillis();
    }

    public T getPooled() {
        return pooled;
    }

    void setTrace(final StackTraceElement[] trace) {
        this.trace = trace;
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof PooledData<?>)) {
            return false;
        }
        return pooled.equals(((PooledData<?>) obj).pooled);
    }

    @Override
    public int hashCode() {
        return pooled.hashCode();
    }

    void resetTrace() {
        thread = null;
        trace = null;
    }

    /**
     * @return the timestamp when this pooled object is last used.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @return the number of milli seconds since this pooled object was last
     * touched by the pool.
     */
    public long getTimeDiff() {
        return System.currentTimeMillis() - timestamp;
    }

    /**
     * @return the time this pooled object is living.
     */
    public long getLiveTime() {
        return System.currentTimeMillis() - createTime;
    }

    Thread getThread() {
        return thread;
    }

    public StackTraceElement[] getTrace() {
        return trace;
    }

    /**
     * @return the identifier
     */
    public int getIdentifier() {
        return identifier;
    }
}
