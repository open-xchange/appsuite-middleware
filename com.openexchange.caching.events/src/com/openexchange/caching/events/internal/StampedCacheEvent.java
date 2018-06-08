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

package com.openexchange.caching.events.internal;

import java.util.List;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import com.openexchange.caching.events.CacheEvent;
import com.openexchange.caching.events.CacheListener;
import com.openexchange.caching.events.Condition;
import com.openexchange.caching.events.ConditionalCacheEvent;

/**
 * {@link StampedCacheEvent}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class StampedCacheEvent implements Delayed {

    private static final long MAX_NANOS = TimeUnit.NANOSECONDS.convert(10, TimeUnit.SECONDS);
    private static final long DELAY_NANOS = TimeUnit.NANOSECONDS.convert(2, TimeUnit.SECONDS);

    /** The special poison element */
    static final StampedCacheEvent POISON = new StampedCacheEvent(null, null, null, false) {

        @Override
        public int compareTo(final Delayed o) {
            return -1;
        }

        @Override
        public long getDelay(final TimeUnit unit) {
            return -1L;
        }
    };

    // ----------------------------------------------------------------------------------------------------------------------------------

    /** The cache event */
    public final CacheEvent event;

    /** The listeners to notify */
    public final List<CacheListener> listeners;

    /** The sender object */
    public final Object sender;

    /** Whether remotely transport or locally */
    public final boolean fromRemote;

    /** The expiration time stamp, after which the cache event becomes available */
    private volatile long stamp;

    /** The max. expiration time stamp, which must not be exceeded when this instance is {@link #reset() reseted} */
    private volatile long maxStamp;

    /** The optional condition */
    private final Condition optCondition;

    /**
     * Initializes a new {@link StampedCacheEvent}.
     */
    public StampedCacheEvent(List<CacheListener> listeners, Object sender, CacheEvent event, boolean fromRemote) {
        super();
        this.listeners = listeners;
        this.sender = sender;
        this.fromRemote = fromRemote;
        this.event = event;
        if (event instanceof ConditionalCacheEvent) {
            this.optCondition = ((ConditionalCacheEvent) event).getCondition();
        } else {
            this.optCondition = null;
        }

        long now = System.nanoTime();
        stamp = now + DELAY_NANOS;
        maxStamp = now + MAX_NANOS;
    }

    /**
     * Gets the optional condition.
     *
     * @return The condition or <code>null</code>
     */
    public Condition optCondition() {
        return optCondition;
    }

    /**
     * Resets this cache event.
     */
    public void reset() {
        long now = System.nanoTime();
        long maxStamp = this.maxStamp;
        long newStamp = now + DELAY_NANOS;
        stamp = now > maxStamp ? maxStamp : newStamp;
    }

    /**
     * Forcedly<b><i>&#42</i></b> resets this cache event.
     * <p>
     * <i>&#42) Means, max. expiration time is adjusted in order to further delay the associated cache event</i>
     */
    public void forceReset() {
        long now = System.nanoTime();
        long maxStamp = this.maxStamp;
        long newStamp = now + DELAY_NANOS;
        stamp = newStamp;
        if (now > maxStamp) {
            this.maxStamp = newStamp;
        }
    }

    @Override
    public int compareTo(final Delayed o) {
        final long thisStamp = stamp;
        final long otherStamp = ((StampedCacheEvent) o).stamp;
        return (thisStamp < otherStamp ? -1 : (thisStamp == otherStamp ? 0 : 1));
    }

    @Override
    public long getDelay(final TimeUnit unit) {
        return unit.convert(stamp - System.nanoTime(), TimeUnit.NANOSECONDS);
    }

    @Override
    public String toString() {
        return event.toString();
    }

}
