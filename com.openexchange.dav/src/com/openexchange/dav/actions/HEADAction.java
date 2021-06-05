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

package com.openexchange.dav.actions;

import static com.openexchange.dav.DAVProtocol.protocolException;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.webdav.action.WebdavRequest;
import com.openexchange.webdav.action.WebdavResponse;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;

/**
 * {@link HEADAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class HEADAction extends DAVAction {

    /**
     * Initializes a new {@link HEADAction}.
     *
     * @param protocol The underlying protocol
     */
    public HEADAction(Protocol protocol) {
        super(protocol);
    }

    /**
     * Sets common headers in the response based on the underlying resource. This includes
     * <ul>
     * <li>Content-Type</li>
     * <li>Content-Length</li>
     * <li>ETag (implicitly as quoted string)</li>
     * <li>Content-Disposition</li>
     * </ul>
     *
     * @param resource The WebDAV resource to set the response headers for
     * @param response The response to set the headers in
     */
    protected void setResponseHeaders(WebdavResource resource, WebdavResponse response) throws WebdavProtocolException {
        setHeaderOpt("Content-Type", resource.getContentType(), response);
        if (false == resource.isCollection()) {
            setHeaderOpt("Content-Length", resource.getLength(), response);
        }
        setHeaderOpt("ETag", resource.getETag(), true, response);
        setHeaderOpt("Content-Disposition", "attachment", response);
    }

    @Override
    public void perform(WebdavRequest request, WebdavResponse response) throws WebdavProtocolException {
        /*
         * get targeted resource, set response headers & status
         */
        WebdavResource resource = request.getResource();
        if (null == resource || false == resource.exists()) {
            throw protocolException(request.getUrl(), HttpServletResponse.SC_NOT_FOUND);
        }
        setResponseHeaders(resource, response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

}
