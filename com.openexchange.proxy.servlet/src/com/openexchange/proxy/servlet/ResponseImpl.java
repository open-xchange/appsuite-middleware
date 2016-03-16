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

package com.openexchange.proxy.servlet;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.httpclient.HttpMethodBase;
import com.openexchange.proxy.Header;
import com.openexchange.proxy.Response;

/**
 * {@link ResponseImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ResponseImpl implements Response {

    private final HttpMethodBase httpMethod;

    /**
     * Initializes a new {@link ResponseImpl}.
     *
     * @param httpMethod The delegatee
     */
    public ResponseImpl(final HttpMethodBase httpMethod) {
        super();
        this.httpMethod = httpMethod;
    }

    @Override
    public int getStatusCode() {
        return httpMethod.getStatusCode();
    }

    @Override
    public String getStatusText() {
        return httpMethod.getStatusText();
    }

    @Override
    public Header[] getResponseHeaders() {
        final org.apache.commons.httpclient.Header[] headers = httpMethod.getResponseHeaders();
        if (null == headers) {
            return null;
        }
        final int length = headers.length;
        final Header[] ret = new Header[length];
        for (int i = 0; i < length; i++) {
            ret[i] = new HeaderImpl(headers[i]);
        }
        return ret;
    }

    @Override
    public Header getResponseHeader(final String headerName) {
        return new HeaderImpl(httpMethod.getResponseHeader(headerName));
    }

    @Override
    public Header[] getResponseHeaders(final String headerName) {
        final org.apache.commons.httpclient.Header[] headers = httpMethod.getResponseHeaders(headerName);
        if (null == headers) {
            return null;
        }
        final int length = headers.length;
        final Header[] ret = new Header[length];
        for (int i = 0; i < length; i++) {
            ret[i] = new HeaderImpl(headers[i]);
        }
        return ret;
    }

    @Override
    public Header[] getResponseFooters() {
        final org.apache.commons.httpclient.Header[] headers = httpMethod.getResponseFooters();
        if (null == headers) {
            return null;
        }
        final int length = headers.length;
        final Header[] ret = new Header[length];
        for (int i = 0; i < length; i++) {
            ret[i] = new HeaderImpl(headers[i]);
        }
        return ret;
    }

    @Override
    public Header getResponseFooter(final String footerName) {
        return new HeaderImpl(httpMethod.getResponseFooter(footerName));
    }

    @Override
    public byte[] getResponseBody() throws IOException {
        return httpMethod.getResponseBody();
    }

    @Override
    public String getResponseBodyAsString() throws IOException {
        return httpMethod.getResponseBodyAsString();
    }

    @Override
    public InputStream getResponseBodyAsStream() throws IOException {
        return httpMethod.getResponseBodyAsStream();
    }

}
