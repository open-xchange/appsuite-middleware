package com.openexchange.TOBEREMOVED.rest.services.configuration.osgi;

import com.openexchange.TOBEREMOVED.rest.services.configuration.ConfigRESTService;
import com.openexchange.TOBEREMOVED.rest.services.osgiservice.OXRESTActivator;
import com.openexchange.config.cascade.ConfigViewFactory;

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
