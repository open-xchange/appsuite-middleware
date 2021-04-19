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

package com.openexchange.mail.compose.mailstorage.storage;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static com.openexchange.logging.LogUtility.toStringObjectFor;
import static com.openexchange.mail.compose.CompositionSpaces.getUUIDForLogging;
import static java.util.stream.Collectors.toList;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import com.google.common.collect.Sets;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.crypto.CryptographicServiceAuthenticationFactory;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.ConfigTools;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.cascade.ConfigViews;
import com.openexchange.exception.OXException;
import com.openexchange.java.CombinedInputStream;
import com.openexchange.java.CountingOutputStream;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailFields;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.Quota;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.IMailMessageStorageEnhancedDeletion;
import com.openexchange.mail.api.IMailMessageStorageMimeSupport;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.crypto.CryptographicAwareMailAccessFactory;
import com.openexchange.mail.compose.Attachment;
import com.openexchange.mail.compose.AttachmentDescription;
import com.openexchange.mail.compose.AttachmentOrigin;
import com.openexchange.mail.compose.AttachmentStorages;
import com.openexchange.mail.compose.ByteArrayDataProvider;
import com.openexchange.mail.compose.ClientToken;
import com.openexchange.mail.compose.CompositionSpaceErrorCode;
import com.openexchange.mail.compose.CompositionSpaces;
import com.openexchange.mail.compose.DefaultAttachment;
import com.openexchange.mail.compose.HeaderUtility;
import com.openexchange.mail.compose.Message.ContentType;
import com.openexchange.mail.compose.Message.Priority;
import com.openexchange.mail.compose.MessageDescription;
import com.openexchange.mail.compose.MessageField;
import com.openexchange.mail.compose.Meta;
import com.openexchange.mail.compose.Meta.MetaType;
import com.openexchange.mail.compose.Security;
import com.openexchange.mail.compose.SharedAttachmentsInfo;
import com.openexchange.mail.compose.SharedFolderReference;
import com.openexchange.mail.compose.VCardAndFileName;
import com.openexchange.mail.compose.mailstorage.MailStorageCompositionSpaceConfig;
import com.openexchange.mail.compose.mailstorage.cache.CacheReference;
import com.openexchange.mail.compose.mailstorage.util.TrackingInputStream;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.SecuritySettings;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.json.compose.share.AttachmentStorageRegistry;
import com.openexchange.mail.json.compose.share.spi.AttachmentStorage;
import com.openexchange.mail.mime.HeaderCollection;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.crypto.PGPMailRecognizer;
import com.openexchange.mail.mime.processing.MimeProcessingUtility;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.parser.MailMessageParser;
import com.openexchange.mail.parser.handlers.NonInlineForwardPartHandler;
import com.openexchange.mail.search.ANDTerm;
import com.openexchange.mail.search.ComparisonType;
import com.openexchange.mail.search.FlagTerm;
import com.openexchange.mail.search.HeaderExistenceTerm;
import com.openexchange.mail.search.HeaderTerm;
import com.openexchange.mail.search.ReceivedDateTerm;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.mail.service.EncryptedMailService;
import com.openexchange.mail.service.MailService;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link MailStorage} - Accesses mail storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class MailStorage implements IMailStorage {

    /** The logger constant */
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailStorage.class);

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final ServiceLookup services;

    /**
     * Initializes a new {@link MailStorage}.
     *
     * @param services The service look-up
     */
    public MailStorage(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public MailStorageResult<Optional<MailStorageId>> lookUp(UUID compositionSpaceId, Session session) throws OXException {
        MailService mailService = services.getServiceSafe(MailService.class);
        MailAccess<? extends IMailFolderStorage,? extends IMailMessageStorage> mailAccess = null;
        try {
            mailAccess = mailService.getMailAccess(session, MailAccount.DEFAULT_ID);
            mailAccess.connect(true);

            Optional<MailPath> optionalPath = doLookUp(compositionSpaceId, mailAccess.getFolderStorage().getDraftsFolder(), mailAccess.getMessageStorage());
            if (optionalPath.isPresent()) {
                DefaultMailStorageId mailStorageId = new DefaultMailStorageId(optionalPath.orElse(null), compositionSpaceId, Optional.empty());
                return MailStorageResult.resultFor(mailStorageId, Optional.of(mailStorageId), false, mailAccess);
            }

            return MailStorageResult.resultFor(null, Optional.empty(), false, mailAccess);
        } finally {
            if (mailAccess != null) {
                mailAccess.close(true);
            }
        }
    }

    private static final MailField[] MAIL_FIELDS_LOOK_UP = new MailField[] { MailField.ID, MailField.RECEIVED_DATE, MailField.HEADERS, MailField.SIZE };

    @Override
    public MailStorageResult<LookUpOutcome> lookUp(Session session) throws OXException {
        MailService mailService = services.getServiceSafe(MailService.class);
        MailAccess<? extends IMailFolderStorage,? extends IMailMessageStorage> mailAccess = null;
        try {
            mailAccess = mailService.getMailAccess(session, MailAccount.DEFAULT_ID);
            mailAccess.connect(true);

            String draftsFolder = mailAccess.getFolderStorage().getDraftsFolder();

            // Search for undeleted, unexpired mails having a "X-OX-Composition-Space-Id" header and sort them by received-date descendingly
            long maxIdleTimeMillis = getMaxIdleTimeMillis(session);
            SearchTerm<?> searchTerm;
            if (maxIdleTimeMillis > 0) {
                HeaderExistenceTerm headerExistenceTerm = new HeaderExistenceTerm(HeaderUtility.HEADER_X_OX_COMPOSITION_SPACE_ID);
                ReceivedDateTerm receivedDateTerm = new ReceivedDateTerm(ComparisonType.GREATER_EQUALS, new Date(System.currentTimeMillis() - maxIdleTimeMillis));
                searchTerm = new ANDTerm(headerExistenceTerm, receivedDateTerm);
            } else {
                searchTerm = new HeaderExistenceTerm(HeaderUtility.HEADER_X_OX_COMPOSITION_SPACE_ID);
            }
            searchTerm = new ANDTerm(searchTerm, new FlagTerm(MailMessage.FLAG_DELETED, false));
            MailMessage[] mailMessages = mailAccess.getMessageStorage().searchMessages(draftsFolder, IndexRange.NULL, MailSortField.RECEIVED_DATE, OrderDirection.DESC, searchTerm, MAIL_FIELDS_LOOK_UP);

            // No such mails
            if (mailMessages == null || mailMessages.length == 0) {
                LOG.debug("Found no open composition spaces");
                return MailStorageResult.resultFor(null, LookUpOutcome.EMPTY, false, mailAccess);
            }

            // Filter duplicate ones and trim to "maxSpacesPerUser"
            int maxSpacesPerUser = getMaxSpacesPerUser(session);
            Map<UUID, MailMessage> id2Message = new HashMap<>(mailMessages.length);
            Map<MailPath, UUID> duplicateSpaces = null;
            for (MailMessage mailMessage : mailMessages) {
                if (mailMessage != null) {
                    Optional<UUID> optCompositionSpaceId = parseCompositionSpaceId(mailMessage);
                    if (!optCompositionSpaceId.isPresent()) {
                        continue;
                    }

                    UUID compositionSpaceId = optCompositionSpaceId.get();
                    boolean isMaxSpacesExceeded = (maxSpacesPerUser > 0 && id2Message.size() >= maxSpacesPerUser);
                    MailMessage existing = isMaxSpacesExceeded ? id2Message.get(compositionSpaceId) : id2Message.putIfAbsent(compositionSpaceId, mailMessage);

                    if (existing != null) {
                        // Duplicate...
                        if (duplicateSpaces == null) {
                            duplicateSpaces = new HashMap<>();
                        }
                        if (mailMessage.getReceivedDate().getTime() > existing.getReceivedDate().getTime()) {
                            // Keep the newer one
                            id2Message.put(compositionSpaceId, mailMessage);
                            duplicateSpaces.put(new MailPath(MailAccount.DEFAULT_ID, draftsFolder, existing.getMailId()), compositionSpaceId);
                        } else {
                            duplicateSpaces.put(new MailPath(MailAccount.DEFAULT_ID, draftsFolder, mailMessage.getMailId()), compositionSpaceId);
                        }
                    }
                }
            }

            // Help GC
            mailMessages = null;

            Map<MailPath, UUID> mailPathsToUUIDs = new LinkedHashMap<>(id2Message.size());
            for (Map.Entry<UUID, MailMessage> id2MessageEntry : id2Message.entrySet()) {
                mailPathsToUUIDs.put(new MailPath(MailAccount.DEFAULT_ID, draftsFolder, id2MessageEntry.getValue().getMailId()), id2MessageEntry.getKey());
            }
            LOG.debug("Found open composition spaces: {}", mailPathsToUUIDs.values().stream().map(uuid -> getUUIDForLogging(uuid)).collect(toList()));
            LookUpOutcome lookUpOutcome = new LookUpOutcome(mailPathsToUUIDs, duplicateSpaces == null ? Collections.emptyMap() : duplicateSpaces);
            return MailStorageResult.resultFor(null, lookUpOutcome, false, mailAccess);
        } finally {
            if (mailAccess != null) {
                mailAccess.close(true);
            }
        }
    }

    @Override
    public MailStorageResult<ComposeRequestAndMeta> getForTransport(MailStorageId mailStorageId, ClientToken clientToken, AJAXRequestData request, Session session) throws OXException, MissingDraftException {
        UUID compositionSpaceId = mailStorageId.getCompositionSpaceId();
        MailPath draftPath = mailStorageId.getDraftPath();
        if (draftPath.getAccountId() != MailAccount.DEFAULT_ID) {
            throw CompositionSpaceErrorCode.ERROR.create("Cannot operate on drafts outside of the default mail account!");
        }

        MailService mailService = services.getServiceSafe(MailService.class);
        List<MailAccess<? extends IMailFolderStorage,? extends IMailMessageStorage>> mailAccesses = new ArrayList<>(2);
        try {
            MailAccess<? extends IMailFolderStorage,? extends IMailMessageStorage> defaultMailAccess = mailService.getMailAccess(session, MailAccount.DEFAULT_ID);
            mailAccesses.add(defaultMailAccess);
            defaultMailAccess.connect(false);

            MailMessage draftMail = requireDraftMail(mailStorageId, defaultMailAccess);

            MailMessageProcessor processor = MailMessageProcessor.initForTransport(compositionSpaceId, draftMail, session, services);
            checkClientToken(clientToken, processor.getClientToken());

            validateIfNeeded(mailStorageId, processor);

            MessageDescription currentDraft = processor.getCurrentDraft(MessageField.META, MessageField.SECURITY);
            Meta meta = currentDraft.getMeta();
            Optional<MailMessage> optRefMessage = Optional.empty();
            if (meta != null) {
                MailPath referencedMessage = null;
                MetaType metaType = meta.getType();
                if (metaType == MetaType.REPLY || metaType == MetaType.REPLY_ALL) {
                    referencedMessage = meta.getReplyFor();
                } else if (metaType == MetaType.FORWARD_INLINE) {
                    referencedMessage = meta.getForwardsFor().get(0);
                }

                if (referencedMessage != null) {
                    try {
                        optRefMessage = Optional.of(getOriginalMail(session, referencedMessage, mailService, mailAccesses, defaultMailAccess, getSecurity(currentDraft).getAuthToken()));
                    } catch (OXException e) {
                        LOG.error("Cannot not apply reference headers because fetching the referenced message failed", e);
                    }
                }
            }

            ComposeRequestAndMeta composeRequestAndMeta = new ComposeRequestAndMeta(processor.compileComposeRequest(request, optRefMessage), meta);
            return MailStorageResult.resultFor(mailStorageId, composeRequestAndMeta, true, defaultMailAccess, processor);
        } finally {
            for (MailAccess<? extends IMailFolderStorage,? extends IMailMessageStorage> mailAccess : mailAccesses) {
                mailAccess.close(true);
            }
        }
    }

    @Override
    public MailStorageResult<MessageInfo> lookUpMessage(UUID compositionSpaceId, Session session) throws OXException {
        MailService mailService = services.getServiceSafe(MailService.class);
        MailAccess<? extends IMailFolderStorage,? extends IMailMessageStorage> mailAccess = null;
        MailMessageProcessor processor = null;
        try {
            mailAccess = mailService.getMailAccess(session, MailAccount.DEFAULT_ID);
            mailAccess.connect(true);

            String draftsFolder = mailAccess.getFolderStorage().getDraftsFolder();

            IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
            Optional<MailPath> optMailPath = doLookUp(compositionSpaceId, draftsFolder, messageStorage);
            if (!optMailPath.isPresent()) {
                throw CompositionSpaceErrorCode.NO_SUCH_COMPOSITION_SPACE.create(getUUIDForLogging(compositionSpaceId));
            }

            MailPath draftPath = optMailPath.get();
            MailMessage draftMail = requireDraftMail(new DefaultMailStorageId(draftPath, compositionSpaceId, Optional.empty()), mailAccess);
            processor = MailMessageProcessor.initReadEnvelope(compositionSpaceId, draftMail, session, services);
            boolean changed = processor.validate();
            MessageDescription currentDraft = processor.getCurrentDraft();
            SecuritySettings securitySettings = getSecuritySettings(currentDraft.getSecurity());
            if (changed) {
                MailMessage newDraft = deleteAndSaveDraftMail(draftPath, processor.getOriginalSize(), processor, securitySettings, mailAccess, false, Optional.empty(), session);
                MailPath newDraftPath = newDraft.getMailPath();
                long size = newDraft.getSize();
                if (size < 0) {
                    size = fetchMailSize(mailAccess.getMessageStorage(), newDraftPath);
                }

                MessageInfo messageInfo = new MessageInfo(processor.getCurrentDraft(), size, newDraft.getSentDate());
                DefaultMailStorageId newId = new DefaultMailStorageId(newDraftPath, compositionSpaceId, processor.getFileCacheReference());
                return MailStorageResult.resultFor(newId, messageInfo, true, mailAccess, processor);
            }

            MessageInfo messageInfo = new MessageInfo(currentDraft, draftMail.getSize(), draftMail.getSentDate());
            MailStorageId newId = new DefaultMailStorageId(draftMail.getMailPath(), compositionSpaceId, processor.getFileCacheReference());
            return MailStorageResult.resultFor(newId, messageInfo, true, mailAccess);
        } catch (MissingDraftException e) {
            throw CompositionSpaceErrorCode.NO_SUCH_COMPOSITION_SPACE.create(e, getUUIDForLogging(e.getFirstMailStorageId().getCompositionSpaceId()));
        } finally {
            if (mailAccess != null) {
                mailAccess.close(true);
            }
            closeProcessorSafe(processor);
        }
    }

    @Override
    public MailStorageResult<MessageInfo> getMessage(MailStorageId mailStorageId, Session session) throws OXException, MissingDraftException {
        UUID compositionSpaceId = mailStorageId.getCompositionSpaceId();
        MailPath draftPath = mailStorageId.getDraftPath();
        if (draftPath.getAccountId() != MailAccount.DEFAULT_ID) {
            throw CompositionSpaceErrorCode.ERROR.create("Cannot operate on drafts outside of the default mail account!");
        }

        MailService mailService = services.getServiceSafe(MailService.class);
        MailAccess<? extends IMailFolderStorage,? extends IMailMessageStorage> mailAccess = null;
        MailMessageProcessor processor = null;
        try {
            mailAccess = mailService.getMailAccess(session, MailAccount.DEFAULT_ID);
            mailAccess.connect(false);

            MailMessage draftMail = requireDraftMail(mailStorageId, mailAccess);

            processor = MailMessageProcessor.initReadEnvelope(compositionSpaceId, draftMail, session, services);
            MessageDescription currentDraft = processor.getCurrentDraft();

            MessageInfo messageInfo = new MessageInfo(currentDraft, draftMail.getSize(), draftMail.getSentDate());
            MailStorageId newId = new DefaultMailStorageId(draftMail.getMailPath(), compositionSpaceId, processor.getFileCacheReference());
            return MailStorageResult.resultFor(newId, messageInfo, true, mailAccess);
        } finally {
            if (mailAccess != null) {
                mailAccess.close(true);
            }
        }
    }

    private static final Set<MessageField> MESSAGE_FIELDS_ALL = Sets.immutableEnumSet(EnumSet.allOf(MessageField.class));

    @Override
    public MailStorageResult<Map<UUID, MessageInfo>> getMessages(Collection<? extends MailStorageId> mailStorageIds, Set<MessageField> fields, Session session) throws OXException, MissingDraftException {
        if (mailStorageIds == null) {
            return null;
        }
        if (mailStorageIds.isEmpty()) {
            return MailStorageResult.resultFor(null, Collections.emptyMap(), false);
        }

        MailFields mailFields = toMailFields(fields);

        MailService mailService = services.getServiceSafe(MailService.class);
        MailAccess<? extends IMailFolderStorage,? extends IMailMessageStorage> mailAccess = null;
        try {
            MailStorageId firstMailStorageId = mailStorageIds.iterator().next();
            if (firstMailStorageId.getAccountId() != MailAccount.DEFAULT_ID) {
                throw CompositionSpaceErrorCode.ERROR.create("Cannot operate on drafts outside of the default mail account!");
            }

            mailAccess = mailService.getMailAccess(session, MailAccount.DEFAULT_ID);
            mailAccess.connect(false);

            Map<UUID, MessageInfo> result = new LinkedHashMap<>(mailStorageIds.size());
            if (mailFields.contains(MailField.FULL) || mailFields.contains(MailField.BODY)) {
                for (MailStorageId mailStorageId : mailStorageIds) {
                    UUID compositionSpaceId = mailStorageId.getCompositionSpaceId();
                    MailMessage draftMail = requireDraftMail(mailStorageId, mailAccess);
                    MailMessageProcessor processor = MailMessageProcessor.initReadEnvelope(compositionSpaceId, draftMail, session, services);
                    result.put(compositionSpaceId, new MessageInfo(processor.getCurrentDraft(MESSAGE_FIELDS_ALL),  draftMail.getSize(), draftMail.getSentDate()));
                }
            } else {
                Map<String, UUID> mailIds = new HashMap<>(mailStorageIds.size());
                for (MailStorageId mailStorageId : mailStorageIds) {
                    mailIds.put(mailStorageId.getMailId(), mailStorageId.getCompositionSpaceId());
                }

                String folderId = firstMailStorageId.getFolderId();
                mailFields.add(MailField.ID);
                mailFields.add(MailField.HEADERS); // For 'Date'
                mailFields.add(MailField.SIZE);

                MailMessage[] messages = mailAccess.getMessageStorage().getMessages(folderId, mailIds.keySet().toArray(new String[mailIds.size()]), mailFields.toArray());
                for (MailMessage mailMessage : messages) {
                    UUID compositionSpaceId = mailIds.remove(mailMessage.getMailId());
                    if (compositionSpaceId != null) {
                        MessageDescription messageDesc = toMessageDescription(mailMessage, fields);
                        result.put(compositionSpaceId, new MessageInfo(messageDesc, mailMessage.getSize(), mailMessage.getSentDate()));
                    }
                }

                if (!mailIds.isEmpty()) {
                    List<MailStorageId> absentOnes = new ArrayList<>(mailIds.size());
                    int accountId = firstMailStorageId.getAccountId();
                    for (Map.Entry<String, UUID> mailIdEntry : mailIds.entrySet()) {
                        MailPath mailPath = new MailPath(accountId, folderId, mailIdEntry.getKey());
                        absentOnes.add(new DefaultMailStorageId(mailPath, mailIdEntry.getValue(), Optional.empty()));
                    }
                    throw new MissingDraftException(absentOnes);
                }
            }
            return MailStorageResult.resultFor(null, result, false, mailAccess);
        } finally {
            if (mailAccess != null) {
                mailAccess.close(true);
            }
        }
    }

    @Override
    public MailStorageResult<MessageInfo> createNew(UUID compositionSpaceId, MessageDescription draftMessage, Optional<SharedFolderReference> optionalSharedFolderRef, ClientToken clientToken, Session session) throws OXException {
        MailService mailService = services.getServiceSafe(MailService.class);
        MailAccess<? extends IMailFolderStorage,? extends IMailMessageStorage> mailAccess = null;
        try {
            mailAccess = mailService.getMailAccess(session, MailAccount.DEFAULT_ID);
            mailAccess.connect(true);

            MailMessageProcessor processor = MailMessageProcessor.initNew(compositionSpaceId, optionalSharedFolderRef, clientToken, session, services);
            processor.applyUpdate(draftMessage);
            processor.addAttachments(draftMessage.getAttachments());
            MessageDescription update = processor.getCurrentDraft();

            ComposedMailMessage composedMessage = processor.compileDraft();
            composedMessage = applyGuardEncryption(getSecuritySettings(draftMessage.getSecurity()), composedMessage, session);
            composedMessage.setSendType(ComposeType.DRAFT);

            String draftsFolder = mailAccess.getFolderStorage().getDraftsFolder();
            IMailMessageStorage draftMessageStorage = mailAccess.getMessageStorage();
            MailMessage savedDraft = saveDraftMail(composedMessage, draftsFolder, true, draftMessageStorage);
            long size = savedDraft.getSize();
            MailPath mailPath = savedDraft.getMailPath();
            if (size < 0) {
                size = fetchMailSize(draftMessageStorage, mailPath);
            }

            DefaultMailStorageId newId = new DefaultMailStorageId(mailPath, compositionSpaceId, processor.getFileCacheReference());
            MessageInfo messageInfo = new MessageInfo(update, size, savedDraft.getSentDate());
            return MailStorageResult.resultFor(newId, messageInfo, false, mailAccess, processor);
        } finally {
            if (mailAccess != null) {
                mailAccess.close(true);
            }
        }
    }

    @Override
    public MailStorageResult<MailPath> saveAsFinalDraft(MailStorageId mailStorageId, ClientToken clientToken, Session session) throws OXException, MissingDraftException {
        MailPath draftPath = mailStorageId.getDraftPath();
        if (draftPath.getAccountId() != MailAccount.DEFAULT_ID) {
            throw CompositionSpaceErrorCode.ERROR.create("Cannot operate on drafts outside of the default mail account!");
        }

        MailService mailService = services.getServiceSafe(MailService.class);
        List<MailAccess<? extends IMailFolderStorage,? extends IMailMessageStorage>> mailAccesses = new ArrayList<>(2);
        try {
            MailAccess<? extends IMailFolderStorage,? extends IMailMessageStorage> defaultMailAccess = mailService.getMailAccess(session, MailAccount.DEFAULT_ID);
            mailAccesses.add(defaultMailAccess);
            defaultMailAccess.connect(false);

            MailMessageProcessor processor = initMessageProcessorFull(mailStorageId, session, defaultMailAccess, clientToken);
            validateIfNeeded(mailStorageId, processor);
            MessageDescription originalDescription = processor.getCurrentDraft(MessageField.META, MessageField.SECURITY);

            Security security = getSecurity(originalDescription);
            SecuritySettings securitySettings = getSecuritySettings(security);

            Meta meta = originalDescription.getMeta();
            Optional<MailMessage> optRefMessage = Optional.empty();
            if (meta != null) {
                MailPath referencedMessage = null;
                MetaType metaType = meta.getType();
                if (metaType == MetaType.REPLY || metaType == MetaType.REPLY_ALL) {
                    referencedMessage = meta.getReplyFor();
                } else if (metaType == MetaType.FORWARD_INLINE) {
                    referencedMessage = meta.getForwardsFor().get(0);
                }

                if (referencedMessage != null) {
                    try {
                        optRefMessage = Optional.of(getOriginalMail(session, referencedMessage, mailService, mailAccesses, defaultMailAccess, security.getAuthToken()));
                    } catch (OXException e) {
                        LOG.error("Cannot not apply reference headers because fetching the referenced message failed", e);
                    }
                }
            }

            MailPath newDraftPath = deleteAndSaveDraftMail(draftPath, processor.getOriginalSize(), processor, securitySettings, defaultMailAccess, true, optRefMessage, session).getMailPath();
            processor.getFileCacheReference().ifPresent(r -> r.cleanUp());

            // Check for edit-draft --> Not needed since already dropped when opening composition space
            /*-
             *
            MailPath editFor = meta == null ? null : meta.getEditFor();
            if (editFor != null) {
                defaultMailAccess.getMessageStorage().deleteMessages(editFor.getFolder(), new String[] { editFor.getMailID() }, true);
            }
             *
             */

            return MailStorageResult.resultFor(null, newDraftPath, true, defaultMailAccess, processor);
        } finally {
            for (MailAccess<? extends IMailFolderStorage,? extends IMailMessageStorage> mailAccess : mailAccesses) {
                mailAccess.close(true);
            }
        }
    }

    @Override
    public MailStorageResult<MessageInfo> update(MailStorageId mailStorageId, MessageDescription newDescription, ClientToken clientToken, Session session) throws OXException, MissingDraftException {
        UUID compositionSpaceId = mailStorageId.getCompositionSpaceId();
        MailPath draftPath = mailStorageId.getDraftPath();
        if (draftPath.getAccountId() != MailAccount.DEFAULT_ID) {
            throw CompositionSpaceErrorCode.ERROR.create("Cannot operate on drafts outside of the default mail account!");
        }

        MailService mailService = services.getServiceSafe(MailService.class);
        MailAccess<? extends IMailFolderStorage,? extends IMailMessageStorage> mailAccess = null;
        try {
            mailAccess = mailService.getMailAccess(session, MailAccount.DEFAULT_ID);
            mailAccess.connect(false);

            String authToken = newDescription.getSecurity() != null ? newDescription.getSecurity().getAuthToken() : null;
            MailMessageProcessor processor = initMessageProcessorFull(mailStorageId, session, mailAccess, authToken, clientToken);
            boolean changed = validateIfNeeded(mailStorageId, processor);
            MessageDescription originalDescription = processor.getCurrentDraft();

            // Check for any difference
            if (!changed && originalDescription.seemsEqual(newDescription)) {
                MessageInfo messageInfo = new MessageInfo(originalDescription, processor.getOriginalSize(), processor.getDateHeader().orElse(null));
                return MailStorageResult.resultFor(mailStorageId, messageInfo, true, mailAccess, processor);
            }

            SecuritySettings securitySettings = prepareSecuritySettings(originalDescription, newDescription);
            processor.applyUpdate(newDescription);
            applySharedAttachmentsChanges(originalDescription, newDescription, processor, session);

            MailMessage newDraft = deleteAndSaveDraftMail(draftPath, processor.getOriginalSize(), processor, securitySettings, mailAccess, false, Optional.empty(), session);
            MailPath newDraftPath = newDraft.getMailPath();
            long size = newDraft.getSize();
            if (size < 0) {
                size = fetchMailSize(mailAccess.getMessageStorage(), newDraftPath);
            }
            MessageInfo messageInfo = new MessageInfo(processor.getCurrentDraft(), size, newDraft.getSentDate());
            DefaultMailStorageId newId = new DefaultMailStorageId(newDraftPath, compositionSpaceId, processor.getFileCacheReference());
            return MailStorageResult.resultFor(newId, messageInfo, true, mailAccess, processor);
        } finally {
            if (mailAccess != null) {
                mailAccess.close(true);
            }
        }
    }

    @Override
    public MailStorageResult<Boolean> delete(MailStorageId mailStorageId, boolean hardDelete, boolean deleteSharedAttachmentsFolderIfPresent, ClientToken clientToken, Session session) throws OXException {
        MailPath draftPath = mailStorageId.getDraftPath();
        if (draftPath.getAccountId() != MailAccount.DEFAULT_ID) {
            throw CompositionSpaceErrorCode.ERROR.create("Cannot operate on drafts outside of the default mail account!");
        }

        MailService mailService = services.getServiceSafe(MailService.class);
        MailAccess<? extends IMailFolderStorage,? extends IMailMessageStorage> mailAccess = null;
        try {
            mailAccess = mailService.getMailAccess(session, MailAccount.DEFAULT_ID);
            mailAccess.connect(false);

            tryCleanUpFileCacheReference(mailStorageId);

            MailMessage draftMail = requireDraftMail(mailStorageId, mailAccess, false);
            checkClientToken(clientToken, parseClientToken(draftMail));

            // In case message is a Drive Mail and associated attachments are supposed to be deleted, the message is required to be hard-deleted
            boolean hardDeleteMessage = hardDelete;
            if (deleteSharedAttachmentsFolderIfPresent) {
                String headerValue = HeaderUtility.decodeHeaderValue(draftMail.getFirstHeader(HeaderUtility.HEADER_X_OX_SHARED_ATTACHMENTS));
                SharedAttachmentsInfo sharedAttachmentsInfo = HeaderUtility.headerValue2SharedAttachments(headerValue);

                if (sharedAttachmentsInfo.isEnabled()) {
                    headerValue = HeaderUtility.decodeHeaderValue(draftMail.getFirstHeader(HeaderUtility.HEADER_X_OX_SHARED_FOLDER_REFERENCE));
                    SharedFolderReference sharedFolderRef = HeaderUtility.headerValue2SharedFolderReference(headerValue);

                    if (sharedFolderRef != null) {
                        AttachmentStorageRegistry attachmentStorageRegistry = services.getServiceSafe(AttachmentStorageRegistry.class);
                        AttachmentStorage attachmentStorage = attachmentStorageRegistry.getAttachmentStorageFor(session);
                        attachmentStorage.deleteFolder(sharedFolderRef.getFolderId(), ServerSessionAdapter.valueOf(session));

                        // Drive Mail cannot be moved to trash. Therefore:
                        hardDeleteMessage = true;
                    }
                }
            }

            IMailMessageStorageEnhancedDeletion enhancedDeletion = mailAccess.getMessageStorage().supports(IMailMessageStorageEnhancedDeletion.class);
            if (enhancedDeletion != null && enhancedDeletion.isEnhancedDeletionSupported()) {
                // Try to delete current draft mail in storage
                if (hardDeleteMessage) {
                    MailPath[] removedPaths = enhancedDeletion.hardDeleteMessages(draftPath.getFolder(), new String[] { draftPath.getMailID() });
                    Boolean deleted = Boolean.valueOf(removedPaths != null && removedPaths.length > 0 && draftPath.equals(removedPaths[0]));
                    return MailStorageResult.resultFor(mailStorageId, deleted, false, mailAccess);
                }

                MailPath[] movedPaths = enhancedDeletion.deleteMessagesEnhanced(draftPath.getFolder(), new String[] { draftPath.getMailID() }, false);
                if (movedPaths == null || movedPaths.length != 1) {
                    return MailStorageResult.resultFor(mailStorageId, Boolean.FALSE, false, mailAccess);
                }

                try {
                    MailPath trashed = movedPaths[0];
                    mailAccess.getMessageStorage().updateMessageFlags(trashed.getFolder(), new String[] { trashed.getMailID() }, MailMessage.FLAG_SEEN, true);
                } catch (Exception e) {
                    LOG.warn("Failed to set \\Seen flag on trashed draft message {} in folder {}", draftPath.getMailID(), draftPath.getFolder());
                }
                return MailStorageResult.resultFor(mailStorageId, Boolean.TRUE, false, mailAccess);
            }

            // Delete by best guess...
            mailAccess.getMessageStorage().deleteMessages(draftPath.getFolder(), new String[] { draftPath.getMailID() }, hardDeleteMessage);
            return MailStorageResult.resultFor(mailStorageId, Boolean.TRUE, false, mailAccess);
        } catch (MissingDraftException e) {
            return MailStorageResult.resultFor(mailStorageId, Boolean.FALSE, false, mailAccess);
        } finally {
            if (mailAccess != null) {
                mailAccess.close(true);
            }
        }
    }

    @Override
    public MailStorageResult<NewAttachmentsInfo> addOriginalAttachments(MailStorageId mailStorageId, ClientToken clientToken, Session session) throws OXException, MissingDraftException {
        UUID compositionSpaceId = mailStorageId.getCompositionSpaceId();
        MailPath draftPath = mailStorageId.getDraftPath();
        if (draftPath.getAccountId() != MailAccount.DEFAULT_ID) {
            throw CompositionSpaceErrorCode.ERROR.create("Cannot operate on drafts outside of the default mail account!");
        }

        MailService mailService = services.getServiceSafe(MailService.class);
        List<MailAccess<? extends IMailFolderStorage,? extends IMailMessageStorage>> mailAccesses = new ArrayList<>(2);
        InputStream draftMimeStream = null;
        try {
            MailAccess<? extends IMailFolderStorage,? extends IMailMessageStorage> defaultMailAccess = mailService.getMailAccess(session, MailAccount.DEFAULT_ID);
            mailAccesses.add(defaultMailAccess);
            defaultMailAccess.connect(false);

            MailMessageProcessor processor = initMessageProcessorFull(mailStorageId, session, defaultMailAccess, clientToken);
            boolean changed = validateIfNeeded(mailStorageId, processor);
            MessageDescription originalDescription = processor.getCurrentDraft();

            Security security = getSecurity(originalDescription);

            // Acquire meta information and determine the "replyFor" path
            Meta meta = originalDescription.getMeta();
            MailPath replyFor = meta.getReplyFor();
            if (null == replyFor) {
                throw CompositionSpaceErrorCode.NO_REPLY_FOR.create();
            }

            MailMessage originalMail = getOriginalMail(session, replyFor, mailService, mailAccesses, defaultMailAccess, security.getAuthToken());
            List<Attachment> newAttachments = fetchOriginalAttachments(session, compositionSpaceId, processor, originalMail);

            List<Attachment> addedAttachments = processor.addAttachments(newAttachments);
            if (addedAttachments.isEmpty()) {
                // No attachments to add
                if (changed) {
                    SecuritySettings securitySettings = getSecuritySettings(originalDescription.getSecurity());
                    MailMessage newDraft = deleteAndSaveDraftMail(draftPath, processor.getOriginalSize(), processor, securitySettings, defaultMailAccess, false, Optional.empty(), session);
                    MailPath newDraftPath = newDraft.getMailPath();
                    long size = newDraft.getSize();
                    if (size < 0) {
                        size = fetchMailSize(defaultMailAccess.getMessageStorage(), newDraftPath);
                    }

                    NewAttachmentsInfo info = new NewAttachmentsInfo(Collections.emptyList(), originalDescription, size, newDraft.getSentDate());
                    DefaultMailStorageId newId = new DefaultMailStorageId(newDraftPath, compositionSpaceId, processor.getFileCacheReference());
                    return MailStorageResult.resultFor(newId, info, true, defaultMailAccess, processor);
                }

                NewAttachmentsInfo info = new NewAttachmentsInfo(Collections.emptyList(), originalDescription, processor.getOriginalSize(), processor.getDateHeader().orElse(null));
                DefaultMailStorageId newId = new DefaultMailStorageId(mailStorageId.getDraftPath(), compositionSpaceId, processor.getFileCacheReference());
                return MailStorageResult.resultFor(newId, info, true, defaultMailAccess, processor);
            }

            SecuritySettings securitySettings = getSecuritySettings(originalDescription.getSecurity());

            MailMessage newDraft = deleteAndSaveDraftMail(draftPath, processor.getOriginalSize(), processor, securitySettings, defaultMailAccess, false, Optional.empty(), session);
            long size = newDraft.getSize();
            if (size < 0) {
                size = fetchMailSize(defaultMailAccess.getMessageStorage(), draftPath);
            }
            NewAttachmentsInfo info = new NewAttachmentsInfo(getAttachmentIds(addedAttachments), processor.getCurrentDraft(), size, newDraft.getSentDate());
            DefaultMailStorageId newId = new DefaultMailStorageId(newDraft.getMailPath(), compositionSpaceId, processor.getFileCacheReference());
            return MailStorageResult.resultFor(newId, info, true, defaultMailAccess, processor);
        } finally {
            Streams.close(draftMimeStream);
            for (MailAccess<? extends IMailFolderStorage,? extends IMailMessageStorage> mailAccess : mailAccesses) {
                mailAccess.close(true);
            }
        }
    }

    @Override
    public MailStorageResult<NewAttachmentsInfo> addVCardAttachment(MailStorageId mailStorageId, ClientToken clientToken, Session session) throws OXException, MissingDraftException {
        UUID compositionSpaceId = mailStorageId.getCompositionSpaceId();
        MailPath draftPath = mailStorageId.getDraftPath();
        if (draftPath.getAccountId() != MailAccount.DEFAULT_ID) {
            throw CompositionSpaceErrorCode.ERROR.create("Cannot operate on drafts outside of the default mail account!");
        }

        MailService mailService = services.getServiceSafe(MailService.class);
        MailAccess<? extends IMailFolderStorage,? extends IMailMessageStorage> mailAccess = null;
        InputStream draftMimeStream = null;
        try {
            mailAccess = mailService.getMailAccess(session, MailAccount.DEFAULT_ID);
            mailAccess.connect(false);

            MailMessageProcessor processor = initMessageProcessorFull(mailStorageId, session, mailAccess, clientToken);
            validateIfNeeded(mailStorageId, processor);
            MessageDescription originalDescription = processor.getCurrentDraft();

            // Check by attachment origin
            for (Attachment existingAttachment : originalDescription.getAttachments()) {
                if (AttachmentOrigin.VCARD == existingAttachment.getOrigin()) {
                    // vCard already contained
                    NewAttachmentsInfo info = new NewAttachmentsInfo(getAttachmentIds(Collections.singletonList(existingAttachment)), originalDescription, processor.getOriginalSize(), processor.getDateHeader().orElse(null));
                    MailStorageId newId = new DefaultMailStorageId(mailStorageId.getDraftPath(), compositionSpaceId, processor.getFileCacheReference());
                    return MailStorageResult.resultFor(newId, info, true, mailAccess, processor);
                }
            }

            // Create vCard
            VCardAndFileName userVCard = CompositionSpaces.getUserVCard(session);

            // Check by file name
            Attachment existingVCardAttachment = null;
            for (Attachment existingAttachment : originalDescription.getAttachments()) {
                String fileName = existingAttachment.getName();
                if (fileName != null && fileName.equals(userVCard.getFileName())) {
                    // vCard already contained
                    existingVCardAttachment = existingAttachment;
                    break;
                }
            }

            // Create vCard attachment representation
            AttachmentDescription attachmentDesc = AttachmentStorages.createVCardAttachmentDescriptionFor(userVCard, compositionSpaceId, true);
            DefaultAttachment.Builder attachment = DefaultAttachment.builder(attachmentDesc);
            attachment.withDataProvider(new ByteArrayDataProvider(userVCard.getVcard()));

            // Either add or replace vCard attachment
            Attachment addedAttachment;
            if (existingVCardAttachment == null) {
                addedAttachment = processor.addAttachments(Collections.singletonList(attachment.build())).get(0);
            } else {
                attachment.withId(existingVCardAttachment.getId());
                addedAttachment = processor.replaceAttachment(attachment.build());
            }

            SecuritySettings securitySettings = getSecuritySettings(originalDescription.getSecurity());
            MailMessage newDraft = deleteAndSaveDraftMail(draftPath, processor.getOriginalSize(), processor, securitySettings, mailAccess, false, Optional.empty(), session);
            MailPath newDraftPath = newDraft.getMailPath();
            long size = newDraft.getSize();
            if (size < 0) {
                size = fetchMailSize(mailAccess.getMessageStorage(), newDraftPath);
            }
            NewAttachmentsInfo info = new NewAttachmentsInfo(getAttachmentIds(Collections.singletonList(addedAttachment)), processor.getCurrentDraft(), size, newDraft.getSentDate());
            MailStorageId newId = new DefaultMailStorageId(newDraftPath, compositionSpaceId, processor.getFileCacheReference());
            return MailStorageResult.resultFor(newId, info, true, mailAccess, processor);
        } finally {
            Streams.close(draftMimeStream);
            if (mailAccess != null) {
                mailAccess.close(true);
            }
        }
    }

    @Override
    public MailStorageResult<NewAttachmentsInfo> addContactVCardAttachment(MailStorageId mailStorageId, String contactId, String folderId, ClientToken clientToken, Session session) throws OXException, MissingDraftException {
        UUID compositionSpaceId = mailStorageId.getCompositionSpaceId();
        MailPath draftPath = mailStorageId.getDraftPath();
        if (draftPath.getAccountId() != MailAccount.DEFAULT_ID) {
            throw CompositionSpaceErrorCode.ERROR.create("Cannot operate on drafts outside of the default mail account!");
        }

        MailService mailService = services.getServiceSafe(MailService.class);
        MailAccess<? extends IMailFolderStorage,? extends IMailMessageStorage> mailAccess = null;
        InputStream draftMimeStream = null;
        try {
            mailAccess = mailService.getMailAccess(session, MailAccount.DEFAULT_ID);
            mailAccess.connect(false);

            MailMessageProcessor processor = initMessageProcessorFull(mailStorageId, session, mailAccess, clientToken);
            validateIfNeeded(mailStorageId, processor);
            MessageDescription originalDescription = processor.getCurrentDraft();

            // Create vCard
            VCardAndFileName contactVCard = CompositionSpaces.getContactVCard(contactId, folderId, session);

            // Create vCard attachment representation
            AttachmentDescription attachmentDesc = AttachmentStorages.createVCardAttachmentDescriptionFor(contactVCard, compositionSpaceId, false);
            DefaultAttachment.Builder attachment = DefaultAttachment.builder(attachmentDesc);
            attachment.withDataProvider(new ByteArrayDataProvider(contactVCard.getVcard()));
            Attachment vcardAttachment = attachment.build();

            // Either add or replace vCard attachment
            Attachment addedAttachment = processor.addAttachments(Collections.singletonList(vcardAttachment)).get(0);

            SecuritySettings securitySettings = getSecuritySettings(originalDescription.getSecurity());
            MailMessage newDraft = deleteAndSaveDraftMail(draftPath, processor.getOriginalSize(), processor, securitySettings, mailAccess, false, Optional.empty(), session);
            MailPath newDraftPath = newDraft.getMailPath();
            long size = newDraft.getSize();
            if (size < 0) {
                size = fetchMailSize(mailAccess.getMessageStorage(), newDraftPath);
            }
            NewAttachmentsInfo info = new NewAttachmentsInfo(getAttachmentIds(Collections.singletonList(addedAttachment)), processor.getCurrentDraft(), size, newDraft.getSentDate());
            MailStorageId newId = new DefaultMailStorageId(newDraftPath, compositionSpaceId, processor.getFileCacheReference());
            return MailStorageResult.resultFor(newId, info, true, mailAccess, processor);
        } finally {
            Streams.close(draftMimeStream);
            if (mailAccess != null) {
                mailAccess.close(true);
            }
        }
    }

    @Override
    public MailStorageResult<NewAttachmentsInfo> addAttachments(MailStorageId mailStorageId, List<Attachment> attachments, ClientToken clientToken, Session session) throws OXException, MissingDraftException {
        UUID compositionSpaceId = mailStorageId.getCompositionSpaceId();
        MailPath draftPath = mailStorageId.getDraftPath();
        if (draftPath.getAccountId() != MailAccount.DEFAULT_ID) {
            throw CompositionSpaceErrorCode.ERROR.create("Cannot operate on drafts outside of the default mail account!");
        }

        MailService mailService = services.getServiceSafe(MailService.class);
        MailAccess<? extends IMailFolderStorage,? extends IMailMessageStorage> mailAccess = null;
        try {
            mailAccess = mailService.getMailAccess(session, MailAccount.DEFAULT_ID);
            mailAccess.connect(false);

            MailMessageProcessor processor = initMessageProcessorFull(mailStorageId, session, mailAccess, clientToken);
            validateIfNeeded(mailStorageId, processor);
            MessageDescription originalDescription = processor.getCurrentDraft();

            SecuritySettings securitySettings = getSecuritySettings(originalDescription.getSecurity());
            List<Attachment> addedAttachments = processor.addAttachments(attachments);

            MailMessage newDraft = deleteAndSaveDraftMail(draftPath, processor.getOriginalSize(), processor, securitySettings, mailAccess, false, Optional.empty(), session);
            MailPath newDraftPath = newDraft.getMailPath();
            long size = newDraft.getSize();
            if (size < 0) {
                size = fetchMailSize(mailAccess.getMessageStorage(), newDraftPath);
            }

            NewAttachmentsInfo info = new NewAttachmentsInfo(getAttachmentIds(addedAttachments), processor.getCurrentDraft(), size, newDraft.getSentDate());
            DefaultMailStorageId newId = new DefaultMailStorageId(newDraftPath, compositionSpaceId, processor.getFileCacheReference());
            return MailStorageResult.resultFor(newId, info, true, mailAccess, processor);
        } finally {
            if (mailAccess != null) {
                mailAccess.close(true);
            }
        }
    }

    @Override
    public MailStorageResult<NewAttachmentsInfo> replaceAttachment(MailStorageId mailStorageId, Attachment attachment, ClientToken clientToken, Session session) throws OXException, MissingDraftException {
        UUID compositionSpaceId = mailStorageId.getCompositionSpaceId();
        MailPath draftPath = mailStorageId.getDraftPath();
        if (draftPath.getAccountId() != MailAccount.DEFAULT_ID) {
            throw CompositionSpaceErrorCode.ERROR.create("Cannot operate on drafts outside of the default mail account!");
        }

        MailService mailService = services.getServiceSafe(MailService.class);
        MailAccess<? extends IMailFolderStorage,? extends IMailMessageStorage> mailAccess = null;
        MailMessageProcessor processor = null;
        try {
            mailAccess = mailService.getMailAccess(session, MailAccount.DEFAULT_ID);
            mailAccess.connect(false);

            processor = initMessageProcessorFull(mailStorageId, session, mailAccess, clientToken);
            validateIfNeeded(mailStorageId, processor);
            MessageDescription originalDescription = processor.getCurrentDraft();

            SecuritySettings securitySettings = getSecuritySettings(originalDescription.getSecurity());
            Attachment addedAttachment = processor.replaceAttachment(attachment);

            MailMessage newDraft = deleteAndSaveDraftMail(draftPath, processor.getOriginalSize(), processor, securitySettings, mailAccess, false, Optional.empty(), session);
            MailPath newDraftPath = newDraft.getMailPath();
            long size = newDraft.getSize();
            if (size < 0) {
                size = fetchMailSize(mailAccess.getMessageStorage(), newDraftPath);
            }

            NewAttachmentsInfo info = new NewAttachmentsInfo(getAttachmentIds(Collections.singletonList(addedAttachment)), processor.getCurrentDraft(), size, newDraft.getSentDate());
            DefaultMailStorageId newId = new DefaultMailStorageId(newDraftPath, compositionSpaceId, processor.getFileCacheReference());
            return MailStorageResult.resultFor(newId, info, true, mailAccess, processor);
        } finally {
            if (mailAccess != null) {
                mailAccess.close(true);
            }

            closeProcessorSafe(processor);
        }

    }

    @Override
    public MailStorageResult<Attachment> getAttachment(MailStorageId mailStorageId, UUID attachmentId, Session session) throws OXException, MissingDraftException {
        UUID compositionSpaceId = mailStorageId.getCompositionSpaceId();
        MailPath draftPath = mailStorageId.getDraftPath();
        if (draftPath.getAccountId() != MailAccount.DEFAULT_ID) {
            throw CompositionSpaceErrorCode.ERROR.create("Cannot operate on drafts outside of the default mail account!");
        }

        MailService mailService = services.getServiceSafe(MailService.class);
        MailAccess<? extends IMailFolderStorage,? extends IMailMessageStorage> mailAccess = null;
        try {
            mailAccess = mailService.getMailAccess(session, MailAccount.DEFAULT_ID);
            mailAccess.connect(false);

            MailMessageProcessor processor = initMessageProcessorFromFileCache(mailStorageId, session, null, ClientToken.NONE);
            if (processor != null) {
                Attachment attachment = processor.getAttachment(attachmentId);
                return MailStorageResult.resultFor(mailStorageId, attachment, false, mailAccess);
            }

            MailMessage draftMail = requireDraftMail(mailStorageId, mailAccess);

            Attachment attachment = MailMessageProcessor.attachmentLookUp(attachmentId, compositionSpaceId, draftMail, session, services);
            DefaultMailStorageId newId = new DefaultMailStorageId(draftMail.getMailPath(), compositionSpaceId, mailStorageId.getFileCacheReference());
            return MailStorageResult.resultFor(newId, attachment, false, mailAccess);
        } finally {
            if (mailAccess != null) {
                mailAccess.close(true);
            }
        }
    }

    @Override
    public MailStorageResult<MessageInfo> deleteAttachments(MailStorageId mailStorageId, List<UUID> attachmentIds, ClientToken clientToken, Session session) throws OXException, MissingDraftException {
        UUID compositionSpaceId = mailStorageId.getCompositionSpaceId();
        MailPath draftPath = mailStorageId.getDraftPath();
        if (draftPath.getAccountId() != MailAccount.DEFAULT_ID) {
            throw CompositionSpaceErrorCode.ERROR.create("Cannot operate on drafts outside of the default mail account!");
        }

        MailService mailService = services.getServiceSafe(MailService.class);
        MailAccess<? extends IMailFolderStorage,? extends IMailMessageStorage> mailAccess = null;
        InputStream draftMimeStream = null;
        MailMessageProcessor processor = null;
        try {
            mailAccess = mailService.getMailAccess(session, MailAccount.DEFAULT_ID);
            mailAccess.connect(false);

            processor = initMessageProcessorFull(mailStorageId, session, mailAccess, clientToken);
            validateIfNeeded(mailStorageId, processor);
            MessageDescription originalDescription = processor.getCurrentDraft(MessageField.SECURITY);

            SecuritySettings securitySettings = getSecuritySettings(originalDescription.getSecurity());

            processor.deleteAttachments(attachmentIds);

            MailMessage newDraft = deleteAndSaveDraftMail(draftPath, processor.getOriginalSize(), processor, securitySettings, mailAccess, false, Optional.empty(), session);
            MailPath newDraftPath = newDraft.getMailPath();
            long size = newDraft.getSize();
            if (size < 0) {
                size = fetchMailSize(mailAccess.getMessageStorage(), newDraftPath);
            }

            DefaultMailStorageId newId = new DefaultMailStorageId(newDraftPath, compositionSpaceId, processor.getFileCacheReference());
            MessageInfo messageInfo = new MessageInfo(processor.getCurrentDraft(), size, newDraft.getSentDate());
            return MailStorageResult.resultFor(newId, messageInfo, true, mailAccess, processor);
        } finally {
            Streams.close(draftMimeStream);
            if (mailAccess != null) {
                mailAccess.close(true);
            }

            closeProcessorSafe(processor);
        }
    }

    @Override
    public MailStorageResult<Quota> getStorageQuota(Session session) throws OXException {
        MailService mailService = services.getServiceSafe(MailService.class);
        MailAccess<? extends IMailFolderStorage,? extends IMailMessageStorage> mailAccess = null;
        try {
            mailAccess = mailService.getMailAccess(session, MailAccount.DEFAULT_ID);
            mailAccess.connect(false);

            IMailFolderStorage folderStorage = mailAccess.getFolderStorage();
            String draftsFolder = folderStorage.getDraftsFolder();
            Quota storageQuota = mailAccess.getFolderStorage().getStorageQuota(draftsFolder);
            return MailStorageResult.resultFor(null, storageQuota, false, mailAccess);
        } finally {
            if (mailAccess != null) {
                mailAccess.close(true);
            }
        }
    }

    @Override
    public MailStorageResult<Optional<MailPath>> validate(MailStorageId mailStorageId, Session session) throws OXException, MissingDraftException {
        // Currently this method only validates against shared attachments folder content

        UUID compositionSpaceId = mailStorageId.getCompositionSpaceId();
        MailPath draftPath = mailStorageId.getDraftPath();
        if (draftPath.getAccountId() != MailAccount.DEFAULT_ID) {
            throw CompositionSpaceErrorCode.ERROR.create("Cannot operate on drafts outside of the default mail account!");
        }

        MailService mailService = services.getServiceSafe(MailService.class);
        MailAccess<? extends IMailFolderStorage,? extends IMailMessageStorage> mailAccess = null;
        try {
            mailAccess = mailService.getMailAccess(session, MailAccount.DEFAULT_ID);
            mailAccess.connect(false);

            MailMessage draftMail = requireDraftMail(mailStorageId, mailAccess);

            SharedAttachmentsInfo sharedAttachmentsInfo = convertSharedAttachmentsInfo(draftMail);
            if (sharedAttachmentsInfo == null || sharedAttachmentsInfo.isDisabled()) {
                // Shared attachments not enabled
                return MailStorageResult.resultFor(null, Optional.empty(), true, mailAccess);
            }

            SharedFolderReference sharedFolderRef = convertSharedFolderReference(draftMail);
            if (sharedFolderRef == null) {
                // No shared attachments folder available
                return MailStorageResult.resultFor(null, Optional.empty(), true, mailAccess);
            }

            MailMessageProcessor processor = MailMessageProcessor.initForWrite(compositionSpaceId, draftMail, session, services);

            boolean changed = processor.validate();
            if (false == changed) {
                return MailStorageResult.resultFor(null, Optional.empty(), true, mailAccess, processor);
            }

            MessageDescription currentDraft = processor.getCurrentDraft();
            SecuritySettings securitySettings = getSecuritySettings(currentDraft.getSecurity());
            MailPath newDraftPath = deleteAndSaveDraftMail(draftMail, processor, securitySettings, mailAccess, false, Optional.empty(), session).getMailPath();
            MailStorageId newId = new DefaultMailStorageId(newDraftPath, compositionSpaceId, processor.getFileCacheReference());
            return MailStorageResult.resultFor(newId, Optional.of(newDraftPath), true, mailAccess, processor);
        } finally {
            if (mailAccess != null) {
                mailAccess.close(true);
            }
        }
    }

    private static final MailField[] MAIL_FIELDS_ID = new MailField[] { MailField.ID };

    private Optional<MailPath> doLookUp(UUID compositionSpaceId, String draftsFolder, IMailMessageStorage messageStorage) throws OXException {
        SearchTerm<?> searchTerm = new HeaderTerm(HeaderUtility.HEADER_X_OX_COMPOSITION_SPACE_ID, UUIDs.getUnformattedString(compositionSpaceId));
        MailMessage[] mailMessages = messageStorage.searchMessages(draftsFolder, IndexRange.NULL, MailSortField.RECEIVED_DATE, OrderDirection.DESC, searchTerm, MAIL_FIELDS_ID);

        if (mailMessages == null || mailMessages.length == 0 || mailMessages[0] == null) {
            LOG.debug("Found no draft message for composition space {}", getUUIDForLogging(compositionSpaceId));
            return Optional.empty();
        }

        LOG.debug("Found draft message for composition space {}: {}", getUUIDForLogging(compositionSpaceId), mailMessages[0].getMailPath());
        return Optional.of(new MailPath(MailAccount.DEFAULT_ID, draftsFolder, mailMessages[0].getMailId()));
    }

    private boolean validateIfNeeded(MailStorageId mailStorageId, MailMessageProcessor processor) throws OXException {
        return (mailStorageId instanceof ValidateAwareMailStorageId) && ((ValidateAwareMailStorageId) mailStorageId).needsValidation() && processor.validate();
    }

    private long getMaxIdleTimeMillis(Session session) throws OXException {
        String defaultValue = "1W";

        ConfigViewFactory viewFactory = services.getOptionalService(ConfigViewFactory.class);
        if (null == viewFactory) {
            return ConfigTools.parseTimespan(defaultValue);
        }

        ConfigView view = viewFactory.getView(session.getUserId(), session.getContextId());
        return ConfigTools.parseTimespan(ConfigViews.getDefinedStringPropertyFrom("com.openexchange.mail.compose.maxIdleTimeMillis", defaultValue, view));
    }

    /**
     * Gets the max. number of allowed concurrent composition spaces.
     *
     * @param session The session
     * @return The max. number of allowed composition spaces
     * @throws OXException If number cannot be returned
     */
    private int getMaxSpacesPerUser(Session session) throws OXException {
        int defaultValue = 20;

        ConfigViewFactory viewFactory = services.getOptionalService(ConfigViewFactory.class);
        if (null == viewFactory) {
            return defaultValue;
        }

        ConfigView view = viewFactory.getView(session.getUserId(), session.getContextId());
        return ConfigViews.getDefinedIntPropertyFrom("com.openexchange.mail.compose.maxSpacesPerUser", defaultValue, view);
    }

    private static void closeProcessorSafe(MailMessageProcessor processor) {
        if (processor != null) {
            try {
                processor.close();
            } catch (Exception e) {
                LOG.warn("Failed to close mail message processor", e);
            }
        }
    }

    private static Security convertSecurity(MailMessage draftMail) {
        String headerValue = HeaderUtility.decodeHeaderValue(draftMail.getFirstHeader(HeaderUtility.HEADER_X_OX_SECURITY));
        return HeaderUtility.headerValue2Security(headerValue);
    }

    private static Security convertSecurity(HeaderCollection headers) {
        String headerValue = HeaderUtility.decodeHeaderValue(headers.getHeader(HeaderUtility.HEADER_X_OX_SECURITY, null));
        return HeaderUtility.headerValue2Security(headerValue);
    }

    private static SharedAttachmentsInfo convertSharedAttachmentsInfo(MailMessage draftMail) {
        String headerValue = HeaderUtility.decodeHeaderValue(draftMail.getFirstHeader(HeaderUtility.HEADER_X_OX_SHARED_ATTACHMENTS));
        return HeaderUtility.headerValue2SharedAttachments(headerValue);
    }

    private static SharedFolderReference convertSharedFolderReference(MailMessage draftMail) {
        String headerValue = HeaderUtility.decodeHeaderValue(draftMail.getFirstHeader(HeaderUtility.HEADER_X_OX_SHARED_FOLDER_REFERENCE));
        return HeaderUtility.headerValue2SharedFolderReference(headerValue);
    }

    private static MessageDescription toMessageDescription(MailMessage mailMessage, Set<MessageField> fields) {
        MessageDescription draftMessage = new MessageDescription();
        for (MessageField field : fields) {
            switch (field) {
                case ATTACHMENTS:
                    throw new UnsupportedOperationException();
                case BCC:
                    draftMessage.setBcc(MailMessageProcessor.convertAddresses(mailMessage.getBcc()));
                    break;
                case CC:
                    draftMessage.setCc(MailMessageProcessor.convertAddresses(mailMessage.getCc()));
                    break;
                case CONTENT:
                    //$FALL-THROUGH$
                case CONTENT_ENCRYPTED:
                    throw new UnsupportedOperationException();
                case CONTENT_TYPE: {
                        String headerValue = HeaderUtility.decodeHeaderValue(mailMessage.getFirstHeader(HeaderUtility.HEADER_X_OX_CONTENT_TYPE));
                        ContentType contentType = ContentType.contentTypeFor(headerValue);
                        draftMessage.setContentType(contentType);
                    }
                    break;
                case CUSTOM_HEADERS:
                    Map<String, String> customHeaders = convertCustomHeaders(mailMessage);
                    if (customHeaders != null) {
                        draftMessage.setCustomHeaders(customHeaders);
                    }
                    break;
                case FROM:
                    draftMessage.setFrom(MailMessageProcessor.convertFirstAddress(mailMessage.getFrom()));
                    break;
                case META: {
                        String headerValue = HeaderUtility.decodeHeaderValue(mailMessage.getFirstHeader(HeaderUtility.HEADER_X_OX_META));
                        Meta parsedMeta = HeaderUtility.headerValue2Meta(headerValue);
                        draftMessage.setMeta(parsedMeta);
                    }
                    break;
                case PRIORITY:
                    draftMessage.setPriority(convertPriority(mailMessage));
                    break;
                case REPLY_TO:
                    draftMessage.setReplyTo(MailMessageProcessor.convertFirstAddress(MimeMessageUtility.getAddressHeader(MessageHeaders.HDR_REPLY_TO, mailMessage)));
                    break;
                case REQUEST_READ_RECEIPT:
                    draftMessage.setRequestReadReceipt("true".equals(HeaderUtility.decodeHeaderValue(mailMessage.getFirstHeader(HeaderUtility.HEADER_X_OX_READ_RECEIPT))));
                    break;
                case SECURITY: {
                        String headerValue = HeaderUtility.decodeHeaderValue(mailMessage.getFirstHeader(HeaderUtility.HEADER_X_OX_SECURITY));
                        Security parsedSecurity = HeaderUtility.headerValue2Security(headerValue);
                        draftMessage.setSecurity(parsedSecurity);
                    }
                    break;
                case SENDER:
                    break;
                case SHARED_ATTACCHMENTS_INFO: {
                        String headerValue = HeaderUtility.decodeHeaderValue(mailMessage.getFirstHeader(HeaderUtility.HEADER_X_OX_SHARED_ATTACHMENTS));
                        SharedAttachmentsInfo parsedSharedAttachments = HeaderUtility.headerValue2SharedAttachments(headerValue);
                        draftMessage.setsharedAttachmentsInfo(parsedSharedAttachments);
                    }
                    break;
                case SUBJECT:
                    draftMessage.setSubject(mailMessage.getSubject());
                    break;
                case TO:
                    draftMessage.setTo(MailMessageProcessor.convertAddresses(mailMessage.getTo()));
                    break;
                default:
                    break;
            }
        }
        return draftMessage;
    }

    private static Map<String, String> convertCustomHeaders(MailMessage mailMessage) {
        String headerValue = HeaderUtility.decodeHeaderValue(mailMessage.getFirstHeader(HeaderUtility.HEADER_X_OX_CUSTOM_HEADERS));
        return HeaderUtility.headerValue2CustomHeaders(headerValue);
    }

    private static Priority convertPriority(MailMessage mailMessage) {
        Priority priority = null;
        String priorityStr = mailMessage.getFirstHeader(MessageHeaders.HDR_X_PRIORITY);
        if (Strings.isNotEmpty(priorityStr)) {
            try {
                int level = Integer.parseInt(priorityStr);
                priority = Priority.priorityForLevel(level);
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        if (priority == null) {
            String importanceStr = mailMessage.getFirstHeader(MessageHeaders.HDR_IMPORTANCE);
            if (Strings.isNotEmpty(importanceStr)) {
                priority = Priority.priorityFor(importanceStr);
            }
        }

        return priority;
    }

    private static MailFields toMailFields(Set<MessageField> fields) {
        if (fields == null || fields.isEmpty()) {
            return new MailFields(MailField.FULL);
        }

        MailFields mailFields = new MailFields();
        for (MessageField messageField : fields) {
            switch (messageField) {
                case ATTACHMENTS:
                    return new MailFields(MailField.FULL);
                case BCC:
                    mailFields.add(MailField.BCC);
                    break;
                case CC:
                    mailFields.add(MailField.CC);
                    break;
                case CONTENT_ENCRYPTED:
                    // fall-through
                case CONTENT:
                    return new MailFields(MailField.FULL);
                case CONTENT_TYPE:
                    mailFields.add(MailField.HEADERS);
                    break;
                case CUSTOM_HEADERS:
                    mailFields.add(MailField.HEADERS);
                    break;
                case FROM:
                    mailFields.add(MailField.FROM);
                    break;
                case META:
                    mailFields.add(MailField.HEADERS);
                    break;
                case PRIORITY:
                    mailFields.add(MailField.HEADERS);
                    break;
                case REPLY_TO:
                    mailFields.add(MailField.HEADERS);
                    break;
                case REQUEST_READ_RECEIPT:
                    mailFields.add(MailField.HEADERS);
                    break;
                case SECURITY:
                    mailFields.add(MailField.HEADERS);
                    break;
                case SENDER:
                    mailFields.add(MailField.HEADERS);
                    break;
                case SHARED_ATTACCHMENTS_INFO:
                    mailFields.add(MailField.HEADERS);
                    break;
                case SUBJECT:
                    mailFields.add(MailField.SUBJECT);
                    break;
                case TO:
                    mailFields.add(MailField.TO);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown message field: " + messageField);
            }
        }
        return mailFields;
    }

    private static final MailField[] MAIL_FIELDS_SIZE = new MailField[] { MailField.SIZE };

    private static long fetchMailSize(IMailMessageStorage draftMessageStorage, MailPath mailPath) {
        try {
            LOG.debug("Fetching mail size of draft {}", mailPath);
            MailMessage[] messages = draftMessageStorage.getMessages(mailPath.getFolder(), new String[] { mailPath.getMailID() }, MAIL_FIELDS_SIZE);
            if (messages != null && messages.length > 0 && messages[0] != null) {
                return messages[0].getSize();
            }

            LOG.warn("Could not fetch size of draft message due to empty response");
        } catch (OXException e) {
            LOG.warn("Error while fetching size of draft message", e);
        }

        return -1L;
    }

    private static List<UUID> getAttachmentIds(List<Attachment> attachments) {
        return attachments.stream().map(Attachment::getId).collect(toList());
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static HeadersAndStream parseHeaders(InputStream mimeStream) throws OXException {
        TrackingInputStream trackingInputStream = new TrackingInputStream(mimeStream);
        HeaderCollection hc = new HeaderCollection(trackingInputStream);
        return new HeadersAndStream(hc, new CombinedInputStream(trackingInputStream.getReadBytes(), mimeStream));
    }

    /**
     * Checks and adjusts security settings updates and returns the to-be-used
     * instance for next encryption attempt. Takes care of auth token changes to
     * be correctly applied.
     *
     * @param originalDescription {@link MessageDescription} from upstream mail message
     * @param newDescription {@link MessageDescription} from update request
     * @return The recent {@link SecuritySettings} to be used
     */
    private SecuritySettings prepareSecuritySettings(MessageDescription originalDescription, MessageDescription newDescription) {
        if (originalDescription.containsNotNullSecurity() && newDescription.containsNotNullSecurity()) {
            Security newSecurity = null;
            if (newDescription.getSecurity().isEncrypt() && Strings.isEmpty(newDescription.getSecurity().getAuthToken())) {
                //we need to preserve the authentication token from the existing draft, if the caller wants us to encrypt but is missing an authToken
                //otherwise the token would get overwritten and de-cryption would fail the next time
                newSecurity = Security.builder(newDescription.getSecurity()).withAuthToken(originalDescription.getSecurity().getAuthToken()).build();
            } else if (newDescription.getSecurity().isEncrypt() == false && Strings.isNotEmpty(originalDescription.getSecurity().getAuthToken())) {
                //Remove the auth-token from the draft, because we don't need it anymore
                newSecurity = Security.builder(newDescription.getSecurity()).withAuthToken(null).build();
            }
            if (newSecurity != null) {
                newDescription.setSecurity(newSecurity);
            }
        }

        SecuritySettings securitySettings = getSecuritySettings(getSecurity(originalDescription));
        Security prevSecurity = getSecurity(originalDescription);
        if (newDescription.containsSecurity()) {
            Security newSecurity = getSecurity(newDescription);
            if (prevSecurity.isDisabled() != newSecurity.isDisabled()) {
                if (newSecurity.isDisabled()) {
                    securitySettings = null;
                } else {
                    securitySettings = getSecuritySettings(newSecurity);
                }
            }
        }

        return securitySettings;
    }

    /**
     * Fetches attachment parts from a given mail message and converts them into {@link Attachment} instances
     * that can be e.g. added to another message.
     */
    private List<Attachment> fetchOriginalAttachments(Session session, UUID compositionSpaceId, MailMessageProcessor processor, MailMessage originalMail) throws OXException {
        if (originalMail.getContentType().startsWith("multipart/")) {
            // Grab first seen text from original message and check for possible referenced inline images
            List<String> contentIds = new ArrayList<String>();
            MimeProcessingUtility.getTextForForward(originalMail, true, false, contentIds, session);

            // Add mail's non-inline parts
            NonInlineForwardPartHandler handler = new NonInlineForwardPartHandler();
            if (false == contentIds.isEmpty()) {
                handler.setImageContentIds(contentIds);
            }

            new MailMessageParser().setInlineDetectorBehavior(true).parseMailMessage(originalMail, handler);

            List<MailPart> nonInlineParts = handler.getNonInlineParts();
            List<Attachment> newAttachments = new ArrayList<>(nonInlineParts.size());
            for (MailPart mailPart : nonInlineParts) {
                Attachment newAttachment;
                if (mailPart.containsContentDisposition() && mailPart.getContentDisposition().isInline()) {
                    newAttachment = processor.createNewInlineAttachmentFor(mailPart, compositionSpaceId, true);
                } else {
                    newAttachment = processor.createNewAttachmentFor(mailPart, compositionSpaceId, true);
                }
                newAttachments.add(newAttachment);
            }

            return newAttachments;
        }

        return Collections.emptyList();
    }

    private MailMessage getOriginalMail(Session session, MailPath mailPath, MailService mailService, List<MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage>> mailAccesses, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> defaultMailAccess, String authToken) throws OXException {
        Optional<MailMessage> optionalMail;
        if (mailPath.getAccountId() == MailAccount.DEFAULT_ID) {
            optionalMail = getMail(mailPath.getMailID(), mailPath.getFolder(), defaultMailAccess.getMessageStorage());
            if (optionalMail.isPresent() && mayDecrypt(session)) {
                PGPMailRecognizer optPgpRecognizer = services.getOptionalService(PGPMailRecognizer.class);
                if (optPgpRecognizer != null && !optPgpRecognizer.isPGPMessage(optionalMail.get()) && !optPgpRecognizer.isPGPSignedMessage(optionalMail.get())) {
                    // Non-encrypted
                    return optionalMail.get();
                }

                defaultMailAccess = createCryptographicAwareAccess(defaultMailAccess, authToken);
                optionalMail = getMail(mailPath.getMailID(), mailPath.getFolder(), defaultMailAccess.getMessageStorage());
            }
        } else {
            MailAccess<? extends IMailFolderStorage,? extends IMailMessageStorage> otherAccess = mailService.getMailAccess(session, mailPath.getAccountId());
            mailAccesses.add(otherAccess);
            otherAccess.connect(false);
            optionalMail = getMail(mailPath.getMailID(), mailPath.getFolder(), otherAccess.getMessageStorage());
            if (optionalMail.isPresent() && mayDecrypt(session)) {
                PGPMailRecognizer optPgpRecognizer = services.getOptionalService(PGPMailRecognizer.class);
                if (optPgpRecognizer != null && !optPgpRecognizer.isPGPMessage(optionalMail.get()) && !optPgpRecognizer.isPGPSignedMessage(optionalMail.get())) {
                    // Non-encrypted
                    return optionalMail.get();
                }

                otherAccess = createCryptographicAwareAccess(otherAccess, authToken);
                optionalMail = getMail(mailPath.getMailID(), mailPath.getFolder(), otherAccess.getMessageStorage());
            }
        }

        return optionalMail.orElseThrow(() -> MailExceptionCode.MAIL_NOT_FOUND.create(mailPath.getMailID(), mailPath.getFolderArgument()));
    }

    /**
     * Applies changes of the {@link SharedAttachmentsInfo} instance of the updated {@link MessageDescription} to the {@link MailMessageProcessor}.
     *
     * @param original The original message description
     * @param update The updated message description
     * @param processor The message processor
     * @param session The session
     * @throws OXException
     */
    private void applySharedAttachmentsChanges(MessageDescription original, MessageDescription update, MailMessageProcessor processor, Session session) throws OXException {
        // Check if shared attachments feature has been enabled/disabled
        SharedAttachmentsInfo prevSharedAttachmentsInfo = getSharedAttachmentsInfo(original);
        if (update.containsSharedAttachmentsInfo()) {
            SharedAttachmentsInfo newSharedAttachmentsInfo = getSharedAttachmentsInfo(update);
            if (prevSharedAttachmentsInfo.isEnabled() != newSharedAttachmentsInfo.isEnabled()) {
                if (newSharedAttachmentsInfo.isEnabled()) {
                    // Shared attachments enabled.
                    if (false == processor.mayShareAttachments()) {
                        // User wants to share attachments, but is not allowed to do so
                        throw MailExceptionCode.SHARING_NOT_POSSIBLE.create(I(session.getUserId()), I(session.getContextId()));
                    }

                    // Save attachments into attachment storage.
                    processor.storeAttachments();
                } else {
                    // Shared attachments disabled
                    processor.unstoreAttachments();
                }
            }
        }
    }

    private void tryCleanUpFileCacheReference(MailStorageId mailStorageId) {
        if (mailStorageId.hasFileCacheReference()) {
            try {
                mailStorageId.getFileCacheReference().get().cleanUp();
            } catch (Exception e) {
                LOG.error("Unable to clean-up sppol reference for composition space: {}", mailStorageId, e);
            }
        }
    }

    private MailMessage deleteAndSaveDraftMail(MailMessage draftMail, MailMessageProcessor processor, SecuritySettings securitySettings, MailAccess<? extends IMailFolderStorage,? extends IMailMessageStorage> mailAccess, boolean asFinalDraft, Optional<MailMessage> optRefMessage, Session session) throws OXException {
        return deleteAndSaveDraftMail(draftMail.getMailPath(), draftMail.getSize(), processor, securitySettings, mailAccess, asFinalDraft, optRefMessage, session);
    }

    private MailMessage deleteAndSaveDraftMail(MailPath draftPath, long oldMessageSize, MailMessageProcessor processor, SecuritySettings securitySettings, MailAccess<? extends IMailFolderStorage,? extends IMailMessageStorage> mailAccess, boolean asFinalDraft, Optional<MailMessage> optRefMessage, Session session) throws OXException {
        // Retrieve quota
        Quota storageQuota = mailAccess.getFolderStorage().getQuotas(draftPath.getFolder(), new Quota.Type[] { Quota.Type.STORAGE })[0];
        if (storageQuota.getLimit() == 0) {
            // Not possible due to quota restrictions
            throw MailExceptionCode.UNABLE_TO_SAVE_DRAFT_QUOTA.create();
        }

        // Create the new draft mail
        IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
        ComposedMailMessage newDraftMail = asFinalDraft ? processor.compileFinalDraft(optRefMessage) : processor.compileDraft();

        newDraftMail = applyGuardEncryption(securitySettings, newDraftMail, session);
        return deleteAndSaveDraftMailSafe(draftPath, storageQuota, messageStorage, newDraftMail);
    }

    /**
     * Checks available quota for enough space for a full copy followed by writing the new draft message. Only on success the former draft is deleted.
     *
     * @param draftPath The path to the current draft mail
     * @param storageQuota The storage quota providing limit and usage in bytes
     * @param messageStorage The message storage to use
     * @param newDraftMail The new draft mail to store
     * @return The new draft message
     * @throws OXException If deleting old and storing new draft mail fails
     */
    private MailMessage deleteAndSaveDraftMailSafe(MailPath draftPath, Quota storageQuota, IMailMessageStorage messageStorage, ComposedMailMessage newDraftMail) throws OXException {
        // Check against quota limit
        if (storageQuota.getLimitBytes() > 0) {
            long newSize = determineSizeOf(newDraftMail);
            checkAvailableQuota(storageQuota, newSize);
        }

        // Prepare new draft mail accordingly.
        newDraftMail.setSendType(ComposeType.DRAFT);

        // Save new draft mail (and thus delete previous draft mail)
        MailMessage savedDraft = saveDraftMail(newDraftMail, draftPath.getFolder(), true, messageStorage);

        // Delete with conflict detection in case enhanced deletion is supported
        boolean deleteFailed = true;
        try {
            IMailMessageStorageEnhancedDeletion enhancedDeletion = messageStorage.supports(IMailMessageStorageEnhancedDeletion.class);
            if (enhancedDeletion == null || !enhancedDeletion.isEnhancedDeletionSupported()) {
                // Delete by best guess...
                LOG.debug("Deleting old draft {}", draftPath);
                messageStorage.deleteMessages(draftPath.getFolder(), new String[] { draftPath.getMailID() }, true);
            } else {
                // Try to delete current draft mail in storage
                LOG.debug("Hard-deleting old draft {}", draftPath);
                MailPath[] removedPaths = enhancedDeletion.hardDeleteMessages(draftPath.getFolder(), new String[] { draftPath.getMailID() });
                if (removedPaths == null || removedPaths.length <= 0 || !draftPath.equals(removedPaths[0])) {
                    LOG.warn("Another process deleted draft mail '{}' in the meantime", draftPath);
                }
            }

            // Return new draft path
            deleteFailed = false;
            return savedDraft;
        } finally {
            if (deleteFailed) {
                MailPath newDraftPath = savedDraft.getMailPath();
                LOG.debug("Delete of {} failed => deleting newly saved draft {} again", draftPath, newDraftPath);
                messageStorage.deleteMessages(newDraftPath.getFolder(), new String[] { newDraftPath.getMailID() }, true);
            }
        }
    }

    /**
     * Checks if given additional bytes fit into current quota
     *
     * @param storageQuota The storage quota known to have a limitation greater than <code>0</code> (zero)
     * @param newSize Number of bytes to store
     * @throws {@link MailExceptionCode#UNABLE_TO_SAVE_DRAFT_QUOTA} in case quota would be exceeded
     */
    private void checkAvailableQuota(Quota storageQuota, long newSize) throws OXException {
        if (!MailStorageCompositionSpaceConfig.getInstance().isEagerUploadChecksEnabled()) {
            LOG.debug("Skipping eager quota checks because they are disabled");
            return;
        }

        if (newSize > 0 && storageQuota.getUsageBytes() + newSize > storageQuota.getLimitBytes()) {
            // Not possible due to quota restrictions
            LOG.debug("Would exceed storage quota by {} bytes", L((storageQuota.getUsageBytes() + newSize) - storageQuota.getLimitBytes()));
            throw MailExceptionCode.UNABLE_TO_SAVE_DRAFT_QUOTA.create();
        }
    }

    private long determineSizeOf(ComposedMailMessage newDraftMail) throws OXException {
        LOG.debug("Determinining size of new draft by counting");
        CountingOutputStream out = null;
        try {
            out = new CountingOutputStream();
            ((MimeMessage) newDraftMail.getContent()).writeTo(out);
            out.flush();
            return out.getCount();
        } catch (IOException e) {
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        } finally {
            Streams.close(out);
        }
    }

    private MailMessageProcessor initMessageProcessorFull(MailStorageId mailStorageId, Session session, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess, ClientToken clientToken) throws OXException, MissingDraftException {
        return initMessageProcessorFull(mailStorageId, session, mailAccess, null, clientToken);
    }

    private MailMessageProcessor initMessageProcessorFull(MailStorageId mailStorageId, Session session, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess, String authToken, ClientToken clientToken) throws OXException, MissingDraftException {
        MailMessageProcessor processor = initMessageProcessorFromFileCache(mailStorageId, session, authToken, clientToken);
        if (processor == null) {
            InputStream mimeStream = null;
            try {
                mimeStream = requireDraftMimeStream(mailStorageId, mailAccess, authToken);
                processor = MailMessageProcessor.initForWrite(mailStorageId.getCompositionSpaceId(), mimeStream, session, services);
                checkClientToken(clientToken, processor.getClientToken());

                LOG.debug("Initialized message processor for composition space from fetched MIME stream: {}", mailStorageId);
            } finally {
                Streams.close(mimeStream);
            }
        }

        return processor;
    }

    /**
     * Initializes a {@link MailMessageProcessor} from the file cache reference of given {@link MailStorageId}.
     *
     * @param mailStorageId The mail storage ID
     * @param session The user session
     * @param authToken The optionally new Guard auth token
     * @return The processor or <code>null</code> if file cache reference is missing or invalid
     * @throws OXException If initialization fails for other reasons than a missing/invalid file cache reference
     */
    private MailMessageProcessor initMessageProcessorFromFileCache(MailStorageId mailStorageId, Session session, String authToken, ClientToken clientToken) throws OXException {
        UUID compositionSpaceId = mailStorageId.getCompositionSpaceId();
        if (mailStorageId.hasValidFileCacheReference()) {
            CacheReference cacheReference = mailStorageId.getFileCacheReference().get();
            MailMessageProcessor processor = null;
            try {
                processor = MailMessageProcessor.initFromFileCache(compositionSpaceId, cacheReference, session, services);
                if (clientToken.isPresent() && clientToken.isNotEquals(processor.getClientToken())) {
                    LOG.debug("Client token mismatch for cached message. Expected: '{}' but was '{}'. Clearing cache to retry.", processor.getClientToken(), clientToken);
                    // force re-fetch to properly detect concurrent modification
                    cacheReference.cleanUp();
                    return null;
                }

                LOG.debug("Initialized message processor for composition space from file cache reference: {}", mailStorageId);
                return processor;
            } catch (OXException e) {
                if (CompositionSpaceErrorCode.IO_ERROR.equals(e)) {
                    LOG.debug("File cache reference for composition space is not readable: {}", mailStorageId, e);
                } else {
                    LOG.error("Failed to initialize message processor from file cache reference: {}", mailStorageId, e);
                    throw e;
                }
            }
        }

        return null;
    }

    private InputStream requireDraftMimeStream(MailStorageId mailStorageId, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess, String authToken) throws OXException, MissingDraftException {
        MailPath draftPath = mailStorageId.getDraftPath();
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccezz = mailAccess;
        IMailMessageStorage messageStorage = mailAccezz.getMessageStorage();

        Optional<InputStream> optionalMimeStream = getMimeStream(draftPath, messageStorage);
        if (optionalMimeStream.isPresent()) {
            InputStream mimeStream = optionalMimeStream.get();
            try {
                HeadersAndStream parsedHeaders = parseHeaders(mimeStream);
                Security security = convertSecurity(parsedHeaders.headers);
                if (!security.isEncrypt()) {
                    mimeStream = null; // Avoid premature closing
                    return parsedHeaders.mimeStream;
                }

                Streams.close(mimeStream);
                mimeStream = null;

                String authTokenToUse = authToken;
                if (authTokenToUse == null) {
                    authTokenToUse = security.getAuthToken();
                }

                mailAccezz = createCryptographicAwareAccess(mailAccezz, authTokenToUse);
                messageStorage = mailAccezz.getMessageStorage();

                optionalMimeStream = getMimeStream(draftPath, messageStorage);
                if (optionalMimeStream.isPresent()) {
                    return optionalMimeStream.get();
                }
            } finally {
                Streams.close(mimeStream);
            }
        }

        throw new MissingDraftException(mailStorageId);
    }

    private Optional<InputStream> getMimeStream(MailPath mailPath, IMailMessageStorage messageStorage) throws OXException {
        return getMimeStream(mailPath.getMailID(), mailPath.getFolder(), messageStorage);
    }

    private Optional<InputStream> getMimeStream(String mailId, String fullName, IMailMessageStorage messageStorage) throws OXException {
        try {
            InputStream in = null;
            IMailMessageStorageMimeSupport mimeSupport = messageStorage.supports(IMailMessageStorageMimeSupport.class);
            if (mimeSupport != null && mimeSupport.isMimeSupported()) {
                in = mimeSupport.getMimeStream(fullName, mailId);
            } else {
                MailMessage mail = messageStorage.getMessage(fullName, mailId, false);
                if (mail != null) {
                    in = MimeMessageUtility.getStreamFromMailPart(mail);
                }
            }

            if (in == null) {
                LOG.debug("Failed to fetch full MIME stream of draft {}/{} because mail does not exist anymore", fullName, mailId);
            } else {
                LOG.debug("Fetched full MIME stream of draft {}/{}", fullName, mailId);
            }
            return Optional.ofNullable(in);
        } catch (OXException e) {
            if (MailExceptionCode.MAIL_NOT_FOUND.equals(e)) {
                LOG.debug("Failed to fetch full MIME stream of draft {}/{} because mail does not exist anymore", fullName, mailId);
                return Optional.empty();
            }

            LOG.warn("Failed to fetch full MIME stream of draft {}/{}", fullName, mailId, e);
            throw e;
        }
    }

    /**
     * Gets a {@link MailMessage}. Tries to decrypt if required.
     *
     * @param mailStorageId The mail storage id
     * @param mailAccess The {@link MailAccess} to use
     * @return The {@link MailMessage}
     * @throws OXException
     * @throws MissingDraftException
     */
    private MailMessage requireDraftMail(MailStorageId mailStorageId, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws OXException, MissingDraftException {
        return requireDraftMail(mailStorageId, mailAccess, true);
    }

    /**
     * Gets a {@link MailMessage}
     *
     * @param mailStorageId The mail storage id
     * @param mailAccess The {@link MailAccess} to use
     * @param decryptIfRequired <code>True</code> in order to decrypt the message if required, <code>False</code> to return the raw PGP message in case it is encrypted.
     * @return The {@link MailMessage}
     * @throws OXException
     * @throws MissingDraftException
     */
    private MailMessage requireDraftMail(MailStorageId mailStorageId, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess, boolean decryptIfRequired) throws OXException, MissingDraftException {
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccezz = mailAccess;
        IMailMessageStorage messageStorage = mailAccezz.getMessageStorage();

        MailPath draftPath = mailStorageId.getDraftPath();

        Optional<MailMessage> optionalDraftMail = getMail(draftPath, messageStorage);
        if (optionalDraftMail.isPresent()) {
            MailMessage mailMessage = optionalDraftMail.get();

            Security security = convertSecurity(mailMessage);
            if (!decryptIfRequired || !security.isEncrypt()) {
                return mailMessage;
            }

            mailAccezz = createCryptographicAwareAccess(mailAccezz, security.getAuthToken());
            messageStorage = mailAccezz.getMessageStorage();

            optionalDraftMail = getMail(draftPath, messageStorage);
            if (optionalDraftMail.isPresent()) {
                return optionalDraftMail.get();
            }
        }

        throw new MissingDraftException(mailStorageId);
    }

    private Optional<MailMessage> getMail(MailPath mailPath, IMailMessageStorage messageStorage) throws OXException {
        return getMail(mailPath.getMailID(), mailPath.getFolder(), messageStorage);
    }

    private Optional<MailMessage> getMail(String mailId, String fullName, IMailMessageStorage messageStorage) throws OXException {
        try {
            MailMessage mail = messageStorage.getMessage(fullName, mailId, false);
            if (mail == null) {
                LOG.debug("Failed to fetch full draft {}/{} because mail does not exist anymore", fullName, mailId);
            } else {
                LOG.debug("Fetched full draft {}/{}", fullName, mailId);
            }
            return Optional.ofNullable(mail);
        } catch (OXException e) {
            if (MailExceptionCode.MAIL_NOT_FOUND.equals(e)) {
                LOG.debug("Failed to fetch full draft {}/{} because mail does not exist anymore", fullName, mailId);
                return Optional.empty();
            }

            LOG.warn("Failed to fetch full draft {}/{}", fullName, mailId, e);
            throw e;
        }
    }

    private MailMessage saveDraftMail(ComposedMailMessage newDraftMail, String draftFullName, boolean markAsSeen, IMailMessageStorage messageStorage) throws OXException {
        MailMessage savedDraft;
        try {
            savedDraft = messageStorage.saveDraft(draftFullName, newDraftMail);
            LOG.debug("Saved new draft as {} with {}: {}", savedDraft.getMailPath(), HeaderUtility.HEADER_X_OX_COMPOSITION_SPACE_ID, toStringObjectFor(() -> savedDraft.getFirstHeader(HeaderUtility.HEADER_X_OX_COMPOSITION_SPACE_ID)));
        } catch (OXException e) {
            LOG.debug("Failed to save new draft", e);
            throw e;
        } catch (Exception e) {
            LOG.debug("Failed to save new draft", e);
            throw CompositionSpaceErrorCode.ERROR.create(e, e.getMessage());
        }

        if (markAsSeen) {
            try {
                messageStorage.updateMessageFlags(draftFullName, new String[] { savedDraft.getMailId() }, MailMessage.FLAG_SEEN, true);
                LOG.debug("Marked new draft {} as seen", savedDraft.getMailPath());
            } catch (Exception e) {
                LOG.debug("Failed to mark new draft {} as seen", savedDraft.getMailPath(), e);
            }
        }

        return savedDraft;
    }

    // --------------------------------------------------------- Guard stuff ---------------------------------------------------------------

    private MailAccess<IMailFolderStorage, IMailMessageStorage> createCryptographicAwareAccess(MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess, String authToken) throws OXException {
        CryptographicAwareMailAccessFactory cryptoMailAccessFactory = services.getServiceSafe(CryptographicAwareMailAccessFactory.class);
        String authTokenToUse = getAuthenticationToken(authToken, mailAccess.getSession());
        return cryptoMailAccessFactory.createAccess((MailAccess<IMailFolderStorage, IMailMessageStorage>) mailAccess, mailAccess.getSession(), authTokenToUse);
    }

    private static final String CAPABILITY_GUARD = "guard";

    /**
     * Applies given security settings to specified mail message.
     *
     * @param securitySettings The security settings to apply
     * @param mailMessage The mail message to apply to
     * @param session The session providing user data
     * @return The security-wise prepared mail message in case security settings are enabled; otherwise given mail message is returned as-is
     * @throws OXException If applying security settings fails
     */
    private ComposedMailMessage applyGuardEncryption(SecuritySettings securitySettings, ComposedMailMessage mailMessage, Session session) throws OXException {
        if (securitySettings == null || !securitySettings.isEncrypt()) {
            return mailMessage;
        }

        if (false == mayDecrypt(session)) {
            throw OXException.noPermissionForModule(CAPABILITY_GUARD);
        }

        EncryptedMailService encryptor = services.getServiceSafe(EncryptedMailService.class);
        mailMessage.setSecuritySettings(securitySettings);
        return encryptor.encryptDraftEmail(mailMessage, session, null /* encryption does not require an auth-token */);
    }

    /**
     * Private method to pull the security settings from given arguments.
     *
     * @param security The security options
     * @param session The session
     * @return The security settings if any present and set, otherwise <code>null</code>
     */
    private SecuritySettings getSecuritySettings(Security security) {
        if (null != security && false == security.isDisabled()) {
            SecuritySettings settings = SecuritySettings.builder()
                .encrypt(security.isEncrypt())
                .pgpInline(security.isPgpInline())
                .sign(security.isSign())
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

    /**
     * Gets the full authentication token for a given client token
     *
     * @param authToken The given client token
     * @param session The session
     * @return The full authentication token ready to be used with OX Guard, or null if the given authToken is null
     * @throws OXException
     */
    private String getAuthenticationToken(String authToken, Session session) throws OXException {
        if (authToken != null) {
            CryptographicServiceAuthenticationFactory authFactory = services.getServiceSafe(CryptographicServiceAuthenticationFactory.class);
            return authFactory.createAuthenticationFrom(session, authToken);
        }
        return null;
    }

    /**
     * Checks if session-associated user is allowed to decrypt guard mails.
     *
     * @param session The session
     * @return <code>true</code> if allowed; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    private boolean mayDecrypt(Session session) throws OXException {
        CapabilityService capabilityService = services.getOptionalService(CapabilityService.class);
        return null == capabilityService ? false : capabilityService.getCapabilities(session).contains(CAPABILITY_GUARD);
    }

    private SharedAttachmentsInfo getSharedAttachmentsInfo(MessageDescription draftMessage) {
        SharedAttachmentsInfo sharedAttachmentsInfo = draftMessage.getSharedAttachmentsInfo();
        return sharedAttachmentsInfo == null ? SharedAttachmentsInfo.DISABLED : sharedAttachmentsInfo;
    }

    private Security getSecurity(MessageDescription draftMessage) {
        Security security = draftMessage.getSecurity();
        return security == null ? Security.DISABLED : security;
    }

    /**
     * Checks the client token contained in current request against the actual one currently assigned to the
     * composition space.
     *
     * @param requestToken The token sent by client to perform the current operation
     * @param actualToken The actual token assigned to composition space
     * @throws OXException {@link CompositionSpaceErrorCode#CONCURRENT_UPDATE} if request token is present but does not
     *         match the actual one
     */
    private static void checkClientToken(ClientToken requestToken, ClientToken actualToken) throws OXException {
        if (requestToken.isPresent() && requestToken.isNotEquals(actualToken)) {
            LOG.info("Client token mismatch. Expected: '{}' but was '{}'", actualToken, requestToken);
            throw CompositionSpaceErrorCode.CONCURRENT_UPDATE.create();
        }
    }

    /**
     * Parses the client token from given draft mails headers
     *
     * @param draftMail The draft mail
     * @return The token
     */
    private static ClientToken parseClientToken(MailMessage draftMail) {
        ClientToken clientToken = ClientToken.NONE;
        String clientTokenValue = null;
        try {
            clientTokenValue = HeaderUtility.decodeHeaderValue(draftMail.getFirstHeader(HeaderUtility.HEADER_X_OX_CLIENT_TOKEN));
            clientToken = ClientToken.of(clientTokenValue);
            if (clientToken == ClientToken.NONE) {
                LOG.warn("Draft mail contains invalid client token: {}", clientTokenValue);
            }
        } catch (IllegalArgumentException e) {
            LOG.warn("Draft mail contains invalid client token: {}", clientTokenValue);
        }

        return clientToken;
    }

    private static Optional<UUID> parseCompositionSpaceId(MailMessage mailMessage) {
        String headerValue = null;
        try {
            headerValue = mailMessage.getFirstHeader(HeaderUtility.HEADER_X_OX_COMPOSITION_SPACE_ID);
            return Optional.of(UUIDs.fromUnformattedString(headerValue));
        } catch (IllegalArgumentException e) {
            LOG.info("Ignoring mail {} with invalid composition space ID: {}", mailMessage.getMailPath(), headerValue);
        }

        return Optional.empty();
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static class HeadersAndStream {

        final HeaderCollection headers;
        final InputStream mimeStream;

        HeadersAndStream(HeaderCollection headers, InputStream mimeStream) {
            super();
            this.headers = headers;
            this.mimeStream = mimeStream;
        }
    }

}
