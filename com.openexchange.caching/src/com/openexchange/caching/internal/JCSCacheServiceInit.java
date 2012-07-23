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

package com.openexchange.caching.internal;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.jcs.access.exception.CacheException;
import org.apache.jcs.engine.control.CompositeCacheConfigurator;
import org.apache.jcs.engine.control.CompositeCacheManager;
import com.openexchange.caching.CacheExceptionCode;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;

/**
 * {@link JCSCacheServiceInit} - Initialization for {@link JCSCache}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JCSCacheServiceInit {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(JCSCacheServiceInit.class));

    private static final String PROP_CACHE_CONF_FILE_NAME = "com.openexchange.caching.configfile";

    private final static String DEFAULT_REGION = "jcs.default";

    private static final String REGION_PREFIX = "jcs.region.";

    private static final String AUX_PREFIX = "jcs.auxiliary.";

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
     * The list of default auxiliary names.
     */
    private final Set<String> auxiliaryNames;

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
     * Initializes a new {@link JCSCacheServiceInit}
     */
    private JCSCacheServiceInit() {
        super();
        started = new AtomicBoolean();
        auxiliaryNames = new HashSet<String>(4);
    }

    private static Properties loadProperties(final String cacheConfigFile) throws OXException {
        try {
            return loadProperties(new FileInputStream(cacheConfigFile));
        } catch (final FileNotFoundException e) {
            throw CacheExceptionCode.MISSING_CACHE_CONFIG_FILE.create(e, cacheConfigFile);
        }
    }

    private static Properties loadProperties(final InputStream in) throws OXException {
        final Properties props = new Properties();
        try {
            props.load(in);
        } catch (final IOException e) {
            throw CacheExceptionCode.IO_ERROR.create(e, e.getMessage());
        } finally {
            try {
                in.close();
            } catch (final IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        return props;
    }

    private void configure(final Properties properties) throws OXException {
        synchronized (ccmInstance) {
            if (null == props) {
                /*
                 * This should be the initial configuration with cache.ccf
                 */
                props = properties;
                checkDefaultAuxiliary();
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
                        LOG.warn(new StringBuilder("Ignoring default cache configuration property: ").append(key).append('=').append(value).toString());
                    } else if (overwritesExisting(key)) {
                        LOG.warn(new StringBuilder("Ignoring overwriting existing cache configuration property: ").append(key).append('=').append(
                            value).toString());
                    } else if (props.containsKey(key)) {
                        LOG.warn(new StringBuilder("Ignoring overwriting existing cache configuration property: ").append(key).append('=').append(
                            value).toString());
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
        LOG.info("JCS caching system successfully configured with property file: " + cacheConfigFile);
    }

    /**
     * Loads the cache configuration from given input stream.
     *
     * @param inputStream The input stream
     * @throws OXException If configuration of JCS caching system fails
     */
    public void loadConfiguration(final InputStream inputStream) throws OXException {
        initializeCompositeCacheManager(true);
        configure(loadProperties(inputStream));
        LOG.info("JCS caching system successfully configured with properties from input stream.");
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
        } catch (final OXException e) {
            // Cannot occur
            LOG.error(e.getMessage(), e);
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
            LOG.warn(ce.getMessage(), ce);
            return;
        }
        try {
            final Properties properties = loadProperties(new FileInputStream(configurationService.getFileByName(cacheConfigFileName)));
            initializeCompositeCacheManager(obtainMutex);
            configure(properties);
            checkDefaultAuxiliary();
            defaultCacheRegions = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(ccmInstance.getCacheNames())));
        } catch (final IOException e) {
            throw CacheExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Checks presence of default auxiliary
     *
     * @throws CacheException If default auxiliary is missing
     */
    private void checkDefaultAuxiliary() throws OXException {
        /*
         * Ensure an auxiliary cache is present
         */
        final String value = props.getProperty(DEFAULT_REGION);
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

}
