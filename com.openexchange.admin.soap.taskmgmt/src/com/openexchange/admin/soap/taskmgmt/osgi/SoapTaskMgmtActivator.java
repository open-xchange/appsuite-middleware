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

package com.openexchange.admin.soap.taskmgmt.osgi;

import java.rmi.Remote;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.admin.rmi.OXTaskMgmtInterface;
import com.openexchange.admin.soap.taskmgmt.soap.OXTaskMgmtServicePortType;
import com.openexchange.admin.soap.taskmgmt.soap.OXTaskMgmtServicePortTypeImpl;
import com.openexchange.osgi.HousekeepingActivator;


/**
 * {@link SoapTaskMgmtActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SoapTaskMgmtActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link SoapTaskMgmtActivator}.
     */
    public SoapTaskMgmtActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] {};
    }

    @Override
    protected void startBundle() throws Exception {
        final BundleContext context = this.context;
        final ServiceTrackerCustomizer<Remote, Remote> trackerCustomizer = new ServiceTrackerCustomizer<Remote, Remote>() {

            @Override
            public void removedService(final ServiceReference<Remote> reference, final Remote service) {
                if (null != service) {
                    OXTaskMgmtServicePortTypeImpl.RMI_REFERENCE.set(null);
                    context.ungetService(reference);
                }
            }

            @Override
            public void modifiedService(final ServiceReference<Remote> reference, final Remote service) {
                // Ignore
            }

            @Override
            public Remote addingService(final ServiceReference<Remote> reference) {
                final Remote service = context.getService(reference);
                if (service instanceof OXTaskMgmtInterface) {
                    OXTaskMgmtServicePortTypeImpl.RMI_REFERENCE.set((OXTaskMgmtInterface) service);
                    return service;
                }
                context.ungetService(reference);
                return null;
            }
        };
        track(Remote.class, trackerCustomizer);
        openTrackers();

        final OXTaskMgmtServicePortTypeImpl soapService = new OXTaskMgmtServicePortTypeImpl();
        registerService(OXTaskMgmtServicePortType.class, soapService);
    }

}
