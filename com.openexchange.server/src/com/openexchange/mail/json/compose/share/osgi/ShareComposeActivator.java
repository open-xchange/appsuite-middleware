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

package com.openexchange.mail.json.compose.share.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.capabilities.DependentCapabilityChecker;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.jslob.ConfigTreeEquivalent;
import com.openexchange.mail.json.compose.ComposeHandler;
import com.openexchange.mail.json.compose.Utilities;
import com.openexchange.mail.json.compose.share.DefaultAttachmentStorage;
import com.openexchange.mail.json.compose.share.MessageGenerators;
import com.openexchange.mail.json.compose.share.ShareComposeHandler;
import com.openexchange.mail.json.compose.share.internal.AttachmentStorageRegistry;
import com.openexchange.mail.json.compose.share.internal.AttachmentStorageRegistryImpl;
import com.openexchange.mail.json.compose.share.internal.MessageGeneratorRegistry;
import com.openexchange.mail.json.compose.share.internal.MessageGeneratorRegistryImpl;
import com.openexchange.mail.json.compose.share.internal.ShareLinkGeneratorRegistry;
import com.openexchange.mail.json.compose.share.internal.ShareLinkGeneratorRegistryImpl;
import com.openexchange.mail.json.compose.share.settings.AbstractShareComposeSetting;
import com.openexchange.mail.json.compose.share.settings.EnabledShareComposeSetting;
import com.openexchange.mail.json.compose.share.settings.FilesAutoExpireShareComposeSetting;
import com.openexchange.mail.json.compose.share.settings.NameShareComposeSetting;
import com.openexchange.mail.json.compose.share.settings.RequiredExpirationShareComposeSetting;
import com.openexchange.mail.json.compose.share.spi.AttachmentStorage;
import com.openexchange.mail.json.compose.share.spi.MessageGenerator;
import com.openexchange.mail.json.compose.share.spi.ShareLinkGenerator;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.templating.TemplateService;
import com.openexchange.timer.TimerService;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;


/**
 * {@link ShareComposeActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class ShareComposeActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link ShareComposeActivator}.
     */
    public ShareComposeActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, TimerService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        RankingAwareNearRegistryServiceTracker<ShareLinkGenerator> shareLinkGeneratorTracker = new RankingAwareNearRegistryServiceTracker<>(context, ShareLinkGenerator.class);
        rememberTracker(shareLinkGeneratorTracker);

        RankingAwareNearRegistryServiceTracker<MessageGenerator> messageGeneratorTracker = new RankingAwareNearRegistryServiceTracker<>(context, MessageGenerator.class);
        rememberTracker(messageGeneratorTracker);

        RankingAwareNearRegistryServiceTracker<AttachmentStorage> attachmentStorageTracker = new RankingAwareNearRegistryServiceTracker<>(context, AttachmentStorage.class);
        rememberTracker(attachmentStorageTracker);

        // Tracker for CapabilityService that declares "publish_mail_attachments" capability
        final BundleContext context = this.context;
        track(CapabilityService.class, new ServiceTrackerCustomizer<CapabilityService, CapabilityService>() {

            private volatile ServiceRegistration<CapabilityChecker> serviceRegistration;

            @Override
            public CapabilityService addingService(ServiceReference<CapabilityService> reference) {
                CapabilityService service = context.getService(reference);
                final String sCapability = "share_mail_attachments";

                // Register CapabilityChecker for "publish_mail_attachments"
                Dictionary<String, Object> properties = new Hashtable<String, Object>(2);
                properties.put(CapabilityChecker.PROPERTY_CAPABILITIES, sCapability);
                DependentCapabilityChecker capabilityChecker = new DependentCapabilityChecker() {

                    @Override
                    public boolean isEnabled(String capability, Session ses, CapabilitySet capabilitySet) throws OXException {
                        if (sCapability.equals(capability)) {
                            ServerSession session = ServerSessionAdapter.valueOf(ses);
                            if (session.isAnonymous() || !session.getUserPermissionBits().hasWebMail()) {
                                return false;
                            }

                            if (false == Utilities.getBoolFromProperty("com.openexchange.mail.compose.share.enabled", true, session)) {
                                return false;
                            }

                            return Utilities.hasCapabilities(capabilitySet, "drive", "share_links");
                        }

                        return true;
                    }
                };
                serviceRegistration = context.registerService(CapabilityChecker.class, capabilityChecker, properties);

                // Declare "share_mail_attachments" capability
                service.declareCapability(sCapability);

                // Return tracked service
                return service;
            }

            @Override
            public void modifiedService(ServiceReference<CapabilityService> reference, CapabilityService service) {
                // Ignore
            }

            @Override
            public void removedService(ServiceReference<CapabilityService> reference, CapabilityService service) {
                ServiceRegistration<CapabilityChecker> serviceRegistration = this.serviceRegistration;
                if (null != serviceRegistration) {
                    this.serviceRegistration = null;
                    serviceRegistration.unregister();
                }
                context.ungetService(reference);
            }
        });

        {
            ServiceTrackerCustomizer<TranslatorFactory, TranslatorFactory> tracker = new ServiceTrackerCustomizer<TranslatorFactory, TranslatorFactory>() {

                @Override
                public void removedService(ServiceReference<TranslatorFactory> reference, TranslatorFactory factory) {
                    MessageGenerators.setTranslatorFactory(null);
                    context.ungetService(reference);
                }

                @Override
                public void modifiedService(ServiceReference<TranslatorFactory> reference, TranslatorFactory factory) {
                    // Ignore
                }

                @Override
                public TranslatorFactory addingService(ServiceReference<TranslatorFactory> reference) {
                    TranslatorFactory factory = context.getService(reference);
                    MessageGenerators.setTranslatorFactory(factory);
                    return factory;
                }
            };
            track(TranslatorFactory.class, tracker);
        }

        {
            ServiceTrackerCustomizer<TemplateService, TemplateService> tracker = new ServiceTrackerCustomizer<TemplateService, TemplateService>() {

                @Override
                public void removedService(ServiceReference<TemplateService> reference, TemplateService templateService) {
                    MessageGenerators.setTemplateService(null);
                    context.ungetService(reference);
                }

                @Override
                public void modifiedService(ServiceReference<TemplateService> reference, TemplateService templateService) {
                    // Ignore
                }

                @Override
                public TemplateService addingService(ServiceReference<TemplateService> reference) {
                    TemplateService templateService = context.getService(reference);
                    MessageGenerators.setTemplateService(templateService);
                    return templateService;
                }
            };
            track(TemplateService.class, tracker);
        }

        openTrackers();

        // Initialize DefaultAttachmentStorage
        DefaultAttachmentStorage.startInstance(getService(ConfigurationService.class), getService(TimerService.class));

        ShareLinkGeneratorRegistryImpl shareLinkGeneratorRegistry = new ShareLinkGeneratorRegistryImpl(shareLinkGeneratorTracker);
        registerService(ShareLinkGeneratorRegistry.class, shareLinkGeneratorRegistry);

        MessageGeneratorRegistryImpl messageGeneratorRegistry = new MessageGeneratorRegistryImpl(messageGeneratorTracker);
        registerService(MessageGeneratorRegistry.class, messageGeneratorRegistry);

        AttachmentStorageRegistryImpl attachmentStorageRegistry = new AttachmentStorageRegistryImpl(attachmentStorageTracker);
        registerService(AttachmentStorageRegistry.class, attachmentStorageRegistry);

        ShareComposeHandler handler = new ShareComposeHandler();
        registerService(ComposeHandler.class, handler);

        // Register settings
        registerSetting(new EnabledShareComposeSetting(handler));
        registerSetting(new NameShareComposeSetting(handler));
        registerSetting(new RequiredExpirationShareComposeSetting(handler));
        registerSetting(new FilesAutoExpireShareComposeSetting(handler));
    }

    private <V> void registerSetting(AbstractShareComposeSetting<V> setting) {
        registerService(PreferencesItemService.class, setting, null);
        registerService(ConfigTreeEquivalent.class, setting, null);
    }

    @Override
    protected void stopBundle() throws Exception {
        ServerServiceRegistry.getInstance().removeService(AttachmentStorageRegistry.class);
        ServerServiceRegistry.getInstance().removeService(MessageGeneratorRegistry.class);
        ServerServiceRegistry.getInstance().removeService(ShareLinkGeneratorRegistry.class);
        DefaultAttachmentStorage.shutDown();
        super.stopBundle();
    }

}
