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

package com.openexchange.ajax.osgi;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import org.apache.commons.logging.Log;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import com.openexchange.log.LogFactory;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link AbstractServletActivator}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public abstract class AbstractServletActivator extends HousekeepingActivator {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(AbstractServletActivator.class));

    private final List<String> servlets = new ArrayList<String>();

    protected void registerServlet(final String alias, final HttpServlet servlet, final HttpService httpService) {
        registerServlet(alias, servlet, null, httpService);
    }

    protected void registerServlet(final String alias, final HttpServlet servlet, final Dictionary<String, String> params, final HttpService httpService) {
        try {
            httpService.registerServlet(alias, servlet, params, null);
            servlets.add(alias);
        } catch (final ServletException e) {
            LOG.error(e.getMessage(), e);
        } catch (final NamespaceException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Override
    protected void cleanUp() {
        unregisterServlets();
        super.cleanUp();
    }

    private void unregisterServlets() {
        final HttpService httpService = getService(HttpService.class);
        if (null != httpService) {
            for (final String servlet : servlets) {
                try {
                    httpService.unregister(servlet);
                } catch (final Exception e) {
                    LOG.warn("Failed to unregister servlet alias: " + servlet, e);
                }
            }
        }
    }

    protected void unregisterServlet(final String alias) {
        final HttpService httpService = getService(HttpService.class);
        if (null != httpService) {
            httpService.unregister(alias);
        }
        servlets.remove(alias);
    }

}
