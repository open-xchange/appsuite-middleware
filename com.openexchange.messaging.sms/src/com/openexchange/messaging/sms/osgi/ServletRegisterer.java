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

package com.openexchange.messaging.sms.osgi;

import javax.servlet.ServletException;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.service.http.HttpServices;

/**
 *
 * @author Benjamin Otterbach
 *
 */
public class ServletRegisterer {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ServletRegisterer.class);

    /**
     * The {@link DefaultDeferringURLService} reference.
     */
    public static final java.util.concurrent.atomic.AtomicReference<DispatcherPrefixService> PREFIX = new java.util.concurrent.atomic.AtomicReference<DispatcherPrefixService>();

    // friend to be able to test
    final static String SERVLET_PATH_APPENDIX = "messaging/sms";

    private String alias;

    /**
     * Initializes a new {@link ServletRegisterer}.
     */
    public ServletRegisterer () {
        super();
    }

    public synchronized void registerServlet() {
        final HttpService http_service;
        try {
            http_service = MessagingSMSServiceRegistry.getServiceRegistry().getService(HttpService.class, true);
        } catch (OXException e) {
            LOG.error("Error registering messaging SMS servlet!", e);
            return;
        }
        try {
            String alias = PREFIX.get().getPrefix()+SERVLET_PATH_APPENDIX;
            http_service.registerServlet(alias, new com.openexchange.messaging.sms.servlet.MessagingSMSServlet(), null, null);
            this.alias = alias;
        } catch (ServletException e) {
            LOG.error("Error registering messaging SMS servlet!", e);
        } catch (NamespaceException e) {
            LOG.error("Error registering messaging SMS servlet!", e);
        }
    }

    public synchronized void unregisterServlet() {
        final HttpService http_service;
        try {
            http_service = MessagingSMSServiceRegistry.getServiceRegistry().getService(HttpService.class, true);
        } catch (OXException e) {
            LOG.error("Error unregistering messaging SMS servlet!", e);
            return;
        }
        String alias = this.alias;
        if (null != alias) {
            HttpServices.unregister(PREFIX.get().getPrefix()+SERVLET_PATH_APPENDIX, http_service);
            this.alias = null;
        }
    }
}
