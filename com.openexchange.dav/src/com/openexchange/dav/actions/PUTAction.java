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

import java.io.IOException;
import java.io.InputStream;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.dav.resources.DAVResource;
import com.openexchange.java.Streams;
import com.openexchange.webdav.action.WebdavPutAction.SizeExceededInputStream;
import com.openexchange.webdav.action.WebdavRequest;
import com.openexchange.webdav.action.WebdavResponse;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavProtocolException;

/**
 * {@link PUTAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public abstract class PUTAction extends DAVAction {

    /**
     * Initializes a new {@link PUTAction}.
     *
     * @param protocol The underlying protocol
     */
    public PUTAction(Protocol protocol) {
        super(protocol);
    }

    protected abstract long getMaxSize();

    protected abstract boolean includeResponseETag();

    protected WebdavProtocolException getSizeExceeded(WebdavRequest request) {
        return WebdavProtocolException.Code.GENERAL_ERROR.create(request.getUrl(), HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
    }

    @Override
    public void perform(WebdavRequest request, WebdavResponse response) throws WebdavProtocolException {
        /*
         * check indicated content length
         */
        long contentLength = getContentLength(request);
        long maxSize = getMaxSize();
        if (-1 != contentLength && 0 < maxSize && maxSize < contentLength) {
            throw getSizeExceeded(request);
        }
        /*
         * get & prepare targeted resource
         */
        DAVResource resource = requireResource(request);
        resource.setLength(Long.valueOf(contentLength));
        resource.setContentType(getContentType(request));
        /*
         * put resource
         */
        InputStream inputStream = null;
        try {
            inputStream = request.getBody();
            if (0 < maxSize) {
                inputStream = new SizeExceededInputStream(inputStream, maxSize);
            }
            resource.putBodyAndGuessLength(inputStream);
        } catch (IOException e) {
            throw WebdavProtocolException.Code.GENERAL_ERROR.create(request.getUrl(), HttpServletResponse.SC_BAD_REQUEST, e);
        } catch (WebdavProtocolException e) {
            if (SizeExceededInputStream.class.isInstance(inputStream) && ((SizeExceededInputStream) inputStream).hasExceeded()) {
                throw getSizeExceeded(request);
            }
            throw e;
        } finally {
            Streams.close(inputStream);
        }
        /*
         * save / create resource & send response
         */
        if (resource.exists() && false == resource.isLockNull()) {
            resource.save();
        } else {
            resource.create();
        }
        response.setStatus(HttpServletResponse.SC_CREATED);
        if (includeResponseETag()) {
            setHeaderOpt("ETag", resource.getETag(), true, response);
        }
    }

}
