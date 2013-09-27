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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

package com.openexchange.drive.internal.throttle;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import org.apache.commons.logging.Log;
import com.openexchange.config.ConfigurationService;
import com.openexchange.drive.internal.DriveServiceLookup;
import com.openexchange.exception.OXException;
import com.openexchange.java.StringAllocator;
import com.openexchange.java.Strings;
import com.openexchange.timer.TimerService;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link DriveTokenBucket}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DriveTokenBucket implements TokenBucket {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(DriveTokenBucket.class);
    private static final int BUCKET_FILLS_PER_SECOND = 4;

    private final ConcurrentMap<String, Semaphore> bucketsPerSession;
    private final Semaphore overallBucket;
    private final int overallBytesPerSecond;
    private final int clientBytesPerSecond;


    /**
     * Initializes a new {@link DriveTokenBucket}.
     *
     * @throws OXException
     */
    public DriveTokenBucket() throws OXException {
        super();
        /*
         * init client / overall bucket semaphores
         */
        ConfigurationService configService = DriveServiceLookup.getService(ConfigurationService.class, true);
        String maxBandwidth = configService.getProperty("com.openexchange.drive.maxBandwidth");
        overallBytesPerSecond = Strings.isEmpty(maxBandwidth) ? -1 : parseBytes(maxBandwidth);
        overallBucket = 0 < overallBytesPerSecond ? new Semaphore(overallBytesPerSecond, true) : null;
        String maxBandwidthPerClient = configService.getProperty("com.openexchange.drive.maxBandwidthPerClient");
        clientBytesPerSecond = Strings.isEmpty(maxBandwidthPerClient) ? -1 : parseBytes(maxBandwidthPerClient);
        bucketsPerSession = 0 < clientBytesPerSecond ? new ConcurrentHashMap<String, Semaphore>() : null;
        /*
         * init bucket filler thread
         */
        if (isEnabled()) {
            long rate = 1000 / BUCKET_FILLS_PER_SECOND;
            DriveServiceLookup.getService(TimerService.class).scheduleAtFixedRate(new Runnable() {

                @Override
                public void run() {
                    fillBuckets();
                }
            }, rate, rate);
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
            getBucket(session).acquire(count);
        }
        if (0 < overallBytesPerSecond) {
            overallBucket.acquire(count);
        }
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
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Released " + permits + " permits for 'overall' bucket.");
                }
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
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Released " + permits + " permits for bucket semaphore of session " + entry.getKey());
                    }
                } else {
                    iterator.remove();
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Removed bucket semaphore for session " + entry.getKey());
                    }
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
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Created new bucket for " + sessionID);
                }
            }
        }
        return bucket;
    }

    /**
     * Parses a byte value including an optional unit.
     *
     * @param value the value to parse
     * @return The parsed number of bytes
     * @throws NumberFormatException If the supplied string is not parsable or greater then <code>Integer.MAX_VALUE</code>
     */
    private static int parseBytes(String value) throws NumberFormatException {
        StringAllocator numberAllocator = new StringAllocator(8);
        StringAllocator unitAllocator = new StringAllocator(4);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isDigit(c) || '.' == c || '-' == c) {
                numberAllocator.append(c);
            } else if (false == Character.isWhitespace(c)) {
                unitAllocator.append(c);
            }
        }
        double number = Double.parseDouble(numberAllocator.toString());
        if (0 < unitAllocator.length()) {
            String unit = unitAllocator.toString().toUpperCase();
            int exp = Arrays.asList("B", "KB", "MB", "GB").indexOf(unit);
            if (0 <= exp) {
                number *= Math.pow(1024, exp);
            } else {
                throw new NumberFormatException(value);
            }
        }
        if (Integer.MAX_VALUE >= number) {
            return (int)number;
        }
        throw new NumberFormatException(value);
    }

}
