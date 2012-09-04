package com.openexchange.http.ui.osgi;

import org.osgi.service.http.HttpService;
import com.openexchange.osgi.HousekeepingActivator;

public class HttpUIActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[]{HttpService.class};
    }

    @Override
    protected void startBundle() throws Exception {

        track(HttpService.class);
        openTrackers();
        
        HttpService httpService = getService(HttpService.class);
        httpService.registerResources("/ui", "/ui/", null);
    }

}
