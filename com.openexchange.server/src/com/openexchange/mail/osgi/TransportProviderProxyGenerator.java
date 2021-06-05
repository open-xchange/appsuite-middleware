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

package com.openexchange.mail.osgi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import com.openexchange.mail.transport.TransportProvider;

/**
 * {@link TransportProviderProxyGenerator} - Generates proxy objects for mail provider which delegate method invocations to the service
 * obtained from a bundle context
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TransportProviderProxyGenerator {

    /**
     * TODO: Does not work since {@link TransportProvider} is not an interface
     * <p>
     * Create a new proxy object for mail provider which delegates method invocations to the service obtained from bundle context
     *
     * @param mailProviderServiceReference The service reference of a mail provider
     * @param context The bundle context (needed to get/unget the service)
     * @return A new proxy object for mail provider
     */
    public static TransportProvider newTransportProviderProxy(ServiceReference mailProviderServiceReference, BundleContext context) {
        return (TransportProvider) java.lang.reflect.Proxy.newProxyInstance(
            TransportProvider.class.getClassLoader(),
            new Class<?>[] { TransportProvider.class },
            new MailProviderInvocationHandler(mailProviderServiceReference, context));
    }

    /**
     * Initializes a new {@link TransportProviderProxyGenerator}
     */
    private TransportProviderProxyGenerator() {
        super();
    }

    private static final class MailProviderInvocationHandler implements java.lang.reflect.InvocationHandler {

        private final BundleContext context;

        private final ServiceReference transportProviderServiceReference;

        /**
         * Initializes a new {@link TransportProviderProxyGenerator}
         */
        private MailProviderInvocationHandler(ServiceReference transportProviderServiceReference, BundleContext context) {
            super();
            this.transportProviderServiceReference = transportProviderServiceReference;
            this.context = context;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object result;
            try {
                final TransportProvider provider = (TransportProvider) context.getService(transportProviderServiceReference);
                try {
                    result = method.invoke(provider, args);
                } finally {
                    context.ungetService(transportProviderServiceReference);
                }
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            } catch (Exception e) {
                throw new RuntimeException("unexpected invocation exception: " + e.getMessage(), e);
            }
            return result;
        }
    }

}
