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

package com.openexchange.mail.compose.impl;

import java.security.Key;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.cascade.ConfigViews;
import com.openexchange.exception.OXException;
import com.openexchange.mail.compose.CompositionSpace;
import com.openexchange.mail.compose.CryptoUtility;
import com.openexchange.mail.compose.Message;
import com.openexchange.mail.compose.security.CompositionSpaceKeyStorage;
import com.openexchange.mail.compose.security.CompositionSpaceKeyStorageService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link AbstractCryptoAware} - Provides helper method to check whether encryption is needed.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public abstract class AbstractCryptoAware {

    /** The service look-up */
    protected final ServiceLookup services;

    /** The key storage service */
    protected final CompositionSpaceKeyStorageService keyStorageService;

    /**
     * Initializes a new {@link AbstractCryptoAware}.
     */
    protected AbstractCryptoAware(CompositionSpaceKeyStorageService keyStorageService, ServiceLookup services) {
        super();
        this.keyStorageService = keyStorageService;
        this.services = services;
    }

    /**
     * Checks if given composition space appears to hold encrypted content.
     *
     * @param compositionSpace The composition space to check
     * @return <code>true</code> if composition space holds encrypted content; otherwise <code>false</code>
     */
    protected boolean hasEncryptedContent(CompositionSpace compositionSpace) {
        if (null == compositionSpace) {
            return false;
        }

        Message message = compositionSpace.getMessage();
        return null == message ? false : message.isContentEncrypted();
    }

    /**
     * Checks if a composition space is supposed to be deleted once associated key needed for decryption is missing.
     *
     * @param session The session
     * @return <code>true</code> for auto-deletion; otherwise <code>false</code>
     * @throws OXException If option cannot be checked
     */
    protected boolean autoDeleteIfKeyIsMissing(Session session) throws OXException {
        boolean defaultValue = false;

        ConfigViewFactory viewFactory = services.getOptionalService(ConfigViewFactory.class);
        if (null == viewFactory) {
            return defaultValue;
        }

        ConfigView view = viewFactory.getView(session.getUserId(), session.getContextId());
        return ConfigViews.getDefinedBoolPropertyFrom("com.openexchange.mail.compose.security.autoDeleteIfKeyIsMissing", defaultValue, view);
    }

    /**
     * Checks whether encryption is needed for specified session.
     * <p>
     * Currently encryption is needed for session-associated user when
     * <ul>
     * <li>Property "com.openexchange.mail.compose.security.encryptionEnabled" is set to "true" (default)</li>
     * <li>Capability "guard" is available (also set via configuration; see <a href="https://www.oxpedia.org/wiki/index.php?title=AppSuite:OX_Guard#Open-Xchange_Middleware_Configuration">here</a>)</li>
     * </ul>
     *
     * @param session The session
     * @return <code>true</code> if encryption is needed; otherwise <code>false</code>
     * @throws OXException If need for encryption cannot be checked
     */
    protected boolean needsEncryption(Session session) throws OXException {
        return CryptoUtility.needsEncryption(session, services);
    }

    /**
     * Gets the key for given composition space.
     *
     * @param compositionSpaceId The composition space identifier
     * @param createIfAbsent <code>true</code> to create a key if there is none; otherwise <code>false</code>
     * @param session The session
     * @return The key or <code>null</code>; never <code>null</code> if <code>createIfAbsent</code> is <code>true</code>
     * @throws OXException If key cannot be returned
     */
    protected Optional<Key> getKeyFor(UUID compositionSpaceId, boolean createIfAbsent, Session session) throws OXException {
        CompositionSpaceKeyStorage keyStorage = keyStorageService.getKeyStorageFor(session);
        return Optional.ofNullable(keyStorage.getKeyFor(compositionSpaceId, createIfAbsent, session));
    }

    /**
     * Deletes the key for given composition space.
     *
     * @param compositionSpaceId The composition space identifier
     * @param session The session
     * @return <code>true</code> if deleted; otherwise <code>false</code>
     * @throws OXException If key cannot be deleted
     */
    protected boolean deleteKeyFor(UUID compositionSpaceId, Session session) throws OXException {
        CompositionSpaceKeyStorage keyStorage = keyStorageService.getKeyStorageFor(session);
        return keyStorage.deleteKeyFor(compositionSpaceId, session);
    }

    /**
     * Deletes the keys for given composition spaces.
     *
     * @param compositionSpaceIds The composition space identifiers
     * @param session The session
     * @return A listing of those composition space identifiers whose key could not be deleted
     * @throws OXException If key cannot be deleted
     */
    protected List<UUID> deleteKeysFor(Collection<UUID> compositionSpaceIds, Session session) throws OXException {
        if (null == compositionSpaceIds || compositionSpaceIds.isEmpty()) {
            return Collections.emptyList();
        }
        CompositionSpaceKeyStorage keyStorage = keyStorageService.getKeyStorageFor(session);
        return keyStorage.deleteKeysFor(compositionSpaceIds, session);
    }

}
