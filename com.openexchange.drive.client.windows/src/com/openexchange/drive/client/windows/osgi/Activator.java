package com.openexchange.drive.client.windows.osgi;

import java.rmi.Remote;
import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpService;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.drive.client.windows.files.UpdateFilesProvider;
import com.openexchange.drive.client.windows.files.UpdateFilesProviderImpl;
import com.openexchange.drive.client.windows.service.BrandingConfigurationRemote;
import com.openexchange.drive.client.windows.service.BrandingConfigurationRemoteImpl;
import com.openexchange.drive.client.windows.service.Constants;
import com.openexchange.drive.client.windows.service.DriveUpdateService;
import com.openexchange.drive.client.windows.service.DriveUpdateServiceImpl;
import com.openexchange.drive.client.windows.service.Services;
import com.openexchange.drive.client.windows.servlet.DownloadServlet;
import com.openexchange.drive.client.windows.servlet.UpdatesXMLServlet;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.templating.TemplateService;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserConfigurationService;

public class Activator extends HousekeepingActivator {


    private static final Class<?>[] NEEDED_SERVICES = { TemplateService.class, ConfigurationService.class, ContextService.class, UserService.class,
        UserConfigurationService.class, HttpService.class, CapabilityService.class, DispatcherPrefixService.class, ConfigViewFactory.class };

    private String downloadServletAlias;
    private String updateServletAlias;
    private ServiceRegistration<Remote> serviceRegistration;

    @Override
    protected Class<?>[] getNeededServices() {
        return NEEDED_SERVICES;
    }

    @Override
    protected void startBundle() throws Exception {
        Services.setServiceLookup(this);
        DriveUpdateService updateService = new DriveUpdateServiceImpl();

        registerService(DriveUpdateService.class, updateService, null);

        //register files provider
        final ConfigurationService config = getService(ConfigurationService.class);
        String path = config.getProperty(Constants.BRANDINGS_PATH);
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
        getService(HttpService.class).registerServlet(updateServletAlias, new UpdatesXMLServlet(templateService, updateService), null, null);

        //register rmi interface
        Dictionary<String, Object> props = new Hashtable<String, Object>(2);
        props.put("RMIName", BrandingConfigurationRemote.RMI_NAME);
        serviceRegistration = context.registerService(Remote.class, new BrandingConfigurationRemoteImpl(), props);

    }

    @Override
    protected void stopBundle() throws Exception {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
            serviceRegistration = null;
        }
        HttpService httpService = getService(HttpService.class);
        if (httpService != null) {
            if (downloadServletAlias != null) {
                httpService.unregister(downloadServletAlias);
            }
            if (updateServletAlias != null) {
                httpService.unregister(updateServletAlias);
            }
        }
        Services.setServiceLookup(null);
        super.stopBundle();
    }
}
