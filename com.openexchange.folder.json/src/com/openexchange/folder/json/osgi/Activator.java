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

package com.openexchange.folder.json.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.folder.json.multiple.FolderMultipleHandlerFactory;
import com.openexchange.folder.json.services.ServiceRegistry;
import com.openexchange.folderstorage.ContentTypeDiscoveryService;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.multiple.MultipleHandlerFactoryService;
import com.openexchange.server.osgiservice.RegistryServiceTrackerCustomizer;

public class Activator implements BundleActivator {

    private ServiceRegistration folderMultipleService;

    private ServiceTracker httpTracker;

    private ServiceTracker folderServiceTracker;

    private ServiceTracker ctServiceTracker;

    public void start(final BundleContext context) throws Exception {
        folderMultipleService = context.registerService(
            MultipleHandlerFactoryService.class.getName(),
            new FolderMultipleHandlerFactory(),
            null);
        folderServiceTracker = new ServiceTracker(
            context,
            FolderService.class.getName(),
            new RegistryServiceTrackerCustomizer<FolderService>(context, ServiceRegistry.getInstance(), FolderService.class));
        folderServiceTracker.open();
        httpTracker = new ServiceTracker(context, HttpService.class.getName(), new ServletRegisterer(context));
        httpTracker.open();
        ctServiceTracker = new ServiceTracker(
            context,
            ContentTypeDiscoveryService.class.getName(),
            new RegistryServiceTrackerCustomizer<ContentTypeDiscoveryService>(
                context,
                ServiceRegistry.getInstance(),
                ContentTypeDiscoveryService.class));
        ctServiceTracker.open();
    }

    public void stop(final BundleContext context) throws Exception {
        ctServiceTracker.close();
        ctServiceTracker = null;
        httpTracker.close();
        httpTracker = null;
        folderServiceTracker.close();
        folderServiceTracker = null;
        folderMultipleService.unregister();
        folderMultipleService = null;
    }

}
