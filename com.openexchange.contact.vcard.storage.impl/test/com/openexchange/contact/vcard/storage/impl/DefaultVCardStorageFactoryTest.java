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

package com.openexchange.contact.vcard.storage.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.contact.vcard.storage.VCardStorageService;
import com.openexchange.exception.OXException;


/**
 * {@link DefaultVCardStorageFactoryTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class DefaultVCardStorageFactoryTest {

    private static final int CONTEXT_ID = 111;

    private DefaultVCardStorageFactory factory;

    @Mock
    private VCardStorageService vCardStorageService;

    @Mock
    private ConfigViewFactory configViewFactory;

    @Mock
    private ConfigView configView;

    @Mock
    private ComposedConfigProperty<Boolean> trueProperty;

    @Mock
    private ComposedConfigProperty<Boolean> falseProperty;

    private final List<String> capabilities = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        capabilities.add("com.openexchange.capability.filestore");
        Mockito.when(vCardStorageService.neededCapabilities()).thenReturn(capabilities);

        Mockito.when(trueProperty.get()).thenReturn(Boolean.TRUE);
        Mockito.when(falseProperty.get()).thenReturn(Boolean.FALSE);

        Mockito.when(configViewFactory.getView(Matchers.anyInt(), Matchers.anyInt())).thenReturn(configView);
        Mockito.when(configView.property(Matchers.anyString(), (Class)Matchers.any())).thenReturn(trueProperty);
    }

    @Test
    public void testGetVCardStorageService_capabilitiyAvailable_returnService() {
        factory = new DefaultVCardStorageFactory(vCardStorageService);
        capabilities.add("mich.gibts.1");
        capabilities.add("mich.gibts.2");
        capabilities.add("mich.gibts.3");
        capabilities.add("mich.gibts.4");

        VCardStorageService returnedService = factory.getVCardStorageService(configViewFactory, CONTEXT_ID);

        assertEquals(vCardStorageService, returnedService);
    }

    @Test
    public void testGetVCardStorageService_capabilitiesAvailable_returnService() {
        factory = new DefaultVCardStorageFactory(vCardStorageService);

        VCardStorageService returnedService = factory.getVCardStorageService(configViewFactory, CONTEXT_ID);

        assertEquals(vCardStorageService, returnedService);
    }

    @Test
    public void testGetVCardStorageService_oneCapNotAvailable_returnNull() throws OXException {
        factory = new DefaultVCardStorageFactory(vCardStorageService);
        capabilities.add("mich.gibts.nicht");

        Mockito.when(configView.property(Matchers.anyString(), (Class)Matchers.any())).thenReturn(trueProperty, falseProperty);

        VCardStorageService returnedService = factory.getVCardStorageService(configViewFactory, CONTEXT_ID);

        assertNull(returnedService);
    }

    @Test
    public void testGetVCardStorageService_onlyCapNotAvailable_returnnull() throws OXException {
        factory = new DefaultVCardStorageFactory(vCardStorageService);
        Mockito.when(configView.property(Matchers.anyString(), (Class)Matchers.any())).thenReturn(falseProperty);

        VCardStorageService returnedService = factory.getVCardStorageService(configViewFactory, CONTEXT_ID);

        assertNull(returnedService);
    }

    @Test
    public void testGetVCardStorageService_oneMiddleCapNotAvailable_returnNull() throws OXException {
        factory = new DefaultVCardStorageFactory(vCardStorageService);
        capabilities.add("mich.gibts.nicht");
        capabilities.add("mich.schon.aber.da.kommen.wir.nicht.hin");

        Mockito.when(configView.property(Matchers.anyString(), (Class)Matchers.any())).thenReturn(trueProperty, falseProperty, trueProperty);

        VCardStorageService returnedService = factory.getVCardStorageService(configViewFactory, CONTEXT_ID);

        assertNull(returnedService);
    }

    @Test
    public void testGetVCardStorageService_configViewThrowsException_returnNull() throws OXException {
        factory = new DefaultVCardStorageFactory(vCardStorageService);

        Mockito.when(configViewFactory.getView(Matchers.anyInt(), Matchers.anyInt())).thenThrow(new OXException());

        VCardStorageService returnedService = factory.getVCardStorageService(configViewFactory, CONTEXT_ID);

        assertNull(returnedService);
    }
}
