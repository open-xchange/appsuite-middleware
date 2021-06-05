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

package com.openexchange.secret.recovery.impl;

import static com.openexchange.java.Autoboxing.I;
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
            LOG.debug("Fast-crypt token not enabled for user {} in context {}", I(session.getUserId()), I(session.getContextId()));
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
            LOG.error("Failed to checked if fast-crypt token is enabled for user {} in context {}", I(session.getUserId()), I(session.getContextId()), e);
            return def;
        }
    }

    private static final String TEST_STRING = "supercalifragilisticexplialidocious";

    private boolean canDecrypt(String encryptedToken, ServerSession session) {
        SecretEncryptionService<UserAndContext> encryptionService = secretEncryptionFactory.createService(this);
        try {
            return TEST_STRING.equals(encryptionService.decrypt(session, encryptedToken, new UserAndContext(session.getUserId(), session.getContext())));
        } catch (OXException e) {
            LOG.debug("Could not decrypt fast-crypt token from user's attributes.", e);
            return false;
        }
    }

    private void saveNewToken(ServerSession session) {
        try {
            SecretEncryptionService<UserAndContext> encryptionService = secretEncryptionFactory.createService(this);
            String encrypted = encryptionService.encrypt(session, TEST_STRING);
            doSaveNewToken(encrypted, session.getUserId(), session.getContext());
        } catch (OXException e) {
            LOG.error("", e);
        }
    }

    private void saveNewTokenUsingSecret(String secret, ServerSession session) {
        try {
            String newEncryptedToken = cryptoService.encrypt(TEST_STRING, secret);
            doSaveNewToken(newEncryptedToken, session.getUserId(), session.getContext());
        } catch (OXException e) {
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
