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

package com.openexchange.client.onboarding.osgi;

import java.rmi.Remote;
import java.util.Dictionary;
import java.util.Hashtable;
import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.client.onboarding.OnboardingUtility;
import com.openexchange.client.onboarding.download.DownloadLinkProvider;
import com.openexchange.client.onboarding.internal.OnboardingConfig;
import com.openexchange.client.onboarding.internal.OnboardingServiceImpl;
import com.openexchange.client.onboarding.rmi.RemoteOnboardingService;
import com.openexchange.client.onboarding.rmi.impl.RemoteOnboardingServiceImpl;
import com.openexchange.client.onboarding.service.OnboardingService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.mime.MimeTypeMap;
import com.openexchange.notification.mail.NotificationMailFactory;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.serverconfig.ServerConfigService;
import com.openexchange.session.Session;
import com.openexchange.sms.SMSServiceSPI;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.uadetector.UserAgentParser;
import com.openexchange.user.UserService;

/**
 * {@link OnboardingActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class OnboardingActivator extends HousekeepingActivator {

    private volatile OnboardingServiceImpl registry;

    /**
     * Initializes a new {@link OnboardingActivator}.
     */
    public OnboardingActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { UserService.class, ConfigViewFactory.class, ConfigurationService.class, MimeTypeMap.class, UserAgentParser.class, ContextService.class,
 TranslatorFactory.class, ServerConfigService.class, CapabilityService.class, NotificationMailFactory.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Services.setServiceLookup(this);

        // Create service instance
        OnboardingServiceImpl serviceImpl = new OnboardingServiceImpl(this);
        this.registry = serviceImpl;
        serviceImpl.setConfiguredScenarios(OnboardingConfig.parseScenarios(getService(ConfigurationService.class)));
        addService(OnboardingService.class, serviceImpl);

        // Track services needed for SMS transport
        trackService(SMSServiceSPI.class);
        trackService(DownloadLinkProvider.class);

        // Initialize & open provider tracker
        OnboardingProviderTracker providerTracker = new OnboardingProviderTracker(context, serviceImpl);
        rememberTracker(providerTracker);
        openTrackers();

        // Capability stuff
        {
            final String sCapability = "client-onboarding";
            Dictionary<String, Object> properties = new Hashtable<String, Object>(1);
            properties.put(CapabilityChecker.PROPERTY_CAPABILITIES, sCapability);
            registerService(CapabilityChecker.class, new CapabilityChecker() {
                @Override
                public boolean isEnabled(String capability, Session ses) throws OXException {
                    if (sCapability.equals(capability)) {
                        ServerSession session = ServerSessionAdapter.valueOf(ses);
                        if (session.isAnonymous() || session.getUser().isGuest()) {
                            return false;
                        }

                        Boolean bool = OnboardingUtility.getBoolFromProperty("com.openexchange.client.onboarding.enabled", Boolean.TRUE, session);
                        return bool.booleanValue();
                    }

                    return true;
                }
            }, properties);

            getService(CapabilityService.class).declareCapability(sCapability);
        }

        // Register services
        registerService(OnboardingService.class, serviceImpl);
        registerService(Reloadable.class, new OnboardingReloadable(serviceImpl));

        // Register appropriate RMI stub
        {
            Dictionary<String, Object> props = new Hashtable<String, Object>(2);
            props.put("RMIName", RemoteOnboardingService.RMI_NAME);
            registerService(Remote.class, new RemoteOnboardingServiceImpl(serviceImpl), props);
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
        OnboardingServiceImpl registry = this.registry;
        if (null != registry) {
            this.registry = null;
            removeService(OnboardingService.class);
        }

        Services.setServiceLookup(null);
    }

    @Override
    public <S> boolean addService(Class<S> clazz, S service) {
        return super.addService(clazz, service);
    }

    @Override
    public <S> boolean removeService(Class<? extends S> clazz) {
        return super.removeService(clazz);
    }

}
