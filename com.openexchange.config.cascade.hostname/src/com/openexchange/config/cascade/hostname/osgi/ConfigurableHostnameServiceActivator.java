package com.openexchange.config.cascade.hostname.osgi;

import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.cascade.hostname.ConfigurableHostnameService;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.server.osgiservice.HousekeepingActivator;


public class ConfigurableHostnameServiceActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[]{ConfigViewFactory.class};
    }

    @Override
    protected void startBundle() throws Exception {
        ConfigViewFactory configViews = getService(ConfigViewFactory.class);
        
        ConfigurableHostnameService hostnameService = new ConfigurableHostnameService(configViews);
    
        registerService(HostnameService.class, hostnameService);
    }


}
