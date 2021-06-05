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

package com.openexchange.file.storage.webdav.utils;

import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.lang3.Validate;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.webdav.AbstractWebDAVFileStorageService;
import com.openexchange.session.Session;

/**
 *
 * {@link WebDAVEndpointConfig}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.4
 */
public class WebDAVEndpointConfig {

    private final String url;

    WebDAVEndpointConfig(String url) {
        super();
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public static class Builder {

        private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(WebDAVEndpointConfig.Builder.class);

        protected String url;

        public Builder(Session session, AbstractWebDAVFileStorageService service, String url) throws OXException {
            Validate.notNull(session, "Session might not be null!");
            Validate.notNull(service, "AbstractWebDAVFileStorageService might not be null!");
            Validate.notNull(url, "URL might not be null!");
            this.url = url;

            optAdaptScheme();
            WebDAVEndpointUtils.verifyURL(session, service, this.url);
        }

        private void optAdaptScheme() {
            try {
                if (!this.url.startsWith("http")) { // includes 'https'
                    this.url = new URL("https://" + this.url).toString();
                }
            } catch (MalformedURLException e) {
                LOG.error("Unable to verify and adapt scheme for endpoint {}.", this.url, e);
            }
        }

        public WebDAVEndpointConfig build() {
            return new WebDAVEndpointConfig(url);
        }
    }
}
