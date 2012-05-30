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

import java.text.MessageFormat;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.jws.WebService;
import javax.xml.ws.Endpoint;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import com.openexchange.soap.cxf.WebserviceName;

/**
 * {@link WebserviceCollector}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class WebserviceCollector implements ServiceListener {

    private static final String WEBSERVICE_NAME = "WebserviceName";

    private final ConcurrentMap<String, Endpoint> endpoints = new ConcurrentHashMap<String, Endpoint>();

    private final BundleContext context;

    private volatile boolean open;

    public WebserviceCollector(final BundleContext context) {
        this.context = context;
    }

    @Override
    public void serviceChanged(final ServiceEvent event) {
        if (!open) {
            return;
        }
        if (event.getType() == ServiceEvent.REGISTERED) {
            final ServiceReference<?> ref = event.getServiceReference();
            add(ref);
        } else if (event.getType() == ServiceEvent.UNREGISTERING) {
            final ServiceReference<?> ref = event.getServiceReference();
            remove(ref);
        }
    }

    public void open() {
        try {
            final ServiceReference<?>[] allServiceReferences = context.getAllServiceReferences(null, null);
            for (final ServiceReference<?> serviceReference : allServiceReferences) {
                add(serviceReference);
            }

        } catch (final InvalidSyntaxException e) {
            // Impossible, no filter specified.
        }

        open = true;
    }

    public void close() {
        open = false;
        for (final Entry<String, Endpoint> entry : endpoints.entrySet()) {
            remove(entry.getKey(), entry.getValue());
        }
    }

    private void remove(final ServiceReference<?> ref) {
        final Object service = context.getService(ref);

        if (isWebservice(service)) {
            final String name = getName(ref, service);
            remove(name, service);
        }
    }

    private void add(final ServiceReference<?> ref) {
        final Object service = context.getService(ref);
        if (isWebservice(service)) {
            final String name = getName(ref, service);
            replace(name, service);
        }
    }

    private String getName(final ServiceReference<?> ref, final Object service) {
        // If an annotation is present with the name, use that
        {
            final WebserviceName webserviceName = service.getClass().getAnnotation(WebserviceName.class);
            if (webserviceName != null) {
                return webserviceName.value();
            }
        }
        // If a service property for WebserviceName is present, use that
        {
            final Object name = ref.getProperty(WEBSERVICE_NAME);
            if (name != null && !"".equals(name)) {
                return name.toString();
            }
        }
        // Next try the WebService annotation

        {
            final WebService webService = service.getClass().getAnnotation(WebService.class);
            String serviceName = webService.serviceName();
            if (serviceName != null && !("".equals(serviceName))) {
                return serviceName;
            }
            serviceName = webService.name();
            if (serviceName != null && !("".equals(serviceName))) {
                return serviceName;
            }
        }
        // Else use the class name
        return service.getClass().getSimpleName();
    }

    private void remove(final String name, final Object service) {
        final Endpoint endpoint = endpoints.remove(name);
        if (endpoint != null) {
            endpoint.stop();
        }
    }

    private void replace(final String name, final Object service) {
        final Endpoint oldEndpoint = endpoints.replace(name, Endpoint.publish(MessageFormat.format("/{0}", name), service));
        if (oldEndpoint != null) {
            oldEndpoint.stop();
        }
    }

    private boolean isWebservice(final Object service) {
        final WebService annotation = service.getClass().getAnnotation(WebService.class);
        return annotation != null;
    }

}
