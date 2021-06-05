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

package com.openexchange.mail.compose.impl.storage.db;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.cascade.ConfigViews;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.java.CountingOutputStream;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.compose.Attachment;
import com.openexchange.mail.compose.AttachmentStorage;
import com.openexchange.mail.compose.AttachmentStorageService;
import com.openexchange.mail.compose.ClientToken;
import com.openexchange.mail.compose.CompositionSpace;
import com.openexchange.mail.compose.CompositionSpaceDescription;
import com.openexchange.mail.compose.CompositionSpaceErrorCode;
import com.openexchange.mail.compose.CompositionSpaceId;
import com.openexchange.mail.compose.CompositionSpaceServiceFactory;
import com.openexchange.mail.compose.DefaultAttachment;
import com.openexchange.mail.compose.ImmutableCompositionSpace;
import com.openexchange.mail.compose.ImmutableMessage;
import com.openexchange.mail.compose.Message;
import com.openexchange.mail.compose.MessageDescription;
import com.openexchange.mail.compose.MessageField;
import com.openexchange.mail.compose.impl.storage.AbstractCompositionSpaceStorageService;
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

    private static final String SERVICE_ID = CompositionSpaceServiceFactory.DEFAULT_SERVICE_ID;

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

    private CompositionSpaceDbStorage newDbStorageFor(Session session) throws OXException {
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
     * @throws OXException in case the context can't be resolved
     */
    public CompositionSpaceDbStorage newDbStorageFor(int userId, int contextId) throws OXException {
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
        ImmutableCompositionSpace ics = new ImmutableCompositionSpace(new CompositionSpaceId(SERVICE_ID, id), null, message, cs.getLastModified().getTime(), getClientToken(cs));

        if (!attachmentsToUpdate.isEmpty()) {
            CompositionSpaceContainer ucs = new CompositionSpaceContainer();
            ucs.setUuid(id);
            ucs.setMessage(new MessageDescription().setAttachments(attachmentsToUpdate));
            updateSafe(ucs, dbStorage);
        }

        return ics;
    }

    /**
     * Gets the client token from given composition space container
     *
     * @param cs The container
     * @return The token
     */
    private ClientToken getClientToken(CompositionSpaceContainer cs) {
        ClientToken clientToken = cs.getMessage() == null ? null : cs.getMessage().getClientToken();
        return clientToken == null ? ClientToken.NONE : clientToken;
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
            spaces.add(new ImmutableCompositionSpace(new CompositionSpaceId(SERVICE_ID, cs.getUuid()), null, message, cs.getLastModified().getTime(), getClientToken(cs)));
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
        applyCachedContent(m, csc.getUuid(), session);
        resolveAttachments(m, optionalEncrypt, session);
        Message message = ImmutableMessage.builder().fromMessageDescription(m).build();
        return new ImmutableCompositionSpace(new CompositionSpaceId(SERVICE_ID, csc.getUuid()), null, message, csc.getLastModified().getTime(), getClientToken(csc));
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
        applyCachedContent(m, compositionSpaceDesc.getUuid(), session);
        List<Attachment> attachmentsToUpdate = resolveAttachments(m, Optional.empty(), session);
        Message message = ImmutableMessage.builder().fromMessageDescription(m).build();
        ImmutableCompositionSpace ics = new ImmutableCompositionSpace(new CompositionSpaceId(SERVICE_ID, compositionSpaceDesc.getUuid()), null, message, cs.getLastModified().getTime(), getClientToken(cs));

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
        // Check if non-empty content is supposed to be stored
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

        // Determine the max. content size that is allowed being stored into database
        long effectiveMaxContentSize;
        {
            long configuredMaxContentSize = getConfiguredMaxContentSize(session.getUserId(), session.getContextId());
            if (configuredMaxContentSize < 0) {
                // No max. content size configured. Derive max. content size from "max_package_size" setting of the database
                long maxAllowedPacketSize = dbStorage.getMaxAllowedPacketSize();
                if (maxAllowedPacketSize > 0) {
                    // Keep a buffer for non-binary portion of the package
                    maxAllowedPacketSize = (long) (maxAllowedPacketSize * 0.66);
                }
                effectiveMaxContentSize = maxAllowedPacketSize;
            } else {
                effectiveMaxContentSize = configuredMaxContentSize;
            }
        }

        /*-
         * Examine the effective max. content size. Either max. content size is 0 (zero) in which case every content is supposed to be
         * stored as a file OR it is greater than 0 (zero) and current content's size exceeds that max. content size.
         */
        if ((effectiveMaxContentSize == 0) || exceedsMaxContentSize(content, effectiveMaxContentSize)) {
            boolean stored = fileCache.storeCachedContent(content, id, session.getUserId(), session.getContextId());
            if (stored) {
                // Successfully stored as file. Set content to set to an empty string.
                messageDesc.setContent("");
            }
        }
    }

    /** Size of write buffer. Aligned to java.io.Writer.WRITE_BUFFER_SIZE */
    private static final int WRITE_BUFFER_LENGTH = 1024;

    private static boolean exceedsMaxContentSize(String content, long maxContentSize) throws OXException {
        // Fast check
        int numberOfCharacters = content.length();
        if (numberOfCharacters > maxContentSize) {
            // Number of unicode code points is yet greater than given max. content size. Thus the limit is exceeded in any case.
            return true;
        }

        // UTF-8 has at max. 2 bytes per character. Thus if that max. number of bytes is less than max. content size, the content does fit.
        long estimate = numberOfCharacters << 1;
        if (estimate <= maxContentSize) {
            return false;
        }

        // Might exceed max. content size. Need to check precisely through generating UTF-8 bytes.
        CountingOutputStream counter = null;
        OutputStreamWriter osw = null;
        try {
            counter = new CountingOutputStream();
            osw = new OutputStreamWriter(counter, StandardCharsets.UTF_8);

            for (int i = 0; i < numberOfCharacters;) {
                int end = i + WRITE_BUFFER_LENGTH;
                if (end > numberOfCharacters) {
                    end = numberOfCharacters;
                }
                osw.write(content.substring(i, end));
                i = end;
            }
            osw.flush();
            osw.close();
            osw = null;

            return counter.getCount() > maxContentSize;
        } catch (IOException e) {
            throw CompositionSpaceErrorCode.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(osw, counter);
        }
    }

    private static final String PROP_NAME = "com.openexchange.mail.compose.rdbstorage.content.maxSize";

    /**
     * Gets the configured max. content size.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The configured max. content size (equal to or greater than <code>0</code> (zero)) or <code>-1</code> if not set
     * @throws OXException If configured max. content size cannot be retrieved
     */
    private long getConfiguredMaxContentSize(int userId, int contextId) throws OXException {
        ConfigViewFactory viewFactory = services.getOptionalService(ConfigViewFactory.class);
        if (viewFactory == null) {
            return -1L;
        }

        ConfigView view = viewFactory.getView(userId, contextId);
        long configuredMaxContentSize = ConfigViews.getDefinedLongPropertyFrom(PROP_NAME, -1L, view);
        return configuredMaxContentSize < 0 ? -1L : configuredMaxContentSize;
    }

    private void deleteCachedContentSafe(Session session, UUID id) {
        try {
            fileCache.deleteCachedContent(id, session.getUserId(), session.getContextId());
        } catch (Exception e) {
            // Ignore...
        }
    }

}
