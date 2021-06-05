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

package com.openexchange.oauth.common.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.http.deferrer.DeferringURLService;
import com.openexchange.net.ssl.config.SSLConfigurationService;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.scope.OAuthScope;
import com.openexchange.oauth.scope.OAuthScopeRegistry;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link AbstractOAuthActivator}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public abstract class AbstractOAuthActivator extends HousekeepingActivator {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractOAuthActivator.class);

    //@formatter:off
    protected static final Class<?>[] NEEDED_SERVICES = { ConfigurationService.class, ConfigViewFactory.class, DeferringURLService.class,
        CapabilityService.class, DispatcherPrefixService.class, OAuthScopeRegistry.class, SSLConfigurationService.class };
    //@formatter:on

    /**
     * Initialises a new {@link AbstractOAuthActivator}.
     */
    public AbstractOAuthActivator() {
        super();
    }

    /**
     * Creates a new {@link OAuthServiceMetaData} instance for the current service provider
     *
     * @return The new {@link OAuthServiceMetaData}
     */
    protected abstract OAuthServiceMetaData getOAuthServiceMetaData();

    /**
     * Returns an array with all valid scopes for this provider
     *
     * @return an array with all valid scopes for this provider
     */
    protected abstract OAuthScope[] getScopes();

    @Override
    protected Class<?>[] getNeededServices() {
        return NEEDED_SERVICES;
    }
    
    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

    @Override
    protected void startBundle() throws Exception {
        OAuthServiceMetaData metadata = getOAuthServiceMetaData();
        if (metadata == null) {
            throw new IllegalStateException("The metadata cannot be 'null'. Please initialise the bundle properly");
        }
        OAuthScope[] scopes = getScopes();
        if (scopes == null) {
            throw new IllegalStateException("The scopes cannot be 'null'. Please initialise the bundle properly");
        }
        try {
            registerService(OAuthServiceMetaData.class, metadata);
            if (metadata instanceof Reloadable) {
                registerService(Reloadable.class, (Reloadable) metadata);
            }

            Dictionary<String, Object> properties = new Hashtable<String, Object>(2);
            properties.put(CapabilityChecker.PROPERTY_CAPABILITIES, metadata.getAPI().getCapability());
            registerService(CapabilityChecker.class, (capability, session) -> {
                if (false == metadata.getAPI().getCapability().equals(capability)) {
                    return true;
                }
                ServerSession syntheticSession = ServerSessionAdapter.valueOf(session);
                if (syntheticSession.isAnonymous() || syntheticSession.getUser().isGuest()) {
                    return false;
                }
                return metadata.isEnabled(session.getUserId(), session.getContextId());
            }, properties);

            getService(CapabilityService.class).declareCapability(metadata.getAPI().getCapability());

            // Register the scope
            OAuthScopeRegistry scopeRegistry = getService(OAuthScopeRegistry.class);
            scopeRegistry.registerScopes(metadata.getAPI(), scopes);

            LOG.info("Successfully initialized {} OAuth service", metadata.getAPI().getDisplayName());
        } catch (Exception e) {
            LOG.warn("Could not start-up {} OAuth service", metadata.getAPI().getDisplayName(), e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        OAuthServiceMetaData metaData = getOAuthServiceMetaData();
        OAuthScopeRegistry scopeRegistry = getService(OAuthScopeRegistry.class);
        if (scopeRegistry != null) {
            scopeRegistry.unregisterScopes(metaData.getAPI());
        }

        CapabilityService capabilityService = getService(CapabilityService.class);
        if (capabilityService != null) {
            capabilityService.undeclareCapability(metaData.getAPI().getCapability());
        }
        super.stopBundle();
    }
}
