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

package com.openexchange.subscribe.crawler.osgi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.yaml.snakeyaml.Yaml;
import com.openexchange.config.ConfigurationService;
import com.openexchange.contact.vcard.VCardService;
import com.openexchange.data.conversion.ical.ICalParser;
import com.openexchange.java.Streams;
import com.openexchange.subscribe.SubscribeService;
import com.openexchange.subscribe.crawler.CrawlerBlacklister;
import com.openexchange.subscribe.crawler.CrawlerDescription;
import com.openexchange.subscribe.crawler.internal.GenericSubscribeService;

/**
 * {@link CrawlersActivator}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CrawlersActivator implements BundleActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CrawlersActivator.class);

    public static final String DIR_NAME_PROPERTY = "com.openexchange.subscribe.crawler.path";

    public static final int CRAWLER_API_VERSION = 620;

    // -------------------------------------------------------------------------------------------------------------------------- //

    private BundleContext bundleContext;

    private volatile Stack<ServiceTracker<?,?>> trackers;
    private final Map<String, ServiceRegistration<?>> activeServices = new HashMap<String, ServiceRegistration<?>>(16);
    private final Map<String, ServiceRegistration<?>> services = new HashMap<String, ServiceRegistration<?>>(16);
    private final Set<String> blacklistedCrawlerIds = new HashSet<String>(6);

    // This assures that every time the server/bundle is restarted it will check for updates
    private final AtomicLong lastTimeChecked = new AtomicLong(0L);

    private final AtomicReference<ICalParser> iCalParserRef = new AtomicReference<ICalParser>(null);
    private final AtomicReference<VCardService> vCardServiceRef = new AtomicReference<VCardService>(null);
    private final AtomicReference<ConfigurationService> configServiceRef = new AtomicReference<ConfigurationService>(null);

    @Override
    public void start(final BundleContext context) throws Exception {
        bundleContext = context;

        final Stack<ServiceTracker<?,?>> trackers = new Stack<ServiceTracker<?,?>>();
        this.trackers = trackers;
        trackers.push(new ServiceTracker<CrawlerBlacklister, CrawlerBlacklister>(context, CrawlerBlacklister.class, new CrawlerBlacklisterTracker(context, this)));
        trackers.push(new ServiceTracker<ConfigurationService,ConfigurationService>(context, ConfigurationService.class, new CrawlerRegisterer(context, this)));
        trackers.push(new ServiceTracker<ICalParser, ICalParser>(context, ICalParser.class, new ICalParserRegisterer(context, this)));

        for (final ServiceTracker<?,?> tracker : trackers) {
            tracker.open();
        }
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        final Stack<ServiceTracker<?,?>> trackers = this.trackers;
        if (null != trackers) {
            while (!trackers.isEmpty()) {
                trackers.pop().close();
            }
            this.trackers = null;
        }
        bundleContext = null;
    }

    public synchronized List<CrawlerDescription> getCrawlersFromFilesystem(final ConfigurationService config) {
        final List<CrawlerDescription> crawlers = new LinkedList<CrawlerDescription>();
        String dirName = config.getProperty(DIR_NAME_PROPERTY);
        File directory = config.getDirectory(dirName);
        if (directory == null) {
            LOG.warn(DIR_NAME_PROPERTY + " not set or crawler configuration directory not found. Skipping crawler initialisation");
            return crawlers;
        }
        final File[] files = directory.listFiles();
        if (files == null) {
            LOG.warn("Could not find crawler descriptions in {}. Skipping crawler initialisation.", directory);
            return crawlers;
        }
        LOG.info("Loading crawler descriptions from directory : {}", directory.getName());
        for (final File file : files) {
            try {
                if (file.isFile() && file.getPath().endsWith(".yml")) {
                    Reader reader = null;
                    try {
                        reader = new FileReader(file);
                        Yaml yaml = new Yaml();
                        final CrawlerDescription crawlerDescription = yaml.loadAs(reader, CrawlerDescription.class);
                        // Only add if not explicitly disabled as per file 'crawler.properties'
                        if (config.getBoolProperty(crawlerDescription.getId(), true)) {
                            crawlers.add(crawlerDescription);
                        } else {
                            LOG.info("Ignoring crawler description \"{}\" as per 'crawler.properties' file.", crawlerDescription.getId());
                        }
                    } finally {
                        Streams.close(reader);
                    }
                }
            } catch (final FileNotFoundException e) {
                // Should not appear because file existence is checked before.
            }
        }
        return crawlers;
    }

    public synchronized boolean removeCrawlerFromFilesystem(final ConfigurationService config, final String crawlerIdToDelete) {
        final String dirName = config.getProperty(DIR_NAME_PROPERTY);
        if (dirName != null) {
            final File directory = config.getDirectory(dirName);
            final File[] files = directory.listFiles();
            if (files != null) {
                for (final File file : files) {
                    try {
                        if (file.isFile() && file.getPath().endsWith(".yml")) {
                            Reader reader = null;
                            try {
                                reader = new FileReader(file);
                                Yaml yaml = new Yaml();
                                CrawlerDescription crawler = yaml.loadAs(reader, CrawlerDescription.class);
                                if (crawler.getId().equals(crawlerIdToDelete)) {
                                    return file.delete();
                                }
                            } finally {
                                Streams.close(reader);
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

    /**
     * Registers all crawlers retrievable from given configuration.
     *
     * @param config The configuration service
     */
    public synchronized void registerServices(final ConfigurationService config) {
        if (config != null) {
            final List<CrawlerDescription> crawlers = getCrawlersFromFilesystem(config);
            for (final CrawlerDescription crawler : crawlers) {
                final String crawlerId = crawler.getId();
                if (false == blacklistedCrawlerIds.contains(crawlerId) && Boolean.parseBoolean(config.getProperty(crawlerId))) {
                    final GenericSubscribeService subscribeService = generateSubscribeService(crawler);
                    final ServiceRegistration<SubscribeService> serviceRegistration = bundleContext.registerService(SubscribeService.class, subscribeService, null);
                    services.put(crawlerId, serviceRegistration);
                    activeServices.put(crawlerId, serviceRegistration);
                    LOG.info("Crawler {} was started.", crawlerId);
                }
            }
        }
    }

    /**
     * Unregisters all formerly registered crawlers.
     */
    public synchronized void unregisterServices() {
        for (final Map.Entry<String, ServiceRegistration<?>> serviceRegistrationEntry : services.entrySet()) {
            serviceRegistrationEntry.getValue().unregister();
            activeServices.remove(serviceRegistrationEntry.getKey());
        }
        services.clear();
        activeServices.clear();
        blacklistedCrawlerIds.clear();
    }

    /**
     * Restarts denoted crawler.
     *
     * @param crawlerIdToUpdate The identifier of the crawler to restart
     * @param config The configuration service
     */
    public synchronized void restartSingleCrawler(final String crawlerIdToUpdate, final ConfigurationService config) {
        // only activate the crawler if it is configured in crawler.properties
        if (false == blacklistedCrawlerIds.contains(crawlerIdToUpdate) && Boolean.parseBoolean(config.getProperty(crawlerIdToUpdate))) {
            ServiceRegistration<?> serviceRegistration = activeServices.remove(crawlerIdToUpdate);
            if (serviceRegistration != null) {
                serviceRegistration.unregister();
                services.remove(crawlerIdToUpdate);
            }
            for (final CrawlerDescription crawler : getCrawlersFromFilesystem(config)) {
                if (crawler.getId().equals(crawlerIdToUpdate)) {
                    final GenericSubscribeService subscribeService = generateSubscribeService(crawler);
                    serviceRegistration = bundleContext.registerService(SubscribeService.class.getName(), subscribeService, null);
                    services.put(crawlerIdToUpdate, serviceRegistration);
                    activeServices.put(crawlerIdToUpdate, serviceRegistration);
                    LOG.info("Crawler {} was restarted.", crawlerIdToUpdate);
                    break;
                }
            }
        } else {
            LOG.error("Crawler {} is not activated via config-file so it will not be (re)started.", crawlerIdToUpdate);
        }
    }

    /**
     * Puts denoted crawler in black-listed crawlers.
     *
     * @param crawlerId The identifier of the crawler
     * @return <code>true</code> if added to black-list; otherwise <code>false</code> if already contained
     */
    public synchronized boolean blacklistSingleCrawler(final String crawlerId) {
        final boolean added = blacklistedCrawlerIds.add(crawlerId);
        if (added) {
            ServiceRegistration<?> serviceRegistration = activeServices.remove(crawlerId);
            if (serviceRegistration != null) {
                serviceRegistration.unregister();
                services.remove(crawlerId);
            }
        }
        return added;
    }

    /**
     * Removes denoted crawler from black-listed crawlers.
     *
     * @param crawlerId The identifier of the crawler
     */
    public synchronized void unblacklistSingleCrawler(final String crawlerId) {
        final boolean removed = blacklistedCrawlerIds.remove(crawlerId);
        if (removed) {
            final ConfigurationService configuration = configServiceRef.get();
            if (null != configuration) {
                restartSingleCrawler(crawlerId, configuration);
            }
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

    public void setVCardService(VCardService vCardService) {
        vCardServiceRef.set(vCardService);
    }

    public VCardService getVCardService() {
        return vCardServiceRef.get();
    }

    /**
     * Set the configuration service
     *
     * @param configurationService The service to set or <code>null</code> to drop
     */
    public void setConfigurationService(final ConfigurationService configurationService) {
        configServiceRef.set(configurationService);
    }

    private GenericSubscribeService generateSubscribeService(final CrawlerDescription crawler) {
        final GenericSubscribeService subscribeService = new GenericSubscribeService(
            crawler.getDisplayName(),
            crawler.getId(),
            crawler.getModule(),
            crawler.getWorkflowString(),
            crawler.getPriority(),
            this,
            crawler.isJavascriptEnabled());
        return subscribeService;
    }

}
