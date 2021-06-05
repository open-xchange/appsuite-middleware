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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.mail.categories.MailCategoriesConfigService;
import com.openexchange.mail.categories.impl.MailCategoriesConfigServiceImpl;
import com.openexchange.mail.categories.impl.MailCategoriesLoginHandler;
import com.openexchange.mail.categories.ruleengine.MailCategoriesRuleEngine;
import com.openexchange.osgi.util.RankedService;

/**
 * {@link MailCategoriesConfigServiceRegisterer}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class MailCategoriesConfigServiceRegisterer implements ServiceTrackerCustomizer<MailCategoriesRuleEngine, MailCategoriesRuleEngine> {

    private final BundleContext context;
    private final List<RankedService<MailCategoriesRuleEngine>> trackedEngines;
    private ServiceRegistration<MailCategoriesConfigService> registeredService;
    private ServiceRegistration<LoginHandlerService> registeredLoginHandler;
    private MailCategoriesConfigServiceImpl service;

    /**
     * Initializes a new {@link MailCategoriesConfigServiceRegisterer}.
     */
    public MailCategoriesConfigServiceRegisterer(BundleContext context) {
        super();
        this.context = context;
        trackedEngines = new ArrayList<RankedService<MailCategoriesRuleEngine>>(2);
    }

    /**
     * Gets the registered service instance.
     *
     * @return The service or <code>null</code>
     */
    public synchronized MailCategoriesConfigServiceImpl getService() {
        return service;
    }

    @Override
    public synchronized MailCategoriesRuleEngine addingService(ServiceReference<MailCategoriesRuleEngine> reference) {
        MailCategoriesRuleEngine engine = context.getService(reference);

        if (trackedEngines.isEmpty()) {
            trackedEngines.add(RankedService.newRankedService(reference, engine));
            reregisterStuff(engine);
        } else {
            RankedService<MailCategoriesRuleEngine> oldEngine = trackedEngines.get(0);

            trackedEngines.add(RankedService.newRankedService(reference, engine));
            Collections.sort(trackedEngines);

            RankedService<MailCategoriesRuleEngine> newEngine = trackedEngines.get(0);
            if (!oldEngine.equals(newEngine)) {
                reregisterStuff(newEngine.service);
            }
        }

        return engine;
    }

    private void reregisterStuff(MailCategoriesRuleEngine engine) {
        unregisterStuff();

        MailCategoriesConfigServiceImpl service = new MailCategoriesConfigServiceImpl(engine);
        registeredService = context.registerService(MailCategoriesConfigService.class, service, null);
        registeredLoginHandler = context.registerService(LoginHandlerService.class, new MailCategoriesLoginHandler(service), null);
        this.service = service;
    }

    @Override
    public void modifiedService(ServiceReference<MailCategoriesRuleEngine> reference, MailCategoriesRuleEngine engine) {
        // Don't care
    }

    @Override
    public synchronized void removedService(ServiceReference<MailCategoriesRuleEngine> reference, MailCategoriesRuleEngine engine) {
        RankedService<MailCategoriesRuleEngine> oldEngine = trackedEngines.get(0);
        if (false == trackedEngines.remove(RankedService.newRankedService(reference, engine))) {
            context.ungetService(reference);
            return;
        }

        if (trackedEngines.isEmpty()) {
            // Last one gone
            context.ungetService(reference);
            return;
        }

        RankedService<MailCategoriesRuleEngine> newEngine = trackedEngines.get(0);
        if (!oldEngine.equals(newEngine)) {
            reregisterStuff(newEngine.service);
        }
        context.ungetService(reference);
    }

    private void unregisterStuff() {
        if (null != registeredLoginHandler) {
            registeredLoginHandler.unregister();
        }
        if (null != registeredService) {
            registeredService.unregister();
        }
        service = null;
    }
}
