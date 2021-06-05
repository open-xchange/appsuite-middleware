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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.java.Streams;
import com.openexchange.tools.io.IOUtils;
import com.openexchange.webdav.action.WebdavRequest;
import com.openexchange.webdav.action.WebdavResponse;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;

/**
 * {@link GETAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class GETAction extends HEADAction {

    /**
     * Initializes a new {@link GETAction}.
     *
     * @param protocol The underlying protocol
     */
    public GETAction(Protocol protocol) {
        super(protocol);
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
        /*
         * write response body
         */
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            outputStream = Streams.bufferedOutputStreamFor(response.getOutputStream());
            inputStream = resource.getBody();
            if (null == inputStream) {
                throw protocolException(resource.getUrl(), new Exception("no resource body"));
            }
            IOUtils.transfer(inputStream, outputStream);
        } catch (IOException e) {
            throw protocolException(resource.getUrl(), e);
        } finally {
            Streams.flush(outputStream);
            Streams.close(inputStream);
        }
    }

}
