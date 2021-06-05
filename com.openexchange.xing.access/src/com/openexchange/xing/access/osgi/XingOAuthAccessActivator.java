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

package com.openexchange.xing.access.osgi;

import static com.openexchange.java.Autoboxing.I;
import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.KnownApi;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.access.InitializeCallable;
import com.openexchange.oauth.access.OAuthAccess;
import com.openexchange.oauth.access.OAuthAccessRegistry;
import com.openexchange.oauth.access.OAuthAccessRegistryService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.xing.access.XingExceptionCodes;
import com.openexchange.xing.access.XingOAuthAccess;
import com.openexchange.xing.access.XingOAuthAccessProvider;
import com.openexchange.xing.access.internal.Services;
import com.openexchange.xing.access.internal.XingEventHandler;
import com.openexchange.xing.access.internal.XingOAuthAccessImpl;

/**
 * {@link XingOAuthAccessActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class XingOAuthAccessActivator extends HousekeepingActivator {

    private ServiceRegistration<XingOAuthAccessProvider> providerRegistration;

    /**
     * Initializes a new {@link XingOAuthAccessActivator}.
     */
    public XingOAuthAccessActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { SessiondService.class, OAuthService.class, OAuthAccessRegistryService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Services.setServices(this);

        // Event handler
        final Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
        serviceProperties.put(EventConstants.EVENT_TOPIC, SessiondEventConstants.TOPIC_LAST_SESSION);
        registerService(EventHandler.class, new XingEventHandler(), serviceProperties);

        // Registerer
        track(OAuthServiceMetaData.class, new OAuthServiceMetaDataRegisterer(context, this));
        openTrackers();
    }

    @Override
    protected void stopBundle() throws Exception {
        Services.setServices(null);
        unregisterProvider();
        super.stopBundle();
    }

    /**
     * Registers the provider.
     */
    public synchronized void registerProvider() {
        final XingOAuthAccessProvider provider = new XingOAuthAccessProvider() {

            @Override
            public XingOAuthAccess accessFor(final int oauthAccountId, final Session session) throws OXException {
                final OAuthService oAuthService = getService(OAuthService.class);
                final OAuthAccount oAuthAccount = oAuthService.getAccount(session, oauthAccountId);

                OAuthAccessRegistryService registryService = Services.getService(OAuthAccessRegistryService.class);
                OAuthAccessRegistry registry = registryService.get(KnownApi.XING.getServiceId());
                OAuthAccess oAuthAccess = registry.get(session.getContextId(), session.getUserId(), oAuthAccount.getId());
                if (oAuthAccess == null) {
                    // Create
                    XingOAuthAccessImpl newInstance = new XingOAuthAccessImpl(session, oAuthAccount);
                    // Add to registry & return
                    oAuthAccess = registry.addIfAbsent(session.getContextId(), session.getUserId(), oAuthAccount.getId(), newInstance, new InitializeCallable(newInstance));
                    if (null == oAuthAccess) {
                        oAuthAccess = newInstance;
                    }
                }
                return (XingOAuthAccessImpl) oAuthAccess;
            }

            @Override
            public int getXingOAuthAccount(Session session) throws OXException {
                try {
                    final OAuthService oAuthService = getService(OAuthService.class);
                    return oAuthService.getDefaultAccount(KnownApi.XING, session).getId();
                } catch (OXException e) {
                    if (OAuthExceptionCodes.ACCOUNT_NOT_FOUND.equals(e)) {
                        throw XingExceptionCodes.NO_OAUTH_ACCOUNT.create(e, I(session.getUserId()), I(session.getContextId()));
                    }
                    throw e;
                }
            }

            @Override
            public XingOAuthAccess accessFor(String token, String secret, Session session) throws OXException {
                return new XingOAuthAccessImpl(session, token, secret);
            }
        };
        providerRegistration = context.registerService(XingOAuthAccessProvider.class, provider, null);
    }

    /**
     * Unregisters the provider.
     */
    public synchronized void unregisterProvider() {
        final ServiceRegistration<XingOAuthAccessProvider> providerRegistration = this.providerRegistration;
        if (null != providerRegistration) {
            providerRegistration.unregister();
            this.providerRegistration = null;
        }
    }

    /**
     * Sets given service.
     *
     * @param authServiceMetaData The service to set or <code>null</code> to remove
     */
    public void setOAuthServiceMetaData(final OAuthServiceMetaData oAuthServiceMetaData) {
        if (null == oAuthServiceMetaData) {
            removeService(OAuthServiceMetaData.class);
        } else {
            addService(OAuthServiceMetaData.class, oAuthServiceMetaData);
        }
    }

}
