package com.openexchange.drive.client.windows.osgi;

import org.osgi.service.http.HttpService;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.drive.client.windows.files.UpdateFilesProvider;
import com.openexchange.drive.client.windows.files.UpdateFilesProviderImpl;
import com.openexchange.drive.client.windows.service.Constants;
import com.openexchange.drive.client.windows.service.DriveUpdateService;
import com.openexchange.drive.client.windows.service.DriveUpdateServiceImpl;
import com.openexchange.drive.client.windows.service.Services;
import com.openexchange.drive.client.windows.servlet.DownloadServlet;
import com.openexchange.drive.client.windows.servlet.UpdaterXMLServlet;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.templating.TemplateService;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserConfigurationService;

public class Activator extends HousekeepingActivator {


    private static final Class<?>[] NEEDED_SERVICES = { TemplateService.class, ConfigurationService.class, ContextService.class, UserService.class,
        UserConfigurationService.class, HttpService.class, CapabilityService.class, DispatcherPrefixService.class, ConfigViewFactory.class };

    private String downloadServletAlias;
    private String updateServletAlias;

    @Override
    protected Class<?>[] getNeededServices() {
        return NEEDED_SERVICES;
    }

    @Override
    protected void startBundle() throws Exception {
        Services.setServiceLookup(this);
        DriveUpdateService updateService = new DriveUpdateServiceImpl();

        registerService(DriveUpdateService.class, updateService, null);

        //register Files Provider Setup
        final ConfigurationService config = getService(ConfigurationService.class);
        String path = config.getProperty(Constants.PROP_PATH);
        UpdateFilesProvider fileProvider = UpdateFilesProviderImpl.getInstance().init(path);
        updateService.init(fileProvider);
        //register download servlet
        DownloadServlet downloadServlet = new DownloadServlet(updateService, fileProvider);
        String prefix = getService(DispatcherPrefixService.class).getPrefix();
        downloadServletAlias = prefix + Constants.DOWNLOAD_SERVLET;
        getService(HttpService.class).registerServlet(downloadServletAlias, downloadServlet, null, null);
        
        //register update servlet
        updateServletAlias = prefix + Constants.UPDATE_SERVLET;
        final TemplateService templateService = getService(TemplateService.class);
        getService(HttpService.class).registerServlet(updateServletAlias, new UpdaterXMLServlet(templateService, updateService), null, null);
    }

    @Override
    protected void stopBundle() throws Exception {
        HttpService httpService = getService(HttpService.class);
        if (httpService != null && downloadServletAlias != null) {
            httpService.unregister(downloadServletAlias);
            httpService.unregister(updateServletAlias);
        }
        Services.setServiceLookup(null);
        super.stopBundle();
    }

}
