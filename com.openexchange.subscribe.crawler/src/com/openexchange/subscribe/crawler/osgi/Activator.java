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

package com.openexchange.subscribe.crawler.osgi;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ho.yaml.Yaml;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.config.ConfigurationService;
import com.openexchange.subscribe.SubscribeService;
import com.openexchange.subscribe.crawler.CrawlerDescription;
import com.openexchange.subscribe.crawler.GenericSubscribeService;

/**
 * {@link Activator}
 * 
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class Activator implements BundleActivator {

    private ArrayList<ServiceRegistration> services;

    private static final Log LOG = LogFactory.getLog(Activator.class);

    public static final String PATH_PROPERTY = "com.openexchange.subscribe.crawler.path";

    public void start(BundleContext context) throws Exception {
        services = new ArrayList<ServiceRegistration>();
        ConfigurationService config = (ConfigurationService) context.getService(context.getServiceReference(ConfigurationService.class.getName()));
        if (config != null) {
            ArrayList<CrawlerDescription> crawlers = getCrawlersFromFilesystem(config);
            for (CrawlerDescription crawler : crawlers) {
                GenericSubscribeService subscribeService = new GenericSubscribeService(
                    crawler.getDisplayName(),
                    crawler.getId(),
                    crawler.getWorkflowString());
                ServiceRegistration serviceRegistration = context.registerService(SubscribeService.class.getName(), subscribeService, null);
                services.add(serviceRegistration);
            }
        } else {

        }
    }

    public void stop(BundleContext context) throws Exception {
        for (ServiceRegistration serviceRegistration : services) {
            serviceRegistration.unregister();
        }
    }

    public ArrayList<CrawlerDescription> getCrawlersFromFilesystem(ConfigurationService config) {
        ArrayList<CrawlerDescription> crawlers = new ArrayList<CrawlerDescription>();
        String path = config.getProperty(PATH_PROPERTY);
        if (path == null) {
            LOG.warn(PATH_PROPERTY + " not set. Skipping crawler initialisation");
            return crawlers;
        }
        File directory = new File(path);
        File[] files = directory.listFiles();
        if (files == null) {
            LOG.warn("Could not find crawler descriptions in " + directory + ". Skipping crawler initialisation.");
            return crawlers;
        }
        for (File file : files) {
            try {
                if (file.isFile() && file.getPath().endsWith(".yml")) {
                    crawlers.add((CrawlerDescription) Yaml.load(file));
                }
            } catch (FileNotFoundException e) {
            }
        }
        return crawlers;
    }

}
