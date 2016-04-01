/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.dav.actions;

import java.io.IOException;
import java.io.InputStream;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.dav.resources.DAVResource;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
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
            String eTag = resource.getETag();
            if (Strings.isNotEmpty(eTag)) {
                response.setHeader("ETag", eTag);
            }
        }
    }

}
