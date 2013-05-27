
package com.openexchange.filemanagement.distributed.osgi;

import java.util.Map;
import org.apache.commons.logging.Log;
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
import com.openexchange.log.LogFactory;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.server.ServiceLookup;

/**
 * {@link DistributedFileManagementActivator}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class DistributedFileManagementActivator extends HousekeepingActivator {

    private static Log LOG = LogFactory.getLog(DistributedFileManagementActivator.class);

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { HazelcastConfigurationService.class, ConfigurationService.class, DispatcherPrefixService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("Starting bundle: com.openexchange.filemanagement.distributed");

        HazelcastConfigurationService hazelcastConfig = getService(HazelcastConfigurationService.class);
        final String prefix = getService(DispatcherPrefixService.class).getPrefix();
        final ServiceLookup services = this;
        if (hazelcastConfig.isEnabled()) {
            track(HazelcastInstance.class, new SimpleRegistryListener<HazelcastInstance>() {

                @Override
                public void added(ServiceReference<HazelcastInstance> ref, HazelcastInstance service) {
                    DistributedFileManagementImpl.setHazelcastInstance(service);
                    String address = service.getCluster().getLocalMember().getInetSocketAddress().getHostName();
                    String mapName = discoverMapName(service.getConfig());
                    address += prefix;
                    registerService(DistributedFileManagement.class, new DistributedFileManagementImpl(services, address, mapName));
                }

                @Override
                public void removed(ServiceReference<HazelcastInstance> ref, HazelcastInstance service) {
                    DistributedFileManagementImpl.setHazelcastInstance(null);
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
        LOG.info("Stopping bundle: com.openexchange.filemanagement.distributed");
        super.stopBundle();
    }

    @Override
    public <S> void registerService(java.lang.Class<S> clazz, S service) {
        super.registerService(clazz, service);
    }

    String discoverMapName(Config config) throws IllegalStateException {
        Map<String, MapConfig> mapConfigs = config.getMapConfigs();
        if (null != mapConfigs && !mapConfigs.isEmpty()) {
            for (String mapName : mapConfigs.keySet()) {
                if (mapName.startsWith("distributedFiles-")) {
                    LOG.info("Using distributed map '" + mapName + "'.");
                    return mapName;
                }
            }
        }
        String msg = "No distributed file map found in hazelcast configuration";
        throw new IllegalStateException(msg, new BundleException(msg, BundleException.ACTIVATOR_ERROR));
    }

}
