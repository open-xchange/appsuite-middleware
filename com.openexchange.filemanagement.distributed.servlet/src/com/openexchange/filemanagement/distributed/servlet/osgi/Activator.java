
package com.openexchange.filemanagement.distributed.servlet.osgi;

import org.osgi.service.http.HttpService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.filemanagement.distributed.servlet.DistributedFileServlet;
import com.openexchange.osgi.HousekeepingActivator;

public class Activator extends HousekeepingActivator {

    private volatile String alias;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { HttpService.class, ManagedFileManagement.class, ConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        HttpService service = getService(HttpService.class);
        String alias = com.openexchange.filemanagement.DistributedFileManagement.PATH;
        service.registerServlet(alias, new DistributedFileServlet(this), null, null);
        this.alias = alias;
    }

    @Override
    protected void stopBundle() throws Exception {
        HttpService service = getService(HttpService.class);
        if (null != service) {
            String alias = this.alias;
            if (null != alias) {
                this.alias = null;
                service.unregister(alias);
            }
        }
        super.stopBundle();
    }

}
