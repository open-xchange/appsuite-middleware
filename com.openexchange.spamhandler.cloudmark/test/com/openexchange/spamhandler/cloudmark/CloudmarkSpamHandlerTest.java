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

package com.openexchange.spamhandler.cloudmark;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import javax.mail.internet.InternetAddress;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link CloudmarkSpamHandlerTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.2
 */
public class CloudmarkSpamHandlerTest {

    private static final String UMLAUT_ADDRESS = "mschneider@xn--tsting-bua.de";

    private static final String ASCII_ADDRESS = "mschneider@open-xchange.com";

    @InjectMocks
    private CloudmarkSpamHandler cloudMarkSpamHandler;

    @Mock
    private Session session;

    @Mock
    private ServiceLookup services;

    @Mock
    private ConfigViewFactory configViewFactory;

    @Mock
    private ConfigView configView;

    @Mock
    private ComposedConfigProperty<String> property;

    @Before
    public void setUp() throws OXException {
        MockitoAnnotations.initMocks(this);

        Mockito.when(services.getService(ConfigViewFactory.class)).thenReturn(configViewFactory);
        Mockito.when(configViewFactory.getView(session.getUserId(), session.getContextId())).thenReturn(configView);
        Mockito.when(configView.property(CloudmarkSpamHandler.TARGET_SPAM_ADDRESS, String.class)).thenReturn(property);
        Mockito.when(property.get()).thenReturn(ASCII_ADDRESS);
    }

    @Test
    public void testGetAddress_isUmlautAdress_returnAddress() {
        InternetAddress senderAddress = CloudmarkSpamHandler.getAddress(UMLAUT_ADDRESS);

        assertNotNull(senderAddress);
        assertEquals(UMLAUT_ADDRESS, senderAddress.getAddress());
    }

    @Test
    public void testGetAddress_isAsciiAdress_returnAddress() {
        InternetAddress senderAddress = CloudmarkSpamHandler.getAddress(ASCII_ADDRESS);

        assertNotNull(senderAddress);
        assertEquals(ASCII_ADDRESS, senderAddress.getAddress());
    }

    private static final int DEFAULT_ID = 0;

    @Test
    public void testHandleSpam_ensureCorrectParameterBecomesRead() throws OXException {
        String[] mailIds = new String[] { "1", "2" };
        try {
            cloudMarkSpamHandler.handleSpam(DEFAULT_ID, "INBOX", mailIds, false, session);
        } catch (Exception e) {
            // expected, as the complete method isn't mocked
        }

        Mockito.verify(configView, Mockito.times(1)).property(CloudmarkSpamHandler.TARGET_SPAM_ADDRESS, String.class);
        Mockito.verify(configView, Mockito.never()).property("com.openexchange.spamhandler.name", String.class);
        Mockito.verify(configView, Mockito.never()).get("com.openexchange.spamhandler.name", String.class);
    }

}
