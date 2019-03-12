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

package com.openexchange.database.tombstone.cleanup.osgi;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import com.openexchange.cluster.timer.ClusterTimerService;
import com.openexchange.config.ConfigTools;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.DefaultInterests;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.tombstone.cleanup.TombstoneCleanerWorker;
import com.openexchange.database.tombstone.cleanup.config.TombstoneCleanupConfig;
import com.openexchange.database.tombstone.cleanup.update.InitialTombstoneCleanupUpdateTask;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.Tools;
import com.openexchange.timer.ScheduledTimerTask;

/**
 *
 * {@link TombstoneCleanupActivator}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.2
 */
public class TombstoneCleanupActivator extends HousekeepingActivator implements Reloadable {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(TombstoneCleanupActivator.class);
    private static final String CLUSTER_ID = "com.openexchange.database.tombstone.cleanup";
    private ScheduledTimerTask cleanupTask;
    private final AtomicBoolean REGISTERED = new AtomicBoolean(false);
    private TombstoneCleanerWorker tombstoneCleanerWorker;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { LeanConfigurationService.class, ClusterTimerService.class, DatabaseService.class };
    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

    @Override
    protected void startBundle() throws Exception {
        LOGGER.info("Starting bundle: {}", context.getBundle().getSymbolicName());
        Services.setServiceLookup(this);

        LeanConfigurationService leanConfig = Tools.requireService(LeanConfigurationService.class, this);
        registerService(Reloadable.class, this);

        initCleanupTimerTask(leanConfig);
    }

    private void initCleanupTimerTask(LeanConfigurationService leanConfig) throws OXException {
        String timespanStr = leanConfig.getProperty(TombstoneCleanupConfig.TIMESPAN);
        long timespan = ConfigTools.parseTimespan(timespanStr);
        if (timespan < 1) {
            LOGGER.warn("Cleanup enabled but no meaningful value defined. Will use the default of 3 months.");
            timespan = ConfigTools.parseTimespan(TombstoneCleanupConfig.TIMESPAN.getDefaultValue(String.class));
        }
        if (!leanConfig.getBooleanProperty(TombstoneCleanupConfig.ENABLED)) {
            LOGGER.info("Skipped starting database cleanup task based on configuration.");
            return;
        }

        ClusterTimerService clusterTimerService = Tools.requireService(ClusterTimerService.class, this);

        // we have to set an initial delay as the HazelcastInstance will be registered latest. Therefore switching to TimeUnit.Minutes instead of handling via days
        this.tombstoneCleanerWorker = new TombstoneCleanerWorker(timespan);
        this.cleanupTask = clusterTimerService.scheduleAtFixedRate(CLUSTER_ID, this.tombstoneCleanerWorker, 60, TimeUnit.DAYS.toMinutes(1), TimeUnit.MINUTES);

        if (REGISTERED.compareAndSet(false, true)) {
            registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new InitialTombstoneCleanupUpdateTask(timespan)));
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        if (this.tombstoneCleanerWorker != null) {
            this.tombstoneCleanerWorker.stop();
        }
        if (this.cleanupTask != null) {
            this.cleanupTask.cancel(true);
        }
        Services.setServiceLookup(null);

        super.stopBundle();
        LOGGER.info("Successfully stopped bundle {}", this.context.getBundle().getSymbolicName());
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        LeanConfigurationService leanConfig;
        try {
            leanConfig = Tools.requireService(LeanConfigurationService.class, this);
            if (this.cleanupTask != null) {
                cleanupTask.cancel(true);
            }

            initCleanupTimerTask(leanConfig);
        } catch (Exception e) {
            LOGGER.error("Encountered an error while restarting database cleanup task", e);
        }
    }

    @Override
    public Interests getInterests() {
        return DefaultInterests.builder().propertiesOfInterest(TombstoneCleanupConfig.TIMESPAN.getFQPropertyName()).build();
    }
}
