package com.openexchange.outlook.updater.osgi;

import java.io.File;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.outlook.updater.ResourceLoader;
import com.openexchange.outlook.updater.UpdaterInstallerAssembler;
import com.openexchange.outlook.updater.UpdaterInstallerServlet;
import com.openexchange.outlook.updater.UpdaterXMLServlet;
import com.openexchange.server.osgiservice.DeferredActivator;
import com.openexchange.templating.TemplateService;
import com.openexchange.tools.service.ServletRegistration;

public class Activator extends DeferredActivator {
    private static final Class[] NEEDED_SERVICES = {ConfigurationService.class, HttpService.class, TemplateService.class, MailAccountStorageService.class};
    private static final String PATH_PROP = "com.openexchange.outlook.updater.path";
    private static final String ALIAS = "/ajax/updater/installer/*";
    private static final String UPDATER_XML_ALIAS = "/ajax/updater/update.xml";
    
    private ServletRegistration registration, xmlRegistration;
    private ResourceLoader loader;

    
	@Override
    protected Class<?>[] getNeededServices() {
        return NEEDED_SERVICES;
    }

    @Override
    protected void handleAvailability(Class<?> clazz) {
        configureLoader(getService(ConfigurationService.class));
        register();
    }

    @Override
    protected void handleUnavailability(Class<?> clazz) {
        unregister();
    }

    @Override
    protected void startBundle() throws Exception {
        ConfigurationService config = getService(ConfigurationService.class);
        if(config != null) {
            configureLoader(config);
            register();
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        unregister();
    }
    
    private void register() {
        if(registration != null) {
            return;
        }
        if(loader == null) {
            unregister();
            return;
        }
        UpdaterInstallerAssembler assembler = new UpdaterInstallerAssembler(loader);
        
        UpdaterInstallerServlet.setAssembler(assembler);
        UpdaterInstallerServlet.setAlias(ALIAS);
        
        UpdaterXMLServlet.setTemplateService(getService(TemplateService.class));
        UpdaterXMLServlet.setMailAccountStorageService(getService(MailAccountStorageService.class));
        
        registration = new ServletRegistration(context, new UpdaterInstallerServlet(), ALIAS);
        xmlRegistration = new ServletRegistration(context, new UpdaterXMLServlet(), UPDATER_XML_ALIAS);
    }
    
    public void unregister() {
        if(registration == null) {
            return;
        }
        registration.remove();
        registration = null;
        xmlRegistration.remove();
        xmlRegistration = null;
    }
    
    private void configureLoader(ConfigurationService config) {
        FileSystemResourceLoader fileSystemResourceLoader = new FileSystemResourceLoader();
        String path = config.getProperty(PATH_PROP);
        if(path == null) {
            loader = null;
            return;
        }
        config.getProperty(PATH_PROP, fileSystemResourceLoader);
        fileSystemResourceLoader.setParentDirectory(new File(path));
        
        BundleResourceLoader bundleLoader = new BundleResourceLoader(context.getBundle());
        
        loader = new CompositeResourceLoader(bundleLoader, fileSystemResourceLoader);
    }

}
