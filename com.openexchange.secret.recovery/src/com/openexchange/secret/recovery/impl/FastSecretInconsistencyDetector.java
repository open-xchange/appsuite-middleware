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

package com.openexchange.secret.recovery.impl;

import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.crypto.CryptoService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.secret.SecretEncryptionFactoryService;
import com.openexchange.secret.SecretEncryptionService;
import com.openexchange.secret.SecretEncryptionStrategy;
import com.openexchange.secret.recovery.EncryptedItemCleanUpService;
import com.openexchange.secret.recovery.EncryptedItemDetectorService;
import com.openexchange.secret.recovery.SecretInconsistencyDetector;
import com.openexchange.secret.recovery.SecretMigrator;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;

/**
 * {@link FastSecretInconsistencyDetector}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FastSecretInconsistencyDetector implements SecretInconsistencyDetector, SecretMigrator, EncryptedItemCleanUpService, SecretEncryptionStrategy<UserAndContext> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FastSecretInconsistencyDetector.class);

    private static final String PROPERTY_TOKEN = "com.openexchange.secret.recovery.fast.token";
    private static final String PROPERTY_ENABLED = "com.openexchange.secret.recovery.fast.enabled";

    private final SecretEncryptionFactoryService secretEncryptionFactory;
    private final CryptoService cryptoService;
    private final UserService userService;
    private final EncryptedItemDetectorService detector;
    private final ConfigViewFactory configViewFactory;

    /**
     * Initializes a new {@link FastSecretInconsistencyDetector}.
     */
    public FastSecretInconsistencyDetector(SecretEncryptionFactoryService secretEncryptionFactory, CryptoService cryptoService, UserService userService, EncryptedItemDetectorService detector, ConfigViewFactory configViewFactory) {
        super();
        this.secretEncryptionFactory = secretEncryptionFactory;
        this.cryptoService = cryptoService;
        this.userService = userService;
        this.detector = detector;
        this.configViewFactory = configViewFactory;
    }

    @Override
    public void update(String recrypted, UserAndContext userAndContext) throws OXException {
        doSaveNewToken(recrypted, userAndContext.getUserId(), userAndContext.getContext());
    }

    @Override
    public String isSecretWorking(final ServerSession session) throws OXException {
        if (!isEnabled(session)) {
            LOG.debug("Fast-crypt token not enabled for user {} in context {}", session.getUserId(), session.getContextId());
            return null;
        }

        String token = session.getUser().getAttributes().get(PROPERTY_TOKEN);
        if (token == null) {
            saveNewToken(session);
            return null;
        }

        if (canDecrypt(token, session)) {
            return null;
        }

        if (detector.hasEncryptedItems(session)) {
            return "Could not decrypt token";
        }

        saveNewToken(session);
        return null;
    }

    private boolean isEnabled(ServerSession session) {
        boolean def = true;

        try {
            ConfigView view = configViewFactory.getView(session.getUserId(), session.getContextId());
            ComposedConfigProperty<Boolean> property = view.property(PROPERTY_ENABLED, boolean.class);
            if (null == property || !property.isDefined()) {
                return def;
            }

            return property.get().booleanValue();
        } catch (OXException e) {
            LOG.error("Failed to checked if fast-crypt token is enabled for user {} in context {}", session.getUserId(), session.getContextId(), e);
            return def;
        }
    }

    private static final String TEST_STRING = "supercalifragilisticexplialidocious";

    private boolean canDecrypt(String encryptedToken, ServerSession session) {
        SecretEncryptionService<UserAndContext> encryptionService = secretEncryptionFactory.createService(this);
        try {
            return TEST_STRING.equals(encryptionService.decrypt(session, encryptedToken, new UserAndContext(session.getUserId(), session.getContext())));
        } catch (final OXException e) {
            LOG.debug("Could not decrypt fast-crypt token from user's attributes.", e);
            return false;
        }
    }

    private void saveNewToken(ServerSession session) {
        try {
            SecretEncryptionService<UserAndContext> encryptionService = secretEncryptionFactory.createService(this);
            String encrypted = encryptionService.encrypt(session, TEST_STRING);
            doSaveNewToken(encrypted, session.getUserId(), session.getContext());
        } catch (final OXException e) {
            LOG.error("", e);
        }
    }

    private void saveNewTokenUsingSecret(String secret, ServerSession session) {
        try {
            String newEncryptedToken = cryptoService.encrypt(TEST_STRING, secret);
            doSaveNewToken(newEncryptedToken, session.getUserId(), session.getContext());
        } catch (final OXException e) {
            LOG.error("", e);
        }
    }

    private void doSaveNewToken(String toStore, int userId, Context context) throws OXException {
        userService.setAttribute(PROPERTY_TOKEN, toStore, userId, context);
        LOG.debug("Saved fast-crypt token in user's attributes: {}", toStore, new Throwable("Saved fast-crypt token"));
    }

    @Override
    public void migrate(final String oldSecret, final String newSecret, final ServerSession session) throws OXException {
        saveNewTokenUsingSecret(newSecret, session);
    }

    @Override
    public void cleanUpEncryptedItems(String secret, ServerSession session) throws OXException {
        userService.setAttribute(PROPERTY_TOKEN, null, session.getUserId(), session.getContext());
    }

    @Override
    public void removeUnrecoverableItems(String secret, ServerSession session) throws OXException {
        saveNewTokenUsingSecret(secret, session);
    }

}
