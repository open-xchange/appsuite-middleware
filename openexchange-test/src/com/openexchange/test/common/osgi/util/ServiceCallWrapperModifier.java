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

package com.openexchange.test.common.osgi.util;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceObjects;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.osgi.util.ServiceCallWrapper;
import com.openexchange.osgi.util.ServiceCallWrapper.BundleContextProvider;
import com.openexchange.osgi.util.ServiceCallWrapper.ServiceException;

/**
 * {@link ServiceCallWrapperModifier}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class ServiceCallWrapperModifier {

    public static void initTestRun(Map<Class<?>, Object> availableServices) {
        final BundleContext contextMock = new TestableBundleContext(availableServices);
        ServiceCallWrapper.BC_PROVIDER_REF.set(new BundleContextProvider() {

            @Override
            public BundleContext getBundleContext(Class<?> caller, Class<?> serviceClass) throws ServiceException {
                return contextMock;
            }
        });
    }

    private static final class TestableBundleContext implements BundleContext {

        private final Map<Class<?>, Object> availableServices;

        TestableBundleContext(Map<Class<?>, Object> availableServices) {
            super();
            this.availableServices = availableServices;
        }

        @Override
        public String getProperty(String key) {
            return null;
        }

        @Override
        public Bundle getBundle() {
            return null;
        }

        @Override
        public Bundle installBundle(String location, InputStream input) throws BundleException {
            return null;
        }

        @Override
        public Bundle installBundle(String location) throws BundleException {
            return null;
        }

        @Override
        public Bundle getBundle(long id) {
            return null;
        }

        @Override
        public Bundle[] getBundles() {
            return null;
        }

        @Override
        public void addServiceListener(ServiceListener listener, String filter) throws InvalidSyntaxException {

        }

        @Override
        public void addServiceListener(ServiceListener listener) {

        }

        @Override
        public void removeServiceListener(ServiceListener listener) {

        }

        @Override
        public void addBundleListener(BundleListener listener) {

        }

        @Override
        public void removeBundleListener(BundleListener listener) {

        }

        @Override
        public void addFrameworkListener(FrameworkListener listener) {

        }

        @Override
        public void removeFrameworkListener(FrameworkListener listener) {

        }

        @Override
        public ServiceRegistration<?> registerService(String[] clazzes, Object service, Dictionary<String, ?> properties) {
            return null;
        }

        @Override
        public ServiceRegistration<?> registerService(String clazz, Object service, Dictionary<String, ?> properties) {
            return null;
        }

        @Override
        public <S> ServiceRegistration<S> registerService(Class<S> clazz, S service, Dictionary<String, ?> properties) {
            return null;
        }

        @Override
        public ServiceReference<?>[] getServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
            return null;
        }

        @Override
        public ServiceReference<?>[] getAllServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
            return null;
        }

        @Override
        public ServiceReference<?> getServiceReference(String clazz) {
            return null;
        }

        @Override
        public <S> ServiceReference<S> getServiceReference(Class<S> clazz) {
            Object service = availableServices.get(clazz);
            if (service == null) {
                return null;
            }

            return (ServiceReference<S>) new TestableServiceReference(service);
        }

        @Override
        public <S> Collection<ServiceReference<S>> getServiceReferences(Class<S> clazz, String filter) throws InvalidSyntaxException {
            return null;
        }

        @Override
        public <S> S getService(ServiceReference<S> reference) {
            if (reference instanceof TestableServiceReference) {
                return (S) ((TestableServiceReference) reference).getService();
            }

            return null;
        }

        @Override
        public boolean ungetService(ServiceReference<?> reference) {
            return false;
        }

        @Override
        public File getDataFile(String filename) {
            return null;
        }

        @Override
        public Filter createFilter(String filter) throws InvalidSyntaxException {
            return null;
        }

        @Override
        public Bundle getBundle(String location) {
            return null;
        }

        @Override
        public <S> ServiceObjects<S> getServiceObjects(ServiceReference<S> arg0) {
            return null;
        }

        @Override
        public <S> ServiceRegistration<S> registerService(Class<S> arg0, ServiceFactory<S> arg1, Dictionary<String, ?> arg2) {
            return null;
        }

    }

    private static final class TestableServiceReference implements ServiceReference<Object> {

        private final Object service;

        TestableServiceReference(final Object service) {
            super();
            this.service = service;
        }

        @Override
        public Object getProperty(String key) {
            return null;
        }

        @Override
        public String[] getPropertyKeys() {
            return new String[0];
        }

        @Override
        public Bundle getBundle() {
            return null;
        }

        @Override
        public Bundle[] getUsingBundles() {
            return new Bundle[0];
        }

        @Override
        public boolean isAssignableTo(Bundle bundle, String className) {
            return true;
        }

        @Override
        public int compareTo(Object reference) {
            return 0;
        }

        public Object getService() {
            return service;
        }

        @Override
        public Dictionary<String, Object> getProperties() {
            return new Hashtable<>(0);
        }

    }

}
