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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.osgi;

import java.util.Collection;
import java.util.Stack;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;

/**
 * {@link Tools}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Tools {

    /**
     * Generates an OR filter matching the services given in the classes varargs.
     * @throws InvalidSyntaxException if the syntax of the generated filter is not correct.
     */
    public static final Filter generateServiceFilter(final BundleContext context, final Class<?>... classes) throws InvalidSyntaxException {
        if (classes.length < 2) {
            throw new IllegalArgumentException("At least the classes of 2 services must be given.");
        }
        final StringBuilder sb = new StringBuilder("(|(");
        for (final Class<?> clazz : classes) {
            sb.append(Constants.OBJECTCLASS);
            sb.append('=');
            sb.append(clazz.getName());
            sb.append(")(");
        }
        sb.setCharAt(sb.length() - 1, ')');
        return context.createFilter(sb.toString());
    }

    public static final void open(Collection<ServiceTracker<?,?>> trackers) {
        for (ServiceTracker<?,?> tracker : trackers) {
            tracker.open();
        }
    }

    public static final void close(Stack<ServiceTracker<?,?>> trackers) {
        while (!trackers.isEmpty()) {
            trackers.pop().close();
        }
    }

    /**
     * Obtains a service from the given {@link ServiceLookup} and returns it. If the
     * service is not available, {@link ServiceExceptionCode#SERVICE_UNAVAILABLE} is thrown.
     * @param serviceClass The service class to obtain
     * @param serviceLookup The service lookup to obtain the service from
     * @return The service
     * @throws OXException if the service is not available
     */
    public static <T> T requireService(Class<T> serviceClass, ServiceLookup serviceLookup) throws OXException {
        T service = serviceLookup.getService(serviceClass);
        if (service == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(serviceClass.getName());
        }

        return service;
    }

    private Tools() {
        super();
    }
}
