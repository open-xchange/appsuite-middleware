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

import static com.openexchange.mail.compose.impl.CryptoUtility.decrypt;
import static com.openexchange.mail.compose.impl.CryptoUtility.encrypt;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.openexchange.crypto.CryptoErrorMessage;
import com.openexchange.crypto.CryptoService;
import com.openexchange.exception.OXException;
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

    private final CompositionSpaceStorageService delegate;

    /**
     * Initializes a new {@link CryptoCompositionSpaceStorageService}.
     */
    public CryptoCompositionSpaceStorageService(CompositionSpaceStorageService delegate, CompositionSpaceKeyStorageService keyStorageService, ServiceLookup services) {
        super(keyStorageService, services);
        this.delegate = delegate;
    }

    private CompositionSpace decryptCompositionSpace(CompositionSpace compositionSpace, Session session) throws OXException {
        UUID id = compositionSpace.getId();
        try {
            Key key = getKeyFor(id, false, session);
            if (null == key) {
                throw CompositionSpaceErrorCode.MISSING_KEY.create(UUIDs.getUnformattedString(id));
            }

            String plainContent = decrypt(compositionSpace.getMessage().getContent(), key, services.getService(CryptoService.class));
            Message msg = ImmutableMessage.builder()
                .fromMessage(compositionSpace.getMessage())
                .withContent(plainContent)
                .build();
            return new ImmutableCompositionSpace(id, msg, compositionSpace.getLastModified());
        } catch (OXException e) {
            if (CryptoErrorMessage.BadPassword.equals(e)) {
                throw CompositionSpaceErrorCode.MISSING_KEY.create(e, UUIDs.getUnformattedString(id));
            }
            throw e;
        }
    }

    private void encryptCompositionSpaceDescription(CompositionSpaceDescription compositionSpaceDesc, boolean createKeyIfAbsent, Session session) throws OXException {
        UUID compositionSpaceId = compositionSpaceDesc.getUuid();
        Key key = getKeyFor(compositionSpaceId, createKeyIfAbsent, session);
        if (!createKeyIfAbsent && key == null) {
            throw CompositionSpaceErrorCode.MISSING_KEY.create(UUIDs.getUnformattedString(compositionSpaceId));
        }

        MessageDescription messageDesc = compositionSpaceDesc.getMessage();

        if (messageDesc.containsContent()) {
            String content = messageDesc.getContent();
            if (null != content) {
                messageDesc.setContent(encrypt(content, key, services.getService(CryptoService.class)));
            }
        }

        // Mark to have encrypted content
        messageDesc.setContentEncrypted(true);
    }

    @Override
    public boolean isContentEncrypted(Session session, UUID id) throws OXException {
        return delegate.isContentEncrypted(session, id);
    }

    @Override
    public CompositionSpace getCompositionSpace(Session session, UUID id) throws OXException {
        CompositionSpace compositionSpace = delegate.getCompositionSpace(session, id);
        if (null != compositionSpace) {
            if (needsEncryption(session) || hasEncryptedContent(compositionSpace)) {
                compositionSpace = decryptCompositionSpace(compositionSpace, session);
            }
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
                    spaces.add(decryptCompositionSpace(compositionSpace, session));
                }
                compositionSpaces = spaces;
            } else {
                // Check individually if space holds encrypted content
                List<CompositionSpace> spaces = null;
                for (int i = 0; i < size; i++) {
                    CompositionSpace compositionSpace = compositionSpaces.get(i);
                    if (hasEncryptedContent(compositionSpace)) {
                        // Encrypted content...
                        if (null == spaces) {
                            spaces = new ArrayList<>(size);
                            if (i > 0) {
                                spaces.addAll(compositionSpaces.subList(0, i));
                            }
                        }
                        spaces.add(decryptCompositionSpace(compositionSpace, session));
                    } else {
                        if (null != spaces) {
                            spaces.add(compositionSpace);
                        }
                    }
                }
                if (null != spaces) {
                    compositionSpaces = spaces;
                }
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
        return decryptCompositionSpace(openedCompositionSpace, session);
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
        return decryptCompositionSpace(updatedCompositionSpace, session);
    }

    @Override
    public boolean closeCompositionSpace(Session session, UUID id) throws OXException {
        boolean deleted = delegate.closeCompositionSpace(session, id);
        if (deleted) {
            deleteKeyFor(id, session);
        }
        return deleted;
    }

    @Override
    public List<UUID> deleteExpiredCompositionSpaces(Session session, long maxIdleTimeMillis) throws OXException {
        List<UUID> deletedCompositionSpaces = delegate.deleteExpiredCompositionSpaces(session, maxIdleTimeMillis);
        deleteKeysFor(deletedCompositionSpaces, session);
        return deletedCompositionSpaces;
    }

}
