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
import java.io.OutputStream;
import javax.servlet.http.HttpServletResponse;
import org.jdom2.Element;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.attachments.AttachmentUtils;
import com.openexchange.dav.internal.ShareHelper;
import com.openexchange.dav.resources.CommonResource;
import com.openexchange.dav.resources.FolderCollection;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.webdav.action.ReplayWebdavRequest;
import com.openexchange.webdav.action.WebdavRequest;
import com.openexchange.webdav.action.WebdavResponse;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;

/**
 * {@link POSTAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public class POSTAction extends DAVAction {

    /**
     * Initializes a new {@link POSTAction}.
     *
     * @param protocol The underlying protocol
     */
    public POSTAction(Protocol protocol) {
        super(protocol);
    }

    @Override
    public void perform(WebdavRequest request, WebdavResponse response) throws WebdavProtocolException {
        /*
         * default handling
         */
        handle(request, response);
    }

    /**
     * Tries to handle a common <code>POST</code> request. This includes:
     * <ul>
     * <li>attachment-related actions on {@link CommonResource}s</li>
     * <li>sharing-related actions on {@link FolderCollection}s</li>
     * </ul>
     *
     * @param request The WebDAV request
     * @param response The response
     * @return <code>true</code> if a suitable request was detected and handled, <code>false</code>, otherwise
     */
    protected boolean handle(WebdavRequest request, WebdavResponse response) throws WebdavProtocolException {
        WebdavResource resource = request.getResource();
        if (null != resource) {
            String action = request.getParameter("action");
            if (Strings.isNotEmpty(action) && CommonResource.class.isInstance(resource)) {
                /*
                 * handle special attachment action
                 */
                return handleAction(request, response);
            }
            String contentType = getContentType(request);
            if (("application/davsharing+xml".equals(contentType)) && FolderCollection.class.isInstance(resource)) {
                request = new ReplayWebdavRequest(request);
                Element rootElement = optRootElement(request, DAVProtocol.DAV_NS, "share-resource");
                if (null != rootElement) {
                    /*
                     * handle WebDAV share request
                     */
                    ShareHelper.shareResource((FolderCollection<?>) resource, rootElement);
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    return true;
                }
            }
            if ("text/xml".equals(contentType) && FolderCollection.class.isInstance(resource)) {
                request = new ReplayWebdavRequest(request);
                Element rootElement = optRootElement(request, DAVProtocol.CALENDARSERVER_NS, "share");
                if (null != rootElement) {
                    /*
                     * handle calendarserver share request
                     */
                    ShareHelper.share((FolderCollection<?>) resource, rootElement);
                    response.setStatus(HttpServletResponse.SC_OK);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Tries to handle a special action as indicated by the <code>action</code> query parameter.
     *
     * @param request The WebDAV request
     * @param response The response
     * @return <code>true</code> if a special action was detected and handled, <code>false</code>, otherwise
     */
    private boolean handleAction(WebdavRequest request, WebdavResponse response) throws WebdavProtocolException {
        String action = request.getParameter("action");
        if (Strings.isEmpty(action)) {
            return false;
        }
        switch (action) {
            case "attachment-add":
                addAttachment(request, response);
                return true;
            case "attachment-remove":
                removeAttachment(request, response);
                return true;
            case "attachment-update":
                updateAttachment(request, response);
                return true;
            default:
                return false;
        }
    }

    protected void writeResource(WebdavResource resource, WebdavResponse response) throws WebdavProtocolException {
        /*
         * write back response
         */
        response.setContentType(resource.getContentType());
        byte[] buffer = new byte[1024];
        OutputStream outputStream = null;
        InputStream inputStream = null;
        try {
            inputStream = resource.getBody();
            outputStream = response.getOutputStream();
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (IOException e) {
            org.slf4j.LoggerFactory.getLogger(POSTAction.class).debug("Client gone?", e);
        } finally {
            Streams.close(inputStream, outputStream);
        }
    }

    private void addAttachment(WebdavRequest request, WebdavResponse response) throws WebdavProtocolException {
        /*
         * get targeted resource & attachment related parameters
         */
        CommonResource<?> resource = requireResource(request, CommonResource.class);
        String contentType = getContentType(request);
        String fileName = AttachmentUtils.parseFileName(request);
        String[] recurrenceIDs = Strings.splitByComma(request.getHeader("rid"));
        long size = getContentLength(request);
        /*
         * save attachment
         */
        AttachmentMetadata metadata = null;
        InputStream inputStream = null;
        try {
            inputStream = request.getBody();
            metadata = resource.addAttachment(inputStream, contentType, fileName, size, recurrenceIDs)[0];
        } catch (IOException e) {
            throw WebdavProtocolException.Code.GENERAL_ERROR.create(request.getUrl(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (OXException e) {
            throw AttachmentUtils.protocolException(e, request.getUrl());
        } finally {
            Streams.close(inputStream);
        }
        /*
         * apply response headers
         */
        response.setStatus(HttpServletResponse.SC_CREATED);
        response.setContentType(resource.getContentType());
        response.setHeader("Content-Location", resource.getUrl().toString());
        response.setHeader("ETag", resource.getETag());
        response.setHeader("Cal-Managed-ID", String.valueOf(metadata.getId()));
    }

    private void removeAttachment(WebdavRequest request, WebdavResponse response) throws WebdavProtocolException {
        /*
         * get targeted resource & attachment related parameters
         */
        CommonResource<?> resource = requireResource(request, CommonResource.class);
        String managedId = request.getHeader("managed-id");
        if (Strings.isEmpty(managedId)) {
            throw WebdavProtocolException.generalError(request.getUrl(), HttpServletResponse.SC_BAD_REQUEST);
        }
        int attachmentId;
        try {
            attachmentId = Integer.parseInt(managedId);
        } catch (NumberFormatException e) {
            throw WebdavProtocolException.generalError(e, request.getUrl(), HttpServletResponse.SC_BAD_REQUEST);
        }
        String[] recurrenceIDs = Strings.splitByComma(request.getHeader("rid"));
        /*
         * remove attachment & apply response headers for successful removal
         */
        try {
            resource.removeAttachment(attachmentId, recurrenceIDs);
        } catch (OXException e) {
            throw AttachmentUtils.protocolException(e, request.getUrl());
        }
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    private void updateAttachment(WebdavRequest request, WebdavResponse response) throws WebdavProtocolException {
        /*
         * get targeted resource & attachment related parameters
         */
        CommonResource<?> resource = requireResource(request, CommonResource.class);
        String contentType = getContentType(request);
        String fileName = AttachmentUtils.parseFileName(request);
        long size = getContentLength(request);
        String managedId = request.getHeader("managed-id");
        if (Strings.isEmpty(managedId)) {
            throw WebdavProtocolException.generalError(request.getUrl(), HttpServletResponse.SC_BAD_REQUEST);
        }
        int attachmentId;
        try {
            attachmentId = Integer.parseInt(managedId);
        } catch (NumberFormatException e) {
            throw WebdavProtocolException.generalError(e, request.getUrl(), HttpServletResponse.SC_BAD_REQUEST);
        }
        /*
         * update attachment
         */
        AttachmentMetadata metadata = null;
        InputStream inputStream = null;
        try {
            inputStream = request.getBody();
            metadata = resource.updateAttachment(attachmentId, inputStream, contentType, fileName, size);
        } catch (IOException e) {
            throw WebdavProtocolException.Code.GENERAL_ERROR.create(request.getUrl(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (OXException e) {
            throw AttachmentUtils.protocolException(e, request.getUrl());
        } finally {
            Streams.close(inputStream);
        }
        /*
         * apply response headers
         */
        response.setStatus(HttpServletResponse.SC_CREATED);
        response.setContentType(resource.getContentType());
        response.setHeader("Content-Location", resource.getUrl().toString());
        response.setHeader("ETag", resource.getETag());
        response.setHeader("Cal-Managed-ID", String.valueOf(metadata.getId()));
    }

}
