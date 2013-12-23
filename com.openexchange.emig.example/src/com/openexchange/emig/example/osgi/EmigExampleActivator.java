package com.openexchange.emig.example.osgi;

import com.openexchange.config.ConfigurationService;
import com.openexchange.emig.EmigService;
import com.openexchange.emig.example.ExampleEmigService;
import com.openexchange.http.client.HTTPClient;
import com.openexchange.osgi.HousekeepingActivator;

public class EmigExampleActivator extends HousekeepingActivator {
    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[]{HTTPClient.class, ConfigurationService.class};
    }

    @Override
    protected void startBundle() throws Exception {
        registerService(EmigService.class, new ExampleEmigService(this));
    }


}
