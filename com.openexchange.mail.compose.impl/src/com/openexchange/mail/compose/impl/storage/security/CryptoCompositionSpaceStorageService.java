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

package com.openexchange.mail.compose.impl.storage.security;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.mail.compose.impl.CryptoUtility.decrypt;
import static com.openexchange.mail.compose.impl.CryptoUtility.encrypt;
import java.security.Key;
import java.util.ArrayList;
import java.util.BitSet;
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
import com.openexchange.mail.compose.CompositionSpaceStorageService;
import com.openexchange.mail.compose.Message;
import com.openexchange.mail.compose.MessageDescription;
import com.openexchange.mail.compose.MessageField;
import com.openexchange.mail.compose.impl.AbstractCryptoAware;
import com.openexchange.mail.compose.impl.storage.ImmutableCompositionSpace;
import com.openexchange.mail.compose.impl.storage.ImmutableMessage;
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

    private static final BitSet BITSET_BASE64 = new BitSet(256);
    // Static initializer for BITSET_BASE64
    static {
        for (int i = 'a'; i <= 'z'; i++) {
            BITSET_BASE64.set(i);
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            BITSET_BASE64.set(i);
        }
        for (int i = '0'; i <= '9'; i++) {
            BITSET_BASE64.set(i);
        }
        BITSET_BASE64.set('+');
        BITSET_BASE64.set('/');
        BITSET_BASE64.set('='); // padding character
    }


    private final CompositionSpaceStorageService delegate;

    /**
     * Initializes a new {@link CryptoCompositionSpaceStorageService}.
     */
    public CryptoCompositionSpaceStorageService(CompositionSpaceStorageService delegate, CompositionSpaceKeyStorageService keyStorageService, ServiceLookup services) {
        super(keyStorageService, services);
        this.delegate = delegate;
    }

    private Optional<Key> optionalKey(CompositionSpace compositionSpace, Session session) throws OXException {
        return getKeyFor(compositionSpace.getId(), false, session);
    }

    private CompositionSpace decryptCompositionSpaceIfNeeded(CompositionSpace compositionSpace, Session session, boolean checkNeedsEncryption) throws OXException {
        if ((checkNeedsEncryption ? needsEncryption(session) : true) || hasEncryptedContent(compositionSpace)) {
            // Decryption is needed
            return decryptCompositionSpace(compositionSpace, session, true).compositionSpace;
        }

        // No decryption needed. Check content...
        String content = compositionSpace.getMessage().getContent();
        if (!isBase64String(content)) {
            return compositionSpace;
        }

        // Content is base64 compliant... Thus it appears to be encrypted content, but check says no.
        // Anyway, try to decrypt it
        DecryptResult decryptResult = decryptCompositionSpace(compositionSpace, session, false);
        if (decryptResult.decrypted && LoggerHolder.LOG.isWarnEnabled()) {
            // Content was decrypted...
            StringBuilder logMsg = new StringBuilder("{}The content of composition space {} of user {} in context {} is encrypted, but checks say it isn't{}");
            List<Object> args = new ArrayList<Object>(6);
            args.add(Strings.getLineSeparator());
            args.add(UUIDs.getUnformattedString(compositionSpace.getId()));
            args.add(I(session.getUserId()));
            args.add(I(session.getContextId()));
            args.add(Strings.getLineSeparator());

            logMsg.append("needs encryption: {}{}");
            args.add(B(needsEncryption(session)));
            args.add(Strings.getLineSeparator());

            logMsg.append("has encrypted content: {}{}");
            args.add(B(hasEncryptedContent(compositionSpace)));
            args.add(Strings.getLineSeparator());

            LoggerHolder.LOG.warn(logMsg.toString(), args.toArray(new Object[args.size()]));
        }
        return decryptResult.compositionSpace;
    }

    private DecryptResult decryptCompositionSpace(CompositionSpace compositionSpace, Session session, boolean errorIfNotDecryptable) throws OXException {
        // Fetch key
        Optional<Key> optionalKey = optionalKey(compositionSpace, session);
        if (optionalKey.isPresent() == false) {
            // No key found...
            if (errorIfNotDecryptable) {
                // ... but required
                throw CompositionSpaceErrorCode.MISSING_KEY.create(UUIDs.getUnformattedString(compositionSpace.getId()));
            }
            // ... and not required. Return given composition space as-is
            return new DecryptResult(compositionSpace, false);
        }

        // Decrypt using key
        Key key = optionalKey.get();
        UUID compositionSpaceId = compositionSpace.getId();
        try {
            String plainContent = decrypt(compositionSpace.getMessage().getContent(), key, services.getService(CryptoService.class));
            Message msg = ImmutableMessage.builder()
                .fromMessage(compositionSpace.getMessage())
                .withContent(plainContent)
                .build();
            ImmutableCompositionSpace decryptedSpace = new ImmutableCompositionSpace(compositionSpaceId, msg, compositionSpace.getLastModified());
            return new DecryptResult(decryptedSpace, true);
        } catch (OXException e) {
            if (errorIfNotDecryptable) {
                if (CryptoErrorMessage.BadPassword.equals(e)) {
                    throw CompositionSpaceErrorCode.MISSING_KEY.create(e, UUIDs.getUnformattedString(compositionSpaceId));
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

        if (messageDesc.containsContent()) {
            String content = messageDesc.getContent();
            if (null != content) {
                messageDesc.setContent(encrypt(content, optionalKey.get(), services.getService(CryptoService.class)));
            }
        }

        // Mark to have encrypted content
        messageDesc.setContentEncrypted(true);
    }

    private boolean isBase64String(String toCheck) {
        if (Strings.isEmpty(toCheck)) {
            return false;
        }

        {
            /*-
             * Plain checks:
             * * Check that the length is a multiple of 4 characters
             * * Check that every character is in the set A-Z, a-z, 0-9, +, / except for padding at the end which is 0, 1 or 2 '=' characters
             */

            int length = toCheck.length();
            if (length % 4 != 0) {
                return false;
            }

            for (int i = length; i-- > 0;) {
                char ch = toCheck.charAt(i);
                if (!BITSET_BASE64.get(ch)) {
                    return false;
                }
            }
        }

        // Ultimately, check if base64 decodable
        try {
            java.util.Base64.getDecoder().decode(toCheck);
            return true;
        } catch (IllegalArgumentException e) {
            // Apparently no base64 string
            return false;
        }
    }

    @Override
    public boolean isContentEncrypted(Session session, UUID id) throws OXException {
        return delegate.isContentEncrypted(session, id);
    }

    @Override
    public CompositionSpace getCompositionSpace(Session session, UUID id) throws OXException {
        CompositionSpace compositionSpace = delegate.getCompositionSpace(session, id);
        if (null != compositionSpace) {
            compositionSpace = decryptCompositionSpaceIfNeeded(compositionSpace, session, true);
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
            if (needsEncryption(session)) {
                List<CompositionSpace> spaces = new ArrayList<>(size);
                for (CompositionSpace compositionSpace : compositionSpaces) {
                    try {
                        spaces.add(decryptCompositionSpace(compositionSpace, session, true).compositionSpace);
                    } catch (OXException e) {
                        if (CompositionSpaceErrorCode.MISSING_KEY.equals(e)) {
                            // Composition space is actually useless
                            closeCompositionSpace(session, compositionSpace.getId());
                            LoggerHolder.LOG.info("Dropped composition space {} since associated key for decrypting is missing", UUIDs.getUnformattedString(compositionSpace.getId()));
                        } else {
                            throw e;
                        }
                    }
                }
                compositionSpaces = spaces;
            } else {
                // Check individually if space holds encrypted content
                List<CompositionSpace> spaces = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    CompositionSpace compositionSpace = compositionSpaces.get(i);
                    spaces.add(decryptCompositionSpaceIfNeeded(compositionSpace, session, false));
                }
                compositionSpaces = spaces;
            }
        }

        return compositionSpaces;
    }

    @Override
    public CompositionSpace openCompositionSpace(Session session, CompositionSpaceDescription compositionSpaceDesc) throws OXException {
        if (!needsEncryption(session)) {
            return delegate.openCompositionSpace(session, compositionSpaceDesc);
        }

        encryptCompositionSpaceDescription(compositionSpaceDesc, true, session);
        CompositionSpace openedCompositionSpace = delegate.openCompositionSpace(session, compositionSpaceDesc);
        return decryptCompositionSpace(openedCompositionSpace, session, true).compositionSpace;
    }

    @Override
    public CompositionSpace updateCompositionSpace(Session session, CompositionSpaceDescription compositionSpaceDesc) throws OXException {
        if (!needsEncryption(session)) {
            // Un-Mark to have encrypted content
            compositionSpaceDesc.getMessage().setContentEncrypted(false);
            return delegate.updateCompositionSpace(session, compositionSpaceDesc);
        }

        encryptCompositionSpaceDescription(compositionSpaceDesc, false, session);
        CompositionSpace updatedCompositionSpace = delegate.updateCompositionSpace(session, compositionSpaceDesc);
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
