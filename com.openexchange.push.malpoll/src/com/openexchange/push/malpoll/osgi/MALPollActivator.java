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

package com.openexchange.push.malpoll.osgi;

import static com.openexchange.push.malpoll.services.MALPollServiceRegistry.getServiceRegistry;
import java.util.Iterator;
import java.util.concurrent.Executor;
import org.osgi.service.event.EventAdmin;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.mail.service.MailService;
import com.openexchange.mailaccount.MailAccountDeleteListener;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.RegistryServiceTrackerCustomizer;
import com.openexchange.pns.PushNotificationService;
import com.openexchange.push.PushManagerService;
import com.openexchange.push.malpoll.MALPollPushListener;
import com.openexchange.push.malpoll.MALPollPushListenerRegistry;
import com.openexchange.push.malpoll.MALPollPushListenerRunnable;
import com.openexchange.push.malpoll.MALPollPushManagerService;
import com.openexchange.push.malpoll.groupware.MALPollCreateTableTask;
import com.openexchange.push.malpoll.groupware.MALPollDeleteListener;
import com.openexchange.push.malpoll.groupware.MALPollMailAccountDeleteListener;
import com.openexchange.push.malpoll.groupware.UpdateTaskPublisher;
import com.openexchange.push.malpoll.services.MALPollServiceRegistry;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link MALPollActivator} - The MAL Poll activator.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MALPollActivator extends HousekeepingActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MALPollActivator.class);

    private ScheduledTimerTask scheduledTimerTask;

    private long period;

    private String folder;

    private boolean global;

    private boolean concurrentGlobal;

    /**
     * Initializes a new {@link MALPollActivator}.
     */
    public MALPollActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { MailService.class, EventAdmin.class, TimerService.class, ConfigurationService.class, DatabaseService.class, ContextService.class };
    }

    @Override
    protected synchronized void handleAvailability(final Class<?> clazz) {
        LOG.info("Re-available service: {}", clazz.getName());
        getServiceRegistry().addService(clazz, getService(clazz));
        if (TimerService.class == clazz) {
            MALPollPushListenerRegistry.getInstance().openAll();
            // Start global if configured
            if (global) {
                startScheduledTask(getService(TimerService.class), period, concurrentGlobal);
            }
        }
    }

    @Override
    protected synchronized void handleUnavailability(final Class<?> clazz) {
        LOG.warn("Absent service: {}", clazz.getName());
        if (TimerService.class == clazz) {
            MALPollPushListenerRegistry.getInstance().closeAll();
            stopScheduledTask(getService(TimerService.class));
        }
        getServiceRegistry().removeService(clazz);
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        try {
            /*
             * (Re-)Initialize service registry with available services
             */
            {
                final MALPollServiceRegistry registry = getServiceRegistry();
                registry.clearRegistry();
                final Class<?>[] classes = getNeededServices();
                for (int i = 0; i < classes.length; i++) {
                    final Object service = getService(classes[i]);
                    if (null != service) {
                        registry.addService(classes[i], service);
                    }
                }
            }
            /*
             * Initialize & open tracker for SessionD service
             */
            {
                final ServiceTrackerCustomizer<SessiondService,SessiondService> trackerCustomizer =
                    new RegistryServiceTrackerCustomizer<SessiondService>(context, getServiceRegistry(), SessiondService.class);
                track(SessiondService.class, trackerCustomizer);
            }
            /*
             * Initialize & open tracker for PNS service
             */
            {
                final ServiceTrackerCustomizer<PushNotificationService,PushNotificationService> trackerCustomizer =
                    new RegistryServiceTrackerCustomizer<PushNotificationService>(context, getServiceRegistry(), PushNotificationService.class);
                track(PushNotificationService.class, trackerCustomizer);
            }
            openTrackers();
            /*
             * Read configuration
             */
            final ConfigurationService configurationService = getService(ConfigurationService.class);
            period = 300000L;
            {
                final String tmp = configurationService.getProperty("com.openexchange.push.malpoll.period");
                if (null != tmp) {
                    try {
                        period = Long.parseLong(tmp.trim());
                    } catch (NumberFormatException e) {
                        LOG.error("Unable to parse com.openexchange.push.malpoll.period: {}. Using default 300000 (5 Minutes) instead.",
                            tmp);
                        period = 300000L;
                    }
                }
            }
            folder = "INBOX";
            {
                final String tmp = configurationService.getProperty("com.openexchange.push.malpoll.folder");
                if (null != tmp) {
                    folder = tmp.trim();
                }
            }
            global = true;
            {
                final String tmp = configurationService.getProperty("com.openexchange.push.malpoll.global");
                if (null != tmp) {
                    global = Boolean.parseBoolean(tmp.trim());
                }
            }
            concurrentGlobal = true;
            {
                final String tmp = configurationService.getProperty("com.openexchange.push.malpoll.concurrentglobal");
                if (null != tmp) {
                    concurrentGlobal = Boolean.parseBoolean(tmp.trim());
                }
            }
            /*
             * Start-up
             */
            MALPollPushListener.setFolder(folder);
            MALPollPushListener.setPeriodMillis(period);
            MALPollPushManagerService.setStartTimerTaskPerListener(!global);
            if (global) {
                startScheduledTask(getService(TimerService.class), period, concurrentGlobal);
            }
            /*
             * Register push manager
             */
            registerService(CreateTableService.class, new MALPollCreateTableTask(), null);
            registerService(UpdateTaskProviderService.class, new UpdateTaskPublisher(), null);
            registerService(PushManagerService.class, new MALPollPushManagerService(), null);
            registerService(MailAccountDeleteListener.class, new MALPollMailAccountDeleteListener(), null);
            registerService(DeleteListener.class, new MALPollDeleteListener(), null);
        } catch (Exception e) {
            LOG.error("", e);
            throw e;
        }
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        try {
            /*
             * Clear all running listeners
             */
            MALPollPushListenerRegistry.getInstance().purgeAllPushListener();
            /*
             * Shut down
             */
            stopScheduledTask(getService(TimerService.class));
            MALPollPushListener.setFolder(null);
            MALPollPushManagerService.setStartTimerTaskPerListener(true);
            MALPollPushListener.setPeriodMillis(0L);
            MALPollPushListenerRegistry.getInstance().clear();
            /*
             * Clear service registry
             */
            getServiceRegistry().clearRegistry();
            /*
             * Reset
             */
            global = true;
            folder = null;
            period = 300000L;
            super.stopBundle();
        } catch (Exception e) {
            LOG.error("", e);
            throw e;
        }
    }

    private void startScheduledTask(final TimerService timerService, final long periodMillis, final boolean parallel) {
        /*
         * Create either an executor starter or a caller-run starter
         */
        final org.slf4j.Logger log = LOG;
        final Starter starter;
        if (parallel) {
            /*
             * An executor-starter
             */
            starter = new Starter() {

                private final Executor executor = timerService.getExecutor();

                @Override
                public void start(final MALPollPushListener l) {
                    /*
                     * Delegate execution to Executor instance
                     */
                    executor.execute(new MALPollPushListenerRunnable(l));
                }
            };
        } else {
            /*
             * A caller-runs-starter
             */
            starter = new Starter() {

                @Override
                public void start(final MALPollPushListener l) {
                    /*
                     * Execute in current thread
                     */
                    try {
                        l.checkNewMail();
                    } catch (OXException e) {
                        log.error("", e);
                    }
                }
            };
        }
        /*
         * Create global runnable
         */
        final Runnable r = new Runnable() {

            @Override
            public void run() {
                try {
                    for (final Iterator<MALPollPushListener> pushListeners = MALPollPushListenerRegistry.getInstance().getPushListeners(); pushListeners.hasNext();) {
                        final MALPollPushListener l = pushListeners.next();
                        if (!l.isIgnoreOnGlobal()) {
                            starter.start(l);
                        }
                    }
                    log.debug("Global run for checking new mails done.");
                } catch (Exception e) {
                    log.error("", e);
                }
            }

        };
        scheduledTimerTask = timerService.scheduleWithFixedDelay(r, 1000, periodMillis);
    }

    private void stopScheduledTask(final TimerService timerService) {
        if (null != scheduledTimerTask) {
            scheduledTimerTask.cancel();
            scheduledTimerTask = null;
            if (null != timerService) {
                timerService.purge();
            }
        }
    }

    /**
     * Simple helper interface to decouple listener handling from global {@link Runnable}.
     */
    private static interface Starter {

        void start(MALPollPushListener l);
    }

}
