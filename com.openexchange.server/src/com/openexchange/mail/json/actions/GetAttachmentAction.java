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

package com.openexchange.mail.json.actions;

import static com.openexchange.java.Strings.toLowerCase;
import static com.openexchange.mail.mime.MimeTypes.equalPrimaryTypes;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.mail.MessageRemovedException;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.AJAXUtility;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.SessionServlet;
import com.openexchange.ajax.container.FileHolder;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.DispatcherNotes;
import com.openexchange.ajax.requesthandler.ETagAwareAJAXActionService;
import com.openexchange.ajax.requesthandler.LastModifiedAwareAJAXActionService;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.parse.FileMetadataParserService;
import com.openexchange.html.HtmlService;
import com.openexchange.html.HtmlServices;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.attachment.storage.DefaultMailAttachmentStorageRegistry;
import com.openexchange.mail.attachment.storage.MailAttachmentStorage;
import com.openexchange.mail.attachment.storage.StoreOperation;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MimeStructureFixer;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.mail.parser.MailMessageParser;
import com.openexchange.mail.parser.handlers.MailPartHandler;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.HashUtility;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;

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
    @Parameter(name = "filter", optional = true, description = "1 to apply HTML white-list filter rules if and only if requested attachment is of MIME type text/htm* AND parameter save is set to 0.") }, responseDescription = "The raw byte data of the document. The response type for the HTTP Request is set accordingly to the defined mimetype for this attachment, except the parameter save is set to 1.")
@DispatcherNotes(allowPublicSession = true)
public final class GetAttachmentAction extends AbstractMailAction implements ETagAwareAJAXActionService, LastModifiedAwareAJAXActionService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(GetAttachmentAction.class);

    private static final long EXPIRES_MILLIS_YEAR = AJAXRequestResult.YEAR_IN_MILLIS * 50;
    private static final String PARAMETER_FILTER = Mail.PARAMETER_FILTER;
    private static final String PARAMETER_SAVE = Mail.PARAMETER_SAVE;
    private static final String PARAMETER_ID = AJAXServlet.PARAMETER_ID;
    private static final String PARAMETER_FOLDERID = AJAXServlet.PARAMETER_FOLDERID;
    private static final String PARAMETER_MAILCID = Mail.PARAMETER_MAILCID;
    private static final String PARAMETER_MAILATTCHMENT = Mail.PARAMETER_MAILATTCHMENT;
    private static final String PARAMETER_DELIVERY = Mail.PARAMETER_DELIVERY;

    private final String lastModified;

    /**
     * Initializes a new {@link GetAttachmentAction}.
     *
     * @param services
     */
    public GetAttachmentAction(ServiceLookup services) {
        super(services);
        lastModified = Tools.formatHeaderDate(new Date(309049200000L));
    }

    @Override
    public boolean checkLastModified(long clientLastModified, AJAXRequestData request, ServerSession session) throws OXException {
        /*
         * Any Last-Modified is valid because an attachment cannot change
         */
        return clientLastModified > 0;
    }

    @Override
    public boolean checkETag(String clientETag, AJAXRequestData request, ServerSession session) throws OXException {
        if (clientETag == null || clientETag.length() == 0) {
            return false;
        }
        /*
         * Any ETag is valid because an attachment cannot change
         */
        return true;
    }

    @Override
    public void setETag(String eTag, long expires, AJAXRequestResult result) throws OXException {
        result.setExpires(expires);
        result.setHeader("ETag", eTag);
    }

    @Override
    protected AJAXRequestResult perform(MailRequest req) throws OXException {
        JSONObject bodyObject = (JSONObject) req.getRequest().getData();
        if (null == bodyObject) {
            return performGET(req);
        }
        return performPUT(req, bodyObject);
    }

    /**
     * Performs GET request.
     *
     * @param req The mail request
     * @return The result
     * @throws OXException If something fails
     */
    public AJAXRequestResult performGET(final MailRequest req) throws OXException {
        try {
            // Read in parameters
            final String folderPath = req.checkParameter(PARAMETER_FOLDERID);
            final String uid = req.checkParameter(PARAMETER_ID);
            String sequenceId = req.getParameter(PARAMETER_MAILATTCHMENT);
            String imageContentId = req.getParameter(PARAMETER_MAILCID);
            String fileNameFromRequest = req.getParameter("save_as");
            boolean saveToDisk;
            {
                String saveParam = req.getParameter(PARAMETER_SAVE);
                saveToDisk = AJAXRequestDataTools.parseBoolParameter(saveParam) || "download".equals(toLowerCase(req.getParameter(PARAMETER_DELIVERY)));
            }
            boolean filter;
            {
                String filterParam = req.getParameter(PARAMETER_FILTER);
                filter = Boolean.parseBoolean(filterParam) || "1".equals(filterParam);
            }
            boolean fromStructure;
            {
                String fromStructureParam = req.getParameter("from_structure");
                fromStructure = Boolean.parseBoolean(fromStructureParam) || "1".equals(fromStructureParam);
            }
            boolean unseen;
            {
                String tmp = req.getParameter(Mail.PARAMETER_UNSEEN);
                unseen = (tmp != null && ("1".equals(tmp) || Boolean.parseBoolean(tmp)));
            }
            boolean asJson = AJAXRequestDataTools.parseBoolParameter("as_json", req.getRequest());
            if (sequenceId == null && imageContentId == null) {
                throw MailExceptionCode.MISSING_PARAM.create(new StringBuilder().append(PARAMETER_MAILATTCHMENT).append(" | ").append(PARAMETER_MAILCID).toString());
            }

            // Get mail interface
            MailServletInterface mailInterface = getMailInterface(req);

            if (asJson) {
                if (sequenceId == null) {
                    throw MailExceptionCode.MISSING_PARAM.create(PARAMETER_MAILATTCHMENT);
                }
                imageContentId = null;
            }

            long size = -1L; /* mail system does not provide exact size */
            MailPart mailPart = null;
            IFileHolder.InputStreamClosure isClosure = null;
            ThresholdFileHolder sink = null;
            Boolean markUnseen = null;

            if (imageContentId == null) {
                // Check if part should be fetched from a previously "fixed" message
                if (fromStructure) {
                    MailMessage mail = mailInterface.getMessage(folderPath, uid);
                    if (null == mail) {
                        throw MailExceptionCode.MAIL_NOT_FOUND.create(uid, folderPath);
                    }
                    boolean wasUnseen = (mail.containsPrevSeen() && !mail.isPrevSeen());
                    markUnseen = Boolean.valueOf(unseen && wasUnseen);
                    if (MimeStructureFixer.getInstance().isApplicableFor(mail)) {
                        // Assume as being "fixed" before passing to client
                        mail = MimeStructureFixer.getInstance().process(mail);
                        final MailPartHandler handler = new MailPartHandler(sequenceId);
                        new MailMessageParser().parseMailMessage(mail, handler);

                        final MailPart ret = handler.getMailPart();
                        if (ret == null) {
                            throw MailExceptionCode.ATTACHMENT_NOT_FOUND.create(sequenceId, uid, folderPath);
                        }

                        mailPart = ret;
                        boolean exactLength = AJAXRequestDataTools.parseBoolParameter(req.getParameter("exact_length")) || clientRequestsRange(req);
                        if (exactLength) {
                            sink = new ThresholdFileHolder();
                            InputStream in = Streams.getNonEmpty(ret.getInputStream());
                            sink.write(null == in ? Streams.EMPTY_INPUT_STREAM : in);
                            size = sink.getLength();
                        } else {
                            isClosure = new IFileHolder.InputStreamClosure() {

                                @Override
                                public InputStream newStream() throws OXException, IOException {
                                    return ret.getInputStream();
                                }
                            };
                        }
                    }
                }

                if (null == mailPart) {
                    if (null == markUnseen && unseen) {
                        MailMessage mail = mailInterface.getMessage(folderPath, uid);
                        if (null == mail) {
                            throw MailExceptionCode.MAIL_NOT_FOUND.create(uid, folderPath);
                        }
                        markUnseen = Boolean.valueOf(mail.containsPrevSeen() && !mail.isPrevSeen());
                    }
                    mailPart = mailInterface.getMessageAttachment(folderPath, uid, sequenceId, !saveToDisk);
                    if (mailPart == null) {
                        throw MailExceptionCode.NO_ATTACHMENT_FOUND.create(sequenceId);
                    }
                }

                if (asJson && mailPart.getContentType().startsWith("message/rfc822")) {
                    MailMessage nestedMailMessage = MailMessageParser.getMessageContentFrom(mailPart);
                    if (null != nestedMailMessage) {
                        nestedMailMessage.setAccountId(mailInterface.getAccountID());
                        nestedMailMessage.setSequenceId(nestedMailMessage.getSequenceId() == null ? sequenceId : sequenceId + "." + nestedMailMessage.getSequenceId());
                        // Prepare request/result objects
                        AJAXRequestData requestData = req.getRequest();
                        requestData.putParameter("embedded", "true");
                        requestData.putParameter(Mail.PARAMETER_ALLOW_NESTED_MESSAGES, "false");
                        AJAXRequestResult requestResult = new AJAXRequestResult(nestedMailMessage, "mail");

                        return requestResult;
                    }
                }

                if (filter && !saveToDisk && ((Strings.startsWithAny(toLowerCase(mailPart.getContentType().getSubType()), "htm", "xhtm") && fileNameAbsentOrIndicatesHtml(mailPart.getFileName())) || fileNameIndicatesHtml(mailPart.getFileName()))) {
                    // Expect the attachment to be HTML content. Therefore apply filter...
                    if (isEmpty(mailPart.getFileName())) {
                        mailPart.setFileName(MailMessageParser.generateFilename(sequenceId, mailPart.getContentType().getBaseType()));
                    }
                    ContentType contentType = mailPart.getContentType();
                    String cs = contentType.containsCharsetParameter() ? contentType.getCharsetParameter() : MailProperties.getInstance().getDefaultMimeCharset();

                    // Read HTML content
                    final byte[] bytes;
                    {
                        String htmlContent = MessageUtility.readMailPart(mailPart, cs);
                        if (htmlContent.length() > HtmlServices.htmlThreshold()) {
                            // HTML cannot be sanitized as it exceeds the threshold for HTML parsing
                            OXException oxe = AjaxExceptionCodes.HTML_TOO_BIG.create();
                            Locale locale = req.getSession().getUser().getLocale();
                            htmlContent = SessionServlet.getErrorPage(200, oxe.getDisplayMessage(locale), "");
                        } else {
                            HtmlService htmlService = ServerServiceRegistry.getInstance().getService(HtmlService.class);
                            htmlContent = sanitizeHtml(htmlContent, htmlService);
                        }

                        // Get its bytes
                        bytes = htmlContent.getBytes(Charsets.forName(cs));
                    }

                    // Proceed
                    contentType.setCharsetParameter(cs);
                    size = bytes.length;
                    isClosure = new IFileHolder.InputStreamClosure() {

                        @Override
                        public InputStream newStream() throws OXException, IOException {
                            return Streams.newByteArrayInputStream(bytes);
                        }
                    };
                    if (null != sink) {
                        Streams.close(sink);
                        sink = null;
                    }
                } else {
                    if (isEmpty(mailPart.getFileName())) {
                        mailPart.setFileName(MailMessageParser.generateFilename(sequenceId, mailPart.getContentType().getBaseType()));
                    }
                    boolean exactLength = AJAXRequestDataTools.parseBoolParameter(req.getParameter("exact_length")) || clientRequestsRange(req);
                    if (exactLength) {
                        if (null == sink) {
                            sink = new ThresholdFileHolder();
                            InputStream in = Streams.getNonEmpty(mailPart.getInputStream());
                            sink.write(null == in ? Streams.EMPTY_INPUT_STREAM : in);
                            size = sink.getLength();
                        }
                    } else {
                        if (null == isClosure) {
                            isClosure = new ReconnectingInputStreamClosure(mailPart, folderPath, uid, sequenceId, false, req.getSession());
                        }
                    }
                }
            } else {
                if (unseen) {
                    MailMessage mail = mailInterface.getMessage(folderPath, uid);
                    if (null == mail) {
                        throw MailExceptionCode.MAIL_NOT_FOUND.create(uid, folderPath);
                    }
                    markUnseen = Boolean.valueOf(mail.containsPrevSeen() && !mail.isPrevSeen());
                }

                mailPart = mailInterface.getMessageImage(folderPath, uid, imageContentId);
                if (mailPart == null) {
                    throw MailExceptionCode.NO_ATTACHMENT_FOUND.create(sequenceId);
                }

                boolean exactLength = AJAXRequestDataTools.parseBoolParameter(req.getParameter("exact_length")) || clientRequestsRange(req);
                if (exactLength) {
                    sink = new ThresholdFileHolder();
                    InputStream in = Streams.getNonEmpty(mailPart.getInputStream());
                    sink.write(null == in ? Streams.EMPTY_INPUT_STREAM : in);
                    size = sink.getLength();
                } else {
                    isClosure = new ReconnectingInputStreamClosure(mailPart, folderPath, uid, imageContentId, true, req.getSession());
                }
            }

            // Check for image data
            AJAXRequestData requestData = req.getRequest();
            boolean isPreviewImage = "preview_image".equals(requestData.getFormat());
            String baseType = mailPart.getContentType().getBaseType();
            String filename = getFileName(fileNameFromRequest, mailPart.getFileName(), baseType);

            // Read from stream
            IFileHolder fileHolder;
            if (saveToDisk) {
                if (null == sink) {
                    @SuppressWarnings("resource") FileHolder tmp = new FileHolder(isClosure, size, MimeType2ExtMap.getContentType(filename), filename);
                    tmp.setDelivery("download");
                    fileHolder = tmp;
                } else {
                    sink.setContentType(baseType);
                    sink.setName(filename);
                    sink.setDelivery("download");
                    fileHolder = sink;
                }
                req.getRequest().putParameter(PARAMETER_DELIVERY, "download");
            } else {
                if (null == sink) {
                    fileHolder = new FileHolder(isClosure, size, baseType, filename);
                } else {
                    sink.setContentType(baseType);
                    sink.setName(filename);
                    fileHolder = sink;
                }
            }
            AJAXRequestResult result = new AJAXRequestResult(fileHolder, "file");

            if (null != markUnseen && markUnseen.booleanValue()) {
                fileHolder.addPostProcessingTask(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            // Get mail interface
                            MailServletInterface mailInterface = getMailInterface(req);
                            mailInterface.updateMessageFlags(folderPath, new String[] { uid }, MailMessage.FLAG_SEEN, false);
                        } catch (Exception e) {
                            Logger logger = org.slf4j.LoggerFactory.getLogger(GetAttachmentAction.class);
                            logger.warn("Failed to unset \\Seen flag for message {} in folder {}", uid, folderPath, e);
                        }
                    }
                });
            }

            // Set format and disallow resource caching
            requestData.putParameter("cache", "false");
            if (!isPreviewImage) {
                requestData.setFormat("file");
            }

            // Set ETag
            setETag(getHash(folderPath, uid, imageContentId == null ? sequenceId : imageContentId), EXPIRES_MILLIS_YEAR, result);
            result.setHeader("Last-Modified", lastModified);

            // Return result
            return result;
        } catch (IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            }
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private boolean clientRequestsRange(MailRequest req) {
        return Tools.hasRangeHeader(req.getRequest().optHttpServletRequest());
    }

    private boolean fileNameIndicatesHtml(String fileName) {
        String mimeTypeByFileName = MimeType2ExtMap.getContentType(fileName, null);
        if (null == mimeTypeByFileName) {
            return false;
        }

        String lc = Strings.asciiLowerCase(mimeTypeByFileName);
        return lc.startsWith("text/htm") || lc.startsWith("text/xhtm");
    }

    private boolean fileNameAbsentOrIndicatesHtml(String fileName) {
        if (null == fileName) {
            return true;
        }

        String mimeTypeByFileName = MimeType2ExtMap.getContentType(fileName, null);
        if (null == mimeTypeByFileName) {
            return true;
        }

        String lc = Strings.asciiLowerCase(mimeTypeByFileName);
        return lc.startsWith("text/htm") || lc.startsWith("text/xhtm");
    }

    private String getFileName(String fileNameFromRequest, String mailPartFileName, String baseType) {
        if (!isEmpty(fileNameFromRequest)) {
            return AJAXUtility.encodeUrl(fileNameFromRequest, true);
        }
        if (!isEmpty(mailPartFileName)) {
            return mailPartFileName;
        }
        String fileExtension = isEmpty(baseType) ? "dat" : MimeType2ExtMap.getFileExtension(baseType);
        return new StringBuilder("file.").append(fileExtension).toString();
    }

    private AJAXRequestResult performPUT(final MailRequest req, final JSONObject jsonFileObject) throws OXException {
        try {
            ServerSession session = req.getSession();

            // Read parameters
            String folderPath = req.checkParameter(PARAMETER_FOLDERID);
            String uid = req.checkParameter(PARAMETER_ID);
            String sequenceId = req.checkParameter(PARAMETER_MAILATTCHMENT);
            String destFolderIdentifier = req.checkParameter(Mail.PARAMETER_DESTINATION_FOLDER);

            // Get mail interface
            MailServletInterface mailInterface = getMailInterface(req);

            // Get attachment storage
            MailAttachmentStorage attachmentStorage = DefaultMailAttachmentStorageRegistry.getInstance().getMailAttachmentStorage();

            if (!session.getUserPermissionBits().hasInfostore()) {
                throw MailExceptionCode.NO_MAIL_ACCESS.create();
            }

            // Get mail part
            MailPart mailPart = mailInterface.getMessageAttachment(folderPath, uid, sequenceId, false);
            if (mailPart == null) {
                throw MailExceptionCode.NO_ATTACHMENT_FOUND.create(sequenceId);
            }

            // Destination folder
            String destFolderID = destFolderIdentifier;

            // Parse file from JSON data
            FileMetadataParserService parser = ServerServiceRegistry.getInstance().getService(FileMetadataParserService.class, true);
            File parsedFile = parser.parse(jsonFileObject);
            List<Field> fields = parser.getFields(jsonFileObject);
            Set<Field> set = EnumSet.copyOf(fields);

            // Apply to mail part
            String mimeType = mailPart.getContentType().getBaseType();
            String fileName = mailPart.getFileName();
            if (isEmpty(fileName)) {
                fileName = "part_" + sequenceId + ".dat";
            } else {
                String contentTypeByFileName = MimeType2ExtMap.getContentType(fileName, null);
                if (null != contentTypeByFileName && !equalPrimaryTypes(mimeType, contentTypeByFileName)) {
                    mimeType = contentTypeByFileName;
                    mailPart.getContentType().setBaseType(mimeType);
                }
            }

            // Set file name
            if (set.contains(Field.FILENAME) && !isEmpty(parsedFile.getFileName())) {
                String givenFileName = parsedFile.getFileName();
                givenFileName = givenFileName.replaceAll(Pattern.quote("/"), "_");
                mailPart.setFileName(givenFileName);
            } else {
                fileName = fileName.replaceAll(Pattern.quote("/"), "_");
                mailPart.setFileName(fileName);
            }

            /*
             * Since file's size given from mail server is just an estimation and therefore does not exactly match the file's size a
             * future file access via WebDAV can fail because of the size mismatch. Thus set the file size to 0 to make the storage
             * measure the size.
             */
            mailPart.setSize(0);

            // Store properties
            Map<String, Object> storeProps = new HashMap<String, Object>(4);
            storeProps.put("folder", destFolderID);

            {
                String description = parsedFile.getDescription();
                if (null != description) {
                    storeProps.put("description", description);
                }
            }

            // Store
            String id = attachmentStorage.storeAttachment(mailPart, StoreOperation.SIMPLE_STORE, storeProps, session);
            MailPart updatedMailPart = attachmentStorage.getAttachment(id, session);

            // File name can differ from expected filename
            String newFilename = updatedMailPart.getFileName();

            // JSON response object
            JSONObject jFileData = new JSONObject(8);
            jFileData.put("mailFolder", folderPath);
            jFileData.put("mailUID", uid);
            jFileData.put("id", id);
            jFileData.put("folder_id", destFolderID);
            jFileData.put("filename", newFilename);
            return new AJAXRequestResult(jFileData, "json");
        } catch (JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    static String sanitizeHtml(String htmlContent, HtmlService htmlService) {
        if (htmlService == null) {
            LOG.warn("HtmlService absent. Unable to sanitize content. Return unsanitized content.");
            return htmlContent;
        }
        return htmlService.sanitize(htmlContent, null, false, null, null);
    }

    private String getHash(String folderPath, String uid, String sequenceId) {
        return HashUtility.getHash(new StringBuilder(32).append(folderPath).append('/').append(uid).append('/').append(sequenceId).toString(), "md5", "hex");
    }

    // -----------------------------------------------------------------------------------------------------------------------------------

    private static final class ReconnectingInputStreamClosure implements IFileHolder.InputStreamClosure {

        private final ServerSession session;
        private final String id;
        private final String uid;
        private final MailPart mailPart;
        private final String folderPath;
        private final boolean image;
        private volatile ThresholdFileHolder tfh;

        ReconnectingInputStreamClosure(MailPart mailPart, String folderPath, String uid, String id, boolean image, ServerSession session) {
            super();
            this.session = session;
            this.id = id;
            this.uid = uid;
            this.mailPart = mailPart;
            this.folderPath = folderPath;
            this.image = image;
        }

        @Override
        public InputStream newStream() throws OXException, IOException {
            {
                final ThresholdFileHolder tfh = this.tfh;
                if (null != tfh) {
                    return tfh.getStream();
                }
            }
            PushbackInputStream in = null;
            boolean error = true;
            try {
                // Try to read first byte and push back immediately
                in = new PushbackInputStream(mailPart.getInputStream());
                int read = in.read();
                if (read < 0) {
                    return Streams.EMPTY_INPUT_STREAM;
                }
                in.unread(read);
                error = false;
                return in;
            } catch (final com.sun.mail.util.FolderClosedIOException e) {
                // Need to reconnect
                return getReconnectedStream();
            } finally {
                if (error) {
                    Streams.close(in);
                }
            }
        }

        private synchronized InputStream getReconnectedStream() throws OXException {
            ThresholdFileHolder tfh = this.tfh;
            if (null != tfh) {
                // Already initialized
                return tfh.getStream();
            }

            FullnameArgument fa = MailFolderUtility.prepareMailFolderParam(folderPath);
            MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> ma = null;
            ThresholdFileHolder newTfh = null;
            boolean error = true;
            try {
                ma = MailAccess.getInstance(session, fa.getAccountId());
                ma.connect(false);
                newTfh = new ThresholdFileHolder();
                if (image) {
                    newTfh.write(ma.getMessageStorage().getImageAttachment(fa.getFullName(), uid, id).getInputStream());
                } else {
                    newTfh.write(ma.getMessageStorage().getAttachment(fa.getFullName(), uid, id).getInputStream());
                }
                this.tfh = newTfh;
                error = false;
                return newTfh.getStream();
            } finally {
                if (null != ma) {
                    ma.close(true);
                }
                if (error) {
                    Streams.close(newTfh);
                }
            }
        }

    } // End of class ReconnectingInputStreamClosure

}
