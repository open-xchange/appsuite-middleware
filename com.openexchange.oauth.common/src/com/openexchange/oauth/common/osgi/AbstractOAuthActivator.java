/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2018-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.osgi.DeferredActivator#getNeededServices()
     */
    @Override
    protected Class<?>[] getNeededServices() {
        return NEEDED_SERVICES;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.osgi.DeferredActivator#startBundle()
     */
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

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.osgi.HousekeepingActivator#stopBundle()
     */
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
