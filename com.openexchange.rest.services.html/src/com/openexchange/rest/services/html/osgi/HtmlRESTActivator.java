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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.rest.services.html.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.html.HtmlService;
import com.openexchange.rest.services.OXRESTService;
import com.openexchange.rest.services.html.HtmlRESTService;
import com.openexchange.rest.services.osgiservice.OXRESTActivator;

/**
 * {@link HtmlRESTActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HtmlRESTActivator extends OXRESTActivator {

    /**
     * Initializes a new {@link HtmlRESTActivator}.
     */
    public HtmlRESTActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return EMPTY_CLASSES;
    }

    @Override
    protected void startBundle() throws Exception {
        final BundleContext context = this.context;
        ServiceTrackerCustomizer<HtmlService, HtmlService> customizer = new ServiceTrackerCustomizer<HtmlService, HtmlService>() {

            @Override
            public HtmlService addingService(ServiceReference<HtmlService> reference) {
                HtmlService service = context.getService(reference);

                registerWebService(HtmlRESTService.class, service);

                return service;
            }

            @Override
            public void modifiedService(ServiceReference<HtmlService> reference, HtmlService service) {
                // Ignore
            }

            @Override
            public void removedService(ServiceReference<HtmlService> reference, HtmlService service) {
                unregisterWebService(HtmlRESTService.class);
            }
        };
        track(HtmlService.class, customizer);
        openTrackers();
    }

    @Override
    public <T> void registerWebService(Class<? extends OXRESTService<T>> serviceClass, T context) {
        super.registerWebService(serviceClass, context);
    }

    @Override
    public <T> void unregisterWebService(Class<? extends OXRESTService<T>> serviceClass) {
        super.unregisterWebService(serviceClass);
    }

}
