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

package com.openexchange.publish.osgi;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.file.storage.composition.IDBasedFolderAccessFactory;
import com.openexchange.osgi.ServiceProvider;
import com.openexchange.osgi.SimpleServiceProvider;
import com.openexchange.test.mock.InjectionFieldConstants;
import com.openexchange.test.mock.MockUtils;
import com.openexchange.test.mock.assertion.ServiceMockActivatorAsserter;

/**
 * Unit tests for {@link LoaderActivator}
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4.1
 */
@RunWith(PowerMockRunner.class)
public class LoaderActivatorTest {

    /**
     * Class under test
     */
    @InjectMocks
    private LoaderActivator loaderActivator = null;

    /**
     * {@link BundleContext} mock
     */
    @Mock
    private BundleContext bundleContext;

    /**
     * {@link Bundle} mock
     */
    @Mock
    private Bundle bundle;

    /**
     * {@link IDBasedFileAccessFactory} mock
     */
    @Mock
    private IDBasedFileAccessFactory idBasedFileAccessFactory;

    /**
     * {@link IDBasedFolderAccessFactory} mock
     */
    @Mock
    private IDBasedFolderAccessFactory idBasedFolderAccessFactory;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        this.loaderActivator = new LoaderActivator();

        // SERVICES
        ConcurrentMap<Class<?>, ServiceProvider<?>> services = new ConcurrentHashMap<Class<?>, ServiceProvider<?>>();
        services.putIfAbsent(IDBasedFileAccessFactory.class, new SimpleServiceProvider<Object>(idBasedFileAccessFactory));
        services.putIfAbsent(IDBasedFolderAccessFactory.class, new SimpleServiceProvider<Object>(idBasedFolderAccessFactory));
        MockUtils.injectValueIntoPrivateField(this.loaderActivator, InjectionFieldConstants.SERVICES, services);

        // CONTEXT
        Mockito.when(this.bundleContext.getBundle()).thenReturn(this.bundle);
        MockUtils.injectValueIntoPrivateField(this.loaderActivator, InjectionFieldConstants.CONTEXT, bundleContext);
    }

    @Test
    public void testStartBundle_Fine_ServiceRegistered() throws Exception {
        this.loaderActivator.startBundle();

        ServiceMockActivatorAsserter.verifyAllServicesRegistered(this.loaderActivator, 1);
    }
}
