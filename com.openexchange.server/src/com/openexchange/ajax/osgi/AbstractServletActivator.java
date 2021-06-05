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

package com.openexchange.ajax.osgi;

import java.util.Dictionary;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.service.http.HttpServices;

/**
 * {@link AbstractServletActivator}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public abstract class AbstractServletActivator extends HousekeepingActivator {

    private final Queue<String> servlets = new ConcurrentLinkedQueue<String>();

    /**
     * Initializes a new {@link AbstractServletActivator}.
     */
    protected AbstractServletActivator() {
        super();
    }

    /**
     * Registers specified Servlet under given alias.
     *
     * @param alias The alias
     * @param servlet The Servlet instance
     * @param httpService The HTTP service
     * @return <code>true</code> on successful Servlet registration; otherwise <code>false</code>
     */
    protected boolean registerServlet(String alias, HttpServlet servlet, HttpService httpService) {
        return registerServlet(alias, servlet, null, httpService);
    }

    /**
     * Registers specified Servlet for given alias.
     *
     * @param alias The alias
     * @param servlet The Servlet instance
     * @param params Optional parameters
     * @param httpService The HTTP service
     * @return <code>true</code> on successful Servlet registration; otherwise <code>false</code>
     */
    protected boolean registerServlet(String alias, HttpServlet servlet, Dictionary<String, String> params, HttpService httpService) {
        try {
            httpService.registerServlet(alias, servlet, params, null);
            servlets.offer(alias);
            return true;
        } catch (ServletException e) {
            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AbstractServletActivator.class);
            logger.error("", e);
        } catch (NamespaceException e) {
            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AbstractServletActivator.class);
            logger.error("", e);
        }
        return false;
    }

    @Override
    protected void cleanUp() {
        unregisterServlets();
        super.cleanUp();
    }

    /**
     * Unregisters all previously registered Servlet instances.
     */
    private void unregisterServlets() {
        HttpService httpService = getService(HttpService.class);
        if (null != httpService) {
            for (String alias : servlets) {
                try {
                    HttpServices.unregister(alias, httpService);
                } catch (Exception e) {
                    org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AbstractServletActivator.class);
                    logger.warn("Failed to unregister servlet alias: {}", alias, e);
                }
            }
            servlets.clear();
        }
    }

    /**
     * Unregisters the Servlet instance registered for given alias
     *
     * @param alias The alias
     */
    protected void unregisterServlet(String alias) {
        if (servlets.remove(alias)) {
            HttpService httpService = getService(HttpService.class);
            if (null != httpService) {
                HttpServices.unregister(alias, httpService);
            }
        }
    }

}
