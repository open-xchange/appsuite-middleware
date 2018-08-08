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

package com.openexchange.database.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import com.openexchange.config.ConfigurationService;
import com.openexchange.database.DBPoolingExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.pooling.ExhaustedActions;
import com.openexchange.pooling.PoolConfig;

/**
 * Contains the settings to connect to the configuration database as well as generic JDBC properties to use.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Configuration {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Configuration.class);

    private static final String CONFIG_DB_FILENAME = "configdb.properties";

    private final AtomicReference<Properties> propsReference = new AtomicReference<Properties>();

    private static final String JDBC_CONFIG = "dbconnector.yaml";

    private final AtomicReference<Properties> jdbcPropsReference = new AtomicReference<Properties>();

    private final Properties configDbReadProps = new Properties();

    private final Properties configDbWriteProps = new Properties();

    private final PoolConfig poolConfig = PoolConfig.DEFAULT_CONFIG.clone();

    public Configuration() {
        super();
    }

    String getReadUrl() {
        return getProperty(Property.READ_URL);
    }

    Properties getConfigDbReadProps() {
        return configDbReadProps;
    }

    String getWriteUrl() {
        return getProperty(Property.WRITE_URL);
    }

    Properties getConfigDbWriteProps() {
        return configDbWriteProps;
    }

    Properties getJdbcProps() {
        return jdbcPropsReference.get();
    }

    private String getProperty(final Property property) {
        return getProperty(property, null);
    }

    private interface Convert<T> {

        T convert(String toConvert);
    }

    private <T> T getUniversal(final Property property, final T def, final Convert<T> converter) {
        final T retval;
        Properties props = propsReference.get();
        if (props != null && props.containsKey(property.getPropertyName())) {
            retval = converter.convert(props.getProperty(property.getPropertyName()));
        } else {
            retval = def;
        }
        return retval;
    }

    String getProperty(final Property property, final String def) {
        return getUniversal(property, def, new Convert<String>() {

            @Override
            public String convert(final String toConvert) {
                return toConvert;
            }
        });
    }

    int getInt(final Property property, final int def) {
        return getUniversal(property, Integer.valueOf(def), new Convert<Integer>() {

            @Override
            public Integer convert(final String toConvert) {
                return Integer.valueOf(toConvert);
            }
        }).intValue();
    }

    long getLong(final Property property, final long def) {
        return getUniversal(property, Long.valueOf(def), new Convert<Long>() {

            @Override
            public Long convert(final String toConvert) {
                return Long.valueOf(toConvert);
            }
        }).longValue();
    }

    boolean getBoolean(final Property property, final boolean def) {
        return getUniversal(property, Boolean.valueOf(def), new Convert<Boolean>() {

            @Override
            public Boolean convert(final String toConvert) {
                return Boolean.valueOf(toConvert);
            }
        }).booleanValue();
    }

    public void readConfiguration(final ConfigurationService service) throws OXException {
        Properties props = propsReference.get();
        if (null != props) {
            throw DBPoolingExceptionCodes.ALREADY_INITIALIZED.create(this.getClass().getName());
        }
        props = service.getFile(CONFIG_DB_FILENAME);
        if (props.isEmpty()) {
            throw DBPoolingExceptionCodes.MISSING_CONFIGURATION.create();
        }
        propsReference.set(props);
        readJdbcProps(service);
        separateReadWrite();
        loadDrivers();
        initPoolConfig();
    }

    private void readJdbcProps(ConfigurationService config) {
        Properties jdbcProps = new Properties();

        // Set defaults:
        jdbcProps.setProperty("useUnicode", "true");
        jdbcProps.setProperty("characterEncoding", "UTF-8");
        jdbcProps.setProperty("autoReconnect", "false");
        jdbcProps.setProperty("useServerPrepStmts", "false");
        jdbcProps.setProperty("useTimezone", "true");
        jdbcProps.setProperty("serverTimezone", "UTC");
        jdbcProps.setProperty("connectTimeout", "15000");
        jdbcProps.setProperty("socketTimeout", "15000");
        jdbcProps.setProperty("useSSL", "false");

        // Apply config
        jdbcProps.putAll(parseJdbcYaml(config));
        jdbcPropsReference.set(jdbcProps);
    }

    private Map<String, String> parseJdbcYaml(ConfigurationService config) {
        Object yaml = config.getYaml(JDBC_CONFIG);
        if (yaml == null) {
            return Collections.emptyMap();
        }

        if (!Map.class.isInstance(yaml)) {
            LOG.error("Can't parse connector configuration file: {}", JDBC_CONFIG);
            return Collections.emptyMap();
        }

        Map<String, Object> jdbcConfig = (Map<String, Object>) yaml;
        if (!jdbcConfig.containsKey("com.mysql.jdbc")) {
            LOG.error("Can't parse connector configuration file: {}", JDBC_CONFIG);
            return Collections.emptyMap();
        }

        if (!Map.class.isInstance(jdbcConfig.get("com.mysql.jdbc"))) {
            LOG.error("Can't parse connector configuration file: {}", JDBC_CONFIG);
            return Collections.emptyMap();
        }

        Map<String, Object> map = (Map<String, Object>) jdbcConfig.get("com.mysql.jdbc");

        // Enforce Strings
        return map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));
    }

    private void separateReadWrite() {
        Properties jdbcProps = jdbcPropsReference.get();
        configDbReadProps.putAll(jdbcProps);
        configDbWriteProps.putAll(jdbcProps);
        Properties props = propsReference.get();
        for (final Object tmp : props.keySet()) {
            final String key = (String) tmp;
            if (key.startsWith("readProperty.")) {
                final String value = props.getProperty(key);
                final int equalSignPos = value.indexOf('=');
                final String readKey = value.substring(0, equalSignPos);
                final String readValue = value.substring(equalSignPos + 1);
                configDbReadProps.put(readKey, readValue);
            } else if (key.startsWith("writeProperty.")) {
                final String value = props.getProperty(key);
                final int equalSignPos = value.indexOf('=');
                final String readKey = value.substring(0, equalSignPos);
                final String readValue = value.substring(equalSignPos + 1);
                configDbWriteProps.put(readKey, readValue);
            }
        }
    }

    private void loadDrivers() throws OXException {
        final String readDriverClass = getProperty(Property.READ_DRIVER_CLASS);
        if (null == readDriverClass) {
            throw DBPoolingExceptionCodes.PROPERTY_MISSING.create(Property.READ_DRIVER_CLASS.getPropertyName());
        }
        try {
            Class.forName(readDriverClass);
        } catch (final ClassNotFoundException e) {
            throw DBPoolingExceptionCodes.NO_DRIVER.create(e, readDriverClass);
        }
        final String writeDriverClass = getProperty(Property.WRITE_DRIVER_CLASS);
        if (null == writeDriverClass) {
            throw DBPoolingExceptionCodes.PROPERTY_MISSING.create(Property.WRITE_DRIVER_CLASS.getPropertyName());
        }
        try {
            Class.forName(writeDriverClass);
        } catch (final ClassNotFoundException e) {
            throw DBPoolingExceptionCodes.NO_DRIVER.create(e, writeDriverClass);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void clear() {
        propsReference.set(null);
        jdbcPropsReference.set(null);
        configDbReadProps.clear();
        configDbWriteProps.clear();
    }

    /**
     * Reads the pooling configuration from the configdb.properties file.
     */
    private void initPoolConfig() {
        poolConfig.maxIdle = getInt(Property.MAX_IDLE, poolConfig.maxIdle);
        poolConfig.maxIdleTime = getLong(Property.MAX_IDLE_TIME, poolConfig.maxIdleTime);
        poolConfig.maxActive = getInt(Property.MAX_ACTIVE, poolConfig.maxActive);
        poolConfig.maxWait = getLong(Property.MAX_WAIT, poolConfig.maxWait);
        poolConfig.maxLifeTime = getLong(Property.MAX_LIFE_TIME, poolConfig.maxLifeTime);
        poolConfig.exhaustedAction = ExhaustedActions.valueOf(getProperty(Property.EXHAUSTED_ACTION, poolConfig.exhaustedAction.name()));
        poolConfig.testOnActivate = getBoolean(Property.TEST_ON_ACTIVATE, poolConfig.testOnActivate);
        poolConfig.testOnDeactivate = getBoolean(Property.TEST_ON_DEACTIVATE, poolConfig.testOnDeactivate);
        poolConfig.testOnIdle = getBoolean(Property.TEST_ON_IDLE, poolConfig.testOnIdle);
        poolConfig.testThreads = getBoolean(Property.TEST_THREADS, poolConfig.testThreads);

        List<Object> logArgs = new ArrayList<>(24);
        logArgs.add(Strings.getLineSeparator());
        logArgs.add(poolConfig.maxIdle);
        logArgs.add(Strings.getLineSeparator());
        logArgs.add(poolConfig.maxIdleTime);
        logArgs.add(Strings.getLineSeparator());
        logArgs.add(poolConfig.maxActive);
        logArgs.add(Strings.getLineSeparator());
        logArgs.add(poolConfig.maxWait);
        logArgs.add(Strings.getLineSeparator());
        logArgs.add(poolConfig.maxLifeTime);
        logArgs.add(Strings.getLineSeparator());
        logArgs.add(poolConfig.exhaustedAction);
        logArgs.add(Strings.getLineSeparator());
        logArgs.add(poolConfig.testOnActivate);
        logArgs.add(Strings.getLineSeparator());
        logArgs.add(poolConfig.testOnDeactivate);
        logArgs.add(Strings.getLineSeparator());
        logArgs.add(poolConfig.testOnIdle);
        logArgs.add(Strings.getLineSeparator());
        logArgs.add(poolConfig.testThreads);
        // @formatter:off
        LOG.info("Database pooling options:" +
            "{}    Maximum idle connections: {}" +
            "{}    Maximum idle time: {}ms" +
            "{}    Maximum active connections: {}" +
            "{}    Maximum wait time for a connection: {}ms" +
            "{}    Maximum life time of a connection: {}ms" +
            "{}    Action if connections exhausted: {}" +
            "{}    Test connections on activate: {}" +
            "{}    Test connections on deactivate: {}" +
            "{}    Test idle connections: {}" +
            "{}    Test threads for bad connection usage (SLOW): {}",
            logArgs.toArray(new Object[logArgs.size()])
         // @formatter:on
        );
    }


    PoolConfig getPoolConfig() {
        return poolConfig;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        Properties jdbcProps = jdbcPropsReference.get();
        result = prime * result + ((jdbcProps == null) ? 0 : jdbcProps.hashCode());
        result = prime * result + ((poolConfig == null) ? 0 : poolConfig.hashCode());
        Properties props = propsReference.get();
        result = prime * result + ((props == null) ? 0 : props.hashCode());
        result = prime * result + ((configDbReadProps == null) ? 0 : configDbReadProps.hashCode());
        result = prime * result + ((configDbWriteProps == null) ? 0 : configDbWriteProps.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Configuration other = (Configuration) obj;
        Properties jdbcProps = jdbcPropsReference.get();
        Properties otherJdbcProps = other.jdbcPropsReference.get();
        if (jdbcProps == null) {
            if (otherJdbcProps != null) {
                return false;
            }
        } else if (!jdbcProps.equals(otherJdbcProps)) {
            return false;
        }
        if (poolConfig == null) {
            if (other.poolConfig != null) {
                return false;
            }
        } else if (!poolConfig.equals(other.poolConfig)) {
            return false;
        }
        Properties props = propsReference.get();
        Properties otherProps = other.propsReference.get();
        if (props == null) {
            if (otherProps != null) {
                return false;
            }
        } else if (!ConfigurationUtil.matches(props, otherProps)) {
            return false;
        }
        if (configDbReadProps == null) {
            if (other.configDbReadProps != null) {
                return false;
            }
        } else if (!ConfigurationUtil.matches(configDbReadProps, other.configDbReadProps)) {
            return false;
        }
        if (configDbWriteProps == null) {
            if (other.configDbWriteProps != null) {
                return false;
            }
        } else if (!ConfigurationUtil.matches(configDbWriteProps, other.configDbWriteProps)) {
            return false;
        }
        return true;
    }

    /**
     * Enumeration of all properties in the configdb.properties file.
     */
    public static enum Property {
        /** URL for configdb read. */
        READ_URL("readUrl"),
        /** URL for configdb write. */
        WRITE_URL("writeUrl"),
        /** Class name of driver for configdb read. */
        READ_DRIVER_CLASS("readDriverClass"),
        /** Class name of driver for configdb write. */
        WRITE_DRIVER_CLASS("writeDriverClass"),
        /** Interval of the cleaner threads. */
        CLEANER_INTERVAL("cleanerInterval"),
        /** Maximum of idle connections. */
        MAX_IDLE("maxIdle"),
        /** Maximum idle time. */
        MAX_IDLE_TIME("maxIdleTime"),
        /** Maximum of active connections. */
        MAX_ACTIVE("maxActive"),
        /** Maximum time to wait for a connection. */
        MAX_WAIT("maxWait"),
        /** Maximum life time of a connection. */
        MAX_LIFE_TIME("maxLifeTime"),
        /** Action if the maximum is reached. */
        EXHAUSTED_ACTION("exhaustedAction"),
        /** Validate connections if they are activated. */
        TEST_ON_ACTIVATE("testOnActivate"),
        /** Validate connections if they are deactivated. */
        TEST_ON_DEACTIVATE("testOnDeactivate"),
        /** Validate connections on a pool clean run. */
        TEST_ON_IDLE("testOnIdle"),
        /** Test threads if they use connections correctly. */
        TEST_THREADS("testThreads"),
        /** Allows to disable the replication monitor. */
        REPLICATION_MONITOR("com.openexchange.database.replicationMonitor"),
        /** Allows to write a warning into the logs if a connection to the master is only used to read data. */
        CHECK_WRITE_CONS("com.openexchange.database.checkWriteCons"),
        /** Specifies the lock mechanism to use. */
        LOCK_MECH("com.openexchange.database.lockMech");

        private final String propertyName;

        private Property(String propertyName) {
            this.propertyName = propertyName;
        }

        public String getPropertyName() {
            return propertyName;
        }
    }
}
