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
    private ComposedConfigProperty property;

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
