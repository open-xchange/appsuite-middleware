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

package com.openexchange.webdav.action;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.webdav.protocol.WebdavFactory;
import com.openexchange.webdav.protocol.WebdavPath;

public class ServletWebdavRequest extends AbstractWebdavRequest implements WebdavRequest {

    private static final ApacheURLDecoder URL_DECODER = new ApacheURLDecoder();
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ServletWebdavRequest.class);

    private final HttpServletRequest req;
    private final WebdavPath url;
    private String urlPrefix;
    private WebdavPath destUrl;

    /**
     * Initializes a new {@link ServletWebdavRequest}.
     *
     * @param factory The WebDAV factory
     * @param req The underlying servlet request
     */
    public ServletWebdavRequest(WebdavFactory factory, HttpServletRequest req) {
        super(factory);
        this.req = req;
        this.urlPrefix = req.getServletPath().endsWith("/") ? req.getServletPath() : req.getServletPath() + '/';
        LOG.debug("WEBDAV URL PREFIX FROM CONTAINER: {}", this.urlPrefix);
        this.url = toWebdavURL(req.getRequestURI());
    }

    @Override
    public InputStream getBody() throws IOException {
        return req.getInputStream();
    }

    @Override
    public String getHeader(String header) {
        return req.getHeader(header);
    }

    @Override
    public List<String> getHeaderNames() {
        Enumeration<String> headerNames = req.getHeaderNames();
        return null == headerNames ? Collections.emptyList() : Collections.list(headerNames);
    }

    @Override
    public String getURLPrefix() {
        return urlPrefix;
    }

    public void setUrlPrefix(String urlPrefix) {
        this.urlPrefix = urlPrefix;
    }

    @Override
    public WebdavPath getUrl() {
        return url;
    }

    @Override
    public WebdavPath getDestinationUrl() {
        if (destUrl != null) {
            return destUrl;
        }

        return destUrl = toWebdavURL(req.getHeader("destination"));
    }

    protected WebdavPath toWebdavURL(String url) {
        if (url == null) {
            return null;
        }

        if (false == url.startsWith("/")) {
            try {
                final URL urlO = new URL(url);
                url = urlO.getPath();
            } catch (MalformedURLException x) {
                LOG.debug("", x);
            }
        }

        if (url.startsWith(req.getServletPath())) {
            url =  url.substring(req.getServletPath().length());
        }

        try {
            String encoding = req.getCharacterEncoding();
            if (encoding == null) {
                encoding = ServerConfig.getProperty(Property.DefaultEncoding);
            }
            final WebdavPath path = new WebdavPath();
            for (final String component : url.split("/+")) {
                if (component.equals("")) {
                    continue;
                }

                path.append(decode(component, encoding));
            }
            return path;
        } catch (UnsupportedEncodingException e) {
            LOG.trace("Fallback to url: {}", url, e);
            return new WebdavPath(url);
        }
    }

    public String decode(final String component, final String encoding) throws UnsupportedEncodingException {
        return URL_DECODER.decode(component, encoding);
    }

    @Override
    public String getCharset() {
        return req.getCharacterEncoding() == null ? ServerConfig.getProperty(Property.DefaultEncoding) : req.getCharacterEncoding();
    }

    @Override
    public String getParameter(String name) {
        return req.getParameter(name);
    }

}
