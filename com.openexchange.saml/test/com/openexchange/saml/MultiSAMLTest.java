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

package com.openexchange.saml;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opensaml.core.config.InitializationService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.config.ConfigurationService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.java.Strings;
import com.openexchange.saml.osgi.SAMLBackendRegistry;
import com.openexchange.saml.spi.CredentialProvider;
import com.openexchange.saml.spi.SAMLBackend;
import com.openexchange.server.SimpleServiceLookup;
import com.openexchange.session.reservation.SessionReservationService;
import com.openexchange.session.reservation.SimSessionReservationService;

/**
 * {@link MultiSAMLTest}
 *
 * @author <a href="mailto:felix.marx@open-xchange.com">Felix Marx</a>
 * @since v7.8.4
 */
public class MultiSAMLTest {

    private static TestConfig config;
    private static TestCredentials testCredentials;
    private static CredentialProvider credentialProvider;
    private static SimSessionReservationService sessionReservationService;
    private static SimpleServiceLookup services;
    private static HttpService httpService;
    private SAMLBackendRegistry registry;
    @Mock
    private ConfigurationService configurationService;

    @Mock
    private BundleContext bundleContext;

    @BeforeClass
    public static void beforeClass() throws Exception {
        InitializationService.initialize();
        testCredentials = new TestCredentials();
        credentialProvider = testCredentials.getSPCredentialProvider();

        /*
         * Init service provider
         */
        config = new TestConfig();

        services = new SimpleServiceLookup();
        sessionReservationService = new SimSessionReservationService();
        services.add(SessionReservationService.class, sessionReservationService);
        services.add(HazelcastInstance.class, Mockito.mock(HazelcastInstance.class));
        httpService = Mockito.mock(HttpService.class);
        services.add(HttpService.class, httpService);
        services.add(DispatcherPrefixService.class, new DispatcherPrefixService() {

            @Override
            public String getPrefix() {
                return "/appsuite/api/";
            }
        });
    }

    @Before
    public void before() throws Exception {
        MockitoAnnotations.initMocks(this);

        services.add(ConfigurationService.class, configurationService);
        Mockito.when(configurationService.getProperty(ArgumentMatchers.anyString())).thenReturn("bla");

        bundleContext = Mockito.mock(BundleContext.class);
        registry = new SAMLBackendRegistry(bundleContext, services);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSamlRegistration() {
        TestSAMLBackend samlBackend = new TestSAMLBackend(credentialProvider, config) {

            @Override
            public String getPath() {
                return "example";
            }
        };
        ServiceReference<SAMLBackend> reference = Mockito.mock(ServiceReference.class);
        Mockito.when(bundleContext.getService(reference)).thenReturn(samlBackend);
        registry.addingService(reference);
        Mockito.verify(bundleContext, Mockito.never()).ungetService(reference);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testMultiSamlRegistrationsShouldFail() throws Exception {
        final String path = "example";
        prepareServletMockitoCheck(path);
        TestSAMLBackend samlBackend = new TestSAMLBackend(credentialProvider, config) {

            @Override
            public String getPath() {
                return path;
            }
        };
        ServiceReference<SAMLBackend> reference = Mockito.mock(ServiceReference.class);
        Mockito.when(bundleContext.getService(reference)).thenReturn(samlBackend);
        registry.addingService(reference);
        TestSAMLBackend samlBackend2 = new TestSAMLBackend(credentialProvider, config) {

            @Override
            public String getPath() {
                return path;
            }
        };
        ServiceReference<SAMLBackend> reference2 = Mockito.mock(ServiceReference.class);
        Mockito.when(bundleContext.getService(reference2)).thenReturn(samlBackend2);
        registry.addingService(reference2);
        Mockito.verify(bundleContext, Mockito.never()).ungetService(reference);
        Mockito.verify(bundleContext, Mockito.atLeastOnce()).ungetService(reference2);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testMultiSamlRegistrationsShouldFailNullPath() throws Exception {
        final String path = null;
        prepareServletMockitoCheck(path);
        TestSAMLBackend samlBackend = new TestSAMLBackend(credentialProvider, config) {

            @Override
            public String getPath() {
                return path;
            }
        };
        ServiceReference<SAMLBackend> reference = Mockito.mock(ServiceReference.class);
        Mockito.when(bundleContext.getService(reference)).thenReturn(samlBackend);
        registry.addingService(reference);
        TestSAMLBackend samlBackend2 = new TestSAMLBackend(credentialProvider, config) {

            @Override
            public String getPath() {
                return path;
            }
        };
        ServiceReference<SAMLBackend> reference2 = Mockito.mock(ServiceReference.class);
        Mockito.when(bundleContext.getService(reference2)).thenReturn(samlBackend2);
        registry.addingService(reference2);
        Mockito.verify(bundleContext, Mockito.never()).ungetService(reference);
        Mockito.verify(bundleContext, Mockito.atLeastOnce()).ungetService(reference2);
    }

    private void prepareServletMockitoCheck(final String path) throws ServletException, NamespaceException {
        if (Strings.isEmpty(path)) {
            Mockito.doNothing().doThrow(NamespaceException.class).when(httpService).registerServlet(Mockito.eq("/appsuite/api/saml/acs"), Mockito.any(Servlet.class), Mockito.isNull(), Mockito.isNull());
        } else {
            Mockito.doNothing().doThrow(NamespaceException.class).when(httpService).registerServlet(Mockito.eq("/appsuite/api/saml/" + path + "/acs"), Mockito.any(Servlet.class), Mockito.isNull(), Mockito.isNull());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testMultiSamlRegistrations() throws Exception {
        final String testPath = "test";
        final String examplePath = "exaple";
        prepareServletMockitoCheck(testPath);
        prepareServletMockitoCheck(examplePath);
        TestSAMLBackend samlBackend = new TestSAMLBackend(credentialProvider, config) {

            @Override
            public String getPath() {
                return testPath;
            }
        };
        ServiceReference<SAMLBackend> reference = Mockito.mock(ServiceReference.class);
        Mockito.when(bundleContext.getService(reference)).thenReturn(samlBackend);
        registry.addingService(reference);
        TestSAMLBackend samlBackend2 = new TestSAMLBackend(credentialProvider, config) {

            @Override
            public String getPath() {
                return examplePath;
            }
        };
        ServiceReference<SAMLBackend> reference2 = Mockito.mock(ServiceReference.class);
        Mockito.when(bundleContext.getService(reference2)).thenReturn(samlBackend2);
        registry.addingService(reference2);
        Mockito.verify(bundleContext, Mockito.never()).ungetService(reference);
        Mockito.verify(bundleContext, Mockito.never()).ungetService(reference2);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSamlRegistrationShouldFailForbiddenPath() {
        TestSAMLBackend samlBackend = new TestSAMLBackend(credentialProvider, config) {

            @Override
            public String getPath() {
                return "%&/%!";
            }
        };
        ServiceReference<SAMLBackend> reference = Mockito.mock(ServiceReference.class);
        Mockito.when(bundleContext.getService(reference)).thenReturn(samlBackend);
        registry.addingService(reference);
        Mockito.verify(bundleContext, Mockito.atLeastOnce()).ungetService(reference);
    }
}
