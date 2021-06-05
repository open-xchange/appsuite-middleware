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

package com.openexchange.halo.osgi;

import org.osgi.framework.ServiceReference;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.contact.provider.composition.IDBasedContactsAccessFactory;
import com.openexchange.halo.ContactHalo;
import com.openexchange.halo.HaloContactDataSource;
import com.openexchange.halo.HaloContactImageSource;
import com.openexchange.halo.contacts.ContactDataSource;
import com.openexchange.halo.internal.ContactHaloImpl;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.session.SessionSpecificContainerRetrievalService;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserPermissionService;

public class HaloActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { UserService.class, UserPermissionService.class,
            SessionSpecificContainerRetrievalService.class, ConfigViewFactory.class, IDBasedContactsAccessFactory.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final ContactHaloImpl halo = new ContactHaloImpl(this);

        ContactDataSource cds = new ContactDataSource(this);
        halo.addContactDataSource(cds);
        halo.addContactImageSource(cds);

        registerService(ContactHalo.class, halo);

        track(HaloContactDataSource.class, new SimpleRegistryListener<HaloContactDataSource>() {

            @Override
            public void added(ServiceReference<HaloContactDataSource> ref, HaloContactDataSource service) {
                halo.addContactDataSource(service);
            }

            @Override
            public void removed(ServiceReference<HaloContactDataSource> ref, HaloContactDataSource service) {
                halo.removeContactDataSource(service);
            }

        });

        track(HaloContactImageSource.class, new SimpleRegistryListener<HaloContactImageSource>() {

            @Override
            public void added(ServiceReference<HaloContactImageSource> ref, HaloContactImageSource service) {
                halo.addContactImageSource(service);
            }

            @Override
            public void removed(ServiceReference<HaloContactImageSource> ref, HaloContactImageSource service) {
                halo.addContactImageSource(service);
            }
        });

        openTrackers();
    }

}
