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

package com.openexchange.publish.json.osgi;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exceptions.osgi.ComponentRegistration;
import com.openexchange.multiple.MultipleHandlerFactoryService;
import com.openexchange.publish.PublicationTargetDiscoveryService;
import com.openexchange.publish.json.PublicationJSONErrorMessage;
import com.openexchange.publish.json.PublicationMultipleHandlerFactory;
import com.openexchange.publish.json.PublicationServlet;
import com.openexchange.publish.json.PublicationTargetMultipleHandlerFactory;
import com.openexchange.publish.json.PublicationTargetServlet;
import com.openexchange.publish.json.types.EntityMap;
import com.openexchange.server.osgiservice.DeferredActivator;
import com.openexchange.tools.service.SessionServletRegistration;

public class ServletActivator extends DeferredActivator {

    private static final Log LOG = LogFactory.getLog(ServletActivator.class);

    private static final String TARGET_ALIAS = "ajax/publicationTargets";
    private static final String PUB_ALIAS = "ajax/publications";

    private static final Class<?>[] NEEDED_SERVICES = { HttpService.class, PublicationTargetDiscoveryService.class, ConfigurationService.class };

    private ComponentRegistration componentRegistration;

    List<SessionServletRegistration> servletRegistrations = new ArrayList<SessionServletRegistration>(2);
    
    private List<ServiceRegistration> serviceRegistrations = new LinkedList<ServiceRegistration>();
    
    @Override
    protected Class<?>[] getNeededServices() {
        return NEEDED_SERVICES;
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        register();

    }

    private void register() {
        PublicationTargetDiscoveryService discovery = getService(PublicationTargetDiscoveryService.class);
        if(discovery == null) {
            return;
        }
        
        ConfigurationService config = getService(ConfigurationService.class);
        if(config == null){
        	return;
        }
        
        PublicationMultipleHandlerFactory publicationHandlerFactory = new PublicationMultipleHandlerFactory(discovery, new EntityMap(), config);
        PublicationTargetMultipleHandlerFactory publicationTargetHandlerFactory = new PublicationTargetMultipleHandlerFactory(discovery);
        
        serviceRegistrations.add(context.registerService(MultipleHandlerFactoryService.class.getName(), publicationHandlerFactory, null));
        serviceRegistrations.add(context.registerService(MultipleHandlerFactoryService.class.getName(), publicationTargetHandlerFactory, null));
        
        PublicationServlet.setFactory(publicationHandlerFactory);
        PublicationTargetServlet.setFactory(publicationTargetHandlerFactory);
        
        servletRegistrations.add(new SessionServletRegistration(context, new PublicationTargetServlet(), TARGET_ALIAS));
        servletRegistrations.add(new SessionServletRegistration(context, new PublicationServlet(), PUB_ALIAS));
        
        for (SessionServletRegistration reg : servletRegistrations) {
            reg.open();
        }
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        unregister();
    }

    private void unregister() {
        PublicationServlet.setFactory(null);
        PublicationTargetServlet.setFactory(null);
        
        for(ServiceRegistration registration : serviceRegistrations) {
            registration.unregister();
        }
        serviceRegistrations.clear();
        
        for (SessionServletRegistration reg : servletRegistrations) {
            reg.close();
        }
        servletRegistrations.clear();

    }

    @Override
    protected void startBundle() throws Exception {
        componentRegistration = new ComponentRegistration(
            context,
            "PUBH",
            "com.openexchange.publish.json",
            PublicationJSONErrorMessage.EXCEPTIONS);

        register();
    }

    @Override
    protected void stopBundle() throws Exception {
        unregister();
        componentRegistration.unregister();
    }
}
