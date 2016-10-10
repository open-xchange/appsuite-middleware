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

package com.openexchange.groupware.attach.json.actions;

import java.io.InputStream;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.java.Streams;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link GetDocumentAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Action(method = RequestMethod.GET, name = "[filename]?action=document", description = "Get an attachments filedata.", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "folder", description = "Object ID of the folder, whose contents are queried."),
    @Parameter(name = "attached", description= "Object ID of the object to which the attachments are attached."),
    @Parameter(name = "module", description = "Module ID (as per Attachment object) of the attached object."),
    @Parameter(name = "id", description = "Object ID of the requested attachment."),
    @Parameter(name = "content_type", optional=true, description = "If set the responses Content-Type header is set to this value, not the attachements file mime type.")
}, responseDescription = "The raw byte data of the document. The response type for the HTTP Request is set accordingly to the defined mimetype for this infoitem. Note: The File name may be added to the customary infostore path to suggest a filename to a Save-As dialog.")
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
        require(
            requestData,
            AJAXServlet.PARAMETER_FOLDERID,
            AJAXServlet.PARAMETER_ATTACHEDID,
            AJAXServlet.PARAMETER_MODULE,
            AJAXServlet.PARAMETER_ID);

        int folderId, attachedId, moduleId, id;
        final String contentType = requestData.getParameter(AJAXServlet.PARAMETER_CONTENT_TYPE);
        folderId = requireNumber(requestData, AJAXServlet.PARAMETER_FOLDERID);
        attachedId = requireNumber(requestData, AJAXServlet.PARAMETER_ATTACHEDID);
        moduleId = requireNumber(requestData, AJAXServlet.PARAMETER_MODULE);
        id = requireNumber(requestData, AJAXServlet.PARAMETER_ID);

        if(!"preview_image".equals(requestData.getFormat())) {
            requestData.setFormat("file");
        }
        return document(
            session, folderId,
            attachedId,
            moduleId,
            id,
            contentType,
            requestData.getParameter(AJAXServlet.PARAMETER_DELIVERY),
            session.getContext(),
            session.getUser(),
            session.getUserConfiguration(),
            requestData);
    }

    private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

    private AJAXRequestResult document(final Session session, final int folderId, final int attachedId, final int moduleId, final int id, final String contentType, final String delivery, final Context ctx, final User user, final UserConfiguration userConfig, final AJAXRequestData requestData) throws OXException {
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
            ThresholdFileHolder fileHolder = new ThresholdFileHolder();
            InputStream documentData = ATTACHMENT_BASE.getAttachedFile(session, folderId, attachedId, moduleId, id, ctx, user, userConfig);
            try {
                fileHolder.write(documentData);
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
            return new AJAXRequestResult(fileHolder, "file");
        } catch (final Throwable t) {
            // This is a bit convoluted: In case the contentType is not
            // overridden the returned file will be opened
            // in a new window. To call the JS callback routine from a popup we
            // can use parent.callback_error() but
            // must use window.opener.callback_error()
            rollback();
            if (t instanceof OXException) {
                throw (OXException) t;
            }
            throw new OXException(t);
        } finally {
            try {
                ATTACHMENT_BASE.finish();
            } catch (final OXException e) {
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

}
