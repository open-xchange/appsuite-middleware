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

import static org.junit.Assert.fail;
import java.io.InputStream;
import java.net.Socket;
import java.util.Date;
import java.util.UUID;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import com.openexchange.icap.ICAPRequest;
import com.openexchange.icap.ICAPResponse;
import com.openexchange.icap.header.OptionsICAPResponseHeader;
import com.openexchange.icap.impl.request.handler.OptionsICAPRequestHandler;
import com.openexchange.icap.test.util.ICAPResponseFactory;

/**
 * {@link ICAPOptionsTest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class ICAPOptionsTest extends AbstractICAPTest {

    /**
     * Initialises a new {@link ICAPOptionsTest}.
     */
    public ICAPOptionsTest() {
        super();
    }

    /**
     * Tests the regular options response
     */
    @Test
    public void testOptions() {
        ICAPResponse.Builder responseBuilder = new ICAPResponse.Builder();
        responseBuilder.withStatusLine("ICAP/1.0 200 OK");
        responseBuilder.withStatusCode(200);
        responseBuilder.addHeader(OptionsICAPResponseHeader.METHODS, "RESPMOD, REQMOD");
        responseBuilder.addHeader(OptionsICAPResponseHeader.SERVICE, "OX Dummy ICAP Server");
        responseBuilder.addHeader(OptionsICAPResponseHeader.ISTAG, UUID.randomUUID().toString());
        responseBuilder.addHeader(OptionsICAPResponseHeader.TRANSFER_PREVIEW, "*");
        responseBuilder.addHeader(OptionsICAPResponseHeader.OPTIONS_TTL, "3600");
        responseBuilder.addHeader(OptionsICAPResponseHeader.DATE, new Date().toString());
        responseBuilder.addHeader(OptionsICAPResponseHeader.PREVIEW, "1024");
        responseBuilder.addHeader(OptionsICAPResponseHeader.ALLOW, "204");
        responseBuilder.addHeader(OptionsICAPResponseHeader.ENCAPSULATED, "null-body=0");
        ICAPResponse expectedResponse = responseBuilder.build();
        InputStream mockedResponse = ICAPResponseFactory.buildICAPResponseInputStream(expectedResponse);


        try (Socket socket = createSocket()) {
            Socket socketMock = PowerMockito.mock(Socket.class);
            PowerMockito.when(socketMock.getInputStream()).thenReturn(mockedResponse);
            PowerMockito.when(socketMock.getOutputStream()).thenReturn(socket.getOutputStream());
            
            ICAPRequest request = new ICAPRequest.Builder().withServer("localhost").withPort(1344).withService("mockService").build();
            handleRequestAndAssert(new OptionsICAPRequestHandler(), request, socketMock, expectedResponse);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}
