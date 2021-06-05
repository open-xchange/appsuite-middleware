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

package com.openexchange.groupware.reminder.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.groupware.reminder.TargetService;
import com.openexchange.groupware.reminder.internal.TargetRegistry;
import com.openexchange.java.Autoboxing;

/**
 * {@link TargetRegistryCustomizer}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class TargetRegistryCustomizer implements ServiceTrackerCustomizer<TargetService, TargetService> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TargetRegistryCustomizer.class);

    private final BundleContext context;

    public TargetRegistryCustomizer(final BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public TargetService addingService(final ServiceReference<TargetService> reference) {
        final TargetService targetService = context.getService(reference);
        final int module = parseModule(reference);
        if (-1 == module) {
            LOG.error("Registration of service {} is missing property defining the module.", targetService.getClass().getName());
            context.ungetService(reference);
            return null;
        }
        TargetRegistry.getInstance().addService(module, targetService);
        return targetService;
    }

    @Override
    public void modifiedService(final ServiceReference<TargetService> reference, final TargetService service) {
        // Nothing to do.
    }

    @Override
    public void removedService(final ServiceReference<TargetService> reference, final TargetService service) {
        if (null == service) {
            return;
        }
        TargetRegistry.getInstance().removeService(parseModule(reference));
        context.ungetService(reference);
    }

    private int parseModule(final ServiceReference<TargetService> reference) {
        final Object obj = reference.getProperty(TargetService.MODULE_PROPERTY);
        final int retval;
        if (obj instanceof Integer) {
            retval = Autoboxing.i((Integer) obj);
        } else {
            retval = -1;
        }
        return retval;
    }
}
