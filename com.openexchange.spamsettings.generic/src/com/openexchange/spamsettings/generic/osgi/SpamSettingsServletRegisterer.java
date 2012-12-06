
package com.openexchange.spamsettings.generic.osgi;

import javax.servlet.ServletException;
import org.apache.commons.logging.Log;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.spamsettings.generic.preferences.SpamSettingsModulePreferences;
import com.openexchange.spamsettings.generic.servlet.SpamSettingsServlet;

/**
 * @author Benjamin Otterbach
 */
public class SpamSettingsServletRegisterer {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(SpamSettingsServletRegisterer.class);

    /**
     * The {@link DefaultDeferringURLService} reference.
     */
    public static final java.util.concurrent.atomic.AtomicReference<DispatcherPrefixService> PREFIX = new java.util.concurrent.atomic.AtomicReference<DispatcherPrefixService>();

    // friend to be able to test
    final static String SERVLET_PATH_APPENDIX = "spamsettings";

    public SpamSettingsServletRegisterer() {
        super();
    }

    public void registerServlet() {
        final HttpService http_service;
        try {
            http_service = SpamSettingsServiceRegistry.getServiceRegistry().getService(HttpService.class, true);
        } catch (final OXException e) {
            LOG.error("Error registering spam settings servlet!", e);
            return;
        }
        try {
            final String alias = PREFIX.get().getPrefix() + SERVLET_PATH_APPENDIX;
            http_service.registerServlet(alias, new SpamSettingsServlet(), null, null);
            LOG.info("Servlet " + alias + " registered.");
            SpamSettingsModulePreferences.setModule(true);
        } catch (final ServletException e) {
            LOG.error("Error registering spam settings servlet!", e);
        } catch (final NamespaceException e) {
            LOG.error("Error registering spam settings servlet!", e);
        }
    }

    public void unregisterServlet() {
        final HttpService http_service;
        try {
            http_service = SpamSettingsServiceRegistry.getServiceRegistry().getService(HttpService.class, true);
        } catch (final OXException e) {
            LOG.error("Error unregistering spam settings servlet!", e);
            return;
        }
        final String alias = PREFIX.get().getPrefix() + SERVLET_PATH_APPENDIX;
        http_service.unregister(alias);
        LOG.info("Servlet " + alias + "unregistered.");
    }

}
