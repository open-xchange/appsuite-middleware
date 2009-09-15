package com.openexchange.outlook.updater.osgi;

import java.io.File;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import com.openexchange.config.ConfigurationService;
import com.openexchange.outlook.updater.ResourceLoader;
import com.openexchange.outlook.updater.UpdaterInstallerAssembler;
import com.openexchange.outlook.updater.UpdaterInstallerServlet;
import com.openexchange.server.osgiservice.DeferredActivator;
import com.openexchange.tools.service.ServletRegistration;

public class Activator extends DeferredActivator {
    private static final Class[] NEEDED_SERVICES = {ConfigurationService.class};
    private static final String PATH_PROP = "com.openexchange.outlook.updater.path";
    private static final String ALIAS = "/ajax/updater/installer/*";
    
    private ServletRegistration registration;
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
        
        registration = new ServletRegistration(context, new UpdaterInstallerServlet(), ALIAS);
    }
    
    public void unregister() {
        if(registration == null) {
            return;
        }
        registration.remove();
        registration = null;
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
