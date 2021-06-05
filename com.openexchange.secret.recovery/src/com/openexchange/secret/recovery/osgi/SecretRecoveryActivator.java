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

package com.openexchange.secret.recovery.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.crypto.CryptoService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.ServiceSet;
import com.openexchange.secret.SecretEncryptionFactoryService;
import com.openexchange.secret.SecretService;
import com.openexchange.secret.SecretUsesPasswordChecker;
import com.openexchange.secret.osgi.tools.WhiteboardSecretService;
import com.openexchange.secret.recovery.EncryptedItemCleanUpService;
import com.openexchange.secret.recovery.SecretInconsistencyDetector;
import com.openexchange.secret.recovery.SecretMigrator;
import com.openexchange.secret.recovery.impl.FastSecretInconsistencyDetector;
import com.openexchange.session.Session;
import com.openexchange.session.SetableSession;
import com.openexchange.session.SetableSessionFactory;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.UserService;

/**
 * {@link SecretRecoveryActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SecretRecoveryActivator extends HousekeepingActivator {

    private WhiteboardEncryptedItemDetector whiteboardEncryptedItemDetector;
    private WhiteboardSecretService whiteboardSecretService;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { CryptoService.class, UserService.class, SecretService.class, SecretUsesPasswordChecker.class, SecretEncryptionFactoryService.class, ConfigViewFactory.class };
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        /*
         * Get ranking of currently applicable SecretService reference
         */
        final WhiteboardSecretService whiteboardSecretService = this.whiteboardSecretService = new WhiteboardSecretService(context);
        whiteboardSecretService.open();
        final SecretUsesPasswordChecker usesPasswordChecker = getService(SecretUsesPasswordChecker.class);

        /*
         * Initialize whiteboard services
         */
        final WhiteboardEncryptedItemDetector whiteboardEncryptedItemDetector = new WhiteboardEncryptedItemDetector(context);
        this.whiteboardEncryptedItemDetector = whiteboardEncryptedItemDetector;

        /*
         * Register SecretInconsistencyDetector
         */
        FastSecretInconsistencyDetector detector;
        {
            CryptoService cryptoService = getService(CryptoService.class);
            UserService userService = getService(UserService.class);
            // final SecretService secretService = checker.passwordUsingSecretService();
            SecretEncryptionFactoryService secretEncryptionFactory = getService(SecretEncryptionFactoryService.class);
            ConfigViewFactory configViewFactory = getService(ConfigViewFactory.class);
            detector = new FastSecretInconsistencyDetector(secretEncryptionFactory, cryptoService, userService, whiteboardEncryptedItemDetector, configViewFactory);
        }
        registerService(SecretInconsistencyDetector.class, detector);
        registerService(EncryptedItemCleanUpService.class, detector);
        registerService(SecretMigrator.class, detector); // Needs Migration as well

        whiteboardEncryptedItemDetector.open();

        final ServiceSet<SecretMigrator> secretMigrators = new ServiceSet<SecretMigrator>();
        track(SecretMigrator.class, secretMigrators);
        openTrackers();

        /*
         * Register appropriate event handler
         */
        {
            final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SecretRecoveryActivator.class);
            final EventHandler eventHandler = new EventHandler() {

                @Override
                public void handleEvent(final Event event) {
                    final String oldPassword = (String) event.getProperty("com.openexchange.passwordchange.oldPassword");
                    final String newPassword = (String) event.getProperty("com.openexchange.passwordchange.newPassword");
                    final SetableSession setableSession = SetableSessionFactory.getFactory().setableSessionFor((Session) event.getProperty("com.openexchange.passwordchange.session"));

                    // Old secret
                    String oldSecret;
                    if (usesPasswordChecker != null && usesPasswordChecker.usesPassword()) {
                        setableSession.setPassword(oldPassword);
                        oldSecret = whiteboardSecretService.getSecret(setableSession);
                    } else {
                        oldSecret = oldPassword;
                    }

                    // New secret
                    setableSession.setPassword(newPassword);
                    final String newSecret = whiteboardSecretService.getSecret(setableSession);

                    // Try to migrate with new password applied to session
                    try {
                        final ServerSession serverSession = ServerSessionAdapter.valueOf(setableSession);
                        for (final SecretMigrator migrator : secretMigrators) {
                            migrator.migrate(oldSecret, newSecret, serverSession);
                        }
                    } catch (Exception e) {
                        log.warn("Password change event could not be handled.", e);
                    }
                }
            };
            final Dictionary<String, Object> dict = new Hashtable<String, Object>(1);
            dict.put(EventConstants.EVENT_TOPIC, "com/openexchange/passwordchange");
            registerService(EventHandler.class, eventHandler, dict);
        }
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        super.stopBundle();
        final WhiteboardEncryptedItemDetector detector = whiteboardEncryptedItemDetector;
        if (null != detector) {
            detector.close();
            whiteboardEncryptedItemDetector = null;
        }
        final WhiteboardSecretService whiteboardSecretService = this.whiteboardSecretService;
        if (whiteboardSecretService != null) {
            whiteboardSecretService.close();
            this.whiteboardSecretService = null;
        }
    }

}
