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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import javax.servlet.Servlet;
import org.osgi.service.http.HttpService;
import com.openexchange.exceptions.osgi.ComponentRegistration;
import com.openexchange.server.osgiservice.DeferredActivator;
import com.openexchange.subscribe.SubscribeService;
import com.openexchange.subscribe.SubscriptionExecutionService;
import com.openexchange.subscribe.SubscriptionHandler;
import com.openexchange.subscribe.json.SubscribeJSONServlet;
import com.openexchange.subscribe.json.SubscriptionJSONErrorMessages;
import com.openexchange.subscribe.json.SubscriptionJSONWriter;
import com.openexchange.subscribe.json.SubscriptionServlet;
import com.openexchange.subscribe.json.SubscriptionSourceJSONWriter;
import com.openexchange.subscribe.json.SubscriptionSourcesServlet;
import com.openexchange.subscribe.osgi.tools.WhiteboardSubscriptionSourceDiscoveryService;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class Activator extends DeferredActivator {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(Activator.class);

    private static final String SUBSCRIPTION_ALIAS = "ajax/subscriptions";

    private static final String SUBSCRIPTION_SOURCES_ALIAS = "ajax/subscriptionSources";

    private Servlet subscriptionSources;

    private Servlet subscriptions;

    private static final Class<?>[] NEEDED_SERVICES = { HttpService.class, SubscriptionExecutionService.class };

    private ComponentRegistration componentRegistration;

    private WhiteboardSubscriptionSourceDiscoveryService discoverer;

    @Override
    protected Class<?>[] getNeededServices() {
        return NEEDED_SERVICES;
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        final HttpService httpService = getService(HttpService.class);
        registerServlets(httpService);
    }

    private void registerServlets(HttpService httpService) {
        try {
            SubscriptionExecutionService subscriptionExecutionService = getService(SubscriptionExecutionService.class);
            SubscriptionServlet.setSubscriptionExecutionService(subscriptionExecutionService);
            
            httpService.registerServlet(SUBSCRIPTION_SOURCES_ALIAS, (subscriptionSources = new SubscriptionSourcesServlet()), null, null);
            httpService.registerServlet(SUBSCRIPTION_ALIAS, (subscriptions = new SubscriptionServlet()), null, null);
            LOG.info("Registered Servlets for Subscriptions");
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void deregisterServlets(HttpService httpService) {
        if (subscriptionSources != null) {
            httpService.unregister(SUBSCRIPTION_SOURCES_ALIAS);
            subscriptionSources = null;
        }
        if (subscriptions != null) {
            httpService.unregister(SUBSCRIPTION_ALIAS);
            subscriptions = null;
        }
        LOG.info("Deregistered Servlets for Subscriptions");
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        final HttpService httpService = getService(HttpService.class);
        SubscriptionServlet.setSubscriptionExecutionService(null);
        deregisterServlets(httpService);
    }

    @Override
    protected void startBundle() throws Exception {
        discoverer = new WhiteboardSubscriptionSourceDiscoveryService(context);
        componentRegistration = new ComponentRegistration(context, "SUBH","com.openexchange.subscribe.json", SubscriptionJSONErrorMessages.FACTORY);
        
        SubscriptionExecutionService subscriptionExecutionService = getService(SubscriptionExecutionService.class);
        SubscriptionServlet.setSubscriptionExecutionService(subscriptionExecutionService);
        
        
        SubscriptionSourcesServlet.setSubscriptionSourceDiscoveryService(discoverer);
        SubscriptionSourcesServlet.setSubscriptionSourceJSONWriter(new SubscriptionSourceJSONWriter());
        
        SubscriptionServlet.setSubscriptionSourceDiscoveryService(discoverer);
        
        final HttpService httpService = getService(HttpService.class);
        if (null != httpService) {
            registerServlets(httpService);
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        final HttpService httpService = getService(HttpService.class);
        if (null != httpService) {
            deregisterServlets(httpService);
        }
        SubscriptionServlet.setSubscriptionExecutionService(null);
        SubscriptionSourcesServlet.setSubscriptionSourceDiscoveryService(null);
        SubscriptionServlet.setSubscriptionSourceDiscoveryService(null);
        discoverer.close();
        componentRegistration.unregister();
        
    }
}
