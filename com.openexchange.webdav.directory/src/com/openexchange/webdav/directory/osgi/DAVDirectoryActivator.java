
package com.openexchange.webdav.directory.osgi;

import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.tools.service.ServletRegistration;
import com.openexchange.webdav.directory.PathRegistration;
import com.openexchange.webdav.directory.servlets.WebdavDirectoryPerformer;
import com.openexchange.webdav.directory.servlets.WebdavDirectoryServlet;

public class DAVDirectoryActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { HttpService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        rememberTracker(new ServletRegistration(context, new WebdavDirectoryServlet(), "/servlet/dav"));
        track(PathRegistration.class, new SimpleRegistryListener<PathRegistration>() {

            @Override
            public void added(final ServiceReference<PathRegistration> ref, final PathRegistration thing) {
                WebdavDirectoryPerformer.getInstance().getFactory().mkdirs(thing.getPaths());
            }

            @Override
            public void removed(final ServiceReference<PathRegistration> ref, final PathRegistration thing) {
                // TODO Auto-generated method stub

            }

        });
        WebdavDirectoryPerformer.getInstance().getFactory().mkdirs("principals", "users");
        openTrackers();
    }
}
