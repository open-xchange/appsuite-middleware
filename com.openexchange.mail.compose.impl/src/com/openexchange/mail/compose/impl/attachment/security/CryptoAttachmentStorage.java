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

package com.openexchange.mail.compose.impl.attachment.security;

import static com.openexchange.mail.compose.CryptoUtility.encrypt;
import java.io.InputStream;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.crypto.CryptoService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.mail.compose.Attachment;
import com.openexchange.mail.compose.AttachmentDescription;
import com.openexchange.mail.compose.AttachmentStorage;
import com.openexchange.mail.compose.AttachmentStorageType;
import com.openexchange.mail.compose.CompositionSpaceErrorCode;
import com.openexchange.mail.compose.CompositionSpaceStorageService;
import com.openexchange.mail.compose.CryptoUtility;
import com.openexchange.mail.compose.DataProvider;
import com.openexchange.mail.compose.SizeProvider;
import com.openexchange.mail.compose.SizeReturner;
import com.openexchange.mail.compose.impl.AbstractCryptoAware;
import com.openexchange.mail.compose.security.CompositionSpaceKeyStorageService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.stream.CountingOnlyInputStream;

/**
 * {@link CryptoAttachmentStorage}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class CryptoAttachmentStorage extends AbstractCryptoAware implements AttachmentStorage {

    private final AttachmentStorage attachmentStorage;
    private final CompositionSpaceStorageService compositionSpaceStorage;

    /**
     * Initializes a new {@link CryptoAttachmentStorage}.
     */
    public CryptoAttachmentStorage(AttachmentStorage attachmentStorage, CompositionSpaceStorageService compositionSpaceStorage, CompositionSpaceKeyStorageService keyStorageService, ServiceLookup services) {
        super(keyStorageService, services);
        this.attachmentStorage = attachmentStorage;
        this.compositionSpaceStorage = compositionSpaceStorage;
    }

    private boolean hasEncryptedContent(UUID compositionSpaceId, Session session) throws OXException {
        return null == compositionSpaceStorage ? false : compositionSpaceStorage.isContentEncrypted(session, compositionSpaceId);
    }

    private boolean hasEncryptedContentElseNeedsEncryption(UUID compositionSpaceId, Session session) throws OXException {
        try {
            return hasEncryptedContent(compositionSpaceId, session);
        } catch (OXException e) {
            if (!CompositionSpaceErrorCode.NO_SUCH_COMPOSITION_SPACE.equals(e)) {
                throw e;
            }
            return needsEncryption(session);
        }
    }

    @Override
    public AttachmentStorageType getStorageType() {
        return attachmentStorage.getStorageType();
    }

    @Override
    public boolean isApplicableFor(CapabilitySet capabilities, Session session) throws OXException {
        return attachmentStorage.isApplicableFor(capabilities, session);
    }

    @Override
    public Attachment getAttachment(UUID id, Optional<Boolean> optionalEncrypt, Session session) throws OXException {
        Attachment attachment = attachmentStorage.getAttachment(id, Optional.empty(), session);
        if (null != attachment) {
            boolean encrypted = optionalEncrypt.isPresent() ? optionalEncrypt.get().booleanValue() : hasEncryptedContent(attachment.getCompositionSpaceId(), session);
            if (encrypted) {
                Optional<Key> optionalKey = getKeyFor(attachment.getCompositionSpaceId(), false, session);
                if (!optionalKey.isPresent()) {
                    throw CompositionSpaceErrorCode.MISSING_KEY.create(UUIDs.getUnformattedString(attachment.getCompositionSpaceId()));
                }
                attachment = new DecryptingAttachment(attachment, optionalKey.get(), services.getService(CryptoService.class));
            }
        }
        return attachment;
    }

    @Override
    public Attachment[] getAttachments(List<UUID> ids, Optional<Boolean> optionalEncrypt, Session session) throws OXException {
        if (ids == null || ids.isEmpty()) {
            return new Attachment[0];
        }

        Attachment[] retval = new Attachment[ids.size()];

        FlagAndKey flagAndKey = null;
        int index = 0;
        for (UUID id : ids) {
            Attachment attachment = attachmentStorage.getAttachment(id, Optional.empty(), session);
            if (attachment == null) {
                // No such attachment
                retval[index++] = null;
            } else {
                flagAndKey = getFlagAndKey(flagAndKey, attachment, optionalEncrypt, session);
                if (flagAndKey.encrypted) {
                    attachment = new DecryptingAttachment(attachment, flagAndKey.key, services.getService(CryptoService.class));
                }
                retval[index++] = attachment;
            }
        }

        return retval;
    }

    private FlagAndKey getFlagAndKey(FlagAndKey existent, Attachment attachment, Optional<Boolean> optionalEncrypt, Session session) throws OXException {
        if (existent != null) {
            return existent;
        }

        boolean exists = compositionSpaceStorage.existsCompositionSpace(session, attachment.getCompositionSpaceId());

        boolean encrypted;
        if (optionalEncrypt.isPresent()) {
            encrypted = optionalEncrypt.get().booleanValue();
        } else {
            if (exists) {
                encrypted = hasEncryptedContent(attachment.getCompositionSpaceId(), session);
            } else {
                // Composition space does not yet exist. Thus encryption is determined by configuration/capabilities.
                encrypted = needsEncryption(session);
            }
        }
        if (encrypted == false) {
            return new FlagAndKey(false, null);
        }

        Optional<Key> optionalKey = getKeyFor(attachment.getCompositionSpaceId(), !exists, session);
        if (!optionalKey.isPresent()) {
            throw CompositionSpaceErrorCode.MISSING_KEY.create(UUIDs.getUnformattedString(attachment.getCompositionSpaceId()));
        }
        return new FlagAndKey(true, optionalKey.get());
    }

    @Override
    public List<Attachment> getAttachmentsByCompositionSpace(UUID compositionSpaceId, Session session) throws OXException {
        List<Attachment> attachments = attachmentStorage.getAttachmentsByCompositionSpace(compositionSpaceId, session);
        if (null == attachments || attachments.isEmpty()) {
            return attachments;
        }

        if (!hasEncryptedContent(compositionSpaceId, session)) {
            return attachments;
        }

        Optional<Key> optionalKey = getKeyFor(compositionSpaceId, false, session);
        if (!optionalKey.isPresent()) {
            throw CompositionSpaceErrorCode.MISSING_KEY.create(UUIDs.getUnformattedString(compositionSpaceId));
        }

        Key key = optionalKey.get();
        CryptoService cryptoService = services.getService(CryptoService.class);
        List<Attachment> decryptedAttachments = new ArrayList<Attachment>(attachments.size());
        for (Attachment attachment : attachments) {
            decryptedAttachments.add(new DecryptingAttachment(attachment, key, cryptoService));
        }
        return decryptedAttachments;
    }

    @Override
    public SizeReturner getSizeOfAttachmentsByCompositionSpace(UUID compositionSpaceId, Session session) throws OXException {
        SizeReturner sizeReturner = attachmentStorage.getSizeOfAttachmentsByCompositionSpace(compositionSpaceId, session);
        if (!sizeReturner.hasDataProviders()) {
            return sizeReturner;
        }

        if (!hasEncryptedContent(compositionSpaceId, session)) {
            return sizeReturner;
        }

        Optional<Key> optionalKey = getKeyFor(compositionSpaceId, false, session);
        if (!optionalKey.isPresent()) {
            throw CompositionSpaceErrorCode.MISSING_KEY.create(UUIDs.getUnformattedString(compositionSpaceId));
        }

        Key key = optionalKey.get();
        CryptoService cryptoService = services.getService(CryptoService.class);
        SizeReturner.Builder decryptingSizeReturner = SizeReturner.builder();
        decryptingSizeReturner.withSize(sizeReturner.getSize());
        for (DataProvider dataProvider : sizeReturner.getDataProviders()) {
            decryptingSizeReturner.addDataProvider(new DecryptingDataProvider(dataProvider, key, cryptoService));
        }
        return decryptingSizeReturner.build();
    }

    @Override
    public Attachment saveAttachment(InputStream input, AttachmentDescription attachment, SizeProvider sizeProvider, Optional<Boolean> optionalEncrypt, Session session) throws OXException {
        boolean encrypt = optionalEncrypt.isPresent() ? optionalEncrypt.get().booleanValue() : hasEncryptedContentElseNeedsEncryption(attachment.getCompositionSpaceId(), session);
        return saveAttachmentEncryptedOrNot(encrypt, input, attachment, sizeProvider, session);
    }

    private Attachment saveAttachmentEncryptedOrNot(boolean entcrypt, InputStream input, AttachmentDescription attachment, SizeProvider sizeProvider, Session session) throws OXException {
        if (!entcrypt) {
            return attachmentStorage.saveAttachment(input, attachment, sizeProvider, Optional.empty(), session);
        }

        // Grab space-associated key
        Key key = getKeyFor(attachment.getCompositionSpaceId(), true, session).get(); // always present since createIfAbsent is true

        // Adjust input stream and size provider
        CryptoService cryptoService = services.getService(CryptoService.class);
        InputStream inputToUse = input;
        SizeProvider sizeProviderToUse = sizeProvider;
        if (null == sizeProviderToUse) {
            final CountingOnlyInputStream countingStream = new CountingOnlyInputStream(input);
            inputToUse = countingStream;
            sizeProviderToUse = new CountingInputStreamSizeProvider(countingStream);
        }
        inputToUse = CryptoUtility.encryptingStreamFor(inputToUse, key, cryptoService);

        // Encrypt attachment's file name
        String name = attachment.getName();
        if (Strings.isNotEmpty(name)) {
            attachment.setName(encrypt(name, key, cryptoService));
        }

        // Save attachment & return decrypted view on it
        Attachment savedAttachment = attachmentStorage.saveAttachment(inputToUse, attachment, sizeProviderToUse, Optional.empty(), session);
        return new DecryptingAttachment(savedAttachment, key, cryptoService);
    }

    @Override
    public void deleteAttachment(UUID id, Session session) throws OXException {
        attachmentStorage.deleteAttachment(id, session);
    }

    @Override
    public void deleteAttachments(List<UUID> ids, Session session) throws OXException {
        attachmentStorage.deleteAttachments(ids, session);
    }

    @Override
    public void deleteAttachmentsByCompositionSpace(UUID compositionSpaceId, Session session) throws OXException {
        attachmentStorage.deleteAttachmentsByCompositionSpace(compositionSpaceId, session);
    }

    @Override
    public void deleteUnreferencedAttachments(Session session) throws OXException {
        attachmentStorage.deleteUnreferencedAttachments(session);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static class CountingInputStreamSizeProvider implements SizeProvider {

        private final CountingOnlyInputStream countingStream;

        /**
         * Initializes a new {@code CountingInputStreamSizeProvider} instance.
         */
        CountingInputStreamSizeProvider(CountingOnlyInputStream countingStream) {
            this.countingStream = countingStream;
        }

        @Override
        public long getSize() {
            return countingStream.getCount();
        }
    }

    private static class FlagAndKey {

        final boolean encrypted;
        final Key key;

        FlagAndKey(boolean encrypted, Key key) {
            super();
            this.encrypted = encrypted;
            this.key = key;
        }
    }

}
