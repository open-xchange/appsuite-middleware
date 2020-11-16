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

package com.openexchange.mail.compose.mailstorage.storage;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.util.UUIDs.getUnformattedString;
import static com.openexchange.mail.text.TextProcessing.performLineFolding;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.idn.IDNA;
import javax.mail.util.ByteArrayDataSource;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableMap;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.crypto.CryptographicServiceAuthenticationFactory;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.exception.OXException;
import com.openexchange.html.HtmlService;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailJSONField;
import com.openexchange.mail.compose.Address;
import com.openexchange.mail.compose.Attachment;
import com.openexchange.mail.compose.Attachment.ContentDisposition;
import com.openexchange.mail.compose.AttachmentComparator;
import com.openexchange.mail.compose.AttachmentDataSource;
import com.openexchange.mail.compose.AttachmentOrigin;
import com.openexchange.mail.compose.AttachmentStorages;
import com.openexchange.mail.compose.ClientToken;
import com.openexchange.mail.compose.CompositionSpaceErrorCode;
import com.openexchange.mail.compose.CompositionSpaces;
import com.openexchange.mail.compose.ContentId;
import com.openexchange.mail.compose.DefaultAttachment;
import com.openexchange.mail.compose.HeaderUtility;
import com.openexchange.mail.compose.Message.ContentType;
import com.openexchange.mail.compose.Message.Priority;
import com.openexchange.mail.compose.MessageDescription;
import com.openexchange.mail.compose.MessageField;
import com.openexchange.mail.compose.Meta;
import com.openexchange.mail.compose.Security;
import com.openexchange.mail.compose.SharedAttachmentReference;
import com.openexchange.mail.compose.SharedAttachmentsInfo;
import com.openexchange.mail.compose.SharedFolderReference;
import com.openexchange.mail.compose.mailstorage.MailStorageCompositionSpaceImageDataSource;
import com.openexchange.mail.compose.mailstorage.SharedAttachmentsUtils;
import com.openexchange.mail.compose.mailstorage.ThresholdFileHolderDataProvider;
import com.openexchange.mail.compose.mailstorage.ThresholdFileHolderFactory;
import com.openexchange.mail.compose.mailstorage.cache.CacheManager;
import com.openexchange.mail.compose.mailstorage.cache.CacheManagerFactory;
import com.openexchange.mail.compose.mailstorage.cache.CacheReference;
import com.openexchange.mail.compose.mailstorage.cache.Result;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.SecuritySettings;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.dataobjects.compose.ContentAwareComposedMailMessage;
import com.openexchange.mail.dataobjects.compose.ReferencedMailPart;
import com.openexchange.mail.dataobjects.compose.TextBodyMailPart;
import com.openexchange.mail.json.compose.ComposeRequest;
import com.openexchange.mail.json.compose.share.AttachmentStorageRegistry;
import com.openexchange.mail.json.compose.share.FileItem;
import com.openexchange.mail.json.compose.share.FileItems;
import com.openexchange.mail.json.compose.share.Item;
import com.openexchange.mail.json.compose.share.StoredAttachments;
import com.openexchange.mail.json.compose.share.spi.AttachmentStorage;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeCleanUp;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.mime.converters.FileBackedMimeMessage;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.mime.dataobjects.MimeRawSource;
import com.openexchange.mail.mime.datasource.MessageDataSource;
import com.openexchange.mail.mime.filler.MimeMessageFiller;
import com.openexchange.mail.mime.processing.MimeProcessingUtility;
import com.openexchange.mail.mime.processing.TextAndContentType;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.parser.MailMessageParser;
import com.openexchange.mail.transport.TransportProvider;
import com.openexchange.mail.transport.TransportProviderRegistry;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link MailMessageProcessor} - Manages the state of a draft message on changes and converts between message and MIME representation.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.5
 */
public class MailMessageProcessor {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailMessageProcessor.class);

    /**
     * Initializes a processor for transporting a draft mail.
     * <p>
     * Draft mail data is buffered into memory or spooled to disk dependent on mail size. All (binary) attachments are loaded.
     *
     * @param compositionSpaceId The composition space identifier
     * @param mailMessage The mail message providing access to draft mail data
     * @param session The session providing user information
     * @param services The service look-up
     * @return Newly initialized processor ready for transporting draft mail
     * @throws OXException If processor cannot be initialized
     * @throws MissingDraftException If {@link MailMessage} throws {@link MailExceptionCode#MAIL_NOT_FOUND} when lazily loading parts of it.
     */
    public static MailMessageProcessor initForTransport(UUID compositionSpaceId, MailMessage mailMessage, Session session, ServiceLookup services) throws OXException, MissingDraftException {
        MailMessageProcessor processor = new MailMessageProcessor(compositionSpaceId, session, services);
        MailMessage clone = processor.initBufferedMimeMessage(mailMessage);
        processor.parse(clone, false); // Since already buffered before
        return processor;
    }

    /**
     * Initializes a processor for writing/modifying a draft mail.
     * <p>
     * Draft mail data is buffered into memory or spooled to disk dependent on mail size. All (binary) attachments are loaded.
     *
     * @param compositionSpaceId The composition space identifier
     * @param mailMessage The mail message providing access to draft mail data
     * @param session The session providing user information
     * @param services The service look-up
     * @return Newly initialized processor ready for modifying draft mail
     * @throws OXException If processor cannot be initialized
     * @throws MissingDraftException If {@link MailMessage} throws {@link MailExceptionCode#MAIL_NOT_FOUND} when lazily loading parts of it.
     */
    public static MailMessageProcessor initForWrite(UUID compositionSpaceId, MailMessage mailMessage, Session session, ServiceLookup services) throws OXException, MissingDraftException {
        MailMessageProcessor processor = new MailMessageProcessor(compositionSpaceId, session, services);
        MailMessage clone = processor.initBufferedMimeMessage(mailMessage);
        processor.parse(clone, false); // Since already buffered before
        return processor;
    }

    /**
     * Initializes a processor for writing/modifying a draft mail.
     * <p>
     * Draft mail data is buffered into memory or spooled to disk dependent on mail size. All (binary) attachments are loaded.
     *
     * @param compositionSpaceId The composition space identifier
     * @param mimeStream The MIME stream providing draft mail data
     * @param session The session providing user information
     * @param services The service look-up
     * @return Newly initialized processor ready for modifying draft mail
     * @throws OXException If processor cannot be initialized
     */
    public static MailMessageProcessor initForWrite(UUID compositionSpaceId, InputStream mimeStream, Session session, ServiceLookup services) throws OXException {
        try {
            MailMessageProcessor processor = new MailMessageProcessor(compositionSpaceId, session, services);
            MailMessage clone = processor.initBufferedMimeMessage(mimeStream);
            processor.parse(clone, false); // Since already buffered before
            return processor;
        } finally {
            Streams.close(mimeStream);
        }
    }


    /**
     * Initializes a processor for writing/modifying a draft mail from an existing cached MIME message.
     *
     * @param compositionSpaceId The composition space identifier
     * @param cacheReference The cache reference
     * @param session The session providing user information
     * @param services The service look-up
     * @return Newly initialized processor ready for modifying draft mail
     * @throws OXException If processor cannot be initialized
     */
    public static MailMessageProcessor initFromFileCache(UUID compositionSpaceId, CacheReference cacheReference, Session session, ServiceLookup services) throws OXException {
        MailMessageProcessor processor = new MailMessageProcessor(compositionSpaceId, session, services);
        MailMessage mailMessage = processor.initMimeMessageFromFileCache(cacheReference);
        processor.parse(mailMessage, false);
        return processor;
    }

    /**
     * Initializes a processor for reading/parsing a draft mail.
     * <p>
     * Draft mail data is not necessarily buffered into memory nor spooled to disk. No (binary) attachments are loaded.
     *
     * @param compositionSpaceId The composition space identifier
     * @param mailMessage The mail message providing access to draft mail data
     * @param session The session providing user information
     * @param services The service look-up
     * @return Newly initialized processor ready for reading draft mail
     * @throws OXException If processor cannot be initialized
     * @throws MissingDraftException If {@link MailMessage} throws {@link MailExceptionCode#MAIL_NOT_FOUND} when lazily loading parts of it.
     */
    public static MailMessageProcessor initReadEnvelope(UUID compositionSpaceId, MailMessage mailMessage, Session session, ServiceLookup services) throws OXException, MissingDraftException {
        MailMessageProcessor processor = new MailMessageProcessor(compositionSpaceId, session, services);

        boolean buffered = false;
        MailMessage clone;
        if (mailMessage instanceof MimeRawSource) {
            // For read-only access it is ok to access MIME message directly
            processor.mimeMessage = (MimeMessage) ((MimeRawSource) mailMessage).getPart();
            clone = mailMessage;
        } else {
            // Need to buffer...
            clone = processor.initBufferedMimeMessage(mailMessage);
            buffered = true;
        }

        processor.parse(clone, !buffered);
        return processor;
    }

    /**
     * Initializes a processor for creating a new draft mail.
     *
     * @param compositionSpaceId The composition space identifier
     * @param optionalSharedFolderRef The optional shared attachment folder reference
     * @param clientToken The client token
     * @param session The session providing user information
     * @param services The service look-up
     * @return Newly initialized processor ready for creating draft mail
     * @throws OXException If processor cannot be initialized
     */
    public static MailMessageProcessor initNew(UUID compositionSpaceId, Optional<SharedFolderReference> optionalSharedFolderRef, ClientToken clientToken, Session session, ServiceLookup services) throws OXException {
        UserSettingMail usm;
        if (session instanceof ServerSession) {
            usm = ((ServerSession) session).getUserSettingMail();
        } else {
            usm = UserSettingMailStorage.getInstance().getUserSettingMail(session);
        }
        MailAccountStorageService mass = services.getServiceSafe(MailAccountStorageService.class);
        MailAccount defaultMailAccount = mass.getDefaultMailAccount(session.getUserId(), session.getContextId());
        Address primaryAddress = new Address(defaultMailAccount.getPersonal(), defaultMailAccount.getPrimaryAddress());
        MailMessageProcessor processor = new MailMessageProcessor(compositionSpaceId, session, services);
        processor.initEmptyMimeMessage(usm.getMsgFormat(), Optional.of(primaryAddress), optionalSharedFolderRef, clientToken);
        return processor;
    }

    /**
     * Looks-up attachment for given identifier.
     *
     * @param attachmentId The attachment identifier
     * @param compositionSpaceId The composition space identifier
     * @param mailMessage The mail message providing access to draft mail data
     * @param session The session providing user information
     * @param services The service look-up
     * @return The attachment
     * @throws OXException If no such attachment exists
     * @throws MissingDraftException If {@link MailMessage} throws {@link MailExceptionCode#MAIL_NOT_FOUND} when lazily loading parts of it.
     */
    public static Attachment attachmentLookUp(UUID attachmentId, UUID compositionSpaceId, MailMessage mailMessage, Session session, ServiceLookup services) throws OXException, MissingDraftException {
        try {
            AttachmentLookUpHandler handler = new AttachmentLookUpHandler(attachmentId);
            new MailMessageParser().setInlineDetectorBehavior(true).parseMailMessage(mailMessage, handler);
            Optional<MailPart> optionalAttachment = handler.getAttachment();

            if (!optionalAttachment.isPresent()) {
                throw CompositionSpaceErrorCode.NO_SUCH_ATTACHMENT_IN_COMPOSITION_SPACE.create(getUnformattedString(attachmentId), getUnformattedString(compositionSpaceId));
            }

            MailMessageProcessor processor = new MailMessageProcessor(compositionSpaceId, session, services);
            MailPart mailPart = optionalAttachment.get();
            if (mailPart.containsContentDisposition() && mailPart.getContentDisposition().isInline()) {
                return processor.createExistingInlineAttachmentFor(mailPart, ContentId.valueOf(mailPart.getContentId()), compositionSpaceId, true);
            }
            return processor.createExistingAttachmentFor(mailPart, compositionSpaceId, true);
        } catch (OXException e) {
            if (MailExceptionCode.MAIL_NOT_FOUND.equals(e)) {
                throw new MissingDraftException(new DefaultMailStorageId(mailMessage.getMailPath(), compositionSpaceId, Optional.empty()));
            }

            throw e;
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final UUID compositionSpaceId;
    private final ServerSession session;
    private final ServiceLookup services;
    private final List<ForwardingAttachmentIfNotSet> attachments;
    private final List<OXException> warnings;

    private MimeMessage mimeMessage;
    private CacheReference cacheReference;
    private ContentType contentType;
    private String contentForDraft;
    private String contentForWeb;
    private long originalLength;
    private ClientToken clientToken;
    private boolean parsed;

    private AttachmentStorage attachmentStorage = null;

    private MailMessageProcessor(UUID compositionSpaceId, Session session, ServiceLookup services) throws OXException {
        super();
        this.compositionSpaceId = compositionSpaceId;
        this.session = ServerSessionAdapter.valueOf(session);
        this.services = services;
        this.attachments = new ArrayList<>();
        warnings = new LinkedList<>();
        clientToken = ClientToken.NONE;
    }

    /**
     * Gets the warnings.
     *
     * @return The warnings or an empty listing
     */
    public List<OXException> getWarnings() {
        return warnings;
    }

    /**
     * Gets the attachment storage for Link Mail.
     *
     * @return The attachment storage
     * @throws OXException If attachment storage cannot be returned
     */
    public AttachmentStorage getAttachmentStorage() throws OXException {
        AttachmentStorage attachmentStorage = this.attachmentStorage;
        if (attachmentStorage == null) {
            AttachmentStorageRegistry attachmentStorageRegistry = services.getServiceSafe(AttachmentStorageRegistry.class);
            attachmentStorage = attachmentStorageRegistry.getAttachmentStorageFor(session);
            this.attachmentStorage = attachmentStorage;
        }
        return attachmentStorage;
    }

    /**
     * Closes this processor; relinquishing any underlying resources.
     * <p>
     * Use with care. Not suitable if this processor was used to e.g. return an attachment as attachment's data will be cleansed.
     */
    public void close() {
        if (mimeMessage instanceof MimeCleanUp) {
            ((MimeCleanUp) mimeMessage).cleanUp();
        }
        parsed = false;
        mimeMessage = null;
        contentType = null;
        contentForDraft = null;
        contentForWeb = null;
    }

    /**
     * Applies all updated fields of given draft message to internal draft state.
     * <p>
     * <b>Note</b>: Attachments are <b>not</b> considered! Use dedicated methods for that; e.g. {@link #addAttachments(List)}
     *
     * @param draftMessage The draft message providing the fields to update
     * @throws OXException
     */
    public void applyUpdate(MessageDescription draftMessage) throws OXException {
        validateState();
        applyUpdate0(draftMessage);
    }

    /**
     * Stores existent (file) attachments into shared attachment storage.
     *
     * @throws OXException If an error occurs
     */
    public void storeAttachments() throws OXException {
        validateState();

        Item folder = null;
        AttachmentStorage attachmentStorage = null;
        try {
            SharedAttachmentsInfo sharedAttachmentsInfo = convertSharedAttachmentsInfo(mimeMessage);
            if (sharedAttachmentsInfo == null || sharedAttachmentsInfo.isDisabled()) {
                throw new IllegalStateException("No shared attachments enabled");
            }

            if (attachments == null || attachments.isEmpty()) {
                return;
            }

            attachmentStorage = getAttachmentStorage();
            for (ForwardingAttachmentIfNotSet attachment : attachments) {
                if (attachment.getOrigin() != AttachmentOrigin.VCARD && attachment.getContentDisposition() != ContentDisposition.INLINE && attachment.getSharedAttachmentReference() == null) {
                    FileItem fileItem = new FileItem(null, attachment.getName(), attachment.getSize(), attachment.getMimeType(), new AttachmentFileItemDataProvider(attachment));

                    if (folder == null) {
                        FileItems singletonList = new FileItems(1);
                        singletonList.add(fileItem);
                        String subject = MimeMessageUtility.getSubject(mimeMessage);
                        StoredAttachments storedAttachments = attachmentStorage.storeAttachments(singletonList, SharedAttachmentsUtils.buildFolderName(subject, true, session), session);
                        folder = storedAttachments.getFolder();
                        attachment.setSharedAttachmentReference(new SharedAttachmentReference(storedAttachments.getAttachments().get(0).getId(), folder.getId()));

                        applySharedFolderReferenceHeader(SharedFolderReference.valueOf(folder.getId()), mimeMessage);
                    } else {
                        Item item = attachmentStorage.appendAttachment(fileItem, folder.getId(), session);
                        attachment.setSharedAttachmentReference(new SharedAttachmentReference(item.getId(), folder.getId()));
                    }
                }
            }
            folder = null;
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        } finally {
            if (folder != null && attachmentStorage != null) {
                // Operation failed if we reach this location
                deleteSharedAttachmentFolderSafe(folder.getId(), attachmentStorage);

                removeHeaderSafe(HeaderUtility.HEADER_X_OX_SHARED_ATTACHMENTS);
                removeHeaderSafe(HeaderUtility.HEADER_X_OX_SHARED_FOLDER_REFERENCE);

                for (ForwardingAttachmentIfNotSet attachment : attachments) {
                    attachment.setSharedAttachmentReference(null);
                }
            }
        }
    }

    private void deleteSharedAttachmentFolderSafe(String folderId, AttachmentStorage attachmentStorage) {
        try {
            attachmentStorage.deleteFolder(folderId, session);
        } catch (Exception e) {
            LOG.warn("Failed to delete shared attachmewnts folder {}", folderId, e);
        }
    }

    private void removeHeaderSafe(String headerName) {
        try {
            mimeMessage.removeHeader(headerName);
        } catch (Exception e) {
            LOG.warn("Failed to remove header \"{}\" from MIME message", headerName, e);
        }
    }

    /**
     * Un-Stores existent (file) attachments from shared attachment storage.
     *
     * @throws OXException If an error occurs
     */
    public void unstoreAttachments() throws OXException {
        validateState();

        try {
            SharedAttachmentsInfo sharedAttachmentsInfo = convertSharedAttachmentsInfo(mimeMessage);
            if (sharedAttachmentsInfo != null && sharedAttachmentsInfo.isEnabled()) {
                throw new IllegalStateException("Shared attachments not disabled");
            }

            AttachmentStorage attachmentStorage = getAttachmentStorage();
            for (ForwardingAttachmentIfNotSet attachment : attachments) {
                SharedAttachmentReference sharedAttachmentRef = attachment.getSharedAttachmentReference();
                if (sharedAttachmentRef != null) {
                    FileItem fileItem = attachmentStorage.getAttachment(sharedAttachmentRef.getAttachmentId(), sharedAttachmentRef.getFolderId(), session);

                    ThresholdFileHolder sink = null;
                    try {
                        sink = ThresholdFileHolderFactory.getInstance().createFileHolder(session);
                        sink.write(fileItem.getData());

                        attachment.setDataProvider(new ThresholdFileHolderDataProvider(sink));
                        sink = null; // Avoid premature closing
                    } finally {
                        Streams.close(sink);
                    }
                    attachment.setSharedAttachmentReference(null);
                }
            }

            SharedFolderReference sharedFolderRef = convertSharedFolderReference(mimeMessage);
            if (sharedFolderRef != null) {
                deleteSharedAttachmentFolderSafe(sharedFolderRef.getFolderId(), attachmentStorage);
            }
            mimeMessage.removeHeader(HeaderUtility.HEADER_X_OX_SHARED_FOLDER_REFERENCE);
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    /**
     * Adds a list of new attachments to the draft message. Every
     * attachment will get a fresh ID and content ID.
     *
     * @param attachments
     * @return The newly added attachments
     * @throws OXException
     */
    public List<Attachment> addAttachments(List<Attachment> attachments) throws OXException {
        if (attachments == null || attachments.isEmpty()) {
            return Collections.emptyList();
        }

        validateState();
        try {
            List<Attachment> newAttachments = new ArrayList<>(attachments.size());
            Set<UUID> attachmentIds = this.attachments.isEmpty() ? Collections.emptySet() : this.attachments.stream().map(a -> a.getId()).collect(toSet());

            SharedAttachmentsInfo sharedAttachmentsInfo = convertSharedAttachmentsInfo(mimeMessage);
            if (sharedAttachmentsInfo != null && sharedAttachmentsInfo.isEnabled()) {
                // Store (file) attachments in attachment storage
                AttachmentStorage attachmentStorage = getAttachmentStorage();

                // Grab possibly already existent shared folder
                SharedFolderReference parsedSharedFolderRef = convertSharedFolderReference(mimeMessage);
                SharedFolderReference sharedFolderRef = parsedSharedFolderRef;

                // Iterate attachments to add
                Next: for (Attachment attach : attachments) {
                    ForwardingAttachmentIfNotSet attachment = ForwardingAttachmentIfNotSet.valueFor(attach);
                    if (attachment.getId() != null) {
                        if (attachmentIds.contains(attachment.getId())) {
                            // Such an attachment already present
                            continue Next;
                        }
                    } else {
                        attachment.setId(UUID.randomUUID());
                    }

                    if (attachment.getContentIdAsObject() == null) {
                        attachment.setContentIdAsObject(AttachmentStorages.generateContentIdForAttachmentId(attachment.getId()));
                    }

                    if (attachment.getOrigin() != AttachmentOrigin.VCARD && attachment.getContentDisposition() != ContentDisposition.INLINE) {
                        if (attachment.getSharedAttachmentReference() == null) {
                            FileItem fileItem = new FileItem(null, attachment.getName(), attachment.getSize(), attachment.getMimeType(), new AttachmentFileItemDataProvider(attachment));
                            if (sharedFolderRef == null) {
                                FileItems singletonList = new FileItems(1);
                                singletonList.add(fileItem);
                                String subject = MimeMessageUtility.getSubject(mimeMessage);
                                StoredAttachments storedAttachments = attachmentStorage.storeAttachments(singletonList, SharedAttachmentsUtils.buildFolderName(subject, true, session), session);
                                String folderId = storedAttachments.getFolder().getId();
                                attachment.setSharedAttachmentReference(new SharedAttachmentReference(storedAttachments.getAttachments().get(0).getId(), folderId));

                                sharedFolderRef = SharedFolderReference.valueOf(folderId);
                                applySharedFolderReferenceHeader(sharedFolderRef, mimeMessage);
                            } else {
                                String folderId = sharedFolderRef.getFolderId();
                                Item item = attachmentStorage.appendAttachment(fileItem, folderId, session);
                                attachment.setSharedAttachmentReference(new SharedAttachmentReference(item.getId(), folderId));
                            }
                        } else {
                            // Shared attachment reference already present
                            if (parsedSharedFolderRef == null) {
                                // But no such folder
                                OXException.general("Cannot add shared attachment");
                            }
                        }
                    }

                    this.attachments.add(attachment);
                    newAttachments.add(attachment);
                }
            } else {
                // Append to attachments listing as usual
                Next: for (Attachment attach : attachments) {
                    ForwardingAttachmentIfNotSet attachment = ForwardingAttachmentIfNotSet.valueFor(attach);
                    if (attachment.getId() != null) {
                        if (attachmentIds.contains(attachment.getId())) {
                            // Such an attachment already present
                            continue Next;
                        }
                    } else {
                        attachment.setId(UUID.randomUUID());
                    }

                    if (attachment.getContentIdAsObject() == null) {
                        attachment.setContentIdAsObject(AttachmentStorages.generateContentIdForAttachmentId(attachment.getId()));
                    }

                    this.attachments.add(attachment);
                    newAttachments.add(attachment);
                }
            }

            Collections.sort(this.attachments, AttachmentComparator.getInstance());
            return newAttachments;
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    /**
     * Replaces an attachment within the draft message. Matching happens based on contained attachment IDs.
     *
     * @param attachment The attachment to replace
     * @return The newly added attachment
     * @throws OXException
     */
    public Attachment replaceAttachment(Attachment attachment) throws OXException {
        validateState();
        try {
            ForwardingAttachmentIfNotSet toAdd = null;
            List<ForwardingAttachmentIfNotSet> newAttachments = new ArrayList<>(attachments.size());
            for (ForwardingAttachmentIfNotSet attach : attachments) {
                if (attach.getId().equals(attachment.getId())) {
                    toAdd = ForwardingAttachmentIfNotSet.valueFor(attachment);

                    SharedAttachmentsInfo sharedAttachmentsInfo = convertSharedAttachmentsInfo(mimeMessage);
                    if (sharedAttachmentsInfo != null && sharedAttachmentsInfo.isEnabled()) {
                        // (File) attachments are stored in shared attachments folder
                        AttachmentStorage attachmentStorage = getAttachmentStorage();
                        if (toAdd.getOrigin() != AttachmentOrigin.VCARD && toAdd.getContentDisposition() != ContentDisposition.INLINE && toAdd.getSharedAttachmentReference() == null) {
                            SharedFolderReference sharedFolderRef = convertSharedFolderReference(mimeMessage);
                            FileItem fileItem = new FileItem(null, attachment.getName(), attachment.getSize(), attachment.getMimeType(), new AttachmentFileItemDataProvider(attachment));
                            if (sharedFolderRef == null) {
                                FileItems singletonList = new FileItems(1);
                                singletonList.add(fileItem);
                                String subject = MimeMessageUtility.getSubject(mimeMessage);
                                StoredAttachments storedAttachments = attachmentStorage.storeAttachments(singletonList, SharedAttachmentsUtils.buildFolderName(subject, true, session), session);
                                String folderId = storedAttachments.getFolder().getId();
                                toAdd.setSharedAttachmentReference(new SharedAttachmentReference(storedAttachments.getAttachments().get(0).getId(), folderId));

                                applySharedFolderReferenceHeader(SharedFolderReference.valueOf(folderId), mimeMessage);
                            } else {
                                if (attach.getSharedAttachmentReference() != null) {
                                    attachmentStorage.deleteAttachment(attach.getSharedAttachmentReference().getAttachmentId(), attach.getSharedAttachmentReference().getFolderId(), session);
                                }
                                String folderId = sharedFolderRef.getFolderId();
                                Item item = attachmentStorage.appendAttachment(fileItem, folderId, session);
                                toAdd.setSharedAttachmentReference(new SharedAttachmentReference(item.getId(), folderId));
                            }
                        } else {
                            if (attach.getSharedAttachmentReference() != null) {
                                attachmentStorage.deleteAttachment(attach.getSharedAttachmentReference().getAttachmentId(), attach.getSharedAttachmentReference().getFolderId(), session);
                            }
                        }
                    }

                    newAttachments.add(toAdd);
                } else {
                    newAttachments.add(attach);
                }
            }

            if (toAdd == null) {
                String sAttachmentId = getUnformattedString(attachment.getId());
                String sCompositionSpaceId = getUnformattedString(compositionSpaceId);
                throw CompositionSpaceErrorCode.NO_SUCH_ATTACHMENT_IN_COMPOSITION_SPACE.create(sAttachmentId, sCompositionSpaceId);
            }

            this.attachments.clear();
            this.attachments.addAll(newAttachments);

            return toAdd;
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    /**
     * Deletes a list of attachments from the draft message. Matching happens based on contained attachment IDs. Content is not re-written
     * afterwards.
     *
     * @param attachmentIds The attachment identifiers
     * @return A draft message resembling the complete internal state
     * @throws OXException
     */
    public MessageDescription deleteAttachments(List<UUID> attachmentIds) throws OXException {
        validateState();

        if (this.attachments.isEmpty()) {
            String sAttachmentId = getUnformattedString(attachmentIds.get(0));
            String sCompositionSpaceId = getUnformattedString(compositionSpaceId);
            throw CompositionSpaceErrorCode.NO_SUCH_ATTACHMENT_IN_COMPOSITION_SPACE.create(sAttachmentId, sCompositionSpaceId);
        }

        for (UUID toDelete : attachmentIds) {
            boolean removed = false;
            for (Iterator<ForwardingAttachmentIfNotSet> it = this.attachments.iterator(); it.hasNext();) {
                ForwardingAttachmentIfNotSet orig = it.next();
                if (toDelete.equals(orig.getId())) {
                    it.remove();
                    removed = true;

                    if (orig.getSharedAttachmentReference() != null) {
                        AttachmentStorage attachmentStorage = getAttachmentStorage();
                        attachmentStorage.deleteAttachment(orig.getSharedAttachmentReference().getAttachmentId(), orig.getSharedAttachmentReference().getFolderId(), session);
                    }
                }
            }
            if (!removed) {
                // No such attachment
                String sAttachmentId = getUnformattedString(toDelete);
                String sCompositionSpaceId = getUnformattedString(compositionSpaceId);
                throw CompositionSpaceErrorCode.NO_SUCH_ATTACHMENT_IN_COMPOSITION_SPACE.create(sAttachmentId, sCompositionSpaceId);
            }
        }

        return getCurrentDraft0();
    }

    /**
     * Validates the draft message.
     *
     * @return <code>true</code> if draft message changed as a result of this call; otherwise <code>false</code>
     * @throws OXException If validation fails
     */
    public boolean validate() throws OXException {
        validateState();
        boolean changed = validateSharedAttachments();
        return changed;
    }

    private boolean validateSharedAttachments() throws OXException {
        try {
            SharedAttachmentsInfo sharedAttachmentsInfo = convertSharedAttachmentsInfo(mimeMessage);
            if (sharedAttachmentsInfo == null || sharedAttachmentsInfo.isDisabled()) {
                // Shared attachments not enabled
                return false;
            }

            SharedFolderReference sharedFolderRef = convertSharedFolderReference(mimeMessage);
            if (sharedFolderRef == null) {
                // No shared attachments folder available
                return false;
            }

            if (false == getAttachmentStorage().existsFolder(sharedFolderRef.getFolderId(), session)) {
                mimeMessage.removeHeader(HeaderUtility.HEADER_X_OX_SHARED_ATTACHMENTS);
                mimeMessage.removeHeader(HeaderUtility.HEADER_X_OX_SHARED_FOLDER_REFERENCE);
                for (Iterator<ForwardingAttachmentIfNotSet> it = this.attachments.iterator(); it.hasNext();) {
                    SharedAttachmentReference sharedAttachmentRef = it.next().getSharedAttachmentReference();
                    if (sharedAttachmentRef != null) {
                        it.remove();
                    }
                }
                warnings.add(CompositionSpaceErrorCode.MISSING_SHARED_ATTACHMENTS_FOLDER.create(sharedFolderRef.getFolderId(), UUIDs.getUnformattedString(compositionSpaceId)));
                return true;
            }

            // Query shared attachments
            Set<SharedAttachmentReference> sharedAttachmentRefs;
            {
                List<Item> sharedAttachments;
                try {
                    sharedAttachments = getAttachmentStorage().getAttachments(sharedFolderRef.getFolderId(), session);
                    sharedAttachmentRefs = sharedAttachments.stream().map(i -> new SharedAttachmentReference(i.getId(), sharedFolderRef.getFolderId())).collect(Collectors.toSet());
                } catch (OXException e) {
                    LOG.warn("Failed to query shared attachments. Aborting validation...", e);
                    return false;
                }
            }

            boolean changed = false;
            for (Iterator<ForwardingAttachmentIfNotSet> it = this.attachments.iterator(); it.hasNext();) {
                SharedAttachmentReference sharedAttachmentRef = it.next().getSharedAttachmentReference();
                if (sharedAttachmentRef != null) {
                    boolean exists = sharedAttachmentRefs.remove(sharedAttachmentRef);
                    if (false == exists) {
                        // No such file item in shared attachments folder
                        it.remove();
                        changed = true;
                        warnings.add(CompositionSpaceErrorCode.INCONSISTENT_SHARED_ATTACHMENTS.create(sharedFolderRef.getFolderId(), UUIDs.getUnformattedString(compositionSpaceId)));
                    }
                }
            }

            if (false == sharedAttachmentRefs.isEmpty()) {
                // Add shared attachments not yet contained as attachment
                for (SharedAttachmentReference sharedAttachmentRef : sharedAttachmentRefs) {
                    try {
                        FileItem fileItem = getAttachmentStorage().getAttachment(sharedAttachmentRef.getAttachmentId(), sharedAttachmentRef.getFolderId(), session);

                        UUID id = UUID.randomUUID();
                        DefaultAttachment.Builder attachment = DefaultAttachment.builder(id);
                        attachment.withContentDisposition(ContentDisposition.ATTACHMENT);
                        attachment.withContentId(AttachmentStorages.generateContentIdForAttachmentId(id));
                        attachment.withMimeType(fileItem.getMimeType());
                        attachment.withName(fileItem.getName());
                        attachment.withOrigin(AttachmentOrigin.UPLOAD);

                        boolean buffer = false;
                        if (buffer) {
                            ThresholdFileHolder sink = ThresholdFileHolderFactory.getInstance().createFileHolder(session);
                            try {
                                sink.write(fileItem.getData());
                                attachment.withDataProvider(new ThresholdFileHolderDataProvider(sink));
                                attachment.withSize(sink.getLength());
                                sink = null; // Avoid premature closing
                            } finally {
                                Streams.close(sink);
                            }
                        } else {
                            attachment.withDataProvider(new FileItemDataProvider(fileItem));
                            attachment.withSize(fileItem.getSize());
                        }

                        ForwardingAttachmentIfNotSet forwardingAttachment = ForwardingAttachmentIfNotSet.valueFor(attachment.build());
                        forwardingAttachment.setSharedAttachmentReference(sharedAttachmentRef);

                        this.attachments.add(forwardingAttachment);
                        changed = true;
                        warnings.add(CompositionSpaceErrorCode.INCONSISTENT_SHARED_ATTACHMENTS.create(sharedFolderRef.getFolderId(), UUIDs.getUnformattedString(compositionSpaceId)));
                    } catch (OXException e) {
                        LOG.warn("Failed to add shared attachment to draft message during validation.", e);
                    }
                }
            }

            return changed;
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    /**
     * Gets the current draft representation pre-filled with all fields.
     *
     * @return The current draft
     * @throws OXException If current draft cannot be returned
     */
    public MessageDescription getCurrentDraft() throws OXException {
        validateState();
        return getCurrentDraft0();
    }

    /**
     * Gets the current draft representation pre-filled with given fields.
     *
     * @param fields The fields to pre-fill; if <code>null</code> or empty all fields are loaded
     * @return The current draft
     * @throws OXException If current draft cannot be returned
     */
    public MessageDescription getCurrentDraft(MessageField... fields) throws OXException {
        validateState();
        return fields == null || fields.length <= 0 ? getCurrentDraft0() : getCurrentDraft0(EnumSet.of(fields[0], fields));
    }

    /**
     * Gets the current draft representation pre-filled with given fields.
     *
     * @param fields The fields to pre-fill ; if <code>null</code> or empty all fields are loaded
     * @return The current draft
     * @throws OXException If current draft cannot be returned
     */
    public MessageDescription getCurrentDraft(Set<MessageField> fields) throws OXException {
        validateState();
        return fields == null || fields.isEmpty() ? getCurrentDraft0() : getCurrentDraft0(fields);
    }

    /**
     * Gets the attachment associated with given identifier.
     *
     * @param attachmentId The attachment identifier
     * @return The attachment or <code>null</code>
     */
    public Attachment getAttachment(UUID attachmentId) {
        validateState();
        return this.attachments.stream().filter(a -> a.getId().equals(attachmentId)).findFirst().orElse(null);
    }

    /**
     * Compiles the draft mail from current draft representation.
     *
     * @param asFinalDraft Whether to compile as final draft mail
     * @return A ComposedMailMessage
     * @throws OXException If compilation failed
     */
    public ComposedMailMessage compileDraft(boolean asFinalDraft) throws OXException {
        validateState();
        compileDraft0(asFinalDraft);
        return new ContentAwareComposedMailMessage(this.mimeMessage, session.getContextId());
    }

    /**
     * Compiles the compose request from current draft representation for mail transport.
     *
     * @param request The request data
     * @param optRefMessage The optional referenced message (in case of reply, forward, etc.)
     * @return The compose request
     * @throws OXException If compose request cannot be returned
     */
    public ComposeRequest compileComposeRequest(AJAXRequestData request, Optional<MailMessage> optRefMessage) throws OXException {
        validateState();
        return compileTransportMessage0(request, optRefMessage);
    }

    /**
     * Gets the optional cache reference
     *
     * @return The cache reference or empty
     */
    public Optional<CacheReference> getFileCacheReference() {
        return Optional.ofNullable(cacheReference);
    }

    /**
     * Gets the current MIME messages <code>Date</code> header value if present.
     *
     * @return The parsed {@link Date} or an empty Optional if header is absent or could not be parsed
     */
    public Optional<Date> getDateHeader() {
        validateState();
        try {
            return Optional.ofNullable(mimeMessage.getSentDate());
        } catch (MessagingException e) {
            LOG.debug("Unable to parse Date header of draft message", e);
            return Optional.empty();
        }
    }

    /**
     * Gets the current client token
     *
     * @return The token
     */
    public ClientToken getClientToken() {
        validateState();
        return clientToken;
    }

    /**
     * Gets the size of the original message this processor was initialized with.
     *
     * @return The size in bytes or <code>-1</code> if unknown
     */
    public long getOriginalSize() {
        return originalLength;
    }

    private void validateState() {
        if (!parsed) {
            throw new IllegalStateException("Message has not been parsed yet!");
        }
    }


    // ---------------------------------------- MESSAGE PARSING ----------------------------------------

    private MailMessage initBufferedMimeMessage(InputStream mimeStream) throws OXException {
        ThresholdFileHolder sink = ThresholdFileHolderFactory.getInstance().createFileHolder(session);
        boolean closeSink = true;
        try {
            sink.write(mimeStream);
            File tempFile = sink.getTempFile();
            if (null == tempFile) {
                this.mimeMessage = new MimeMessage(MimeDefaultSession.getDefaultSession(), sink.getStream());
            } else {
                this.mimeMessage = new FileBackedMimeMessage(MimeDefaultSession.getDefaultSession(), tempFile);
            }
            MailMessage mailMessage = MimeMessageConverter.convertMessage(mimeMessage, false);
            this.originalLength = sink.getLength();
            closeSink = false;
            return mailMessage;
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        } catch (IOException e) {
            throw CompositionSpaceErrorCode.IO_ERROR.create(e, e.getMessage());
        } finally {
            if (closeSink) {
                sink.close();
            }
        }
    }

    private MailMessage initBufferedMimeMessage(MailMessage mailMessage) throws OXException, MissingDraftException {
        ThresholdFileHolder sink = ThresholdFileHolderFactory.getInstance().createFileHolder(session);
        boolean closeSink = true;
        try {
            mailMessage.writeTo(sink.asOutputStream());
            File tempFile = sink.getTempFile();
            if (null == tempFile) {
                this.mimeMessage = new MimeMessage(MimeDefaultSession.getDefaultSession(), sink.getStream());
            } else {
                this.mimeMessage = new FileBackedMimeMessage(MimeDefaultSession.getDefaultSession(), tempFile);
            }
            this.originalLength = sink.getLength();
            closeSink = false;
            return MimeMessageConverter.convertMessage(this.mimeMessage, false);
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        } catch (IOException e) {
            throw CompositionSpaceErrorCode.IO_ERROR.create(e, e.getMessage());
        } catch (OXException e) {
            if (MailExceptionCode.MAIL_NOT_FOUND.equals(e)) {
                throw new MissingDraftException(new DefaultMailStorageId(mailMessage.getMailPath(), compositionSpaceId, Optional.empty()));
            }

            throw e;
        } finally {
            if (closeSink) {
                sink.close();
            }
        }
    }

    private MailMessage initMimeMessageFromFileCache(CacheReference cacheReference) throws OXException {
        try {
            this.mimeMessage = new FileBackedMimeMessage(MimeDefaultSession.getDefaultSession(), cacheReference.getMimeStream());
            this.cacheReference = cacheReference;
            this.originalLength = cacheReference.getSize();
            return MimeMessageConverter.convertMessage(this.mimeMessage, false);
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        } catch (IOException e) {
            throw CompositionSpaceErrorCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    private void initEmptyMimeMessage(int msgFormat, Optional<Address> optionalPrimaryAddress, Optional<SharedFolderReference> optionalSharedFolderRef, ClientToken clientToken) throws OXException {
        ContentType defaultContentType = ContentType.TEXT_HTML;
        if (msgFormat == UserSettingMail.MSG_FORMAT_TEXT_ONLY) {
            defaultContentType = ContentType.TEXT_PLAIN;
        } else if (msgFormat == UserSettingMail.MSG_FORMAT_BOTH) {
            defaultContentType = ContentType.MULTIPART_ALTERNATIVE;
        }

        this.mimeMessage = new MimeMessage(MimeDefaultSession.getDefaultSession());
        this.contentType = defaultContentType;
        this.contentForWeb = ""; //defaultContentType.isImpliesHtml() ? getEmptyHtmlContent() : ""; Adapt to UI behavior
        this.contentForDraft = this.contentForWeb;
        this.parsed = true;
        this.clientToken = clientToken;

        try {
            mimeMessage.setSubject("", "UTF-8");
            if (optionalPrimaryAddress.isPresent()) {
                mimeMessage.setFrom(toMimeAddress(optionalPrimaryAddress.get()));
            }
            applyDraftFlag(mimeMessage);
            applyCompositionSpaceHeadersForNew(compositionSpaceId, contentType, optionalSharedFolderRef, clientToken, mimeMessage);
            mimeMessage.saveChanges();
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    /*
     * With inline image and attachment:
     *
     * - multipart/mixed
     *   - multipart/alternative
     *     - text/plain (disposition: null/inline; name=null)
     *     - multipart/related
     *       - text/html (disposition: null/inline; name=null)
     *       - image/png (disposition: null/inline; name=non-null; content-id: non-null)
     *   - application/pdf (disposition: attachment; name=non-null; content-id: null)
     */
    private void parse(MailMessage mailMessage, boolean buffer) throws OXException {
        boolean multipart = isMultipartMessage();
        Optional<ContentType> optionalContentType = Optional.empty();
        String headerValue = HeaderUtility.decodeHeaderValue(mailMessage.getFirstHeader(HeaderUtility.HEADER_X_OX_CONTENT_TYPE));
        ContentType ct = ContentType.contentTypeFor(headerValue);
        if (ct != null) {
            optionalContentType = Optional.of(ct);
        }

        UserSettingMail usm = session.getUserSettingMail();
        boolean isHtml = optionalContentType.isPresent() ? optionalContentType.get().isImpliesHtml() : usm.isDisplayHtmlInlineContent();

        // Grab first seen text from original message and check for possible referenced inline images
        List<ContentId> contentIds = multipart ? new ArrayList<>() : null;
        parseContentAndType(mailMessage, optionalContentType, isHtml, contentIds);

        // Check if original mail may contain attachments
        if (multipart) {
            ParsedAttachments parsedAttachments = parseAttachments(mailMessage, buffer);
            this.attachments.clear();
            this.attachments.addAll(parsedAttachments.attachments);

            // Replace content with newly assigned Content-Ids
            if (!parsedAttachments.oldToNewContentIds.isEmpty()) {
                this.contentForDraft = replaceCids(this.contentForDraft, parsedAttachments.oldToNewContentIds);
            }

            // Replace with <img> tags accessible for Web Client
            if (!parsedAttachments.attachmentsByContentId.isEmpty()) {
                this.contentForWeb = CompositionSpaces.replaceCidInlineImages(this.contentForDraft, Optional.of(compositionSpaceId),
                    parsedAttachments.attachmentsByContentId, MailStorageCompositionSpaceImageDataSource.getInstance(), session);
            }
        }

        // Get Client Token
        String clientTokenValue = null;
        try {
            clientTokenValue = HeaderUtility.decodeHeaderValue(mailMessage.getFirstHeader(HeaderUtility.HEADER_X_OX_CLIENT_TOKEN));
            this.clientToken = ClientToken.of(clientTokenValue);
            if (this.clientToken.isAbsent()) {
                LOG.warn("Draft mail contains invalid client token: {}", clientTokenValue);
            }
        } catch (IllegalArgumentException e) {
            LOG.warn("Draft mail contains invalid client token: {}", clientTokenValue);
            this.clientToken = ClientToken.NONE;
        }

        this.parsed = true;
        LOG.debug("Current in memory representation:{}{}", System.lineSeparator(), new LoggableMessageRepresentation(this));
    }

    private void parseContentAndType(MailMessage mailMessage, Optional<ContentType> optionalContentType, boolean isHtml, List<ContentId> contentIds) throws OXException {
        TextAndContentType textForForward;
        if (contentIds == null) {
            textForForward = MimeProcessingUtility.getTextForForward(mailMessage, isHtml, false, null, session);
        } else {
            List<String> cids = new ArrayList<>();
            textForForward = MimeProcessingUtility.getTextForForward(mailMessage, isHtml, false, cids, session);
            for (String cid : cids) {
                contentIds.add(ContentId.valueOf(cid));
            }
        }

        if (optionalContentType.isPresent()) {
            ContentType contentType = optionalContentType.get();
            if (null == textForForward) {
                if (isHtml) {
                    this.contentForDraft = getEmptyHtmlContent();
                } else {
                    this.contentForDraft = "";
                }
                this.contentType = contentType;
            } else {
                this.contentForDraft = textForForward.getText();
                if (textForForward.isHtml()) {
                    this.contentType = contentType.isImpliesHtml() ? contentType : ContentType.TEXT_HTML;
                } else {
                    this.contentType = ContentType.TEXT_PLAIN;
                }
            }
        } else {
            if (null == textForForward) {
                if (isHtml) {
                    this.contentForDraft = getEmptyHtmlContent();
                    this.contentType = ContentType.TEXT_HTML;
                } else {
                    this.contentForDraft = "";
                    this.contentType = ContentType.TEXT_PLAIN;
                }
            } else {
                this.contentForDraft = textForForward.getText();
                this.contentType = textForForward.isHtml() ? ContentType.TEXT_HTML : ContentType.TEXT_PLAIN;
            }
        }

        this.contentForWeb = this.contentForDraft;
    }

    private ParsedAttachments parseAttachments(MailMessage mailMessage, boolean buffer) throws OXException {
        ParsedAttachments result = new ParsedAttachments();

        // Determine all attachments contained in current message
        AllAttachmentsHandler attachmentsHandler = new AllAttachmentsHandler();
        new MailMessageParser().setInlineDetectorBehavior(true).parseMailMessage(mailMessage, attachmentsHandler);
        List<MailPart> allAttachmentParts = attachmentsHandler.getAttachmentParts();
        if (allAttachmentParts.isEmpty()) {
            return result;
        }

        // Determine all inline and non-inline parts
        Map<ContentId, MailPart> inlinePartsByContentId = null;
        List<MailPart> attachmentParts = null;
        for (MailPart p : allAttachmentParts) {
            if (p.getContentId() != null && (false == p.containsContentDisposition() || (p.getContentDisposition().isInline()))) {
                if (inlinePartsByContentId == null) {
                    inlinePartsByContentId = new LinkedHashMap<>(allAttachmentParts.size());
                }
                inlinePartsByContentId.put(ContentId.valueOf(p.getContentId()), p);
            } else {
                if (attachmentParts == null) {
                    attachmentParts = new ArrayList<>(allAttachmentParts.size());
                }
                attachmentParts.add(p);
            }
        }

        if (inlinePartsByContentId != null) {
            /*-
             *  - Add inline parts to composition space attachments and
             *  - Check for inline parts not captured as composition space attachment, yet. Prepare & add them.
             */
            //Map<ContentId, ContentId> oldToNewContentIds = new HashMap<>(4);
            //Map<ContentId, Attachment> attachmentsByContentId = new HashMap<>(inlinePartsByContentId.size() * 2);
            for (Map.Entry<ContentId, MailPart> entry : inlinePartsByContentId.entrySet()) {
                MailPart part = entry.getValue();
                ContentId contentId = entry.getKey();
                ForwardingAttachmentIfNotSet attachment = createExistingInlineAttachmentFor(part, contentId, compositionSpaceId, buffer);
                if (attachment.getId() == null) {
                    // this was a yet unknown one
                    UUID attachmentId = UUID.randomUUID();
                    attachment.setId(attachmentId);
                    ContentId newContentId = AttachmentStorages.generateContentIdForAttachmentId(attachmentId);
                    if (attachment.getContentIdAsObject() != null) {
                        result.oldToNewContentIds.put(attachment.getContentIdAsObject(), newContentId);
                        attachment.setContentIdAsObject(newContentId);
                    }
                }
                result.attachmentsByContentId.put(contentId, attachment);
                result.attachments.add(attachment);
            }
        }

        if (attachmentParts != null) {
            /*-
             *  - Add non-inline parts to composition space attachments and
             *  - Check for non-inline parts not captured as composition space attachment, yet. Prepare & add them.
             */
            for (MailPart part : attachmentParts) {
                ForwardingAttachmentIfNotSet attachment = createExistingAttachmentFor(part, compositionSpaceId, buffer);
                if (attachment.getId() == null) {
                    // this was a yet unknown one
                    UUID attachmentId = UUID.randomUUID();
                    attachment.setId(attachmentId);
                    attachment.setContentIdAsObject(AttachmentStorages.generateContentIdForAttachmentId(attachmentId));
                }
                result.attachments.add(attachment);
            }
        }

        return result;
    }

    private static final class ParsedAttachments {

        final List<ForwardingAttachmentIfNotSet> attachments;
        final Map<ContentId, ContentId> oldToNewContentIds;
        final Map<ContentId, Attachment> attachmentsByContentId;

        ParsedAttachments() {
            super();
            attachments = new LinkedList<>();
            oldToNewContentIds = new HashMap<>(4);
            attachmentsByContentId = new HashMap<>(4);
        }
    }

    private static final Pattern PATTERN_SRC = MimeMessageUtility.PATTERN_SRC;

    /**
     * Replaces <code>"cid:"</code> references of &lt;img&gt; tags from old to new content IDs.
     * <p>
     * <code>&lt;img src="cid:123456"&gt;</code> is converted to<br>
     * <code>&lt;img src="cid:654321"&gt;</code>
     *
     * @param htmlContent The HTML content to replace in
     * @param oldToNewContentIds A mapping from old to new IDs
     * @return The (possibly) processed HTML content
     * @throws OXException If replacing &lt;img&gt; tags fails
     */
    private static String replaceCids(String htmlContent, Map<ContentId, ContentId> oldToNewContentIds) {
        // Fast check
        if (htmlContent.indexOf("<img") < 0) {
            return htmlContent;
        }

        Matcher matcher = PATTERN_SRC.matcher(htmlContent);
        if (!matcher.find()) {
            return htmlContent;
        }

        StringBuffer sb = new StringBuffer(htmlContent.length());
        do {
            String imageTag = matcher.group();
            String srcValue = matcher.group(1);
            if (srcValue.startsWith("cid:")) {
                ContentId oldContentId = ContentId.valueOf(srcValue.substring(4));
                ContentId newContentId = oldToNewContentIds.get(oldContentId);
                if (newContentId == null) {
                    // No such inline image... Yield a blank "src" attribute for current <img> tag
                    LOG.warn("No such inline image found for old content ID identifier {}", oldContentId);
                    matcher.appendReplacement(sb, "");
                } else {
                    String imageUrl = "cid:" + newContentId.getContentId();
                    int st = matcher.start(1) - matcher.start();
                    int end = matcher.end(1) - matcher.start();
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(imageTag.substring(0, st) + imageUrl + imageTag.substring(end)));
                }
            }
        } while (matcher.find());
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Creates an attachment for given inline mail part. This method expects an <code>X-Part-Id</code> MIME header
     * containing a valid attachment ID.
     *
     * @param mailPart The mail part
     * @param contentId The value for the Content-Id header
     * @param compositionSpaceId The composition space identifier
     * @param buffer Whether to buffer binary data
     * @return The newly created attachment description
     * @throws OXException If attachment cannot be generated
     */
    public ForwardingAttachmentIfNotSet createExistingInlineAttachmentFor(MailPart mailPart, ContentId contentId, UUID compositionSpaceId, boolean buffer) throws OXException {
        Optional<UUID> optAttachmentId = UUIDs.optionalFromUnformattedString(mailPart.getFirstHeader(MessageHeaders.HDR_X_PART_ID));
        if (optAttachmentId.isPresent()) {
            return createInlineAttachmentFor(mailPart, optAttachmentId.get(), contentId, compositionSpaceId, buffer);
        }

        throw OXException.general("Missing attachment identifier in mail part");
    }

    /**
     * Creates an attachment for given inline mail part. The attachment gets assigned a fresh ID and according Content-ID.
     *
     * @param mailPart The mail part
     * @param contentId The value for the Content-Id header
     * @param compositionSpaceId The composition space identifier
     * @param buffer Whether to buffer binary data
     * @return The newly created attachment description
     * @throws OXException If attachment cannot be generated
     */
    public ForwardingAttachmentIfNotSet createNewInlineAttachmentFor(MailPart mailPart, UUID compositionSpaceId, boolean buffer) throws OXException {
        UUID attachmentId = UUID.randomUUID();
        ContentId contentId = AttachmentStorages.generateContentIdForAttachmentId(attachmentId);
        return createInlineAttachmentFor(mailPart, attachmentId, contentId, compositionSpaceId, buffer);
    }

    private ForwardingAttachmentIfNotSet createInlineAttachmentFor(MailPart mailPart, UUID attachmentId, ContentId contentId, UUID compositionSpaceId, boolean buffer) throws OXException {
        DefaultAttachment.Builder attachment = DefaultAttachment.builder(attachmentId);
        attachment.withCompositionSpaceId(compositionSpaceId);
        attachment.withContentDisposition(ContentDisposition.INLINE);
        attachment.withContentId(contentId);
        attachment.withMimeType(mailPart.getContentType().getBaseType());
        {
            String fileName = mailPart.getFileName();
            attachment.withName(Strings.isEmpty(fileName) ? MailMessageParser.generateFilename(mailPart.getSequenceId(), mailPart.getContentType().getBaseType()) : fileName);
        }
        {
            AttachmentOrigin origin = AttachmentOrigin.getOriginFor(mailPart.getFirstHeader(MessageHeaders.HDR_X_OX_ATTACHMENT_ORIGIN));
            attachment.withOrigin(origin == null ? AttachmentOrigin.UPLOAD : origin);
        }
        return fillData(mailPart, attachment, buffer);
    }

    /**
     * Creates an attachment for given non-inline mail part. This method expects an <code>X-Part-Id</code> MIME header
     * containing a valid attachment ID.
     *
     * @param mailPart The mail part
     * @param compositionSpaceId The composition space identifier
     * @param buffer Whether to buffer binary data
     * @return The newly created attachment description
     * @throws OXException If attachment cannot be generated
     */
    public ForwardingAttachmentIfNotSet createExistingAttachmentFor(MailPart mailPart, UUID compositionSpaceId, boolean buffer) throws OXException {
        Optional<UUID> optAttachmentId = UUIDs.optionalFromUnformattedString(mailPart.getFirstHeader(MessageHeaders.HDR_X_PART_ID));
        if (optAttachmentId.isPresent()) {
            ContentId contentId = null;
            String sContentId = MimeMessageUtility.trimContentId(mailPart.getContentId());
            if (sContentId != null) {
                contentId = new ContentId(sContentId);
            }
            return createAttachmentFor(mailPart, optAttachmentId.get(), contentId, compositionSpaceId, buffer);
        }

        throw OXException.general("Missing attachment identifier in mail part");
    }

    /**
     * Creates an attachment for given non-inline mail part. The attachment gets assigned a fresh ID and according Content-ID.
     *
     * @param mailPart The mail part
     * @param compositionSpaceId The composition space identifier
     * @param buffer Whether to buffer binary data
     * @return The newly created attachment description
     * @throws OXException If attachment cannot be generated
     */
    public ForwardingAttachmentIfNotSet createNewAttachmentFor(MailPart mailPart, UUID compositionSpaceId, boolean buffer) throws OXException {
        UUID attachmentId = UUID.randomUUID();
        ContentId contentId = AttachmentStorages.generateContentIdForAttachmentId(attachmentId);
        return createAttachmentFor(mailPart, attachmentId, contentId, compositionSpaceId, buffer);
    }

    private ForwardingAttachmentIfNotSet createAttachmentFor(MailPart mailPart, UUID attachmentId, ContentId contentId, UUID compositionSpaceId, boolean buffer) throws OXException {
        DefaultAttachment.Builder attachment = DefaultAttachment.builder(attachmentId);
        attachment.withCompositionSpaceId(compositionSpaceId);
        attachment.withContentDisposition(ContentDisposition.ATTACHMENT);
        attachment.withContentId(contentId);
        attachment.withMimeType(mailPart.getContentType().getBaseType());
        String fileName = mailPart.getFileName();
        attachment.withName(Strings.isEmpty(fileName) ? MailMessageParser.generateFilename(mailPart.getSequenceId(), mailPart.getContentType().getBaseType()) : fileName);
        AttachmentOrigin origin = AttachmentOrigin.getOriginFor(mailPart.getFirstHeader(MessageHeaders.HDR_X_OX_ATTACHMENT_ORIGIN));
        if (origin == null) {
            origin = CompositionSpaces.hasVCardMarker(mailPart, session) ? AttachmentOrigin.VCARD : AttachmentOrigin.UPLOAD;
        }
        attachment.withOrigin(origin);
        return fillData(mailPart, attachment, buffer);
    }

    private ForwardingAttachmentIfNotSet fillData(MailPart mailPart, DefaultAttachment.Builder attachment, boolean buffer) throws OXException {
        String headerValue = HeaderUtility.decodeHeaderValue(mailPart.getFirstHeader(HeaderUtility.HEADER_X_OX_SHARED_ATTACHMENT_REFERENCE));
        SharedAttachmentReference sharedAttachmentRef = HeaderUtility.headerValue2SharedAttachmentReference(headerValue);

        if (sharedAttachmentRef == null) {
            if (buffer) {
                ThresholdFileHolder sink = ThresholdFileHolderFactory.getInstance().createFileHolder(session);
                try {
                    sink.write(mailPart.getInputStream());
                    attachment.withDataProvider(new ThresholdFileHolderDataProvider(sink));
                    attachment.withSize(sink.getLength());
                    sink = null; // Avoid premature closing
                } finally {
                    Streams.close(sink);
                }
            } else {
                attachment.withDataProvider(new MailPartDataProvider(mailPart));
                attachment.withSize(mailPart.getSize());
            }
        } else {
            Optional<FileItem> optionalFileItem = getFileItemSafe(sharedAttachmentRef, getAttachmentStorage());
            if (optionalFileItem.isPresent()) {
                FileItem fileItem = optionalFileItem.get();
                if (buffer) {
                    ThresholdFileHolder sink = ThresholdFileHolderFactory.getInstance().createFileHolder(session);
                    try {
                        sink.write(fileItem.getData());
                        attachment.withDataProvider(new ThresholdFileHolderDataProvider(sink));
                        attachment.withSize(sink.getLength());
                        sink = null; // Avoid premature closing
                    } finally {
                        Streams.close(sink);
                    }
                } else {
                    attachment.withDataProvider(new FileItemDataProvider(fileItem));
                    attachment.withSize(fileItem.getSize());
                }
            } else {
                // No such file item available
                attachment.withDataProvider(EmptyDataProvider.getInstance());
                attachment.withSize(0);
            }
        }

        ForwardingAttachmentIfNotSet forwardingAttachment = ForwardingAttachmentIfNotSet.valueFor(attachment.build());
        forwardingAttachment.setSharedAttachmentReference(sharedAttachmentRef);
        return forwardingAttachment;
    }

    private Optional<FileItem> getFileItemSafe(SharedAttachmentReference sharedAttachmentRef, AttachmentStorage attachmentStorage) {
        try {
            return Optional.of(attachmentStorage.getAttachment(sharedAttachmentRef.getAttachmentId(), sharedAttachmentRef.getFolderId(), ServerSessionAdapter.valueOf(session)));
        } catch (Exception e) {
            LOG.warn("Failed to get file item {} from folder {}", sharedAttachmentRef.getAttachmentId(), sharedAttachmentRef.getFolderId(), e);
            return Optional.empty();
        }
    }

    // ---------------------------------------- MESSAGE CONVERSION ----------------------------------------

    private MessageDescription getCurrentDraft0() throws OXException {
        return getCurrentDraft0(EnumSet.allOf(MessageField.class));
    }

    private MessageDescription getCurrentDraft0(Set<MessageField> fields) throws OXException {
        try {
            MessageDescription draftMessage = new MessageDescription();
            for (MessageField field : fields) {
                switch (field) {
                    case ATTACHMENTS:
                        draftMessage.setAttachments(this.attachments);
                        break;
                    case BCC:
                        draftMessage.setBcc(convertAddresses(MimeMessageUtility.getAddressHeader(MessageHeaders.HDR_BCC, mimeMessage)));
                        break;
                    case CC:
                        draftMessage.setCc(convertAddresses(MimeMessageUtility.getAddressHeader(MessageHeaders.HDR_CC, mimeMessage)));
                        break;
                    case CONTENT:
                    case CONTENT_ENCRYPTED:
                        draftMessage.setContent(contentForWeb);
                        break;
                    case CONTENT_TYPE:
                        draftMessage.setContentType(this.contentType);
                        break;
                    case CUSTOM_HEADERS: {
                        Map<String, String> customHeaders = convertCustomHeaders(mimeMessage);
                        if (customHeaders != null) {
                            draftMessage.setCustomHeaders(customHeaders);
                        }
                        break;
                    }
                    case FROM:
                        draftMessage.setFrom(convertFirstAddress(MimeMessageUtility.getAddressHeader(MessageHeaders.HDR_FROM, mimeMessage)));
                        break;
                    case META: {
                        String headerValue = HeaderUtility.decodeHeaderValue(MimeMessageConverter.getStringHeader(HeaderUtility.HEADER_X_OX_META, mimeMessage));
                        Meta parsedMeta = HeaderUtility.headerValue2Meta(headerValue);
                        draftMessage.setMeta(parsedMeta);
                        break;
                    }
                    case PRIORITY:
                        draftMessage.setPriority(convertPriority(mimeMessage));
                        break;
                    case REPLY_TO:
                        draftMessage.setReplyTo(convertFirstAddress(MimeMessageUtility.getAddressHeader(MessageHeaders.HDR_REPLY_TO, mimeMessage)));
                        break;
                    case REQUEST_READ_RECEIPT: {
                        String headerValue = HeaderUtility.decodeHeaderValue(MimeMessageConverter.getStringHeader(HeaderUtility.HEADER_X_OX_READ_RECEIPT, mimeMessage));
                        if ("true".equalsIgnoreCase(headerValue)) {
                            draftMessage.setRequestReadReceipt(true);
                        }
                        break;
                    }
                    case SECURITY:
                        draftMessage.setSecurity(convertSecurity(mimeMessage));
                        break;
                    case SENDER:
                        draftMessage.setSender(convertFirstAddress(MimeMessageUtility.getAddressHeader(MessageHeaders.HDR_SENDER, mimeMessage)));
                        break;
                    case SHARED_ATTACCHMENTS_INFO: {
                        SharedAttachmentsInfo parsedSharedAttachments = convertSharedAttachmentsInfo(mimeMessage);
                        draftMessage.setsharedAttachmentsInfo(parsedSharedAttachments);
                        break;
                    }
                    case SUBJECT:
                        draftMessage.setSubject(MimeMessageUtility.getSubject(mimeMessage));
                        break;
                    case TO:
                        draftMessage.setTo(convertAddresses(MimeMessageUtility.getAddressHeader(MessageHeaders.HDR_TO, mimeMessage)));
                        break;
                    default:
                        LOG.error("Unknown draftMessage field: {}", field);
                        break;
                }
            }

            // always set client token
            draftMessage.setClientToken(clientToken);
            return draftMessage;
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    private static SharedAttachmentsInfo convertSharedAttachmentsInfo(MimeMessage mimeMessage) throws MessagingException {
        String headerValue = HeaderUtility.decodeHeaderValue(MimeMessageConverter.getStringHeader(HeaderUtility.HEADER_X_OX_SHARED_ATTACHMENTS, mimeMessage));
        return HeaderUtility.headerValue2SharedAttachments(headerValue);
    }

    private static SharedFolderReference convertSharedFolderReference(MimeMessage mimeMessage) throws MessagingException {
        String headerValue = HeaderUtility.decodeHeaderValue(MimeMessageConverter.getStringHeader(HeaderUtility.HEADER_X_OX_SHARED_FOLDER_REFERENCE, mimeMessage));
        return HeaderUtility.headerValue2SharedFolderReference(headerValue);
    }

    private static Priority convertPriority(MimeMessage mimeMessage) throws MessagingException {
        Priority priority = null;
        String priorityStr = MimeMessageConverter.getStringHeader(MessageHeaders.HDR_X_PRIORITY, mimeMessage);
        if (Strings.isNotEmpty(priorityStr)) {
            try {
                int level = Integer.parseInt(priorityStr);
                priority = Priority.priorityForLevel(level);
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        if (priority == null) {
            String importanceStr = MimeMessageConverter.getStringHeader(MessageHeaders.HDR_IMPORTANCE, mimeMessage);
            if (Strings.isNotEmpty(importanceStr)) {
                priority = Priority.priorityFor(importanceStr);
            }
        }

        return priority;
    }

    private static Security convertSecurity(MimeMessage mimeMessage) throws MessagingException {
        String headerValue = HeaderUtility.decodeHeaderValue(MimeMessageConverter.getStringHeader(HeaderUtility.HEADER_X_OX_SECURITY, mimeMessage));
        return HeaderUtility.headerValue2Security(headerValue);
    }

    /**
     * Converts given Internet email address to an {@link Address} instance.
     *
     * @param addr The Internet email address to convert
     * @return The resulting {@code Address} instance
     */
    private static Address convertAddress(InternetAddress addr) {
        return null == addr ? null : new Address(addr.getPersonal(), IDNA.toIDN(addr.getAddress()));
    }

    /**
     * Converts given Internet email addresses to {@link Address} instances.
     *
     * @param addrs The Internet email addresses to convert
     * @return The resulting {@code Address} instances
     */
    public static List<Address> convertAddresses(InternetAddress[] addrs) {
        if (null == addrs || 0 == addrs.length) {
            return Collections.emptyList();
        }

        List<Address> addresses = new ArrayList<Address>(addrs.length);
        for (InternetAddress addr : addrs) {
            Address address = convertAddress(addr);
            if (null != address) {
                addresses.add(address);
            }
        }
        return addresses;
    }

    /**
     * Converts first given Internet email addresses to an {@link Address} instance.
     *
     * @param addr The Internet email addresses to convert
     * @return The resulting {@code Address} instance
     */
    public static Address convertFirstAddress(InternetAddress[] addresses) {
        List<Address> converted = convertAddresses(addresses);
        if (converted != null && !converted.isEmpty()) {
            return converted.get(0);
        }
        return null;
    }

    /**
     * Converts first given addresses header value to an {@link Address} instance.
     *
     * @param header The header value
     * @return The address or <code>null</code>
     */
    private static Address convertAddressHeader(String header) {
        if (header == null) {
            return null;
        }
        try {
            InternetAddress[] parsed = QuotedInternetAddress.parseHeader(header, true);
            return convertFirstAddress(parsed);
        } catch (AddressException e) {
            LOG.debug("Got invalid address header value: {}", header);
        }
        return null;
    }

    private static Map<String, String> convertCustomHeaders(MimeMessage mimeMessage) throws MessagingException {
        String headerValue = HeaderUtility.decodeHeaderValue(MimeMessageConverter.getStringHeader(HeaderUtility.HEADER_X_OX_CUSTOM_HEADERS, mimeMessage));
        return HeaderUtility.headerValue2CustomHeaders(headerValue);
    }


    // ---------------------------------------- MESSAGE PERMUTATION ----------------------------------------

    /**
     * Applies all updated fields from given {@link MessageDescription} to the internal {@link MimeMessage} instance.
     * <p>
     * <b>Note</b>: Attachments are <b>not</b> considered!
     *
     * @param draftMessage The composition space update
     * @throws OXException
     */
    private void applyUpdate0(MessageDescription draftMessage) throws OXException {
        try {
            applyDraftFlag(mimeMessage);
            applyCompositionSpaceHeaders(compositionSpaceId, draftMessage, mimeMessage);
            applyAddressHeaders(draftMessage, mimeMessage);
            applySubject(draftMessage, mimeMessage);
            applyPriority(draftMessage, mimeMessage);

            if (draftMessage.containsContent()) {
                setContent0(draftMessage.containsContentType() ? draftMessage.getContentType() : null, draftMessage.getContent());
            }
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    private void setContent0(ContentType contentType, String content) throws OXException {
        if (contentType != null) {
            this.contentType = contentType;
        }

        if (content == null) {
            this.contentForWeb = this.contentType.isImpliesHtml() ? getEmptyHtmlContent() : "";
            this.contentForDraft = this.contentForWeb;
        } else {
            this.contentForWeb = content;
            if (this.contentType.isImpliesHtml() && !attachments.isEmpty()) {
                this.contentForDraft = getHtmlContentForDraft(content);
            } else {
                this.contentForDraft = content;
            }
        }
    }

    private String getHtmlContentForDraft(String content) throws OXException {
        // If an inline image has been previously uploaded, it is only now referenced as such.
        // We need to scan the new content for image URLs and convert matching attachments to inline ones
        Map<UUID, ForwardingAttachmentIfNotSet> attachmentsById = getAttachmentsById();
        if (!attachmentsById.isEmpty()) {
            List<UUID> referencedIds = CompositionSpaces.getReferencedImageAttachmentIds(content, MailStorageCompositionSpaceImageDataSource.getInstance());
            for (UUID id : referencedIds) {
                ForwardingAttachmentIfNotSet attachment = attachmentsById.get(id);
                if (attachment != null) {
                    attachment.setDisposition(ContentDisposition.INLINE);
                }
            }
        }

        Map<UUID, ContentId> contentIdsByAttachmentIds = attachments.stream()
            .filter(a -> a.getContentIdAsObject() != null && a.getContentDisposition() == ContentDisposition.INLINE)
            .collect(toMap(a -> a.getId(), a -> a.getContentIdAsObject()));
        if (contentIdsByAttachmentIds.isEmpty()) {
            return content;
        }

        String adjustedContent = CompositionSpaces.replaceLinkedInlineImages(content, contentIdsByAttachmentIds, MailStorageCompositionSpaceImageDataSource.getInstance());
        adjustedContent = getHtmlService().getConformHTML(adjustedContent, MailProperties.getInstance().getDefaultMimeCharset());
        return adjustedContent;
    }

    private static void applyDraftFlag(MimeMessage mimeMessage) throws MessagingException {
        Flags msgFlags = new Flags();
        msgFlags.add(Flags.Flag.DRAFT);
        mimeMessage.setFlags(msgFlags, true);
    }

    private static void applyCompositionSpaceHeaders(UUID compositionSpaceId, MessageDescription draftMessage, MimeMessage mimeMessage) throws MessagingException, OXException {
        mimeMessage.setHeader(HeaderUtility.HEADER_X_OX_COMPOSITION_SPACE_ID,
            MimeMessageUtility.forceFold(HeaderUtility.HEADER_X_OX_COMPOSITION_SPACE_ID.length() + 2, UUIDs.getUnformattedString(compositionSpaceId)));
        if (draftMessage.containsContentType() && draftMessage.getContentType() != null) {
            mimeMessage.setHeader(HeaderUtility.HEADER_X_OX_CONTENT_TYPE, HeaderUtility.encodeHeaderValue(19, draftMessage.getContentType().getId()));
        }
        if (draftMessage.containsMeta() && draftMessage.getMeta() != null) {
            mimeMessage.setHeader(HeaderUtility.HEADER_X_OX_META, HeaderUtility.encodeHeaderValue(11, HeaderUtility.meta2HeaderValue(draftMessage.getMeta())));
        }
        if (draftMessage.containsSecurity() && draftMessage.getSecurity() != null) {
            mimeMessage.setHeader(HeaderUtility.HEADER_X_OX_SECURITY, HeaderUtility.encodeHeaderValue(15, HeaderUtility.security2HeaderValue(draftMessage.getSecurity())));
        }
        if (draftMessage.containsSharedAttachmentsInfo() && draftMessage.getSharedAttachmentsInfo() != null) {
            mimeMessage.setHeader(HeaderUtility.HEADER_X_OX_SHARED_ATTACHMENTS, HeaderUtility.encodeHeaderValue(25, HeaderUtility.sharedAttachments2HeaderValue(draftMessage.getSharedAttachmentsInfo())));
        }
        if (draftMessage.isRequestReadReceipt()) {
            mimeMessage.setHeader(HeaderUtility.HEADER_X_OX_READ_RECEIPT, HeaderUtility.encodeHeaderValue(19, "true"));
            if (draftMessage.getFrom() != null) {
                mimeMessage.setHeader(MessageHeaders.HDR_X_OX_NOTIFICATION, toMimeAddress(draftMessage.getFrom()).toString());
            }
        }
        if (draftMessage.getCustomHeaders() != null) {
            mimeMessage.setHeader(HeaderUtility.HEADER_X_OX_CUSTOM_HEADERS, HeaderUtility.encodeHeaderValue(19, HeaderUtility.customHeaders2HeaderValue(draftMessage.getCustomHeaders())));
        }
        if (draftMessage.containsValidClientToken()) {
            mimeMessage.setHeader(HeaderUtility.HEADER_X_OX_CLIENT_TOKEN, HeaderUtility.encodeHeaderValue(HeaderUtility.HEADER_X_OX_CLIENT_TOKEN.length() + 2,
                draftMessage.getClientToken().toString()));
        }
    }

    private void applySharedFolderReferenceHeader(SharedFolderReference sharedFolderRef, MimeMessage mimeMessage) throws MessagingException {
        mimeMessage.setHeader(HeaderUtility.HEADER_X_OX_SHARED_FOLDER_REFERENCE, HeaderUtility.encodeHeaderValue(30, HeaderUtility.sharedFolderReference2HeaderValue(sharedFolderRef)));
    }

    private void applyCompositionSpaceHeadersForNew(UUID compositionSpaceId, ContentType contentType, Optional<SharedFolderReference> optionalSharedFolderRef, ClientToken clientToken, MimeMessage mimeMessage) throws MessagingException {
        mimeMessage.setHeader(HeaderUtility.HEADER_X_OX_COMPOSITION_SPACE_ID, MimeMessageUtility.forceFold(
            HeaderUtility.HEADER_X_OX_COMPOSITION_SPACE_ID.length() + 2, UUIDs.getUnformattedString(compositionSpaceId)));
        mimeMessage.setHeader(HeaderUtility.HEADER_X_OX_CONTENT_TYPE, HeaderUtility.encodeHeaderValue(19, contentType.getId()));
        mimeMessage.setHeader(HeaderUtility.HEADER_X_OX_META, HeaderUtility.encodeHeaderValue(11, HeaderUtility.meta2HeaderValue(Meta.META_NEW)));
        if (optionalSharedFolderRef.isPresent()) {
            applySharedFolderReferenceHeader(optionalSharedFolderRef.get(), mimeMessage);
        }
        mimeMessage.setSentDate(new Date());
        mimeMessage.setHeader(HeaderUtility.HEADER_X_OX_CLIENT_TOKEN, HeaderUtility.encodeHeaderValue(HeaderUtility.HEADER_X_OX_CLIENT_TOKEN.length() + 2, clientToken.toString()));
    }

    private void applySubject(MessageDescription draftMessage, MimeMessage mimeMessage) throws MessagingException, OXException {
        if (draftMessage.containsSubject()) {
            String newSubject = draftMessage.getSubject();

            // Check if a shared attachments folder has already been created and needs to be renamed
            SharedAttachmentsInfo sharedAttachmentsInfo = convertSharedAttachmentsInfo(mimeMessage);
            if (sharedAttachmentsInfo != null && sharedAttachmentsInfo.isEnabled()) {
                SharedFolderReference sharedFolderRef = convertSharedFolderReference(mimeMessage);
                if (sharedFolderRef != null) {
                    AttachmentStorage attachmentStorage = getAttachmentStorage();
                    String folderName = SharedAttachmentsUtils.buildFolderName(newSubject, true, session);
                    attachmentStorage.renameFolder(folderName, sharedFolderRef.getFolderId(), session);
                }
            }

            if (newSubject == null) {
                // Drop subject
                mimeMessage.setSubject(null, "UTF-8");
            } else {
                // Apply new subject
                mimeMessage.setSubject(newSubject, "UTF-8");
            }
        }
    }

    private static void applyAddressHeaders(MessageDescription draftMessage, MimeMessage mimeMessage) throws MessagingException, OXException {
        if (draftMessage.containsFrom()) {
            mimeMessage.setFrom(toMimeAddress(draftMessage.getFrom()));
        }

        if (draftMessage.containsSender()) {
            mimeMessage.setSender(toMimeAddress(draftMessage.getSender()));
        }

        if (draftMessage.containsReplyTo()) {
            Address replyTo = draftMessage.getReplyTo();
            if (replyTo == null) {
                mimeMessage.setReplyTo(null);
            } else {
                mimeMessage.setReplyTo(new javax.mail.Address[] { toMimeAddress(replyTo) });
            }
        }

        if (draftMessage.containsTo()) {
            mimeMessage.setRecipients(MimeMessage.RecipientType.TO, toMimeAddresses(draftMessage.getTo()));
        }

        if (draftMessage.containsCc()) {
            mimeMessage.setRecipients(MimeMessage.RecipientType.CC, toMimeAddresses(draftMessage.getCc()));
        }

        if (draftMessage.containsBcc()) {
            mimeMessage.setRecipients(MimeMessage.RecipientType.BCC, toMimeAddresses(draftMessage.getBcc()));
        }
    }

    private static void applyPriority(MessageDescription draftMessage, MimeMessage mimeMessage) throws MessagingException {
        if (draftMessage.containsPriority()) {
            Priority priority = draftMessage.getPriority();
            if (priority == null) {
                priority = Priority.NORMAL;
            }

            mimeMessage.setHeader(MessageHeaders.HDR_X_PRIORITY, String.valueOf(priority.getLevel()));
            if (Priority.NORMAL == priority) {
                mimeMessage.setHeader(MessageHeaders.HDR_IMPORTANCE, "Normal");
            } else if (Priority.LOW == priority) {
                mimeMessage.setHeader(MessageHeaders.HDR_IMPORTANCE, "Low");
            } else {
                mimeMessage.setHeader(MessageHeaders.HDR_IMPORTANCE, "High");
            }
        }
    }

    private static InternetAddress[] toMimeAddresses(List<Address> addrs) throws OXException {
        if (null == addrs) {
            return null;
        }

        int numberOfAddresses = addrs.size();
        switch (numberOfAddresses) {
            case 0:
                return new InternetAddress[0];
            case 1: {
                Address address = addrs.get(0);
                return address == null ? new InternetAddress[0] : new InternetAddress[] { toMimeAddress(address) };
            }
            default: {
                List<InternetAddress> mimeAddresses = new ArrayList<>(numberOfAddresses);
                for (Address address : addrs) {
                    InternetAddress mimeAddress = toMimeAddress(address);
                    if (null != mimeAddress) {
                        mimeAddresses.add(mimeAddress);
                    }
                }
                return mimeAddresses.toArray(new InternetAddress[mimeAddresses.size()]);
            }
        }
    }

    private static InternetAddress toMimeAddress(Address a) throws OXException {
        if (null == a) {
            return null;
        }
        try {
            QuotedInternetAddress mimeAddress = new QuotedInternetAddress(a.getAddress(), true);
            mimeAddress.setPersonal(a.getPersonal(), "UTF-8");
            return mimeAddress;
        } catch (UnsupportedEncodingException e) {
            // Nah...
            throw OXException.general("UTF-8 charset not available", e);
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }


    // ---------------------------------------- COMPOSED MESSAGE CREATION ----------------------------------------

    /**
     * Applies all local in-memory changes to the MIME message representation and tries to store
     * it as self-contained file in the cache directory. As a result, {@link #mimeMessage}
     * will point to the new file and can be used for transport or saving a new draft in
     * mail store.
     *
     * @param asFinalDraft <code>true</code> if the compiled message is supposed to be stored
     *        due to an explicit "save as draft" request. <code>false</code> if message is used for
     *        transport.
     * @throws OXException
     */
    private void compileDraft0(boolean asFinalDraft) throws OXException {
        // Apply outstanding changes to MIME Message representation
        compileMimeMessage(asFinalDraft ? CompileMode.FINAL_DRAFT : CompileMode.COMPOSITION_SPACE);

        Result cacheResult = cacheMessage();
        if (cacheResult.success()) {
            reInitFromFileCacheReference(cacheResult.getFileCacheReference());
        } else {
            LOG.debug("Did not cache compiled message for reason: {}", cacheResult.getErrorReason(), cacheResult.getException());
        }
    }

    private Result cacheMessage() {
        CacheManagerFactory cacheManagerFactory = services.getOptionalService(CacheManagerFactory.class);
        if (cacheManagerFactory == null) {
            return Result.disabledResult();
        }

        try {
            CacheManager cacheManager = cacheManagerFactory.getCacheManager();
            return cacheManager.cacheMessage(compositionSpaceId, mimeMessage);
        } catch (Exception e) {
            return Result.exceptionResultFor(e);
        }
    }

    private void reInitFromFileCacheReference(CacheReference newReference) throws OXException {
        MimeMessage oldMessage = this.mimeMessage;
        CacheReference oldReference = this.cacheReference;
        try {
            this.mimeMessage = new FileBackedMimeMessage(MimeDefaultSession.getDefaultSession(), newReference.getMimeStream());
            this.cacheReference = newReference;

            MailMessage newMailMessage = MimeMessageConverter.convertMessage(mimeMessage, false);
            if (isMultipartMessage()) {
                // need to re-create attachments to swap their data providers accordingly
                ParsedAttachments parseAttachments = parseAttachments(newMailMessage, false);
                this.attachments.clear();
                this.attachments.addAll(parseAttachments.attachments);
            }
        } catch (IOException e) {
            throw CompositionSpaceErrorCode.IO_ERROR.create(e, e.getMessage());
        } catch (MessagingException e) {
            throw CompositionSpaceErrorCode.IO_ERROR.create(e, e.getMessage());
        } finally {
            // internal message was successfully re-initialized
            if (oldMessage != this.mimeMessage && oldMessage instanceof MimeCleanUp) {
                ((MimeCleanUp) oldMessage).cleanUp();
            }

            if (oldReference != null && oldReference != this.cacheReference) {
                oldReference.cleanUp();
            }
        }
    }

    private ComposeRequest compileTransportMessage0(AJAXRequestData request, Optional<MailMessage> optRefMessage) throws OXException {
        try {
            // Check From address
            InternetAddress[] from = MimeMessageUtility.getAddressHeader(MessageHeaders.HDR_FROM, mimeMessage);
            if (from == null || from.length == 0) {
                throw MailExceptionCode.MISSING_FIELD.create(MailJSONField.FROM.getKey());
            }
            InternetAddress fromAddresss = from[0];

            // Determine the account identifier by From address
            int accountId;
            try {
                accountId = MimeMessageFiller.resolveFrom2Account(session, fromAddresss, true, true);
            } catch (OXException e) {
                if (MailExceptionCode.NO_TRANSPORT_SUPPORT.equals(e) || MailExceptionCode.INVALID_SENDER.equals(e)) {
                    // Re-throw
                    throw e;
                }
                LOG.warn("{}. Using default account's transport.", e.getMessage());
                // Send with default account's transport provider
                accountId = MailAccount.DEFAULT_ID;
            }

            // Create a new compose message
            TransportProvider provider = TransportProviderRegistry.getTransportProviderBySession(session, accountId);
            ComposedMailMessage sourceMessage = provider.getNewComposedMailMessage(session, session.getContext());
            sourceMessage.setAccountId(accountId);
            MessageDescription draftMessage = getCurrentDraft0();
            fillComposedMailMessage(request, sourceMessage, draftMessage, optRefMessage);

            // Create a new text part instance
            TextBodyMailPart textPart = provider.getNewTextBodyPart(this.contentForDraft);
            textPart.setContentType(this.contentType.getId());
            if (!this.contentType.isImpliesHtml()) {
                textPart.setPlainText(this.contentForDraft);
            }

            // Check for shared attachments
            SharedAttachmentsInfo sharedAttachmentsInfo = draftMessage.getSharedAttachmentsInfo();
            Map<String, Object> parameters;
            SharedFolderReference sharedFolderRef;
            if (sharedAttachmentsInfo != null && sharedAttachmentsInfo.isEnabled()) {
                // Check permission
                if (false == mayShareAttachments()) {
                    // User wants to share attachments, but is not allowed to do so
                    throw MailExceptionCode.SHARING_NOT_POSSIBLE.create(I(session.getUserId()), I(session.getContextId()));
                }

                // Check identifier of shared attachments folder
                sharedFolderRef = convertSharedFolderReference(mimeMessage);

                // Create sharing parameters
                parameters = getSharingParameters(sharedAttachmentsInfo, sharedFolderRef);
            } else {
                sharedFolderRef = null;
                parameters = Collections.emptyMap();
            }

            List<MailPart> parts = new ArrayList<>(attachments.size());
            for (ForwardingAttachmentIfNotSet attachment : attachments) {
                // Only take over attachments that are not shared
                if (attachment.getSharedAttachmentReference() == null) {
                    MimeBodyPart mimePart = convertAttachmentToMimeBodyPart(attachment, CompileMode.TRANSPORT);
                    ReferencedMailPart referencedPart = provider.getNewReferencedPart(MimeMessageConverter.convertPart(mimePart), session);
                    parts.add(referencedPart);
                }
            }

            return new ComposeRequest(accountId, sourceMessage, textPart, parts, parameters, request, new LinkedList<>());
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }


    private Map<String, Object> getSharingParameters(SharedAttachmentsInfo sharedAttachmentsInfo, SharedFolderReference sharedFolderRef) throws OXException {
        ImmutableMap.Builder<String, Object> parameters = ImmutableMap.builderWithExpectedSize(1);
        try {
            JSONObject jShareAttachmentOptions = new JSONObject(6);
            jShareAttachmentOptions.put("enable", sharedAttachmentsInfo.isEnabled());
            jShareAttachmentOptions.put("autodelete", sharedAttachmentsInfo.isAutoDelete());
            String password = sharedAttachmentsInfo.getPassword();
            if (password != null) {
                jShareAttachmentOptions.put("password", password);
            }
            Date expiryDate = sharedAttachmentsInfo.getExpiryDate();
            if (expiryDate != null) {
                jShareAttachmentOptions.put("expiry_date", expiryDate.getTime());
            }
            if (sharedFolderRef != null) {
                jShareAttachmentOptions.put("folder", sharedFolderRef.getFolderId());
            }

            parameters.put("share_attachments", jShareAttachmentOptions);
        } catch (JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
        return parameters.build();
    }

    private static final String CAPABILITY_SHARE_MAIL_ATTACHMENTS = "share_mail_attachments";

    /**
     * Checks if session-associated user is allowed to share mail attachments.
     *
     * @return <code>true</code> if allowed; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    public boolean mayShareAttachments() throws OXException {
        CapabilityService capabilityService = services.getOptionalService(CapabilityService.class);
        return null == capabilityService ? false : capabilityService.getCapabilities(session).contains(CAPABILITY_SHARE_MAIL_ATTACHMENTS);
    }

    private void fillComposedMailMessage(AJAXRequestData request, ComposedMailMessage sourceMessage, MessageDescription draftMessage, Optional<MailMessage> optRefMessage) throws OXException {
        applyAddressHeadersToMailMessage(draftMessage, sourceMessage);
        applyCustomHeadersToMailMessage(draftMessage, sourceMessage);
        // Set References and In-Reply-To headers
        if (optRefMessage.isPresent()) {
            setReplyHeaders(optRefMessage.get(), sourceMessage);
        }
        applySubjectToMailMessage(draftMessage, sourceMessage);
        applyPriorityToMailMessage(draftMessage, sourceMessage);
        applySecurityToMailMessage(request, draftMessage, sourceMessage);
        applyRequestReceivedReceiptToMailMessage(draftMessage, sourceMessage);
        sourceMessage.setSentDate(new Date());
        sourceMessage.setContentType(this.contentType.getId());
    }

    private void compileMimeMessage(CompileMode compileMode) throws OXException {
        boolean isHtml = this.contentType.isImpliesHtml();
        String charset = MailProperties.getInstance().getDefaultMimeCharset();
        try {
            // Fill attachments into MIME message
            if (attachments != null && !attachments.isEmpty()) {
                fillMessageWithAttachments(mimeMessage, charset, isHtml, compileMode);
            } else {
                fillMessageWithoutAttachments(mimeMessage, charset, isHtml);
            }

            // Drop inappropriate header
            switch (compileMode) {
                case TRANSPORT:
                    cleanCompositionSpaceHeaders(true);
                    break;
                case FINAL_DRAFT:
                    cleanCompositionSpaceHeaders(false);
                    break;
                default:
                    // Nothing
                    break;
            }

            // Update last modified
            mimeMessage.setSentDate(new Date());

            // Save changes to MIME message
            mimeMessage.saveChanges();
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    /**
     * Cleans composition space headers
     *
     * @param transportMessage Whether this operation has been invoked to yield a MIME message ready for being transported
     */
    private void cleanCompositionSpaceHeaders(boolean forTransport) {
        try {
            mimeMessage.removeHeader(HeaderUtility.HEADER_X_OX_COMPOSITION_SPACE_ID);
            mimeMessage.removeHeader(MessageHeaders.HDR_X_OX_NOTIFICATION);
            mimeMessage.removeHeader(MessageHeaders.HDR_X_OX_CLIENT_TOKEN);
            if (forTransport) {
                mimeMessage.removeHeader(HeaderUtility.HEADER_X_OX_CONTENT_TYPE);
                mimeMessage.removeHeader(HeaderUtility.HEADER_X_OX_META);
                mimeMessage.removeHeader(HeaderUtility.HEADER_X_OX_SECURITY);
                mimeMessage.removeHeader(HeaderUtility.HEADER_X_OX_SHARED_ATTACHMENTS);
                mimeMessage.removeHeader(HeaderUtility.HEADER_X_OX_SHARED_FOLDER_REFERENCE);
                mimeMessage.removeHeader(HeaderUtility.HEADER_X_OX_READ_RECEIPT);
                mimeMessage.removeHeader(HeaderUtility.HEADER_X_OX_CUSTOM_HEADERS);
            }
        } catch (MessagingException e) {
            LOG.warn("Error while cleaning composition space headers from message", e);
        }
    }

    private void fillMessageWithoutAttachments(MimeMessage mimeMessage, String charset, boolean isHtml) throws MessagingException {
        MessageUtility.setText(this.contentForDraft, charset, isHtml ? "html" : "plain", mimeMessage);
        mimeMessage.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
        mimeMessage.setHeader(MessageHeaders.HDR_CONTENT_TYPE, new StringBuilder(24).append("text/").append(isHtml ? "html" : "plain").append("; charset=").append(charset).toString());
        if (CharMatcher.ascii().matchesAllOf(this.contentForDraft)) {
            mimeMessage.setHeader(MessageHeaders.HDR_CONTENT_TRANSFER_ENC, "7bit");
        }
    }

    private HtmlService getHtmlService() throws OXException {
        return services.getServiceSafe(HtmlService.class);
    }

    private void fillMessageWithAttachments(MimeMessage mimeMessage, String charset, boolean isHtml, CompileMode compileMode) throws OXException, MessagingException {
        String content = this.contentForDraft;
        if (isHtml) {
            List<ForwardingAttachmentIfNotSet> fileAttachments = this.attachments.stream()
                .filter(a -> a.getContentDisposition() == ContentDisposition.ATTACHMENT)
                .collect(toList());
            Map<ContentId, ForwardingAttachmentIfNotSet> contentId2InlineAttachment = this.attachments.stream()
                .filter(a -> a.getContentDisposition() == ContentDisposition.INLINE)
                .collect(toMap(a -> a.getContentIdAsObject(), a -> a));

            Multipart primaryMultipart;
            if (contentId2InlineAttachment.isEmpty()) {
                // No inline images.
                if (fileAttachments.isEmpty()) {
                    // No file attachments as well
                    fillMessageWithoutAttachments(mimeMessage, charset, isHtml);
                    return;
                }

                // A simple multipart draftMessage
                primaryMultipart = new MimeMultipart();

                // Add text part
                primaryMultipart.addBodyPart(createHtmlBodyPart(content, charset));

                // Add attachments
                for (ForwardingAttachmentIfNotSet attachment : fileAttachments) {
                    addAttachment(attachment, primaryMultipart, compileMode);
                }
            } else {
                if (fileAttachments.isEmpty()) {
                    // Only inline images
                    primaryMultipart = createMultipartRelated(content, charset, contentId2InlineAttachment, compileMode);
                } else {
                    // Both - file attachments and inline images
                    primaryMultipart = new MimeMultipart();

                    // Add multipart/related
                    BodyPart altBodyPart = new MimeBodyPart();
                    MessageUtility.setContent(createMultipartRelated(content, charset, contentId2InlineAttachment, compileMode), altBodyPart);
                    primaryMultipart.addBodyPart(altBodyPart);

                    // Add remaining file attachments
                    for (ForwardingAttachmentIfNotSet fileAttachment : fileAttachments) {
                        addAttachment(fileAttachment, primaryMultipart, compileMode);
                    }
                }
            }

            mimeMessage.setContent(primaryMultipart);
        } else {
            // A plain-text draftMessage
            Multipart primaryMultipart = new MimeMultipart();

            // Add text part
            primaryMultipart.addBodyPart(createTextBodyPart(performLineFolding(content, 0), charset));

            // Add attachments
            for (ForwardingAttachmentIfNotSet attachment : this.attachments) {
                addAttachment(attachment, primaryMultipart, compileMode);
            }

            mimeMessage.setContent(primaryMultipart);
        }
    }

    private Multipart createMultipartRelated(String wellFormedHTMLContent, String charset, Map<ContentId, ForwardingAttachmentIfNotSet> contentId2InlineAttachment, CompileMode compileMode) throws MessagingException, OXException {
        Multipart relatedMultipart = new MimeMultipart("related");

        relatedMultipart.addBodyPart(createHtmlBodyPart(wellFormedHTMLContent, charset), 0);

        for (ForwardingAttachmentIfNotSet inlineImage : contentId2InlineAttachment.values()) {
            addAttachment(inlineImage, relatedMultipart, compileMode);
        }

        return relatedMultipart;
    }

    /**
     * Creates a body part of type <code>text/html</code> from given HTML content
     *
     * @param wellFormedHTMLContent The well-formed HTML content
     * @param charset The charset
     * @return A body part of type <code>text/html</code> from given HTML content
     * @throws MessagingException If a messaging error occurs
     * @throws OXException If a processing error occurs
     */
    private BodyPart createHtmlBodyPart(final String wellFormedHTMLContent, final String charset) throws MessagingException, OXException {
        try {
            final String contentType = new StringBuilder("text/html; charset=").append(charset).toString();
            final MimeBodyPart html = new MimeBodyPart();
            html.setDataHandler(new DataHandler(new MessageDataSource(wellFormedHTMLContent, contentType)));
            html.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
            html.setHeader(MessageHeaders.HDR_CONTENT_TYPE, contentType);
            return html;
        } catch (UnsupportedEncodingException e) {
            throw new MessagingException("Unsupported encoding.", e);
        }
    }

    /**
     * Creates a body part of type <code>text/plain</code> for given content
     *
     * @param content The content
     * @param charset The character encoding
     * @return A body part of type <code>text/plain</code>
     * @throws MessagingException If a messaging error occurs
     */
    private BodyPart createTextBodyPart(String content, String charset) throws MessagingException {
        MimeBodyPart text = new MimeBodyPart();
        MessageUtility.setText(performLineFolding(content, 0), charset, text);
        text.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
        text.setHeader(MessageHeaders.HDR_CONTENT_TYPE, new StringBuilder("text/plain; charset=").append(charset).toString());
        if (CharMatcher.ascii().matchesAllOf(content)) {
            text.setHeader(MessageHeaders.HDR_CONTENT_TRANSFER_ENC, "7bit");
        }
        return text;
    }

    private void addAttachment(ForwardingAttachmentIfNotSet attachment, Multipart multiPart, CompileMode compileMode) throws MessagingException, OXException {
        MimeBodyPart draftMessageBodyPart = convertAttachmentToMimeBodyPart(attachment, compileMode);
        multiPart.addBodyPart(draftMessageBodyPart);
    }

    private MimeBodyPart convertAttachmentToMimeBodyPart(ForwardingAttachmentIfNotSet attachment, CompileMode compileMode) throws MessagingException, OXException {
        com.openexchange.mail.mime.ContentType ct = new com.openexchange.mail.mime.ContentType(attachment.getMimeType());
        if (ct.startsWith(MimeTypes.MIME_MESSAGE_RFC822)) {
            return convertNestedMessage(attachment, compileMode);
        }

        SharedAttachmentReference sharedAttachmentRef = null;
        if (compileMode != CompileMode.TRANSPORT) {
            sharedAttachmentRef = attachment.getSharedAttachmentReference();
        }

        String fileName = attachment.getName();

        // Create MIME body part...
        MimeBodyPart draftMessageBodyPart = new MimeBodyPart();

        // ... and set its content
        {
            DataSource dataSource = sharedAttachmentRef == null ? new AttachmentDataSource(attachment) : new ByteArrayDataSource(new byte[0], ct.toString());
            draftMessageBodyPart.setDataHandler(new DataHandler(dataSource));
        }

        // Content-Type
        if (fileName != null && !ct.containsNameParameter()) {
            ct.setNameParameter(fileName);
        }
        draftMessageBodyPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MimeMessageUtility.foldContentType(ct.toString()));

        // Content-Transfer-Encoding
        if (ContentDisposition.INLINE != attachment.getContentDisposition()) {
            // Force base64 encoding to keep data as it is
            draftMessageBodyPart.setHeader(MessageHeaders.HDR_CONTENT_TRANSFER_ENC, "base64");
        }

        // Content-Disposition
        com.openexchange.mail.mime.ContentDisposition cd = new com.openexchange.mail.mime.ContentDisposition(attachment.getContentDisposition().getId());
        if (fileName != null) {
            cd.setFilenameParameter(fileName);
        }
        draftMessageBodyPart.setHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, MimeMessageUtility.foldContentDisposition(cd.toString()));

        // Content-ID
        ContentId contentId = attachment.getContentIdAsObject();
        if (contentId != null && (ContentDisposition.INLINE == attachment.getContentDisposition() || compileMode != CompileMode.TRANSPORT)) {
            // set not only for inline, despite 'skipAttachmentHeaders=true'
            draftMessageBodyPart.setContentID(contentId.getContentIdForHeader());
        }

        // Id
        draftMessageBodyPart.setHeader(MessageHeaders.HDR_X_PART_ID, UUIDs.getUnformattedString(attachment.getId()));

        // Origin
        if (compileMode == CompileMode.COMPOSITION_SPACE) {
            draftMessageBodyPart.setHeader(MessageHeaders.HDR_X_OX_ATTACHMENT_ORIGIN, attachment.getOrigin().getIdentifier());
        }

        // vCard
        if (AttachmentOrigin.VCARD == attachment.getOrigin() && compileMode != CompileMode.TRANSPORT) {
            draftMessageBodyPart.setHeader(MessageHeaders.HDR_X_OX_VCARD, new StringBuilder(16).append(session.getUserId()).append('@').append(session.getContextId()).toString());
        }

        // Shared attachment reference
        if (sharedAttachmentRef != null) {
            draftMessageBodyPart.setHeader(HeaderUtility.HEADER_X_OX_SHARED_ATTACHMENT_REFERENCE, HeaderUtility.encodeHeaderValue(24, HeaderUtility.sharedAttachmentReference2HeaderValue(sharedAttachmentRef)));
        }

        return draftMessageBodyPart;
    }

    private MimeBodyPart convertNestedMessage(ForwardingAttachmentIfNotSet attachment, CompileMode compileMode) throws MessagingException, OXException {
        String fileName;
        if (null == attachment.getName()) {
            InputStream data = null;
            try {
                data = attachment.getData();
                String subject = MimeMessageUtility.checkNonAscii(new InternetHeaders(data).getHeader(MessageHeaders.HDR_SUBJECT, null));
                if (null == subject || subject.length() == 0) {
                    fileName = "part.eml";
                } else {
                    subject = MimeMessageUtility.decodeMultiEncodedHeader(MimeMessageUtility.unfold(subject));
                    fileName = subject.replaceAll("\\p{Blank}+", "_") + ".eml";
                }
            } finally {
                Streams.close(data);
            }
        } else {
            fileName = attachment.getName();
        }

        SharedAttachmentReference sharedAttachmentRef = null;
        if (compileMode != CompileMode.TRANSPORT) {
            sharedAttachmentRef = attachment.getSharedAttachmentReference();
        }

        // Create MIME body part...
        MimeBodyPart draftMessageBodyPart = new MimeBodyPart();

        // ... and set its content
        {
            DataSource dataSource = sharedAttachmentRef == null ? new AttachmentDataSource(attachment, MimeTypes.MIME_MESSAGE_RFC822) : new ByteArrayDataSource(new byte[0], MimeTypes.MIME_MESSAGE_RFC822);
            draftMessageBodyPart.setDataHandler(new DataHandler(dataSource));
        }

        // Content-Type
        com.openexchange.mail.mime.ContentType ct = new com.openexchange.mail.mime.ContentType(MimeTypes.MIME_MESSAGE_RFC822);
        if (fileName != null) {
            ct.setNameParameter(fileName);
        }
        draftMessageBodyPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MimeMessageUtility.foldContentType(ct.toString()));

        // Content-Disposition
        com.openexchange.mail.mime.ContentDisposition cd = new com.openexchange.mail.mime.ContentDisposition(attachment.getContentDisposition().getId());
        if (fileName != null) {
            cd.setFilenameParameter(fileName);
        }
        draftMessageBodyPart.setHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, MimeMessageUtility.foldContentDisposition(cd.toString()));

        // Content-ID
        ContentId contentId = attachment.getContentIdAsObject();
        if (contentId != null) {
            draftMessageBodyPart.setContentID(contentId.getContentIdForHeader());
        }

        // Id
        draftMessageBodyPart.setHeader(MessageHeaders.HDR_X_PART_ID, UUIDs.getUnformattedString(attachment.getId()));

        // Shared attachment reference
        if (sharedAttachmentRef != null) {
            draftMessageBodyPart.setHeader(HeaderUtility.HEADER_X_OX_SHARED_ATTACHMENT_REFERENCE, HeaderUtility.encodeHeaderValue(24, HeaderUtility.sharedAttachmentReference2HeaderValue(sharedAttachmentRef)));
        }

        return draftMessageBodyPart;
    }

    private static String extensionFor(String fileName) {
        if (null == fileName) {
            return null;
        }

        int pos = fileName.lastIndexOf('.');
        return Strings.asciiLowerCase(pos > 0 ? fileName.substring(pos + 1) : fileName);
    }


    // ---------------------------------------- COMPOSED MESSAGE CONVERTERS --------------------

    /**
     * Private method to pull the security settings from given draftMessage.
     *
     * @param draftMessage The draftMessage from which to pull security settings
     * @param optRequest The optional AJAX request if authentication should be generated, may be <code>null</code>
     * @return The security settings if any present and set, otherwise <code>null</code>
     * @throws OXException If security settings cannot be returned
     */
    private SecuritySettings getSecuritySettings(MessageDescription draftMessage, AJAXRequestData optRequest) throws OXException {
        Security security = draftMessage.getSecurity();
        return getSecuritySettings(security, optRequest);
    }

    /**
     * Private method to pull the security settings from given draftMessage.
     *
     * @param security The security settings
     * @param optRequest The optional AJAX request if authentication should be generated, may be <code>null</code>
     * @return The security settings if any present and set, otherwise <code>null</code>
     * @throws OXException If security settings cannot be returned
     */
    private SecuritySettings getSecuritySettings(Security security, AJAXRequestData optRequest) throws OXException {
        if (null != security && false == security.isDisabled()) {
            String authentication = null;
            if (optRequest != null) {
                CryptographicServiceAuthenticationFactory authenticationFactory = services.getOptionalService(CryptographicServiceAuthenticationFactory.class);
                if (authenticationFactory != null) {
                    authentication = authenticationFactory.createAuthenticationFrom(optRequest);
                }
            }

            SecuritySettings settings = SecuritySettings.builder()
                .encrypt(security.isEncrypt())
                .pgpInline(security.isPgpInline())
                .sign(security.isSign())
                .authentication(authentication)
                .guestLanguage(security.getLanguage())
                .guestMessage(security.getMessage())
                .pin(security.getPin())
                .msgRef(security.getMsgRef())
                .build();
            if (settings.anythingSet()) {
                return settings;
            }
        }
        return null;
    }

    private static void applyDraftFlagToMailMessage(MailMessage mailMessage) throws OXException {
        mailMessage.setFlag(MailMessage.FLAG_DRAFT, true);
    }

    private static void applyCompositionSpaceHeadersToMailMessage(UUID compositionSpaceId, MessageDescription draftMessage, MailMessage mailMessage) throws OXException {
        mailMessage.setHeader(HeaderUtility.HEADER_X_OX_COMPOSITION_SPACE_ID, MimeMessageUtility.forceFold(
            HeaderUtility.HEADER_X_OX_COMPOSITION_SPACE_ID.length() + 2, UUIDs.getUnformattedString(compositionSpaceId)));
        if (draftMessage.containsContentType()) {
            mailMessage.setHeader(HeaderUtility.HEADER_X_OX_CONTENT_TYPE, HeaderUtility.encodeHeaderValue(19, draftMessage.getContentType().getId()));
        }
        if (draftMessage.containsMeta()) {
            mailMessage.setHeader(HeaderUtility.HEADER_X_OX_META, HeaderUtility.encodeHeaderValue(11, HeaderUtility.meta2HeaderValue(draftMessage.getMeta())));
        }
        if (draftMessage.containsSecurity()) {
            mailMessage.setHeader(HeaderUtility.HEADER_X_OX_SECURITY, HeaderUtility.encodeHeaderValue(15, HeaderUtility.security2HeaderValue(draftMessage.getSecurity())));
        }
        if (draftMessage.containsSharedAttachmentsInfo()) {
            mailMessage.setHeader(HeaderUtility.HEADER_X_OX_SHARED_ATTACHMENTS, HeaderUtility.encodeHeaderValue(25, HeaderUtility.sharedAttachments2HeaderValue(draftMessage.getSharedAttachmentsInfo())));
        }
        if (draftMessage.isRequestReadReceipt()) {
            mailMessage.setHeader(HeaderUtility.HEADER_X_OX_READ_RECEIPT, HeaderUtility.encodeHeaderValue(19, "true"));
            if (draftMessage.getFrom() != null) {
                mailMessage.setHeader(MessageHeaders.HDR_X_OX_NOTIFICATION, toMimeAddress(draftMessage.getFrom()).toString());
            }
        }
        if (draftMessage.getCustomHeaders() != null) {
            mailMessage.setHeader(HeaderUtility.HEADER_X_OX_CUSTOM_HEADERS, HeaderUtility.encodeHeaderValue(19, HeaderUtility.customHeaders2HeaderValue(draftMessage.getCustomHeaders())));
        }
    }

    private static void applySubjectToMailMessage(MessageDescription draftMessage, MailMessage mailMessage) {
        if (draftMessage.getSubject() != null) {
            mailMessage.setSubject(draftMessage.getSubject());
        }
    }

     private static void applyAddressHeadersToMailMessage(MessageDescription draftMessage, MailMessage mailMessage) throws OXException {
        if (draftMessage.getFrom() != null) {
            mailMessage.removeFrom();
            mailMessage.addFrom(toMimeAddress(draftMessage.getFrom()));
        }

        if (draftMessage.getSender() != null) {
            mailMessage.setHeader(MessageHeaders.HDR_SENDER, toMimeAddress(draftMessage.getSender()).toUnicodeString());
        }

        if (draftMessage.getReplyTo() != null) {
            Address replyTo = draftMessage.getReplyTo();
            mailMessage.removeReplyTo();
            mailMessage.addReplyTo(toMimeAddress(replyTo));
        }

        if (draftMessage.getTo() != null) {
            mailMessage.removeTo();
            mailMessage.addTo(toMimeAddresses(draftMessage.getTo()));
        }

        if (draftMessage.getCc() != null) {
            mailMessage.removeCc();
            mailMessage.addCc(toMimeAddresses(draftMessage.getCc()));
        }

        if (draftMessage.getBcc() != null) {
            mailMessage.removeBcc();
            mailMessage.addBcc(toMimeAddresses(draftMessage.getBcc()));
        }
     }

    private static void applyPriorityToMailMessage(MessageDescription draftMessage, MailMessage mailMessage) {
        if (draftMessage.getPriority() != null) {
            Priority priority = draftMessage.getPriority();
            mailMessage.setHeader(MessageHeaders.HDR_X_PRIORITY, String.valueOf(priority.getLevel()));
            if (Priority.NORMAL == priority) {
                mailMessage.setHeader(MessageHeaders.HDR_IMPORTANCE, "Normal");
            } else if (Priority.LOW == priority) {
                mailMessage.setHeader(MessageHeaders.HDR_IMPORTANCE, "Low");
            } else {
                mailMessage.setHeader(MessageHeaders.HDR_IMPORTANCE, "High");
            }
        }
    }

    private void applySecurityToMailMessage(AJAXRequestData request, MessageDescription draftMessage, ComposedMailMessage mailMessage) throws OXException {
        SecuritySettings securitySettings = getSecuritySettings(draftMessage, request);
        if (securitySettings != null) {
            mailMessage.setSecuritySettings(securitySettings);
        }
    }

    private static void applyRequestReceivedReceiptToMailMessage(MessageDescription draftMessage, MailMessage mailMessage) throws OXException {
        if (draftMessage.isRequestReadReceipt()) {
            mailMessage.setDispositionNotification(toMimeAddress(draftMessage.getFrom()));
        }
    }

    private static void applyCustomHeadersToMailMessage(MessageDescription draftMessage, MailMessage mailMessage) {
        Map<String, String> customHeaders = draftMessage.getCustomHeaders();
        if (customHeaders != null) {
            for (Map.Entry<String, String> customHeader : customHeaders.entrySet()) {
                String headerName = customHeader.getKey();
                if (MimeMessageFiller.isCustomOrReplyHeader(headerName)) {
                    mailMessage.setHeader(headerName, customHeader.getValue());
                }
            }
        }
    }

    /**
     * Sets the appropriate headers <code>In-Reply-To</code> and <code>References</code> in specified MIME message.
     *
     * @param referencedMail The referenced mail
     * @param message The message to set in
     */
    private static void setReplyHeaders(MailMessage referencedMail, ComposedMailMessage message) {
        if (null == referencedMail) {
            /*
             * Obviously referenced mail does no more exist; cancel setting reply headers Message-Id, In-Reply-To, and References.
             */
            return;
        }
        final String pMsgId = referencedMail.getFirstHeader(MessageHeaders.HDR_MESSAGE_ID);
        if (pMsgId != null) {
            message.setHeader(MessageHeaders.HDR_IN_REPLY_TO, pMsgId);
        }
        /*
         * Set References header field
         */
        final String pReferences = referencedMail.getFirstHeader(MessageHeaders.HDR_REFERENCES);
        final String pInReplyTo = referencedMail.getFirstHeader(MessageHeaders.HDR_IN_REPLY_TO);
        final StringBuilder refBuilder = new StringBuilder();
        if (pReferences != null) {
            /*
             * The "References:" field will contain the contents of the parent's "References:" field (if any) followed by the contents of
             * the parent's "Message-ID:" field (if any).
             */
            refBuilder.append(pReferences);
        } else if (pInReplyTo != null) {
            /*
             * If the parent message does not contain a "References:" field but does have an "In-Reply-To:" field containing a single
             * message identifier, then the "References:" field will contain the contents of the parent's "In-Reply-To:" field followed by
             * the contents of the parent's "Message-ID:" field (if any).
             */
            refBuilder.append(pInReplyTo);
        }
        if (pMsgId != null) {
            if (refBuilder.length() > 0) {
                refBuilder.append(' ');
            }
            refBuilder.append(pMsgId);
            /*
             * If the parent has none of the "References:", "In-Reply-To:", or "Message-ID:" fields, then the new message will have no
             * "References:" field.
             */
            message.setHeader(MessageHeaders.HDR_REFERENCES, refBuilder.toString());
        } else if (refBuilder.length() > 0) {
            /*
             * If the parent has none of the "References:", "In-Reply-To:", or "Message-ID:" fields, then the new message will have no
             * "References:" field.
             */
            message.setHeader(MessageHeaders.HDR_REFERENCES, refBuilder.toString());
        }
    }


    // ---------------------------------------- HELPERS ----------------------------------------

    private boolean isMultipartMessage() throws OXException {
        try {
            String contentType = mimeMessage.getContentType();
            return contentType != null &&  Strings.asciiLowerCase(contentType).trim().startsWith("multipart/");
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    private Map<UUID, ForwardingAttachmentIfNotSet> getAttachmentsById() {
        return this.attachments.stream().collect(toMap(a -> a.getId(), a -> a));
    }

    private static String getEmptyHtmlContent() {
        return "<!doctype html>\n" +
            "<html>\n" +
            " <head> \n" +
            "  <meta charset=\"UTF-8\">\n" +
            " </head>\n" +
            " <body>\n" +
            " </body>\n" +
            "</html>";
    }

    private static enum CompileMode {
        /** The mail is supposed to be compiled to be saved as composition-space-associated draft mail */
        COMPOSITION_SPACE,
        /** The mail is supposed to be compiled to be saved as final draft mail */
        FINAL_DRAFT,
        /** The mail is supposed to be compiled to be used for transport */
        TRANSPORT;
    }

    private static class AttachmentFileItemDataProvider implements FileItem.DataProvider {

        private final Attachment attachment;

        AttachmentFileItemDataProvider(Attachment attachment) {
            this.attachment = attachment;
        }

        @Override
        public InputStream getData() throws OXException {
            return attachment.getData();
        }
    }

    /** String representation for processor's message state */
    private static final class LoggableMessageRepresentation {

        private final MailMessageProcessor processor;

        LoggableMessageRepresentation(MailMessageProcessor processor) {
            super();
            this.processor = processor;
        }

        @Override
        public String toString() {
            String lf = System.lineSeparator();
            StringBuilder sb = new StringBuilder(1024)
                .append("Composition space: ").append(UUIDs.getUnformattedString(processor.compositionSpaceId)).append(lf)
                .append("Content type: ").append(processor.contentType.getId()).append(lf)
                .append("Content (web): ").append(processor.contentForWeb).append(lf)
                .append("Content (draft): ").append(processor.contentForDraft).append(lf);

            sb.append("Attachments:");
            List<ForwardingAttachmentIfNotSet> attachments = processor.attachments;
            if (attachments == null || attachments.isEmpty()) {
                sb.append(" <none>").append(lf);
            } else {
                sb.append(lf);
                boolean first = true;
                for (ForwardingAttachmentIfNotSet attachment : attachments) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append("  ---").append(lf);
                    }
                    sb.append("  ID: ").append(UUIDs.getUnformattedString(attachment.getId())).append(lf);
                    sb.append("  Name: ").append(attachment.getName()).append(lf);
                    sb.append("  Size: ").append(attachment.getSize()).append(lf);
                    sb.append("  MIME Type: ").append(attachment.getMimeType()).append(lf);
                    sb.append("  Disposition: ").append(attachment.getContentDisposition().getId()).append(lf);
                    sb.append("  Origin: ").append(attachment.getOrigin()).append(lf);
                    sb.append("  Drive Mail: ").append(Boolean.toString(attachment.getSharedAttachmentReference() != null)).append(lf);
                }
            }

            return sb.toString();
        }
    }

}
