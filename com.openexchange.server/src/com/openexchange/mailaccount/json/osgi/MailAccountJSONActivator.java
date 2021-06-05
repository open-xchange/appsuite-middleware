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

package com.openexchange.mailaccount.json.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.ajax.requesthandler.Dispatcher;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.caching.events.CacheEventService;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.jslob.storage.registry.JSlobStorageRegistry;
import com.openexchange.mailaccount.Constants;
import com.openexchange.mailaccount.CredentialsProviderRegistry;
import com.openexchange.mailaccount.CredentialsProviderService;
import com.openexchange.mailaccount.internal.MailAccountOAuthAccountDeleteListener;
import com.openexchange.mailaccount.internal.MailAccountOAuthAccountReauthorizedListener;
import com.openexchange.mailaccount.internal.Pop3SessionCacheInvalidator;
import com.openexchange.mailaccount.json.MailAccountActionProvider;
import com.openexchange.mailaccount.json.MailAccountOAuthConstants;
import com.openexchange.mailaccount.json.actions.AbstractMailAccountAction;
import com.openexchange.mailaccount.json.factory.MailAccountActionFactory;
import com.openexchange.oauth.OAuthAccountDeleteListener;
import com.openexchange.oauth.OAuthAccountReauthorizedListener;
import com.openexchange.oauth.provider.resourceserver.scope.AbstractScopeProvider;
import com.openexchange.oauth.provider.resourceserver.scope.OAuthScopeProvider;

/**
 * {@link MailAccountJSONActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailAccountJSONActivator extends AJAXModuleActivator {

    /**
     * Initializes a new {@link MailAccountJSONActivator}.
     */
    public MailAccountJSONActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return EMPTY_CLASSES;
    }

    @Override
    protected void startBundle() throws Exception {
        final BundleContext context = this.context;

        MailAccountActionProviderTracker providerTracker = new MailAccountActionProviderTracker(context);
        track(MailAccountActionProvider.class, providerTracker);

        CredentialsProviderTracker credentialsProviderTracker = new CredentialsProviderTracker(context);
        track(CredentialsProviderService.class, credentialsProviderTracker);
        CredentialsProviderRegistry.getInstance().applyListing(credentialsProviderTracker);

        track(JSlobStorageRegistry.class, new ServiceTrackerCustomizer<JSlobStorageRegistry, JSlobStorageRegistry>() {

            @Override
            public JSlobStorageRegistry addingService(ServiceReference<JSlobStorageRegistry> reference) {
                final JSlobStorageRegistry storageRegistry = context.getService(reference);
                AbstractMailAccountAction.setJSlobStorageRegistry(storageRegistry);
                return storageRegistry;
            }

            @Override
            public void modifiedService(ServiceReference<JSlobStorageRegistry> reference, JSlobStorageRegistry service) {
                // nothing
            }

            @Override
            public void removedService(ServiceReference<JSlobStorageRegistry> reference, JSlobStorageRegistry service) {
                AbstractMailAccountAction.setJSlobStorageRegistry(null);
                context.ungetService(reference);
            }
        });
        trackService(Dispatcher.class);
        track(CacheEventService.class, new Pop3SessionCacheInvalidator(context));
        openTrackers();

        registerModule(new MailAccountActionFactory(providerTracker), Constants.getModule());

        registerService(OAuthAccountDeleteListener.class, new MailAccountOAuthAccountDeleteListener());
        registerService(OAuthAccountReauthorizedListener.class, new MailAccountOAuthAccountReauthorizedListener());

        registerService(OAuthScopeProvider.class, new AbstractScopeProvider(MailAccountOAuthConstants.OAUTH_READ_SCOPE, OAuthScopeDescription.READ_ONLY) {

            @Override
            public boolean canBeGranted(CapabilitySet capabilities) {
                return capabilities.contains(Permission.MULTIPLE_MAIL_ACCOUNTS.getCapabilityName());
            }
        });
        registerService(OAuthScopeProvider.class, new AbstractScopeProvider(MailAccountOAuthConstants.OAUTH_WRITE_SCOPE, OAuthScopeDescription.WRITABLE) {

            @Override
            public boolean canBeGranted(CapabilitySet capabilities) {
                return capabilities.contains(Permission.MULTIPLE_MAIL_ACCOUNTS.getCapabilityName());
            }
        });
    }

    @Override
    protected void stopBundle() throws Exception {
        CredentialsProviderRegistry.getInstance().applyListing(null);
        super.stopBundle();
    }

}
