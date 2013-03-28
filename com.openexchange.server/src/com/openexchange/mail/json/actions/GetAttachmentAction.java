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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.mail.json.actions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.container.ByteArrayFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.DispatcherNotes;
import com.openexchange.ajax.requesthandler.ETagAwareAJAXActionService;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.file.storage.parse.FileMetadataParserService;
import com.openexchange.html.HtmlService;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * {@link GetAttachmentAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Action(method = RequestMethod.GET, name = "get", description = "Get a mail.", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "folder", description = "The folder identifier."),
    @Parameter(name = "id", description = "Object ID of the mail which contains the attachment."),
    @Parameter(name = "attachment", description = "ID of the requested attachment OR"),
    @Parameter(name = "cid", description = "Value of header 'Content-ID' of the requested attachment"),
    @Parameter(name = "save", description = "1 overwrites the defined mimetype for this attachment to force the download dialog, otherwise 0."),
    @Parameter(name = "filter", optional=true, description = "1 to apply HTML white-list filter rules if and only if requested attachment is of MIME type text/htm* AND parameter save is set to 0.")
}, responseDescription = "The raw byte data of the document. The response type for the HTTP Request is set accordingly to the defined mimetype for this attachment, except the parameter save is set to 1.")
@DispatcherNotes(allowPublicSession = true)
public final class GetAttachmentAction extends AbstractMailAction implements ETagAwareAJAXActionService {

    /**
     * Initializes a new {@link GetAttachmentAction}.
     *
     * @param services
     */
    public GetAttachmentAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    public boolean checkETag(final String clientETag, final AJAXRequestData request, final ServerSession session) throws OXException {
        if (clientETag == null || clientETag.length() == 0) {
            return false;
        }
        /*
         * Any ETag is valid because an attachment cannot change
         */
        return true;
    }

    @Override
    public void setETag(final String eTag, final long expires, final AJAXRequestResult result) throws OXException {
        result.setExpires(expires);
        result.setHeader("ETag", eTag);
    }

    @Override
    protected AJAXRequestResult perform(final MailRequest req) throws OXException {
        final JSONObject bodyObject = (JSONObject) req.getRequest().getData();
        if (null == bodyObject) {
            return performGET(req);
        }
        return performPUT(req, bodyObject);
    }

    private AJAXRequestResult performGET(final MailRequest req) throws OXException {
        try {
            // final ServerSession session = req.getSession();
            /*
             * Read in parameters
             */
            final String folderPath = req.checkParameter(AJAXServlet.PARAMETER_FOLDERID);
            final String uid = req.checkParameter(AJAXServlet.PARAMETER_ID);
            final String sequenceId = req.getParameter(Mail.PARAMETER_MAILATTCHMENT);
            final String imageContentId = req.getParameter(Mail.PARAMETER_MAILCID);
            final boolean saveToDisk;
            {
                final String saveParam = req.getParameter(Mail.PARAMETER_SAVE);
                saveToDisk = ((saveParam == null || saveParam.length() == 0) ? false : ((Integer.parseInt(saveParam)) > 0));
            }
            final boolean filter;
            {
                final String filterParam = req.getParameter(Mail.PARAMETER_FILTER);
                filter = Boolean.parseBoolean(filterParam) || "1".equals(filterParam);
            }
            /*
             * Get mail interface
             */
            final MailServletInterface mailInterface = getMailInterface(req);
            if (sequenceId == null && imageContentId == null) {
                throw MailExceptionCode.MISSING_PARAM.create(new com.openexchange.java.StringAllocator().append(Mail.PARAMETER_MAILATTCHMENT).append(" | ").append(
                    Mail.PARAMETER_MAILCID).toString());
            }
            final MailPart mailPart;
            InputStream attachmentInputStream;
            if (imageContentId == null) {
                mailPart = mailInterface.getMessageAttachment(folderPath, uid, sequenceId, !saveToDisk);
                if (mailPart == null) {
                    throw MailExceptionCode.NO_ATTACHMENT_FOUND.create(sequenceId);
                }
                if (filter && !saveToDisk && mailPart.getContentType().isMimeType(MimeTypes.MIME_TEXT_HTM_ALL)) {
                    /*
                     * Apply filter
                     */
                    final ContentType contentType = mailPart.getContentType();
                    final String cs =
                        contentType.containsCharsetParameter() ? contentType.getCharsetParameter() : MailProperties.getInstance().getDefaultMimeCharset();
                    final String htmlContent = MessageUtility.readMailPart(mailPart, cs);
                    final HtmlService htmlService = ServerServiceRegistry.getInstance().getService(HtmlService.class);
                    attachmentInputStream = new UnsynchronizedByteArrayInputStream(sanitizeHtml(htmlContent, htmlService).getBytes(Charsets.forName(cs)));
                } else {
                    attachmentInputStream = mailPart.getInputStream();
                }
            } else {
                mailPart = mailInterface.getMessageImage(folderPath, uid, imageContentId);
                if (mailPart == null) {
                    throw MailExceptionCode.NO_ATTACHMENT_FOUND.create(sequenceId);
                }
                attachmentInputStream = mailPart.getInputStream();
            }
            /*
             * Read from stream
             */
            @SuppressWarnings("resource")
            ByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream();
            /*
             * Write from content's input stream to byte array output stream
             */
            try {
                final int buflen = 0xFFFF;
                final byte[] buffer = new byte[buflen];
                for (int len; (len = attachmentInputStream.read(buffer, 0, buflen)) > 0;) {
                    out.write(buffer, 0, len);
                }
                out.flush();
            } finally {
                Streams.close(attachmentInputStream);
            }
            /*
             * Create file holder
             */
            {
                final AJAXRequestData requestData = req.getRequest();
                if(!"preview_image".equals(requestData.getFormat())) {
                    requestData.setFormat("file");
                }
            }
            final ByteArrayFileHolder fileHolder = new ByteArrayFileHolder(out.toByteArray());
            out = null;
            fileHolder.setName(mailPart.getFileName());
            fileHolder.setContentType(saveToDisk ? "application/octet-stream" : mailPart.getContentType().toString());
            final AJAXRequestResult result = new AJAXRequestResult(fileHolder, "file");
            /*
             * Set ETag
             */
            setETag(UUID.randomUUID().toString(), AJAXRequestResult.YEAR_IN_MILLIS * 50, result);
            /*
             * Return result
             */
            return result;
        } catch (final IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName())) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            }
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private AJAXRequestResult performPUT(final MailRequest req, final JSONObject bodyObject) throws OXException {
        try {
            final ServerSession session = req.getSession();
            /*
             * Read in parameters
             */
            final String folderPath = req.checkParameter(AJAXServlet.PARAMETER_FOLDERID);
            final String uid = req.checkParameter(AJAXServlet.PARAMETER_ID);
            final String sequenceId = req.checkParameter(Mail.PARAMETER_MAILATTCHMENT);
            final String destFolderIdentifier = req.checkParameter(Mail.PARAMETER_DESTINATION_FOLDER);
            /*
             * Get mail interface
             */
            final MailServletInterface mailInterface = getMailInterface(req);
            final ServerServiceRegistry serviceRegistry = ServerServiceRegistry.getInstance();
            final IDBasedFileAccess fileAccess = serviceRegistry.getService(IDBasedFileAccessFactory.class).createAccess(session);
            boolean performRollback = false;
            try {
                if (!session.getUserConfiguration().hasInfostore()) {
                    throw MailExceptionCode.NO_MAIL_ACCESS.create();
                }
                final MailPart mailPart = mailInterface.getMessageAttachment(folderPath, uid, sequenceId, false);
                if (mailPart == null) {
                    throw MailExceptionCode.NO_ATTACHMENT_FOUND.create(sequenceId);
                }
                final String destFolderID = destFolderIdentifier;
                /*
                 * Create document's meta data
                 */
                final FileMetadataParserService parser = serviceRegistry.getService(FileMetadataParserService.class, true);
                final JSONObject jsonFileObject = bodyObject;
                final File file = parser.parse(jsonFileObject);
                final List<Field> fields = parser.getFields(jsonFileObject);
                final Set<Field> set = EnumSet.copyOf(fields);
                String fileName = mailPart.getFileName();
                if (isEmpty(fileName)) {
                    fileName = "part_" + sequenceId + ".dat";
                }
                if (!set.contains(Field.FILENAME) || isEmpty(file.getFileName())) {
                    file.setFileName(fileName);
                }
                file.setFileMIMEType(mailPart.getContentType().toString());
                /*
                 * Since file's size given from IMAP server is just an estimation and therefore does not exactly match the file's size a
                 * future file access via webdav can fail because of the size mismatch. Thus set the file size to 0 to make the infostore
                 * measure the size.
                 */
                file.setFileSize(0);
                if (!set.contains(Field.TITLE) || isEmpty(file.getTitle())) {
                    file.setTitle(fileName);
                }
                file.setFolderId(destFolderID);
                /*
                 * Start writing to infostore folder
                 */
                fileAccess.startTransaction();
                performRollback = true;
                fileAccess.saveDocument(file, mailPart.getInputStream(), System.currentTimeMillis(), fields);
                fileAccess.commit();
            } catch (final Exception e) {
                if (performRollback) {
                    fileAccess.rollback();
                }
                throw e;
            } finally {
                if (fileAccess != null) {
                    fileAccess.finish();
                }
            }
            return new AJAXRequestResult(new JSONArray(), "json");
        } catch (final OXException e) {
            throw e;
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private static String sanitizeHtml(final String htmlContent, final HtmlService htmlService) {
        return htmlService.sanitize(htmlContent, null, false, null, null);
    }

}
