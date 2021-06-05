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

package com.openexchange.oauth.provider.json.osgi;

import org.osgi.framework.ServiceReference;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.oauth.provider.authorizationserver.grant.GrantManagement;
import com.openexchange.oauth.provider.json.OAuthProviderActionFactory;
import com.openexchange.oauth.provider.resourceserver.OAuthResourceService;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;


/**
 * {@link OAuthProviderJSONActivator}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class OAuthProviderJSONActivator extends AJAXModuleActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { TranslatorFactory.class, ManagedFileManagement.class, CapabilityService.class };
    }

    @Override
    protected Class<?>[] getOptionalServices() {
        return new Class<?>[] { OAuthResourceService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final ServiceLookup services = this;
        track(GrantManagement.class, new SimpleRegistryListener<GrantManagement>() {
            @Override
            public void added(ServiceReference<GrantManagement> ref, GrantManagement service) {
                addService(GrantManagement.class, service);
                getService(CapabilityService.class).declareCapability("oauth-grants");
                registerService(CapabilityChecker.class, new CapabilityChecker() {

                    @Override
                    public boolean isEnabled(String capability, Session session) throws OXException {
                        if ("oauth-grants".equals(capability)) {
                            ServerSession serverSession = ServerSessionAdapter.valueOf(session);
                            if (serverSession.isAnonymous() || serverSession.getUser().isGuest()) {
                                return false;
                            }
                            OAuthResourceService oAuthResourceService = getService(OAuthResourceService.class);
                            if (oAuthResourceService == null) {
                                return false;
                            }
                            return oAuthResourceService.isProviderEnabled(session.getContextId(), session.getUserId());
                        }
                        return true;
                    }
                });
                registerModule(new OAuthProviderActionFactory(services), "oauth/grants");
            }

            @Override
            public void removed(ServiceReference<GrantManagement> ref, GrantManagement service) {
                getService(CapabilityService.class).undeclareCapability("oauth-grants");
            }
        });
        openTrackers();
    }

    @Override
    public <S> boolean addService(Class<S> clazz, S service) {
        return super.addService(clazz, service);
    }

    @Override
    public <S> void registerService(Class<S> clazz, S service) {
        super.registerService(clazz, service);
    }

}
