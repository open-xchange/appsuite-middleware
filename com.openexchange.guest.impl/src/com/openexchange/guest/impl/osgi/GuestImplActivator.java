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

package com.openexchange.guest.impl.osgi;

import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.contact.storage.ContactUserStorage;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.delete.contextgroup.DeleteContextGroupListener;
import com.openexchange.guest.GuestService;
import com.openexchange.guest.impl.internal.DefaultGuestService;
import com.openexchange.guest.impl.internal.DelegateGuestService;
import com.openexchange.guest.impl.internal.GuestDeleteContextGroupListener;
import com.openexchange.guest.impl.internal.GuestDeleteListenerImpl;
import com.openexchange.guest.impl.internal.GuestStorageServiceLookup;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.password.mechanism.PasswordMechRegistry;
import com.openexchange.user.UserService;

/**
 * {@link GuestImplActivator}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class GuestImplActivator extends HousekeepingActivator {

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] {
            UserService.class, DatabaseService.class, ConfigViewFactory.class, ContactUserStorage.class, ConfigurationService.class, ContextService.class, PasswordMechRegistry.class
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void startBundle() throws Exception {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(GuestImplActivator.class);
        logger.info("Starting bundle: {}", this.context.getBundle().getSymbolicName());

        GuestStorageServiceLookup.set(this);

        GuestService guestService = new DefaultGuestService(getService(UserService.class), getService(ContextService.class), getService(ContactUserStorage.class), getService(ConfigViewFactory.class), getService(PasswordMechRegistry.class));
        GuestService delegateGuestService = new DelegateGuestService(guestService, getService(ConfigurationService.class));
        registerService(GuestService.class, delegateGuestService);

        registerService(DeleteListener.class, new GuestDeleteListenerImpl(delegateGuestService));
        registerService(DeleteContextGroupListener.class, new GuestDeleteContextGroupListener(delegateGuestService));
    }

    @Override
    protected void stopBundle() throws Exception {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(GuestImplActivator.class);
        logger.info("Stopping bundle: {}", this.context.getBundle().getSymbolicName());

        GuestStorageServiceLookup.set(null);
        super.stopBundle();
    }
}
