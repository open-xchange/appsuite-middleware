package com.openexchange.rest.services.configuration.osgi;

import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.rest.services.configuration.ConfigRESTService;
import com.openexchange.rest.services.osgiservice.OXRESTActivator;

public class ConfigRESTServiceActivator extends OXRESTActivator {
    
    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[]{ConfigViewFactory.class};
    }
    
    @Override
    protected void startBundle() throws Exception {
        registerWebService(ConfigRESTService.class);
    }


}
