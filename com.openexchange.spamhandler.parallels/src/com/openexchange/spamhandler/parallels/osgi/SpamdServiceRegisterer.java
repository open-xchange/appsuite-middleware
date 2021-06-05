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

package com.openexchange.spamhandler.parallels.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.spamhandler.parallels.ParallelsSpamdService;
import com.openexchange.spamhandler.spamassassin.api.SpamdService;
import com.openexchange.user.UserService;

/**
 * Registers the SpamdService implementation as an OSGi service.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SpamdServiceRegisterer implements ServiceTrackerCustomizer<Object, Object> {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SpamdServiceRegisterer.class);

    private final BundleContext context;
    private ServiceRegistration<SpamdService> registration;
    private ConfigViewFactory factory;
    private ContextService contextService;
    private UserService userService;

    /**
     * Initializes a new {@link SpamdServiceRegisterer}.
     *
     * @param context The bundle context
     */
    public SpamdServiceRegisterer(BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public synchronized Object addingService(ServiceReference<Object> reference) {
        final Object obj = context.getService(reference);
        final boolean needsRegistration;
        {
            if (obj instanceof ConfigViewFactory) {
                factory = (ConfigViewFactory) obj;
            }
            if (obj instanceof ContextService) {
                contextService = (ContextService) obj;
            }
            if (obj instanceof UserService) {
                userService = (UserService) obj;
            }
            needsRegistration = null != factory && null != contextService && null != userService && null == registration;
        }
        if (needsRegistration) {
            LOG.info("Registering Parallels spam handler service.");
            registration = context.registerService(SpamdService.class, new ParallelsSpamdService(factory, userService), null);
        }
        return null;
    }

    @Override
    public void modifiedService(ServiceReference<Object> reference, Object service) {
        // Nothing to do.
    }

    @Override
    public synchronized void removedService(ServiceReference<Object> reference, Object service) {
        ServiceRegistration<?> unregister = null;
        {
            if (service instanceof ConfigViewFactory) {
                factory = null;
            }
            if (service instanceof ContextService) {
                contextService = null;
            }
            if (service instanceof UserService) {
                userService = null;
            }
            if (null != registration && (null == contextService || null == userService || null == factory)) {
                unregister = registration;
                registration = null;
            }
        }
        if (null != unregister) {
            LOG.info("Unregistering Parallels spam handler service.");
            unregister.unregister();
        }
        context.ungetService(reference);
    }
}
