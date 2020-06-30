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

package com.openexchange.mail.compose.impl.cleanup;

import static com.openexchange.java.Autoboxing.I;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import com.openexchange.config.ConfigTools;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.cascade.ConfigViews;
import com.openexchange.exception.OXException;
import com.openexchange.mail.compose.CompositionSpaceService;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.session.UserAndContext;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessiond.SessiondServiceExtended;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.behavior.CallerRunsBehavior;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link CompositionSpaceCleanUpRegistry}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class CompositionSpaceCleanUpRegistry implements EventHandler {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(CompositionSpaceCleanUpRegistry.class);
    }

    private static final AtomicReference<CompositionSpaceCleanUpRegistry> INSTANCE_REFERENCE = new AtomicReference<>(null);

    /**
     * Initializes the instance
     *
     * @param compositionSpaceService The service to use
     * @param services The service look-up to use
     * @return The freshly initialized instance or empty if already initialized before
     * @throws OXException If initialization fails
     */
    public static synchronized Optional<CompositionSpaceCleanUpRegistry> initInstance(CompositionSpaceService compositionSpaceService, ServiceLookup services) throws OXException {
        if (INSTANCE_REFERENCE.get() != null) {
            // Already initialized
            return Optional.empty();
        }

        CompositionSpaceCleanUpRegistry instance = new CompositionSpaceCleanUpRegistry(compositionSpaceService, services);
        INSTANCE_REFERENCE.set(instance);
        return Optional.of(instance);
    }

    /**
     * Releases the instance
     */
    public static synchronized void releaseInstance() {
        CompositionSpaceCleanUpRegistry instance = INSTANCE_REFERENCE.getAndSet(null);
        if (instance != null) {
            instance.checkerTask.cancel(true);

            TimerService timerService = instance.services.getOptionalService(TimerService.class);
            if (timerService != null) {
                timerService.purge();
            }
        }
    }

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static CompositionSpaceCleanUpRegistry getInstance() {
        return INSTANCE_REFERENCE.get();
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final ConcurrentMap<UserAndContext, CleanUpTask> tasks;
    private final CompositionSpaceService compositionSpaceService;
    private final ScheduledTimerTask checkerTask;
    private final ServiceLookup services;


    /**
     * Initializes a new {@link CompositionSpaceCleanUpRegistry}.
     *
     * @param compositionSpaceService The service to use
     * @param services The service look-up to use
     * @throws OXException If initialization fails
     */
    private CompositionSpaceCleanUpRegistry(CompositionSpaceService compositionSpaceService, ServiceLookup services) throws OXException {
        super();
        this.compositionSpaceService = compositionSpaceService;
        this.services = services;
        ConcurrentMap<UserAndContext, CleanUpTask> tasks = new ConcurrentHashMap<>(256, 0.9F, 1);
        this.tasks = tasks;

        TimerService timerService = services.getOptionalService(TimerService.class);
        if (timerService == null) {
            throw ServiceExceptionCode.absentService(TimerService.class);
        }

        Runnable r = new Runnable() {

            @Override
            public void run() {
                SessiondService sessiondService = services.getOptionalService(SessiondService.class);
                if (sessiondService == null) {
                    return;
                }

                if (sessiondService instanceof SessiondServiceExtended) {
                    SessiondServiceExtended sessiondServiceExtended = (SessiondServiceExtended) sessiondService;

                    Thread currentThread = Thread.currentThread();
                    for (Iterator<Entry<UserAndContext, CleanUpTask>> it = tasks.entrySet().iterator(); !currentThread.isInterrupted() && it.hasNext();) {
                        checkEntry(it.next(), it, sessiondServiceExtended, services);
                    }
                }
            }

            private void checkEntry(Map.Entry<UserAndContext, CleanUpTask> entry, Iterator<Entry<UserAndContext, CleanUpTask>> it, SessiondServiceExtended sessiondServiceExtended, ServiceLookup services) {
                UserAndContext key = entry.getKey();
                CleanUpTask cleanUpTask = entry.getValue();

                if (sessiondServiceExtended.getActiveSessions(key.getUserId(), key.getContextId()).isEmpty()) {
                    // Apparently no active user-associated session available
                    it.remove();

                    Optional<ScheduledTimerTask> optionalTimerTask = cleanUpTask.getAndDropTimerTask();
                    if (optionalTimerTask.isPresent()) {
                        optionalTimerTask.get().cancel();

                        TimerService timerService = services.getOptionalService(TimerService.class);
                        if (timerService != null) {
                            timerService.purge();
                        }
                    }
                }
            }

        };
        checkerTask = timerService.scheduleWithFixedDelay(r, 1, 1, TimeUnit.DAYS);
    }

    /**
     * Schedules clean-up task for given arguments.
     *
     * @param session The session
     * @return <code>true</code> if caller scheduled clean-up task; otherwise <code>false</code> if there was already such a task
     * @throws OXException If operation fails
     */
    public boolean scheduleCleanUpFor(Session session) throws OXException {
        boolean scheduleTask = false;
        UserAndContext key = UserAndContext.newInstance(session);
        CleanUpTask task = tasks.get(key);
        if (task == null) {
            CleanUpTask newTask = new CleanUpTask(session, compositionSpaceService, this, services);
            task = tasks.putIfAbsent(key, newTask);
            if (task == null) {
                scheduleTask = true;
                task = newTask;
            }
        }

        synchronized (task) {
            if (scheduleTask) {
                // This current thread put task into concurrent map
                TimerService timerService = services.getOptionalService(TimerService.class);
                if (timerService == null) {
                    task.markObsolete();
                    tasks.remove(key);
                    throw ServiceExceptionCode.absentService(TimerService.class);
                }

                try {
                    ScheduledTimerTask timerTask = timerService.scheduleWithFixedDelay(task, 5000L, 3600000L); // Every 60 minutes
                    task.setTimerTask(timerTask);
                } catch (Throwable t) {
                    task.markObsolete();
                    tasks.remove(key);
                    if (t instanceof Error) {
                        throw (Error) t;
                    }
                    throw OXException.general("Failed to schedule clean-up task for expired composition spaces for user " + session.getUserId() + " in context " + session.getContextId(), t);
                }
            } else {
                // Check if obsolete
                if (task.isObsolete()) {
                    return scheduleCleanUpFor(session);
                }
                task.addSessionId(session.getSessionID());
            }
        }

        return scheduleTask;
    }

    @Override
    public void handleEvent(Event event) {
        if (false == SessiondEventConstants.TOPIC_LAST_SESSION.equals(event.getTopic())) {
            return;
        }

        ThreadPoolService threadPool = services.getOptionalService(ThreadPoolService.class);
        if (null == threadPool) {
            doHandleEvent(event);
        } else {
            AbstractTask<Void> t = new AbstractTask<Void>() {

                @Override
                public Void call() throws Exception {
                    try {
                        doHandleEvent(event);
                    } catch (Exception e) {
                        LoggerHolder.LOG.warn("Handling event {} failed.", event.getTopic(), e);
                    }
                    return null;
                }
            };
            threadPool.submit(t, CallerRunsBehavior.<Void> getInstance());
        }
    }

    /**
     * Handles given event.
     *
     * @param lastSessionEvent The event
     */
    protected void doHandleEvent(Event lastSessionEvent) {
        Integer contextId = (Integer) lastSessionEvent.getProperty(SessiondEventConstants.PROP_CONTEXT_ID);
        if (null != contextId) {
            Integer userId = (Integer) lastSessionEvent.getProperty(SessiondEventConstants.PROP_USER_ID);
            if (null != userId) {
                removeCleanUpTaskFor(userId.intValue(), contextId.intValue());
            }
        }
    }

    /**
     * Removes the clean-up task for given user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public void removeCleanUpTaskFor(int userId, int contextId) {
        CleanUpTask removed = tasks.remove(UserAndContext.newInstance(userId, contextId));
        if (removed != null) {
            Optional<ScheduledTimerTask> optionalTimerTask = removed.getAndDropTimerTask();
            if (optionalTimerTask.isPresent()) {
                optionalTimerTask.get().cancel();

                TimerService timerService = services.getOptionalService(TimerService.class);
                if (timerService != null) {
                    timerService.purge();
                }
            }
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static class CleanUpTask implements Runnable {

        private static final Object PRESENT = new Object();

        private final ConcurrentMap<String, Object> sessionIds;
        private final int userId;
        private final int contextId;
        private final CompositionSpaceService compositionSpaceService;
        private final CompositionSpaceCleanUpRegistry cleanUpRegistry;
        private final ServiceLookup services;
        private boolean obsolete; // Guarded by synchronized
        private final AtomicReference<ScheduledTimerTask> timerTaskReference;

        /**
         * Initializes a new {@link CleanUpTask}.
         *
         * @param initiatingSession The initial session for which this task is started
         * @param compositionSpaceService The service used to drop expired composition spaces
         * @param cleanUpRegistry The clean-up registry reference
         * @param services The service look-up to obtain needed services
         */
        CleanUpTask(Session initiatingSession, CompositionSpaceService compositionSpaceService, CompositionSpaceCleanUpRegistry cleanUpRegistry, ServiceLookup services) {
            super();
            this.cleanUpRegistry = cleanUpRegistry;
            this.sessionIds = new ConcurrentHashMap<>(10, 0.9F, 1);
            this.sessionIds.put(initiatingSession.getSessionID(), PRESENT);
            userId = initiatingSession.getUserId();
            contextId = initiatingSession.getContextId();
            this.compositionSpaceService = compositionSpaceService;
            this.services = services;
            obsolete = false;
            timerTaskReference = new AtomicReference<>(null);
        }

        /**
         * Sets the timer task that cares about periodic execution of this clean-up task.
         *
         * @param timerTask The timer task to set
         */
        void setTimerTask(ScheduledTimerTask timerTask) {
            timerTaskReference.set(timerTask);
        }

        /**
         * Gets the currently active timer task (if any) that cares about periodic execution of this clean-up task.
         *
         * @return The timer task or <code>null</code>
         */
        Optional<ScheduledTimerTask> getAndDropTimerTask() {
            return Optional.ofNullable(timerTaskReference.getAndSet(null));
        }

        /**
         * Marks this task as obsolete
         */
        void markObsolete() {
            obsolete = true;
        }

        /**
         * Checks if this task became obsolete
         *
         * @return <code>true</code> if obsolete; otherwise <code>false</code> if still active
         */
        boolean isObsolete() {
            return obsolete;
        }

        /**
         * Adds given session identifier to collection of known user-associated sessions.
         *
         * @param sessionId The session identifier to add
         */
        void addSessionId(String sessionId) {
            this.sessionIds.put(sessionId, PRESENT);
        }

        @Override
        public void run() {
            try {
                SessiondServiceExtended sessiondService = (SessiondServiceExtended) services.getServiceSafe(SessiondService.class);
                Session session = null;
                for (Iterator<Map.Entry<String, Object>> it = sessionIds.entrySet().iterator(); session == null && it.hasNext();) {
                    session = sessiondService.peekSession(it.next().getKey(), false);
                    if (session == null) {
                        // No such session
                        it.remove();
                    }
                }

                if (session == null) {
                    // No suitable session available (anymore)
                    cleanUpRegistry.removeCleanUpTaskFor(userId, contextId);
                    return;
                }

                long maxIdleTimeMillis = getMaxIdleTimeMillis(session);
                if (maxIdleTimeMillis > 0) {
                    compositionSpaceService.closeExpiredCompositionSpaces(maxIdleTimeMillis, session);
                }
            } catch (Exception e) {
                LoggerHolder.LOG.error("Failed to clean-up expired composition spaces for user {} in context {}", I(userId), I(contextId), e);
            }
        }

        private long getMaxIdleTimeMillis(Session session) throws OXException {
            String defaultValue = "1W";

            ConfigViewFactory viewFactory = services.getOptionalService(ConfigViewFactory.class);
            if (null == viewFactory) {
                return ConfigTools.parseTimespan(defaultValue);
            }

            ConfigView view = viewFactory.getView(session.getUserId(), session.getContextId());
            return ConfigTools.parseTimespan(ConfigViews.getDefinedStringPropertyFrom("com.openexchange.mail.compose.maxIdleTimeMillis", defaultValue, view));
        }
    } // End of class CleanUpTask

}
