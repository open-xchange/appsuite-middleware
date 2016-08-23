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


package com.openexchange.hazelcast.configuration.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import com.hazelcast.config.Config;
import com.hazelcast.config.ConfigLoader;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapIndexConfig;
import com.hazelcast.config.MultiMapConfig;
import com.hazelcast.config.QueueConfig;
import com.hazelcast.config.SemaphoreConfig;
import com.hazelcast.config.SymmetricEncryptionConfig;
import com.hazelcast.config.TopicConfig;
import com.hazelcast.instance.GroupProperty;
import com.hazelcast.nio.serialization.ClassDefinition;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.WildcardNamePropertyFilter;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.hazelcast.configuration.HazelcastConfigurationService;
import com.openexchange.hazelcast.serialization.DynamicPortableFactory;
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

    private static final String NETWORK_JOIN_EMPTY = "empty";
    private static final String NETWORK_JOIN_STATIC = "static";
    private static final String NETWORK_JOIN_MULTICAST = "multicast";
    private static final String NETWORK_JOIN_AWS = "aws";

    /** Named logger instance */
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(HazelcastConfigurationServiceImpl.class);

    /** Name of the subdirectory containing the hazelcast data structure properties */
    private static final String DIRECTORY_NAME = "hazelcast";

    // -----------------------------------------------------------------------------------------------------------------------------------

    /** The Hazelcast configuration instance */
    private volatile Config config;

    /**
     * Initializes a new {@link HazelcastConfigurationServiceImpl}.
     */
    public HazelcastConfigurationServiceImpl() {
        super();
    }

    @Override
    public boolean shutdownOnOutOfMemory() {
        ConfigurationService service = Services.optService(ConfigurationService.class);
        return null == service ? false : service.getBoolProperty("com.openexchange.hazelcast.shutdownOnOutOfMemory", false);
    }

    @Override
    public boolean isEnabled() throws OXException {
        return Services.requireService(ConfigurationService.class).getBoolProperty("com.openexchange.hazelcast.enabled", true);
    }

    @Override
    public Config getConfig() throws OXException {
        Config config = this.config;
        if (null == config) {
            synchronized (this) {
                config = this.config;
                if (null == config) {
                    config = loadConfig();
                    this.config = config;
                }
            }
        }
        return config;
    }

    @Override
    public String discoverMapName(String namePrefix) throws OXException {
        Map<String, MapConfig> mapConfigs = getConfig().getMapConfigs();
        if (null != mapConfigs && 0 < mapConfigs.size()) {
            for (String mapName : mapConfigs.keySet()) {
                if (mapName.startsWith(namePrefix)) {
                    LOG.info("Using distributed map '{}'.", mapName);
                    return mapName;
                }
            }
        }
        OXException exception = ConfigurationExceptionCodes.INVALID_CONFIGURATION.create(
            "No distributed map matching prefix '" + namePrefix + "'  found in hazelcast configuration");
        LOG.warn("", exception);
        throw exception;
    }

    /**
     * Gets the Hazelcast cofiguration.
     *
     * @return The Hazelcast cofiguration or <code>null</code>
     */
    public Config getConfigDirect() {
        return config;
    }

    // -----------------------------------------------------------------------------------------------------------------------------------

    /**
     * Loads and configures the hazelcast configuration.
     *
     * @return The config
     */
    private static Config loadConfig() throws OXException {
        /*
         * Load or create default config
         */
        Config config = loadXMLConfig();
        if (null == config) {
            config = new Config();
        }
        /*
         * Set values from properties file
         */
        ConfigurationService configService = Services.requireService(ConfigurationService.class);
        /*
         * General
         */
        String join = configService.getProperty("com.openexchange.hazelcast.network.join", NETWORK_JOIN_EMPTY);
        String groupName = configService.getProperty("com.openexchange.hazelcast.group.name");
        if (false == Strings.isEmpty(groupName)) {
            config.getGroupConfig().setName(groupName);
        } else if (false == NETWORK_JOIN_EMPTY.equalsIgnoreCase(join)) {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create("com.openexchange.hazelcast.group.name");
        }
        String groupPassword = configService.getProperty("com.openexchange.hazelcast.group.password");
        if (false == Strings.isEmpty(groupPassword)) {
            if ("wtV6$VQk8#+3ds!a".equalsIgnoreCase(groupPassword)) {
                LOG.warn("The value 'wtV6$VQk8#+3ds!a' for 'com.openexchange.hazelcast.group.password' has not been changed from its "
                    + "default. Please do so to restrict access to your cluster.");
            }
            config.getGroupConfig().setPassword(groupPassword);
        }
        /*
         * Network Join
         */
        if (NETWORK_JOIN_EMPTY.equalsIgnoreCase(join)) {
            config.getNetworkConfig().getJoin().getAwsConfig().setEnabled(false);
            config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
            config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(false);
        } else if (NETWORK_JOIN_STATIC.equalsIgnoreCase(join)) {
            config.getNetworkConfig().getJoin().getAwsConfig().setEnabled(false);
            config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
            config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(true);
            config.getNetworkConfig().getJoin().getTcpIpConfig().setConnectionTimeoutSeconds(
                configService.getIntProperty("com.openexchange.hazelcast.network.join.static.connectionTimeout", 10));
            String[] members = Strings.splitByComma(
                configService.getProperty("com.openexchange.hazelcast.network.join.static.nodes"));
            if (null != members && 0 < members.length) {
                for (String member : members) {
                    if (false == Strings.isEmpty(member)) {
                        try {
                            config.getNetworkConfig().getJoin().getTcpIpConfig().addMember(InetAddress.getByName(member).getHostAddress());
                        } catch (UnknownHostException e) {
                            throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create(
                                e, "com.openexchange.hazelcast.network.join.static.nodes");
                        }
                    }
                }
            }
        } else if (NETWORK_JOIN_MULTICAST.equalsIgnoreCase(join)) {
            config.getNetworkConfig().getJoin().getAwsConfig().setEnabled(false);
            config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(false);
            config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(true);
            String group = configService.getProperty("com.openexchange.hazelcast.network.join.multicast.group", "224.2.2.3");
            int port = configService.getIntProperty("com.openexchange.hazelcast.network.join.multicast.group", 54327);
            config.getNetworkConfig().getJoin().getMulticastConfig().setMulticastGroup(group).setMulticastPort(port);
        } else if (NETWORK_JOIN_AWS.equalsIgnoreCase(join)) {
            config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
            config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(false);
            config.getNetworkConfig().getJoin().getAwsConfig().setEnabled(true);
            String accessKey = configService.getProperty("com.openexchange.hazelcast.network.join.aws.accessKey");
            if (Strings.isEmpty(accessKey)) {
                throw ConfigurationExceptionCodes.PROPERTY_MISSING.create("com.openexchange.hazelcast.network.join.aws.accessKey");
            }
            config.getNetworkConfig().getJoin().getAwsConfig().setAccessKey(accessKey);
            String secretKey = configService.getProperty("com.openexchange.hazelcast.network.join.aws.secretKey");
            if (Strings.isEmpty(secretKey)) {
                throw ConfigurationExceptionCodes.PROPERTY_MISSING.create("com.openexchange.hazelcast.network.join.aws.secretKey");
            }
            config.getNetworkConfig().getJoin().getAwsConfig().setSecretKey(secretKey);
            String region = configService.getProperty("com.openexchange.hazelcast.network.join.aws.region", "us-west-1");
            config.getNetworkConfig().getJoin().getAwsConfig().setRegion(region);
            String hostHeader = configService.getProperty("com.openexchange.hazelcast.network.join.aws.hostHeader", "ec2.amazonaws.com");
            config.getNetworkConfig().getJoin().getAwsConfig().setHostHeader(hostHeader);
            String tagKey = configService.getProperty("com.openexchange.hazelcast.network.join.aws.tagKey", "type");
            config.getNetworkConfig().getJoin().getAwsConfig().setTagKey(tagKey);
            String tagValue = configService.getProperty("com.openexchange.hazelcast.network.join.aws.tagValue", "hz-nodes");
            config.getNetworkConfig().getJoin().getAwsConfig().setTagValue(tagValue);
        } else {
            throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create("com.openexchange.hazelcast.network.join");
        }
        String mergeFirstRunDelay = configService.getProperty("com.openexchange.hazelcast.merge.firstRunDelay", "120s");
        config.setProperty(GroupProperty.MERGE_FIRST_RUN_DELAY_SECONDS,
            String.valueOf(TimeSpanParser.parseTimespan(mergeFirstRunDelay).longValue() / 1000));
        String mergeRunDelay = configService.getProperty("com.openexchange.hazelcast.merge.runDelay", "120s");
        config.setProperty(GroupProperty.MERGE_NEXT_RUN_DELAY_SECONDS,
            String.valueOf(TimeSpanParser.parseTimespan(mergeRunDelay).longValue() / 1000));
        /*
         * Network Config
         */
        String[] interfaces = Strings.splitByComma(configService.getProperty("com.openexchange.hazelcast.network.interfaces"));
        if (null != interfaces && 0 < interfaces.length) {
            for (String interfaze : interfaces) {
                if (false == Strings.isEmpty(interfaze)) {
                    config.getNetworkConfig().getInterfaces().setEnabled(true).addInterface(interfaze);
                }
            }
        }
        int port = configService.getIntProperty("com.openexchange.hazelcast.network.port", 5701);
        boolean portAutoIncrement = configService.getBoolProperty("com.openexchange.hazelcast.network.portAutoIncrement", true);
        config.getNetworkConfig().setPort(port).setPortAutoIncrement(portAutoIncrement);
        String[] outboundPortDefinitions = Strings.splitByComma(
            configService.getProperty("com.openexchange.hazelcast.network.outboundPortDefinitions"));
        if (null != outboundPortDefinitions && 0 < outboundPortDefinitions.length) {
            for (String portDefintion : outboundPortDefinitions) {
                if (false == Strings.isEmpty(portDefintion)) {
                    config.getNetworkConfig().addOutboundPortDefinition(portDefintion);
                }
            }
        }
        if (configService.getBoolProperty("com.openexchange.hazelcast.network.enableIPv6Support", false)) {
            config.setProperty(GroupProperty.PREFER_IPv4_STACK, "false");
        }
        config.setProperty(GroupProperty.SOCKET_BIND_ANY, String.valueOf(
            configService.getBoolProperty("com.openexchange.hazelcast.socket.bindAny", false)));
        /*
         * Encryption
         */
        if (configService.getBoolProperty("com.openexchange.hazelcast.network.symmetricEncryption", false)) {
            config.getNetworkConfig().setSymmetricEncryptionConfig(new SymmetricEncryptionConfig().setEnabled(true)
                .setAlgorithm(configService.getProperty("com.openexchange.hazelcast.network.symmetricEncryption.algorithm", "PBEWithMD5AndDES"))
                .setSalt(configService.getProperty("com.openexchange.hazelcast.network.symmetricEncryption.salt", "2mw67LqNDEb3"))
                .setPassword(configService.getProperty("com.openexchange.hazelcast.network.symmetricEncryption.password", "D2xhL8mPkjsF"))
                .setIterationCount(configService.getIntProperty("com.openexchange.hazelcast.network.symmetricEncryption.iterationCount", 19)))
            ;
        }
        /*
         * Miscellaneous
         */
        String loggingType = configService.getBoolProperty("com.openexchange.hazelcast.logging.enabled", true) ? "slf4j" : "none";
        System.setProperty(GroupProperty.LOGGING_TYPE.getName(), loggingType);
        config.setProperty(GroupProperty.LOGGING_TYPE, loggingType);
        config.setProperty(GroupProperty.VERSION_CHECK_ENABLED, "false");
        config.setProperty(GroupProperty.HEALTH_MONITORING_LEVEL, configService.getProperty("com.openexchange.hazelcast.healthMonitorLevel", "silent").toUpperCase());
        config.setProperty(GroupProperty.OPERATION_CALL_TIMEOUT_MILLIS, configService.getProperty("com.openexchange.hazelcast.maxOperationTimeout", "30000"));
        config.setProperty(GroupProperty.ENABLE_JMX, configService.getProperty("com.openexchange.hazelcast.jmx", "true"));
        config.setProperty(GroupProperty.ENABLE_JMX_DETAILED, configService.getProperty("com.openexchange.hazelcast.jmxDetailed", "true"));
        config.setProperty(GroupProperty.MEMCACHE_ENABLED, configService.getProperty("com.openexchange.hazelcast.memcache.enabled", "false"));
        config.setProperty(GroupProperty.REST_ENABLED, configService.getProperty("com.openexchange.hazelcast.rest.enabled", "false"));
        /*
         * Arbitrary Hazelcast properties
         */
        Map<String, String> properties = configService.getProperties(new WildcardNamePropertyFilter("hazelcast.*"));
        if (null != properties && !properties.isEmpty()) {
            for (Entry<String, String> entry : properties.entrySet()) {
                String value = entry.getValue();
                if (!Strings.isEmpty(value)) {
                    config.setProperty(entry.getKey(), value.trim());
                }
            }
        }
        /*
         * Data structure configs
         */
        applyDataStructures(config, listPropertyFiles());
        /*
         * Register serialization factory
         */
        DynamicPortableFactory dynamicPortableFactory = Services.requireService(DynamicPortableFactory.class);
        config.getSerializationConfig().addPortableFactory(DynamicPortableFactory.FACTORY_ID, dynamicPortableFactory);
        for (ClassDefinition classDefinition : dynamicPortableFactory.getClassDefinitions()) {
            config.getSerializationConfig().addClassDefinition(classDefinition);
        }
        /*
         * Config ready
         */
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
                if (false == Strings.isEmpty(attributes)) {
                    String[] attrs = attributes.split(" *, *");
                    if (null != attrs && 0 < attrs.length) {
                        for (String attribute : attrs) {
                            mapConfig.addMapIndexConfig(new MapIndexConfig(attribute, false));
                        }
                    }
                }
                String orderedAttributes = properties.getProperty(
                    "com.openexchange.hazelcast.configuration.map.indexes.orderedAttributes");
                if (false == Strings.isEmpty(orderedAttributes)) {
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
        StringParser stringParser = Services.requireService(StringParser.class);
        Field[] declaredFields = dataConfigType.getDeclaredFields();
        for (String propertyName : properties.stringPropertyNames()) {
            Field field = findMatching(declaredFields, propertyName);
            if (null != field) {
                try {
                    field.setAccessible(true);
                    field.set(dataConfig, stringParser.parse(properties.getProperty(propertyName), field.getType()));
                } catch (SecurityException e) {
                    LOG.warn("Unable to set field for ''{}''", propertyName, e);
                } catch (IllegalArgumentException e) {
                    throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create(e, propertyName);
                } catch (IllegalAccessException e) {
                    LOG.warn("Unable to set field for ''{}''", propertyName, e);
                }
            } else {
                LOG.debug("No matching field found for ''{}'', skipping.", propertyName);
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
        File directory = Services.requireService(ConfigurationService.class).getDirectory(DIRECTORY_NAME);
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
            in = new FileInputStream(file);
            properties.load(in);
        } catch (FileNotFoundException e) {
            throw ConfigurationExceptionCodes.READ_ERROR.create(file);
        } catch (IOException e) {
            throw ConfigurationExceptionCodes.READ_ERROR.create(file);
        } finally {
            Streams.close(in);
        }
        return properties;
    }

    /**
     * Loads a Hazelcast configuration from a file named <code>hazelcast.xml</code> in the {@link #DIRECTORY_NAME} folder if present.
     *
     * @return The loaded Hazelcast configuration, or <code>null</code> if no such file exists or can't be loaded
     * @throws OXException
     */
    private static Config loadXMLConfig() throws OXException {
        File directory = Services.requireService(ConfigurationService.class).getDirectory(DIRECTORY_NAME);
        if (null != directory) {
            File xmlConfigFile = new File(directory, "hazelcast.xml");
            if (xmlConfigFile.exists()) {
                try {
                    return ConfigLoader.load(xmlConfigFile.getAbsolutePath());
                } catch (RuntimeException e) {
                   LOG.warn("Error loading configuration from file {}", xmlConfigFile.getAbsolutePath(), e);
                } catch (IOException e) {
                    LOG.warn("Error loading configuration from file {}", xmlConfigFile.getAbsolutePath(), e);
                }
            }
        }
        return null;
    }

}
