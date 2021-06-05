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

package com.openexchange.mail.compose.impl.storage.inmemory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.java.BufferingQueue;
import com.openexchange.java.util.UUIDs;
import com.openexchange.mail.compose.CompositionSpace;
import com.openexchange.mail.compose.CompositionSpaceDescription;
import com.openexchange.mail.compose.CompositionSpaceErrorCode;
import com.openexchange.mail.compose.CompositionSpaceId;
import com.openexchange.mail.compose.CompositionSpaceServiceFactory;
import com.openexchange.mail.compose.ImmutableCompositionSpace;
import com.openexchange.mail.compose.ImmutableMessage;
import com.openexchange.mail.compose.MessageDescription;
import com.openexchange.mail.compose.MessageField;
import com.openexchange.mail.compose.impl.NonCryptoCompositionSpaceStorageService;
import com.openexchange.mail.compose.impl.storage.db.RdbCompositionSpaceStorageService;
import com.openexchange.session.Session;


/**
 * {@link InMemoryCompositionSpaceStorageService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class InMemoryCompositionSpaceStorageService implements NonCryptoCompositionSpaceStorageService, Runnable {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(InMemoryCompositionSpaceStorageService.class);
    }

    private static final String SERVICE_ID = CompositionSpaceServiceFactory.DEFAULT_SERVICE_ID;

    /** The special poison object */
    private static final InMemoryMessage POISON = new InMemoryMessage(new UUID(0, 0), null, null, 0, 0);

    private final RdbCompositionSpaceStorageService persistentStorage;
    private final ConcurrentMap<UUID, InMemoryCompositionSpace> spacesById;
    private final BufferingQueue<InMemoryMessage> bufferingQueue;
    private final Thread pollThread;
    private final AtomicBoolean isRunning;

    /**
     * Initializes a new {@link InMemoryCompositionSpaceStorageService}.
     */
    public InMemoryCompositionSpaceStorageService(long delayDuration, long maxDelayDuration, RdbCompositionSpaceStorageService persistentStorage) {
        super();
        this.persistentStorage = persistentStorage;
        isRunning = new AtomicBoolean(true);
        bufferingQueue = new BufferingQueue<InMemoryMessage>(delayDuration, maxDelayDuration);
        spacesById = new ConcurrentHashMap<UUID, InMemoryCompositionSpace>(2048, 0.9F, 1);
        pollThread = new Thread(this, this.getClass().getSimpleName());
    }

    private boolean isInvalid(InMemoryCompositionSpace compositionSpace, Session session) {
        return null == compositionSpace || compositionSpace.getContextId() != session.getContextId() || compositionSpace.getUserId() != session.getUserId();
    }

    /**
     * Starts the polling thread.
     *
     * @return This instance
     */
    public InMemoryCompositionSpaceStorageService start() {
        pollThread.start();
        return this;
    }

    /**
     * Closes this <code>InMemoryCompositionSpaceStorageService</code>.
     */
    public void close() {
        isRunning.set(false);
        // Feed poison element to enforce quit
        bufferingQueue.offerImmediately(POISON);
    }

    @Override
    public void run() {
        BufferingQueue<InMemoryMessage> bufferingQueue = this.bufferingQueue;
        List<InMemoryMessage> objects = new ArrayList<InMemoryMessage>(16);
        while (isRunning.get()) {
            try {
                objects.clear();

                // Blocking wait for at least 1 element to expire.
                InMemoryMessage object = bufferingQueue.take();
                if (POISON == object) {
                    // Flush rest
                    for (InMemoryMessage message : bufferingQueue) {
                        message.flushToStorage(persistentStorage);
                    }
                    return;
                }

                // Add taken element & drain more if available
                objects.add(object);
                bufferingQueue.drainTo(objects);

                boolean poisoned = false;
                for (InMemoryMessage message : objects) {
                    if (POISON == message) {
                        poisoned = true;
                    } else {
                        message.flushToStorage(persistentStorage);
                    }
                }

                if (poisoned) {
                    // Leave if poisoned
                    return;
                }
            } catch (Exception exc) {
                LoggerHolder.LOG.error("", exc);
            }
        }
    }

    @Override
    public boolean isContentEncrypted(Session session, UUID id) throws OXException {
        InMemoryCompositionSpace compositionSpace = spacesById.get(id);
        if (isInvalid(compositionSpace, session)) {
            throw CompositionSpaceErrorCode.NO_SUCH_COMPOSITION_SPACE.create(UUIDs.getUnformattedString(id));
        }

        return compositionSpace.getMessage().isContentEncrypted();
    }

    @Override
    public boolean existsCompositionSpace(Session session, UUID id) throws OXException {
        InMemoryCompositionSpace compositionSpace = spacesById.get(id);
        return !isInvalid(compositionSpace, session);
    }

    @Override
    public CompositionSpace getCompositionSpace(Session session, UUID id) throws OXException {
        InMemoryCompositionSpace compositionSpace = spacesById.get(id);
        if (isInvalid(compositionSpace, session)) {
            return null;
        }

        ImmutableMessage message = ImmutableMessage.builder().fromMessage(compositionSpace.getMessage()).build();
        return new ImmutableCompositionSpace(new CompositionSpaceId(SERVICE_ID, id), null, message, compositionSpace.getLastModified(), compositionSpace.getClientToken());
    }

    @Override
    public List<CompositionSpace> getCompositionSpaces(Session session, MessageField[] fields) throws OXException {
        int contextId = session.getContextId();
        int userId = session.getUserId();
        List<CompositionSpace> spaces = new LinkedList<CompositionSpace>();
        for (InMemoryCompositionSpace compositionSpace : spacesById.values()) {
            if (compositionSpace.getContextId() == contextId && compositionSpace.getUserId() == userId) {
                spaces.add(compositionSpace);
            }
        }
        return spaces;
    }

    @Override
    public CompositionSpace openCompositionSpace(Session session, CompositionSpaceDescription compositionSpaceDesc, Optional<Boolean> optionalEncrypt) throws OXException {
        CompositionSpace persistentCompositionSpace = persistentStorage.openCompositionSpace(session, compositionSpaceDesc, optionalEncrypt);

        MessageDescription messageDesc = compositionSpaceDesc.getMessage();
        InMemoryCompositionSpace compositionSpace = new InMemoryCompositionSpace(persistentCompositionSpace.getId().getId(), messageDesc, bufferingQueue, session.getUserId(), session.getContextId(), persistentCompositionSpace.getClientToken());
        spacesById.put(persistentCompositionSpace.getId().getId(), compositionSpace);

        return compositionSpace;
    }

    @Override
    public CompositionSpace updateCompositionSpace(Session session, CompositionSpaceDescription compositionSpaceDesc, Optional<CompositionSpace> optionalOriginalSpace) throws OXException {
        if (compositionSpaceDesc == null) {
            return null;
        }

        UUID id = compositionSpaceDesc.getUuid();
        if (null == id) {
            return null;
        }

        InMemoryCompositionSpace compositionSpace = spacesById.get(id);
        if (isInvalid(compositionSpace, session)) {
            throw CompositionSpaceErrorCode.NO_SUCH_COMPOSITION_SPACE.create(UUIDs.getUnformattedString(id));
        }

        if (false == compositionSpace.updateLastModifiedStamp(compositionSpaceDesc.getLastModifiedDate())) {
            throw CompositionSpaceErrorCode.CONCURRENT_UPDATE.create();
        }

        MessageDescription messageDesc = compositionSpaceDesc.getMessage();
        compositionSpace.getMessage().applyFromMessageDescription(messageDesc);

        ImmutableMessage message = ImmutableMessage.builder().fromMessage(compositionSpace.getMessage()).build();
        return new ImmutableCompositionSpace(new CompositionSpaceId(SERVICE_ID, id), null, message, compositionSpace.getLastModified(), compositionSpace.getClientToken());
    }

    @Override
    public boolean closeCompositionSpace(Session session, UUID id) throws OXException {
        if (null == id) {
            return false;
        }

        InMemoryCompositionSpace compositionSpace = spacesById.remove(id);
        if (isInvalid(compositionSpace, session)) {
            return false;
        }

        // Remove from queue & remove from persistent storage
        InMemoryMessage message = compositionSpace.getMessage();
        bufferingQueue.remove(message);
        persistentStorage.closeCompositionSpace(session, id);

        return true;
    }

    @Override
    public List<UUID> deleteExpiredCompositionSpaces(Session session, long maxIdleTimeMillis) throws OXException {
        ReentrantLock queueLock = bufferingQueue.getLock();
        queueLock.lock();
        try {
            List<UUID> deleted = persistentStorage.deleteExpiredCompositionSpaces(session, maxIdleTimeMillis);
            if (null != deleted && !deleted.isEmpty()) {
                for (UUID id : deleted) {
                    InMemoryCompositionSpace compositionSpace = spacesById.remove(id);

                    // Remove from queue & remove from persistent storage
                    InMemoryMessage message = compositionSpace.getMessage();
                    bufferingQueue.remove(message);
                }
            }
            return deleted;
        } finally {
            queueLock.unlock();
        }
    }

}
