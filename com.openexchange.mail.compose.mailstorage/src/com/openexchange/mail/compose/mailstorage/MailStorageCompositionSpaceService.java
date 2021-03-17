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

package com.openexchange.mail.compose.mailstorage;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static com.openexchange.java.util.UUIDs.getUnformattedString;
import static com.openexchange.mail.MailExceptionCode.getSize;
import static com.openexchange.mail.compose.AttachmentResults.attachmentResultFor;
import static com.openexchange.mail.compose.CompositionSpaces.getUUIDForLogging;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.annotation.Nullable;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCodeSet;
import com.openexchange.groupware.upload.StreamedUploadFile;
import com.openexchange.groupware.upload.StreamedUploadFileIterator;
import com.openexchange.java.Streams;
import com.openexchange.java.util.UUIDs;
import com.openexchange.lock.AccessControl;
import com.openexchange.lock.LockService;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.Quota;
import com.openexchange.mail.compose.Attachment;
import com.openexchange.mail.compose.Attachment.ContentDisposition;
import com.openexchange.mail.compose.AttachmentComparator;
import com.openexchange.mail.compose.AttachmentDescription;
import com.openexchange.mail.compose.AttachmentResult;
import com.openexchange.mail.compose.AttachmentStorages;
import com.openexchange.mail.compose.ByteArrayDataProvider;
import com.openexchange.mail.compose.ClientToken;
import com.openexchange.mail.compose.CompositionSpace;
import com.openexchange.mail.compose.CompositionSpaceErrorCode;
import com.openexchange.mail.compose.CompositionSpaceId;
import com.openexchange.mail.compose.CompositionSpaceInfo;
import com.openexchange.mail.compose.CompositionSpaceService;
import com.openexchange.mail.compose.CompositionSpaces;
import com.openexchange.mail.compose.CryptoUtility;
import com.openexchange.mail.compose.DefaultAttachment;
import com.openexchange.mail.compose.ImmutableCompositionSpace;
import com.openexchange.mail.compose.ImmutableCompositionSpaceInfo;
import com.openexchange.mail.compose.ImmutableMessage;
import com.openexchange.mail.compose.Message.Priority;
import com.openexchange.mail.compose.MessageDescription;
import com.openexchange.mail.compose.MessageField;
import com.openexchange.mail.compose.Meta;
import com.openexchange.mail.compose.Meta.MetaType;
import com.openexchange.mail.compose.OpenCompositionSpaceParameters;
import com.openexchange.mail.compose.SharedFolderReference;
import com.openexchange.mail.compose.Type;
import com.openexchange.mail.compose.UploadLimits;
import com.openexchange.mail.compose.VCardAndFileName;
import com.openexchange.mail.compose.mailstorage.MailStorageExclusiveOperation.MailStorageCallable;
import com.openexchange.mail.compose.mailstorage.association.AssociationLock;
import com.openexchange.mail.compose.mailstorage.association.AssociationLock.LockResult;
import com.openexchange.mail.compose.mailstorage.association.AttachmentMetadata;
import com.openexchange.mail.compose.mailstorage.association.CompositionSpaceToDraftAssociation;
import com.openexchange.mail.compose.mailstorage.association.CompositionSpaceToDraftAssociationUpdate;
import com.openexchange.mail.compose.mailstorage.association.DraftMetadata;
import com.openexchange.mail.compose.mailstorage.association.IAssociationStorage;
import com.openexchange.mail.compose.mailstorage.association.IAssociationStorageManager;
import com.openexchange.mail.compose.mailstorage.open.EditCopy;
import com.openexchange.mail.compose.mailstorage.open.Forward;
import com.openexchange.mail.compose.mailstorage.open.OpenState;
import com.openexchange.mail.compose.mailstorage.open.Reply;
import com.openexchange.mail.compose.mailstorage.open.Resend;
import com.openexchange.mail.compose.mailstorage.storage.ComposeRequestAndMeta;
import com.openexchange.mail.compose.mailstorage.storage.DefaultMailStorageId;
import com.openexchange.mail.compose.mailstorage.storage.IMailStorage;
import com.openexchange.mail.compose.mailstorage.storage.LookUpOutcome;
import com.openexchange.mail.compose.mailstorage.storage.MailStorageId;
import com.openexchange.mail.compose.mailstorage.storage.MailStorageResult;
import com.openexchange.mail.compose.mailstorage.storage.MessageInfo;
import com.openexchange.mail.compose.mailstorage.storage.MissingDraftException;
import com.openexchange.mail.compose.mailstorage.storage.NewAttachmentsInfo;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.json.compose.ComposeHandler;
import com.openexchange.mail.json.compose.ComposeHandlerRegistry;
import com.openexchange.mail.json.compose.ComposeRequest;
import com.openexchange.mail.json.compose.ComposeTransportResult;
import com.openexchange.mail.json.compose.share.AttachmentStorageRegistry;
import com.openexchange.mail.json.compose.share.StorageQuota;
import com.openexchange.mail.json.compose.share.spi.AttachmentStorage;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.transport.MtaStatusInfo;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.utils.ContactCollectorUtility;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.MailAccounts;
import com.openexchange.preferences.ServerUserSetting;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link MailStorageCompositionSpaceService} - The composition space service implementation using mail back-end as storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class MailStorageCompositionSpaceService implements CompositionSpaceService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailStorageCompositionSpaceService.class);

    private final ServiceLookup services;
    private final IMailStorage mailStorage;
    private final IAssociationStorageManager associationStorageManager;
    private final String serviceId;
    private final Session session;
    private final List<OXException> warnings;

    /**
     * Initializes a new {@link MailStorageCompositionSpaceService}.
     *
     * @param session The session
     * @param mailStorage The mail storage
     * @param associationStorageManager The storage manager for active composition spaces having a backing draft message
     * @param services The service look-up
     * @param serviceId The service identifier
     */
    public MailStorageCompositionSpaceService(Session session, IMailStorage mailStorage, IAssociationStorageManager associationStorageManager, ServiceLookup services, String serviceId) {
        super();
        this.session = session;
        this.associationStorageManager = associationStorageManager;
        this.serviceId = serviceId;
        if (null == mailStorage) {
            throw new IllegalArgumentException("Storage must not be null");
        }
        if (null == associationStorageManager) {
            throw new IllegalArgumentException("Association storage manager must not be null");
        }
        if (null == services) {
            throw new IllegalArgumentException("Service registry must not be null");
        }
        this.mailStorage = mailStorage;
        this.services = services;
        warnings = new LinkedList<>();
    }

    /**
     * Gets the session associated with this instance.
     *
     * @return The session
     */
    Session getSession() {
        return session;
    }

    /**
     * Gets the association storage manager access used by this instance.
     *
     * @return The association storage
     */
    IAssociationStorageManager getAssociationStorageManager() {
        return associationStorageManager;
    }

    /**
     * Gets the mail storage access used by this instance.
     *
     * @return The mail storage access
     */
    IMailStorage getMailStorage() {
        return mailStorage;
    }

    /**
     * Adds given warnings.
     *
     * @param warnings The warnings to add
     */
    void addWarnings(Collection<? extends OXException> warnings) {
        if (warnings != null) {
            this.warnings.addAll(warnings);
        }
    }

    @Override
    public String getServiceId() {
        return serviceId;
    }

    @Override
    public Collection<OXException> getWarnings() {
        return warnings;
    }



    /**
     * Gets the association for given composition space identifier.
     *
     * @param compositionSpaceId The composition space identifier
     * @return The look-up result
     * @throws OXException If no such composition space exists (as draft mail)
     */
    LookUpResult requireCompositionSpaceToDraftAssociation(UUID compositionSpaceId) throws OXException {
        if (compositionSpaceId == null) {
            throw CompositionSpaceErrorCode.ERROR.create("Composition space identifier must not be null");
        }
        LookUpResult lookUpResult = optCompositionSpaceToDraftAssociation(compositionSpaceId);
        if (lookUpResult.isEmpty()) {
            throw CompositionSpaceErrorCode.NO_SUCH_COMPOSITION_SPACE.create(getUnformattedString(compositionSpaceId));
        }
        // Check validity
        if (lookUpResult.getAssociation().isInvalid()) {
            throw CompositionSpaceErrorCode.NO_SUCH_COMPOSITION_SPACE.create(getUnformattedString(compositionSpaceId));
        }
        return lookUpResult;
    }

    private LookUpResult optCompositionSpaceToDraftAssociation(UUID compositionSpaceId) throws OXException {
        // Check local storage
        IAssociationStorage associationStorage = associationStorageManager.getStorageFor(session);
        Optional<CompositionSpaceToDraftAssociation> optionalAssociation = associationStorage.opt(compositionSpaceId);
        if (optionalAssociation.isPresent()) {
            LOG.debug("Got association from cache: {}", getUUIDForLogging(compositionSpaceId));
            return LookUpResult.resultFor(optionalAssociation.get(), true, associationStorage);
        }

        // Not contained in local storage. Try to look-up
        LockService lockService = services.getOptionalService(LockService.class);
        if (lockService == null) {
            return lookUpCompositionSpace(compositionSpaceId, associationStorage);
        }

        AccessControl accessControl = lockService.getAccessControlFor(new StringBuilder(getUnformattedString(compositionSpaceId)).append("-msgcs.lookup").toString(), 1, session.getUserId(), session.getContextId());
        try {
            accessControl.acquireGrant();

            optionalAssociation = associationStorage.opt(compositionSpaceId);
            if (optionalAssociation.isPresent()) {
                LOG.debug("Got association from cache: {}", getUUIDForLogging(compositionSpaceId));
                return LookUpResult.resultFor(optionalAssociation.get(), true, associationStorage);
            }

            return lookUpCompositionSpace(compositionSpaceId, associationStorage);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        } finally {
            Streams.close(accessControl);
        }
    }

    private LookUpResult lookUpCompositionSpace(UUID compositionSpaceId, IAssociationStorage associationStorage) throws OXException {
        MailStorageResult<Optional<MailStorageId>> storageResult = mailStorage.lookUp(compositionSpaceId, session);
        warnings.addAll(storageResult.getWarnings());
        Optional<MailStorageId> optMailStorageId = storageResult.getResult();
        if (!optMailStorageId.isPresent()) {
            LOG.debug("Found no composition space for ID: {}", getUUIDForLogging(compositionSpaceId));
            return LookUpResult.emptyResult(associationStorage);
        }

        // Add to local storage
        CompositionSpaceToDraftAssociation newAssociation = CompositionSpaceToDraftAssociation.builder(true).withMailStorageId(optMailStorageId.get()).withSession(session).build();
        CompositionSpaceToDraftAssociation association = associationStorage.storeIfAbsent(newAssociation);
        if (association == null) {
            association = newAssociation;
        }
        LOG.debug("Got association from mail storage: {}", getUUIDForLogging(compositionSpaceId));
        return LookUpResult.resultFor(association, false, associationStorage);
    }

    private CompositionSpaceInfo getCurrentCompositionSpaceInfo(UUID compositionSpaceId, long lastModified) throws OXException {
        IAssociationStorage associationStorage = associationStorageManager.getStorageFor(session);
        CompositionSpaceId id = new CompositionSpaceId(serviceId, compositionSpaceId);
        MailPath draftPath = associationStorage.get(compositionSpaceId).getDraftPath();
        return new ImmutableCompositionSpaceInfo(id, draftPath, lastModified);
    }

    private static final OXExceptionCodeSet CODES_COPY_TO_SENT_FOLDER_FAILED = new OXExceptionCodeSet(MailExceptionCode.COPY_TO_SENT_FOLDER_FAILED_QUOTA, MailExceptionCode.COPY_TO_SENT_FOLDER_FAILED);

    @Override
    public MailPath transportCompositionSpace(UUID compositionSpaceId, Optional<StreamedUploadFileIterator> optionalUploadedAttachments, UserSettingMail mailSettings, AJAXRequestData request, List<OXException> warnings, boolean deleteAfterTransport, final ClientToken clientToken) throws OXException {
        LookUpResult lookUpResult = requireCompositionSpaceToDraftAssociation(compositionSpaceId);
        CompositionSpaceToDraftAssociation association = lookUpResult.getAssociation();

        if (optionalUploadedAttachments.isPresent()) {
            String disposition = com.openexchange.mail.compose.Attachment.ContentDisposition.ATTACHMENT.getId();
            StreamedUploadFileIterator uploadedAttachments = optionalUploadedAttachments.get();
            addAttachmentToCompositionSpace(compositionSpaceId, uploadedAttachments, disposition, clientToken);
            association = lookUpResult.getAssociationStorage().get(compositionSpaceId);
        }

        MailServletInterface mailInterface = null;
        ComposeTransportResult transportResult = null;
        try {
            boolean newMessageId = AJAXRequestDataTools.parseBoolParameter(AJAXServlet.ACTION_NEW, request);
            // Create compose request to process
            MailStorageResult<ComposeRequestAndMeta> storageResult = mailStorage.getForTransport(association, clientToken, request, session);
            warnings.addAll(storageResult.getWarnings());
            ComposeRequestAndMeta result = storageResult.getResult();
            ComposeRequest composeRequest = result.getComposeRequest();

            // Check for shared attachments folder
            SharedFolderReference sharedFolderRef;
            {
                JSONObject jShareAttachmentOptions = (JSONObject) composeRequest.getParameters().get("share_attachments");
                if (jShareAttachmentOptions == null) {
                    sharedFolderRef = null;
                } else {
                    String folderId = jShareAttachmentOptions.optString("folder", null);
                    sharedFolderRef = folderId == null ? null : SharedFolderReference.valueOf(folderId);
                }
            }

            // Determine appropriate compose handler
            ComposeHandlerRegistry handlerRegistry = services.getService(ComposeHandlerRegistry.class);
            ComposeHandler composeHandler = handlerRegistry.getComposeHandlerFor(composeRequest);

            // As new/transport message
            transportResult = composeHandler.createTransportResult(composeRequest);
            warnings.addAll(composeRequest.getWarnings());

            List<? extends ComposedMailMessage> composedMails = transportResult.getTransportMessages();
            ComposedMailMessage sentMessage = transportResult.getSentMessage();
            boolean transportEqualToSent = transportResult.isTransportEqualToSent();

            if (newMessageId) {
                for (ComposedMailMessage composedMail : composedMails) {
                    if (null != composedMail) {
                        composedMail.removeHeader("Message-ID");
                        composedMail.removeMessageId();
                    }
                }
                if (null != sentMessage) {
                    sentMessage.removeHeader("Message-ID");
                    sentMessage.removeMessageId();
                }
            }

            for (ComposedMailMessage cm : composedMails) {
                if (null != cm) {
                    cm.setSendType(ComposeType.NEW);
                }
            }

            // Yield server session
            ServerSession serverSession = ServerSessionAdapter.valueOf(session);

            // User settings
            int accountId = composeRequest.getAccountId();
            UserSettingMail usm = getMailSendSettings(serverSession, accountId, request);
            for (ComposedMailMessage cm : composedMails) {
                if (null != cm) {
                    cm.setMailSettings(usm);
                }
            }
            if (null != sentMessage) {
                sentMessage.setMailSettings(usm);
            }

            mailInterface = MailServletInterface.getInstance(serverSession);

            MailPath sentMailPath = null;
            OXException sendFailed = null;
            try {
                sentMailPath = doTransport(request, accountId, mailInterface, composedMails, sentMessage, transportEqualToSent, usm);
            } catch (OXException oxe) {
                if (!CODES_COPY_TO_SENT_FOLDER_FAILED.contains(oxe)) {
                    // Re-throw...
                    throw oxe;
                }
                sendFailed = oxe;
            }

            // Commit results as actual transport was executed
            try {
                transportResult.commit();
                transportResult.finish();
                transportResult = null;
            } catch (Exception e) {
                LOG.warn("Failed to finalize transport of compositon space {}.", getUnformattedString(compositionSpaceId), e);
            }

            updateReferencedMessages(result.getMeta(), serverSession, mailInterface, warnings);

            warnings.addAll(mailInterface.getWarnings());

            mailInterface.close();
            mailInterface = null;

            // Trigger contact collector
            try {
                boolean memorizeAddresses = ServerUserSetting.getInstance().isContactCollectOnMailTransport(serverSession.getContextId(), serverSession.getUserId()).booleanValue();
                ContactCollectorUtility.triggerContactCollector(serverSession, composedMails, memorizeAddresses, true);
            } catch (Exception e) {
                LOG.warn("Contact collector could not be triggered.", e);
            }

            // Delete associated composition space (and rename shared attachments folder)
            if (deleteAfterTransport) {
                try {
                    MailPath editFor = result.getMeta().getEditFor();
                    if (null != editFor && false == MailProperties.getInstance().isDeleteDraftOnTransport(session.getUserId(), session.getContextId())) {
                        // Draft mail should NOT be deleted
                        LOG.debug("Keeping draft mail '{}' associated with composition space '{}' after transport", association.getDraftPath(), getUnformattedString(compositionSpaceId));
                        MailStorageResult<MailPath> saveAsDraftResult = mailStorage.saveAsFinalDraft(association, ClientToken.NONE, serverSession);
                        warnings.addAll(saveAsDraftResult.getWarnings());
                    } else {
                        MailStorageResult<Boolean> deleteResult = mailStorage.delete(association, true, false, ClientToken.NONE, serverSession);
                        warnings.addAll(deleteResult.getWarnings());
                        boolean closed = deleteResult.getResult().booleanValue();

                        if (closed) {
                            LOG.debug("Closed composition space '{}' after transport", getUnformattedString(compositionSpaceId));
                        } else {
                            LOG.warn("Compositon space {} could not be closed after transport.", getUnformattedString(compositionSpaceId));
                        }
                    }
                } catch (OXException e) {
                    LOG.warn("Failed final mail storage operation for compositon space {}.", getUnformattedString(compositionSpaceId), e);
                    warnings.add(e);
                } catch (Exception e) {
                    LOG.warn("Failed final mail storage operation for compositon space {}.", getUnformattedString(compositionSpaceId), e);
                }

                try {
                    lookUpResult.getAssociationStorage().delete(compositionSpaceId, false);
                } catch (Exception e) {
                    LOG.warn("Failed to delete composition-space-to-draft association of compositon space {}.", getUnformattedString(compositionSpaceId), e);
                }

                if (sharedFolderRef != null) {
                    try {
                        AttachmentStorageRegistry attachmentStorageRegistry = services.getServiceSafe(AttachmentStorageRegistry.class);
                        AttachmentStorage attachmentStorage = attachmentStorageRegistry.getAttachmentStorageFor(serverSession);
                        String folderName = SharedAttachmentsUtils.buildFolderName(composedMails.get(0).getSubject(), false, serverSession);
                        attachmentStorage.renameFolder(folderName, sharedFolderRef.getFolderId(), serverSession);
                    } catch (OXException e) {
                        LOG.warn("Failed to rename shared attachments folder {} from compositon space {}.", sharedFolderRef.getFolderId(), getUnformattedString(compositionSpaceId), e);
                        warnings.add(e);
                    } catch (Exception e) {
                        LOG.warn("Failed to rename shared attachments folder {} from compositon space {}.", sharedFolderRef.getFolderId(), getUnformattedString(compositionSpaceId), e);
                    }
                }
            }

            if (sendFailed != null) {
                throw sendFailed;
            }

            return sentMailPath;
        } catch (MissingDraftException e) {
            LOG.debug("Unable to load draft for transport due to invalid draft path: {}", association);
            lookUpResult.getAssociationStorage().delete(compositionSpaceId, false);
            throw CompositionSpaceErrorCode.CONCURRENT_UPDATE.create(e);
        } finally {
            if (transportResult != null) {
                transportResult.rollback();
                transportResult.finish();
                transportResult = null;
            }
            if (null != mailInterface) {
                mailInterface.close();
            }
        }
    }

    private MailPath doTransport(AJAXRequestData request, int accountId, MailServletInterface mailInterface, List<? extends ComposedMailMessage> composedMails, ComposedMailMessage sentMessage, boolean transportEqualToSent, UserSettingMail usm) throws OXException {
        // Determine remote address (if possible)
        HttpServletRequest servletRequest = request.optHttpServletRequest();
        String remoteAddress = null == servletRequest ? request.getRemoteAddress() : servletRequest.getRemoteAddr();

        // Transport...
        List<String> ids = mailInterface.sendMessages(composedMails, sentMessage, transportEqualToSent, ComposeType.NEW, accountId, usm, new MtaStatusInfo(), remoteAddress);
        if (null == ids || ids.isEmpty()) {
            return null;
        }

        // Extract identifier
        String msgIdentifier = ids.get(0);
        try {
            return MailPath.getMailPathFor(msgIdentifier);
        } catch (Exception x) {
            LOG.warn("Failed to parse mail path from {}", msgIdentifier, x);
        }
        return null;
    }

    /**
     * updateReferencedMessages
     *
     * @param meta
     * @param mailInterface
     * @param warnings
     */
    private void updateReferencedMessages(Meta meta, Session session, MailServletInterface mailInterface, List<OXException> warnings) {
        // Check if original mails needs to be marked or removed
        MetaType metaType = meta.getType();
        if (metaType == MetaType.REPLY || metaType == MetaType.REPLY_ALL) {
            MailPath replyFor = meta.getReplyFor();
            try {
                mailInterface.updateMessageFlags(replyFor.getFolderArgument(), new String[] { replyFor.getMailID() }, MailMessage.FLAG_ANSWERED, null, true);
            } catch (Exception e) {
                LOG.warn("Failed to mark original mail '{}' as answered", replyFor, e);
                warnings.add(MailExceptionCode.FLAG_FAIL.create());
            }
        } else if (metaType == MetaType.FORWARD_INLINE) {
            MailPath forwardFor = meta.getForwardsFor().get(0);
            try {
                mailInterface.updateMessageFlags(forwardFor.getFolderArgument(), new String[] { forwardFor.getMailID() }, MailMessage.FLAG_FORWARDED, null, true);
            } catch (Exception e) {
                LOG.warn("Failed to mark original mail '{}' as forwarded", forwardFor, e);
                warnings.add(MailExceptionCode.FLAG_FAIL.create());
            }
        }
        // Not needed since already dropped when opening composition space
        /*-
         *
        MailPath editFor = meta.getEditFor();
        if (null != editFor && MailProperties.getInstance().isDeleteDraftOnTransport(session.getUserId(), session.getContextId())) {
            try {
                mailInterface.deleteMessages(editFor.getFolderArgument(), new String[] { editFor.getMailID() }, true);
            } catch (Exception e) {
                LOG.warn("Failed to delete edited draft mail '{}'", editFor, e);
            }
        }
         *
         */
    }

    private UserSettingMail getMailSendSettings(ServerSession serverSession, int accountId, AJAXRequestData request) throws OXException {
        UserSettingMail usm = serverSession.getUserSettingMail();
        usm.setNoSave(true);
        String paramName = "copy2Sent";
        String sCopy2Sent = request.getParameter(paramName);
        if (null != sCopy2Sent) { // Provided as URL parameter
            if (AJAXRequestDataTools.parseBoolParameter(sCopy2Sent)) {
                usm.setNoCopyIntoStandardSentFolder(false);
            } else if (Boolean.FALSE.equals(AJAXRequestDataTools.parseFalseBoolParameter(sCopy2Sent))) {
                // Explicitly deny copy to sent folder
                usm.setNoCopyIntoStandardSentFolder(true);
            }
        } else {
            MailAccountStorageService mass = services.getOptionalService(MailAccountStorageService.class);
            if (mass != null && MailAccounts.isGmailTransport(mass.getTransportAccount(accountId, serverSession.getUserId(), serverSession.getContextId()))) {
                // Deny copy to sent folder for Gmail
                usm.setNoCopyIntoStandardSentFolder(true);
            }
        }

        paramName = "lineWrapAfter";
        if (request.containsParameter(paramName)) { // Provided as URL parameter
            String sLineWrapAfter = request.getParameter(paramName);
            if (null != sLineWrapAfter) {
                try {
                    int lineWrapAfter = Integer.parseInt(sLineWrapAfter.trim());
                    usm.setAutoLinebreak(lineWrapAfter <= 0 ? 0 : lineWrapAfter);
                } catch (NumberFormatException nfe) {
                    throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(nfe, paramName, sLineWrapAfter);
                }
            }
        } else {
            // Disable by default
            usm.setAutoLinebreak(0);
        }
        return usm;
    }

    @Override
    public MailPath saveCompositionSpaceToDraftMail(UUID compositionSpaceId, Optional<StreamedUploadFileIterator> optionalUploadedAttachments, boolean deleteAfterSave, ClientToken clientToken) throws OXException {
        LookUpResult lookUpResult = requireCompositionSpaceToDraftAssociation(compositionSpaceId);
        CompositionSpaceToDraftAssociation association = lookUpResult.getAssociation();
        // TODO: optimize: if association is already newer, throw CONCURRENT_UPDATE

        if (optionalUploadedAttachments.isPresent()) {
            String disposition = com.openexchange.mail.compose.Attachment.ContentDisposition.ATTACHMENT.getId();
            StreamedUploadFileIterator uploadedAttachments = optionalUploadedAttachments.get();
            addAttachmentToCompositionSpace(compositionSpaceId, uploadedAttachments, disposition, clientToken);
            association = lookUpResult.getAssociationStorage().get(compositionSpaceId);
        }

        if (deleteAfterSave) {
            MailStorageCallable<MailPath> saveAsFinalDraftCallable = new MailStorageCallable<MailPath>() {

                @Override
                public MailStorageResult<MailPath> call(LookUpResult lookUpResult, IMailStorage mailStorage, Session session, ClientToken clientToken) throws OXException, MissingDraftException {
                    return mailStorage.saveAsFinalDraft(lookUpResult.getAssociation(), clientToken, session);
                }
            };
            MailPath path = MailStorageExclusiveOperation.performOperation(lookUpResult, this, saveAsFinalDraftCallable, clientToken);
            lookUpResult.getAssociationStorage().delete(compositionSpaceId, false);
            return path;
        }

        return association.getDraftPath();
    }

    @Override
    public CompositionSpace openCompositionSpace(OpenCompositionSpaceParameters parameters) throws OXException {
        if (parameters == null) {
            throw CompositionSpaceErrorCode.ERROR.create("Parameters must not be null");
        }

        MailServletInterface mailInterface = null;
        List<Attachment> attachments = null;
        try {
            Type type = parameters.getType();
            if (null == type) {
                type = Type.NEW;
            }

            if (parameters.isAppendOriginalAttachments() && (Type.REPLY != type && Type.REPLY_ALL != type)) {
                throw CompositionSpaceErrorCode.NO_REPLY_FOR.create();
            }

            // Generate composition space identifier
            UUID compositionSpaceId = UUID.randomUUID();

            // Compile message (draft) for the new composition space
            MessageDescription messageDesc = new MessageDescription();

            // Check for priority
            {
                Priority priority = parameters.getPriority();
                if (null != priority) {
                    messageDesc.setPriority(priority);
                }
            }

            // Check for Content-Type
            {
                com.openexchange.mail.compose.Message.ContentType contentType = parameters.getContentType();
                if (null != contentType) {
                    messageDesc.setContentType(contentType);
                }
            }

            // Check if a read receipt should be requested
            if (parameters.isRequestReadReceipt()) {
                messageDesc.setRequestReadReceipt(true);
            }

            // Check if composition space to open is supposed to be encrypted
            Boolean encrypt = B(CryptoUtility.needsEncryption(session, services));

            // Determine the meta information for the message (draft)
            MailPath editFor = null;
            SharedFolderReference sharedFolderRef = null;
            boolean referencesOpenCompositionSpace = false;
            if (Type.NEW == type) {
                LOG.debug("Opening new composition space '{}' with client token '{}'", UUIDs.getUnformattedString(compositionSpaceId), parameters.getClientToken());
                messageDesc.setMeta(Meta.META_NEW);
            } else if (Type.FAX == type) {
                LOG.debug("Opening fax composition space '{}' with client token '{}'", UUIDs.getUnformattedString(compositionSpaceId), parameters.getClientToken());
                messageDesc.setMeta(Meta.META_FAX);
            } else if (Type.SMS == type) {
                LOG.debug("Opening SMS composition space '{}' with client token '{}'", UUIDs.getUnformattedString(compositionSpaceId), parameters.getClientToken());
                messageDesc.setMeta(Meta.META_SMS);
            } else {
                OpenState args = new OpenState(compositionSpaceId, messageDesc, encrypt, Meta.builder());
                try {
                    Meta.Builder metaBuilder = args.metaBuilder;
                    metaBuilder.withType(Meta.MetaType.metaTypeFor(type));

                    if (type == Type.FORWARD) {
                        LOG.debug("Opening forward composition space '{}' with client token '{}'", UUIDs.getUnformattedString(compositionSpaceId), parameters.getClientToken());
                        new Forward(services).doOpenForForward(parameters, args, session);
                    } else if (type == Type.REPLY || type == Type.REPLY_ALL) {
                        LOG.debug("Opening reply composition space '{}' with client token '{}'", UUIDs.getUnformattedString(compositionSpaceId), parameters.getClientToken());
                        new Reply(services).doOpenForReply(type == Type.REPLY_ALL, parameters, args, session);
                    } else if (type == Type.EDIT) {
                        LOG.debug("Opening edit-draft composition space '{}' with client token '{}'", UUIDs.getUnformattedString(compositionSpaceId), parameters.getClientToken());
                        new EditCopy(services).doOpenForEditCopy(true, parameters, args, session);
                        editFor = parameters.getReferencedMails().get(0);
                    } else if (type == Type.COPY) {
                        LOG.debug("Opening copy-draft composition space '{}' with client token '{}'", UUIDs.getUnformattedString(compositionSpaceId), parameters.getClientToken());
                        new EditCopy(services).doOpenForEditCopy(false, parameters, args, session);
                    } else if (type == Type.RESEND) {
                        LOG.debug("Opening resend composition space '{}' with client token '{}'", UUIDs.getUnformattedString(compositionSpaceId), parameters.getClientToken());
                        new Resend(services).doOpenForResend(parameters, args, session);
                    }

                    sharedFolderRef = args.sharedFolderRef;
                    referencesOpenCompositionSpace = args.referencesOpenCompositionSpace;
                    messageDesc.setMeta(metaBuilder.build());
                } catch (MessagingException e) {
                    throw MimeMailException.handleMessagingException(e);
                } finally {
                    attachments = args.attachments;
                    if (null != args.mailInterface) {
                        if (editFor == null || referencesOpenCompositionSpace) {
                            args.mailInterface.close(true);
                        } else {
                            mailInterface = args.mailInterface;
                        }
                    }
                }
            }

            // Check if vCard of session-associated user is supposed to be attached
            if (parameters.isAppendVCard()) {
                // Create VCard
                VCardAndFileName userVCard = CompositionSpaces.getUserVCard(session);

                // Check by file name
                boolean contained = false;
                if (null != attachments) {
                    for (Iterator<Attachment> it = attachments.iterator(); !contained && it.hasNext();) {
                        String fileName = it.next().getName();
                        if (fileName != null && fileName.equals(userVCard.getFileName())) {
                            // vCard already contained
                            contained = true;
                        }
                    }
                }

                // Compile attachment (if not contained)
                if (!contained) {
                    AttachmentDescription attachmentDesc = AttachmentStorages.createVCardAttachmentDescriptionFor(userVCard, compositionSpaceId, true);
                    DefaultAttachment.Builder attachment = DefaultAttachment.builder(attachmentDesc);
                    attachment.withDataProvider(new ByteArrayDataProvider(userVCard.getVcard()));

                    Attachment vcardAttachment = attachment.build();
                    if (null == attachments) {
                        attachments = new ArrayList<>(1);
                    }
                    attachments.add(vcardAttachment);
                }
            }

            if (null != attachments) {
                Collections.sort(attachments, AttachmentComparator.getInstance());
                messageDesc.setAttachments(attachments);
            }

            MailStorageResult<MessageInfo> storageResult = mailStorage.createNew(compositionSpaceId, messageDesc, Optional.ofNullable(sharedFolderRef), parameters.getClientToken(), session);
            warnings.addAll(storageResult.getWarnings());
            MessageInfo messageInfo = storageResult.getResult();

            if (false == referencesOpenCompositionSpace && null != editFor && null != mailInterface) {
                try {
                    mailInterface.deleteMessages(editFor.getFolderArgument(), new String[] { editFor.getMailID() }, true);
                } catch (Exception e) {
                    LOG.warn("Failed to delete edited draft mail '{}'", editFor, e);
                } finally {
                    mailInterface.close(true);
                    mailInterface = null;
                }
            }

            CompositionSpaceToDraftAssociation newAssociation = CompositionSpaceToDraftAssociation.builder().withMailStorageId(storageResult.getMailStorageId()).withDraftMetadata(DraftMetadata.fromMessageInfo(messageInfo)).withSession(session).build();
            associationStorageManager.getStorageFor(session).storeIfAbsent(newAssociation);

            ImmutableMessage message = ImmutableMessage.builder().fromMessageDescription(messageInfo.getMessage()).build();
            CompositionSpace compositionSpace = new ImmutableCompositionSpace(new CompositionSpaceId(serviceId, compositionSpaceId),
                storageResult.getMailStorageId().getDraftPath(),
                message,
                messageInfo.getLastModified(),
                parameters.getClientToken());
            LOG.debug("Opened composition space '{}' with client token '{}'", compositionSpace.getId(), parameters.getClientToken());
            attachments = null; // Avoid premature deletion
            return compositionSpace;
        } finally {
            if (null != mailInterface) {
                mailInterface.close(true);
            }
            if (null != attachments) {
                for (Attachment closeMe : attachments) {
                    closeMe.close();
                }
            }
        }
    }

    @Override
    public AttachmentResult addAttachmentToCompositionSpace(UUID compositionSpaceId, AttachmentDescription attachmentDesc, InputStream data, ClientToken clientToken) throws OXException {
        try {
            if (attachmentDesc == null) {
                throw CompositionSpaceErrorCode.ERROR.create("Attachment description must not be null");
            }
            if (data == null) {
                throw CompositionSpaceErrorCode.ERROR.create("Data must not be null");
            }

            LOG.debug("Adding uploaded attachment with disposition {} to composition space: {}", attachmentDesc.getContentDisposition(), getUUIDForLogging(compositionSpaceId));

            LookUpResult lookUpResult = requireCompositionSpaceToDraftAssociation(compositionSpaceId);
            synchronized (lookUpResult.getAssociation()) {
                checkStorageQuota(lookUpResult.getAssociation(), attachmentDesc.getSize(), attachmentDesc.getContentDisposition());
                checkMaxMailSize(lookUpResult.getAssociation(), attachmentDesc.getSize());

                Attachment newAttachment = null;
                try {
                    attachmentDesc.setCompositionSpaceId(compositionSpaceId);
                    newAttachment = spoolUploadFile(attachmentDesc, data);

                    final Attachment newAttach = newAttachment;
                    MailStorageCallable<NewAttachmentsInfo> addAttachmentsCallable = new MailStorageCallable<NewAttachmentsInfo>() {

                        @Override
                        public MailStorageResult<NewAttachmentsInfo> call(LookUpResult lookUpResult, IMailStorage mailStorage, Session session, ClientToken clientToken) throws OXException, MissingDraftException {
                            return mailStorage.addAttachments(lookUpResult.getAssociation(), Collections.singletonList(newAttach), clientToken, session);
                        }
                    };
                    NewAttachmentsInfo result = MailStorageExclusiveOperation.performOperation(lookUpResult, this, addAttachmentsCallable, clientToken);
                    newAttachment = result.getNewAttachments().get(0);

                    // Everything went fine
                    AttachmentResult retval = attachmentResultFor(newAttachment, getCurrentCompositionSpaceInfo(compositionSpaceId, result.getLastModified()));
                    newAttachment = null;
                    return retval;
                } finally {
                    if (null != newAttachment) {
                        newAttachment.close();
                    }
                }
            }
        } finally {
            Streams.close(data);
        }
    }

    @Override
    public AttachmentResult replaceAttachmentInCompositionSpace(UUID compositionSpaceId, UUID attachmentId, StreamedUploadFileIterator uploadedAttachments, String disposition, ClientToken clientToken) throws OXException {
        if (attachmentId == null) {
            throw CompositionSpaceErrorCode.ERROR.create("Attachment identifier must not be null");
        }
        if (uploadedAttachments == null) {
            throw CompositionSpaceErrorCode.ERROR.create("Upload must not be null");
        }

        // Check attachment validity
        LOG.debug("Replacing uploaded attachments stream with disposition {} in composition space: {}", disposition, getUUIDForLogging(compositionSpaceId));
        LookUpResult lookUpResult = requireCompositionSpaceToDraftAssociation(compositionSpaceId);
        CompositionSpaceToDraftAssociation association = ensureDraftMetadataIsSet(lookUpResult.getAssociation());
        synchronized (association) {
            DraftMetadata draftMetadata = association.getOptionalDraftMetadata().get();
            Optional<AttachmentMetadata> oldAttachmentMeta = draftMetadata.getAttachments().stream().filter(a -> attachmentId.equals(a.getId())).findFirst();
            if (!oldAttachmentMeta.isPresent()) {
                throw CompositionSpaceErrorCode.NO_SUCH_ATTACHMENT_IN_COMPOSITION_SPACE.create(UUIDs.getUnformattedString(attachmentId), UUIDs.getUnformattedString(compositionSpaceId));
            }

            // Calculate change in draft size and check quota
            ContentDisposition contentDisposition = ContentDisposition.dispositionFor(disposition);
            long additionalBytes = uploadedAttachments.getRawTotalBytes() - oldAttachmentMeta.get().getSize();
            if (additionalBytes > 0) {
                checkStorageQuota(association, uploadedAttachments.getRawTotalBytes(), contentDisposition);
                checkMaxMailSize(association, uploadedAttachments.getRawTotalBytes());
            }

            NewAttachmentsInfo result;
            Attachment newAttachment = null;
            try {
                newAttachment = spoolNextUploadFile(compositionSpaceId, attachmentId, uploadedAttachments, contentDisposition);
                if (newAttachment == null) {
                    throw CompositionSpaceErrorCode.ERROR.create("Upload must not be empty");
                }

                {
                    Attachment newAttach = newAttachment;
                    MailStorageCallable<NewAttachmentsInfo> replaceAttachmentCallable = new MailStorageCallable<NewAttachmentsInfo>() {

                        @Override
                        public MailStorageResult<NewAttachmentsInfo> call(LookUpResult lookUpResult, IMailStorage mailStorage, Session session, ClientToken clientToken) throws OXException, MissingDraftException {
                            return mailStorage.replaceAttachment(lookUpResult.getAssociation(), newAttach, clientToken, session);
                        }
                    };
                    result = MailStorageExclusiveOperation.performOperation(lookUpResult, this, replaceAttachmentCallable, clientToken);
                    newAttachment = result.getNewAttachments().get(0);
                }

                // Everything went fine
                AttachmentResult retval = attachmentResultFor(newAttachment, getCurrentCompositionSpaceInfo(compositionSpaceId, result.getLastModified()));
                newAttachment = null;
                return retval;
            } finally {
                if (null != newAttachment) {
                    newAttachment.close();
                }
            }
        }
    }

    @Override
    public AttachmentResult addAttachmentToCompositionSpace(UUID compositionSpaceId, StreamedUploadFileIterator uploadedAttachments, String disposition, ClientToken clientToken) throws OXException {
        if (uploadedAttachments == null) {
            throw CompositionSpaceErrorCode.ERROR.create("Upload must not be null");
        }

        LOG.debug("Adding uploaded attachments stream with disposition {} to composition space: {}", disposition, getUUIDForLogging(compositionSpaceId));

        LookUpResult lookUpResult = requireCompositionSpaceToDraftAssociation(compositionSpaceId);
        synchronized (lookUpResult.getAssociation()) {
            ContentDisposition contentDisposition = ContentDisposition.dispositionFor(disposition);
            checkStorageQuota(lookUpResult.getAssociation(), uploadedAttachments.getRawTotalBytes(), contentDisposition);
            checkMaxMailSize(lookUpResult.getAssociation(), uploadedAttachments.getRawTotalBytes());

            List<Attachment> newAttachments = spoolUploadFiles(compositionSpaceId, uploadedAttachments, contentDisposition);
            if (newAttachments.isEmpty()) {
                throw CompositionSpaceErrorCode.ERROR.create("Upload must not be empty");
            }

            try {
                final List<Attachment> spooledAttachments = newAttachments;
                MailStorageCallable<NewAttachmentsInfo> addAttachmentsCallable = new MailStorageCallable<NewAttachmentsInfo>() {

                    @Override
                    public MailStorageResult<NewAttachmentsInfo> call(LookUpResult lookUpResult, IMailStorage mailStorage, Session session, ClientToken clientToken) throws OXException, MissingDraftException {
                        return mailStorage.addAttachments(lookUpResult.getAssociation(), spooledAttachments, clientToken, session);
                    }
                };
                NewAttachmentsInfo result = MailStorageExclusiveOperation.performOperation(lookUpResult, this, addAttachmentsCallable, clientToken);
                newAttachments = null; // upload successful, resources already closed
                return attachmentResultFor(result.getNewAttachments(), getCurrentCompositionSpaceInfo(compositionSpaceId, result.getLastModified()));
            } finally {
                if (newAttachments != null) {
                    for (Attachment newAttachment : newAttachments) {
                        newAttachment.close();
                    }
                }
            }
        }
    }

    private void checkStorageQuota(CompositionSpaceToDraftAssociation association, long additionalBytes, ContentDisposition disposition) throws OXException {
        if (!MailStorageCompositionSpaceConfig.getInstance().isEagerUploadChecksEnabled()) {
            LOG.debug("Skipping eager quota check because eager upload checks are disabled: {}", association);
            return;
        }

        if (additionalBytes < 0) {
            LOG.debug("Skipping eager quota checks due to unknown additional bytes: {}", association);
            return;
        }

        ContentDisposition effectiveDisposition = disposition;
        if (effectiveDisposition == null) {
            effectiveDisposition = ContentDisposition.ATTACHMENT;
        }

        CompositionSpaceToDraftAssociation effectiveAssociation = ensureDraftMetadataIsSet(association);
        DraftMetadata draftMetadata = effectiveAssociation.getOptionalDraftMetadata().get();
        try {
            switch (effectiveDisposition) {
                case ATTACHMENT:
                    if (draftMetadata.getSharedAttachmentsInfo().isEnabled()) {
                        LOG.debug("Checking availability of additional {} bytes for attachment disposition {} against attachment storage " + "quota due to enabled Drive Mail: {}", L(additionalBytes), effectiveDisposition, effectiveAssociation);
                        checkAttachmentStorageQuota(additionalBytes);
                    } else {
                        LOG.debug("Checking availability of additional {} bytes for attachment disposition {} against mail storage " + "quota: {}", L(additionalBytes), effectiveDisposition, effectiveAssociation);
                        checkMailStorageQuota(additionalBytes);
                    }
                    break;

                case INLINE:
                    LOG.debug("Checking availability of additional {} bytes for attachment disposition {} against mail storage " + "quota: {}", L(additionalBytes), effectiveDisposition, effectiveAssociation);
                    checkMailStorageQuota(additionalBytes);
                    break;

                default:
                    throw new IllegalArgumentException("Unknown content disposition: " + effectiveDisposition.name());
            }
        } catch (OXException e) {
            if (MailExceptionCode.UNABLE_TO_SAVE_DRAFT_QUOTA.equals(e)) {
                LOG.debug("Aborting attachment upload due to unavailable quota for additional {} bytes: {}", L(additionalBytes), effectiveAssociation);
            }

            throw e;
        }
    }

    private void checkMaxMailSize(CompositionSpaceToDraftAssociation association, long additionalBytes) throws OXException {
        if (!MailStorageCompositionSpaceConfig.getInstance().isEagerUploadChecksEnabled()) {
            LOG.debug("Skipping max mail size check because eager upload checks are disabled: {}", association);
            return;
        }

        CompositionSpaceToDraftAssociation effectiveAssociation = ensureDraftMetadataIsSet(association);
        DraftMetadata draftMetadata = effectiveAssociation.getOptionalDraftMetadata().get();
        long maxMailSize = MailProperties.getInstance().getMaxMailSize(session.getUserId(), session.getContextId());
        if (maxMailSize > 0) {
            if (draftMetadata.getSize() + additionalBytes > maxMailSize) {
                LOG.debug("Aborting attachment upload due to max mail size exceedance for additional {} bytes: {}", L(additionalBytes), effectiveAssociation);
                throw MailExceptionCode.MAX_MESSAGE_SIZE_EXCEEDED.create(getSize(maxMailSize, 0, false, true));
            }
        }
    }

    private CompositionSpaceToDraftAssociation ensureDraftMetadataIsSet(CompositionSpaceToDraftAssociation association) throws OXException {
        return ensureDraftMetadataIsSet(association, false);
    }

    private CompositionSpaceToDraftAssociation ensureDraftMetadataIsSet(CompositionSpaceToDraftAssociation association, boolean isRetry) throws OXException {
        if (association.getOptionalDraftMetadata().isPresent()) {
            LOG.debug("Draft metadata is already contained in association: {}", association);
            return association;
        }

        IAssociationStorage associationStorage = associationStorageManager.getStorageFor(session);
        CompositionSpaceToDraftAssociation azzociation = association;
        do {
            AssociationLock lock = azzociation.getLock();
            LockResult lockResult = lock.lock();
            try {
                if (LockResult.IMMEDIATE_ACQUISITION == lockResult) {
                    if (azzociation.getOptionalDraftMetadata().isPresent()) {
                        LOG.debug("Draft metadata is already contained in association: {}", azzociation);
                        return azzociation;
                    }

                    MailStorageResult<MessageInfo> storageResult = mailStorage.getMessage(azzociation, session);
                    LOG.debug("Loaded missing draft metadata for association: {}", azzociation);

                    warnings.addAll(storageResult.getWarnings());
                    MessageInfo messageInfo = storageResult.getResult();
                    DraftMetadata draftMetadata = DraftMetadata.fromMessageInfo(messageInfo);

                    CompositionSpaceToDraftAssociationUpdate update =
                        new CompositionSpaceToDraftAssociationUpdate(azzociation.getCompositionSpaceId())
                        .setDraftMetadata(draftMetadata)
                        .setValidate(false);
                    associationStorage.update(update);
                    return azzociation;
                }

                // Lock could not be immediately acquired
                if (azzociation.getOptionalDraftMetadata().isPresent()) {
                    LOG.debug("Draft metadata is already contained in association: {}", azzociation);
                    return azzociation;
                }
                Optional<CompositionSpaceToDraftAssociation> optionalAssociation = associationStorage.opt(azzociation.getCompositionSpaceId());
                azzociation = optionalAssociation.orElse(null);
            } catch (MissingDraftException e) {
                if (isRetry) {
                    throw CompositionSpaceErrorCode.CONCURRENT_UPDATE.create(e);
                }

                // retry once as cache result might be outdated
                associationStorage.delete(azzociation.getCompositionSpaceId(), false);
                LOG.debug("Loading missing draft metadata for association failed. Retrying: {}");
                CompositionSpaceToDraftAssociation reloadedAssociation = requireCompositionSpaceToDraftAssociation(azzociation.getCompositionSpaceId()).getAssociation();
                return ensureDraftMetadataIsSet(reloadedAssociation, true);
            } finally {
                lock.unlock();
            }
        } while (true);
    }

    private void checkMailStorageQuota(long additionalBytes) throws OXException {
        MailStorageResult<Quota> storageResult = mailStorage.getStorageQuota(session);
        warnings.addAll(storageResult.getWarnings());
        Quota quota = storageResult.getResult();
        if (quota.getLimit() >= 0 && quota.getUsageBytes() + additionalBytes > quota.getLimitBytes()) {
            throw MailExceptionCode.UNABLE_TO_SAVE_DRAFT_QUOTA.create();
        }
    }

    private void checkAttachmentStorageQuota(long additionalBytes) throws OXException {
        AttachmentStorageRegistry attachmentStorageRegistry = services.getServiceSafe(AttachmentStorageRegistry.class);
        AttachmentStorage attachmentStorage = attachmentStorageRegistry.getAttachmentStorageFor(session);
        StorageQuota quota = attachmentStorage.getStorageQuota(ServerSessionAdapter.valueOf(session));
        if (quota.getLimitBytes() >= 0 && quota.getUsageBytes() + additionalBytes > quota.getLimitBytes()) {
            throw MailExceptionCode.UNABLE_TO_SAVE_DRAFT_QUOTA.create();
        }
    }

    @Override
    public AttachmentResult addVCardToCompositionSpace(UUID compositionSpaceId, ClientToken clientToken) throws OXException {
        LookUpResult lookUpResult = requireCompositionSpaceToDraftAssociation(compositionSpaceId);
        synchronized (lookUpResult.getAssociation()) {
            NewAttachmentsInfo result;
            Attachment vcardAttachment = null;
            try {
                {
                    MailStorageCallable<NewAttachmentsInfo> addVCardCallable = new MailStorageCallable<NewAttachmentsInfo>() {

                        @Override
                        public MailStorageResult<NewAttachmentsInfo> call(LookUpResult lookUpResult, IMailStorage mailStorage, Session session, ClientToken clientToken) throws OXException, MissingDraftException {
                            return mailStorage.addVCardAttachment(lookUpResult.getAssociation(), clientToken, session);
                        }
                    };
                    result = MailStorageExclusiveOperation.performOperation(lookUpResult, this, addVCardCallable, clientToken);
                    vcardAttachment = result.getNewAttachments().get(0);
                }

                // Everything went fine
                AttachmentResult retval = attachmentResultFor(vcardAttachment, getCurrentCompositionSpaceInfo(compositionSpaceId, result.getLastModified()));
                vcardAttachment = null;
                return retval;
            } finally {
                if (null != vcardAttachment) {
                    vcardAttachment.close();
                }
            }
        }
    }

    @Override
    public AttachmentResult addContactVCardToCompositionSpace(UUID compositionSpaceId, String contactId, String folderId, ClientToken clientToken) throws OXException {
        if (contactId == null) {
            throw CompositionSpaceErrorCode.ERROR.create("Contact identifier must not be null");
        }
        if (folderId == null) {
            throw CompositionSpaceErrorCode.ERROR.create("Folder identifier must not be null");
        }

        LookUpResult lookUpResult = requireCompositionSpaceToDraftAssociation(compositionSpaceId);
        synchronized (lookUpResult.getAssociation()) {
            NewAttachmentsInfo result;
            Attachment vcardAttachment = null;
            try {
                {
                    MailStorageCallable<NewAttachmentsInfo> addContactVCardCallable = new MailStorageCallable<NewAttachmentsInfo>() {

                        @Override
                        public MailStorageResult<NewAttachmentsInfo> call(LookUpResult lookUpResult, IMailStorage mailStorage, Session session, ClientToken clientToken) throws OXException, MissingDraftException {
                            return mailStorage.addContactVCardAttachment(lookUpResult.getAssociation(), contactId, folderId, clientToken, session);
                        }
                    };
                    result = MailStorageExclusiveOperation.performOperation(lookUpResult, this, addContactVCardCallable, clientToken);
                }

                // Everything went fine
                vcardAttachment = result.getNewAttachments().get(0);
                AttachmentResult retval = attachmentResultFor(vcardAttachment, getCurrentCompositionSpaceInfo(compositionSpaceId, result.getLastModified()));
                vcardAttachment = null;
                return retval;
            } finally {
                if (null != vcardAttachment) {
                    vcardAttachment.close();
                }
            }
        }
    }

    @Override
    public CompositionSpace getCompositionSpace(UUID compositionSpaceId) throws OXException {
        IAssociationStorage associationStorage = associationStorageManager.getStorageFor(session);

        Optional<CompositionSpaceToDraftAssociation> optionalAssociation = associationStorage.opt(compositionSpaceId);
        if (optionalAssociation.isPresent()) {
            CompositionSpaceToDraftAssociation association = optionalAssociation.get();
            LookUpResult lookUpResult = LookUpResult.resultFor(association, true, associationStorage);

            MailStorageCallable<MessageInfo> getCompositionSpaceCallable = new MailStorageCallable<MessageInfo>() {

                @Override
                public MailStorageResult<MessageInfo> call(LookUpResult lookUpResult, IMailStorage mailStorage, Session session, ClientToken clientToken) throws OXException, MissingDraftException {
                    return mailStorage.lookUpMessage(compositionSpaceId, session);
                }
            };

            MessageInfo messageInfo = MailStorageExclusiveOperation.performOperation(lookUpResult, this, getCompositionSpaceCallable, ClientToken.NONE);
            ImmutableMessage message = ImmutableMessage.builder().fromMessageDescription(messageInfo.getMessage()).build();
            return new ImmutableCompositionSpace(new CompositionSpaceId(serviceId, compositionSpaceId), association.getDraftPath(), message, messageInfo.getLastModified(), ClientToken.NONE);
        }

        // No such association in cache, yet
        try {
            MailStorageResult<MessageInfo> storageResult = mailStorage.lookUpMessage(compositionSpaceId, session);
            warnings.addAll(storageResult.getWarnings());

            MailStorageId mailStorageId = storageResult.getMailStorageId();
            MessageInfo messageInfo = storageResult.getResult();

            CompositionSpaceToDraftAssociation association = CompositionSpaceToDraftAssociation.builder(true)
                .withMailStorageId(mailStorageId)
                .withDraftMetadata(DraftMetadata.fromMessageInfo(messageInfo))
                .withSession(session)
                .build();
            CompositionSpaceToDraftAssociation existent = associationStorage.storeIfAbsent(association);
            if (existent != null) {
                // Another thread inserted in the meantime
                return getCompositionSpace(compositionSpaceId);
            }

            ImmutableMessage message = ImmutableMessage.builder().fromMessageDescription(messageInfo.getMessage()).build();
            return new ImmutableCompositionSpace(new CompositionSpaceId(serviceId, compositionSpaceId), mailStorageId.getDraftPath(), message, messageInfo.getLastModified(), ClientToken.NONE);
        } catch (OXException e) {
            if (CompositionSpaceErrorCode.NO_SUCH_COMPOSITION_SPACE.equals(e) ) {
                associationStorage.delete(compositionSpaceId, false);
            }

            throw e;
        }
    }

    @Override
    public List<CompositionSpace> getCompositionSpaces(MessageField[] fields) throws OXException {
        // Look-up what really exists in mail storage
        MailStorageResult<LookUpOutcome> storageResult = mailStorage.lookUp(session);
        warnings.addAll(storageResult.getWarnings());
        Map<MailPath, UUID> existingDraftPaths = storageResult.getResult().getDraftPath2CompositionSpaceId();

        // Get node-local ones
        IAssociationStorage associationStorage = associationStorageManager.getStorageFor(session);
        List<CompositionSpaceToDraftAssociation> associations = associationStorage.getAll();

        if (existingDraftPaths.isEmpty()) {
            // Mail storage signals no available composition-space-related draft messages...
            int numberOfAssociations = associations.size();
            if (numberOfAssociations <= 0) {
                // ... and there are no associations in cache as well.
                LOG.debug("Found no open composition spaces and cache is empty as well. Signaling no available composition spaces.", I(numberOfAssociations));
                return Collections.emptyList();
            }

            // ... but there are associations in cache. Check each cached association if really non-existent.
            LOG.debug("Found no open composition spaces, but cache indicates {} available composition-space-related draft messages. Going to check check each one individually.", I(numberOfAssociations));
            existingDraftPaths = new LinkedHashMap<>(numberOfAssociations);
            for (CompositionSpaceToDraftAssociation association : associations) {
                UUID compositionSpaceId = association.getCompositionSpaceId();
                MailStorageResult<Optional<MailStorageId>> lookUpResult = mailStorage.lookUp(compositionSpaceId, session);
                warnings.addAll(lookUpResult.getWarnings());
                Optional<MailStorageId> optionalMailStorageId = lookUpResult.getResult();

                if (optionalMailStorageId.isPresent()) {
                    // Draft message does still exist as proven through look-up by composition space identifier
                    existingDraftPaths.put(optionalMailStorageId.get().getDraftPath(), compositionSpaceId);
                    LOG.debug("Found composition-space-related draft message for composition space identifier", UUIDs.getUnformattedString(compositionSpaceId));
                } else {
                    // Draft message does not exist
                    associationStorage.delete(compositionSpaceId, false);
                    LOG.debug("Found no composition-space-related draft message for composition space identifier", UUIDs.getUnformattedString(compositionSpaceId));
                }
            }

            if (existingDraftPaths.isEmpty()) {
                // Still empty
                for (CompositionSpaceToDraftAssociation association : associations) {
                    associationStorage.delete(association.getCompositionSpaceId(), false);
                }
                LOG.debug("Verified no open composition spaces exist. Dropping cache entries & signaling no available composition spaces.", I(numberOfAssociations));
                return Collections.emptyList();
            }
        }

        boolean somethingChanged = false;
        for (CompositionSpaceToDraftAssociation association : associations) {
            UUID removed = existingDraftPaths.remove(association.getDraftPath());
            if (removed == null) {
                // No such composition space exists in mail storage
                associationStorage.delete(association.getCompositionSpaceId(), false);
                somethingChanged = true;
            }
        }

        if (!existingDraftPaths.isEmpty()) {
            // There are composition spaces in mail storage that are not contained in node-local associations
            for (Map.Entry<MailPath, UUID> path2Uuid : existingDraftPaths.entrySet()) {
                CompositionSpaceToDraftAssociation association = CompositionSpaceToDraftAssociation.builder(true)
                    .withMailStorageId(new DefaultMailStorageId(path2Uuid.getKey(), path2Uuid.getValue(), Optional.empty()))
                    .withSession(session)
                    .build();
                associationStorage.storeIfAbsent(association);
                somethingChanged = true;
            }
        }

        if (somethingChanged) {
            associations = associationStorage.getAll();
        }

        do {
            try {
                if (associations.isEmpty()) {
                    return Collections.emptyList();
                }
                MailStorageResult<Map<UUID, MessageInfo>> messagesResult = mailStorage.getMessages(associations, fields == null || fields.length <= 0 ? null : EnumSet.of(fields[0], fields), session);
                warnings.addAll(messagesResult.getWarnings());
                Map<UUID, MessageInfo> results = messagesResult.getResult();
                List<CompositionSpace> spaces = new ArrayList<>(results.size());
                Map<UUID, CompositionSpaceToDraftAssociation> associationsById = associations.stream().collect(Collectors.toMap(a -> a.getCompositionSpaceId(), a -> a));
                for (Map.Entry<UUID, MessageInfo> resultEntry : results.entrySet()) {
                    UUID compositionSpaceId = resultEntry.getKey();
                    MessageInfo messageInfo = resultEntry.getValue();
                    MailPath draftPath = null;
                    CompositionSpaceToDraftAssociation association = associationsById.get(compositionSpaceId);
                    if (association != null) {
                        draftPath = association.getDraftPath();
                    }
                    ImmutableMessage message = ImmutableMessage.builder().fromMessageDescription(messageInfo.getMessage()).build();
                    spaces.add(new ImmutableCompositionSpace(new CompositionSpaceId(serviceId, compositionSpaceId), draftPath, message, messageInfo.getLastModified(), ClientToken.NONE));
                }
                return spaces;
            } catch (MissingDraftException e) {
                // Draft mail deleted in the meantime.
                for (MailStorageId absentOne : e.getMailStorageIds()) {
                    associationStorage.delete(absentOne.getCompositionSpaceId(), false);
                }
            }
            associations = associationStorage.getAll();
        } while (true);
    }

    @Override
    public CompositionSpace updateCompositionSpace(UUID compositionSpaceId, MessageDescription md, ClientToken clientToken) throws OXException {
        if (md == null) {
            throw CompositionSpaceErrorCode.ERROR.create("Message description must not be null");
        }

        // Load/fetch association
        LookUpResult lookUpResult = requireCompositionSpaceToDraftAssociation(compositionSpaceId);
        MailStorageCallable<MessageInfo> updateCallable = new MailStorageCallable<MessageInfo>() {

            @Override
            public MailStorageResult<MessageInfo> call(LookUpResult lookUpResult, IMailStorage mailStorage, Session session, ClientToken clientToken) throws OXException, MissingDraftException {
                return mailStorage.update(lookUpResult.getAssociation(), md, clientToken, session);
            }
        };
        MessageInfo messageInfo = MailStorageExclusiveOperation.performOperation(lookUpResult, this, updateCallable, clientToken);
        lookUpResult = requireCompositionSpaceToDraftAssociation(compositionSpaceId);

        ImmutableMessage message = ImmutableMessage.builder().fromMessageDescription(messageInfo.getMessage()).build();
        CompositionSpace compositionSpace = new ImmutableCompositionSpace(new CompositionSpaceId(serviceId, compositionSpaceId), lookUpResult.getAssociation().getDraftPath(), message, messageInfo.getLastModified(), ClientToken.NONE);
        return compositionSpace;
    }

    @Override
    public boolean closeCompositionSpace(UUID compositionSpaceId, boolean hardDelete, ClientToken clientToken) throws OXException {
        LookUpResult lookUpResult = optCompositionSpaceToDraftAssociation(compositionSpaceId);
        if (lookUpResult.isEmpty()) {
            return false;
        }

        IAssociationStorage associationStorage = lookUpResult.getAssociationStorage();
        CompositionSpaceToDraftAssociation association = lookUpResult.getAssociation();
        do {
            AssociationLock lock = association.getLock();
            LockResult lockResult = lock.lock();
            try {
                if (LockResult.IMMEDIATE_ACQUISITION == lockResult) {
                    Optional<CompositionSpaceToDraftAssociation> optionalAssociation = associationStorage.delete(compositionSpaceId, false);
                    MailStorageId toDelete = optionalAssociation.orElse(lookUpResult.getAssociation());
                    MailStorageResult<Boolean> storageResult = mailStorage.delete(toDelete, hardDelete, true, clientToken, session);
                    warnings.addAll(storageResult.getWarnings());
                    return storageResult.getResult().booleanValue();
                }

                // Already closed by another thread
                Optional<CompositionSpaceToDraftAssociation> optionalAssociation = associationStorage.opt(compositionSpaceId);
                association = optionalAssociation.orElse(null);
            } finally {
                lock.unlock();
            }
        } while (association != null);
        return false;
    }

    @Override
    public void closeExpiredCompositionSpaces(long maxIdleTimeMillis) throws OXException {
        // Do not delete any drafts without user interaction
    }

    @Override
    public AttachmentResult addOriginalAttachmentsToCompositionSpace(UUID compositionSpaceId, ClientToken clientToken) throws OXException {
        LookUpResult lookUpResult = requireCompositionSpaceToDraftAssociation(compositionSpaceId);
        synchronized (lookUpResult.getAssociation()) {
            MailStorageCallable<NewAttachmentsInfo> addOriginalAttachmentsCallable = new MailStorageCallable<NewAttachmentsInfo>() {

                @Override
                public MailStorageResult<NewAttachmentsInfo> call(LookUpResult lookUpResult, IMailStorage mailStorage, Session session, ClientToken clientToken) throws OXException, MissingDraftException {
                    return mailStorage.addOriginalAttachments(lookUpResult.getAssociation(), clientToken, session);
                }
            };
            NewAttachmentsInfo result = MailStorageExclusiveOperation.performOperation(lookUpResult, this, addOriginalAttachmentsCallable, clientToken);
            List<Attachment> newAttachments = result.getNewAttachments();
            return attachmentResultFor(newAttachments, getCurrentCompositionSpaceInfo(compositionSpaceId, result.getLastModified()));
        }
    }

    @Override
    public AttachmentResult getAttachment(UUID compositionSpaceId, UUID attachmentId) throws OXException {
        if (attachmentId == null) {
            throw CompositionSpaceErrorCode.ERROR.create("Attachment identifier must not be null");
        }

        // Load/fetch association
        LookUpResult lookUpResult = requireCompositionSpaceToDraftAssociation(compositionSpaceId);
        CompositionSpaceToDraftAssociation association = lookUpResult.getAssociation();
        synchronized (association) {
            if (association.needsValidation()) {
                MailStorageCallable<Optional<MailPath>> validateCallable = new MailStorageCallable<Optional<MailPath>>() {

                    @Override
                    public MailStorageResult<Optional<MailPath>> call(LookUpResult lookUpResult, IMailStorage mailStorage, Session session, ClientToken clientToken) throws OXException, MissingDraftException {
                        CompositionSpaceToDraftAssociation azzociation = lookUpResult.getAssociation();
                        if (azzociation.needsValidation()) {
                            return mailStorage.validate(azzociation, session);
                        }

                        // Another thread already performed validation
                        return MailStorageResult.resultFor(azzociation, Optional.of(azzociation.getDraftPath()), true);
                    }
                };
                Optional<MailPath> optional = MailStorageExclusiveOperation.performOperation(lookUpResult, this, validateCallable, ClientToken.NONE);
                if (optional.isPresent()) {
                    lookUpResult = requireCompositionSpaceToDraftAssociation(compositionSpaceId);
                    association = lookUpResult.getAssociation();
                }
            }

            MailStorageCallable<Attachment> getAttachmentCallable = new MailStorageCallable<Attachment>() {

                @Override
                public MailStorageResult<Attachment> call(LookUpResult lookUpResult, IMailStorage mailStorage, Session session, ClientToken clientToken) throws OXException, MissingDraftException {
                    return mailStorage.getAttachment(lookUpResult.getAssociation(), attachmentId, session);
                }
            };
            Attachment attachment = MailStorageExclusiveOperation.performOperation(lookUpResult, this, getAttachmentCallable, ClientToken.NONE);
            return attachmentResultFor(attachment, getCurrentCompositionSpaceInfo(association.getCompositionSpaceId(), -1L));
        }
    }

    @Override
    public AttachmentResult deleteAttachment(UUID compositionSpaceId, UUID attachmentId, ClientToken clientToken) throws OXException {
        if (attachmentId == null) {
            throw CompositionSpaceErrorCode.ERROR.create("Attachment identifier must not be null");
        }

        // Load/fetch association
        LookUpResult lookUpResult = requireCompositionSpaceToDraftAssociation(compositionSpaceId);
        synchronized (lookUpResult.getAssociation()) {
            MailStorageCallable<MessageInfo> deleteAttachmentsCallable = new MailStorageCallable<MessageInfo>() {

                @Override
                public MailStorageResult<MessageInfo> call(LookUpResult lookUpResult, IMailStorage mailStorage, Session session, ClientToken clientToken) throws OXException, MissingDraftException {
                    return mailStorage.deleteAttachments(lookUpResult.getAssociation(), Collections.singletonList(attachmentId), clientToken, session);
                }
            };
            MessageInfo messageInfo = MailStorageExclusiveOperation.performOperation(lookUpResult, this, deleteAttachmentsCallable, clientToken);
            return attachmentResultFor(getCurrentCompositionSpaceInfo(compositionSpaceId, messageInfo.getLastModified()));
        }
    }

    @Override
    public UploadLimits getAttachmentUploadLimits(UUID compositionSpaceId) throws OXException {
        LookUpResult lookUpResult = requireCompositionSpaceToDraftAssociation(compositionSpaceId);
        CompositionSpaceToDraftAssociation association = ensureDraftMetadataIsSet(lookUpResult.getAssociation());
        DraftMetadata draftMetadata = association.getOptionalDraftMetadata().get();
        boolean driveMailEnabled = draftMetadata.getSharedAttachmentsInfo().isEnabled();
        UploadLimits.Type type = driveMailEnabled ? UploadLimits.Type.DRIVE : UploadLimits.Type.MAIL;
        return UploadLimits.get(type, session);
    }

    private List<Attachment> spoolUploadFiles(UUID compositionSpaceId, StreamedUploadFileIterator uploadedAttachments, ContentDisposition contentDisposition) throws OXException {
        List<Attachment> newAttachments = new LinkedList<>();
        try {
            while (uploadedAttachments.hasNext()) {
                Attachment newAttachment = spoolNextUploadFile(compositionSpaceId, uploadedAttachments, contentDisposition);
                if (newAttachment != null) {
                    newAttachments.add(newAttachment);
                }
            }
        } catch (OXException e) {
            for (Attachment attachment : newAttachments) {
                attachment.close();
            }

            throw e;
        }

        return newAttachments;
    }

    private Attachment spoolNextUploadFile(UUID compositionSpaceId, StreamedUploadFileIterator uploadedAttachments, ContentDisposition contentDisposition) throws OXException {
        return spoolNextUploadFile(compositionSpaceId, null, uploadedAttachments, contentDisposition);
    }

    private Attachment spoolNextUploadFile(UUID compositionSpaceId, @Nullable UUID attachmentId, StreamedUploadFileIterator uploadedAttachments, ContentDisposition contentDisposition) throws OXException {
        if (uploadedAttachments.hasNext()) {
            StreamedUploadFile uploadFile = uploadedAttachments.next();
            AttachmentDescription attachmentDesc = AttachmentStorages.createUploadFileAttachmentDescriptionFor(uploadFile, contentDisposition, compositionSpaceId);
            if (attachmentId != null) {
                attachmentDesc.setId(attachmentId);
            }

            InputStream inputStream;
            try {
                inputStream = uploadFile.getStream();
            } catch (IOException e) {
                LOG.info("Unable to get input stream for upload file '{}' ({}) of composition space: {}", uploadFile.getPreparedFileName(), getSizeForLogging(uploadFile.getSize()), getUUIDForLogging(compositionSpaceId), e);
                throw CompositionSpaceErrorCode.IO_ERROR.create(e, e.getMessage());
            }

            return spoolUploadFile(attachmentDesc, inputStream);
        }

        return null;
    }

    private Attachment spoolUploadFile(AttachmentDescription attachmentDesc, InputStream data) throws OXException {
        Attachment newAttachment;
        ThresholdFileHolder sink = ThresholdFileHolderFactory.getInstance().createFileHolder(session);
        try {
            sink.write(data);

            // Compile attachment
            DefaultAttachment.Builder attachment = DefaultAttachment.builder(attachmentDesc);
            attachment.withDataProvider(new ThresholdFileHolderDataProvider(sink));
            attachment.withSize(sink.getLength());

            newAttachment = attachment.build();
            sink = null; // Avoid premature closing
        } catch (OXException e) {
            LOG.info("Error while trying to spool upload file '{}' ({}) of composition space: {}", attachmentDesc.getName(), getSizeForLogging(attachmentDesc.getSize()), getUUIDForLogging(attachmentDesc.getCompositionSpaceId()), e);
            Streams.close(data);
            throw e;
        } finally {
            Streams.close(sink);
        }
        return newAttachment;
    }

    private static String getSizeForLogging(long numBytes) {
        if (numBytes < 0) {
            return "unknown".intern();
        }

        return getSize(numBytes, 0, false, true);
    }

}
