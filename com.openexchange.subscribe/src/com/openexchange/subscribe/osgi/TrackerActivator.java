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

package com.openexchange.subscribe.osgi;

import com.openexchange.config.ConfigurationService;
import com.openexchange.contact.ContactService;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.RegistryServiceTrackerCustomizer;
import com.openexchange.secret.SecretService;
import com.openexchange.secret.osgi.tools.WhiteboardSecretService;

/**
 * {@link TrackerActivator} - The activator for starting/stopping needed service trackers.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TrackerActivator extends HousekeepingActivator {

    private WhiteboardSecretService secretService;

    /**
     * Initializes a new {@link TrackerActivator}.
     */
    public TrackerActivator() {
        super();
    }

    @Override
    public void startBundle() throws Exception {
        track(ConfigurationService.class, new RegistryServiceTrackerCustomizer<ConfigurationService>(context, SubscriptionServiceRegistry.getInstance(), ConfigurationService.class));
        track(ContactService.class, new RegistryServiceTrackerCustomizer<ContactService>(context, SubscriptionServiceRegistry.getInstance(), ContactService.class));
        track(SSLSocketFactoryProvider.class, new RegistryServiceTrackerCustomizer<SSLSocketFactoryProvider>(context, SubscriptionServiceRegistry.getInstance(), SSLSocketFactoryProvider.class));
        openTrackers();
        SubscriptionServiceRegistry.getInstance().addService(SecretService.class, secretService = new WhiteboardSecretService(context));
        secretService.open();
    }

    @Override
    public void stopBundle() throws Exception {
        if (secretService != null) {
            secretService.close();
        }
        super.stopBundle();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return EMPTY_CLASSES;
    }

}
