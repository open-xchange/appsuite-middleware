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

package com.openexchange.html.internal.image;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.proxy.ImageContentTypeRestriction;
import com.openexchange.proxy.ProxyRegistration;
import com.openexchange.proxy.ProxyRegistry;

/**
 * {@link ProxyRegistryImageHandler}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class ProxyRegistryImageHandler implements ImageHandler {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ProxyRegistryImageHandler.class);

    private final String sessionId;
    private final ProxyRegistry proxyRegistry;

    /**
     * Initializes a new {@link ProxyRegistryImageHandler}.
     *
     * @param sessionId The session identifier
     * @param proxyRegistry The proxy registry
     */
    public ProxyRegistryImageHandler(String sessionId, ProxyRegistry proxyRegistry) {
        super();
        this.sessionId = sessionId;
        this.proxyRegistry = proxyRegistry;
    }

    @Override
    public void handleImage(String urlStr, String src, StringBuilder sb) throws OXException {
        try {
            // Add proxy registration
            URL imageUrl = new URL(urlStr);
            URI uri = proxyRegistry.register(new ProxyRegistration(imageUrl, sessionId, ImageContentTypeRestriction.getInstance()));

            // Append replacement
            sb.append("src=\"").append(uri.toString()).append('"');
        } catch (MalformedURLException e) {
            LOG.debug("Invalid URL found in \"img\" tag: {}. Keeping original content.", src, e);
            sb.append(src);
        }
    }

}
