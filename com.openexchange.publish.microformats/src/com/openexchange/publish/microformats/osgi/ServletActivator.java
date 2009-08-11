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

package com.openexchange.publish.microformats.osgi;

import javax.servlet.ServletException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import com.openexchange.context.ContextService;
import com.openexchange.groupware.contact.ContactInterfaceDiscoveryService;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.publish.PublicationDataLoaderService;
import com.openexchange.publish.microformats.ContactPictureServlet;
import com.openexchange.publish.microformats.InfostoreFileServlet;
import com.openexchange.publish.microformats.MicroformatServlet;
import com.openexchange.publish.microformats.OnlinePublicationServlet;
import com.openexchange.server.osgiservice.DeferredActivator;
import com.openexchange.templating.TemplateService;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserConfigurationService;

/**
 * {@link ServletActivator}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ServletActivator extends DeferredActivator {

    private static final Log LOG = LogFactory.getLog(ServletActivator.class);

    private PublicationServicesActivator activator = new PublicationServicesActivator();

    private boolean registered;

    private static final Class<?>[] NEEDED_SERVICES = {
        HttpService.class, PublicationDataLoaderService.class, ContextService.class, TemplateService.class,
        ContactInterfaceDiscoveryService.class, UserConfigurationService.class, UserService.class, InfostoreFacade.class  };

    @Override
    protected Class<?>[] getNeededServices() {
        return NEEDED_SERVICES;
    }

    @Override
    protected void handleAvailability(Class<?> clazz) {
        registerServlet();
    }

    @Override
    protected void handleUnavailability(Class<?> clazz) {
        unregisterServlet();
    }

    @Override
    protected void startBundle() throws Exception {
        activator.start(context);
        registerServlet();
    }

    @Override
    protected void stopBundle() throws Exception {
        activator.stop(context);
        unregisterServlet();
    }

    private void registerServlet() {
        HttpService httpService = getService(HttpService.class);
        PublicationDataLoaderService dataLoader = getService(PublicationDataLoaderService.class);
        ContextService contexts = getService(ContextService.class);
        TemplateService templates = getService(TemplateService.class);
        ContactInterfaceDiscoveryService contacts = getService(ContactInterfaceDiscoveryService.class);
        InfostoreFacade infostore = getService(InfostoreFacade.class);
        UserConfigurationService userConfigs = getService(UserConfigurationService.class);
        UserService users = getService(UserService.class);
        
        
        if (null == httpService || null == dataLoader || null == contexts || null == templates || null == contacts || null == userConfigs || null == users) {
            return;
        }

        OnlinePublicationServlet.setContextService(contexts);

        MicroformatServlet.setPublicationDataLoaderService(dataLoader);
        MicroformatServlet.setTemplateService(templates);

        MicroformatServlet microformatServlet = new MicroformatServlet();

        ContactPictureServlet.setContactInterfaceDiscoveryService(contacts);

        InfostoreFileServlet.setUserConfigs(userConfigs);
        InfostoreFileServlet.setUsers(users);
        InfostoreFileServlet.setInfostore(infostore);
        
        
        registered = true;
        for (String alias : activator.getAliases()) {
            try {
                httpService.registerServlet(alias + "/*", microformatServlet, null, null);
            } catch (ServletException e) {
                LOG.error(e.getMessage(), e);
            } catch (NamespaceException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        try {
            httpService.registerServlet("/publications/contactPictures/*", new ContactPictureServlet(), null, null);
            httpService.registerServlet("/publications/files/*", new InfostoreFileServlet(), null, null);
        } catch (ServletException e) {
            LOG.error(e.getMessage(), e);
        } catch (NamespaceException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void unregisterServlet() {
        if (!registered)
            return;
        registered = false;

        HttpService httpService = getService(HttpService.class);

        for (String alias : activator.getAliases()) {
            httpService.unregister(alias + "/*");
        }
    }

}
