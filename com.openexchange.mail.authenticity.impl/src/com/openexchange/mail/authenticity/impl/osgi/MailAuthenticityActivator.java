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
import com.openexchange.mail.authenticity.impl.TempDisableFail;
import com.openexchange.mail.authenticity.impl.core.CustomRuleChecker;
import com.openexchange.mail.authenticity.impl.core.MailAuthenticityFetchListener;
import com.openexchange.mail.authenticity.impl.core.MailAuthenticityHandlerImpl;
import com.openexchange.mail.authenticity.impl.core.MailAuthenticityHandlerRegistryImpl;
import com.openexchange.mail.authenticity.impl.core.MailAuthenticityJSlobEntry;
import com.openexchange.mail.authenticity.impl.core.metrics.MailAuthenticityMetricFileLogger;
import com.openexchange.mail.authenticity.impl.core.metrics.MailAuthenticityMetricLogger;
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
        final BundleContext context = this.context;
        // It is OK to pass service references since 'stopOnServiceUnavailability' returns 'true'
        LeanConfigurationService leanConfigService = getService(LeanConfigurationService.class);

        MailAuthenticityHandlerRegistryImpl registry = new MailAuthenticityHandlerRegistryImpl(leanConfigService, context);
        registerService(MailAuthenticityHandlerRegistry.class, registry);

        registerService(MailAuthenticityMetricLogger.class, new MailAuthenticityMetricFileLogger(leanConfigService));
        trackService(MailAuthenticityMetricLogger.class);

        track(MailAuthenticityHandler.class, registry);
        openTrackers();

        TrustedMailServiceImpl authenticationHandler = new TrustedMailServiceImpl(this);
        registerService(ForcedReloadable.class, authenticationHandler);
        CustomRuleChecker ruleChecker = new CustomRuleChecker(leanConfigService);
        registerService(Reloadable.class, ruleChecker);
        MailAuthenticityHandlerImpl handlerImpl = new MailAuthenticityHandlerImpl(authenticationHandler, this, ruleChecker);
        registerService(MailAuthenticityHandler.class, handlerImpl);
        registerService(Reloadable.class, new ConfigReloader(registry, handlerImpl));

        MailAuthenticityFetchListener fetchListener = new MailAuthenticityFetchListener(registry, getService(ThreadPoolService.class));
        registerService(MailFetchListener.class, fetchListener);

        registerService(JSlobEntry.class, new MailAuthenticityJSlobEntry(this));

        // FIXME: Delete after changing fail evaluation
        registerService(JSlobEntry.class, new TempDisableFail());

        {
            // Register image data source
            TrustedMailDataSource trustedMailDataSource = TrustedMailDataSource.getInstance();
            Dictionary<String, Object> props = new Hashtable<String, Object>(1);
            props.put("identifier", trustedMailDataSource.getRegistrationName());
            registerService(DataSource.class, trustedMailDataSource, props);
            ImageActionFactory.addMapping(trustedMailDataSource.getRegistrationName(), trustedMailDataSource.getAlias());
        }
        Services.setServiceLookup(this);
    }

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
