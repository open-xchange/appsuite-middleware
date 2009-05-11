
package com.openexchange.datatypes.genericonf.storage.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.datatypes.genericonf.storage.GenericConfigurationStorageService;
import com.openexchange.datatypes.genericonf.storage.impl.MySQLGenericConfigurationStorage;
import com.openexchange.groupware.tx.osgi.WhiteboardDBProvider;

public class Activator implements BundleActivator {

    private ServiceRegistration serviceRegistration;

    public void start(BundleContext context) throws Exception {
        MySQLGenericConfigurationStorage mySQLGenericConfigurationStorage = new MySQLGenericConfigurationStorage();
        mySQLGenericConfigurationStorage.setDBProvider(new WhiteboardDBProvider(context));
        serviceRegistration = context.registerService(
            GenericConfigurationStorageService.class.getName(),
            mySQLGenericConfigurationStorage,
            null);
    }

    public void stop(BundleContext context) throws Exception {
        serviceRegistration.unregister();
    }

}
