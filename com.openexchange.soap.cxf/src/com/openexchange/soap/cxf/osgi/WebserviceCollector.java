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

package com.openexchange.soap.cxf.osgi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.jws.WebService;
import javax.xml.ws.Endpoint;
import org.apache.cxf.endpoint.ServerImpl;
import org.apache.cxf.frontend.WSDLGetUtils;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.wsdl.interceptors.DocLiteralInInterceptor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import com.openexchange.soap.cxf.ExceptionUtils;
import com.openexchange.soap.cxf.WebserviceName;
import com.openexchange.soap.cxf.interceptor.MetricsInterceptor;

/**
 * {@link WebserviceCollector}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class WebserviceCollector implements ServiceListener {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(WebserviceCollector.class);

    private static final String WEBSERVICE_NAME = "WebserviceName";

    private final ConcurrentMap<String, Endpoint> endpoints;
    private final BundleContext context;
    private final String baseUri;
    private volatile boolean open;

    /**
     * Initializes a new {@link WebserviceCollector}.
     *
     * @param context The bundle context
     */
    public WebserviceCollector(String baseUri, BundleContext context) {
        super();
        this.baseUri = baseUri;
        endpoints = new ConcurrentHashMap<String, Endpoint>();
        this.context = context;
    }

    @Override
    public void serviceChanged(final ServiceEvent event) {
        if (!open) {
            return;
        }
        final int type = event.getType();
        if (ServiceEvent.REGISTERED == type) {
            add(event.getServiceReference());
        } else if (ServiceEvent.UNREGISTERING == type) {
            remove(event.getServiceReference());
        }
    }

    /**
     * Opens this collector.
     */
    public void open() {
        try {
            for (final ServiceReference<?> serviceReference : context.getAllServiceReferences(null, null)) {
                add(serviceReference);
            }
        } catch (InvalidSyntaxException e) {
            // Impossible, no filter specified.
        }

        open = true;
    }

    /**
     * Closes this collector.
     */
    public void close() {
        open = false;
        for (final Entry<String, Endpoint> entry : new ArrayList<Entry<String, Endpoint>>(endpoints.entrySet())) {
            remove(entry.getKey());
        }
    }

    private void remove(final ServiceReference<?> ref) {
        final Object service = context.getService(ref);
        if (isWebservice(service)) {
            final String name = getName(ref, service);
            remove(name);
        } else {
            context.ungetService(ref);
        }
    }

    private void add(final ServiceReference<?> ref) {
        final Object service = context.getService(ref);
        if (isWebservice(service)) {
            final String name = getName(ref, service);
            replace(name, service);
        } else {
            context.ungetService(ref);
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
            final String sName = null == name ? null : name.toString();
            if (!com.openexchange.java.Strings.isEmpty(sName)) {
                return sName;
            }
        }
        // Next try the WebService annotation
        {
            final WebService webService = service.getClass().getAnnotation(WebService.class);
            String serviceName = webService.serviceName();
            if (!com.openexchange.java.Strings.isEmpty(serviceName)) {
                return serviceName;
            }
            serviceName = webService.name();
            if (!com.openexchange.java.Strings.isEmpty(serviceName)) {
                return serviceName;
            }
        }
        // Else use the class name
        return service.getClass().getSimpleName();
    }

    private void remove(final String name) {
        final Endpoint endpoint = endpoints.remove(name);
        if (endpoint != null) {
            endpoint.stop();
        }
    }

    private void replace(final String name, final Object service) {
        String address = '/' + name; // MessageFormat.format("/{0}", name);
        Endpoint oldEndpoint;
        try {
            // Publish new server endpoint
            final Endpoint endpoint = Endpoint.create(service);
            {
                org.apache.cxf.jaxws.EndpointImpl endpointImpl = (org.apache.cxf.jaxws.EndpointImpl) endpoint;
                if (null != baseUri) {
                    ServerImpl serv = endpointImpl.getServer(address);
                    EndpointInfo endpointInfo = serv.getEndpoint().getEndpointInfo();
                    String publishedEndpointUrl = baseUri + address;
                    endpointInfo.setProperty(WSDLGetUtils.PUBLISHED_ENDPOINT_URL, publishedEndpointUrl);
                }
            }
            endpoint.publish(address);
            {
                // Alter server's in-stream interceptors
                final org.apache.cxf.endpoint.Endpoint serverEndpoint = (org.apache.cxf.endpoint.Endpoint) endpoint.getProperties();
                {
                    final List<Interceptor<? extends Message>> inInterceptors = serverEndpoint.getBinding().getInInterceptors();
                    boolean found = false;
                    int index = 0;
                    for (final Interceptor<? extends Message> interceptor : inInterceptors) {
                        if (interceptor instanceof DocLiteralInInterceptor) {
                            found = true;
                            break;
                        }
                        index++;
                    }
                    if (found) {
                        inInterceptors.remove(index);
                        inInterceptors.add(index, new com.openexchange.soap.cxf.interceptor.DocLiteralInInterceptor());
                    }
                }
                {
                    final List<Interceptor<? extends Message>> inInterceptors = serverEndpoint.getBinding().getInInterceptors();
                    boolean found = false;
                    int index = 0;
                    for (final Interceptor<? extends Message> interceptor : inInterceptors) {
                        if (interceptor instanceof org.apache.cxf.binding.soap.interceptor.SoapActionInInterceptor) {
                            found = true;
                            break;
                        }
                        index++;
                    }
                    if (found) {
                        inInterceptors.remove(index);
                        inInterceptors.add(index, new com.openexchange.soap.cxf.interceptor.SoapActionInInterceptor());
                    }
                }
                {
                    final List<Interceptor<? extends Message>> outInterceptors = serverEndpoint.getBinding().getOutFaultInterceptors();
                    boolean found = false;
                    int index = 0;
                    for (final Interceptor<? extends Message> interceptor : outInterceptors) {
                        if (interceptor instanceof org.apache.cxf.binding.xml.interceptor.XMLFaultOutInterceptor) {
                            found = true;
                            break;
                        }
                        index++;
                    }
                    if (found) {
                        outInterceptors.remove(index);
                        outInterceptors.add(index, new com.openexchange.soap.cxf.interceptor.XMLFaultOutInterceptor());
                    }
                }
                {
                    final List<Interceptor<? extends Message>> inInterceptors = serverEndpoint.getInInterceptors();
                    boolean found = false;
                    int index = 0;
                    for (final Interceptor<? extends Message> interceptor : inInterceptors) {
                        if (interceptor instanceof org.apache.cxf.frontend.WSDLGetInterceptor) {
                            found = true;
                            break;
                        }
                        index++;
                    }
                    if (found) {
                        inInterceptors.remove(index);
                        inInterceptors.add(index, new com.openexchange.soap.cxf.interceptor.HttpsAwareWSDLGetInterceptor());
                    }
                }
                // Add logging interceptors
                serverEndpoint.getInInterceptors().add(new com.openexchange.soap.cxf.interceptor.LoggingInInterceptor());
                serverEndpoint.getOutInterceptors().add(new com.openexchange.soap.cxf.interceptor.LoggingOutInterceptor());
                // Add metric interceptors
                serverEndpoint.getInInterceptors().add(new MetricsInterceptor(Phase.RECEIVE));
                serverEndpoint.getOutInterceptors().add(new MetricsInterceptor(Phase.SEND));
                serverEndpoint.getOutFaultInterceptors().add(new MetricsInterceptor(Phase.SEND));
            }
            oldEndpoint = endpoints.replace(name, endpoint);
            LOG.info("Publishing endpoint succeeded. Published \"{}\" under address \"{}\".", name, address);
        } catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            LOG.error("Publishing endpoint failed. Couldn't publish \"{}\" under address \"{}\".", name, address, t);
            oldEndpoint = null;
        }
        if (oldEndpoint != null) {
            oldEndpoint.stop();
        }
    }

    private boolean isWebservice(final Object service) {
        try {
            final Class<? extends Object> clazz = service.getClass();
            return (null == clazz) ? false : (null != clazz.getAnnotation(WebService.class));
        } catch (Exception e) {
            return false;
        }
    }
}
