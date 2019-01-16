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
import com.openexchange.ajax.fileholder.IFileHolder.InputStreamClosure;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.responseRenderers.FileResponseRenderer;
import com.openexchange.ajax.requesthandler.responseRenderers.RenderListener;
import com.openexchange.antivirus.AntiVirusResult;
import com.openexchange.antivirus.AntiVirusResultEvaluatorService;
import com.openexchange.antivirus.AntiVirusService;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.Document;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageUtility;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.groupware.modules.Module;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.servlet.handler.AccessShareRequest;
import com.openexchange.share.servlet.handler.HttpAuthShareHandler;
import com.openexchange.share.servlet.handler.ResolvedShare;
import com.openexchange.tools.id.IDMangler;
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
    protected boolean handles(AccessShareRequest shareRequest, HttpServletRequest request, HttpServletResponse response) {
        if (indicatesDownload(request) || indicatesRaw(request)) {
            ShareTarget target = shareRequest.getTarget();
            return null == target || Module.INFOSTORE.getFolderConstant() == target.getModule() && null != target.getItem();
        }
        return false;
    }

    @Override
    protected void handleResolvedShare(ResolvedShare resolvedShare) throws OXException, IOException {
        /*
         * get document
         */
        ServerSession session = ServerSessionAdapter.valueOf(resolvedShare.getSession());
        ShareTarget target = resolvedShare.getShareRequest().getTarget();
        if (null == target) {
            throw ShareExceptionCodes.UNKNOWN_SHARE.create(resolvedShare.getShareRequest().getTargetPath());
        }
        final String id = target.getItem();
        final String version = null; // as per com.openexchange.file.storage.FileStorageFileAccess.CURRENT_VERSION
        IDBasedFileAccessFactory service = Services.getService(IDBasedFileAccessFactory.class);
        if (null == service) {
            throw ServiceExceptionCode.absentService(IDBasedFileAccessFactory.class);
        }
        final IDBasedFileAccess fileAccess = service.createAccess(session);
        final Document document = fileAccess.getDocumentAndMetadata(id, version);
        /*
         * create file holder
         */
        FileHolder fileHolder = null;
        try {
            String eTag;
            String uniqueId;
            if (null == document) {
                /*
                 * load metadata, document on demand
                 */
                final File fileMetadata = fileAccess.getFileMetadata(id, version);
                IFileHolder.InputStreamClosure isClosure = streamClosureFor(id, version, fileAccess);
                fileHolder = new FileHolder(isClosure, fileMetadata.getFileSize(), fileMetadata.getFileMIMEType(), fileMetadata.getFileName());
                eTag = FileStorageUtility.getETagFor(fileMetadata);
                uniqueId = getUniqueId(fileMetadata, session.getContextId());
                boolean scanned = scan(session, resolvedShare.getResponse(), fileHolder, uniqueId);
                if (scanned && false == fileHolder.repetitive()) {
                    fileHolder = new FileHolder(isClosure, fileMetadata.getFileSize(), fileMetadata.getFileMIMEType(), fileMetadata.getFileName());
                }
            } else {
                /*
                 * prefer document and metadata if available
                 */
                fileHolder = new FileHolder(() -> document.getData(), document.getSize(), document.getMimeType(), document.getName());
                eTag = document.getEtag();
                uniqueId = document.getFile() == null ? eTag : getUniqueId(document.getFile(), session.getContextId());
                boolean scanned = scan(session, resolvedShare.getResponse(), fileHolder, uniqueId);
                if (scanned && false == fileHolder.repetitive()) {
                    fileHolder = new FileHolder(() -> document.getData(), document.getSize(), document.getMimeType(), document.getName());
                }
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
        } finally {
            Streams.close(fileHolder);
        }
    }

    /**
     * Creates an {@link InputStreamClosure} for the file with the specified identifier
     * 
     * @param id The file identifier
     * @param version The file version
     * @param fileAccess The {@link IDBasedFileAccess}
     * @return The {@InputStreamClosure}
     */
    private IFileHolder.InputStreamClosure streamClosureFor(final String id, final String version, final IDBasedFileAccess fileAccess) {
        return () -> {
            InputStream inputStream = fileAccess.getDocument(id, version);
            if (BufferedInputStream.class.isInstance(inputStream) || ByteArrayInputStream.class.isInstance(inputStream)) {
                return inputStream;
            }
            return new BufferedInputStream(inputStream, 65536);
        };
    }

    private static boolean indicatesRaw(HttpServletRequest request) {
        return "view".equalsIgnoreCase(AJAXUtility.sanitizeParam(request.getParameter("delivery"))) || isTrue(AJAXUtility.sanitizeParam(request.getParameter("raw")));
    }

    /**
     * Scans the specified IFileHolder and sends a 403 error to the client if the enclosed stream is infected.
     * 
     * @param session The session
     * @param response The {@link HttpServletResponse} with which to send a 403 error to the client in case the file is infected
     * @param fileHolder The {@link IFileHolder}
     * @param uniqueId the unique identifier
     * @throws OXException if the file is too large, or if the {@link AntiVirusService} is absent,
     *             or if the file is infected, or if a timeout or any other error is occurred. If the file
     *             is infected then a 403 will be send to the client
     * @throws IOException if an I/O error is occurred when sending a 403 error to the client
     */
    private boolean scan(Session session, HttpServletResponse response, IFileHolder fileHolder, String uniqueId) throws OXException, IOException {
        AntiVirusService service = Services.getOptionalService(AntiVirusService.class);
        AntiVirusResultEvaluatorService evaluator = Services.getOptionalService(AntiVirusResultEvaluatorService.class);
        if (null == service || null == evaluator) {
            return false;
        }
        if (false == service.isEnabled(session)) {
            return false;
        }
        try {
            AntiVirusResult result = service.scan(fileHolder, uniqueId);
            evaluator.evaluate(result, fileHolder.getName());
            return result.isStreamScanned();
        } catch (OXException e) {
            response.sendError(403);
            throw e;
        }
    }

    /**
     * Gets the identifier that uniquely identifies the specified {@link File}, being
     * either the MD5 checksum, or the file identifier (in that order). If none is present
     * then the fall-back identifier is returned
     * 
     * @param file The {@link File}
     * @param contextId The context identifier
     * @return The unique identifier, never <code>null</code>
     */
    private String getUniqueId(File file, int contextId) {
        String id = file.getFileMD5Sum();
        if (Strings.isNotEmpty(id)) {
            return id;
        }
        return IDMangler.mangle(Integer.toString(contextId), file.getId(), file.getVersion(), Long.toString(file.getSequenceNumber()));
    }
}
