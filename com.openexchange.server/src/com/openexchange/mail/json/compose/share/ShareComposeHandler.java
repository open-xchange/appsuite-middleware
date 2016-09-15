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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.mail.json.compose.share;

import static com.openexchange.java.Autoboxing.I;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.idn.IDNA;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.ajax.requesthandler.converters.cover.Mp3CoverExtractor;
import com.openexchange.config.ConfigurationService;
import com.openexchange.conversion.DataProperties;
import com.openexchange.conversion.SimpleData;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.LdapExceptionCode;
import com.openexchange.groupware.ldap.User;
import com.openexchange.imagetransformation.ImageTransformationService;
import com.openexchange.imagetransformation.ImageTransformations;
import com.openexchange.imagetransformation.ScaleType;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.dataobjects.compose.DelegatingComposedMailMessage;
import com.openexchange.mail.json.compose.AbstractComposeHandler;
import com.openexchange.mail.json.compose.ComposeDraftResult;
import com.openexchange.mail.json.compose.ComposeRequest;
import com.openexchange.mail.json.compose.ComposeTransportResult;
import com.openexchange.mail.json.compose.DefaultComposeDraftResult;
import com.openexchange.mail.json.compose.DefaultComposeTransportResult;
import com.openexchange.mail.json.compose.Utilities;
import com.openexchange.mail.json.compose.share.internal.AttachmentStorageRegistry;
import com.openexchange.mail.json.compose.share.internal.EnabledCheckerRegistry;
import com.openexchange.mail.json.compose.share.internal.MessageGeneratorRegistry;
import com.openexchange.mail.json.compose.share.internal.ShareComposeLinkGenerator;
import com.openexchange.mail.json.compose.share.spi.AttachmentStorage;
import com.openexchange.mail.json.compose.share.spi.EnabledChecker;
import com.openexchange.mail.json.compose.share.spi.MessageGenerator;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.MimeMailExceptionCode;
import com.openexchange.mail.mime.dataobjects.MimeMailPart;
import com.openexchange.mail.mime.datasource.FileHolderDataSource;
import com.openexchange.preview.PreviewDocument;
import com.openexchange.preview.PreviewOutput;
import com.openexchange.preview.PreviewService;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.ShareLink;
import com.openexchange.share.ShareService;
import com.openexchange.share.ShareTarget;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;

/**
 * {@link ShareComposeHandler}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class ShareComposeHandler extends AbstractComposeHandler<ShareTransportComposeContext, ShareDraftComposeContext> {

    private static final Logger LOG = LoggerFactory.getLogger(ShareComposeHandler.class);

    // Default thumbnails
    private static final String THUMBNAIL_ARCHIVE = "font-awesome-file-archive.png";
    private static final String THUMBNAIL_AUDIO = "font-awesome-file-audio.png";
    private static final String THUMBNAIL_EXCEL = "font-awesome-file-excel.png";
    private static final String THUMBNAIL_IMAGE = "font-awesome-file-image.png";
    private static final String THUMBNAIL_DEFAULT = "font-awesome-file-default.png";
    private static final String THUMBNAIL_PDF = "font-awesome-file-pdf.png";
    private static final String THUMBNAIL_POWERPOINT = "font-awesome-file-powerpoint.png";
    private static final String THUMBNAIL_VIDEO = "font-awesome-file-video.png";
    private static final String THUMBNAIL_WORD = "font-awesome-file-word.png";

    /**
     * Initializes a new {@link ShareComposeHandler}.
     */
    public ShareComposeHandler() {
        super();
    }

    /**
     * Gets the optional share options from JSON message representation.
     *
     * @param composeRequest The compose request providing JSON message representation
     * @return The share options or <code>null</code>
     */
    private JSONObject optShareAttachmentOptions(ComposeRequest composeRequest) {
        return composeRequest.getJsonMail().optJSONObject("share_attachments");
    }

    /**
     * Checks whether specified compose request signals to compose a share message.
     *
     * @param composeRequest The compose request
     * @return <code>true</code> to create a share message; otherwise <code>false</code>
     */
    private boolean isCreateShare(ComposeRequest composeRequest) {
        JSONObject jShareAttachmentOptions = optShareAttachmentOptions(composeRequest);
        return null != jShareAttachmentOptions && jShareAttachmentOptions.optBoolean("enable", false);
    }

    /**
     * Checks whether created files are supposed to expire as well (provided that an expiration date is set)
     *
     * @param request The compose request
     * @return <code>true</code> for auto-expiration; otherwise <code>false</code>
     */
    private boolean isAutoDelete(ComposeRequest request) {
        JSONObject jShareAttachmentOptions = optShareAttachmentOptions(request);
        return null != jShareAttachmentOptions && jShareAttachmentOptions.optBoolean("autodelete", false);
    }

    /**
     * Gets the password from given compose request.
     *
     * @param request The compose request
     * @return The password or <code>null</code>
     */
    private String getPassword(ComposeRequest request) {
        JSONObject jShareAttachmentOptions = optShareAttachmentOptions(request);
        return null == jShareAttachmentOptions ? null : jShareAttachmentOptions.optString("password", null);
    }

    /**
     * Gets the expiration date from given compose request.
     *
     * @param request The compose request
     * @return The expiration date or <code>null</code>
     * @throws OXException If value is invalid (NaN)
     */
    protected Date getExpirationDate(ComposeRequest request) throws OXException {
        JSONObject jShareAttachmentOptions = optShareAttachmentOptions(request);
        if (null == jShareAttachmentOptions) {
            return null;
        }

        long millis = jShareAttachmentOptions.optLong("expiry_date");
        if (millis > 0) {
            int offset = TimeZoneUtils.getTimeZone(request.getSession().getUser().getTimeZone()).getOffset(millis);
            return new Date(millis - offset);
        }

        String sDate = jShareAttachmentOptions.optString("expiry_date");
        if (Strings.isEmpty(sDate)) {
            return null;
        }

        try {
            return com.openexchange.java.ISO8601Utils.parse(sDate);
        } catch (IllegalArgumentException iso8601ParsingFailed) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(iso8601ParsingFailed, "expiry_date", sDate);
        }
    }

    /**
     * Checks if share compose is enabled and session-associated user holds sufficient capabilities.
     *
     * @param session The session
     * @return <code>true</code> if enabled; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    public boolean isEnabled(Session session) throws OXException {
        if (false == Utilities.getBoolFromProperty("com.openexchange.mail.compose.share.enabled", true, session)) {
            // Not enabled as per configuration
            return false;
        }

        // Check capabilities, too
        EnabledCheckerRegistry checkerRegistry = ServerServiceRegistry.getServize(EnabledCheckerRegistry.class);
        EnabledChecker checker = checkerRegistry.getEnabledCheckerFor(session);
        return checker.isEnabled(session);
    }

    @Override
    public String getId() {
        return "share";
    }

    @Override
    public boolean applicableFor(ComposeRequest composeRequest) throws OXException {
        if (false == isCreateShare(composeRequest)) {
            return false;
        }

        ServerSession session = composeRequest.getSession();
        boolean applicable = isEnabled(session);
        if (!applicable) {
            throw MailExceptionCode.SHARING_NOT_POSSIBLE.create(I(session.getUserId()), I(session.getContextId()));
        }
        return applicable;
    }

    @Override
    protected ShareDraftComposeContext createDraftComposeContext(ComposeRequest composeRequest) throws OXException {
        return new ShareDraftComposeContext(composeRequest);
    }

    @Override
    protected ShareTransportComposeContext createTransportComposeContext(ComposeRequest composeRequest) throws OXException {
        return new ShareTransportComposeContext(composeRequest);
    }

    @Override
    protected ComposeDraftResult doCreateDraftResult(ComposeRequest composeRequest, ShareDraftComposeContext context) throws OXException {
        ComposedMailMessage composeMessage = createRegularComposeMessage(context);
        return new DefaultComposeDraftResult(composeMessage);
    }

    @Override
    protected ComposeTransportResult doCreateTransportResult(ComposeRequest composeRequest, ShareTransportComposeContext context) throws OXException {
        // Check if context collected any attachment at all
        if (false == context.hasAnyPart()) {
            // No attachments
            ComposedMailMessage composeMessage = createRegularComposeMessage(context);
            DelegatingComposedMailMessage transportMessage = new DelegatingComposedMailMessage(composeMessage);
            transportMessage.setAppendToSentFolder(false);
            return new DefaultComposeTransportResult(Collections.<ComposedMailMessage> singletonList(transportMessage), composeMessage);
        }

        // Get the basic source message
        ServerSession session = composeRequest.getSession();
        ComposedMailMessage source = context.getSourceMessage();

        // Collect recipients
        Set<Recipient> recipients;
        {
            Set<InternetAddress> addresses = new HashSet<>();
            addresses.addAll(Arrays.asList(source.getTo()));
            addresses.addAll(Arrays.asList(source.getCc()));
            addresses.addAll(Arrays.asList(source.getBcc()));

            UserService userService = ServerServiceRegistry.getServize(UserService.class);
            if (null == userService) {
                throw ServiceExceptionCode.absentService(UserService.class);
            }
            Context ctx = composeRequest.getContext();

            recipients = new LinkedHashSet<>(addresses.size());
            for (InternetAddress address : addresses) {
                User user = resolveToUser(address, ctx, userService);
                String personal = address.getPersonal();
                String sAddress = address.getAddress();
                recipients.add(null == user ? Recipient.createExternalRecipient(personal, sAddress) : Recipient.createInternalRecipient(personal, sAddress, user));
            }
        }

        // Optional password
        String password = getPassword(composeRequest);

        // Optional expiration date
        Date expirationDate = getExpirationDate(composeRequest);
        if (null == expirationDate && Utilities.getBoolFromProperty("com.openexchange.mail.compose.share.requiredExpiration", false, session)) {
            throw MailExceptionCode.EXPIRATION_DATE_MISSING.create(I(session.getUserId()), I(session.getContextId()));
        }

        // Optional auto-expiration of folder/files
        boolean autoDelete;
        if (null == expirationDate) {
            autoDelete = false;
        } else {
            autoDelete = Utilities.getBoolFromProperty("com.openexchange.mail.compose.share.forceAutoDelete", false, session) || isAutoDelete(composeRequest);
        }

        // Determine attachment storage to use
        AttachmentStorageRegistry storageRegistry = ServerServiceRegistry.getServize(AttachmentStorageRegistry.class);
        if (null == storageRegistry) {
            throw ServiceExceptionCode.absentService(AttachmentStorageRegistry.class);
        }
        AttachmentStorage attachmentStorage = storageRegistry.getAttachmentStorageFor(composeRequest);

        // Some state variables
        StoredAttachmentsControl attachmentsControl = null;
        boolean rollback = true;
        Map<String, ThresholdFileHolder> previewImages = null;
        try {
            // Store attachments associated with compose context
            attachmentsControl = attachmentStorage.storeAttachments(source, password, expirationDate, autoDelete, context);

            // The share target for an anonymous user
            ShareTarget folderTarget = attachmentsControl.getFolderTarget();
            ShareService shareService = ServerServiceRegistry.getServize(ShareService.class);
            if (null == shareService) {
                throw ServiceExceptionCode.absentService(ShareService.class);
            }
            ShareLink folderLink = shareService.getLink(session, folderTarget);

            // Create share compose reference
            ShareReference shareReference;
            {
                String shareToken = folderLink.getGuest().getBaseToken();
                shareReference = new ShareReference.Builder(session.getUserId(), session.getContextId()).expiration(expirationDate).password(password).folder(attachmentsControl.getFolder()).items(attachmentsControl.getAttachments()).shareToken(shareToken).build();
            }

            // Create share link(s) for recipients
            Map<ShareComposeLink, Set<Recipient>> links = new LinkedHashMap<>(recipients.size());
            {
                GuestInfo guest = folderLink.getGuest();
                for (Recipient recipient : recipients) {
                    ShareComposeLink linkedAttachment = ShareComposeLinkGenerator.getInstance().createShareLink(recipient, folderTarget, guest, null, composeRequest);
                    Set<Recipient> associatedRecipients = links.get(linkedAttachment);
                    if (null == associatedRecipients) {
                        associatedRecipients = new LinkedHashSet<>(recipients.size());
                        links.put(linkedAttachment, associatedRecipients);
                    }
                    associatedRecipients.add(recipient);
                }
            }

            // Create personal share link
            ShareComposeLink personalLink;
            {
                personalLink = ShareComposeLinkGenerator.getInstance().createPersonalShareLink(folderTarget, null, composeRequest);
            }

            // Generate preview images
            previewImages = generatePreviewImages(session, shareReference);
            Map<String, String> cidMapping = getCidMapping(previewImages);
            List<MailPart> imageParts = createPreviewPart(cidMapping, previewImages);

            // Generate messages from links
            List<ComposedMailMessage> transportMessages = new LinkedList<>();
            ComposedMailMessage sentMessage;
            {
                MessageGeneratorRegistry generatorRegistry = ServerServiceRegistry.getServize(MessageGeneratorRegistry.class);
                if (null == generatorRegistry) {
                    throw ServiceExceptionCode.absentService(MessageGeneratorRegistry.class);
                }
                MessageGenerator messageGenerator = generatorRegistry.getMessageGeneratorFor(composeRequest);
                for (Map.Entry<ShareComposeLink, Set<Recipient>> entry : links.entrySet()) {
                    ShareComposeMessageInfo messageInfo = new ShareComposeMessageInfo(entry.getKey(), new ArrayList<>(entry.getValue()), password, expirationDate, source, context, composeRequest);
                    List<ComposedMailMessage> generatedTransportMessages = messageGenerator.generateTransportMessagesFor(messageInfo, shareReference, cidMapping, shareReference.getItems().size() - 6);
                    for (ComposedMailMessage generatedTransportMessage : generatedTransportMessages) {
                        generatedTransportMessage.setAppendToSentFolder(false);
                        for (MailPart imagePart : imageParts) {
                            generatedTransportMessage.addEnclosedPart(imagePart);
                        }
                        transportMessages.add(generatedTransportMessage);
                    }
                }

                String sendAddr = session.getUserSettingMail().getSendAddr();
                User user = composeRequest.getUser();
                Recipient userRecipient = Recipient.createInternalRecipient(user.getDisplayName(), sendAddr, user);
                sentMessage = messageGenerator.generateSentMessageFor(new ShareComposeMessageInfo(personalLink, Collections.singletonList(userRecipient), password, expirationDate, source, context, composeRequest), shareReference, cidMapping, shareReference.getItems().size() - 6);
                for (MailPart imagePart : imageParts) {
                    sentMessage.addEnclosedPart(imagePart);
                }
            }

            // Commit attachment storage
            attachmentsControl.commit();
            rollback = false;

            return new DefaultComposeTransportResult(transportMessages, sentMessage);
        } finally {
            if (null != attachmentsControl) {
                if (rollback) {
                    attachmentsControl.rollback();
                }
                attachmentsControl.finish();
            }
            if (null != previewImages) {
                if (rollback) {
                    for (ThresholdFileHolder tfh : previewImages.values()) {
                        tfh.close();
                    }
                }
            }
        }
    }

    private User resolveToUser(InternetAddress address, Context ctx, UserService userService) throws OXException {
        User user;
        try {
            user = userService.searchUser(IDNA.toIDN(address.getAddress()), ctx);
        } catch (final OXException e) {
            /*
             * Unfortunately UserService.searchUser() throws an exception if no user could be found matching given email address.
             * Therefore check for this special error code and throw an exception if it is not equal.
             */
            if (!LdapExceptionCode.NO_USER_BY_MAIL.equals(e)) {
                throw e;
            }
            user = null;
        }
        return user;
    }

    private Map<String, ThresholdFileHolder> generatePreviewImages(Session session, ShareReference reference) throws OXException {
        List<Item> items = reference.getItems();
        PreviewService previewService = ServerServiceRegistry.getInstance().getService(PreviewService.class);
        ImageTransformationService transformationService = ServerServiceRegistry.getInstance().getService(ImageTransformationService.class);
        IDBasedFileAccessFactory fileAccessFactory = ServerServiceRegistry.getInstance().getService(IDBasedFileAccessFactory.class);
        ThreadPoolService threadPoolService = ServerServiceRegistry.getInstance().getService(ThreadPoolService.class);
        ConfigurationService configurationService = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
        boolean documentPreviewEnabled = false;
        int timeout = 500;
        String templatePath = null;
        if (null != configurationService) {
            documentPreviewEnabled = configurationService.getBoolProperty("com.openexchange.mail.compose.share.documentPreviewEnabled", false);
            timeout = configurationService.getIntProperty("com.openexchange.mail.compose.share.preview.timeout", 500);
            templatePath = configurationService.getProperty("com.openexchange.templating.path");
        }
        if (null == items || items.isEmpty() || null == previewService || null == transformationService || null == fileAccessFactory || null == threadPoolService) {
            return java.util.Collections.emptyMap();
        }
        Map<String, ThresholdFileHolder> previews = new HashMap<>(6);
        Map<String, Future<ThresholdFileHolder>> previewFutures = new HashMap<>(6);
        Map<String, String> mimeTypes = new HashMap<>(6);
        IDBasedFileAccess access = fileAccessFactory.createAccess(session);
        for (int k = Math.min(items.size(), 6), i = 0; k-- > 0; i++) {
            String id = items.get(i).getId();
            InputStream document = access.getDocument(id, FileStorageFileAccess.CURRENT_VERSION);
            String mimeType = access.getFileMetadata(id, FileStorageFileAccess.CURRENT_VERSION).getFileMIMEType();
            PreviewTask previewTask = new PreviewTask(id, document, mimeType, transformationService, previewService, documentPreviewEnabled, session);
            Future<ThresholdFileHolder> future = threadPoolService.submit(previewTask);
            previewFutures.put(id, future);
            mimeTypes.put(id, mimeType);
        }
        for (Entry<String, Future<ThresholdFileHolder>> entry : previewFutures.entrySet()) {
            String id = entry.getKey();
            try {
                ThresholdFileHolder encodedThumbnail = entry.getValue().get(timeout, TimeUnit.MILLISECONDS);
                previews.put(id, encodedThumbnail);
            } catch (InterruptedException | TimeoutException e) {
                LOG.debug(e.getMessage(), e);
            } catch (ExecutionException e) {
                LOG.error(e.getMessage(), e);
            }
            if (null == previews.get(id)) {
                previews.put(id, getDefaultThumbnail(mimeTypes.get(id), templatePath));
            }
        }
        return previews;
    }

    private ThresholdFileHolder getDefaultThumbnail(String mimeType, String templatePath) throws OXException {
        String thumbnailName = null;
        switch (mimeType) {
            case "application/zip":
            case "application/x-bzip2":
            case "application/x-gzip":
                thumbnailName = THUMBNAIL_ARCHIVE;
                break;
            case "audio/mpeg":
            case "audio/x-wav":
                thumbnailName = THUMBNAIL_AUDIO;
                break;
            case "application/excel":
            case "application/vnd.oasis.opendocument.spreadsheet":
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
                thumbnailName = THUMBNAIL_EXCEL;
                break;
            case "image/png":
            case "image/jpg":
            case "image/jpeg":
            case "image/gif":
            case "image/svg":
                thumbnailName = THUMBNAIL_IMAGE;
                break;
            case "application/pdf":
                thumbnailName = THUMBNAIL_PDF;
                break;
            case "application/powerpoint":
            case "application/vnd.oasis.opendocument.presentation":
            case "application/vnd.openxmlformats-officedocument.presentationml.presentation":
                thumbnailName = THUMBNAIL_POWERPOINT;
                break;
            case "video/mpeg":
            case "video/mp4":
            case "video/avi":
            case "video/3gpp":
            case "video/quicktime":
            case "video/msvideo":
            case "video/x-ms-wmv":
                thumbnailName = THUMBNAIL_VIDEO;
                break;
            case "application/msword":
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
            case "application/vnd.oasis.opendocument.text":
                thumbnailName = THUMBNAIL_WORD;
                break;
            default: thumbnailName = THUMBNAIL_DEFAULT;
        }
        String thumbnail = templatePath + java.io.File.separator + thumbnailName;
        InputStream in = null;
        ThresholdFileHolder preview = null;
        try {
            in = new FileInputStream(thumbnail);
            preview = new ThresholdFileHolder();
            preview.write(in);
        } catch (IOException e) {
            throw MimeMailExceptionCode.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(in);
        }
        return preview;
    }

    private Map<String, String> getCidMapping(Map<String, ThresholdFileHolder> previewImages) {
        if (null == previewImages || previewImages.size() == 0) {
            return Collections.emptyMap();
        }

        Map<String, String> cidMapping = new HashMap<>();
        for (String id : previewImages.keySet()) {
            cidMapping.put(id, UUID.randomUUID().toString());
        }
        return cidMapping;
    }

    private List<MailPart> createPreviewPart(Map<String, String> cidMapping, Map<String, ThresholdFileHolder> previews) throws OXException {
        try {
            List<MailPart> parts = new ArrayList<>(cidMapping.size());
            for (String id : cidMapping.keySet()) {
                String contentId = cidMapping.get(id);
                MimeBodyPart imagePart = new MimeBodyPart();
                imagePart.setDisposition("inline");
                imagePart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, "image/jpeg");
                imagePart.setContentID("<" + contentId + ">");
                imagePart.setHeader("X-Attachment-Id", contentId);
                imagePart.setDataHandler(new DataHandler(new FileHolderDataSource(previews.get(id), "image/jpeg")));
                MimeMailPart mimeMailPart = new MimeMailPart(imagePart);
                mimeMailPart.setContentDisposition("inline");
                mimeMailPart.setContentId("<" + contentId + ">");
                mimeMailPart.setContentType("image/jpeg");
                mimeMailPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, "image/jpeg");
                mimeMailPart.setHeader(MessageHeaders.HDR_CONTENT_ID, "<" + contentId + ">");
                mimeMailPart.setHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, "inline");
                mimeMailPart.setHeader("X-Attachment-Id", contentId);
                parts.add(mimeMailPart);
            }
            return parts;
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    private static class PreviewTask extends AbstractTask<ThresholdFileHolder> {

        private final String id;
        private final InputStream document;
        private final String mimeType;
        private final ImageTransformationService transformationService;
        private final PreviewService previewService;
        private final boolean documentPreviewEnabled;
        private final Session session;

        public PreviewTask(String id, InputStream document, String mimeType, ImageTransformationService transformationService, PreviewService previewService, boolean documentPreviewEnabled, Session session) {
            super();
            this.id = id;
            this.document = document;
            this.mimeType = mimeType;
            this.transformationService = transformationService;
            this.previewService = previewService;
            this.documentPreviewEnabled = documentPreviewEnabled;
            this.session = session;
        }

        @Override
        public ThresholdFileHolder call() throws Exception {
            ThresholdFileHolder encodedThumbnail = null;
            try {

                // Document is an image
                if (!Strings.isEmpty(mimeType) && mimeType.toLowerCase().startsWith("image")) {
                    encodedThumbnail = transformImage(document, mimeType);
                }

                // Document is an audio file
                else if (!Strings.isEmpty(mimeType) && mimeType.toLowerCase().startsWith("audio/mpeg")) {
                    if (Mp3CoverExtractor.isSupported(mimeType)) {
                        IFileHolder mp3Cover = null;
                        try {
                            mp3Cover = getCoverImage(document);
                            encodedThumbnail = transformImage(mp3Cover.getStream(), "image/jpeg");
                        } finally {
                            if (null != mp3Cover) {
                                mp3Cover.close();
                            }
                        }
                    }
                }

                // Document is something else, try to get preview image with document converter
                else {
                    if (documentPreviewEnabled) {
                        PreviewDocument preview = getDocumentPreview(document, mimeType, session);
                        InputStream in = null;
                        try {
                            in = preview.getThumbnail();
                            encodedThumbnail = new ThresholdFileHolder();
                            encodedThumbnail.write(in);
                        } finally {
                            Streams.close(in);
                        }
                    }
                }
            } catch (OXException e) {
                if (!"PREVIEW".equals(e.getPrefix())) {
                    throw e;
                }
            } finally {
                Streams.close(document);
            }
            return encodedThumbnail;
        }

        private ThresholdFileHolder transformImage(InputStream image, String mimeType) throws OXException {
            try {
                ImageTransformations transformed = transformationService.transfom(image).scale(200, 150, ScaleType.COVER, true).compress();
                ThresholdFileHolder transformedImage = new ThresholdFileHolder();
                transformedImage.write(transformed.getTransformedImage(mimeType).getImageStream());
                return transformedImage;
            } catch (IOException e) {
                throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
            }
        }

        private IFileHolder getCoverImage(InputStream audioFile) throws OXException {
            Mp3CoverExtractor mp3CoverExtractor = new Mp3CoverExtractor();
            ThresholdFileHolder fileHolder = null;
            try {
                fileHolder = new ThresholdFileHolder();
                fileHolder.write(audioFile);
                fileHolder.setContentType("audio/mpeg");
                fileHolder.setName(id + ".mp3");
                return mp3CoverExtractor.extractCover(fileHolder);
            } finally {
                if (null != fileHolder) {
                    fileHolder.close();
                }
            }
        }

        private PreviewDocument getDocumentPreview(InputStream document, String mimeType, Session session) throws OXException {
            DataProperties dataProperties = new DataProperties(5);
            dataProperties.put("PreviewWidth", "200");
            dataProperties.put("PreviewHeight", "150");
            dataProperties.put("PreviewScaleType", "cover");
            dataProperties.put(DataProperties.PROPERTY_NAME, id);
            dataProperties.put(DataProperties.PROPERTY_CONTENT_TYPE, mimeType);
            SimpleData<InputStream> data = new SimpleData<>(document, dataProperties);
            return previewService.getPreviewFor(data, PreviewOutput.IMAGE, session, 0);
        }

    }

}
