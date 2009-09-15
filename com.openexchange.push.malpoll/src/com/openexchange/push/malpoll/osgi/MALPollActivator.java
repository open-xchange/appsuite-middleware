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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventAdmin;
import com.openexchange.mail.service.MailService;
import com.openexchange.push.PushException;
import com.openexchange.push.PushManagerService;
import com.openexchange.push.malpoll.MALPollPushListener;
import com.openexchange.push.malpoll.MALPollPushListenerRegistry;
import com.openexchange.push.malpoll.MALPollPushManagerService;
import com.openexchange.server.osgiservice.DeferredActivator;
import com.openexchange.server.osgiservice.ServiceRegistry;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link MALPollActivator} - The MAL poll activator.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MALPollActivator extends DeferredActivator {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(MALPollActivator.class);

    private ServiceRegistration serviceRegistration;

    private ScheduledTimerTask scheduledTimerTask;

    /**
     * Initializes a new {@link MALPollActivator}.
     */
    public MALPollActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { MailService.class, EventAdmin.class, TimerService.class };
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Re-available service: " + clazz.getName());
        }
        getServiceRegistry().addService(clazz, getService(clazz));
        if (TimerService.class == clazz) {
            startScheduledTask(getService(TimerService.class));
        }
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        if (LOG.isWarnEnabled()) {
            LOG.warn("Absent service: " + clazz.getName());
        }
        if (TimerService.class == clazz) {
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
                final ServiceRegistry registry = getServiceRegistry();
                registry.clearRegistry();
                final Class<?>[] classes = getNeededServices();
                for (int i = 0; i < classes.length; i++) {
                    final Object service = getService(classes[i]);
                    if (null != service) {
                        registry.addService(classes[i], service);
                    }
                }
            }
            startScheduledTask(getService(TimerService.class));
            serviceRegistration = context.registerService(PushManagerService.class.getName(), new MALPollPushManagerService(), null);
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        try {
            if (null != serviceRegistration) {
                serviceRegistration.unregister();
                serviceRegistration = null;
            }
            stopScheduledTask(getService(TimerService.class));
            /*
             * Clear service registry
             */
            getServiceRegistry().clearRegistry();
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }
    }

    private void startScheduledTask(final TimerService timerService) {
        final Runnable r = new Runnable() {

            private final org.apache.commons.logging.Log log = LOG;

            public void run() {
                for (final Iterator<MALPollPushListener> pushListeners = MALPollPushListenerRegistry.getInstance().getPushListeners(); pushListeners.hasNext();) {
                    final MALPollPushListener l = pushListeners.next();
                    try {
                        l.checkNewMail();
                    } catch (final PushException e) {
                        log.error(e.getMessage(), e);
                    }
                }
                if (log.isDebugEnabled()) {
                    log.debug("Run for checking new mails done.");
                }
            }
        };
        // By now every 5 minutes -> TODO: Configurable
        scheduledTimerTask = timerService.scheduleWithFixedDelay(r, 1000, 300000);
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

}
