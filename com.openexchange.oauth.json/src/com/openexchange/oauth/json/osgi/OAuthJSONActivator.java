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

package com.openexchange.oauth.json.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.cluster.lock.ClusterLockService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.oauth.OAuthAPIRegistry;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.access.OAuthAccessRegistryService;
import com.openexchange.oauth.association.OAuthAccountAssociationService;
import com.openexchange.oauth.http.OAuthHTTPClientFactory;
import com.openexchange.oauth.json.AbstractOAuthAJAXActionService;
import com.openexchange.oauth.json.Services;
import com.openexchange.oauth.json.oauthaccount.actions.AccountActionFactory;
import com.openexchange.oauth.json.oauthmeta.actions.MetaDataActionFactory;
import com.openexchange.oauth.json.proxy.OAuthProxyActionFactory;
import com.openexchange.oauth.scope.OAuthScopeRegistry;
import com.openexchange.secret.osgi.tools.WhiteboardSecretService;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link OAuthJSONActivator} - Activator for JSON OAuth interface.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class OAuthJSONActivator extends AJAXModuleActivator {

    private OSGiOAuthService oAuthService;
    private WhiteboardSecretService secretService;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, DispatcherPrefixService.class, OAuthService.class, OAuthScopeRegistry.class, OAuthHTTPClientFactory.class, CapabilityService.class, ClusterLockService.class,
            OAuthAccessRegistryService.class, OAuthAccountAssociationService.class, OAuthAPIRegistry.class };
    }

    @Override
    public synchronized void startBundle() throws Exception {
        try {
            Services.setServiceLookup(this);
            AbstractOAuthAJAXActionService.PREFIX.set(getService(DispatcherPrefixService.class));
            /*
             * Service registrations
             */
            registerModule(AccountActionFactory.getInstance(), "oauth/accounts");
            registerModule(MetaDataActionFactory.getInstance(), "oauth/services");
            registerModule(new OAuthProxyActionFactory(getService(OAuthService.class), getService(OAuthHTTPClientFactory.class)), "oauth/proxy");
            /*
             * Apply OAuth service to actions
             */
            final OSGiOAuthService oAuthService = new OSGiOAuthService().start(context);
            this.oAuthService = oAuthService;
            // registry.addService(OAuthService.class, oAuthService);
            AbstractOAuthAJAXActionService.setOAuthService(oAuthService);
            AbstractOAuthAJAXActionService.setOAuthAccountAssociationService(getService(OAuthAccountAssociationService.class));
            final WhiteboardSecretService secretService = new WhiteboardSecretService(context);
            this.secretService = secretService;
            secretService.open();
            AbstractOAuthAJAXActionService.setSecretService(secretService);
            /*
             * declare "oauth" capability & appropriate checker
             */
            getService(CapabilityService.class).declareCapability("oauth");
            Dictionary<String, Object> properties = new Hashtable<String, Object>(2);
            properties.put(CapabilityChecker.PROPERTY_CAPABILITIES, "oauth");
            registerService(CapabilityChecker.class, new CapabilityChecker() {

                @Override
                public boolean isEnabled(String capability, Session session) throws OXException {
                    if ("oauth".equals(capability)) {
                        ServerSession serverSession = ServerSessionAdapter.valueOf(session);
                        if (serverSession.isAnonymous() || serverSession.getUser().isGuest()) {
                            return false;
                        }
                    }
                    return true;
                }
            }, properties);

            trackService(HostnameService.class);

            openTrackers();
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(OAuthJSONActivator.class).error("", e);
            throw e;
        }
    }

    @Override
    public synchronized void stopBundle() throws Exception {
        try {
            super.stopBundle();
            final WhiteboardSecretService secretService = this.secretService;
            if (secretService != null) {
                secretService.close();
                this.secretService = null;
            }
            final OSGiOAuthService oAuthService = this.oAuthService;
            if (null != oAuthService) {
                oAuthService.stop();
                this.oAuthService = null;
            }
            AbstractOAuthAJAXActionService.setOAuthService(null);
            AbstractOAuthAJAXActionService.setOAuthAccountAssociationService(null);
            AbstractOAuthAJAXActionService.PREFIX.set(null);
            Services.setServiceLookup(null);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(OAuthJSONActivator.class).error("", e);
            throw e;
        }
    }

}
