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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.caching.internal;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.jcs.engine.control.CompositeCacheManager;
import com.openexchange.caching.CacheException;
import com.openexchange.config.ConfigurationService;
import com.openexchange.server.ServiceException;

/**
 * {@link JCSCacheServiceInit} - Initialization for {@link JCSCache}.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JCSCacheServiceInit {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(JCSCacheServiceInit.class);

    private static final String PROP_CACHE_CONF_FILE = "com.openexchange.caching.configfile";

    private final static String DEFAULT_REGION = "jcs.default";

    private static JCSCacheServiceInit SINGLETON;

    /**
     * Initializes the instance of {@link JCSCacheServiceInit}.
     */
    public static void initInstance() {
        SINGLETON = new JCSCacheServiceInit();
    }

    /**
     * Releases the instance of {@link JCSCacheServiceInit}.
     */
    public static void releaseInstance() {
        SINGLETON = null;
    }

    /**
     * Gets the singleton instance of {@link JCSCacheServiceInit}
     * 
     * @return The singleton instance of {@link JCSCacheServiceInit}
     */
    public static JCSCacheServiceInit getInstance() {
        return SINGLETON;
    }

    /**
     * The configuration service
     */
    private ConfigurationService configurationService;

    /**
     * The cache manager instance
     */
    private CompositeCacheManager ccmInstance;

    /**
     * The cache manager's properties
     */
    private Properties props;

    /**
     * Atomic boolean to keep track of initialization status
     */
    private final AtomicBoolean started;

    /**
     * A set holding the names of default cache regions; meaning those caches which are configured through default configuration file.
     */
    private Set<String> defaultCacheRegions;

    /**
     * Initializes a new {@link JCSCacheServiceInit}
     */
    private JCSCacheServiceInit() {
        super();
        started = new AtomicBoolean();
    }

    private static Properties loadProperties(final String cacheConfigFile) throws CacheException {
        try {
            return loadProperties(new FileInputStream(cacheConfigFile));
        } catch (final FileNotFoundException e) {
            throw new CacheException(CacheException.Code.MISSING_CACHE_CONFIG_FILE, e, cacheConfigFile);
        }
    }

    private static Properties loadProperties(final InputStream in) throws CacheException {
        final Properties props = new Properties();
        try {
            props.load(in);
        } catch (final IOException e) {
            throw new CacheException(CacheException.Code.IO_ERROR, e, e.getMessage());
        } finally {
            try {
                in.close();
            } catch (final IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        return props;
    }

    private void configure(final Properties props) throws CacheException {
        if (this.props == null) {
            // This should be the initial configuration with cache.ccf
            this.props = props;
            checkDefaultAuxiliary();
            ccmInstance.configure(this.props, false);
        } else {
            // Additional caches are added here. Check that already existing caches are not touched.
            Properties additionalProps = new Properties();
            for (final Entry<Object, Object> property : props.entrySet()) {
                String key = (String) property.getKey();
                String value = (String) property.getValue();
                if (isDefault(key)) {
                    LOG.warn("Ignoring default cache configuration property: " + key + '=' + value);
                } else if (overwritesExisting(key)) {
                    LOG.warn("Ignoring overwriting existing cache configuration property: " + key + '=' + value);
                } else {
                    additionalProps.put(key, value);
                }
            }
            this.props.putAll(additionalProps);
            ccmInstance.configure(additionalProps, false);
            //this.props.putAll(props);
        }
        /*
         * ... and (re-)configure composite cache manager
         */
    }

    /**
     * Delegates to {@link CompositeCacheManager#freeCache(String)}: The cache identified through given <code>cacheName</code> is removed
     * from cache manager and all of its items are going to be disposed.
     * 
     * @param cacheName The name of the cache region that ought to be freed
     */
    public void freeCache(final String cacheName) {
        if (null == ccmInstance) {
            return;
        }
        ccmInstance.freeCache(cacheName);
    }

    private void initializeCompositeCacheManager(final boolean obtainMutex) {
        if (obtainMutex) {
            synchronized (this) {
                if (null == ccmInstance) {
                    ccmInstance = CompositeCacheManager.getUnconfiguredInstance();
                }
            }
        } else {
            if (null == ccmInstance) {
                ccmInstance = CompositeCacheManager.getUnconfiguredInstance();
            }
        }
    }

    /**
     * Loads the cache configuration file denoted by specified cache configuration file.
     * 
     * @param cacheConfigFile The cache configuration file
     * @throws CacheException If configuration of JCS caching system fails
     */
    public void loadConfiguration(final String cacheConfigFile) throws CacheException {
        initializeCompositeCacheManager(true);
        configure(loadProperties(cacheConfigFile.trim()));
        LOG.info("JCS caching system successfully configured with property file: " + cacheConfigFile);
    }

    /**
     * Loads the cache configuration from given input stream.
     * 
     * @param inputStream The input stream
     * @throws CacheException If configuration of JCS caching system fails
     */
    public void loadConfiguration(final InputStream inputStream) throws CacheException {
        initializeCompositeCacheManager(true);
        configure(loadProperties(inputStream));
        LOG.info("JCS caching system successfully configured with properties from input stream.");
    }

    /**
     * Loads the default cache configuration file.
     * 
     * @throws CacheException If configuration of JCS caching system fails
     */
    public void loadDefaultConfiguration() throws CacheException {
        if (configurationService == null) {
            throw new CacheException(new ServiceException(ServiceException.Code.SERVICE_UNAVAILABLE, ConfigurationService.class.getName()));
        }
        configureByPropertyFile(true, true);
        LOG.info("JCS caching system successfully re-configured.");
    }

    /**
     * Starts the JCS caching system.
     * 
     * @param configurationService The configuration service
     * @throws CacheException If configuration of JCS caching system fails
     */
    public void start(final ConfigurationService configurationService) throws CacheException {
        if (!started.compareAndSet(false, true)) {
            LOG.error("JCS cache service has already been started. Start-up canceled.");
        }
        this.configurationService = configurationService;
        /*
         * Configure by property file
         */
        configureByPropertyFile(true, false);
        LOG.info("JCS caching system successfully started");
    }

    /**
     * Re-Configure JCS caching system with newly read property file.
     */
    public void reconfigureByPropertyFile() {
        try {
            configureByPropertyFile(false, true);
        } catch (final CacheException e) {
            // Cannot occur
            LOG.error(e.getMessage(), e);
        }
    }

    private void configureByPropertyFile(final boolean errorIfNull, final boolean obtainMutex) throws CacheException {
        /*
         * Check default cache configuration file defined through property
         */
        final String cacheConfigFile = configurationService.getProperty(PROP_CACHE_CONF_FILE);
        if (cacheConfigFile == null) {
            final CacheException ce = new CacheException(CacheException.Code.MISSING_CONFIGURATION_PROPERTY, PROP_CACHE_CONF_FILE);
            if (errorIfNull) {
                throw ce;
            }
            LOG.warn(ce.getMessage(), ce);
            return;
        }
        initializeCompositeCacheManager(obtainMutex);
        final Properties properties = loadProperties(cacheConfigFile.trim());
        configure(properties);
        checkDefaultAuxiliary();
        defaultCacheRegions = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(ccmInstance.getCacheNames())));
    }

    /**
     * Checks presence of default auxiliary
     * 
     * @throws CacheException If default auxiliary is missing
     */
    private void checkDefaultAuxiliary() throws CacheException {
        /*
         * Ensure an auxiliary cache is present
         */
        final String value = props.getProperty(DEFAULT_REGION);
        if (null == value || 0 == value.length()) {
            throw new CacheException(CacheException.Code.MISSING_DEFAULT_AUX);
        }
    }

    /**
     * Stops the JCS caching system.
     */
    public void stop() {
        if (!started.compareAndSet(true, false)) {
            LOG.error("JCS cache service has not been started before and therefore cannot be stopped.");
        }
        props = null;
        defaultCacheRegions = null;
        ccmInstance.shutDown();
        ccmInstance = null;
        LOG.info("JCS caching system successfully stopped.");
    }

    /**
     * Sets the configuration service to specified reference.
     * 
     * @param configurationService The configuration service to set
     */
    public void setConfigurationService(final ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    /**
     * Checks if specified region names is contained in default region names.
     * 
     * @param regionName The region name to check
     * @return <code>true</code> if specified region names is contained in default region names; otherwise <code>false</code>.
     */
    public boolean isDefaultCacheRegion(final String regionName) {
        return defaultCacheRegions.contains(regionName);
    }

    final Pattern regionPattern = Pattern.compile("jcs\\.region\\.([a-zA-Z]*)\\.");

    private boolean overwritesExisting(String key) {
        Matcher matcher = regionPattern.matcher(key);
        if (matcher.matches()) {
            String regionName = matcher.group(1);
            String[] existingRegionNames = ccmInstance.getCacheNames();
            for (String existingRegionName : existingRegionNames) {
                if (existingRegionName.equals(regionName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isDefault(String key) {
        return key.startsWith(DEFAULT_REGION);
    }
}
