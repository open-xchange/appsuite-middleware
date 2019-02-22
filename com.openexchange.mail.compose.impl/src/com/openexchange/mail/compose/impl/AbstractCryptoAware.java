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

package com.openexchange.mail.compose.impl;

import java.security.Key;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.cascade.ConfigViews;
import com.openexchange.exception.OXException;
import com.openexchange.mail.compose.CompositionSpace;
import com.openexchange.mail.compose.Message;
import com.openexchange.mail.compose.security.CompositionSpaceKeyStorage;
import com.openexchange.mail.compose.security.CompositionSpaceKeyStorageService;
import com.openexchange.server.ServiceExceptionCode;
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

    private CapabilitySet getCapabilitySet(Session session) throws OXException {
        CapabilityService capabilityService = services.getOptionalService(CapabilityService.class);
        if (null == capabilityService) {
            throw ServiceExceptionCode.absentService(CapabilityService.class);
        }
        return capabilityService.getCapabilities(session);
    }

    private boolean isEncryptionEnabled(Session session) throws OXException {
        boolean defaultValue = true;

        ConfigViewFactory viewFactory = services.getOptionalService(ConfigViewFactory.class);
        if (null == viewFactory) {
            return defaultValue;
        }

        ConfigView view = viewFactory.getView(session.getUserId(), session.getContextId());
        return ConfigViews.getDefinedBoolPropertyFrom("com.openexchange.mail.compose.security.encryptionEnabled", defaultValue, view);
    }

    /**
     * Checks whether encryption is needed for specified session.
     * <p>
     * Currently encryption is needed for session-associated user when
     * <ul>
     * <li>Property "com.openexchange.mail.compose.security.encryptionEnabled" is set to "true" (default)</li>
     * <li>Capability "guard" is available</li>
     * </ul>
     *
     * @param session The session
     * @return <code>true</code> if encryption is needed; otherwise <code>false</code>
     * @throws OXException If need for encryption cannot be checked
     */
    protected boolean needsEncryption(Session session) throws OXException {
        return isEncryptionEnabled(session) && getCapabilitySet(session).contains("guard");
    }

    /**
     * Gets the key for given composition space.
     *
     * @param compositionSpaceId The composition space identifier
     * @param createIfAbsent <code>true</code> to create a key if there is none; otherwise <code>false</code>
     * @param session The session
     * @return The key
     * @throws OXException If key cannot be returned
     */
    protected Key getKeyFor(UUID compositionSpaceId, boolean createIfAbsent, Session session) throws OXException {
        CompositionSpaceKeyStorage keyStorage = keyStorageService.getKeyStorageFor(session);
        return keyStorage.getKeyFor(compositionSpaceId, createIfAbsent, session);
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
