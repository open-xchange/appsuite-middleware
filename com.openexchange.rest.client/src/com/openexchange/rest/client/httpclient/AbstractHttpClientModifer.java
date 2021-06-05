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
    
    protected final static String DEFAULT_UA = "OX App Suite HTTP client";

    private final String userAgent;
    private final AtomicBoolean isContentCompressionDisabled;

    /**
     * Initializes a new {@link AbstractHttpClientModifer}.
     */
    protected AbstractHttpClientModifer() {
        this(null);
    }
    
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
