package com.openexchange.preview.thirdwing.osgi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.preview.InternalPreviewService;
import com.openexchange.server.osgiservice.HousekeepingActivator;
import com.openexchange.threadpool.ThreadPoolService;

public class ThirdwingPreviewActivator extends HousekeepingActivator {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(ThirdwingPreviewActivator.class));

    private static final Class<?>[] NEEDED = new Class<?>[] { ManagedFileManagement.class,
                                                              ThreadPoolService.class };
    

    @Override
    protected Class<?>[] getNeededServices() {
        return NEEDED;
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("Starting bundle com.openexchange.preview.thirdwing.");      
        registerService(InternalPreviewService.class, new ThirdwingPreviewService(this));
    }

}
