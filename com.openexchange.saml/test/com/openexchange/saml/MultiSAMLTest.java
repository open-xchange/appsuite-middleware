
package com.openexchange.saml;

import java.util.Dictionary;
import javax.servlet.ServletException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.opensaml.DefaultBootstrap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.java.Strings;
import com.openexchange.saml.osgi.SAMLBackendRegistry;
import com.openexchange.saml.spi.CredentialProvider;
import com.openexchange.saml.spi.SAMLBackend;
import com.openexchange.server.SimpleServiceLookup;
import com.openexchange.session.reservation.SessionReservationService;
import com.openexchange.session.reservation.SimSessionReservationService;

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
    private BundleContext bundleContext;

    @BeforeClass
    public static void beforeClass() throws Exception {
        DefaultBootstrap.bootstrap();
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
        bundleContext = Mockito.mock(BundleContext.class);
        registry = new SAMLBackendRegistry(bundleContext, services);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSamlRegistration() throws Exception {
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
            Mockito.doNothing().doThrow(NamespaceException.class).when(httpService).registerServlet(Mockito.eq("/appsuite/api/saml/acs"), Mockito.any(javax.servlet.Servlet.class), Mockito.any(Dictionary.class), Mockito.any(HttpContext.class));
        } else {
            Mockito.doNothing().doThrow(NamespaceException.class).when(httpService).registerServlet(Mockito.eq("/appsuite/api/saml/" + path + "/acs"), Mockito.any(javax.servlet.Servlet.class), Mockito.any(Dictionary.class), Mockito.any(HttpContext.class));
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
    public void testSamlRegistrationShouldFailForbiddenPath() throws Exception {
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
