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

package com.openexchange.drive.impl.internal.throttle;

import static com.openexchange.java.Autoboxing.I;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import com.openexchange.drive.impl.internal.DriveServiceLookup;
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
     * @param overallBytesPerSecond The (overall) allowed bytes per second, or <code>-1</code> if unlimited
     * @param clientBytesPerSecond The allowed bytes per second and client, or <code>-1</code> if unlimited
     * @throws OXException
     */
    public DriveTokenBucket(int overallBytesPerSecond, int clientBytesPerSecond) {
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
            LOG.debug("acquire: " + permits + "/" + count);
            semaphore.acquire(permits);
            acquired += permits;
            LOG.debug("acquired: " + acquired + "/" + count);
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

    void fillBuckets() {
        /*
         * release permits for overall bucket
         */
        if (0 < overallBytesPerSecond && null != overallBucket) {
            int maxPermists = overallBytesPerSecond / BUCKET_FILLS_PER_SECOND;
            int permits = Math.min(maxPermists, overallBytesPerSecond - overallBucket.availablePermits());
            if (0 < permits) {
                overallBucket.release(permits);
                LOG.trace("Released {} permits for 'overall' bucket.", I(permits));
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
                    LOG.trace("Released {} permits for bucket semaphore of session {}", I(permits), entry.getKey());
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
