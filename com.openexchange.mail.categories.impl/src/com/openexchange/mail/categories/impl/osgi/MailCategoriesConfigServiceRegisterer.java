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
