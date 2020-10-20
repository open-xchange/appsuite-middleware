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

package com.openexchange.mail.compose.impl.storage.db;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.compose.Attachment;
import com.openexchange.mail.compose.AttachmentStorage;
import com.openexchange.mail.compose.AttachmentStorageService;
import com.openexchange.mail.compose.CompositionSpace;
import com.openexchange.mail.compose.CompositionSpaceDescription;
import com.openexchange.mail.compose.CompositionSpaceErrorCode;
import com.openexchange.mail.compose.DefaultAttachment;
import com.openexchange.mail.compose.Message;
import com.openexchange.mail.compose.MessageDescription;
import com.openexchange.mail.compose.MessageField;
import com.openexchange.mail.compose.impl.storage.AbstractCompositionSpaceStorageService;
import com.openexchange.mail.compose.impl.storage.ImmutableCompositionSpace;
import com.openexchange.mail.compose.impl.storage.ImmutableMessage;
import com.openexchange.mail.compose.impl.storage.db.filecache.FileCache;
import com.openexchange.mail.compose.impl.storage.db.filecache.FileCacheImpl;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link RdbCompositionSpaceStorageService}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.2
 */
public class RdbCompositionSpaceStorageService extends AbstractCompositionSpaceStorageService {

    // private static final DBTransactionPolicy txPolicy = DBTransactionPolicy.NORMAL_TRANSACTIONS;

    private final DBProvider dbProvider;
    private final AttachmentStorageService attachmentStorageService;
    private final FileCache fileCache;

    /**
     * Initializes a new {@link RdbCompositionSpaceStorageService}.
     *
     * @param dbProvider The provider for connections to database
     * @param attachmentStorageService The attachment storage service to use
     * @param services The service look-up
     * @throws OXException If initialization fails
     */
    public RdbCompositionSpaceStorageService(DBProvider dbProvider, AttachmentStorageService attachmentStorageService, ServiceLookup services) throws OXException {
        super(services);
        this.dbProvider = dbProvider;
        this.attachmentStorageService = attachmentStorageService;
        fileCache = new FileCacheImpl(this, services);
    }

    private CompositionSpaceDbStorage newDbStorageFor(Session session) {
        return new CompositionSpaceDbStorage(dbProvider, /*txPolicy, */session, services);
    }

    private void updateSafe(CompositionSpaceContainer ucs, CompositionSpaceDbStorage dbStorage) {
        try {
            dbStorage.updateCompositionSpace(ucs, false);
        } catch (@SuppressWarnings("unused") Exception e) {
            // Ignore...
        }
    }

    /**
     * Signals that application is going to be stopped.
     *
     * @throws OXException If operation fails fatally
     */
    public void signalStop() throws OXException {
        fileCache.signalStop();
    }

    /**
     * Creates the appropriate database storage for given user/context pair
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The database storage
     */
    public CompositionSpaceDbStorage newDbStorageFor(int userId, int contextId) {
        return new CompositionSpaceDbStorage(dbProvider, /*txPolicy, */userId, contextId, services);
    }

    @Override
    public boolean isContentEncrypted(Session session, UUID id) throws OXException {
        CompositionSpaceDbStorage dbStorage = newDbStorageFor(session);
        return dbStorage.isContentEncrypted(id);
    }

    @Override
    public boolean existsCompositionSpace(Session session, UUID id) throws OXException {
        CompositionSpaceDbStorage dbStorage = newDbStorageFor(session);
        return dbStorage.exists(id);
    }

    @Override
    public CompositionSpace getCompositionSpace(Session session, UUID id) throws OXException {
        CompositionSpaceDbStorage dbStorage = newDbStorageFor(session);

        CompositionSpaceContainer cs = dbStorage.select(id);
        if (null == cs) {
            return null;
        }

        MessageDescription m = cs.getMessage();
        applyCachedContent(m, id, session);
        List<Attachment> attachmentsToUpdate = resolveAttachments(m, Optional.empty(), session);
        Message message = ImmutableMessage.builder().fromMessageDescription(m).build();
        ImmutableCompositionSpace ics = new ImmutableCompositionSpace(id, message, cs.getLastModified().getTime());

        if (!attachmentsToUpdate.isEmpty()) {
            CompositionSpaceContainer ucs = new CompositionSpaceContainer();
            ucs.setUuid(id);
            ucs.setMessage(new MessageDescription().setAttachments(attachmentsToUpdate));
            updateSafe(ucs, dbStorage);
        }

        return ics;
    }

    @Override
    public List<CompositionSpace> getCompositionSpaces(Session session, MessageField[] fields) throws OXException {
        CompositionSpaceDbStorage dbStorage = newDbStorageFor(session);

        MessageField[] fieldsToQuery = null == fields ? MessageField.values() : MessageField.addMessageFieldIfAbsent(fields, MessageField.CONTENT_ENCRYPTED);
        List<CompositionSpaceContainer> containers = dbStorage.selectAll(fieldsToQuery);
        int size;
        if (null == containers || (size = containers.size()) <= 0) {
            return Collections.emptyList();
        }

        List<CompositionSpace> spaces = new ArrayList<>(size);
        boolean attachmentsQueried = MessageField.isContained(fieldsToQuery, MessageField.ATTACHMENTS);
        boolean contentQueried = MessageField.isContained(fieldsToQuery, MessageField.CONTENT);
        List<CompositionSpaceContainer> toUpdate = null;
        for (CompositionSpaceContainer cs : containers) {
            MessageDescription m = cs.getMessage();
            if (contentQueried) {
                applyCachedContent(m, cs.getUuid(), session);
            }
            if (attachmentsQueried) {
                List<Attachment> attachmentsToUpdate = resolveAttachments(m, Optional.empty(), session);
                if (!attachmentsToUpdate.isEmpty()) {
                    if (toUpdate == null) {
                        toUpdate = new ArrayList<>(size);
                    }
                    CompositionSpaceContainer ucs = new CompositionSpaceContainer();
                    ucs.setUuid(cs.getUuid());
                    ucs.setMessage(new MessageDescription().setAttachments(attachmentsToUpdate));
                    toUpdate.add(ucs);
                }
            }
            Message message = ImmutableMessage.builder().fromMessageDescription(m).build();
            spaces.add(new ImmutableCompositionSpace(cs.getUuid(), message, cs.getLastModified().getTime()));
        }

        if (toUpdate != null) {
            for (CompositionSpaceContainer ucs : toUpdate) {
                updateSafe(ucs, dbStorage);
            }
        }

        return spaces;
    }

    @Override
    public CompositionSpace openCompositionSpace(Session session, CompositionSpaceDescription compositionSpaceDesc, Optional<Boolean> optionalEncrypt) throws OXException {
        CompositionSpaceDbStorage dbStorage = newDbStorageFor(session);

        // Check if user exceeds max. number of composition spaces
        int maxSpacesPerUser = getMaxSpacesPerUser(session);
        if (maxSpacesPerUser == 0 || (maxSpacesPerUser > 0 && dbStorage.countAll() >= maxSpacesPerUser)) {
            throw CompositionSpaceErrorCode.MAX_NUMBER_OF_COMPOSITION_SPACE_REACHED.create(Integer.valueOf(maxSpacesPerUser));
        }

        CompositionSpaceContainer csc = new CompositionSpaceContainer();
        csc.setLastModified(new Date(System.currentTimeMillis()));
        if (compositionSpaceDesc != null) {
            UUID compositionSpaceId = null == compositionSpaceDesc.getUuid() ? UUID.randomUUID() : compositionSpaceDesc.getUuid();
            prepareContentFrom(compositionSpaceDesc, dbStorage, compositionSpaceId, session);
            csc.setUuid(compositionSpaceId);
            csc.setMessage(compositionSpaceDesc.getMessage());
        } else {
            csc.setUuid(UUID.randomUUID());
        }

        dbStorage.insert(csc, maxSpacesPerUser);

        MessageDescription m = csc.getMessage();
        resolveAttachments(m, optionalEncrypt, session);
        Message message = ImmutableMessage.builder().fromMessageDescription(m).build();
        return new ImmutableCompositionSpace(csc.getUuid(), message, csc.getLastModified().getTime());
    }

    @Override
    public CompositionSpace updateCompositionSpace(Session session, CompositionSpaceDescription compositionSpaceDesc, Optional<CompositionSpace> optionalOriginalSpace) throws OXException {
        if (compositionSpaceDesc == null) {
            return null;
        }

        CompositionSpaceDbStorage dbStorage = newDbStorageFor(session);
        prepareContentFrom(compositionSpaceDesc, dbStorage, compositionSpaceDesc.getUuid(), session);
        CompositionSpaceContainer cs = dbStorage.updateCompositionSpace(CompositionSpaceContainer.fromCompositionSpaceDescription(compositionSpaceDesc), true);

        MessageDescription m = cs.getMessage();
        List<Attachment> attachmentsToUpdate = resolveAttachments(m, Optional.empty(), session);
        Message message = ImmutableMessage.builder().fromMessageDescription(m).build();
        ImmutableCompositionSpace ics = new ImmutableCompositionSpace(compositionSpaceDesc.getUuid(), message, cs.getLastModified().getTime());

        if (!attachmentsToUpdate.isEmpty()) {
            CompositionSpaceContainer ucs = new CompositionSpaceContainer();
            ucs.setUuid(compositionSpaceDesc.getUuid());
            ucs.setMessage(new MessageDescription().setAttachments(attachmentsToUpdate));
            updateSafe(ucs, dbStorage);
        }

        return ics;
    }

    @Override
    public boolean closeCompositionSpace(Session session, UUID id) throws OXException {
        try {
            CompositionSpaceDbStorage dbStorage = newDbStorageFor(session);
            return dbStorage.delete(id);
        } finally {
            deleteCachedContentSafe(session, id);
        }
    }

    @Override
    public List<UUID> deleteExpiredCompositionSpaces(Session session, long maxIdleTimeMillis) throws OXException {
        CompositionSpaceDbStorage dbStorage = newDbStorageFor(session);
        List<UUID> deletedOnes = dbStorage.deleteExpired(maxIdleTimeMillis);
        deletedOnes.forEach(compositionSpaceId -> deleteCachedContentSafe(session, compositionSpaceId));
        return deletedOnes;
    }

    /**
     * Adds the specified attachment to the {@link CompositionSpace} associated with given identifier.
     *
     * @param session The session providing user information
     * @param id The {@link CompositionSpace} identifier
     * @param attachment The attachment to add
     * @throws OXException If adding the attachment fails (e.g. if no such {@link CompositionSpace} exists)
     */
    public void addAttachment(Session session, UUID id, Attachment attachment) throws OXException {
        CompositionSpaceDbStorage dbStorage = newDbStorageFor(session);
        dbStorage.addAttachment(id, attachment);
    }

    /**
     * Removes the specified attachment from the {@link CompositionSpace} associated with given identifier.
     *
     * @param session The session providing user information
     * @param id The {@link CompositionSpace} identifier
     * @param attachment The attachment to remove
     * @throws OXException If adding the attachment fails (e.g. if no such {@link CompositionSpace} exists)
     */
    public void removeAttachment(Session session, UUID id, Attachment attachment) throws OXException {
        CompositionSpaceDbStorage dbStorage = newDbStorageFor(session);
        dbStorage.removeAttachment(id, attachment);
    }

    /**
     * (Re-)loads all attachments in a message by the the real data from appropriate attachment storage.
     * <p>
     * Necessary if only the attachment identifier is set.
     *
     * @param messageDescription The message description
     * @param optionalEncrypt The optional encryption flag on initial opening of a composition space. If present and <code>true</code> the
     *                        attachment to save is supposed to be encrypted according to caller. If present and <code>false</code>  the
     *                        attachment to save is <b>not</b> supposed to be encrypted according to caller. If absent, encryption is
     *                        automatically determined.<br>
     *                        <b>Note</b>: The flag MUST be aligned to associated composition space
     * @param session The session
     * @return The attachments to update
     * @throws OXException If attachments cannot be resolved
     */
    private List<Attachment> resolveAttachments(MessageDescription messageDescription, Optional<Boolean> optionalEncrypt, Session session) throws OXException {
        if (null == messageDescription) {
            return Collections.emptyList();
        }

        List<Attachment> availableAttachments = messageDescription.getAttachments();
        if (availableAttachments == null) {
            return Collections.emptyList();
        }

        int size = availableAttachments.size();
        if (size <= 0) {
            return Collections.emptyList();
        }

        AttachmentStorage attachmentStorage = attachmentStorageService.getAttachmentStorageFor(session);
        List<Attachment> attachmentsToSet = new ArrayList<>(size);
        boolean modified = false;
        for (Attachment attachment : attachmentStorage.getAttachments(getIdsFrom(availableAttachments), optionalEncrypt, session)) {
            if (null == attachment) {
                modified = true;
            } else {
                attachmentsToSet.add(attachment);
            }
        }
        messageDescription.setAttachments(attachmentsToSet);

        if (!modified) {
            return Collections.emptyList();
        }

        return attachmentsToSet.stream().map(attachment -> DefaultAttachment.createWithId(attachment.getId(), null)).collect(Collectors.toList());
    }

    private List<UUID> getIdsFrom(List<Attachment> availableAttachments) {
        return availableAttachments.stream().map(a -> a.getId()).collect(Collectors.toList());
    }

    // ----------------------------------------------- File cache stuff --------------------------------------------------------------------

    private void applyCachedContent(MessageDescription m, UUID id, Session session) throws OXException {
        Optional<String> cachedContent = fileCache.getCachedContent(id, session.getUserId(), session.getContextId());
        if (cachedContent.isPresent()) {
            m.setContent(cachedContent.get());
        }
    }

    private void prepareContentFrom(CompositionSpaceDescription compositionSpaceDesc, CompositionSpaceDbStorage dbStorage, UUID id, Session session) throws OXException {
        MessageDescription messageDesc = compositionSpaceDesc.getMessage();
        if (messageDesc == null) {
            return;
        }

        if (!messageDesc.containsContent() && messageDesc.getContent() == null) {
            return;
        }

        String content = messageDesc.getContent();
        if (Strings.isEmpty(content)) {
            return;
        }

        long maxAllowedPacketSize = dbStorage.getMaxAllowedPacketSize();
        if (maxAllowedPacketSize > 0) {
            // Keep a buffer for non-binary portion of the package
            maxAllowedPacketSize = (long) (maxAllowedPacketSize * 0.66);
        }

        if (maxAllowedPacketSize > 0 && (content.length() > maxAllowedPacketSize || content.getBytes(StandardCharsets.UTF_8).length > maxAllowedPacketSize)) {
            boolean stored = fileCache.storeCachedContent(content, id, session.getUserId(), session.getContextId());
            if (stored) {
                messageDesc.setContent("");
            }
        }
    }

    private void deleteCachedContentSafe(Session session, UUID id) {
        try {
            fileCache.deleteCachedContent(id, session.getUserId(), session.getContextId());
        } catch (Exception e) {
            // Ignore...
        }
    }

}
