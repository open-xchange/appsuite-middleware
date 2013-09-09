
package com.openexchange.hazelcast.configuration.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Properties;
import org.apache.commons.logging.Log;
import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapIndexConfig;
import com.hazelcast.config.MultiMapConfig;
import com.hazelcast.config.QueueConfig;
import com.hazelcast.config.SemaphoreConfig;
import com.hazelcast.config.TopicConfig;
import com.hazelcast.impl.GroupProperties;
import com.openexchange.config.ConfigurationService;
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

    /**
     * Initializes a new {@link HazelcastConfigurationServiceImpl}.
     *
     * @param configService A reference to the configuration service
     */
    public HazelcastConfigurationServiceImpl() {
        super();
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
        if ("ox".equalsIgnoreCase(groupName)) {
            LOG.warn("\n\tThe configuration value for \"com.openexchange.cluster.name\" has not been changed from it's default " +
                    "value \"ox\". Please do so to make this warning disappear.\n");
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
        String interfaces = configService.getProperty("com.openexchange.hazelcast.network.interfaces");
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
        if (null != directory) {
            return directory.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    return null != name && name.toLowerCase().endsWith(".properties");
                }
            });
        } else {
            return new File[0];
        }
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
            if (false == Character.isWhitespace(string.charAt(i))) {
                return false;
            }
        }
        return true;
    }

}
