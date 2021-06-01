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

package com.openexchange.groupware.attach.json.actions;

import java.io.InputStream;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.antivirus.AntiVirusEncapsulatedContent;
import com.openexchange.antivirus.AntiVirusEncapsulationUtil;
import com.openexchange.antivirus.AntiVirusResult;
import com.openexchange.antivirus.AntiVirusResultEvaluatorService;
import com.openexchange.antivirus.AntiVirusService;
import com.openexchange.antivirus.exceptions.AntiVirusServiceExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;

/**
 * {@link GetDocumentAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class GetDocumentAction extends AbstractAttachmentAction {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(GetDocumentAction.class);

    /**
     * Initializes a new {@link GetDocumentAction}.
     */
    public GetDocumentAction(final ServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @Override
    public AJAXRequestResult perform(final AJAXRequestData requestData, final ServerSession session) throws OXException {
        require(requestData, AJAXServlet.PARAMETER_FOLDERID, AJAXServlet.PARAMETER_ATTACHEDID, AJAXServlet.PARAMETER_MODULE, AJAXServlet.PARAMETER_ID);

        int folderId, attachedId, moduleId, id;
        final String contentType = requestData.getParameter(AJAXServlet.PARAMETER_CONTENT_TYPE);
        folderId = requireNumber(requestData, AJAXServlet.PARAMETER_FOLDERID);
        attachedId = requireNumber(requestData, AJAXServlet.PARAMETER_ATTACHEDID);
        moduleId = requireNumber(requestData, AJAXServlet.PARAMETER_MODULE);
        id = requireNumber(requestData, AJAXServlet.PARAMETER_ID);

        if (!"preview_image".equals(requestData.getFormat())) {
            requestData.setFormat("file");
        }
        return document(session, folderId, attachedId, moduleId, id, contentType, requestData.getParameter(AJAXServlet.PARAMETER_DELIVERY), session.getContext(), session.getUser(), session.getUserConfiguration(), requestData);
    }

    private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

    private AJAXRequestResult document(ServerSession session, int folderId, int attachedId, int moduleId, int id, String contentType, String delivery, Context ctx, User user, UserConfiguration userConfig, AJAXRequestData requestData) throws OXException {
        ThresholdFileHolder fileHolder = null;
        boolean rollback = true;
        try {
            ATTACHMENT_BASE.startTransaction();
            final AttachmentMetadata attachment = ATTACHMENT_BASE.getAttachment(session, folderId, attachedId, moduleId, id, ctx, user, userConfig);
            String sContentType;
            if ((null == contentType) || (APPLICATION_OCTET_STREAM.equals(com.openexchange.java.Strings.toLowerCase(contentType)))) {
                sContentType = APPLICATION_OCTET_STREAM;
            } else {
                final String contentTypeByFileName = MimeType2ExtMap.getContentType(attachment.getFilename(), null);
                String preferredContentType = attachment.getFileMIMEType();
                if (null != contentTypeByFileName) {
                    if (APPLICATION_OCTET_STREAM.equals(preferredContentType)) {
                        preferredContentType = contentTypeByFileName;
                    } else {
                        final String primaryType1 = getPrimaryType(preferredContentType);
                        final String primaryType2 = getPrimaryType(contentTypeByFileName);
                        if (!com.openexchange.java.Strings.toLowerCase(primaryType1).startsWith(com.openexchange.java.Strings.toLowerCase(primaryType2))) {
                            preferredContentType = contentTypeByFileName;
                        }
                    }
                }
                // Compare...
                final String primaryType1 = getPrimaryType(preferredContentType);
                final String primaryType2 = getPrimaryType(contentType);
                if (com.openexchange.java.Strings.toLowerCase(primaryType1).startsWith(com.openexchange.java.Strings.toLowerCase(primaryType2))) {
                    sContentType = contentType;
                } else {
                    // Specified Content-Type does NOT match file's real MIME type
                    // Therefore ignore it due to security reasons (see bug #25343)
                    final StringBuilder sb = new StringBuilder(128);
                    sb.append("Denied parameter \"").append(AJAXServlet.PARAMETER_CONTENT_TYPE).append("\" due to security constraints (");
                    sb.append(contentType).append(" vs. ").append(preferredContentType).append(").");
                    LOG.warn(sb.toString());
                    sContentType = preferredContentType;
                }
            }
            /*
             * Get input stream & write to sink
             */
            fileHolder = new ThresholdFileHolder();
            InputStream documentData = ATTACHMENT_BASE.getAttachedFile(session, folderId, attachedId, moduleId, id, ctx, user, userConfig);
            try {
                fileHolder.write(documentData);
                boolean scanned = scanAttachment(requestData, session, folderId, attachedId, moduleId, id, userConfig, fileHolder, attachment);
                if (scanned && false == fileHolder.repetitive()) {
                    documentData = ATTACHMENT_BASE.getAttachedFile(session, folderId, attachedId, moduleId, id, ctx, user, userConfig);
                    fileHolder.write(documentData);
                }
            } finally {
                Streams.close(documentData);
            }
            /*
             * File holder
             */
            fileHolder.setContentType(sContentType);
            fileHolder.setName(attachment.getFilename());
            fileHolder.setDelivery(delivery);
            ATTACHMENT_BASE.commit();
            AJAXRequestResult requestResult = new AJAXRequestResult(fileHolder, "file");
            fileHolder = null; // Avoid premature closing
            rollback = false;
            return requestResult;
        } finally {
            Streams.close(fileHolder);
            if (rollback) {
                rollback();
            }
            try {
                ATTACHMENT_BASE.finish();
            } catch (Exception e) {
                LOG.debug("", e);
            }
        }
    }

    private String getPrimaryType(final String contentType) {
        if (com.openexchange.java.Strings.isEmpty(contentType)) {
            return contentType;
        }
        final int pos = contentType.indexOf('/');
        return pos > 0 ? contentType.substring(0, pos) : contentType;
    }

    /**
     * Checks whether the {@link InputStream} in the specified closure should be scanned
     * 
     * @param request The {@link AJAXRequestData}
     * @param session The {@link ServerSession}
     * @param folderId the folder identifier
     * @param attachedId The attached identifier
     * @param moduleId The module identifier
     * @param id The identifier
     * @param userConfig The user configuration
     * @param attachment The attachment metadata
     * @return <code>true</code> if the stream was scanned; <code>false</code> otherwise.
     * @throws OXException if the file is too large, or if the {@link AntiVirusService} is absent,
     *             or if the file is infected, or if a timeout or any other error is occurred
     */
    private boolean scanAttachment(AJAXRequestData request, ServerSession session, int folderId, int attachedId, int moduleId, int id, UserConfiguration userConfig, IFileHolder fileHolder, AttachmentMetadata attachment) throws OXException {
        String scan = request.getParameter("scan");
        Boolean s = Strings.isEmpty(scan) ? Boolean.FALSE : Boolean.valueOf(scan);
        if (false == s.booleanValue()) {
            LOG.debug("No anti-virus scanning was performed.");
            return false;
        }
        AntiVirusService antiVirusService = serviceLookup.getOptionalService(AntiVirusService.class);
        if (antiVirusService == null) {
            throw AntiVirusServiceExceptionCodes.ANTI_VIRUS_SERVICE_ABSENT.create();
        }
        if (false == antiVirusService.isEnabled(request.getSession())) {
            return false;
        }
        AntiVirusEncapsulatedContent content = AntiVirusEncapsulationUtil.encapsulate(request.optHttpServletRequest(), request.optHttpServletResponse());
        AntiVirusResult result = antiVirusService.scan(fileHolder, attachment.getFileId(), content);
        serviceLookup.getServiceSafe(AntiVirusResultEvaluatorService.class).evaluate(result, attachment.getFilename());
        return result.isStreamScanned();
    }
}
