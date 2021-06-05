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

package com.openexchange.mail.oauth.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.mail.oauth.MailOAuthProvider;
import com.openexchange.mail.oauth.internal.MailOAuthProviderRegistry;


/**
 * {@link MailOAuthProviderTracker}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class MailOAuthProviderTracker implements ServiceTrackerCustomizer<MailOAuthProvider, MailOAuthProvider> {

    private final BundleContext context;
    private final MailOAuthProviderRegistry registry;

    /**
     * Initializes a new {@link MailOAuthProviderTracker}.
     */
    public MailOAuthProviderTracker(MailOAuthProviderRegistry registry, BundleContext context) {
        super();
        this.registry = registry;
        this.context = context;
    }

    @Override
    public MailOAuthProvider addingService(ServiceReference<MailOAuthProvider> reference) {
        MailOAuthProvider provider = context.getService(reference);
        if (registry.addProvider(provider)) {
            return provider;
        }

        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(ServiceReference<MailOAuthProvider> reference, MailOAuthProvider service) {
        // Ignore
    }

    @Override
    public void removedService(ServiceReference<MailOAuthProvider> reference, MailOAuthProvider service) {
        registry.removeProvider(service);
        context.ungetService(reference);
    }

}
