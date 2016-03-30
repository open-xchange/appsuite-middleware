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

package com.openexchange.realtime.json.protocol;

import java.util.List;
import java.util.concurrent.TimeUnit;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.util.Duration;

/**
 * {@link RTClientState} - The {@link RTClientState} encapsulates the state of a connected client by keeping track of the sequenced and
 * unsequenced Stanzas that still have to be delivered to the client.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public interface RTClientState {

    /**
     * Called when the client sent an acknowledgement for a stanza
     * @param sequenceNumber
     */
    public abstract void acknowledgementReceived(long sequenceNumber);

    /**
     * Enqueues a stanza. If it contains a sequence number, it is enqueued in the resendBuffer, otherwise in the nonsequenceStanzas
     * @param stanza
     */
    public abstract void enqueue(Stanza stanza);

    /**
     * Retrieves a list of stanzas that are still to be transmitted to the client
     * @return
     */
    public abstract List<Stanza> getStanzasToSend();

    /**
     * A purge run removes all unsequenced stanzas from the state and increases the TTL counter of sequenced stanzas.
     */
    public abstract void purge();

    public abstract ID getId();

    /**
     * Gets the thread that currently holds this client state's lock (if any).
     *
     * @return The thread or <code>null</code>
     */
    public Thread getOwner();

    /**
     * Acquires the lock if it is not held by another thread within the given
     * waiting time and the current thread has not been
     * {@linkplain Thread#interrupt interrupted}.
     *
     * <p>Acquires the lock if it is not held by another thread and returns
     * immediately with the value {@code true}, setting the lock hold count
     * to one. If this lock has been set to use a fair ordering policy then
     * an available lock <em>will not</em> be acquired if any other threads
     * are waiting for the lock. This is in contrast to the {@link #tryLock()}
     * method. If you want a timed {@code tryLock} that does permit barging on
     * a fair lock then combine the timed and un-timed forms together:
     *
     * <pre>if (lock.tryLock() || lock.tryLock(timeout, unit) ) { ... }
     * </pre>
     *
     * <p>If the current thread
     * already holds this lock then the hold count is incremented by one and
     * the method returns {@code true}.
     *
     * <p>If the lock is held by another thread then the
     * current thread becomes disabled for thread scheduling
     * purposes and lies dormant until one of three things happens:
     *
     * <ul>
     *
     * <li>The lock is acquired by the current thread; or
     *
     * <li>Some other thread {@linkplain Thread#interrupt interrupts}
     * the current thread; or
     *
     * <li>The specified waiting time elapses
     *
     * </ul>
     *
     * <p>If the lock is acquired then the value {@code true} is returned and
     * the lock hold count is set to one.
     *
     * <p>If the current thread:
     *
     * <ul>
     *
     * <li>has its interrupted status set on entry to this method; or
     *
     * <li>is {@linkplain Thread#interrupt interrupted} while
     * acquiring the lock,
     *
     * </ul>
     * then {@link InterruptedException} is thrown and the current thread's
     * interrupted status is cleared.
     *
     * <p>If the specified waiting time elapses then the value {@code false}
     * is returned.  If the time is less than or equal to zero, the method
     * will not wait at all.
     *
     * <p>In this implementation, as this method is an explicit
     * interruption point, preference is given to responding to the
     * interrupt over normal or reentrant acquisition of the lock, and
     * over reporting the elapse of the waiting time.
     *
     * @param timeout the time to wait for the lock
     * @param unit the time unit of the timeout argument
     * @return {@code true} if the lock was free and was acquired by the
     *         current thread, or the lock was already held by the current
     *         thread; and {@code false} if the waiting time elapsed before
     *         the lock could be acquired
     * @throws InterruptedException if the current thread is interrupted
     * @throws NullPointerException if the time unit is null
     *
     */
    public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException;

    /**
     * Acquires the lock for this client state.
     *
     * <p>Acquires the lock if it is not held by another thread and returns
     * immediately, setting the lock hold count to one.
     *
     * <p>If the current thread already holds the lock then the hold
     * count is incremented by one and the method returns immediately.
     *
     * <p>If the lock is held by another thread then the
     * current thread becomes disabled for thread scheduling
     * purposes and lies dormant until the lock has been acquired,
     * at which time the lock hold count is set to one.
     */
    public abstract void lock();

    /**
     * Attempts to release this lock.
     *
     * <p>If the current thread is the holder of this lock then the hold
     * count is decremented.  If the hold count is now zero then the lock
     * is released.  If the current thread is not the holder of this
     * lock then {@link IllegalMonitorStateException} is thrown.
     *
     * @throws IllegalMonitorStateException if the current thread does not
     *         hold this lock
     */
    public abstract void unlock();

    /**
     * Touch sets the last-seen timestamp for this state entry
     */
    public abstract void touch();

    /**
     * Retrieves the timestamp for when this user was last seen in milliseconds
     */
    public abstract long getLastSeen();

    /**
     * Checks whether this state should be considered timed out relative to the given timestamp
     * @param timestamp - The timestamp to check the timeout status for
     * @return true if the timestamp is more than thirty minutes ahead of the lastSeen timestamp, false otherwise
     */
    public abstract boolean isTimedOut(long timestamp);

    /**
     * Retrieve the duration of inactivity for this RTCLientState. Inactivity is defined as time the state wasn't actively touched by the
     * associated client. This combines calls to {@link RTClientState#getLastSeen()} and creating the nearest Duration via
     * {@link Duration#roundDownTo(long, java.util.concurrent.TimeUnit)}
     *
     * @return the duration of inactivity for this RTCLientState
     */
    public abstract Duration getInactivityDuration();

    /**
     * Resets the state by clearing out sequenced and unsequenced stanzas. This is needed when a client wants to trigger a reset e.g. via
     * resetting the sequence number in use.
     */
    public abstract void reset();

}
