package com.openexchange.solr.osgi;

import com.openexchange.config.ConfigurationService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.solr.SolrManagementService;
import com.openexchange.solr.internal.Services;
import com.openexchange.solr.internal.SolrManagementServiceImpl;

/**
* {@link SolrActivator}
* 
* @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
*/
public class SolrActivator extends HousekeepingActivator {

    private volatile SolrManagementServiceImpl managementService;

    @Override
    protected Class<?>[] getNeededServices() {        
        return new Class<?>[] { ConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Services.setServiceLookup(this);
        final SolrManagementServiceImpl managementService = this.managementService = new SolrManagementServiceImpl();
        managementService.startUp();
        
        registerService(SolrManagementService.class, managementService);
    }
    
    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
        final SolrManagementServiceImpl managementService = this.managementService;
        if (managementService != null) {
            managementService.shutdown();
            this.managementService = null;
        }
    }

}
