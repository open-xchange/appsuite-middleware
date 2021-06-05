/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */


package com.openexchange.spamsettings.generic.osgi;

import javax.servlet.ServletException;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.service.http.HttpServices;
import com.openexchange.spamsettings.generic.preferences.SpamSettingsModulePreferences;
import com.openexchange.spamsettings.generic.servlet.SpamSettingsServlet;

/**
 * @author Benjamin Otterbach
 */
public class SpamSettingsServletRegisterer {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SpamSettingsServletRegisterer.class);

    /**
     * The {@link DefaultDeferringURLService} reference.
     */
    public static final java.util.concurrent.atomic.AtomicReference<DispatcherPrefixService> PREFIX = new java.util.concurrent.atomic.AtomicReference<DispatcherPrefixService>();

    // friend to be able to test
    final static String SERVLET_PATH_APPENDIX = "spamsettings";

    private volatile String alias;

    public SpamSettingsServletRegisterer() {
        super();
    }

    public void registerServlet() {
        final HttpService httpService;
        try {
            httpService = SpamSettingsServiceRegistry.getServiceRegistry().getService(HttpService.class, true);
        } catch (OXException e) {
            LOG.error("Error registering spam settings servlet!", e);
            return;
        }
        try {
            String alias = PREFIX.get().getPrefix() + SERVLET_PATH_APPENDIX;
            httpService.registerServlet(alias, new SpamSettingsServlet(), null, null);
            this.alias = alias;
            LOG.info("Servlet {} registered.", alias);
            SpamSettingsModulePreferences.setModule(true);
        } catch (ServletException e) {
            LOG.error("Error registering spam settings servlet!", e);
        } catch (NamespaceException e) {
            LOG.error("Error registering spam settings servlet!", e);
        }
    }

    public void unregisterServlet() {
        final HttpService httpService;
        try {
            httpService = SpamSettingsServiceRegistry.getServiceRegistry().getService(HttpService.class, true);
        } catch (OXException e) {
            LOG.error("Error unregistering spam settings servlet!", e);
            return;
        }
        String alias = this.alias;
        if (null != alias) {
            this.alias = null;
            HttpServices.unregister(alias, httpService);
            LOG.info("Servlet {}unregistered.", alias);
        }
    }

}
