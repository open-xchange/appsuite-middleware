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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.subscribe.json.osgi;

import java.util.ArrayList;
import java.util.List;
import org.osgi.service.http.HttpService;
import com.openexchange.multiple.MultipleHandlerFactoryService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.secret.osgi.tools.WhiteboardSecretService;
import com.openexchange.subscribe.SubscriptionExecutionService;
import com.openexchange.subscribe.json.SubscriptionMultipleFactory;
import com.openexchange.subscribe.json.SubscriptionServlet;
import com.openexchange.subscribe.json.SubscriptionSourceMultipleFactory;
import com.openexchange.subscribe.json.SubscriptionSourcesServlet;
import com.openexchange.subscribe.osgi.tools.WhiteboardSubscriptionSourceDiscoveryService;
import com.openexchange.tools.service.SessionServletRegistration;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class ServletActivator extends HousekeepingActivator {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(ServletActivator.class));

    private static final String SUBSCRIPTION_ALIAS = "ajax/subscriptions";

    private static final String SUBSCRIPTION_SOURCES_ALIAS = "ajax/subscriptionSources";

    private final List<SessionServletRegistration> servletRegistrations = new ArrayList<SessionServletRegistration>(2);

    private static final Class<?>[] NEEDED_SERVICES = { HttpService.class, SubscriptionExecutionService.class };

    private WhiteboardSubscriptionSourceDiscoveryService discoverer;

    private WhiteboardSecretService secretService;

    @Override
    protected Class<?>[] getNeededServices() {
        return NEEDED_SERVICES;
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        // Ignore
    }

    private void registerServlets() {
        try {
            //servletRegistrations.add(new SessionServletRegistration(context, new SubscriptionSourcesServlet(), SUBSCRIPTION_SOURCES_ALIAS));
            servletRegistrations.add(new SessionServletRegistration(context, new SubscriptionServlet(), SUBSCRIPTION_ALIAS));
            for (final SessionServletRegistration reg : servletRegistrations) {
                reg.open();
            }
            LOG.info("Registered Servlets for Subscriptions");
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void deregisterServlets() {
        for (final SessionServletRegistration reg : servletRegistrations) {
            reg.close();
        }
        LOG.info("Deregistered Servlets for Subscriptions");
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        // Ignore
    }

    @Override
    protected void startBundle() throws Exception {
        discoverer = new WhiteboardSubscriptionSourceDiscoveryService(context);

        registerServlets();
        createMultipleHandler();
    }

    private void createMultipleHandler() {
        final SubscriptionExecutionService subscriptionExecutionService = getService(SubscriptionExecutionService.class);

        final SubscriptionMultipleFactory subscriptionsFactory = new SubscriptionMultipleFactory(
            discoverer,
            subscriptionExecutionService,
            secretService = new WhiteboardSecretService(context));
        secretService.open();
        final SubscriptionSourceMultipleFactory subscriptionSourcesFactory = new SubscriptionSourceMultipleFactory(discoverer);

        SubscriptionServlet.setFactory(subscriptionsFactory);
        SubscriptionSourcesServlet.setFactory(subscriptionSourcesFactory);

        registerService(MultipleHandlerFactoryService.class, subscriptionsFactory);
        registerService(MultipleHandlerFactoryService.class, subscriptionSourcesFactory);

    }

    @Override
    protected void stopBundle() throws Exception {
        deregisterServlets();
        discoverer.close();
        destroyMultipleHandler();
    }

    private void destroyMultipleHandler() {
        secretService.close();
        unregisterServices();
        SubscriptionServlet.setFactory(null);
        SubscriptionSourcesServlet.setFactory(null);
    }
}
