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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.Version;
import com.hazelcast.config.Config;
import com.hazelcast.config.IndexConfig;
import com.hazelcast.config.IndexType;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MemcacheProtocolConfig;
import com.hazelcast.config.MultiMapConfig;
import com.hazelcast.config.QueueConfig;
import com.hazelcast.config.RestApiConfig;
import com.hazelcast.config.SSLConfig;
import com.hazelcast.config.SymmetricEncryptionConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.config.TopicConfig;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.config.cp.SemaphoreConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.nio.serialization.ClassDefinition;
import com.hazelcast.spi.properties.ClusterProperty;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.WildcardNamePropertyFilter;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.hazelcast.configuration.HazelcastConfigurationService;
import com.openexchange.hazelcast.configuration.KnownNetworkJoin;
import com.openexchange.hazelcast.configuration.osgi.Services;
import com.openexchange.hazelcast.configuration.ssl.HazelcastSSLFactory;
import com.openexchange.hazelcast.dns.HazelcastDnsResolver;
import com.openexchange.hazelcast.dns.HazelcastDnsResolverConfig;
import com.openexchange.hazelcast.dns.HazelcastDnsService;
import com.openexchange.hazelcast.serialization.DynamicPortableFactory;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;
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
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(HazelcastConfigurationServiceImpl.class);

    /** Name of the subdirectory containing the hazelcast data structure properties */
    private static final String DIRECTORY_NAME = "hazelcast";

    /** The applied port offset suitable for the underlying Hazelcast library v4.0.2 if automatic offset is enabled */
    private static final int AUTO_PORT_OFFSET = 4;

    // -----------------------------------------------------------------------------------------------------------------------------------

    /** The Hazelcast configuration instance */
    private volatile Config config;

    /** The timer task for DNS resolver */
    private volatile ScheduledTimerTask dnsResolverTimerTask;

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
                    config = loadConfig(this);
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
     * Gets the Hazelcast configuration.
     *
     * @return The Hazelcast configuration or <code>null</code>
     */
    public Config getConfigDirect() {
        return config;
    }

    /**
     * (Re-)Initializes the DNS look-up to feed TCP/IP network configuration with.
     *
     * @param config The active Hazelcast configuration
     * @param configService The configuration service providing up-to-date properties
     * @throws OXException If initialization fails
     */
    public void reinitializeDnsLookUp(Config config, ConfigurationService configService) throws OXException {
        if (null == config) {
            return;
        }

        // Acquire needed services
        HazelcastDnsService dnsService = Services.optService(HazelcastDnsService.class);
        if (dnsService == null) {
            throw ServiceExceptionCode.absentService(HazelcastDnsService.class);
        }
        TimerService timerService = Services.optService(TimerService.class);
        if (timerService == null) {
            throw ServiceExceptionCode.absentService(TimerService.class);
        }

        // Stop possibly running timer task
        if (stopDnsResolverTimerTask()) {
            timerService.purge();
        }

        // Disable other network join alternatives
        config.getNetworkConfig().getJoin().getAwsConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);

        // Obtain config options
        Collection<String> domainNames;
        {

            String[] domNames = Strings.splitByComma(configService.getProperty("com.openexchange.hazelcast.network.join.dns.domainNames"));
            if (domNames == null || domNames.length == 0) {
                throw ConfigurationExceptionCodes.PROPERTY_MISSING.create("com.openexchange.hazelcast.network.join.dns.domainNames");
            }
            domainNames = new ArrayList<>(domNames.length);
            for (String domainName : domNames) {
                if (Strings.isNotEmpty(domainName)) {
                    domainNames.add(domainName);
                }
            }
            if (domainNames.isEmpty()) {
                throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create("com.openexchange.hazelcast.network.join.dns.domainNames");
            }
        }
        String resolverHost = configService.getProperty("com.openexchange.hazelcast.network.join.dns.resolverHost");
        int resolverPort = configService.getIntProperty("com.openexchange.hazelcast.network.join.dns.resolverPort", -1);
        long refreshMillis = configService.getIntProperty("com.openexchange.hazelcast.network.join.dns.refreshMillis", 60000);

        // Initialize DNS resolver
        HazelcastDnsResolver dnsResolver = dnsService.createResolver(HazelcastDnsResolverConfig.builder().withResolverHost(resolverHost).withResolverPort(resolverPort).build());

        // Query host addresses for domain names
        List<String> hostAddresses = dnsResolver.resolveByName(domainNames);

        // Apply addresses to TCP/IP network configuration
        TcpIpConfig tcpIpConfig = config.getNetworkConfig().getJoin().getTcpIpConfig();
        tcpIpConfig.setEnabled(true);
        tcpIpConfig.setConnectionTimeoutSeconds(configService.getIntProperty("com.openexchange.hazelcast.network.join.static.connectionTimeout", 10));
        if (!hostAddresses.isEmpty()) {
            for (String hostAddress : hostAddresses) {
                if (Strings.isNotEmpty(hostAddress)) {
                    tcpIpConfig.addMember(hostAddress);
                }
            }
        }

        // Initialize timer task
        Runnable task = new HazelcastApplyResolvedMembersTask(domainNames, dnsResolver, new LinkedHashSet<>(hostAddresses), config);
        dnsResolverTimerTask = timerService.scheduleAtFixedRate(task, refreshMillis, refreshMillis);
    }

    /**
     * Performs shut-down operations.
     */
    public void shutDown() {
        stopDnsResolverTimerTask();
    }

    private boolean stopDnsResolverTimerTask() {
        ScheduledTimerTask dnsResolverTimerTask = this.dnsResolverTimerTask;
        if (dnsResolverTimerTask == null) {
            return false;
        }
        this.dnsResolverTimerTask = null;
        dnsResolverTimerTask.cancel(true);
        return true;
    }

    // -----------------------------------------------------------------------------------------------------------------------------------

    /**
     * Loads and configures the hazelcast configuration.
     *
     * @return The config
     */
    private static Config loadConfig(HazelcastConfigurationServiceImpl hazelcastConfig) throws OXException {
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
        String licenseKey = configService.getProperty("com.openexchange.hazelcast.licenseKey");
        if (Strings.isNotEmpty(licenseKey)) {
            config.setLicenseKey(licenseKey);
        }
        KnownNetworkJoin join;
        {
            String sJoin = configService.getProperty("com.openexchange.hazelcast.network.join", KnownNetworkJoin.EMPTY.getIdentifier()).trim();
            config.setProperty("com.openexchange.hazelcast.network.join", sJoin);
            join = KnownNetworkJoin.networkJoinFor(sJoin);
            if (join == null) {
                throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create("com.openexchange.hazelcast.network.join");
            }
        }
        String groupName = configService.getProperty("com.openexchange.hazelcast.group.name");
        if (Strings.isNotEmpty(groupName)) {
            Bundle bundle = FrameworkUtil.getBundle(HazelcastInstance.class);
            if (null == bundle) {
                LOG.warn("Bundle for {} not found, unable to append version to group name.", HazelcastInstance.class);
            } else {
                groupName = buildGroupName(groupName, bundle.getVersion(), null != config.getLicenseKey());
            }
            config.setClusterName(groupName);
        } else if (join != KnownNetworkJoin.EMPTY) {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create("com.openexchange.hazelcast.group.name");
        }
        if (Strings.isNotEmpty(configService.getProperty("com.openexchange.hazelcast.group.password"))) {
            LOG.info("The configured value for 'com.openexchange.hazelcast.group.password' is no longer used and should be removed.");
        }
        config.setLiteMember(configService.getBoolProperty("com.openexchange.hazelcast.liteMember", config.isLiteMember()));
        switch (join) {
            case EMPTY:
                config.getNetworkConfig().getJoin().getAwsConfig().setEnabled(false);
                config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
                config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(false);
                break;
            case STATIC:
                config.getNetworkConfig().getJoin().getAwsConfig().setEnabled(false);
                config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
                config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(true);
                config.getNetworkConfig().getJoin().getTcpIpConfig().setConnectionTimeoutSeconds(
                    configService.getIntProperty("com.openexchange.hazelcast.network.join.static.connectionTimeout", 10));
                String[] members = Strings.splitByComma(
                    configService.getProperty("com.openexchange.hazelcast.network.join.static.nodes"));
                if (null != members && 0 < members.length) {
                    for (String member : members) {
                        if (Strings.isNotEmpty(member)) {
                            try {
                                config.getNetworkConfig().getJoin().getTcpIpConfig().addMember(InetAddress.getByName(member).getHostAddress());
                            } catch (UnknownHostException e) {
                                throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create(
                                    e, "com.openexchange.hazelcast.network.join.static.nodes");
                            }
                        }
                    }
                }
                break;
            case MULTICAST:
                config.getNetworkConfig().getJoin().getAwsConfig().setEnabled(false);
                config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(false);
                config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(true);
                String group = configService.getProperty("com.openexchange.hazelcast.network.join.multicast.group", "224.2.2.3");
                int port = configService.getIntProperty("com.openexchange.hazelcast.network.join.multicast.port", 54327);
                config.getNetworkConfig().getJoin().getMulticastConfig().setMulticastGroup(group).setMulticastPort(port);
                break;
            case AWS:
                // See https://github.com/hazelcast/hazelcast-aws#java-based-configuration
                /*-
                 * The following properties can be configured (all are optional).
                 *
                 *  - "access-key", "secret-key": access and secret keys of your AWS account; if not set, iam-role is used
                 *  - "iam-role": IAM Role attached to EC2 instance used to fetch credentials (if access-key/secret-key not specified); if not set, default IAM Role attached to EC2 instance is used
                 *  - "region": region where Hazelcast members are running; default is the current region
                 *  - "host-header": ec2, ecs, or the URL of a EC2/ECS API endpoint; automatically detected by default
                 *  - "security-group-name": filter to look only for EC2 instances with the given security group
                 *  - "tag-key", "tag-value": filter to look only for EC2 Instances with the given tag-key/tag-value
                 *  - "connection-timeout-seconds", "read-timeout-seconds": connection and read timeouts when making a call to AWS API; default to 10
                 *  - "connection-retries": number of retries while connecting to AWS API; default to 3
                 *  - "hz-port": a range of ports where the plugin looks for Hazelcast members; default is 5701-5708
                 */
                config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
                config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(false);
                config.getNetworkConfig().getJoin().getAwsConfig().setEnabled(true);
                String accessKey = configService.getProperty("com.openexchange.hazelcast.network.join.aws.accessKey");
                if (Strings.isEmpty(accessKey)) {
                    throw ConfigurationExceptionCodes.PROPERTY_MISSING.create("com.openexchange.hazelcast.network.join.aws.accessKey");
                }
                config.getNetworkConfig().getJoin().getAwsConfig().setProperty("access-key", accessKey);
                String secretKey = configService.getProperty("com.openexchange.hazelcast.network.join.aws.secretKey");
                if (Strings.isEmpty(secretKey)) {
                    throw ConfigurationExceptionCodes.PROPERTY_MISSING.create("com.openexchange.hazelcast.network.join.aws.secretKey");
                }
                config.getNetworkConfig().getJoin().getAwsConfig().setProperty("secret-key", secretKey);
                String region = configService.getProperty("com.openexchange.hazelcast.network.join.aws.region", "us-west-1");
                config.getNetworkConfig().getJoin().getAwsConfig().setProperty("region", region);
                String hostHeader = configService.getProperty("com.openexchange.hazelcast.network.join.aws.hostHeader", "ec2.amazonaws.com");
                config.getNetworkConfig().getJoin().getAwsConfig().setProperty("host-header", hostHeader);
                String tagKey = configService.getProperty("com.openexchange.hazelcast.network.join.aws.tagKey", "type");
                config.getNetworkConfig().getJoin().getAwsConfig().setProperty("tag-key", tagKey);
                String tagValue = configService.getProperty("com.openexchange.hazelcast.network.join.aws.tagValue", "hz-nodes");
                config.getNetworkConfig().getJoin().getAwsConfig().setProperty("tag-value", tagValue);
                break;
            case DNS:
                // Initialize DNS look-up
                hazelcastConfig.reinitializeDnsLookUp(config, configService);
                break;
            default:
                throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create("com.openexchange.hazelcast.network.join");
        }
        String mergeFirstRunDelay = configService.getProperty("com.openexchange.hazelcast.merge.firstRunDelay", "120s");
        config.setProperty(ClusterProperty.MERGE_FIRST_RUN_DELAY_SECONDS.getName(),
            String.valueOf(TimeSpanParser.parseTimespanToPrimitive(mergeFirstRunDelay) / 1000));
        String mergeRunDelay = configService.getProperty("com.openexchange.hazelcast.merge.runDelay", "120s");
        config.setProperty(ClusterProperty.MERGE_NEXT_RUN_DELAY_SECONDS.getName(),
            String.valueOf(TimeSpanParser.parseTimespanToPrimitive(mergeRunDelay) / 1000));
        /*
         * Network Config
         */
        String[] interfaces = Strings.splitByComma(configService.getProperty("com.openexchange.hazelcast.network.interfaces"));
        if (null != interfaces && 0 < interfaces.length) {
            for (String interfaze : interfaces) {
                if (Strings.isNotEmpty(interfaze)) {
                    config.getNetworkConfig().getInterfaces().setEnabled(true).addInterface(interfaze);
                }
            }
        }
        int port = configService.getIntProperty("com.openexchange.hazelcast.network.port", 5701);
        String portOffset = configService.getProperty("com.openexchange.hazelcast.network.portOffset", "auto").trim();
        if ("auto".equalsIgnoreCase(portOffset)) {
            port += AUTO_PORT_OFFSET;
        } else {
            try {
                port += Integer.parseInt(portOffset);
            } catch (NumberFormatException e) {
                throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create(e, "com.openexchange.hazelcast.network.portOffset");
            }
        }
        boolean portAutoIncrement = configService.getBoolProperty("com.openexchange.hazelcast.network.portAutoIncrement", true);
        config.getNetworkConfig().setPort(port).setPortAutoIncrement(portAutoIncrement);
        String[] outboundPortDefinitions = Strings.splitByComma(
            configService.getProperty("com.openexchange.hazelcast.network.outboundPortDefinitions"));
        if (null != outboundPortDefinitions && 0 < outboundPortDefinitions.length) {
            for (String portDefintion : outboundPortDefinitions) {
                if (Strings.isNotEmpty(portDefintion)) {
                    config.getNetworkConfig().addOutboundPortDefinition(portDefintion);
                }
            }
        }
        if (configService.getBoolProperty("com.openexchange.hazelcast.network.enableIPv6Support", false)) {
            config.setProperty(ClusterProperty.PREFER_IPv4_STACK.getName(), "false");
        }
        config.setProperty(ClusterProperty.SOCKET_BIND_ANY.getName(), String.valueOf(
        configService.getBoolProperty("com.openexchange.hazelcast.socket.bindAny", false)));

        /*
         * Encryption
         */
        // Only one encryption method can be use. so start with strongest
        if (configService.getBoolProperty("com.openexchange.hazelcast.network.ssl", false)) {
            config.getNetworkConfig().setSSLConfig(new SSLConfig().setEnabled(true)
                .setFactoryImplementation(new HazelcastSSLFactory())
                .setFactoryClassName(HazelcastSSLFactory.class.getName())
                .setProperties(HazelcastSSLFactory.getPropertiesFromService(configService)));
        } else if (configService.getBoolProperty("com.openexchange.hazelcast.network.symmetricEncryption", false)) {
            config.getNetworkConfig().setSymmetricEncryptionConfig(new SymmetricEncryptionConfig()
                .setEnabled(true)
                .setAlgorithm(configService.getProperty("com.openexchange.hazelcast.network.symmetricEncryption.algorithm", "PBEWithMD5AndDES"))
                .setSalt(getPassword(configService, "com.openexchange.hazelcast.network.symmetricEncryption.salt", "X-k4nY-Y*v38f=dSJrr)"))
                .setPassword(getPassword(configService, "com.openexchange.hazelcast.network.symmetricEncryption.password", "&3sFs<^6[cKbWDW#du9s"))
                .setIterationCount(configService.getIntProperty("com.openexchange.hazelcast.network.symmetricEncryption.iterationCount", 19)));
        }
        /*
         * Miscellaneous
         */
        String loggingType = configService.getBoolProperty("com.openexchange.hazelcast.logging.enabled", true) ? "slf4j" : "none";
        System.setProperty(ClusterProperty.LOGGING_TYPE.getName(), loggingType);
        config.setProperty(ClusterProperty.LOGGING_TYPE.getName(), loggingType);
        config.setProperty(ClusterProperty.PHONE_HOME_ENABLED.getName(), "false");
        config.setProperty(ClusterProperty.HEALTH_MONITORING_LEVEL.getName(), configService.getProperty("com.openexchange.hazelcast.healthMonitorLevel", "silent").toUpperCase());
        config.setProperty(ClusterProperty.OPERATION_CALL_TIMEOUT_MILLIS.getName(), configService.getProperty("com.openexchange.hazelcast.maxOperationTimeout", "30000"));
        config.setProperty(ClusterProperty.ENABLE_JMX.getName(), configService.getProperty("com.openexchange.hazelcast.jmx", "true"));
        MemcacheProtocolConfig memcacheProtocolConfig = config.getNetworkConfig().getMemcacheProtocolConfig();
        if (memcacheProtocolConfig == null) {
            memcacheProtocolConfig = new MemcacheProtocolConfig();
        }
        memcacheProtocolConfig.setEnabled(configService.getBoolProperty("com.openexchange.hazelcast.memcache.enabled", false));
        config.getNetworkConfig().setMemcacheProtocolConfig(memcacheProtocolConfig);

        RestApiConfig restApiConfig = config.getNetworkConfig().getRestApiConfig();
        if (restApiConfig == null) {
            restApiConfig = new RestApiConfig();
        }
        restApiConfig.setEnabled(configService.getBoolProperty("com.openexchange.hazelcast.rest.enabled", false));
        config.getNetworkConfig().setRestApiConfig(restApiConfig);
        /*
         * Arbitrary Hazelcast properties
         */
        Map<String, String> properties = configService.getProperties(new WildcardNamePropertyFilter("hazelcast.*"));
        if (null != properties && !properties.isEmpty()) {
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                String value = entry.getValue();
                if (Strings.isNotEmpty(value)) {
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

    private static String getPassword( ConfigurationService configurationService, String propertyName, String defaultPassword) {
        String property = configurationService.getProperty(propertyName);
        if (Strings.isNotEmpty(property) && defaultPassword.equalsIgnoreCase(property)) {
            LOG.warn("The value '{}' for '{}' has not been changed from its default. Please do so to restrict access to your cluster.", defaultPassword, propertyName);
        }
        return property;
    }

    private static void applyDataStructures(Config config, File[] propertyFiles) throws OXException {
        if (null != propertyFiles) {
            for (File file : propertyFiles) {
                applyDataStructure(config, loadProperties(file), file);
            }
        }
    }

    private static void applyDataStructure(Config config, Properties properties, File file) throws OXException {
        if (null != properties) {
            StringParser stringParser = Services.requireService(StringParser.class);

            boolean mapConfigApplied = false;
            boolean topicConfigApplied = false;
            boolean queueConfigApplied = false;
            boolean multiMapConfigApplied = false;
            boolean semaphoreConfigApplied = false;

            for (String propertyName : properties.stringPropertyNames()) {
                if (propertyName.startsWith("com.openexchange.hazelcast.configuration.map")) {
                    if (!mapConfigApplied) {
                        mapConfigApplied = true;
                        MapConfig mapConfig = createDataConfig(properties, new MapConfig(), stringParser);
                        String attributes = properties.getProperty("com.openexchange.hazelcast.configuration.map.indexes.attributes");
                        if (Strings.isNotEmpty(attributes)) {
                            String[] attrs = Strings.splitByComma(attributes);
                            if (null != attrs) {
                                for (String attribute : attrs) {
                                    mapConfig.addIndexConfig(new IndexConfig(IndexType.HASH, attribute));
                                }
                            }
                        }
                        String orderedAttributes = properties.getProperty("com.openexchange.hazelcast.configuration.map.indexes.orderedAttributes");
                        if (Strings.isNotEmpty(orderedAttributes)) {
                            String[] attrs = Strings.splitByComma(orderedAttributes);
                            if (null != attrs) {
                                for (String attribute : attrs) {
                                    mapConfig.addIndexConfig(new IndexConfig(IndexType.SORTED, attribute));
                                }
                            }
                        }
                        config.addMapConfig(mapConfig);
                    }
                } else if (propertyName.startsWith("com.openexchange.hazelcast.configuration.topic")) {
                    if (!topicConfigApplied) {
                        topicConfigApplied = true;
                        config.addTopicConfig(createDataConfig(properties, new TopicConfig(), stringParser));
                    }
                } else if (propertyName.startsWith("com.openexchange.hazelcast.configuration.queue")) {
                    if (!queueConfigApplied) {
                        queueConfigApplied = true;
                        config.addQueueConfig(createDataConfig(properties, new QueueConfig(), stringParser));
                    }
                } else if (propertyName.startsWith("com.openexchange.hazelcast.configuration.multimap")) {
                    if (!multiMapConfigApplied) {
                        multiMapConfigApplied = true;
                        config.addMultiMapConfig(createDataConfig(properties, new MultiMapConfig(), stringParser));
                    }
                } else if (propertyName.startsWith("com.openexchange.hazelcast.configuration.semaphore")) {
                    if (!semaphoreConfigApplied) {
                        semaphoreConfigApplied = true;
                        config.getCPSubsystemConfig().addSemaphoreConfig(createDataConfig(properties, new SemaphoreConfig(), stringParser));
                    }
                } else {
                    LOG.warn("Illegal property ''{}'' in file {}", propertyName, file.getPath());
                }
            }
        }
    }

    private static <T> T createDataConfig(Properties properties, T dataConfig, StringParser stringParser) throws OXException {
        Map<String, Field> declaredFields = getFieldsFrom(dataConfig);
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

    private static <T> Map<String, Field> getFieldsFrom(T dataConfig) throws OXException {
        try {
            return Arrays.stream(dataConfig.getClass().getDeclaredFields()).collect(Collectors.toMap(f -> Strings.asciiLowerCase(f.getName()), f -> f));
        } catch (SecurityException e) {
            throw OXException.general("Unable to get fields from " + dataConfig.getClass(), e);
        }
    }

    private static Field findMatching(Map<String, Field> declaredFields, String propertyName) {
        if (null == declaredFields) {
            return null;
        }

        int idx = propertyName.lastIndexOf('.');
        String fieldName = idx >= 0 && propertyName.length() > idx ? propertyName.substring(1 + idx) : propertyName;
        return declaredFields.get(Strings.asciiLowerCase(fieldName));
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
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            Properties properties = new Properties();
            properties.load(in);
            return properties;
        } catch (FileNotFoundException e) {
            throw ConfigurationExceptionCodes.READ_ERROR.create(file);
        } catch (IOException e) {
            throw ConfigurationExceptionCodes.READ_ERROR.create(file);
        } finally {
            Streams.close(in);
        }
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
                FileInputStream in = null;
                try {
                    in = new FileInputStream(xmlConfigFile.getAbsolutePath());
                    return new XmlConfigBuilder(in).build();
                } catch (RuntimeException e) {
                   LOG.warn("Error loading configuration from file {}", xmlConfigFile.getAbsolutePath(), e);
                } catch (IOException e) {
                    LOG.warn("Error loading configuration from file {}", xmlConfigFile.getAbsolutePath(), e);
                } finally {
                    Streams.close(in);
                }
            }
        }
        return null;
    }

    /**
     * Constructs the effective group name to use in the cluster group configuration for Hazelcast. The full group name is constructed
     * based on the configured group name prefix, optionally appended with a version identifier string of the underlying Hazelcast library.
     * <p/>
     * This needs to be done to form separate Hazelcast clusters during rolling upgrades of the nodes with incompatible Hazelcast
     * libraries.
     *
     * @param groupName The configured cluster group name
     * @param version The version of the Hazelcast library
     * @param enterprise <code>true</code> if enterprise features are available for rolling upgrades, <code>false</code>, otherwise
     * @return The full cluster group name
     */
    private static String buildGroupName(String groupName, Version version, boolean enterprise) {
        if (enterprise) {
            return groupName;
        }
        return new StringBuilder(20).append(groupName).append('-').append(version.getMajor()).append('.').append(version.getMinor()).toString();
    }

}
