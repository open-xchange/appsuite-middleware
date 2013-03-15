
package com.openexchange.filemanagement.distributed.osgi;

import org.apache.commons.logging.Log;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.config.ConfigurationService;
import com.openexchange.filemanagement.DistributedFileManagement;
import com.openexchange.filemanagement.distributed.DistributedFileManagementImpl;
import com.openexchange.filemanagement.distributed.servlet.DistributedFileServlet;
import com.openexchange.hazelcast.configuration.HazelcastConfigurationService;
import com.openexchange.log.LogFactory;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;

/**
 * {@link DistributedFileManagementActivator}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class DistributedFileManagementActivator extends HousekeepingActivator {

    private static Log LOG = LogFactory.getLog(DistributedFileManagementActivator.class);

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { HazelcastInstance.class, HazelcastConfigurationService.class, ConfigurationService.class, HttpService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("Starting bundle: com.openexchange.filemanagement.distributed");

        HazelcastConfigurationService hazelcastConfig = getService(HazelcastConfigurationService.class);
        if (hazelcastConfig.isEnabled()) {
            track(HazelcastInstance.class, new SimpleRegistryListener<HazelcastInstance>() {

                @Override
                public void added(ServiceReference<HazelcastInstance> ref, HazelcastInstance service) {
                    DistributedFileManagementImpl.setHazelcastInstance(service);
                    String address = service.getCluster().getLocalMember().getInetSocketAddress().getHostName();
                    context.registerService(DistributedFileManagement.class, new DistributedFileManagementImpl(DistributedFileManagementActivator.this, address), null);
                }

                @Override
                public void removed(ServiceReference<HazelcastInstance> ref, HazelcastInstance service) {
                    DistributedFileManagementImpl.setHazelcastInstance(null);
                }
            });
        }

        openTrackers();

        HttpService service = getService(HttpService.class);
        service.registerServlet(DistributedFileServlet.PATH, new DistributedFileServlet(this), null, null);
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("Stopping bundle: com.openexchange.filemanagement.distributed");
        super.stopBundle();
    }

}
