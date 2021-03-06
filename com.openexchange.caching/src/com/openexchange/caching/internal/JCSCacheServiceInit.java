/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.caching.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.jcs.access.exception.CacheException;
import org.apache.jcs.engine.control.CompositeCache;
import org.apache.jcs.engine.control.CompositeCacheConfigurator;
import org.apache.jcs.engine.control.CompositeCacheManager;
import com.openexchange.caching.CacheExceptionCode;
import com.openexchange.caching.events.CacheEventService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ConfigurationServices;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;

/**
 * {@link JCSCacheServiceInit} - Initialization for {@link JCSCache}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JCSCacheServiceInit {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(JCSCacheServiceInit.class);

    private static final String PROP_CACHE_CONF_FILE_NAME = "com.openexchange.caching.configfile";

    private final static String DEFAULT_REGION = "jcs.default";

    private static final String REGION_PREFIX = "jcs.region.";

    private static final String AUX_PREFIX = "jcs.auxiliary.";

    private static final String[] AUX_TYPES = { "LTCP", "SessionLTCP" };

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
     * The cache event service
     */
    private CacheEventService cacheEventService;

    /**
     * The cache manager instance
     */
    private CompositeCacheManager ccmInstance;

    /**
     * The cache manager's properties
     */
    private Properties props;

    /**
     * The list of default auxiliary names.
     */
    /* private final Set<String> auxiliaryNames; */

    /**
     * The auxiliary properties.
     */
    private Properties auxiliaryProps;

    /**
     * The cache configurator.
     */
    private CompositeCacheConfigurator configurator;

    /**
     * Atomic boolean to keep track of initialization status
     */
    private final AtomicBoolean started;

    /**
     * A set holding the names of default cache regions; meaning those caches which are configured through default configuration file.
     */
    private Set<String> defaultCacheRegions;

    /**
     * Holds all cache region names with their defined cache type
     */
    private final Map<String, String> cacheRegionTypes;

    /**
     * The cache type of the default region (holding the value of the "jcs.default" property)
     */
    private String defaultRegionType;

    /**
     * Initializes a new {@link JCSCacheServiceInit}
     */
    private JCSCacheServiceInit() {
        super();
        started = new AtomicBoolean();
        /* auxiliaryNames = new HashSet<String>(4); */
        cacheRegionTypes = new HashMap<String, String>();
    }

    private static Properties loadProperties(final String cacheConfigFile) throws OXException {
        try {
            return loadProperties(new FileInputStream(cacheConfigFile));
        } catch (FileNotFoundException e) {
            throw CacheExceptionCode.MISSING_CACHE_CONFIG_FILE.create(e, cacheConfigFile);
        }
    }

    private static Properties loadProperties(final InputStream in) throws OXException {
        try {
            return ConfigurationServices.loadPropertiesFrom(in, true);
        } catch (IOException e) {
            throw CacheExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    private void configure(final Properties properties) throws OXException {
        configure(properties, false);
    }

    private void configure(final Properties properties, final boolean overwrite) throws OXException {
        synchronized (ccmInstance) {
            if (null == props) {
                /*
                 * This should be the initial configuration with cache.ccf
                 */
                checkDefaultAuxiliary(properties);
                if (isEventInvalidation()) {
                    preProcessAuxiliaries(properties, AUX_TYPES);
                }
                props = properties;
                auxiliaryProps = getAuxiliaryProps(properties);
                configurator = ccmInstance.configure(props, false);
            } else {
                /*
                 * Additional caches are added here. Check that already existing caches are not touched.
                 */
                if (properties.isEmpty()) {
                    /*
                     * Nothing to do.
                     */
                    return;
                }
                if (isEventInvalidation()) {
                    preProcessAuxiliaries(properties, AUX_TYPES);
                }
                final Properties additionalProps = new Properties();
                boolean addAuxProps = false;
                for (final Entry<Object, Object> property : properties.entrySet()) {
                    final String key = (String) property.getKey();
                    final String value = (String) property.getValue();
                    /*
                     * Check if additional configuration requires any auxiliary cache.
                     */
                    addAuxProps |= checkAdditionalAuxiliary(key, value);
                    if (isDefault(key)) {
                        LOG.warn("Ignoring default cache configuration property: {}={}", key, value);
                    } else if (overwritesExisting(key)) {
                        if (overwrite) {
                            additionalProps.put(key, value);
                        } else {
                            LOG.warn("Ignoring overwriting existing cache configuration property: {}={}", key, value);
                        }
                    } else if (props.containsKey(key)) {
                        if (overwrite) {
                            additionalProps.put(key, value);
                        } else {
                            LOG.warn("Ignoring overwriting existing cache configuration property: {}={}", key, value);
                        }
                    } else {
                        additionalProps.put(key, value);
                    }
                }
                props.putAll(additionalProps);
                if (addAuxProps) {
                    additionalProps.putAll(auxiliaryProps);
                }
                configurator.doConfigureCaches(additionalProps);
                // ccmInstance.configure(additionalProps, false);
            }
        }
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

    /**
     * Gets a value indicating whether the supplied cache region was configured to use an auxiliary cache or not.
     *
     * @param cacheName The name of the cache region
     * @return <code>true</code>, if the region has an auxiliary cache, <code>false</code>, otherwise
     */
    public boolean hasAuxiliary(String cacheName) {
        String cacheType = cacheRegionTypes.get(cacheName);
        if (null == cacheType) {
            cacheType = defaultRegionType;
        }
        return contains(cacheType, AUX_TYPES);
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
     * @throws OXException If configuration of JCS caching system fails
     */
    public void loadConfiguration(final String cacheConfigFile) throws OXException {
        initializeCompositeCacheManager(true);
        configure(loadProperties(cacheConfigFile.trim()));
        LOG.info("JCS caching system successfully configured with property file: {}", cacheConfigFile);
    }

    /**
     * Loads the cache configuration from given input stream.
     *
     * @param inputStream The input stream
     * @throws OXException If configuration of JCS caching system fails
     */
    public void loadConfiguration(final InputStream inputStream, final boolean overwrite) throws OXException {
        initializeCompositeCacheManager(true);
        configure(loadProperties(inputStream), overwrite);
        LOG.info("JCS caching system successfully configured with properties from input stream.");
    }

    /**
     * Loads the cache configuration from given properties.
     *
     * @param properties The properties
     * @throws OXException If configuration of JCS caching system fails
     */
    public void loadConfiguration(final Properties properties) throws OXException {
        initializeCompositeCacheManager(true);
        configure(properties);
        LOG.info("JCS caching system successfully configured with properties from property set.");
    }

    /**
     * Loads the default cache configuration file.
     *
     * @throws OXException If configuration of JCS caching system fails
     */
    public void loadDefaultConfiguration() throws OXException {
        if (configurationService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ConfigurationService.class.getName());
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
    public void start(final ConfigurationService configurationService) throws OXException {
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
        } catch (OXException e) {
            // Cannot occur
            LOG.error("", e);
        }
    }

    private void configureByPropertyFile(final boolean errorIfNull, final boolean obtainMutex) throws OXException {
        /*
         * Check default cache configuration file defined through property
         */
        final String cacheConfigFileName = configurationService.getProperty(PROP_CACHE_CONF_FILE_NAME);
        if (cacheConfigFileName == null) {
            final OXException ce = CacheExceptionCode.MISSING_CONFIGURATION_PROPERTY.create(PROP_CACHE_CONF_FILE_NAME);
            if (errorIfNull) {
                throw ce;
            }
            LOG.warn("", ce);
            return;
        }
        try {
            File file = configurationService.getFileByName(cacheConfigFileName);
            final Properties properties = loadProperties(new FileInputStream(file));
            initializeCompositeCacheManager(obtainMutex);
            configure(properties);
            defaultCacheRegions = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(ccmInstance.getCacheNames())));
        } catch (IOException e) {
            throw CacheExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Checks presence of default auxiliary
     *
     * @throws CacheException If default auxiliary is missing
     */
    private static void checkDefaultAuxiliary(Properties properties) throws OXException {
        /*
         * Ensure an auxiliary cache is present
         */
        final String value = properties.getProperty(DEFAULT_REGION);
        if (null == value || 0 == value.length()) {
            throw CacheExceptionCode.MISSING_DEFAULT_AUX.create();
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
        // Destroy default event queue processor
        CompositeCache.elementEventQ.destroy();
        // Shutdown cache manager
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
     * Sets the cache event service to specified reference.
     *
     * @param eventService The cache event service to set
     */
    public void setCacheEventService(final CacheEventService eventService) {
        this.cacheEventService = eventService;
    }

    /**
     * Gets the cache event service
     *
     * @return The event service
     */
    public CacheEventService getCacheEventService() {
        return this.cacheEventService;
    }

    /**
     * Gets a value indicating whether remote cache invalidations should be performed using the internal cache event service or not.
     *
     * @return <code>true</code> if cache events should be performed via the cache event messaging service, <code>false</code>, otherwise
     */
    public boolean isEventInvalidation() {
        boolean def = true;
        ConfigurationService configurationService = this.configurationService;
        return null == configurationService ? def : configurationService.getBoolProperty("com.openexchange.caching.jcs.eventInvalidation", def);
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

    private boolean overwritesExisting(final String key) {
        if (key.startsWith(REGION_PREFIX) && (key.indexOf("attributes") < 0)) {
            final String regionName = key.substring(REGION_PREFIX.length());
            for (final String existingRegionName : ccmInstance.getCacheNames()) {
                if (existingRegionName.equals(regionName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkAdditionalAuxiliary(final String key, final String value) {
        /*-
         *
        if (key.startsWith(REGION_PREFIX) && (key.indexOf("attributes") < 0) && (value != null) && (value.length() > 0)) {
            throw new ElevenException(CacheErrorCode.NO_ADDITIONAL_AUX_REGION, key.substring(REGION_PREFIX.length()), value);
        }
         */
        return (key.startsWith(REGION_PREFIX) && (key.indexOf("attributes") < 0) && (value != null) && (value.length() > 0));
    }

    private static boolean isDefault(final String key) {
        return key.startsWith(DEFAULT_REGION);
    }

    private Properties getAuxiliaryProps(final Properties properties) {
        final Properties ret = new Properties();
        for (final Entry<Object, Object> property : properties.entrySet()) {
            final String key = (String) property.getKey();
            if (key.startsWith(AUX_PREFIX)) {
                ret.put(key, property.getValue());
            }
        }
        return ret;
    }

    /**
     * Pre-processes the supplied JCS properties and stores all regions with their cache type definitions in the {@link #cacheRegionTypes}
     * map, the default cache region type is stored in {@link #defaultRegionType}. Then, all cache region references to the supplied
     * auxiliaries are removed from the properties.
     *
     * @param properties The properties to pre-process
     * @param auxiliaries The name of the auxiliary caches to remove
     */
    private void preProcessAuxiliaries(Properties properties, String...auxiliaries) {
        if (null != properties) {
            List<Object> propertiesToClear = new ArrayList<Object>();
            for (Entry<Object, Object> property : properties.entrySet()) {
                /*
                 * check for cache type definition
                 */
                String key = (String)property.getKey();
                if (key.equals(DEFAULT_REGION)) {
                    /*
                     * remember original default cache region type
                     */
                    String value = (String)property.getValue();
                    LOG.debug("Original default cache region type: {}", value);
                    defaultRegionType = value;
                    if (contains(value, auxiliaries)) {
                        propertiesToClear.add(property.getKey());
                    }
                } else if (key.startsWith(REGION_PREFIX)) {
                    String regionName = key.substring(REGION_PREFIX.length());
                    if (false == regionName.contains("attributes")) {
                        /*
                         * remember original cache region type
                         */
                        String value = (String)property.getValue();
                        LOG.debug("Original cache region type for '{}': {}", regionName, value);
                        cacheRegionTypes.put(regionName, value);
                        if (contains(value, auxiliaries)) {
                            propertiesToClear.add(property.getKey());
                        }
                    }
                }
            }
            /*
             * clear properties referencing the auxiliaries
             */
            for (Object key : propertiesToClear) {
                LOG.debug("Clearing cache region type property: {}", key);
                properties.put(key, "");
            }
        }
    }

    private static boolean contains(String value, String[] array) {
        if (null != array) {
            for (String string : array) {
                if (null != string && string.equals(value)) {
                    return true;
                }
            }
        }
        return false;
    }

}
