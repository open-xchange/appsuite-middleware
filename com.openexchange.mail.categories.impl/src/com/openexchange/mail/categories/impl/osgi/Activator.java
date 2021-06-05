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

package com.openexchange.mail.categories.impl.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.FailureAwareCapabilityChecker;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.mail.categories.impl.MailCategoriesConfigUtil;
import com.openexchange.mail.categories.ruleengine.MailCategoriesRuleEngine;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.session.Session;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link Activator}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.2
 */
public class Activator extends HousekeepingActivator {

    private static final String MAIL_CATEGORIES_CAPABILITY = "mail_categories";
    private static final String PROPERTY = "com.openexchange.mail.categories";

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { ConfigViewFactory.class, ConfigurationService.class, MailCategoriesRuleEngine.class, ThreadPoolService.class,
            CapabilityService.class, EventAdmin.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Services.setServiceLookup(this);

        final MailCategoriesConfigServiceRegisterer registerer = new MailCategoriesConfigServiceRegisterer(context);
        track(MailCategoriesRuleEngine.class, registerer);
        openTrackers();

        Dictionary<String, Object> properties = new Hashtable<>(2);
        properties.put(CapabilityChecker.PROPERTY_CAPABILITIES, MAIL_CATEGORIES_CAPABILITY);
        registerService(CapabilityChecker.class, new FailureAwareCapabilityChecker() {
            @Override
            public FailureAwareCapabilityChecker.Result checkEnabled(String capability, Session ses) throws OXException {
                if (MAIL_CATEGORIES_CAPABILITY.equals(capability)) {
                    ServerSession session = ServerSessionAdapter.valueOf(ses);
                    if (session.isAnonymous() || session.getUser().isGuest()) {
                        return FailureAwareCapabilityChecker.Result.DISABLED;
                    }

                    ConfigViewFactory service = Services.getService(ConfigViewFactory.class);
                    ConfigView view = service.getView(ses.getUserId(), ses.getContextId());

                    ComposedConfigProperty<Boolean> property = view.property(PROPERTY, Boolean.class);
                    if (!property.isDefined() || !property.get().booleanValue()) {
                        // Not enabled as per configuration
                        return FailureAwareCapabilityChecker.Result.DISABLED;
                    }

                    // Enabled. Check rule engine, too
                    try {
                        if (!registerer.getService().isRuleEngineApplicable(session)) {
                            return FailureAwareCapabilityChecker.Result.DISABLED;
                        }
                    } catch (OXException e) {
                        // Failed to reliably check rule engine
                        return FailureAwareCapabilityChecker.Result.FAILURE;
                    }
                }

                return FailureAwareCapabilityChecker.Result.ENABLED;
            }
        }, properties);
        getService(CapabilityService.class).declareCapability(MAIL_CATEGORIES_CAPABILITY);

        registerService(Reloadable.class, MailCategoriesConfigUtil.getInstance());

        Logger logger = org.slf4j.LoggerFactory.getLogger(Activator.class);
        logger.info("Bundle successfully started: {}", context.getBundle().getSymbolicName());

    }

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
        Services.setServiceLookup(null);
        Logger logger = org.slf4j.LoggerFactory.getLogger(Activator.class);
        logger.info("Bundle successfully stopped: {}", context.getBundle().getSymbolicName());
    }

}
