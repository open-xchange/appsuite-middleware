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
package com.openexchange.oidc.osgi;

import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.hazelcast.serialization.CustomPortableFactory;
import com.openexchange.oidc.OIDCBackend;
import com.openexchange.oidc.hz.PortableAuthenticationRequestFactory;
import com.openexchange.oidc.hz.PortableLogoutRequestFactory;
import com.openexchange.oidc.impl.OIDCConfigImpl;
import com.openexchange.oidc.impl.OIDCSessionInspectorService;
import com.openexchange.oidc.impl.OIDCSessionParameterNamesProvider;
import com.openexchange.oidc.spi.OIDCCoreBackend;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.inspector.SessionInspectorService;
import com.openexchange.session.reservation.SessionReservationService;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessionstorage.SessionStorageParameterNamesProvider;
import com.openexchange.sessionstorage.SessionStorageService;
import com.openexchange.user.UserService;

/**
 * Activates the OpenID feature.
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.10.0
 */
public class OIDCActivator extends HousekeepingActivator{

    private OIDCBackendRegistry oidcBackendRegistry;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[]
        {
            LeanConfigurationService.class,
            ConfigurationService.class,
            HttpService.class,
            DispatcherPrefixService.class,
            HazelcastInstance.class,
            SessionReservationService.class,
            ContextService.class,
            UserService.class,
            SessiondService.class
        };
    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

    private void getOIDCBackends(ServiceLookup services) {
        if (this.oidcBackendRegistry == null) {
            this.oidcBackendRegistry = new OIDCBackendRegistry(context, services);
            this.oidcBackendRegistry.open();
        }
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        Services.setServices(this);
        trackService(SessionStorageService.class);
        openTrackers();

        OIDCConfigImpl config = new OIDCConfigImpl(this);

        Logger logger = org.slf4j.LoggerFactory.getLogger(OIDCActivator.class);
        if (config.isEnabled().booleanValue()) {
            logger.info("Starting core OpenID Connect support... ");
            getOIDCBackends(this);
            registerService(SessionInspectorService.class, new OIDCSessionInspectorService(oidcBackendRegistry), null);
            registerService(CustomPortableFactory.class, new PortableAuthenticationRequestFactory(), null);
            registerService(CustomPortableFactory.class, new PortableLogoutRequestFactory(), null);
        } else {
            logger.info("OpenID Connect support is disabled by configuration. Skipping initialization...");
        }

        //register default oidc backend if configured
        if (config.startDefaultBackend().booleanValue()) {
            context.registerService(OIDCBackend.class, new OIDCCoreBackend() , null);
        }
        context.registerService(SessionStorageParameterNamesProvider.class, new OIDCSessionParameterNamesProvider(), null);
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        OIDCBackendRegistry oidcBackends = this.oidcBackendRegistry;
        if (null != oidcBackends) {
            this.oidcBackendRegistry = null;
            oidcBackends.close();
        }
        Services.setServices(null);
        super.stopBundle();
    }
}
