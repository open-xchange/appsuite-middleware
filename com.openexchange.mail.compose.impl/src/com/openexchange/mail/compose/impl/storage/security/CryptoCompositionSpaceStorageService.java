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

package com.openexchange.mail.compose.impl.storage.security;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.mail.compose.CryptoUtility.decrypt;
import static com.openexchange.mail.compose.CryptoUtility.encrypt;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import com.openexchange.crypto.CryptoErrorMessage;
import com.openexchange.crypto.CryptoService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.mail.compose.CompositionSpace;
import com.openexchange.mail.compose.CompositionSpaceDescription;
import com.openexchange.mail.compose.CompositionSpaceErrorCode;
import com.openexchange.mail.compose.CompositionSpaceId;
import com.openexchange.mail.compose.CompositionSpaceStorageService;
import com.openexchange.mail.compose.ImmutableCompositionSpace;
import com.openexchange.mail.compose.ImmutableMessage;
import com.openexchange.mail.compose.Message;
import com.openexchange.mail.compose.MessageDescription;
import com.openexchange.mail.compose.MessageField;
import com.openexchange.mail.compose.impl.AbstractCryptoAware;
import com.openexchange.mail.compose.security.CompositionSpaceKeyStorage;
import com.openexchange.mail.compose.security.CompositionSpaceKeyStorageService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link CryptoCompositionSpaceStorageService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class CryptoCompositionSpaceStorageService extends AbstractCryptoAware implements CompositionSpaceStorageService {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(CryptoCompositionSpaceStorageService.class);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final CompositionSpaceStorageService delegate;

    /**
     * Initializes a new {@link CryptoCompositionSpaceStorageService}.
     */
    public CryptoCompositionSpaceStorageService(CompositionSpaceStorageService delegate, CompositionSpaceKeyStorageService keyStorageService, ServiceLookup services) {
        super(keyStorageService, services);
        this.delegate = delegate;
    }

    private Optional<Key> optionalKey(CompositionSpace compositionSpace, Session session) throws OXException {
        return getKeyFor(compositionSpace.getId().getId(), false, session);
    }

    private static boolean isBase64String(String toCheck) {
        if (Strings.isEmpty(toCheck)) {
            return false;
        }

        // Check if base64 decodable
        try {
            java.util.Base64.getDecoder().decode(toCheck);
            return true;
        } catch (IllegalArgumentException e) {
            // Apparently no base64 string
            return false;
        }
    }

    private CompositionSpace decryptCompositionSpaceIfNeeded(CompositionSpace compositionSpace, Session session) throws OXException {
        // Flag that indicates if composition space advertises to hold encrypted content
        boolean hasEncryptedContent = hasEncryptedContent(compositionSpace);

        // Flag that indicates if encryption is enabled/configured for session-associated user
        boolean needsEncryption = needsEncryption(session);

        if (hasEncryptedContent) {
            // Decryption is needed
            if (!needsEncryption) {
                StringBuilder logMsg = new StringBuilder("{}{}The content of composition space {} is not encrypted, but should{}");
                List<Object> args = new ArrayList<Object>(6);
                args.add(Strings.getLineSeparator());
                args.add(Strings.getLineSeparator());
                args.add(UUIDs.getUnformattedString(compositionSpace.getId().getId()));
                args.add(Strings.getLineSeparator());

                logMsg.append("needs encryption: {}{}");
                args.add(B(needsEncryption));
                args.add(Strings.getLineSeparator());

                logMsg.append("has encrypted content: {}{}");
                args.add(B(hasEncryptedContent));
                args.add(Strings.getLineSeparator());

                LoggerHolder.LOG.error(logMsg.toString(), args.toArray(new Object[args.size()]));
            }
            return decryptCompositionSpace(compositionSpace, session, true).compositionSpace;
        }

        if (needsEncryption && isBase64String(compositionSpace.getMessage().getContent())) {
            // Content is base64 compliant... Thus it appears to be encrypted content, but check says no.
            // Anyway, try to decrypt it
            DecryptResult decryptResult = decryptCompositionSpace(compositionSpace, session, false);
            if (decryptResult.decrypted) {
                // Content was decrypted...
                StringBuilder logMsg = new StringBuilder("{}{}The content of composition space {} is encrypted, but content-encrypted flag says it isn't{}");
                List<Object> args = new ArrayList<Object>(6);
                args.add(Strings.getLineSeparator());
                args.add(Strings.getLineSeparator());
                args.add(UUIDs.getUnformattedString(compositionSpace.getId().getId()));
                args.add(Strings.getLineSeparator());

                logMsg.append("needs encryption: {}{}");
                args.add(B(needsEncryption));
                args.add(Strings.getLineSeparator());

                logMsg.append("has encrypted content: {}{}");
                args.add(B(hasEncryptedContent));
                args.add(Strings.getLineSeparator());

                LoggerHolder.LOG.error(logMsg.toString(), args.toArray(new Object[args.size()]));
            }
            return decryptResult.compositionSpace;
        }

        // Either no encryption enabled/configured or appears to be no base64 content
        return compositionSpace;
    }

    private DecryptResult decryptCompositionSpace(CompositionSpace compositionSpace, Session session, boolean errorIfNotDecryptable) throws OXException {
        // Fetch key
        Optional<Key> optionalKey = optionalKey(compositionSpace, session);
        if (optionalKey.isPresent() == false) {
            // No key found...
            if (errorIfNotDecryptable) {
                // ... but required
                throw CompositionSpaceErrorCode.MISSING_KEY.create(compositionSpace.getId());
            }
            // ... and not required. Return given composition space as-is
            return new DecryptResult(compositionSpace, false);
        }

        // Decrypt using key
        Key key = optionalKey.get();
        CompositionSpaceId compositionSpaceId = compositionSpace.getId();
        try {
            String plainContent = decrypt(compositionSpace.getMessage().getContent(), key, services.getService(CryptoService.class));
            Message msg = ImmutableMessage.builder()
                .fromMessage(compositionSpace.getMessage())
                .withContent(plainContent)
                .build();
            ImmutableCompositionSpace decryptedSpace = new ImmutableCompositionSpace(compositionSpaceId, null, msg, compositionSpace.getLastModified(), compositionSpace.getClientToken());
            return new DecryptResult(decryptedSpace, true);
        } catch (OXException e) {
            if (errorIfNotDecryptable) {
                if (CryptoErrorMessage.BadPassword.equals(e)) {
                    throw CompositionSpaceErrorCode.MISSING_KEY.create(e, compositionSpaceId);
                }
                throw e;
            }
            return new DecryptResult(compositionSpace, false);
        }
    }

    private void encryptCompositionSpaceDescription(CompositionSpaceDescription compositionSpaceDesc, boolean createKeyIfAbsent, Session session) throws OXException {
        UUID compositionSpaceId = compositionSpaceDesc.getUuid();
        Optional<Key> optionalKey = getKeyFor(compositionSpaceId, createKeyIfAbsent, session);
        if (!createKeyIfAbsent && !optionalKey.isPresent()) {
            throw CompositionSpaceErrorCode.MISSING_KEY.create(UUIDs.getUnformattedString(compositionSpaceId));
        }

        MessageDescription messageDesc = compositionSpaceDesc.getMessage();

        if (messageDesc != null) {
            if (messageDesc.containsContent()) {
                String content = messageDesc.getContent();
                if (null != content) {
                    messageDesc.setContent(encrypt(content, optionalKey.get(), services.getService(CryptoService.class)));
                }
            }

            // Mark to have encrypted content
            messageDesc.setContentEncrypted(true);
        }
    }

    @Override
    public boolean isContentEncrypted(Session session, UUID id) throws OXException {
        return delegate.isContentEncrypted(session, id);
    }

    @Override
    public boolean existsCompositionSpace(Session session, UUID id) throws OXException {
        return delegate.existsCompositionSpace(session, id);
    }

    @Override
    public CompositionSpace getCompositionSpace(Session session, UUID id) throws OXException {
        CompositionSpace compositionSpace = delegate.getCompositionSpace(session, id);
        if (null != compositionSpace) {
            compositionSpace = decryptCompositionSpaceIfNeeded(compositionSpace, session);
        }
        return compositionSpace;
    }

    @Override
    public List<CompositionSpace> getCompositionSpaces(Session session, MessageField[] fields) throws OXException {
        MessageField[] fieldsToUse = fields;
        if (null != fieldsToUse) {
            MessageField.addMessageFieldIfAbsent(fieldsToUse, MessageField.CONTENT_ENCRYPTED);
        }

        List<CompositionSpace> compositionSpaces = delegate.getCompositionSpaces(session, fieldsToUse);
        int size;
        if (null != compositionSpaces && (size = compositionSpaces.size()) > 0) {
            // Check individually if space holds encrypted content
            List<CompositionSpace> spaces = new ArrayList<>(size);
            for (CompositionSpace compositionSpace : compositionSpaces) {
                spaces.add(decryptCompositionSpaceIfNeeded(compositionSpace, session));
            }
            compositionSpaces = spaces;
        }

        return compositionSpaces;
    }

    @Override
    public CompositionSpace openCompositionSpace(Session session, CompositionSpaceDescription compositionSpaceDesc, Optional<Boolean> optionalEncrypt) throws OXException {
        boolean encrypt = optionalEncrypt.isPresent() ? optionalEncrypt.get().booleanValue() : needsEncryption(session);

        if (!encrypt) {
            // Mark to have NO encrypted content
            MessageDescription message = compositionSpaceDesc.getMessage();
            if (message != null) {
                message.setContentEncrypted(false);
            }
            CompositionSpace compositionSpace = delegate.openCompositionSpace(session, compositionSpaceDesc, Optional.empty());
            LoggerHolder.LOG.debug("Opened composition space {}: encrypted=false", UUIDs.getUnformattedStringObjectFor(compositionSpace.getId().getId()));
            return compositionSpace;
        }

        encryptCompositionSpaceDescription(compositionSpaceDesc, true, session);
        CompositionSpace openedCompositionSpace = delegate.openCompositionSpace(session, compositionSpaceDesc, Optional.of(B(encrypt)));
        CompositionSpace compositionSpace = decryptCompositionSpace(openedCompositionSpace, session, true).compositionSpace;
        LoggerHolder.LOG.debug("Opened composition space {}: encrypted=true", UUIDs.getUnformattedStringObjectFor(compositionSpace.getId().getId()));
        return compositionSpace;
    }

    @Override
    public CompositionSpace updateCompositionSpace(Session session, CompositionSpaceDescription compositionSpaceDesc, Optional<CompositionSpace> optionalOriginalSpace) throws OXException {
        CompositionSpace originalSpace = optionalOriginalSpace.isPresent() ? optionalOriginalSpace.get() : getCompositionSpace(session, compositionSpaceDesc.getUuid());
        if (!hasEncryptedContent(originalSpace)) {
            // Mark to have NO encrypted content
            MessageDescription message = compositionSpaceDesc.getMessage();
            if (message != null) {
                message.setContentEncrypted(false);
            }
            return delegate.updateCompositionSpace(session, compositionSpaceDesc, Optional.of(originalSpace));
        }

        encryptCompositionSpaceDescription(compositionSpaceDesc, false, session);
        CompositionSpace updatedCompositionSpace = delegate.updateCompositionSpace(session, compositionSpaceDesc, Optional.of(originalSpace));
        return decryptCompositionSpace(updatedCompositionSpace, session, true).compositionSpace;
    }

    @Override
    public boolean closeCompositionSpace(Session session, UUID id) throws OXException {
        boolean deleted = delegate.closeCompositionSpace(session, id);
        if (deleted) {
            try {
                deleteKeyFor(id, session);
            } catch (Exception e) {
                LoggerHolder.LOG.warn("Failed to delete key associated with composition space {}", UUIDs.getUnformattedString(id), e);
            }
        }
        return deleted;
    }

    @Override
    public List<UUID> deleteExpiredCompositionSpaces(Session session, long maxIdleTimeMillis) throws OXException {
        List<UUID> deletedCompositionSpaceIds = delegate.deleteExpiredCompositionSpaces(session, maxIdleTimeMillis);
        if (null != deletedCompositionSpaceIds && !deletedCompositionSpaceIds.isEmpty()) {
            CompositionSpaceKeyStorage keyStorage = keyStorageService.getKeyStorageFor(session);
            for (UUID compositionSpaceId : deletedCompositionSpaceIds) {
                try {
                    keyStorage.deleteKeyFor(compositionSpaceId, session);
                } catch (Exception e) {
                    LoggerHolder.LOG.warn("Failed to delete key associated with composition space {}", UUIDs.getUnformattedString(compositionSpaceId), e);
                }
            }
        }
        return deletedCompositionSpaceIds;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static class DecryptResult {

        final CompositionSpace compositionSpace;
        final boolean decrypted;

        DecryptResult(CompositionSpace compositionSpace, boolean decrypted) {
            super();
            this.compositionSpace = compositionSpace;
            this.decrypted = decrypted;
        }
    }
}
