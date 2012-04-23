/**
 *
 */

package com.openexchange.proxy.servlet.osgi;

import javax.servlet.ServletException;
import com.openexchange.log.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.proxy.servlet.Constants;
import com.openexchange.proxy.servlet.ProxyServlet;

/**
 * {@link ServletRegisterer}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ServletRegisterer implements ServiceTrackerCustomizer {

    private final BundleContext context;

    /**
     * Initializes a new {@link ServletRegisterer}.
     *
     * @param context The bundle context
     */
    public ServletRegisterer(final BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public Object addingService(final ServiceReference reference) {
        final Object service = context.getService(reference);
        tryRegistering((HttpService) service);
        return service;
    }

    private static void tryRegistering(final HttpService httpService) {
        if (httpService == null) {
            return;
        }
        try {
            httpService.registerServlet(Constants.PATH, new ProxyServlet(), null, null);
        } catch (final ServletException e) {
            com.openexchange.log.Log.valueOf(LogFactory.getLog(ServletRegisterer.class)).error(e.getMessage(), e);
        } catch (final NamespaceException e) {
            com.openexchange.log.Log.valueOf(LogFactory.getLog(ServletRegisterer.class)).error(e.getMessage(), e);
        }

    }

    @Override
    public void modifiedService(final ServiceReference reference, final Object service) {
        // Nope
    }

    @Override
    public void removedService(final ServiceReference reference, final Object service) {
        ((HttpService) service).unregister(Constants.PATH);
        context.ungetService(reference);
    }
}
