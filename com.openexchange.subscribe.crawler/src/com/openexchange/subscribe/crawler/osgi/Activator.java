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

package com.openexchange.subscribe.crawler.osgi;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.logging.Log;
import org.ho.yaml.Yaml;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.config.ConfigurationService;
import com.openexchange.data.conversion.ical.ICalParser;
import com.openexchange.log.LogFactory;
import com.openexchange.management.ManagementService;
import com.openexchange.subscribe.SubscribeService;
import com.openexchange.subscribe.crawler.CrawlerDescription;
import com.openexchange.subscribe.crawler.internal.GenericSubscribeService;
import com.openexchange.timer.TimerService;

/**
 * {@link Activator}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class Activator implements BundleActivator {

    private ArrayList<ServiceRegistration<?>> services;

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(Activator.class));

    public static final String DIR_NAME_PROPERTY = "com.openexchange.subscribe.crawler.path";

    public static final String UPDATE_INTERVAL = "com.openexchange.subscribe.crawler.updateinterval";

    public static final String ENABLE_AUTO_UPDATE = "com.openexchange.subscribe.crawler.enableautoupdate";

    public static final String ONLY_UPDATE_INSTALLED = "com.openexchange.subscribe.crawler.onlyupdatealreadyinstalled";

    private BundleContext bundleContext;

    private final Stack<ServiceTracker<?,?>> trackers = new Stack<ServiceTracker<?,?>>();

    private final Map<String, ServiceRegistration<?>> activeServices = new HashMap<String, ServiceRegistration<?>>();

    public static final int CRAWLER_API_VERSION = 620;

    // This assures that every time the server/bundle is restarted it will check for updates
    private final AtomicLong lastTimeChecked = new AtomicLong(0L);

    private final AtomicReference<ICalParser> iCalParserRef = new AtomicReference<ICalParser>(null);

    @Override
    public void start(final BundleContext context) throws Exception {

        bundleContext = context;
        services = new ArrayList<ServiceRegistration<?>>();

        // react dynamically to the appearance/disappearance of ConfigurationService, TimerService, ManagementService and iCalParserService
        trackers.push(new ServiceTracker<ConfigurationService,ConfigurationService>(context, ConfigurationService.class, new CrawlerRegisterer(context, this)));
        final Filter filter = context.createFilter("(|(" + Constants.OBJECTCLASS + '=' + ConfigurationService.class.getName() + ")(" + Constants.OBJECTCLASS + '=' + TimerService.class.getName() + "))");
        final ServiceTracker<Object,Object> configAndTimerTracker = new ServiceTracker<Object,Object>(context, filter, new CrawlerAutoUpdater(context, this));
        trackers.push(configAndTimerTracker);
        final Filter filter2 = context.createFilter("(|(" + Constants.OBJECTCLASS + '=' + ConfigurationService.class.getName() + ")(" + Constants.OBJECTCLASS + '=' + ManagementService.class.getName() + "))");
        final ServiceTracker<Object,Object> configAndManagementTracker = new ServiceTracker<Object,Object>(context, filter2, new CrawlerMBeanRegisterer(context, this));
        trackers.push(configAndManagementTracker);
        trackers.push(new ServiceTracker<ICalParser,ICalParser>(context, ICalParser.class, new ICalParserRegisterer(context, this)));
        for (final ServiceTracker<?,?> tracker : trackers) {
            tracker.open();
        }
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        while (!trackers.isEmpty()) {
            trackers.pop().close();
        }
        bundleContext = null;
    }

    public ArrayList<CrawlerDescription> getCrawlersFromFilesystem(final ConfigurationService config) {
        final ArrayList<CrawlerDescription> crawlers = new ArrayList<CrawlerDescription>();
        String dirName = config.getProperty(DIR_NAME_PROPERTY);
        File directory = config.getDirectory(dirName);
        if (directory == null) {
            LOG.warn(DIR_NAME_PROPERTY + " not set or crawler configuration directory not found. Skipping crawler initialisation");
            return crawlers;
        }
        final File[] files = directory.listFiles();
        if (files == null) {
            LOG.warn("Could not find crawler descriptions in " + directory + ". Skipping crawler initialisation.");
            return crawlers;
        }
        LOG.info("Loading crawler descriptions from directory : " + directory.getName());
        for (final File file : files) {
            try {
                if (file.isFile() && file.getPath().endsWith(".yml")) {
                    final CrawlerDescription crawlerDescription = Yaml.loadType(file, CrawlerDescription.class);
                    // Only add if not explicitly disabled as per file 'crawler.properties'
                    if (config.getBoolProperty(crawlerDescription.getId(), true)) {
                        crawlers.add(crawlerDescription);
                    } else {
                        LOG.info("Ignoring crawler description \"" + crawlerDescription.getId() + "\" as per 'crawler.properties' file.");
                    }
                }
            } catch (final FileNotFoundException e) {
                // Should not appear because file existence is checked before.
            }
        }
        return crawlers;
    }

    public boolean removeCrawlerFromFilesystem(final ConfigurationService config, final String crawlerIdToDelete) {
        final String dirName = config.getProperty(DIR_NAME_PROPERTY);
        if (dirName != null) {
            final File directory = config.getDirectory(dirName);
            final File[] files = directory.listFiles();
            if (files != null) {
                for (final File file : files) {
                    try {
                        if (file.isFile() && file.getPath().endsWith(".yml")) {
                            final CrawlerDescription crawler = Yaml.loadType(file, CrawlerDescription.class);
                            if (crawler.getId().equals(crawlerIdToDelete)) {
                                return file.delete();
                            }
                        }
                    } catch (final FileNotFoundException e) {
                        // Should not appear because file existence is checked before.
                    }
                }
            }
        }
        return false;
    }

    public void registerServices(final ConfigurationService config) {
        if (config != null) {
            final ArrayList<CrawlerDescription> crawlers = getCrawlersFromFilesystem(config);
            for (final CrawlerDescription crawler : crawlers) {
                final GenericSubscribeService subscribeService = new GenericSubscribeService(
                    crawler.getDisplayName(),
                    crawler.getId(),
                    crawler.getModule(),
                    crawler.getWorkflowString(),
                    crawler.getPriority(),
                    this,
                    crawler.isJavascriptEnabled());
                final ServiceRegistration<SubscribeService> serviceRegistration = bundleContext.registerService(
                    SubscribeService.class,
                    subscribeService,
                    null);
                services.add(serviceRegistration);
                activeServices.put(crawler.getId(), serviceRegistration);
                LOG.info("Crawler " + crawler.getId() + " was started.");
            }
        }
    }

    public void unregisterServices() {
        for (final ServiceRegistration<?> serviceRegistration : services) {
            serviceRegistration.unregister();
        }
    }

    public void restartSingleCrawler(final String crawlerIdToUpdate, final ConfigurationService config) {
        // only activate the crawler if it is configured in crawler.properties
        if (Boolean.parseBoolean(config.getProperty(crawlerIdToUpdate))) {
            ServiceRegistration<?> serviceRegistration = activeServices.get(crawlerIdToUpdate);
            if (serviceRegistration != null) {
                serviceRegistration.unregister();
                activeServices.remove(crawlerIdToUpdate);
            }
            for (final CrawlerDescription crawler : getCrawlersFromFilesystem(config)) {
                if (crawler.getId().equals(crawlerIdToUpdate)) {
                    final GenericSubscribeService subscribeService = new GenericSubscribeService(
                        crawler.getDisplayName(),
                        crawler.getId(),
                        crawler.getModule(),
                        crawler.getWorkflowString(),
                        crawler.getPriority(),
                        this,
                        crawler.isJavascriptEnabled());
                    serviceRegistration = bundleContext.registerService(SubscribeService.class.getName(), subscribeService, null);
                    services.add(serviceRegistration);
                    activeServices.put(crawler.getId(), serviceRegistration);
                }
            }
            LOG.info("Crawler " + crawlerIdToUpdate + " was restarted.");
        } else {
            LOG.error("Crawler " + crawlerIdToUpdate + " is not activated via config-file so it will not be (re)started.");
        }
    }

    public long getLastTimeChecked() {
        return lastTimeChecked.get();
    }

    public void setLastTimeChecked(final long stamp) {
        lastTimeChecked.set(stamp);
    }

    public static int getCRAWLER_API_VERSION() {
        return CRAWLER_API_VERSION;
    }

    public void setICalParser(final ICalParser iCalParser) {
        iCalParserRef.set(iCalParser);
    }

    public ICalParser getICalParser() {
        return iCalParserRef.get();
    }

    // THESE METHODS SHOULD ONLY BE USED FOR TESTING
    public ArrayList<ServiceRegistration<?>> getServices() {
        return services;
    }

    public void setServices(final ArrayList<ServiceRegistration<?>> services) {
        this.services = services;
    }

    public void setBundleContext(final BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }
    // THESE METHODS SHOULD ONLY BE USED FOR TESTING

}
