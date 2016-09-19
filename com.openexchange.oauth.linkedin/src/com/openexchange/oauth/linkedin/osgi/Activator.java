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

import java.util.Dictionary;
import java.util.Hashtable;
import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.linkedin.LinkedInOAuthScope;
import com.openexchange.oauth.linkedin.LinkedInService;
import com.openexchange.oauth.linkedin.LinkedInServiceImpl;
import com.openexchange.oauth.linkedin.OAuthServiceMetaDataLinkedInImpl;
import com.openexchange.oauth.scope.OAuthScopeRegistry;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

public class Activator extends HousekeepingActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Activator.class);
    private OAuthService oauthService = null;

    public Activator() {
        super();
    }

    @Override
    public void unregisterServices() {
        super.unregisterServices();
    }

    public OAuthService getOauthService() {
        if (oauthService != null) {
            return oauthService;
        }
        return getService(OAuthService.class);
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, OAuthService.class, ConfigViewFactory.class, CapabilityService.class, OAuthScopeRegistry.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final OAuthServiceMetaDataLinkedInImpl linkedInMetaDataService = new OAuthServiceMetaDataLinkedInImpl(this);
        registerService(OAuthServiceMetaData.class, linkedInMetaDataService, null);
        registerService(Reloadable.class, linkedInMetaDataService);
        LOG.info("OAuthServiceMetaData for LinkedIn was started");

        final LinkedInService linkedInService = new LinkedInServiceImpl(this);
        registerService(LinkedInService.class, linkedInService, null);
        LOG.info("LinkedInService was started.");

        final Dictionary<String, Object> properties = new Hashtable<String, Object>(1);
        properties.put(CapabilityChecker.PROPERTY_CAPABILITIES, "linkedin");
        registerService(CapabilityChecker.class, new CapabilityChecker() {

            @Override
            public boolean isEnabled(String capability, Session ses) throws OXException {
                if ("linkedin".equals(capability)) {
                    final ServerSession session = ServerSessionAdapter.valueOf(ses);
                    if (session.isAnonymous() || session.getUser().isGuest()) {
                        return false;
                    }

                    return linkedInMetaDataService.isEnabled(session.getUserId(), session.getContextId());
                }

                return true;
            }
        }, properties);
        getService(CapabilityService.class).declareCapability("linkedin");

        // Register the scope
        OAuthScopeRegistry scopeRegistry = getService(OAuthScopeRegistry.class);
        scopeRegistry.registerScope(linkedInMetaDataService.getAPI(), LinkedInOAuthScope.contacts_ro);
    }

    @Override
    protected void stopBundle() {
        closeTrackers();
        cleanUp();
    }

    public void setOauthService(OAuthService service) {
        this.oauthService = service;
    }

}
