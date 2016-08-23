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

package com.openexchange.secret.recovery.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.crypto.CryptoService;
import com.openexchange.exception.OXException;
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

    private volatile WhiteboardEncryptedItemDetector whiteboardEncryptedItemDetector;
    private volatile WhiteboardSecretService whiteboardSecretService;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { CryptoService.class, UserService.class, SecretService.class, SecretUsesPasswordChecker.class, SecretEncryptionFactoryService.class, ConfigViewFactory.class };
    }

    @Override
    protected void startBundle() throws Exception {
        /*
         * Get ranking of currently applicable SecretService reference
         */
        final WhiteboardSecretService whiteboardSecretService = this.whiteboardSecretService = new WhiteboardSecretService(context);
        whiteboardSecretService.open();

        final SecretUsesPasswordChecker checker = getService(SecretUsesPasswordChecker.class);
        if ((whiteboardSecretService.getRanking() < 0) && (null != checker) && checker.usesPassword()) {
            /*-
             * Token list in use and main entry uses password for secret retrieval.
             *
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

            /*
             * Register SecretMigrator
             */
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
                        setableSession.setPassword(oldPassword);
                        final String oldSecret = whiteboardSecretService.getSecret(setableSession);

                        // New secret
                        setableSession.setPassword(newPassword);
                        final String newSecret = whiteboardSecretService.getSecret(setableSession);

                        // Try to migrate with new password applied to session
                        try {
                            final ServerSession serverSession = ServerSessionAdapter.valueOf(setableSession);
                            for (final SecretMigrator migrator : secretMigrators) {
                                migrator.migrate(oldSecret, newSecret, serverSession);
                            }
                        } catch (final Exception e) {
                            log.warn("Password change event could not be handled.", e);
                        }
                    }
                };
                final Dictionary<String, Object> dict = new Hashtable<String, Object>(1);
                dict.put(EventConstants.EVENT_TOPIC, "com/openexchange/passwordchange");
                registerService(EventHandler.class, eventHandler, dict);
            }
        } else {
            registerService(SecretInconsistencyDetector.class, new SecretInconsistencyDetector() {

                @Override
                public String isSecretWorking(final ServerSession session) throws OXException {
                    return null;
                }
            });
            registerService(SecretMigrator.class, new SecretMigrator() {

                @Override
                public void migrate(final String oldSecret, final String newSecret, final ServerSession session) throws OXException {
                    // No nothing
                }
            });
        }
    }

    @Override
    protected void stopBundle() throws Exception {
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
