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

package com.openexchange.webdav.protocol.helpers;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.webdav.protocol.WebdavCollection;
import com.openexchange.webdav.protocol.WebdavFactory;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;

/**
 * {@link AbstractWebdavFactory}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public abstract class AbstractWebdavFactory implements WebdavFactory {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractWebdavFactory.class);


    private PropertyMixin[] mixins;

    @Override
    public void beginRequest() {

    }

    @Override
    public void endRequest(final int status) {

    }

    @Override
    public WebdavCollection resolveCollection(String url) throws WebdavProtocolException {
        url = normalize(url);
        return resolveCollection(decode(new WebdavPath(url)));
    }

    @Override
    public WebdavResource resolveResource(String url) throws WebdavProtocolException {
        url = normalize(url);
        return resolveResource(decode(new WebdavPath(url)));
    }

    public WebdavPath decode(final WebdavPath webdavPath) throws WebdavProtocolException {
        final WebdavPath path = new WebdavPath();
        for(final String component : webdavPath) {
            try {
                path.append(URLDecoder.decode(component, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                // Won't happen
                LOGGER.trace("Encoding error", e);
            } catch (IllegalArgumentException e) {
                LOGGER.error("Unable to decode component: {}", component, e);
                throw WebdavProtocolException.generalError(e, webdavPath, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
        return path;
    }

    public <T extends AbstractResource> T mixin(T thing) {
        if (mixins != null && thing != null) {
            thing.includeProperties(mixins);
        }
        return thing;
    }

    protected String normalize(String url) {
        if (url.length()==0) {
            return "/";
        }
        url = url.replaceAll("/+", "/");
        if (url.charAt(url.length()-1)=='/') {
            return url.substring(0,url.length()-1);
        }
        return url;
    }

    public void setGlobalMixins(final PropertyMixin...mixins) {
        this.mixins = mixins;
    }



}
