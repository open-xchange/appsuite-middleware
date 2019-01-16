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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.powermock.api.mockito.PowerMockito;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.icap.ICAPRequest;
import com.openexchange.icap.ICAPResponse;
import com.openexchange.icap.conf.ICAPClientProperty;
import com.openexchange.icap.impl.request.handler.ICAPRequestHandler;
import com.openexchange.icap.test.util.AssertUtil;
import com.openexchange.icap.test.util.ICAPServerMock;
import com.openexchange.icap.test.util.ICAPTestProperties;
import com.openexchange.java.Charsets;
import com.openexchange.server.ServiceLookup;
import com.openexchange.timer.TimerService;

/**
 * {@link AbstractICAPTest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public abstract class AbstractICAPTest {

    private static Executor executor;

    /**
     * Initialises a new {@link AbstractICAPTest}.
     */
    public AbstractICAPTest() {
        super();
    }

    @BeforeClass
    public static void setUpClass() {
        executor = Executors.newFixedThreadPool(5);
        executor.execute(() -> new ICAPServerMock().start());
    }

    /**
     * Setup mocks
     * 
     * @throws IOException
     */
    @Before
    public void setUp() throws IOException {
        LeanConfigurationService leanConfigServiceMock = PowerMockito.mock(LeanConfigurationService.class);
        PowerMockito.when(leanConfigServiceMock.getIntProperty(ICAPClientProperty.SOCKET_TIMEOUT)).thenReturn(10000);

        ServiceLookup serviceLookupMock = PowerMockito.mock(ServiceLookup.class);
        PowerMockito.when(serviceLookupMock.getService(LeanConfigurationService.class)).thenReturn(leanConfigServiceMock);

        TimerService timerServiceMock = PowerMockito.mock(TimerService.class);
        PowerMockito.when(timerServiceMock.getExecutor()).thenReturn(executor);
    }

    @AfterClass
    public static void tearDownClass() {
        try (Socket socket = new Socket(ICAPTestProperties.ICAP_SERVER_ADDRESS, ICAPTestProperties.ICAP_SERVER_PORT)) {
            socket.setSoTimeout(5000);
            socket.getOutputStream().write(ICAPServerMock.SHUT_DOWN_COMMAND.getBytes(Charsets.UTF_8));
        } catch (IOException e) {
            System.err.println("Unable to terminate mock ICAP server" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates a {@link Socket} to the default {@link ICAPTestProperties#ICAP_SERVER_ADDRESS}
     * 
     * @return The {@link Socket}
     * @throws UnknownHostException If the server address cannot be resolved
     * @throws IOException if an I/O error is occurred
     */
    Socket createSocket() throws UnknownHostException, IOException {
        return createSocket(ICAPTestProperties.ICAP_SERVER_ADDRESS, ICAPTestProperties.ICAP_SERVER_PORT);
    }

    /**
     * Creates a {@link Socket} to the specified server and port
     * 
     * @param server The server address
     * @param port The port
     * @return The new {@link Socket}
     * @throws UnknownHostException If the server address cannot be resolved
     * @throws IOException if an I/O error is occurred
     */
    Socket createSocket(String server, int port) throws UnknownHostException, IOException {
        Socket socket = new Socket(server, port);
        socket.setSoTimeout(5000);
        return socket;
    }

    /**
     * Handles the specified {@link ICAPRequest}.
     * 
     * @param requestHandler The {@link ICAPRequestHandler} to handle the request
     * @param request The {@link ICAPRequest} to handle
     * @param mockedResponse The mocked response {@link InputStream} that the server will return (and thus will be handled by the handler)
     * @param socketOutputStream The socket {@link OutputStream}
     * @param expectedResponse The expected {@link ICAPResponse}
     * @return The actual {@link ICAPResponse} from the handler
     * @throws IOException if an I/O error is occurred
     */
    ICAPResponse handleRequestAndAssert(ICAPRequestHandler requestHandler, ICAPRequest request, Socket socket, ICAPResponse expectedResponse) throws IOException {
        ICAPResponse actualResponse = requestHandler.handle(request, socket);
        AssertUtil.assertICAPResponse(expectedResponse, actualResponse);
        return actualResponse;
    }
}
