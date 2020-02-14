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

package com.openexchange.antivirus.impl;

import java.util.concurrent.TimeUnit;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;

/**
 * {@link MetricHandler} - Simple utility class to handle metric updates
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.2
 */
final class MetricHandler {

    private static final String RESULTS = "results";
    
    private static final String GROUP = "appsuite.antivirus.";

    /**
     * Prevents initialization
     */
    private MetricHandler() {
        super();
    }

    private final static Counter CACHE_HIT = Counter.builder(GROUP + "cache.hit").description("Cached Anti-Virus results hits").baseUnit(RESULTS).register(Metrics.globalRegistry);
    private final static Counter CACHE_MISS = Counter.builder(GROUP + "cache.miss").description("Cached Anti-Virus results misses").baseUnit(RESULTS).register(Metrics.globalRegistry);
    private final static Counter CACHE_INVALIDATIONS = Counter.builder(GROUP + "cache.invalidations").description("Cached Anti-Virus results invalidations").baseUnit(RESULTS).register(Metrics.globalRegistry);
    private final static Counter TRANSFER_SIZE = Counter.builder(GROUP + "transfer.size").description("Measures the amount of bytes transfered to the anti-virus server").baseUnit("bytes").register(Metrics.globalRegistry);

    private final static Timer SCANNING_RATE = Timer.builder(GROUP + "scanning.rate").description("Measures the number of files scanned per second").register(Metrics.globalRegistry);
    private final static Timer SCANNING_TIME = Timer.builder(GROUP + "scanning.time").description("Measures the time elapsed during scanning a file").publishPercentileHistogram().register(Metrics.globalRegistry);

    /**
     * Increments the cache hits metric
     */
    static void incrementCacheHits() {
        CACHE_HIT.increment();
    }

    /**
     * Increments the cache misses metric
     */
    static void incrementCacheMisses() {
        CACHE_MISS.increment();
    }

    /**
     * Increments the cache invalidations metric
     */
    static void incrementCacheInvalidations() {
        CACHE_INVALIDATIONS.increment();
    }

    /**
     * Records the scan time
     */
    static void recordScanTime(long  duration) {
        SCANNING_RATE.record(duration, TimeUnit.MILLISECONDS);
    }

    /**
     * Updates the scanning time by the specified elapsed time
     *
     * @param timeElapsed The elapsed time in milliseconds
     */
    static void updateScanningTime(long timeElapsed) {
        SCANNING_TIME.record(timeElapsed, TimeUnit.MILLISECONDS);
    }

    /**
     * Updates the transfer size by the specified content length
     *
     * @param contentLength The content length in bytes
     */
    static void updateTransferRate(long contentLength) {
        TRANSFER_SIZE.increment(contentLength);
    }

}
