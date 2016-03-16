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

package com.openexchange.share.impl.cleanup;

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.cluster.timer.ClusterTimerService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.java.BufferingQueue;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.tools.strings.TimeSpanParser;

/**
 * {@link GuestCleaner}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class GuestCleaner {

    private static final Logger LOG = LoggerFactory.getLogger(GuestCleaner.class);

    private final ServiceLookup services;
    private final BufferingQueue<GuestCleanupTask> cleanupTasks;
    private final BackgroundGuestCleaner backgroundCleaner;
    private final long guestExpiry;
    private final ScheduledTimerTask periodicCleanerTask;
    private final PeriodicCleaner periodicCleaner;

    /**
     * Initializes a new {@link GuestCleaner}.
     *
     * @param services A service lookup reference
     */
    public GuestCleaner(final ServiceLookup services) throws OXException {
        super();
        this.services = services;
        ConfigurationService configService = services.getService(ConfigurationService.class);
        this.guestExpiry = parseTimespanProperty(
            configService, "com.openexchange.share.cleanup.guestExpiry", DAYS.toMillis(14), DAYS.toMillis(1), true);
        /*
         * prepare background task queue and worker thread
         */
        long delayDuration = parseTimespanProperty(
            configService, "com.openexchange.share.cleanup.delayDuration", 2000, 200, false);
        long maxDelayDuration = parseTimespanProperty(
            configService, "com.openexchange.share.cleanup.maxDelayDuration", 0, 0, true);
        this.cleanupTasks = 0 < maxDelayDuration ? new BufferingQueue<GuestCleanupTask>(delayDuration, maxDelayDuration) :
            new BufferingQueue<GuestCleanupTask>(delayDuration);
        this.backgroundCleaner = new BackgroundGuestCleaner(cleanupTasks);
        services.getService(ExecutorService.class).submit(backgroundCleaner);
        /*
         * schedule context cleanups regularly
         */
        long periodicCleanerInterval = parseTimespanProperty(
            configService, "com.openexchange.share.cleanup.periodicCleanerInterval", DAYS.toMillis(1), HOURS.toMillis(1), true);
        if (0 < periodicCleanerInterval) {
            this.periodicCleaner = new PeriodicCleaner(services, guestExpiry);
            this.periodicCleanerTask = services.getService(ClusterTimerService.class).scheduleWithFixedDelay(
                PeriodicCleaner.class.getName(), periodicCleaner, periodicCleanerInterval, periodicCleanerInterval);
        } else {
            periodicCleaner = null;
            periodicCleanerTask = null;
        }
    }

    /**
     * Stops all background processing by signaling a termination flag.
     */
    public void stop() {
        if (null != backgroundCleaner) {
            backgroundCleaner.stop();
        }
        if (null != periodicCleaner) {
            periodicCleaner.stop();
        }
        if (null != periodicCleanerTask) {
            periodicCleanerTask.cancel();
        }
    }

    /**
     * Asynchronously cleans obsolete shares and corresponding guest user remnants for a context.
     *
     * @param contextID The context ID
     */
    public void scheduleContextCleanup(final int contextID) throws OXException {
        LOG.debug("Scheduling context cleanup task for context {}.", contextID);
        services.getService(ExecutorService.class).submit(new Runnable() {

            @Override
            public void run() {
                try {
                    ContextCleanupTask contextCleanupTask = new ContextCleanupTask(services, contextID, guestExpiry);
                    List<GuestCleanupTask> tasks = contextCleanupTask.call();
                    cleanupTasks.offerIfAbsentElseReset(tasks);
                } catch (Exception e) {
                    LOG.warn("error enqueuing cleanup tasks.", e);
                }
            }
        });
    }

    /**
     * Asynchronously cleans obsolete shares and corresponding guest user remnants for specific guest users in a context.
     *
     * @param contextID The context ID
     * @param guestIDs The identifiers of the guest users to consider for cleanup
     */
    public void scheduleGuestCleanup(int contextID, int[] guestIDs) throws OXException {
        LOG.debug("Scheduling guest cleanup tasks for guest users {} in context {}.", Arrays.toString(guestIDs), contextID);
        cleanupTasks.offerIfAbsentElseReset(GuestCleanupTask.create(services, contextID, guestIDs, guestExpiry));
    }

    private static long parseTimespanProperty(ConfigurationService configService, String propertyName, long defaultValue, long minimumValue, boolean allowDisabling) throws OXException  {
        String value = configService.getProperty(propertyName);
        if (Strings.isEmpty(value)) {
            return defaultValue;
        }
        long timespan;
        try {
            timespan = TimeSpanParser.parseTimespan(value).longValue();
        } catch (IllegalArgumentException e) {
            throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create(e, propertyName);
        }
        if (allowDisabling && 0 >= timespan) {
            return 0;
        }
        if (0 < minimumValue && minimumValue > timespan) {
            LOG.warn("Ignoring too low value of \"{}\" for \"{}\", falling back to defaults.", value, propertyName);
            return defaultValue;
        }
        return timespan;
    }

}
