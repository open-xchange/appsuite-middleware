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

        final String sCapability = "mail_categories";
        Dictionary<String, Object> properties = new Hashtable<>(2);
        properties.put(CapabilityChecker.PROPERTY_CAPABILITIES, sCapability);
        registerService(CapabilityChecker.class, new FailureAwareCapabilityChecker() {
            @Override
            public FailureAwareCapabilityChecker.Result checkEnabled(String capability, Session ses) throws OXException {
                if (sCapability.equals(capability)) {
                    ServerSession session = ServerSessionAdapter.valueOf(ses);
                    if (session.isAnonymous() || session.getUser().isGuest()) {
                        return FailureAwareCapabilityChecker.Result.DISABLED;
                    }

                    ConfigViewFactory service = Services.getService(ConfigViewFactory.class);
                    ConfigView view = service.getView(ses.getUserId(), ses.getContextId());

                    ComposedConfigProperty<Boolean> property = view.property("com.openexchange.mail.categories", Boolean.class);
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
        getService(CapabilityService.class).declareCapability(sCapability);

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
