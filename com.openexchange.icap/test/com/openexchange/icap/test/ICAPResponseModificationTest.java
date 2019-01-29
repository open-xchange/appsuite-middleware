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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.icap.test;

import static org.junit.Assert.fail;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import com.openexchange.exception.OXException;
import com.openexchange.icap.ICAPMethod;
import com.openexchange.icap.ICAPRequest;
import com.openexchange.icap.ICAPResponse;
import com.openexchange.icap.impl.request.handler.ResponseModificationICAPRequestHandler;
import com.openexchange.icap.test.util.ICAPResponseFactory;
import com.openexchange.icap.test.util.ICAPTestProperties;
import com.openexchange.java.Charsets;

/**
 * {@link ICAPResponseModificationTest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class ICAPResponseModificationTest extends AbstractICAPTest {

    /**
     * Initialises a new {@link ICAPResponseModificationTest}.
     */
    public ICAPResponseModificationTest() {
        super();
    }

    /**
     * Tests sending an infected file to the ICAP server
     */
    @Test
    public void testSendInfected() throws OXException, IOException {
        ICAPResponse expectedResponse = ICAPResponseFactory.buildInfectedFoundResponse();
        InputStream mockedResponse = ICAPResponseFactory.buildICAPResponseInputStream(expectedResponse);

        try (Socket socket = createSocket()) {
            Socket socketMock = PowerMockito.mock(Socket.class);
            PowerMockito.when(socketMock.getInputStream()).thenReturn(mockedResponse);
            PowerMockito.when(socketMock.getOutputStream()).thenReturn(socket.getOutputStream());

            ICAPRequest request = new ICAPRequest.Builder().withMethod(ICAPMethod.RESPMOD).withServer("localhost").withPort(1344).withService("mockService").withBody(new ByteArrayInputStream(ICAPTestProperties.VIRUS.getBytes(Charsets.UTF_8))).build();
            handleRequestAndAssert(new ResponseModificationICAPRequestHandler(), request, socketMock, expectedResponse);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}
