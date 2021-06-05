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

package com.openexchange.contact.vcard.storage.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
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

        Mockito.when(configViewFactory.getView(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt())).thenReturn(configView);
        Mockito.when(configView.property(ArgumentMatchers.anyString(), Class.class.cast(ArgumentMatchers.any()))).thenReturn(trueProperty);
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

        Mockito.when(configView.property(ArgumentMatchers.anyString(), Class.class.cast(ArgumentMatchers.any()))).thenReturn(trueProperty, falseProperty);

        VCardStorageService returnedService = factory.getVCardStorageService(configViewFactory, CONTEXT_ID);

        assertNull(returnedService);
    }

     @Test
     public void testGetVCardStorageService_onlyCapNotAvailable_returnnull() throws OXException {
        factory = new DefaultVCardStorageFactory(vCardStorageService);
        Mockito.when(configView.property(ArgumentMatchers.anyString(), Class.class.cast(ArgumentMatchers.any()))).thenReturn(falseProperty);

        VCardStorageService returnedService = factory.getVCardStorageService(configViewFactory, CONTEXT_ID);

        assertNull(returnedService);
    }

     @Test
     public void testGetVCardStorageService_oneMiddleCapNotAvailable_returnNull() throws OXException {
        factory = new DefaultVCardStorageFactory(vCardStorageService);
        capabilities.add("mich.gibts.nicht");
        capabilities.add("mich.schon.aber.da.kommen.wir.nicht.hin");

        Mockito.when(configView.property(ArgumentMatchers.anyString(), Class.class.cast(ArgumentMatchers.any()))).thenReturn(trueProperty, falseProperty, trueProperty);

        VCardStorageService returnedService = factory.getVCardStorageService(configViewFactory, CONTEXT_ID);

        assertNull(returnedService);
    }

     @Test
     public void testGetVCardStorageService_configViewThrowsException_returnNull() throws OXException {
        factory = new DefaultVCardStorageFactory(vCardStorageService);

        Mockito.when(configViewFactory.getView(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt())).thenThrow(new OXException());

        VCardStorageService returnedService = factory.getVCardStorageService(configViewFactory, CONTEXT_ID);

        assertNull(returnedService);
    }
}
