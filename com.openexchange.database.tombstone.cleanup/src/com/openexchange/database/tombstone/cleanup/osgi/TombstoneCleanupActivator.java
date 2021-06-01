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

package com.openexchange.database.tombstone.cleanup.osgi;

import org.slf4j.Logger;
import com.openexchange.config.ConfigTools;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.DefaultInterests;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.cleanup.CleanUpInfo;
import com.openexchange.database.cleanup.CleanUpJob;
import com.openexchange.database.cleanup.DatabaseCleanUpService;
import com.openexchange.database.tombstone.cleanup.SchemaTombstoneCleaner;
import com.openexchange.database.tombstone.cleanup.config.TombstoneCleanupConfig;
import com.openexchange.database.tombstone.cleanup.update.InitialTombstoneCleanupUpdateTask;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.Tools;

/**
 *
 * {@link TombstoneCleanupActivator}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.2
 */
public class TombstoneCleanupActivator extends HousekeepingActivator implements Reloadable {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(TombstoneCleanupActivator.class);

    private CleanUpInfo cleanupTask;

    /**
     * Initializes a new {@link TombstoneCleanupActivator}.
     */
    public TombstoneCleanupActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { LeanConfigurationService.class, DatabaseService.class, ContextService.class, DatabaseCleanUpService.class };
    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        LOGGER.info("Starting bundle: {}", context.getBundle().getSymbolicName());
        Services.setServiceLookup(this);

        LeanConfigurationService leanConfig = Tools.requireService(LeanConfigurationService.class, this);
        registerService(Reloadable.class, this);

        initCleanupTask(leanConfig, true);
    }

    private void initCleanupTask(LeanConfigurationService leanConfig, boolean registerUpdateTask) throws OXException {
        String timespanStr = leanConfig.getProperty(TombstoneCleanupConfig.TIMESPAN).trim();
        long timespan = ConfigTools.parseTimespan(timespanStr);
        if (timespan < 1) {
            LOGGER.warn("Cleanup enabled but no meaningful value defined: \"{}\" Will use the default of 12 weeks (~3 months).", timespanStr);
            timespan = ConfigTools.parseTimespan(TombstoneCleanupConfig.TIMESPAN.getDefaultValue(String.class));
        }
        if (!leanConfig.getBooleanProperty(TombstoneCleanupConfig.ENABLED)) {
            LOGGER.info("Skipped starting database cleanup task based on configuration.");
            return;
        }

        DatabaseCleanUpService cleanUpService = Tools.requireService(DatabaseCleanUpService.class, this);
        CleanUpJob cleanUpJob = SchemaTombstoneCleaner.getCleanUpJob(timespan);
        this.cleanupTask = cleanUpService.scheduleCleanUpJob(cleanUpJob);

        if (registerUpdateTask) {
            registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new InitialTombstoneCleanupUpdateTask(timespan)));
        }
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        CleanUpInfo cleanupTask = this.cleanupTask;
        if (cleanupTask != null) {
            this.cleanupTask = null;
            cleanupTask.cancel(true);
        }
        Services.setServiceLookup(null);

        super.stopBundle();
        LOGGER.info("Successfully stopped bundle {}", this.context.getBundle().getSymbolicName());
    }

    @Override
    public synchronized void reloadConfiguration(ConfigurationService configService) {
        LeanConfigurationService leanConfig;
        try {
            leanConfig = Tools.requireService(LeanConfigurationService.class, this);

            if (this.cleanupTask != null) {
                cleanupTask.cancel(true);
            }

            initCleanupTask(leanConfig, false);
        } catch (Exception e) {
            LOGGER.error("Encountered an error while restarting database cleanup task", e);
        }
    }

    @Override
    public Interests getInterests() {
        return DefaultInterests.builder().propertiesOfInterest(TombstoneCleanupConfig.TIMESPAN.getFQPropertyName()).build();
    }
}
