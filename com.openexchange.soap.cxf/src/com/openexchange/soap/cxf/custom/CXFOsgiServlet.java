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

package com.openexchange.soap.cxf.custom;

import java.lang.reflect.Field;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import org.apache.cxf.transport.http.DestinationRegistry;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.apache.cxf.transport.servlet.ServletController;
import org.apache.cxf.transport.servlet.servicelist.ServiceListGeneratorServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link CXFOsgiServlet}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class CXFOsgiServlet extends CXFNonSpringServlet {

    private static final Logger LOG = LoggerFactory.getLogger(CXFOsgiServlet.class);

    /** serialVersionUID */
    private static final long serialVersionUID = 2941782846712141279L;

    @Override
    protected void finalizeServletInit(ServletConfig servletConfig) throws ServletException {
        // do not call super.finalizeServletInit(...) as reading files from WEB-INF folder is not possible
    }

    @Override
    protected ServletController createServletController(ServletConfig servletConfig) {
        Field f = null;
        try {
            f = CXFNonSpringServlet.class.getDeclaredField("destinationRegistry");
            f.setAccessible(true);
            DestinationRegistry registry = (DestinationRegistry) f.get(this);

            HttpServlet serviceListGeneratorServlet = new ServiceListGeneratorServlet(registry, bus);
            return new CustomServletController(registry, servletConfig, serviceListGeneratorServlet);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            LOG.error("Unable to create custom ServletController. Falling back to the default one.", e);
            return super.createServletController(servletConfig);
        } finally {
            if (f != null) {
                f.setAccessible(false);
            }
        }
    }
}
