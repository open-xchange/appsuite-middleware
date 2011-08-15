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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.easylogin.osgi;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.ServletException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.config.ConfigurationService;
import com.openexchange.easylogin.EasyLogin;

/**
 * {@link ServletRegisterer}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class ServletRegisterer implements ServiceTrackerCustomizer<Object, Object> {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(ServletRegisterer.class));

    private static final String ALIAS = "servlet/easylogin";

    private final BundleContext context;

    private final Lock lock = new ReentrantLock();

    private ConfigurationService configService;

    private HttpService httpService;

    private boolean isRegistered;

    public ServletRegisterer(final BundleContext context){
        super();
        this.context = context;
    }

    @Override
    public Object addingService(final ServiceReference<Object> reference) {
        final Object service = context.getService(reference);
        boolean needsRegistration = false;
        lock.lock();
        try {
            if (service instanceof ConfigurationService) {
                configService = (ConfigurationService) service;
            }
            if (service instanceof HttpService) {
                httpService = (HttpService) service;
            }
            if (configService != null && httpService != null && !isRegistered) {
                needsRegistration = true;
                isRegistered = true;
            }
        } finally {
            lock.unlock();
        }
        if (needsRegistration) {
            try {
                httpService.registerServlet(ALIAS, new EasyLogin(), configService.getFile("easylogin.properties"), null);
                LOG.info(EasyLogin.class.getName() + " successfully registered");
            } catch (final ServletException e) {
                LOG.error("EasyLogin servlet can not be registered.", e);
            } catch (final NamespaceException e) {
                LOG.error("EasyLogin servlet can not be registered.", e);
            }
        }
        return service;
    }

    @Override
    public void modifiedService(final ServiceReference<Object> reference, final Object service) {
        // nothing to do here
    }

    @Override
    public void removedService(final ServiceReference<Object> reference, final Object service) {
        HttpService leavingService = null;
        boolean needsUnregistration = false;
        if (service instanceof ConfigurationService) {
            configService = null;
        }
        if (service instanceof HttpService) {
            httpService = null;
            leavingService = (HttpService) service;
        }
        lock.lock();
        try {
            if (null != leavingService && isRegistered) {
                needsUnregistration = true;
                isRegistered = false;
            }
        } finally {
            lock.unlock();
        }
        if (null != leavingService && needsUnregistration) {
            leavingService.unregister(ALIAS);
            LOG.info(EasyLogin.class.getName() + " successfully unregistered");
        }
    }
}
