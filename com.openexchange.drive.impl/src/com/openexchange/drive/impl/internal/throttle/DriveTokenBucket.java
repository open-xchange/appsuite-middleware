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

package com.openexchange.drive.impl.internal.throttle;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import com.openexchange.drive.impl.internal.DriveServiceLookup;
import com.openexchange.drive.impl.management.DriveConfig;
import com.openexchange.exception.OXException;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link DriveTokenBucket}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DriveTokenBucket implements TokenBucket {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DriveTokenBucket.class);
    private static final int BUCKET_FILLS_PER_SECOND = 4;

    private final ConcurrentMap<String, Semaphore> bucketsPerSession;
    private final Semaphore overallBucket;
    private final int overallBytesPerSecond;
    private final int clientBytesPerSecond;
    private final ScheduledTimerTask bucketFillerTask;

    /**
     * Initializes a new {@link DriveTokenBucket}.
     *
     * @throws OXException
     */
    public DriveTokenBucket() throws OXException {
        this(DriveConfig.getInstance().getMaxBandwidth(), DriveConfig.getInstance().getMaxBandwidthPerClient());
    }

    /**
     * Initializes a new {@link DriveTokenBucket}.
     *
     * @param overallBytesPerSecond The (overall) allowed bytes per second, or <code>-1</code> if unlimited
     * @param clientBytesPerSecond The allowed bytes per second and client, or <code>-1</code> if unlimited
     * @throws OXException
     */
    public DriveTokenBucket(int overallBytesPerSecond, int clientBytesPerSecond) throws OXException {
        super();
        /*
         * init client / overall bucket semaphores
         */
        this.overallBytesPerSecond = overallBytesPerSecond;
        this.overallBucket = 0 < overallBytesPerSecond ? new Semaphore(overallBytesPerSecond, true) : null;
        this.clientBytesPerSecond = clientBytesPerSecond;
        this.bucketsPerSession = 0 < clientBytesPerSecond ? new ConcurrentHashMap<String, Semaphore>() : null;
        /*
         * init bucket filler thread
         */
        if (isEnabled()) {
            long rate = 1000 / BUCKET_FILLS_PER_SECOND;
            bucketFillerTask = DriveServiceLookup.getService(TimerService.class).scheduleAtFixedRate(new Runnable() {

                @Override
                public void run() {
                    fillBuckets();
                }
            }, rate, rate);
        } else {
            bucketFillerTask = null;
        }
    }

    /**
     * Stops filling up the token bucket by canceling the periodic filler task.
     */
    public void stop() {
        if (null != bucketFillerTask) {
            LOG.debug("Cancelling bucket filler task...");
            if (bucketFillerTask.cancel()) {
                LOG.info("Bucket filler task cancelled.");
            }
        }
    }

    /**
     * Gets a value indicating whether the token bucket is enabled or not, i.e. if there were configured restrictions.
     * @return <code>true</code> if enabled, <code>false</code>, othwerwise
     */
    public boolean isEnabled() {
        return 0 < overallBytesPerSecond || 0 < clientBytesPerSecond;
    }

    @Override
    public void takeBlocking(ServerSession session, int count) throws InterruptedException {
        if (0 < clientBytesPerSecond) {
            acquire(count, getBucket(session), clientBytesPerSecond / BUCKET_FILLS_PER_SECOND);
        }
        if (0 < overallBytesPerSecond) {
            acquire(count, overallBucket, overallBytesPerSecond / BUCKET_FILLS_PER_SECOND);
        }
    }

    private static void acquire(int count, Semaphore semaphore, int maxPermits) throws InterruptedException {
        int acquired = 0;
        do {
            int permits = Math.min(count - acquired, maxPermits);
            System.out.println("acquire: " + permits + "/" + count);
            semaphore.acquire(permits);
            acquired += permits;
            System.out.println("acquired: " + acquired + "/" + count);
        } while (acquired < count);
    }

    @Override
    public boolean tryTake(ServerSession session, int count) {
        if (0 < clientBytesPerSecond) {
            if (false == getBucket(session).tryAcquire(count)) {
                return false;
            }
        }
        if (0 < overallBytesPerSecond) {
            if (false == overallBucket.tryAcquire(count)) {
                return false;
            }
        }
        return true;
    }

    private void fillBuckets() {
        /*
         * release permits for overall bucket
         */
        if (0 < overallBytesPerSecond && null != overallBucket) {
            int maxPermists = overallBytesPerSecond / BUCKET_FILLS_PER_SECOND;
            int permits = Math.min(maxPermists, overallBytesPerSecond - overallBucket.availablePermits());
            if (0 < permits) {
                overallBucket.release(permits);
                LOG.trace("Released {} permits for 'overall' bucket.", permits);
            }
        }
        /*
         * release permits for client buckets
         */
        if (0 < clientBytesPerSecond && null != bucketsPerSession && 0 < bucketsPerSession.size()) {
            int maxPermits = clientBytesPerSecond / BUCKET_FILLS_PER_SECOND;
            Iterator<Entry<String, Semaphore>> iterator = bucketsPerSession.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<String, Semaphore> entry = iterator.next();
                Semaphore bucket = entry.getValue();
                int permits = Math.min(maxPermits, clientBytesPerSecond - bucket.availablePermits());
                if (0 < permits) {
                    bucket.release(permits);
                    LOG.trace("Released {} permits for bucket semaphore of session {}", permits, entry.getKey());
                } else {
                    iterator.remove();
                    LOG.trace("Removed bucket semaphore for session {}", entry.getKey());
                }
            }
        }
    }

    /**
     * Gets or creates the bucket semaphore bound to the supplied session.
     *
     * @param session The session
     * @return The bucket semaphore
     */
    private Semaphore getBucket(ServerSession session) {
        String sessionID = session.getSessionID();
        Semaphore bucket = bucketsPerSession.get(sessionID);
        if (null == bucket) {
            Semaphore newBucket = new Semaphore(0, false);
            bucket = bucketsPerSession.putIfAbsent(sessionID, newBucket);
            if (null == bucket) {
                bucket = newBucket;
                LOG.trace("Created new bucket for {}", sessionID);
            }
        }
        return bucket;
    }

}
