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

package com.openexchange.icap.test;

import static com.openexchange.java.Autoboxing.I;
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
    public void setUp() {
        LeanConfigurationService leanConfigServiceMock = PowerMockito.mock(LeanConfigurationService.class);
        PowerMockito.when(I(leanConfigServiceMock.getIntProperty(ICAPClientProperty.SOCKET_TIMEOUT))).thenReturn(I(10000));

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
