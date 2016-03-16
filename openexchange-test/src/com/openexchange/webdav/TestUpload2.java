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

package com.openexchange.webdav;

import java.io.ByteArrayInputStream;
import java.util.Properties;
import junit.framework.TestCase;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import com.meterware.httpunit.Base64;
import com.openexchange.groupware.configuration.AbstractConfigWrapper;
import com.openexchange.test.WebdavInit;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class TestUpload2 extends TestCase {

    private Properties webdavProps;

    private String login;

    private String password;

    private String hostname;

    /**
     * @param name
     */
    public TestUpload2(final String name) {
        super(name);
        // TODO Auto-generated constructor stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        webdavProps = WebdavInit.getWebdavProperties();
        login = AbstractConfigWrapper.parseProperty(webdavProps, "login", "");
        password = AbstractConfigWrapper.parseProperty(webdavProps, "password", "");
        hostname = AbstractConfigWrapper.parseProperty(webdavProps, "hostname", "localhost");
    }

    private static final int PAKETS = 10;

    public void testUpload() throws Throwable {
        final HttpClient client = new HttpClient();
        // TODO read home infostore dir
        final String uri = "http://" + hostname + "/servlet/webdav.infostore/Marcus%20Klein/testfile";
        final PutMethod put = new PutMethod(uri);

        put.setHttp11(true);
        put.setRequestHeader(new Header("Content-Type", "application/octet-stream"));
        final byte[] bytes = new byte[PAKETS * 1024];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = 'a';
        }
        put.setRequestBody(new ByteArrayInputStream(bytes));
        put.setRequestContentLength(EntityEnclosingMethod.CONTENT_LENGTH_CHUNKED);
        put.addRequestHeader(new Header("Authorization", "Basic " + Base64
            .encode(login + ":" + password)));

        client.executeMethod(put);
        assertEquals(201, put.getStatusCode());
    }
}
