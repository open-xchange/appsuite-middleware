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

package com.openexchange.soap.cxf.interceptor;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import org.apache.cxf.frontend.WSDLGetInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.message.Message;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.soap.cxf.osgi.Services;


/**
 * {@link HttpsAwareWSDLGetInterceptor}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class HttpsAwareWSDLGetInterceptor extends WSDLGetInterceptor {

    private static final String HTTPS = "https";

    /**
     * Initializes a new {@link HttpsAwareWSDLGetInterceptor}.
     */
    public HttpsAwareWSDLGetInterceptor() {
        super();
    }

    /**
     * Initializes a new {@link HttpsAwareWSDLGetInterceptor}.
     * @param outInterceptor
     */
    public HttpsAwareWSDLGetInterceptor(Interceptor<Message> outInterceptor) {
        super(outInterceptor);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        try {
            String baseUri = (String) message.get(Message.REQUEST_URL);

            URI uri = new URI(baseUri);
            ConfigurationService configService = Services.optService(ConfigurationService.class);
            boolean forceHTTPS = configService != null && configService.getBoolProperty(ServerConfig.Property.FORCE_HTTPS.getPropertyName(), Boolean.valueOf(ServerConfig.Property.FORCE_HTTPS.getDefaultValue()).booleanValue());
            TreeMap<String, List<String>> headers = (TreeMap<String, List<String>>) message.get(Message.PROTOCOL_HEADERS);
            Optional<List<String>> optProto = headers != null ? Optional.ofNullable(headers.get("X-Forwarded-Proto")) : Optional.empty();
            if (forceHTTPS || (optProto.isPresent() && optProto.get().size() > 0 && optProto.get().get(0).equalsIgnoreCase(HTTPS))) {
                Optional<List<String>> optForwardPort = headers != null ? Optional.ofNullable(headers.get("X-Forwarded-Port")) : Optional.empty();
                int port = 443;
                try {
                    if (optForwardPort.isPresent() && optForwardPort.get().isEmpty() == false) {
                        port = Integer.valueOf(optForwardPort.get().get(0)).intValue();
                    }
                } catch (NumberFormatException e) {
                    // ignore
                }
                baseUri = new URI(HTTPS, uri.getUserInfo(), uri.getHost(), port, uri.getPath(), uri.getQuery(), uri.getFragment()).toString();
                message.put(Message.REQUEST_URL, baseUri);
            }

            super.handleMessage(message);
        } catch (URISyntaxException e) {
            throw new Fault(e);
        }
    }

}
