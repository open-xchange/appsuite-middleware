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

package com.openexchange.subscribe.json.osgi;

import org.osgi.service.http.HttpService;
import com.openexchange.ajax.osgi.AbstractSessionServletActivator;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.multiple.MultipleHandlerFactoryService;
import com.openexchange.secret.osgi.tools.WhiteboardSecretService;
import com.openexchange.subscribe.SubscriptionExecutionService;
import com.openexchange.subscribe.json.SubscriptionMultipleFactory;
import com.openexchange.subscribe.json.SubscriptionServlet;
import com.openexchange.subscribe.json.SubscriptionSourceMultipleFactory;
import com.openexchange.subscribe.json.SubscriptionSourcesServlet;
import com.openexchange.subscribe.osgi.tools.WhiteboardSubscriptionSourceDiscoveryService;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class ServletActivator extends AbstractSessionServletActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ServletActivator.class);

    private static final String SUBSCRIPTION_ALIAS_APPENDIX = "subscriptions";

    private static final String SUBSCRIPTION_SOURCES_ALIAS_APPENDIX = "subscriptionSources";

    private WhiteboardSubscriptionSourceDiscoveryService discoverer;

    private WhiteboardSecretService secretService;

    @Override
    protected Class<?>[] getAdditionalNeededServices() {
        return new Class<?>[] { HttpService.class, SubscriptionExecutionService.class, DispatcherPrefixService.class };
    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

    private void registerServlets() {
        try {
            final DispatcherPrefixService dispatcherPrefixService = getService(DispatcherPrefixService.class);
            registerSessionServlet(dispatcherPrefixService.getPrefix() + SUBSCRIPTION_SOURCES_ALIAS_APPENDIX, new SubscriptionSourcesServlet());
            registerSessionServlet(dispatcherPrefixService.getPrefix() + SUBSCRIPTION_ALIAS_APPENDIX, new SubscriptionServlet());
            LOG.info("Registered Servlets for Subscriptions");
        } catch (final Exception e) {
            LOG.error("", e);
        }
    }

    @Override
    protected void startBundle() throws Exception {
        discoverer = new WhiteboardSubscriptionSourceDiscoveryService(context);

        registerServlets();
        createMultipleHandler();
    }

    private void createMultipleHandler() {
        SubscriptionExecutionService subscriptionExecutionService = getService(SubscriptionExecutionService.class);

        SubscriptionMultipleFactory subscriptionsFactory = new SubscriptionMultipleFactory(
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
