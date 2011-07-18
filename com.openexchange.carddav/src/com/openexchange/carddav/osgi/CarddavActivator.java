package com.openexchange.carddav.osgi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.carddav.servlet.CardDAV;
import com.openexchange.carddav.servlet.CarddavPerformer;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.server.osgiservice.HousekeepingActivator;
import com.openexchange.tools.service.ServletRegistration;
import com.openexchange.webdav.protocol.osgi.OSGiPropertyMixin;

public class CarddavActivator extends HousekeepingActivator {

    private static final Log LOG = LogFactory.getLog(CarddavActivator.class);
    
    private static final Class<?>[] NEEDED = new Class[]{FolderService.class};
    private OSGiPropertyMixin mixin;

    @Override
    protected Class<?>[] getNeededServices() {
        return NEEDED;
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            CarddavPerformer.setServices(this);
            
            rememberTracker(new ServletRegistration(context, new CardDAV(), "/servlet/carddav"));
            
            CarddavPerformer performer = CarddavPerformer.getInstance();
            mixin = new OSGiPropertyMixin(context, performer);
            performer.setGlobalMixins(mixin);
            
            openTrackers();
        } catch (Throwable t) {
            LOG.error(t.getMessage(), t);
        }
    }
    
    @Override
    protected void stopBundle() throws Exception {
        mixin.close();
        super.stopBundle();
    }


}
