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
import com.openexchange.push.PushManagerService;
import com.openexchange.push.malpoll.MALPollCreateTableTask;
import com.openexchange.push.malpoll.MALPollDeleteListener;
import com.openexchange.push.malpoll.MALPollMailAccountDeleteListener;
import com.openexchange.push.malpoll.MALPollPushListener;
import com.openexchange.push.malpoll.MALPollPushListenerRegistry;
import com.openexchange.push.malpoll.MALPollPushListenerRunnable;
import com.openexchange.push.malpoll.MALPollPushManagerService;
import com.openexchange.push.malpoll.UpdateTaskPublisher;
import com.openexchange.push.malpoll.services.MALPollServiceRegistry;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;
import com.openexchange.userconf.UserPermissionService;

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
        return new Class<?>[] {
            MailService.class, EventAdmin.class, TimerService.class, ConfigurationService.class, DatabaseService.class,
            ContextService.class };
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
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
    protected void handleUnavailability(final Class<?> clazz) {
        LOG.warn("Absent service: {}", clazz.getName());
        if (TimerService.class == clazz) {
            MALPollPushListenerRegistry.getInstance().closeAll();
            stopScheduledTask(getService(TimerService.class));
        }
        getServiceRegistry().removeService(clazz);
    }

    @Override
    protected void startBundle() throws Exception {
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
            trackService(UserPermissionService.class);
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
                    } catch (final NumberFormatException e) {
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
        } catch (final Exception e) {
            LOG.error("", e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        try {
            cleanUp();
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
        } catch (final Exception e) {
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
                    } catch (final OXException e) {
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
                } catch (final Exception e) {
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
