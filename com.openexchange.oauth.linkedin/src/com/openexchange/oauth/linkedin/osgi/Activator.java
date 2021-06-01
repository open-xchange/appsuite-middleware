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

package com.openexchange.oauth.linkedin.osgi;

import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.http.deferrer.DeferringURLService;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.common.osgi.AbstractOAuthActivator;
import com.openexchange.oauth.linkedin.LinkedInOAuthScope;
import com.openexchange.oauth.linkedin.LinkedInOAuthServiceMetaData;
import com.openexchange.oauth.linkedin.LinkedInService;
import com.openexchange.oauth.linkedin.LinkedInServiceImpl;
import com.openexchange.oauth.scope.OAuthScope;
import com.openexchange.oauth.scope.OAuthScopeRegistry;
import com.openexchange.rest.client.endpointpool.EndpointManagerFactory;

/**
 * Activator for LinkedIn OAuth bundle.
 */
public class Activator extends AbstractOAuthActivator {

    private LinkedInServiceImpl linkedInService;

    /**
     * Initializes a new {@link Activator}.
     */
    public Activator() {
        super();
    }

    @Override
    public void unregisterServices() {
        super.unregisterServices();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, DeferringURLService.class, DispatcherPrefixService.class, OAuthService.class, ConfigViewFactory.class, CapabilityService.class, OAuthScopeRegistry.class, EndpointManagerFactory.class };
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Activator.class);
        LinkedInServiceImpl linkedInService = new LinkedInServiceImpl(this);
        this.linkedInService = linkedInService;
        registerService(LinkedInService.class, linkedInService, null);
        logger.info("LinkedInService was started.");
        super.startBundle();
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        LinkedInServiceImpl linkedInService = this.linkedInService;
        if (null != linkedInService) {
            this.linkedInService = null;
            linkedInService.shutDown();
        }

        super.stopBundle();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.common.osgi.AbstractOAuthActivator#getOAuthServiceMetaData()
     */
    @Override
    protected OAuthServiceMetaData getOAuthServiceMetaData() {
        return new LinkedInOAuthServiceMetaData(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.common.osgi.AbstractOAuthActivator#getScopes()
     */
    @Override
    protected OAuthScope[] getScopes() {
        return LinkedInOAuthScope.values();
    }

}
