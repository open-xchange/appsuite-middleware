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

package com.openexchange.unifiedinbox.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.BundleActivator;
import com.openexchange.caching.CacheService;
import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.continuation.ContinuationRegistryService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.i18n.I18nService;
import com.openexchange.mail.api.MailProvider;
import com.openexchange.mail.api.unified.UnifiedViewService;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.unifiedinbox.Enabled;
import com.openexchange.unifiedinbox.UnifiedInboxMessageStorage;
import com.openexchange.unifiedinbox.UnifiedInboxProvider;
import com.openexchange.unifiedinbox.services.Services;
import com.openexchange.unifiedinbox.utility.UnifiedInboxSynchronousQueueProvider;
import com.openexchange.user.UserService;

/**
 * {@link UnifiedInboxActivator} - The {@link BundleActivator activator} for Unified Mail bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UnifiedInboxActivator extends HousekeepingActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(UnifiedInboxActivator.class);

    /**
     * Initializes a new {@link UnifiedInboxActivator}
     */
    public UnifiedInboxActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, CacheService.class, UserService.class, MailAccountStorageService.class, ContextService.class, ThreadPoolService.class, ConfigViewFactory.class, UnifiedInboxManagement.class, CapabilityService.class };
    }

    @Override
    public void startBundle() throws Exception {
        try {
            Services.setServiceLookup(this);

            // Create & open trackers
            track(I18nService.class, new I18nCustomizer(context));
            trackService(ContinuationRegistryService.class);
            openTrackers();

            // Register service(s)
            {
                Dictionary<String, String> dictionary = new Hashtable<String, String>(1);
                dictionary.put("protocol", UnifiedInboxProvider.PROTOCOL_UNIFIED_INBOX.toString());
                registerService(MailProvider.class, UnifiedInboxProvider.getInstance(), dictionary);
            }
            registerService(PreferencesItemService.class, new Enabled(getService(ConfigViewFactory.class)));

            // Detect what SynchronousQueue to use
            String property = System.getProperty("java.specification.version");
            if (null == property) {
                property = System.getProperty("java.runtime.version");
                if (null == property) {
                    // JRE not detectable, use fallback
                    UnifiedInboxSynchronousQueueProvider.initInstance(false);
                } else {
                    // "java.runtime.version=1.6.0_0-b14" OR "java.runtime.version=1.5.0_18-b02"
                    UnifiedInboxSynchronousQueueProvider.initInstance(!property.startsWith("1.5"));
                }
            } else {
                // "java.specification.version=1.5" OR "java.specification.version=1.6"
                UnifiedInboxSynchronousQueueProvider.initInstance("1.5".compareTo(property) < 0);
            }

            // Register unified service
            registerService(UnifiedViewService.class, new UnifiedInboxMessageStorage());

            // Register "unified-mailbox" capability
            {
                final ServiceLookup services = this;
                final String sCapability = "unified-mailbox";
                Dictionary<String, Object> dictionary = new Hashtable<String, Object>(2);
                dictionary.put(CapabilityChecker.PROPERTY_CAPABILITIES, sCapability);
                registerService(CapabilityChecker.class, new CapabilityChecker() {

                    @Override
                    public boolean isEnabled(String capability, Session session) throws OXException {
                        if (sCapability.equals(capability)) {
                            ServerSession serverSession = ServerSessionAdapter.valueOf(session);
                            if (serverSession.isAnonymous() || !serverSession.getUserPermissionBits().hasWebMail()) {
                                return false;
                            }

                            ConfigViewFactory factory = services.getService(ConfigViewFactory.class);
                            ConfigView view = factory.getView(session.getUserId(), session.getContextId());
                            ComposedConfigProperty<Boolean> property = view.property("com.openexchange.unifiedinbox.enabled", boolean.class);
                            // Either absent or "com.openexchange.unifiedinbox.enabled=true"
                            return property.isDefined() ? property.get().booleanValue() : true;
                        }

                        return true;
                    }
                }, dictionary);

                getService(CapabilityService.class).declareCapability(sCapability);
            }
        } catch (Exception e) {
            LOG.error("", e);
            throw e;
        }
    }

    @Override
    public void stopBundle() throws Exception {
        try {
            UnifiedInboxSynchronousQueueProvider.releaseInstance();
            super.stopBundle();
            Services.setServiceLookup(null);
        } catch (Exception e) {
            LOG.error("", e);
            throw e;
        }
    }

}
