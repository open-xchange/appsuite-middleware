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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.rest.client.httpclient;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import com.openexchange.annotation.Nullable;
import com.openexchange.java.Strings;

/**
 * {@link AbstractHttpClientModifer}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.4
 */
public abstract class AbstractHttpClientModifer implements HttpClientBuilderModifier {

    private final String userAgent;
    private final AtomicBoolean isContentCompressionDisabled;

    /**
     * Initializes a new {@link AbstractHttpClientModifer}.
     *
     * @param userAgent The user agent to set; may be <code>null</code>
     */
    protected AbstractHttpClientModifer(@Nullable String userAgent) {
        super();
        this.userAgent = userAgent;
        isContentCompressionDisabled = new AtomicBoolean(false);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This standard implementation will set the user agent,
     * the compression and enable BasicAuth. It is <b>recommended</b> to call
     * <code>super.modify(builder)</code> if the method is overwritten
     */
    @Override
    public void modify(HttpClientBuilder builder) {
        if (Strings.isNotEmpty(userAgent)) {
            builder.setUserAgent(userAgent);
        }
        setCompression(builder);
    }

    /**
     * Sets the isContentCompressionDisabled
     *
     * @param isContentCompressionDisabled The isContentCompressionDisabled to set
     */
    protected void setContentCompressionDisabled(boolean isContentCompressionDisabled) {
        this.isContentCompressionDisabled.set(isContentCompressionDisabled);
    }

    private void setCompression(HttpClientBuilder builder) {
        if (isContentCompressionDisabled.get()) {
            return;
        }

        builder.addInterceptorLast(new HttpResponseInterceptor() {

            @Override
            public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
                HttpEntity entity = response.getEntity();
                if (entity == null) {
                    return;
                }
                final Header ceheader = entity.getContentEncoding();
                if (ceheader == null) {
                    return;
                }
                final HeaderElement[] codecs = ceheader.getElements();
                for (final HeaderElement codec : codecs) {
                    if (codec.getName().equalsIgnoreCase("gzip")) {
                        response.setEntity(new GzipDecompressingEntity(response.getEntity()));
                        return;
                    }
                }
            }
        });
    }

}
