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

package com.openexchange.admin.osgi;

import java.rmi.Remote;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.services.AdminServiceRegistry;

/**
 * {@link OXContextInterfaceTracker}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.8.0
 */
public class OXContextInterfaceTracker implements ServiceTrackerCustomizer<Remote, Remote> {

    private final BundleContext context;

    /**
     * Initializes a new {@link OXContextInterfaceTracker}.
     */
    public OXContextInterfaceTracker(BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public OXContextInterface addingService(ServiceReference<Remote> reference) {
        Remote remote = context.getService(reference);
        if (remote instanceof OXContextInterface) {
            OXContextInterface oxContextInterface = (OXContextInterface) remote;
            AdminServiceRegistry.getInstance().addService(OXContextInterface.class, oxContextInterface);
            return oxContextInterface;
        }

        // Discard
        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(ServiceReference<Remote> reference, Remote service) {
        // Ignore
    }

    @Override
    public void removedService(ServiceReference<Remote> reference, Remote service) {
        if (service instanceof OXContextInterface) {
            AdminServiceRegistry.getInstance().removeService(OXContextInterface.class);
        }
        context.ungetService(reference);
    }

}
