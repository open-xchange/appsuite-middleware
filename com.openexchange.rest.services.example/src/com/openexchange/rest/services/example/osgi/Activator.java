package com.openexchange.rest.services.example.osgi;

import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.html.HtmlService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.rest.services.example.ExampleService;
import com.openexchange.rest.services.example.ExampleService2;
import com.openexchange.rest.services.example.RESTConfigurationService;
import com.openexchange.rest.services.example.RESTHtmlService;
import com.openexchange.rest.services.example.Shuffler;

public class Activator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, ConfigViewFactory.class, HtmlService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        //registerService(RESTConfigurationService.class, new RESTConfigurationService(this));
        //registerService(RESTHtmlService.class, new RESTHtmlService(getService(HtmlService.class)));

        /*
         * Example stuff
         */
        registerService(Shuffler.class, new Shuffler());
        trackService(Shuffler.class);
        openTrackers();

        registerService(ExampleService.class, new ExampleService(this));
        registerService(ExampleService2.class, new ExampleService2(this));
    }

}
