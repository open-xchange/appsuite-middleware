/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.soap.cxf.osgi;

import org.apache.commons.logging.Log;
import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.osgi.service.http.HttpService;
import com.openexchange.log.LogFactory;
import com.openexchange.osgi.DeferredActivator;

/**
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CXFActivator extends DeferredActivator {

    private static final String PATH = "/webservices"; // /servlet/axis2/services

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(CXFActivator.class));

    private WebserviceCollector collector;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { HttpService.class };
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        // Nope
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        // Nope
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            LOG.info("Starting Bundle :com.openexchange.soap.cxf");
            final HttpService httpService = getService(HttpService.class);
            final CXFNonSpringServlet cxf = new CXFNonSpringServlet();
            final Bus bus = cxf.getBus();
            BusFactory.setDefaultBus(bus);

            LOG.info("Registering CXF servlet under " + PATH);
            httpService.registerServlet(PATH, cxf, null, null);

            collector = new WebserviceCollector(context);
            context.addServiceListener(collector);
            collector.open();
            LOG.info("com.openexchange.soap.cxf is up and running");
        } catch (final Throwable t) {
            LOG.error(t.getMessage(), t);
        }

    }

    @Override
    protected void stopBundle() throws Exception {
        final HttpService httpService = getService(HttpService.class);
        if (httpService != null) {
            httpService.unregister(PATH);
        }
        if (null != collector) {
            collector.close();
            collector = null;
        }
    }

}
