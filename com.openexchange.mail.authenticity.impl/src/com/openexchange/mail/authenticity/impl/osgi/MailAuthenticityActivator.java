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

package com.openexchange.mail.authenticity.impl.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.BundleContext;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ForcedReloadable;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.conversion.DataSource;
import com.openexchange.image.ImageActionFactory;
import com.openexchange.jslob.JSlobEntry;
import com.openexchange.mail.MailFetchListener;
import com.openexchange.mail.authenticity.MailAuthenticityHandler;
import com.openexchange.mail.authenticity.MailAuthenticityHandlerRegistry;
import com.openexchange.mail.authenticity.MailAuthenticityProperty;
import com.openexchange.mail.authenticity.impl.core.CustomRuleChecker;
import com.openexchange.mail.authenticity.impl.core.MailAuthenticityFetchListener;
import com.openexchange.mail.authenticity.impl.core.MailAuthenticityHandlerImpl;
import com.openexchange.mail.authenticity.impl.core.MailAuthenticityHandlerRegistryImpl;
import com.openexchange.mail.authenticity.impl.core.jslob.MailAuthenticityFeatureJSlobEntry;
import com.openexchange.mail.authenticity.impl.core.jslob.MailAuthenticityLevelJSlobEntry;
import com.openexchange.mail.authenticity.impl.core.metrics.MailAuthenticityMetricFileLogger;
import com.openexchange.mail.authenticity.impl.core.metrics.MailAuthenticityMetricLogger;
import com.openexchange.mail.authenticity.impl.trusted.TrustedMailService;
import com.openexchange.mail.authenticity.impl.trusted.internal.TrustedMailDataSource;
import com.openexchange.mail.authenticity.impl.trusted.internal.TrustedMailServiceImpl;
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.timer.TimerService;

/**
 * {@link MailAuthenticityActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class MailAuthenticityActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link MailAuthenticityActivator}.
     */
    public MailAuthenticityActivator() {
        super();
    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { LeanConfigurationService.class, ConfigurationService.class, UnifiedInboxManagement.class, ThreadPoolService.class, TimerService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Services.setServiceLookup(this);
        final BundleContext context = this.context;
        // It is OK to pass service references since 'stopOnServiceUnavailability' returns 'true'
        LeanConfigurationService leanConfigService = getService(LeanConfigurationService.class);

        MailAuthenticityHandlerRegistryImpl registry = new MailAuthenticityHandlerRegistryImpl(leanConfigService, context);
        registerService(MailAuthenticityHandlerRegistry.class, registry);

        registerService(MailAuthenticityMetricLogger.class, new MailAuthenticityMetricFileLogger(leanConfigService));
        trackService(MailAuthenticityMetricLogger.class);

        track(MailAuthenticityHandler.class, registry);
        openTrackers();

        TrustedMailServiceImpl trustedMailService = new TrustedMailServiceImpl(this);
        registerService(ForcedReloadable.class, trustedMailService);
        addService(TrustedMailService.class, trustedMailService);
        CustomRuleChecker ruleChecker = new CustomRuleChecker(leanConfigService);
        registerService(Reloadable.class, ruleChecker);
        MailAuthenticityHandlerImpl handlerImpl = new MailAuthenticityHandlerImpl(trustedMailService, this, ruleChecker);
        registerService(MailAuthenticityHandler.class, handlerImpl);
        registerService(Reloadable.class, new ConfigReloader(registry, handlerImpl));

        MailAuthenticityFetchListener fetchListener = new MailAuthenticityFetchListener(registry, getService(ThreadPoolService.class));
        registerService(MailFetchListener.class, fetchListener);

        registerService(JSlobEntry.class, new MailAuthenticityFeatureJSlobEntry(this));
        registerService(JSlobEntry.class, new MailAuthenticityLevelJSlobEntry());

        {
            // Register image data source
            TrustedMailDataSource trustedMailDataSource = TrustedMailDataSource.getInstance();
            Dictionary<String, Object> props = new Hashtable<String, Object>(1);
            props.put("identifier", trustedMailDataSource.getRegistrationName());
            registerService(DataSource.class, trustedMailDataSource, props);
            ImageActionFactory.addMapping(trustedMailDataSource.getRegistrationName(), trustedMailDataSource.getAlias());
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
        removeService(TrustedMailService.class);
        Services.setServiceLookup(null);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static class ConfigReloader implements Reloadable {

        private final MailAuthenticityHandlerRegistryImpl registry;
        private final MailAuthenticityHandlerImpl handlerImpl;

        /**
         * Initializes a new {@link MailAuthenticityActivator.ConfigReloader}.
         */
        ConfigReloader(MailAuthenticityHandlerRegistryImpl registry, MailAuthenticityHandlerImpl handlerImpl) {
            super();
            this.registry = registry;
            this.handlerImpl = handlerImpl;

        }

        @Override
        public void reloadConfiguration(ConfigurationService configService) {
            registry.invalidateCache();
            handlerImpl.invalidateAuthServIdsCache();
        }

        @Override
        public Interests getInterests() {
            return Reloadables.interestsForProperties(MailAuthenticityProperty.ENABLED.getFQPropertyName(), MailAuthenticityProperty.THRESHOLD.getFQPropertyName(), MailAuthenticityProperty.AUTHSERV_ID.getFQPropertyName());
        }
    }

}
