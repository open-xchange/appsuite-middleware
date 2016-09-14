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

package com.openexchange.xing.access.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.API;
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

    private volatile ServiceRegistration<XingOAuthAccessProvider> providerRegistration;

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
    public void registerProvider() {
        final XingOAuthAccessProvider provider = new XingOAuthAccessProvider() {

            @Override
            public XingOAuthAccess accessFor(final int oauthAccountId, final Session session) throws OXException {
                final OAuthService oAuthService = getService(OAuthService.class);
                final OAuthAccount oAuthAccount = oAuthService.getAccount(oauthAccountId, session, session.getUserId(), session.getContextId());

                OAuthAccessRegistryService registryService = Services.getService(OAuthAccessRegistryService.class);
                OAuthAccessRegistry registry = registryService.get(API.XING.getFullName());
                OAuthAccess oAuthAccess = registry.get(session);
                if (oAuthAccess == null) {
                    // Create
                    XingOAuthAccessImpl newInstance = new XingOAuthAccessImpl(session, oAuthAccount);
                    // Add to registry & return
                    oAuthAccess = registry.addIfAbsent(session.getContextId(), session.getUserId(), newInstance, new InitializeCallable(newInstance));
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
                    return oAuthService.getDefaultAccount(API.XING, session).getId();
                } catch (OXException e) {
                    if (OAuthExceptionCodes.ACCOUNT_NOT_FOUND.equals(e)) {
                        throw XingExceptionCodes.NO_OAUTH_ACCOUNT.create(e, session.getUserId(), session.getContextId());
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
    public void unregisterProvider() {
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
