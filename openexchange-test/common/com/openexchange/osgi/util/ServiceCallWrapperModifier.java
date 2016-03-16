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

package com.openexchange.osgi.util;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.Dictionary;
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
            BundleContext getBundleContext(Class<?> caller, Class<?> serviceClass) throws ServiceException {
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
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Bundle getBundle() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Bundle installBundle(String location, InputStream input) throws BundleException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Bundle installBundle(String location) throws BundleException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Bundle getBundle(long id) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Bundle[] getBundles() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void addServiceListener(ServiceListener listener, String filter) throws InvalidSyntaxException {
            // TODO Auto-generated method stub

        }

        @Override
        public void addServiceListener(ServiceListener listener) {
            // TODO Auto-generated method stub

        }

        @Override
        public void removeServiceListener(ServiceListener listener) {
            // TODO Auto-generated method stub

        }

        @Override
        public void addBundleListener(BundleListener listener) {
            // TODO Auto-generated method stub

        }

        @Override
        public void removeBundleListener(BundleListener listener) {
            // TODO Auto-generated method stub

        }

        @Override
        public void addFrameworkListener(FrameworkListener listener) {
            // TODO Auto-generated method stub

        }

        @Override
        public void removeFrameworkListener(FrameworkListener listener) {
            // TODO Auto-generated method stub

        }

        @Override
        public ServiceRegistration<?> registerService(String[] clazzes, Object service, Dictionary<String, ?> properties) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ServiceRegistration<?> registerService(String clazz, Object service, Dictionary<String, ?> properties) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public <S> ServiceRegistration<S> registerService(Class<S> clazz, S service, Dictionary<String, ?> properties) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ServiceReference<?>[] getServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ServiceReference<?>[] getAllServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ServiceReference<?> getServiceReference(String clazz) {
            // TODO Auto-generated method stub
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
            // TODO Auto-generated method stub
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
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public File getDataFile(String filename) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Filter createFilter(String filter) throws InvalidSyntaxException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Bundle getBundle(String location) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public <S> ServiceObjects<S> getServiceObjects(ServiceReference<S> arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public <S> ServiceRegistration<S> registerService(Class<S> arg0, ServiceFactory<S> arg1, Dictionary<String, ?> arg2) {
            // TODO Auto-generated method stub
            return null;
        }

    }

    private static final class TestableServiceReference implements ServiceReference<Object> {

        private final Object service;

        private TestableServiceReference(final Object service) {
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

    }

}
