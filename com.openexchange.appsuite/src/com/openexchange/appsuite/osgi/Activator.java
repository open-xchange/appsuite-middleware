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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.appsuite.osgi;

import java.io.File;
import org.osgi.framework.BundleException;
import org.osgi.service.http.HttpService;
import com.openexchange.ajax.requesthandler.Dispatcher;
import com.openexchange.appsuite.AppSuiteLoginRampUp;
import com.openexchange.appsuite.AppsLoadServlet;
import com.openexchange.appsuite.FileContribution;
import com.openexchange.appsuite.FileContributor;
import com.openexchange.config.ConfigurationService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.login.LoginRampUpService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.NearRegistryServiceTracker;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link Activator} - The activator for <code>"com.openexchange.appsuite"</code> bundle.
 */
public class Activator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { HttpService.class, ConfigurationService.class, DispatcherPrefixService.class, Dispatcher.class, ThreadPoolService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        String prefix = getService(DispatcherPrefixService.class).getPrefix();
        if (null == prefix) {
            prefix = Dispatcher.PREFIX.get();
        }
        ConfigurationService config = getService(ConfigurationService.class);
        String property = config.getProperty("com.openexchange.apps.path");
        if (property == null) {
            throw new BundleException("Missing property: com.openexchange.apps.path", BundleException.ACTIVATOR_ERROR);
        }
        String[] paths = property.split(":");
        File[] apps = new File[paths.length];
        int i = 0;
        for (String path : paths) {
            apps[i++] = new File(new File(path), "apps");
        }
        File zoneinfo = new File(config.getProperty("com.openexchange.apps.tzdata", "/usr/share/zoneinfo/"));
        HttpService service = getService(HttpService.class);
        service.registerServlet(prefix + "apps/load", new AppsLoadServlet(apps, zoneinfo), null, null);

        final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Activator.class);
        StringBuilder sb = new StringBuilder();
        for (File app : apps) {
            sb.append(':');
            sb.append(app.getPath());
        }
        logger.info("Servlet path \"apps/load\" successfully registered with \"apps\"={} and \"zoneinfo\"={}", sb.substring(1), zoneinfo.getPath());
        
        final NearRegistryServiceTracker<FileContributor> contributorTracker = new NearRegistryServiceTracker<FileContributor>(context, FileContributor.class);
        rememberTracker(contributorTracker);
        
        openTrackers();
        
        AppsLoadServlet.contributors = new FileContributor() {
            
            @Override
            public FileContribution getData(ServerSession session, String moduleName) throws OXException {
                for(FileContributor contributor: contributorTracker.getServiceList()) {
                    FileContribution contribution = null;
                    try {
                        contribution = contributor.getData(session, moduleName);                        
                    } catch (OXException x) {
                        
                    }
                    if (contribution != null) {
                        return contribution;
                    }
                }
                return null;
            }
        };
        
        registerService(LoginRampUpService.class, new AppSuiteLoginRampUp(this));
    }
}
