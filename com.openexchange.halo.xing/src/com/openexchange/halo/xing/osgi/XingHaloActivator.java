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

package com.openexchange.halo.xing.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.halo.HaloContactDataSource;
import com.openexchange.halo.xing.XingInvestigationResultConverter;
import com.openexchange.halo.xing.XingUserDataSource;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.xing.access.XingOAuthAccessProvider;

/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class XingHaloActivator extends HousekeepingActivator {

    ServiceRegistration<HaloContactDataSource> contactRegistration = null;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] {};
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        registerService(ResultConverter.class, new XingInvestigationResultConverter());
        final BundleContext xingBungleContext = context;
        track(XingOAuthAccessProvider.class, new SimpleRegistryListener<XingOAuthAccessProvider>() {

            @Override
            public void added(ServiceReference<XingOAuthAccessProvider> ref, XingOAuthAccessProvider service) {
                XingUserDataSource xingDataSource = new XingUserDataSource(service);
                contactRegistration = xingBungleContext.registerService(HaloContactDataSource.class, xingDataSource, null);
            }

            @Override
            public void removed(ServiceReference<XingOAuthAccessProvider> ref, XingOAuthAccessProvider service) {
                unregisterContact();
            }

        });
        openTrackers();
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        unregisterContact();
        super.stopBundle();
    }

    protected void unregisterContact() {
        if (contactRegistration != null) {
            contactRegistration.unregister();
            contactRegistration = null;
        }
    }

}
