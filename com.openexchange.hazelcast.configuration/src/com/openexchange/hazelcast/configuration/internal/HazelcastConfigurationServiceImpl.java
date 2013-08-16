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


package com.openexchange.hazelcast.configuration.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapIndexConfig;
import com.hazelcast.config.MultiMapConfig;
import com.hazelcast.config.QueueConfig;
import com.hazelcast.config.SemaphoreConfig;
import com.hazelcast.config.TopicConfig;
import com.hazelcast.impl.GroupProperties;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.PropertyFilter;
import com.openexchange.config.WildcardNamePropertyFilter;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.hazelcast.configuration.HazelcastConfigurationService;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.tools.strings.StringParser;
import com.openexchange.tools.strings.TimeSpanParser;

/**
 * {@link HazelcastConfigurationServiceImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class HazelcastConfigurationServiceImpl implements HazelcastConfigurationService {

    /** Named logger instance */
    private static final Log LOG = com.openexchange.log.Log.loggerFor(HazelcastConfigurationServiceImpl.class);

    /** Name of the subdirectory containing the hazelcast data structure properties */
    private static final String DIRECTORY_NAME = "hazelcast";

    /** The bundle context */
    private final BundleContext context;

    /**
     * Initializes a new {@link HazelcastConfigurationServiceImpl}.
     *
     * @param configService A reference to the configuration service
     */
    public HazelcastConfigurationServiceImpl(final BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public boolean isEnabled() throws OXException {
        return Services.getService(ConfigurationService.class).getBoolProperty("com.openexchange.hazelcast.enabled", true);
    }

    @Override
    public Config getConfig() throws OXException {
        ConfigurationService configService = Services.getService(ConfigurationService.class);
        Config config = new Config();
        /*
         * cluster group name
         */
        String groupName = configService.getProperty("com.openexchange.cluster.name");
        if (isEmpty(groupName)) {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create("com.openexchange.cluster.name");
        }
        /*
         * Continue Hazelcast configuration
         */
        final String lf = Strings.getLineSeparator();
        if ("ox".equalsIgnoreCase(groupName)) {
            LOG.warn(lf + "    The configuration value for \"com.openexchange.cluster.name\" has not been changed from it's default " +
                    "value \"ox\". Please do so to make this warning disappear." + lf);
        }
        String groupPassword = configService.getProperty("com.openexchange.hazelcast.group.password");
        if (false == Strings.isEmpty(groupPassword)) {
            if ("wtV6$VQk8#+3ds!a".equalsIgnoreCase(groupPassword)) {
                LOG.warn("The value 'wtV6$VQk8#+3ds!a' for 'com.openexchange.hazelcast.group.password' has not been changed from it's " +
                    "default. Please do so to restrict access to your cluster.");
            }
            config.getGroupConfig().setPassword(groupPassword);
        }
        config.setProperty(GroupProperties.PROP_MEMCACHE_ENABLED,
            configService.getProperty("com.openexchange.hazelcast.memcache.enabled", "false"));
        config.setProperty(GroupProperties.PROP_REST_ENABLED,
            configService.getProperty("com.openexchange.hazelcast.rest.enabled", "false"));
        config.setProperty(GroupProperties.PROP_SOCKET_BIND_ANY, String.valueOf(
            configService.getBoolProperty("com.openexchange.hazelcast.socket.bindAny", false)));
        /*
         * Logging system
         */
        {
            final boolean loggingEnabled = configService.getBoolProperty("com.openexchange.hazelcast.logging.enabled", true);
            if (loggingEnabled) {
                // Check if log4j is running
                boolean hasLog4J = false;
                final Bundle[] bundles = context.getBundles();
                for (int i = 0; !hasLog4J && i < bundles.length; i++) {
                    hasLog4J = ("org.apache.commons.logging.log4j".equals(bundles[i].getSymbolicName()));
                }
                if (hasLog4J) {
                    System.setProperty("hazelcast.logging.type", "log4j");
                    config.setProperty("hazelcast.logging.type", "log4j");
                }
            } else {
                System.setProperty("hazelcast.logging.type", "none");
                config.setProperty("hazelcast.logging.type", "none");
            }
        }
        /*
         * JMX
         */
        if (configService.getBoolProperty("com.openexchange.hazelcast.jmx", true)) {
            config.setProperty(GroupProperties.PROP_ENABLE_JMX, "true")
                .setProperty(GroupProperties.PROP_ENABLE_JMX_DETAILED, "true");
        }
        /*
         * IPv6 support
         */
        if (configService.getBoolProperty("com.openexchange.hazelcast.enableIPv6Support", false)) {
            config.setProperty(GroupProperties.PROP_PREFER_IPv4_STACK, "false");
        }
        /*
         * limit number of redos
         */
        config.setProperty(GroupProperties.PROP_REDO_GIVE_UP_THRESHOLD, "10");
        /*
         * configure merge run intervals
         */
        String mergeFirstRunDelay = configService.getProperty("com.openexchange.hazelcast.mergeFirstRunDelay", "120s");
        config.setProperty(GroupProperties.PROP_MERGE_FIRST_RUN_DELAY_SECONDS,
            String.valueOf(TimeSpanParser.parseTimespan(mergeFirstRunDelay).longValue() / 1000));
        String mergeRunDelay = configService.getProperty("com.openexchange.hazelcast.mergeRunDelay", "120s");
        config.setProperty(GroupProperties.PROP_MERGE_NEXT_RUN_DELAY_SECONDS,
            String.valueOf(TimeSpanParser.parseTimespan(mergeRunDelay).longValue() / 1000));
        /*
         * set interfaces
         */
        String interfaces = configService.getProperty("com.openexchange.hazelcast.interfaces");
        if (false == isEmpty(interfaces)) {
            String[] ips = Strings.splitByComma(interfaces);
            if (null != ips && 0 < ips.length) {
                config.getNetworkConfig().getInterfaces().setInterfaces(Arrays.asList(ips)).setEnabled(true);
            }
        }
        /*
         * enable TCP/IP network join for ox internal cluster discovery
         */
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getAwsConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(true).setConnectionTimeoutSeconds(10);
        /*
         * incoming / outgoing ports
         */
        config.getNetworkConfig().setPort(configService.getIntProperty("com.openexchange.hazelcast.networkConfig.port", 5701));
        config.getNetworkConfig().setPortAutoIncrement(
            configService.getBoolProperty("com.openexchange.hazelcast.networkConfig.portAutoIncrement", true));
        String[] outboundPortDefinitions = Strings.splitByComma(
            configService.getProperty("com.openexchange.hazelcast.networkConfig.outboundPortDefinitions", ""));
        if (null != outboundPortDefinitions && 0 < outboundPortDefinitions.length) {
            for (String portDefintion : outboundPortDefinitions) {
                if (false == isEmpty(portDefintion)) {
                    config.getNetworkConfig().addOutboundPortDefinition(portDefintion);
                }
            }
        }
        /*-
         * Maximum operation timeout in milliseconds if no timeout is specified for an operation. (default 300 seconds)
         *
         * hazelcast.max.operation.timeout
         */
        String maxOperationTimeout = configService.getProperty("com.openexchange.hazelcast.maxOperationTimeout", "300000");
        config.setProperty(GroupProperties.PROP_MAX_OPERATION_TIMEOUT, maxOperationTimeout);
        /*-
         * Arbitrary Hazelcast properties
         *
         * http://www.hazelcast.com/docs/2.3/manual/multi_html/ch12s06.html
         */
        final PropertyFilter filter = new WildcardNamePropertyFilter("hazelcast.*");
        final Map<String, String> properties = configService.getProperties(filter);
        if (null != properties && !properties.isEmpty()) {
            for (final Entry<String, String> entry : properties.entrySet()) {
                final String value = entry.getValue();
                if (!isEmpty(value)) {
                    config.setProperty(entry.getKey(), value.trim());
                }
            }
        }
        /*
         * data structure configs
         */
        applyDataStructures(config, listPropertyFiles());
        return config;
    }

    private static void applyDataStructures(Config config, File[] propertyFiles) throws OXException {
        if (null != propertyFiles && 0 < propertyFiles.length) {
            for (File file : propertyFiles) {
                applyDataStructure(config, loadProperties(file));
            }
        }
    }

    private static void applyDataStructure(Config config, Properties properties) throws OXException {
        if (null != properties && 0 < properties.size()) {
            String propertyName = (String)properties.keys().nextElement();
            if (propertyName.startsWith("com.openexchange.hazelcast.configuration.map")) {
                MapConfig mapConfig = createDataConfig(properties, MapConfig.class);
                String attributes = properties.getProperty("com.openexchange.hazelcast.configuration.map.indexes.attributes");
                if (false == isEmpty(attributes)) {
                    String[] attrs = attributes.split(" *, *");
                    if (null != attrs && 0 < attrs.length) {
                        for (String attribute : attrs) {
                            mapConfig.addMapIndexConfig(new MapIndexConfig(attribute, false));
                        }
                    }
                }
                String orderedAttributes = properties.getProperty(
                    "com.openexchange.hazelcast.configuration.map.indexes.orderedAttributes");
                if (false == isEmpty(orderedAttributes)) {
                    String[] attrs = orderedAttributes.split(" *, *");
                    if (null != attrs && 0 < attrs.length) {
                        for (String attribute : attrs) {
                            mapConfig.addMapIndexConfig(new MapIndexConfig(attribute, true));
                        }
                    }
                }
                config.addMapConfig(mapConfig);
            } else if (propertyName.startsWith("com.openexchange.hazelcast.configuration.topic")) {
                config.addTopicConfig(createDataConfig(properties, TopicConfig.class));
            } else if (propertyName.startsWith("com.openexchange.hazelcast.configuration.queue")) {
                config.addQueueConfig(createDataConfig(properties, QueueConfig.class));
            } else if (propertyName.startsWith("com.openexchange.hazelcast.configuration.multimap")) {
                config.addMultiMapConfig(createDataConfig(properties, MultiMapConfig.class));
            } else if (propertyName.startsWith("com.openexchange.hazelcast.configuration.semaphore")) {
                config.addSemaphoreConfig(createDataConfig(properties, SemaphoreConfig.class));
            }
        }
    }

    private static <T> T createDataConfig(Properties properties, Class<T> dataConfigType) throws OXException {
        T dataConfig = null;
        try {
            dataConfig = dataConfigType.newInstance();
        } catch (InstantiationException e) {
            throw ConfigurationExceptionCodes.CLASS_NOT_FOUND.create(e, dataConfigType.toString());
        } catch (IllegalAccessException e) {
            throw ConfigurationExceptionCodes.CLASS_NOT_FOUND.create(e, dataConfigType.toString());
        }
        StringParser stringParser = Services.getService(StringParser.class);
        Field[] declaredFields = dataConfigType.getDeclaredFields();
        for (String propertyName : properties.stringPropertyNames()) {
            Field field = findMatching(declaredFields, propertyName);
            if (null != field) {
                try {
                    field.setAccessible(true);
                    field.set(dataConfig, stringParser.parse(properties.getProperty(propertyName), field.getType()));
                } catch (SecurityException e) {
                    LOG.warn("Unable to set field for '" + propertyName + "'", e);
                } catch (IllegalArgumentException e) {
                    throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create(e, propertyName);
                } catch (IllegalAccessException e) {
                    LOG.warn("Unable to set field for '" + propertyName + "'", e);
                }
            } else {
                LOG.debug("No matching field found for '" + propertyName + "', skipping.");
            }
        }
        return dataConfig;
    }

    private static Field findMatching(Field[] declaredFields, String propertyName) {
        if (null != declaredFields && 0 < declaredFields.length) {
            int idx = propertyName.lastIndexOf('.');
            String fieldName = -1 != idx && propertyName.length() > idx ? propertyName.substring(1 + idx) : propertyName;
            for (Field field : declaredFields) {
                if (field.getName().equalsIgnoreCase(fieldName)) {
                    return field;
                }
            }
        }
        return null;
    }

    private static File[] listPropertyFiles() throws OXException {
        File directory = Services.getService(ConfigurationService.class).getDirectory(DIRECTORY_NAME);
        if (null == directory) {
            return new File[0];
        }
        return directory.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return null != name && name.toLowerCase().endsWith(".properties");
            }
        });
    }

    private static Properties loadProperties(File file) throws OXException {
        Properties properties = new Properties();
        FileInputStream in = null;
        try {
            properties.load(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw ConfigurationExceptionCodes.READ_ERROR.create(file);
        } catch (IOException e) {
            throw ConfigurationExceptionCodes.READ_ERROR.create(file);
        } finally {
            Streams.close(in);
        }
        return properties;
    }

    private static boolean isEmpty(String string) {
        if (null == string) {
            return true;
        }
        for (int i = 0; i < string.length(); i++) {
            if (false == Strings.isWhitespace(string.charAt(i))) {
                return false;
            }
        }
        return true;
    }

}
