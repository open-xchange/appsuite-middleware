
package com.openexchange.spamsettings.generic.osgi;

import javax.servlet.ServletException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import com.openexchange.exception.OXException;
import com.openexchange.spamsettings.generic.preferences.SpamSettingsModulePreferences;
import com.openexchange.spamsettings.generic.servlet.SpamSettingsServlet;

/**
 * @author Benjamin Otterbach
 */
public class SpamSettingsServletRegisterer {

    private static final Log LOG = LogFactory.getLog(SpamSettingsServletRegisterer.class);

    // friend to be able to test
    final static String SERVLET_PATH = "/ajax/spamsettings";

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
            http_service.registerServlet(SERVLET_PATH, new SpamSettingsServlet(), null, null);
            LOG.info("Servlet " + SERVLET_PATH + " registered.");
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
        http_service.unregister(SERVLET_PATH);
        LOG.info("Servlet " + SERVLET_PATH + "unregistered.");
    }

}
