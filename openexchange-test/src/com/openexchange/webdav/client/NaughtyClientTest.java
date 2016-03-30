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

package com.openexchange.webdav.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Random;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.auth.BasicScheme;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import com.openexchange.webdav.WebdavClientTest;

public class NaughtyClientTest extends WebdavClientTest {

    // Bug 7642
    // TODO Rewrite this test to get it working again.
    public void testContentLengthTooLarge() throws Exception{
        contentLengthTest(20, 30);
    }

    // Bug 7642
    // This doesn't work, as the webserver faithfully closes the stream after receiving content-length bytes.
    // In this case the file would be truncated to the claimed length.
    //public void testContentLengthTooSmall() throws Exception {
    //    contentLengthTest(20,10);
    //}

    public void contentLengthTest(final int size, final int pretendSize) throws MalformedURLException, IOException, InterruptedException {
        final byte[] data = new byte[size];
        final Random r = new Random();
        for(int i = 0; i < data.length; i++) { data[i] = (byte) r.nextInt(); }

        HttpURL url = new HttpURL(getUrl("testFile.bin"));

        final PutMethod put = new PutMethod(url.getEscapedURI());
        put.getHostAuthState().setAuthScheme(new BasicScheme());
        put.setRequestEntity(new RequestEntity() {
            @Override
            public long getContentLength() {
                return pretendSize;
            }
            @Override
            public String getContentType() {
                return "application/octet-stream";
            }
            @Override
            public boolean isRepeatable() {
                return false;
            }
            @Override
            public void writeRequest(OutputStream out) throws IOException {
                out.write(data);
            }
        });

        final HttpClient client = new HttpClient();
        setAuth(client);

        try {
            client.executeMethod(put);
        } catch (final IOException x) {
            // This exception is expected, because we don't provide all the data (or more) than we claim.
        }
        clean.add("testFile.bin");

        // The invalid request mucks up synchronization between client and server, so the file is not
        // necessarily saved at this point (The stream is closed but server processing continues anyway.
        // We'll try to load it a few times, and see, if we can succeed.

        GetMethod get = new GetMethod(url.getEscapedURI());
        int i = 0;
        do {
            Thread.sleep(100);
            get =  new GetMethod(url.getEscapedURI());

            client.executeMethod(get);
            i++;
        } while(i < 10 && get.getStatusCode() != 200);

        assertEquals(200, get.getStatusCode());
        assertEquals(String.valueOf(size), get.getResponseHeader("content-length").getValue());
        assertEqualContent(get.getResponseBodyAsStream(), new ByteArrayInputStream(data));
    }
}
