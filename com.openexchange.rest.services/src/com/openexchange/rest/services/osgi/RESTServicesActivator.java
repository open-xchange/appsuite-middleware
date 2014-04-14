package com.openexchange.rest.services.osgi;

import org.osgi.service.http.HttpService;
import com.openexchange.conversion.simple.SimpleConverter;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.rest.services.internal.OXRESTServiceFactory;
import com.openexchange.rest.services.internal.Services;
import com.openexchange.rest.services.servlet.OXRESTServlet;

public class RESTServicesActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[]{HttpService.class, SimpleConverter.class };
    }

    @Override
    protected void startBundle() throws Exception {
        track(OXRESTServiceFactory.class, OXRESTServlet.REST_SERVICES);
        openTrackers();

        HttpService httpService = getService(HttpService.class);
        httpService.registerServlet("/rest", new OXRESTServlet(), null, null);
        
        Services.SERVICES = this;
    }


}
