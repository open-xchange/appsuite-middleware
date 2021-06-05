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

package com.openexchange.authentication.kerberos.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.authentication.kerberos.impl.DelegationTicketLifecycle;
import com.openexchange.kerberos.KerberosService;
import com.openexchange.kerberos.KerberosUtils;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.timer.TimerService;

/**
 * {@link RenewalLoginHandlerRegisterer}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class RenewalLoginHandlerRegisterer implements ServiceTrackerCustomizer<Object, Object> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RenewalLoginHandlerRegisterer.class);

    private final BundleContext context;
    private KerberosService kerberosService;
    private TimerService timerService;
    private ServiceRegistration<?> registration;
    private DelegationTicketLifecycle starter;

    public RenewalLoginHandlerRegisterer(BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public synchronized Object addingService(ServiceReference<Object> reference) {
        final Object obj = context.getService(reference);
        final boolean needsRegistration;
        {
            if (obj instanceof KerberosService) {
                kerberosService = (KerberosService) obj;
            }
            if (obj instanceof TimerService) {
                timerService = (TimerService) obj;
            }
            needsRegistration = null != kerberosService && null != timerService && registration == null;
        }
        if (needsRegistration) {
            LOG.info("Registering delegation ticket renewal service.");
            final Dictionary<String, Object> properties = new Hashtable<String, Object>(1);
            properties.put(EventConstants.EVENT_TOPIC, new String[] { SessiondEventConstants.TOPIC_REMOVE_SESSION, SessiondEventConstants.TOPIC_REMOVE_CONTAINER, KerberosUtils.TOPIC_TICKET_READDED });
            starter = new DelegationTicketLifecycle(kerberosService, timerService);
            registration = context.registerService(new String[] { LoginHandlerService.class.getName() , EventHandler.class.getName() }, starter, properties);
        }
        return obj;
    }

    @Override
    public void modifiedService(ServiceReference<Object> reference, Object service) {
        // Nothing to do.
    }

    @Override
    public synchronized void removedService(ServiceReference<Object> reference, Object service) {
        ServiceRegistration<?> unregister = null;
        {
            if (service instanceof TimerService) {
                timerService = null;
            }
            if (service instanceof KerberosService) {
                kerberosService = null;
            }
            if (registration != null && (timerService == null || kerberosService == null)) {
                unregister = registration;
                registration = null;
            }
        }
        if (null != unregister) {
            LOG.info("Unregistering delegation ticket renewal service.");
            unregister.unregister();
            starter.stopAll();
        }
        context.ungetService(reference);
    }
}
