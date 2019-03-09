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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import com.openexchange.exception.OXException;
import com.openexchange.icap.ICAPClient;
import com.openexchange.icap.ICAPMethod;
import com.openexchange.icap.ICAPOptions;
import com.openexchange.icap.ICAPRequest;
import com.openexchange.icap.ICAPResponse;
import com.openexchange.icap.header.ICAPRequestHeader;
import com.openexchange.server.ServiceLookup;

/**
 * {@link TestClient}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class TestClient {

    /**
     * X5O_P_AP_4_PZX54_P_7CC_7_$EICAR_STANDARD_ANTIVIRUS_TEST_FILE_$H_H
     */
    private static final String VIRUS = "X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*";
    private static final ICAPClient client = new ICAPClient(new ServiceLookup() {

        @Override
        public <S> S getService(Class<? extends S> clazz) {
            return null;
        }

        @Override
        public <S> S getOptionalService(Class<? extends S> clazz) {
            return null;
        }
    });
    private static String server = "avservice";
    private static int port = 1344;
    private static String service = "avscan";
    private static final int LOWER_BOUND = 33;
    private static final int UPPER_BOUND = 126;

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws OXException, IOException {
        //sendVirus();

        //send512();
        //send512WithPreview();
        //send1100();
        //send1100WithPreview();
        //send9000();
        //send9000WithPreview();
        //sendFileWithPreview(VIRUS + prepareData(1024123));
        //sendFile(prepareData(11562));
        sendFile(prepareData(15));
    }

    ///////////////////////////////////

    private static void sendVirus() throws OXException, IOException {
        sendFile(VIRUS);
    }

    private static void send512() throws OXException, IOException {
        sendFile(prepareData(512));
    }

    private static void send1100() throws OXException, IOException {
        sendFile(prepareData(1100));
    }

    private static void send9000() throws OXException, IOException {
        sendFile(prepareData(9000));
    }

    private static void send512WithPreview() throws OXException, IOException, ExecutionException {
        sendFileWithPreview(prepareData(512));
    }

    private static void send1100WithPreview() throws OXException, IOException, ExecutionException {
        sendFileWithPreview(prepareData(1100));
    }

    private static void send9000WithPreview() throws OXException, IOException, ExecutionException {
        sendFileWithPreview(prepareData(9000));
    }

    private static String prepareData(int size) {
        StringBuilder builder = new StringBuilder(size);
        int c = 0;
        int charIndex = LOWER_BOUND;
        int round = 1;
        builder.append(round).append(". ");
        while (c++ < size) {
            builder.append((char) charIndex++);
            if (charIndex > UPPER_BOUND) {
                charIndex = LOWER_BOUND;
                builder.append('\n').append(++round).append(". ");
            }
        }
        return builder.toString();
    }

    private static void sendFile(String data) throws OXException, IOException {
        sendFile(prepareForDelivery(data.getBytes()), data.getBytes(), -1);
    }

    private static void sendFileWithPreview(String data) throws OXException, IOException, ExecutionException {
        ICAPOptions options = client.getOptions(server, port, service);
        sendFile(prepareForDelivery(data.getBytes()), data.getBytes(), options.getPreviewSize());
    }

    private static InputStream prepareForDelivery(byte[] data) throws OXException, IOException {
        return new ByteArrayInputStream(data);
    }

    private static void sendFile(InputStream file, byte[] originalData, long preview) throws OXException, IOException {
        ICAPRequest.Builder builder = new ICAPRequest.Builder();
        if (preview > 0) {
            builder.withHeader(ICAPRequestHeader.PREVIEW, Long.toString(preview));
        }
        builder.withBodyHeader(ICAPRequestHeader.CONTENT_LENGTH, Integer.toString(originalData.length));
        ICAPRequest.Builder rBuilder = builder.withServer(server);
        //builder.withHeader(ICAPRequestHeader.ALLOW, "204");
        ICAPRequest r = rBuilder.withMethod(ICAPMethod.RESPMOD).withBody(file).withService("avscan").build();
        ICAPResponse response = client.execute(r);
        System.out.println("Status Code: " + response.getStatusCode());
        System.out.println(response.getStatusLine());
        for (Entry<String, String> entry : response.getHeaders().entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }

        System.out.println("Status Code: " + response.getEncapsulatedStatusCode());
        System.out.println(response.getEncapsulatedStatusLine());

        if (response.getEncapsulatedHeaders() != null && !response.getEncapsulatedHeaders().isEmpty()) {
            for (Entry<String, String> entry : response.getEncapsulatedHeaders().entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
        }
        System.out.println(response.getEncapsulatedBody());

        // Enable only if the streaming mode is implemented correctly
        //        if (response.getInputStream() == null) {
        //            return;
        //        }
        //        StringBuilder lastChunk = new StringBuilder(15);
        //        byte[] lastDataChunk = Arrays.copyOfRange(originalData, originalData.length - 10, originalData.length);
        //        for (int i = 0; i < lastDataChunk.length; i++) {
        //            lastChunk.append(lastDataChunk[i]).append(' ');
        //        }
        //        System.out.println("Last Chunk: " + lastChunk);
        //        System.out.println("Input Stream");
        //        byte[] actual = IOUtils.toByteArray(response.getInputStream());
        //        System.out.println(new String(actual, "UTF-8"));
        //        assertArrayEquals(originalData, actual);
    }
}
