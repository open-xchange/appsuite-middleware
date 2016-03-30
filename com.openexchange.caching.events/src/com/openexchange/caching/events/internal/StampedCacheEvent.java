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

/**
 * {@link StampedCacheEvent}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class StampedCacheEvent implements Delayed {

    /** The special poison element */
    static final StampedCacheEvent POISON = new StampedCacheEvent(null, null, null, false, true);

    private static final long MAX_NANOS = TimeUnit.NANOSECONDS.convert(10, TimeUnit.SECONDS);
    private static final long DELAY_NANOS = TimeUnit.NANOSECONDS.convert(2, TimeUnit.SECONDS);

    /** The cache event */
    public final CacheEvent event;

    /** The listeners to notify */
    public final List<CacheListener> listeners;

    /** The sender object */
    public final Object sender;

    /** Whether remotely transport or locally */
    public final boolean fromRemote;

    private volatile long stamp;
    private final long maxStamp;
    private final boolean poison;

    /**
     * Initializes a new {@link StampedCacheEvent}.
     *
     * @param event The event
     */
    public StampedCacheEvent(List<CacheListener> listeners, Object sender, CacheEvent event, boolean fromRemote) {
        this(listeners, sender, event, fromRemote, false);
    }

    /**
     * Initializes a new {@link StampedCacheEvent}.
     */
    private StampedCacheEvent(List<CacheListener> listeners, Object sender, CacheEvent event, boolean fromRemote, boolean poison) {
        super();
        this.listeners = listeners;
        this.sender = sender;
        this.fromRemote = fromRemote;
        this.event = event;
        this.poison = poison;

        long now = System.nanoTime();
        stamp = now;
        maxStamp = now + MAX_NANOS;
    }

    /**
     * Resets this cache event.
     */
    public void reset() {
        long now = System.nanoTime();
        stamp = now > maxStamp ? maxStamp : now;
    }

    @Override
    public int compareTo(final Delayed o) {
        if (poison) {
            return -1;
        }
        final long thisStamp = stamp;
        final long otherStamp = ((StampedCacheEvent) o).stamp;
        return (thisStamp < otherStamp ? -1 : (thisStamp == otherStamp ? 0 : 1));
    }

    @Override
    public long getDelay(final TimeUnit unit) {
        return poison ? -1L : (unit.convert(DELAY_NANOS - (System.nanoTime() - stamp), TimeUnit.NANOSECONDS));
    }

    @Override
    public String toString() {
        return event.toString();
    }

}
