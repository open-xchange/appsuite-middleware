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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.authentication.application.impl.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import com.openexchange.ajax.requesthandler.AJAXActionAnnotationProcessor;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.authentication.application.AppAuthenticatorService;
import com.openexchange.authentication.application.AppPasswordApplication;
import com.openexchange.authentication.application.AppPasswordMailOauthService;
import com.openexchange.authentication.application.AppPasswordService;
import com.openexchange.authentication.application.impl.AppPasswordAnnotationProcessor;
import com.openexchange.authentication.application.impl.AppPasswordProperty;
import com.openexchange.authentication.application.impl.AppPasswordServiceImpl;
import com.openexchange.authentication.application.impl.AppPasswordSessionStorageParameterNamesProvider;
import com.openexchange.authentication.application.impl.api.AppPasswordActionFactory;
import com.openexchange.authentication.application.impl.api.AppPasswordApplicationsConverter;
import com.openexchange.authentication.application.impl.api.ApplicationPasswordResultConverter;
import com.openexchange.authentication.application.impl.notification.AppPasswordNotifierRegistry;
import com.openexchange.authentication.application.notification.AppPasswordNotifier;
import com.openexchange.authentication.application.storage.AppPasswordStorage;
import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.capabilities.DependentCapabilityChecker;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.geolocation.GeoLocationService;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;
import com.openexchange.osgi.ServiceSet;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.sessionstorage.SessionStorageParameterNamesProvider;

/**
 * {@link ApplicationPasswordActivator}
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.4
 */
public class ApplicationPasswordActivator extends AJAXModuleActivator {

    /**
     * Initializes a new {@link ApplicationPasswordActivator}.
     */
    public ApplicationPasswordActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { CapabilityService.class, LeanConfigurationService.class, ConfigurationService.class };
    }

    @Override
    protected Class<?>[] getOptionalServices() {
        return new Class[] { GeoLocationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        AppPasswordServiceImpl appPasswordService = loadServices();
        // Only allow capability to be broadcast if all loaded
        String sCapability = "app_passwords";
        Dictionary<String, Object> properties = new Hashtable<String, Object>(1);
        properties.put(CapabilityChecker.PROPERTY_CAPABILITIES, sCapability);
        final ServiceLookup services = this;
        registerService(CapabilityChecker.class, new DependentCapabilityChecker() {
            @Override
            public boolean isEnabled(String capability, Session session, CapabilitySet capabilities) throws OXException {
                if (sCapability.equals(capability)) {
                    // Confirm loaded, user has the capability, and at least one storage registered
                    if (null != appPasswordService) {
                        List<AppPasswordApplication> applications = appPasswordService.getApplications(session);
                        if (null != applications && 0 < applications.size()) {
                            return null != services.getService(AppPasswordStorage.class);
                        }
                    }
                    return false;
                }
                return true;
            }
        }, properties);
        this.getService(CapabilityService.class).declareCapability(sCapability);
    }

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
    }

    /**
     * Loads services if the feature is enabled
     *
     * @return The service implementation if the feature is enabled, <code>null</code> otherwise
     * @throws OXException if an error is occurred
     */
    private AppPasswordServiceImpl loadServices() throws OXException {
        LeanConfigurationService leanConfigService = this.getServiceSafe(LeanConfigurationService.class);
        if (false == leanConfigService.getBooleanProperty(AppPasswordProperty.ENABLED)) {
            return null;
        }

        // Track storage services
        ServiceSet<AppPasswordStorage> storages = new ServiceSet<AppPasswordStorage>();
        track(AppPasswordStorage.class, storages);
        trackService(AppPasswordStorage.class);

        // Register session storage to track the restricted scopes
        registerService(SessionStorageParameterNamesProvider.class, new AppPasswordSessionStorageParameterNamesProvider());

        // Register notification registry
        RankingAwareNearRegistryServiceTracker<AppPasswordNotifier> notifierTracker = new RankingAwareNearRegistryServiceTracker<>(this.context, AppPasswordNotifier.class);
        rememberTracker(notifierTracker);
        AppPasswordNotifierRegistry notifierRegistry = new AppPasswordNotifierRegistry(notifierTracker);

        registerService(ResultConverter.class, new ApplicationPasswordResultConverter(this));
        registerService(ResultConverter.class, new AppPasswordApplicationsConverter());

        AppPasswordServiceImpl appPasswordService = new AppPasswordServiceImpl(this, notifierRegistry, storages);
        registerService(AppPasswordService.class, appPasswordService);
        registerService(AppAuthenticatorService.class, appPasswordService);
        registerService(Reloadable.class, appPasswordService);

        // Inspector
        registerService(AJAXActionAnnotationProcessor.class, new AppPasswordAnnotationProcessor());

        // Track plugin services
        trackService(AppPasswordMailOauthService.class);

        // Register API Module
        registerModule(new AppPasswordActionFactory(this), AppPasswordActionFactory.MODULE_PATH);

        openTrackers();
        return appPasswordService;
    }
}
