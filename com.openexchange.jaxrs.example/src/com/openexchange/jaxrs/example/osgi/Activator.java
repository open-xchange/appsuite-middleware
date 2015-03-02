package com.openexchange.jaxrs.example.osgi;

import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.html.HtmlService;
import com.openexchange.jaxrs.example.ExampleService;
import com.openexchange.jaxrs.example.ExampleService2;
import com.openexchange.jaxrs.example.RESTConfigurationService;
import com.openexchange.jaxrs.example.RESTHtmlService;
import com.openexchange.jaxrs.example.Shuffler;
import com.openexchange.osgi.HousekeepingActivator;

public class Activator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, ConfigViewFactory.class, HtmlService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        registerService(RESTConfigurationService.class, new RESTConfigurationService(this));
        registerService(RESTHtmlService.class, new RESTHtmlService(getService(HtmlService.class)));

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
