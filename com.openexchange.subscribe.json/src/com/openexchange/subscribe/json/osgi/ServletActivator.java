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
        } catch (Exception e) {
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
        super.stopBundle();
    }

    private void destroyMultipleHandler() {
        secretService.close();
        unregisterServices();
        SubscriptionServlet.setFactory(null);
        SubscriptionSourcesServlet.setFactory(null);
    }
}
