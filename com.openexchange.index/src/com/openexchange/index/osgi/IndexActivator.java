package com.openexchange.index.osgi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.index.ConfigIndexService;
import com.openexchange.index.internal.StaticConfigIndexService;
import com.openexchange.server.osgiservice.HousekeepingActivator;

public class IndexActivator extends HousekeepingActivator {
    
    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(IndexActivator.class));

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { DatabaseService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("Starting Bundle com.openexchange.index.osgi.");
        // Only for testing purpose...
        // registerService(ConfigIndexService.class, new ConfigIndexServiceImpl(getService(DatabaseService.class)));
        registerService(ConfigIndexService.class, new StaticConfigIndexService());
    }


}
