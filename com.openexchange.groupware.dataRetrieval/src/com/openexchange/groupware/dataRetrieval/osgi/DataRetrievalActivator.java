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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.groupware.dataRetrieval.osgi;

import java.util.Map;
import org.osgi.service.http.HttpService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.groupware.dataRetrieval.actions.RetrievalActions;
import com.openexchange.groupware.dataRetrieval.services.Services;
import com.openexchange.groupware.dataRetrieval.servlets.FileDeliveryServlet;
import com.openexchange.groupware.dataRetrieval.servlets.Paths;
import com.openexchange.groupware.dataRetrieval.servlets.RetrievalServlet;
import com.openexchange.multiple.AJAXActionServiceAdapterHandler;
import com.openexchange.multiple.MultipleHandlerFactoryService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.session.RandomTokenContainer;
import com.openexchange.session.SessionSpecificContainerRetrievalService;
import com.openexchange.tools.service.ServletRegistration;
import com.openexchange.tools.service.SessionServletRegistration;

/**
 * {@link DataRetrievalActivator}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class DataRetrievalActivator extends HousekeepingActivator {

    private static final String NAMESPACE = "com.openexchange.groupware.dataRetrieval.tokens";

    private OSGIDataProviderRegistry dataProviderRegistry;

    private ServletRegistration servletRegistration1;

    private ServletRegistration servletRegistration2;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { HttpService.class, SessionSpecificContainerRetrievalService.class, ConfigurationService.class };
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        // IGNORE
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        // IGNORE
    }

    @Override
    protected void startBundle() throws Exception {
        Services.SERVICE_LOOKUP = this;
        dataProviderRegistry = new OSGIDataProviderRegistry(context);
        dataProviderRegistry.open();

        final SessionSpecificContainerRetrievalService containerRetrievalService = getService(SessionSpecificContainerRetrievalService.class);
        final RandomTokenContainer<Map<String, Object>> randomTokenContainer = containerRetrievalService.getRandomTokenContainer(
            NAMESPACE,
            null,
            null);

        final RetrievalActions retrievalActions = new RetrievalActions(dataProviderRegistry, randomTokenContainer);
        RetrievalServlet.RETRIEVAL_ACTIONS = retrievalActions;

        FileDeliveryServlet.DATA_PROVIDERS = dataProviderRegistry;
        FileDeliveryServlet.PARAM_MAP = randomTokenContainer;

        final AJAXActionServiceAdapterHandler actionService = new AJAXActionServiceAdapterHandler(retrievalActions, Paths.MODULE);

        servletRegistration1 = new SessionServletRegistration(context, new RetrievalServlet(), "/ajax/" + Paths.MODULE);
        servletRegistration2 = new ServletRegistration(context, new FileDeliveryServlet(), Paths.FILE_DELIVERY_PATH);

        registerService(MultipleHandlerFactoryService.class, actionService, null);

    }

    @Override
    protected void stopBundle() throws Exception {
        if (servletRegistration1 != null) {
            servletRegistration1.remove();
        }
        if (servletRegistration2 != null) {
            servletRegistration2.remove();
        }

        unregisterServices();

        if (dataProviderRegistry != null) {
            dataProviderRegistry.close();
            final SessionSpecificContainerRetrievalService containerRetrievalService = getService(SessionSpecificContainerRetrievalService.class);
            containerRetrievalService.destroyRandomTokenContainer(NAMESPACE, null);
        }

    }

}
