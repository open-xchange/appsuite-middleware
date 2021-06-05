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

package com.openexchange.dav.wellknown;

import javax.servlet.http.HttpServletResponse;
import com.openexchange.annotation.NonNull;
import com.openexchange.dav.DAVFactory;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.SessionHolder;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavCollection;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;

/**
 * {@link WellknownFactory}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.3
 */
public class WellknownFactory extends DAVFactory {

    /**
     * Initializes a new {@link WellknownFactory}.
     *
     * @param protocol The protocol
     * @param sessionHolder The session holder to use
     */
    public WellknownFactory(Protocol protocol, SessionHolder sessionHolder) {
        super(protocol, null, sessionHolder);
    }

    @Override
    public WebdavResource resolveResource(WebdavPath url) throws WebdavProtocolException {
        throw WebdavProtocolException.generalError(url, HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    public WebdavCollection resolveCollection(WebdavPath url) throws WebdavProtocolException {
        throw WebdavProtocolException.generalError(url, HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    public String getURLPrefix() {
        return "/";
    }

    /*
     * ----------------------------------------
     *    Avoid service lookup functionality
     * ----------------------------------------
     */

    @Override
    public <S> S getService(Class<? extends S> clazz) {
        return null;
    }

    @Override
    public @NonNull <S> S getServiceSafe(Class<? extends S> clazz) throws OXException {
        throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(clazz.getName());
    }

    @Override
    public <S> S getOptionalService(Class<? extends S> clazz) {
        return null;
    }

}
