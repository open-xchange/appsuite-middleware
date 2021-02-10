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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.config.ConfigTools;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.cascade.ConfigViews;
import com.openexchange.exception.OXException;
import com.openexchange.mail.compose.CompositionSpaceServiceFactory;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.timer.TimerService;

/**
 * {@link CompositionSpaceCleanUpScheduler}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class CompositionSpaceCleanUpScheduler {

    /** The logger constant */
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CompositionSpaceCleanUpScheduler.class);

    private static final AtomicReference<CompositionSpaceCleanUpScheduler> INSTANCE_REFERENCE = new AtomicReference<>(null);

    /**
     * Initializes the instance
     *
     * @param compositionSpaceServiceFactory The service factory to use
     * @param services The service look-up to use
     * @return The freshly initialized instance or empty if already initialized before
     */
    public static synchronized Optional<CompositionSpaceCleanUpScheduler> initInstance(CompositionSpaceServiceFactory compositionSpaceServiceFactory, ServiceLookup services) {
        if (INSTANCE_REFERENCE.get() != null) {
            // Already initialized
            return Optional.empty();
        }

        CompositionSpaceCleanUpScheduler instance = new CompositionSpaceCleanUpScheduler(compositionSpaceServiceFactory, services);
        INSTANCE_REFERENCE.set(instance);
        return Optional.of(instance);
    }

    /**
     * Releases the instance
     */
    public static synchronized void releaseInstance() {
        INSTANCE_REFERENCE.getAndSet(null);
    }

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static CompositionSpaceCleanUpScheduler getInstance() {
        return INSTANCE_REFERENCE.get();
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final CompositionSpaceServiceFactory compositionSpaceServiceFactory;
    private final ServiceLookup services;


    /**
     * Initializes a new {@link CompositionSpaceCleanUpScheduler}.
     *
     * @param compositionSpaceServiceFactory The service factory to use
     * @param services The service look-up to use
     */
    private CompositionSpaceCleanUpScheduler(CompositionSpaceServiceFactory compositionSpaceServiceFactory, ServiceLookup services) {
        super();
        this.compositionSpaceServiceFactory = compositionSpaceServiceFactory;
        this.services = services;
    }

    /**
     * Schedules clean-up task for given arguments.
     *
     * @param session The session
     * @return <code>true</code> if caller scheduled clean-up task; otherwise <code>false</code>
     */
    public boolean scheduleCleanUpFor(Session session) {
        TimerService timerService = services.getOptionalService(TimerService.class);
        if (timerService == null) {
            return false;
        }

        timerService.schedule(new CleanUpTask(session, compositionSpaceServiceFactory, services), 5000L);
        LOG.debug("Scheduled composition space clean-up task for user {} in context {}", I(session.getUserId()), I(session.getContextId()));
        return true;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static class CleanUpTask implements Runnable {

        private final Session session;
        private final CompositionSpaceServiceFactory compositionSpaceServiceFactory;
        private final ServiceLookup services;

        /**
         * Initializes a new {@link CleanUpTask}.
         *
         * @param session The session for which this task is started
         * @param compositionSpaceServiceFactory The service factory used to drop expired composition spaces
         * @param services The service look-up
         */
        CleanUpTask(Session session, CompositionSpaceServiceFactory compositionSpaceServiceFactory, ServiceLookup services) {
            super();
            this.session = session;
            this.compositionSpaceServiceFactory = compositionSpaceServiceFactory;
            this.services = services;
        }

        @Override
        public void run() {
            try {
                long maxIdleTimeMillis = getMaxIdleTimeMillis(session);
                if (maxIdleTimeMillis > 0) {
                    compositionSpaceServiceFactory.createServiceFor(session).closeExpiredCompositionSpaces(maxIdleTimeMillis);
                }
            } catch (Exception e) {
                LOG.error("Failed to clean-up expired composition spaces for user {} in context {}", I(session.getUserId()), I(session.getContextId()), e);
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
