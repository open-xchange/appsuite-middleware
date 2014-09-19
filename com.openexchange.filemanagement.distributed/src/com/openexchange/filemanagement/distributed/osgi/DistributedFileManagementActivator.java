
package com.openexchange.filemanagement.distributed.osgi;

import java.util.Map;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.config.ConfigurationService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.filemanagement.DistributedFileManagement;
import com.openexchange.filemanagement.distributed.DistributedFileManagementImpl;
import com.openexchange.hazelcast.configuration.HazelcastConfigurationService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.server.ServiceLookup;
import com.openexchange.threadpool.ThreadPoolService;

/**
 * {@link DistributedFileManagementActivator}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class DistributedFileManagementActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { HazelcastConfigurationService.class, ConfigurationService.class, DispatcherPrefixService.class, ThreadPoolService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DistributedFileManagementActivator.class);
        logger.info("Starting bundle: com.openexchange.filemanagement.distributed");

        HazelcastConfigurationService hazelcastConfig = getService(HazelcastConfigurationService.class);
        final String prefix = getService(DispatcherPrefixService.class).getPrefix();
        final int port = getService(ConfigurationService.class).getIntProperty("com.openexchange.connector.networkListenerPort", 8009);
        final ServiceLookup services = this;
        final BundleContext context = this.context;
        if (hazelcastConfig.isEnabled()) {
            track(HazelcastInstance.class, new SimpleRegistryListener<HazelcastInstance>() {

                volatile DistributedFileManagementImpl distributedFileManagement;

                @Override
                public void added(ServiceReference<HazelcastInstance> ref, HazelcastInstance service) {
                    DistributedFileManagementImpl.setHazelcastInstance(service);

                    // Address and map name
                    String address = service.getCluster().getLocalMember().getSocketAddress().getHostName();
                    String mapName = discoverMapName(service.getConfig(), logger);
                    address = address + ":" + port + prefix;

                    // Clean-up task
                    Runnable shutDownTask = new Runnable() {

                        @Override
                        public void run() {
                            try {
                                shutDownDistributedFileManagement();
                            } catch (Exception e) {
                                logger.error("Failed to shut-down distributed file management", e);
                            }
                        }
                    };

                    // Register distributed file management service
                    DistributedFileManagementImpl distributedFileManagement = new DistributedFileManagementImpl(services, address, mapName, context, shutDownTask);
                    this.distributedFileManagement = distributedFileManagement;
                    registerService(DistributedFileManagement.class, distributedFileManagement);
                }

                @Override
                public void removed(ServiceReference<HazelcastInstance> ref, HazelcastInstance service) {
                    shutDownDistributedFileManagement();
                }

                void shutDownDistributedFileManagement() {
                    DistributedFileManagementImpl.setHazelcastInstance(null);

                    DistributedFileManagementImpl distributedFileManagement = this.distributedFileManagement;
                    if(distributedFileManagement != null) {
                        this.distributedFileManagement = null;
                        unregisterService(distributedFileManagement);
                        distributedFileManagement.cleanUp();
                    }
                }
            });

            /*-
             * Already registered by tracker above, isn't it?
             *
            HazelcastInstance service = getOptionalService(HazelcastInstance.class);
            if (service != null) {
                DistributedFileManagementImpl.setHazelcastInstance(service);
                String address = service.getCluster().getLocalMember().getInetSocketAddress().getHostName();
                String mapName = discoverMapName(service.getConfig());
                address += prefix;
                registerService(DistributedFileManagement.class, new DistributedFileManagementImpl(services, address, mapName));
            }
             *
             */
        }

        openTrackers();
    }

    @Override
    protected void stopBundle() throws Exception {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DistributedFileManagementActivator.class);
        logger.info("Stopping bundle: com.openexchange.filemanagement.distributed");
        super.stopBundle();
    }

    @Override
    public <S> void registerService(java.lang.Class<S> clazz, S service) {
        super.registerService(clazz, service);
    }

    @Override
    public <S> void unregisterService(S service) {
        super.unregisterService(service);
    }

    String discoverMapName(Config config, org.slf4j.Logger logger) throws IllegalStateException {
        Map<String, MapConfig> mapConfigs = config.getMapConfigs();
        if (null != mapConfigs && !mapConfigs.isEmpty()) {
            for (String mapName : mapConfigs.keySet()) {
                if (mapName.startsWith("distributedFiles-")) {
                    logger.info("Using distributed map '{}'.", mapName);
                    return mapName;
                }
            }
        }
        String msg = "No distributed file map found in hazelcast configuration";
        throw new IllegalStateException(msg, new BundleException(msg, BundleException.ACTIVATOR_ERROR));
    }

}
