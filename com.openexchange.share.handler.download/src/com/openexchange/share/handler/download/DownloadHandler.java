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

package com.openexchange.share.handler.download;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.AJAXUtility;
import com.openexchange.ajax.container.FileHolder;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.responseRenderers.FileResponseRenderer;
import com.openexchange.ajax.requesthandler.responseRenderers.RenderListener;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.Document;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageUtility;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.groupware.modules.Module;
import com.openexchange.share.servlet.handler.AccessShareRequest;
import com.openexchange.share.servlet.handler.HttpAuthShareHandler;
import com.openexchange.share.servlet.handler.ResolvedShare;
import com.openexchange.tools.servlet.ratelimit.RateLimitedException;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link DownloadHandler}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DownloadHandler extends HttpAuthShareHandler {

    private final FileResponseRenderer renderer;

    /**
     * Initializes a new {@link DownloadHandler}.
     */
    public DownloadHandler() {
        super();
        this.renderer = new FileResponseRenderer();
    }

    public void addRenderListener(RenderListener listener) {
        this.renderer.addRenderListener(listener);
    }

    public void removeRenderListener(RenderListener listener) {
        this.renderer.removeRenderListener(listener);
    }

    @Override
    public boolean keepSession() {
        return false;
    }

    @Override
    public int getRanking() {
        return 100;
    }

    @Override
    protected boolean handles(AccessShareRequest shareRequest, HttpServletRequest request, HttpServletResponse response) throws OXException {
        return Module.INFOSTORE.getFolderConstant() == shareRequest.getTarget().getModule() && null != shareRequest.getTarget().getItem() && (indicatesDownload(request) || indicatesRaw(request));
    }

    @Override
    protected void handleResolvedShare(ResolvedShare resolvedShare) throws OXException, IOException {
        /*
         * get document
         */
        ServerSession session = ServerSessionAdapter.valueOf(resolvedShare.getSession());
        final String id = resolvedShare.getShareRequest().getTarget().getItem();
        final String version = null; // as per com.openexchange.file.storage.FileStorageFileAccess.CURRENT_VERSION
        final IDBasedFileAccess fileAccess = Services.getService(IDBasedFileAccessFactory.class).createAccess(session);
        FileHolder fileHolder;
        String eTag;
        final Document document = fileAccess.getDocumentAndMetadata(id, version);
        if (null != document) {
            /*
             * prefer document and metadata if available
             */
            fileHolder = new FileHolder(new IFileHolder.InputStreamClosure() {

                @Override
                public InputStream newStream() throws OXException, IOException {
                    return document.getData();
                }

            }, document.getSize(), document.getMimeType(), document.getName());
            eTag = document.getEtag();
        } else {
            /*
             * load metadata, document on demand
             */
            final File fileMetadata = fileAccess.getFileMetadata(id, version);
            IFileHolder.InputStreamClosure isClosure = new IFileHolder.InputStreamClosure() {

                @Override
                public InputStream newStream() throws OXException, IOException {
                    InputStream inputStream = fileAccess.getDocument(id, version);
                    if (BufferedInputStream.class.isInstance(inputStream) || ByteArrayInputStream.class.isInstance(inputStream)) {
                        return inputStream;
                    }
                    return new BufferedInputStream(inputStream, 65536);
                }
            };
            fileHolder = new FileHolder(isClosure, fileMetadata.getFileSize(), fileMetadata.getFileMIMEType(), fileMetadata.getFileName());
            eTag = FileStorageUtility.getETagFor(fileMetadata);
        }
        /*
         * prepare renderer-compatible request result
         */
        AJAXRequestData request = AJAXRequestDataTools.getInstance().parseRequest(resolvedShare.getRequest(), false, false, session, "/share", resolvedShare.getResponse());
        request.setSession(session);
        AJAXRequestResult result = new AJAXRequestResult(fileHolder, "file");
        if (null != eTag) {
            result.setHeader("ETag", eTag);
        }
        /*
         * render response via file response renderer
         */
        if (indicatesRaw(resolvedShare.getRequest())) {
            fileHolder.setDelivery("view");
            fileHolder.setDisposition("inline");
        } else {
            fileHolder.setDelivery("download");
            fileHolder.setDisposition("attachment");
        }
        try {
            renderer.write(request, result, resolvedShare.getRequest(), resolvedShare.getResponse());
        } catch (RateLimitedException e) {
            e.send(resolvedShare.getResponse());
        }
    }

    private static boolean indicatesRaw(HttpServletRequest request) {
        return "view".equalsIgnoreCase(AJAXUtility.sanitizeParam(request.getParameter("delivery"))) || isTrue(AJAXUtility.sanitizeParam(request.getParameter("raw")));
    }

}
