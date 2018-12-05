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
 *     Copyright (C) 2016-2020 OX Software GmbH
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
