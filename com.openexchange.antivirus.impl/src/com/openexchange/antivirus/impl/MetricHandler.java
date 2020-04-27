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

import java.time.Duration;
import com.google.common.cache.Cache;
import com.openexchange.antivirus.AntiVirusResult;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.cache.GuavaCacheMetrics;

/**
 * {@link MetricHandler} - Simple utility class to handle metric updates
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.2
 */
final class MetricHandler {

    private final Counter byteTransferCounter;

    /**
     * Initializes a new {@link MetricHandler}.
     * @param cachedResults
     */
    public MetricHandler(Cache<String, AntiVirusResult> cachedResults) {
        super();
        GuavaCacheMetrics.monitor(Metrics.globalRegistry, cachedResults, "antivirus");
        this.byteTransferCounter = Counter.builder("appsuite.antivirus.transfer")
            .description("Measures the amount of bytes transfered to the anti-virus server")
            .baseUnit("bytes")
            .register(Metrics.globalRegistry);
    }

    /**
     * recordScanIOError
     *
     * @param duration
     */
    public void recordScanIOError(Duration duration) {
        Timer timer = Timer.builder("appsuite.antivirus.scans.duration")
            .description("Measures the number of files scanned per second")
            .tags("status", "IO_ERROR")
            .register(Metrics.globalRegistry);
        timer.record(duration);
    }

    /**
     * recordScanResult
     *
     * @param statusCode
     * @param duration
     * @param contentLength
     */
    public void recordScanResult(int statusCode, Duration duration, long contentLength) {
        Timer timer = Timer.builder("appsuite.antivirus.scans.duration")
            .description("Measures the number of files scanned per second")
            .tags("status", Integer.toString(statusCode))
            .register(Metrics.globalRegistry);
        timer.record(duration);

        byteTransferCounter.increment(contentLength);
    }

}
