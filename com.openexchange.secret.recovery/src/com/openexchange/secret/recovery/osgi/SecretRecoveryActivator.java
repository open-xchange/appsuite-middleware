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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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
import org.osgi.framework.Constants;
import com.openexchange.crypto.CryptoService;
import com.openexchange.exception.OXException;
import com.openexchange.secret.SecretService;
import com.openexchange.secret.SecretUsesPasswordChecker;
import com.openexchange.secret.osgi.tools.WhiteboardSecretService;
import com.openexchange.secret.recovery.SecretInconsistencyDetector;
import com.openexchange.secret.recovery.SecretMigrator;
import com.openexchange.secret.recovery.impl.FastSecretInconsistencyDetector;
import com.openexchange.server.osgiservice.HousekeepingActivator;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;

/**
 * {@link SecretRecoveryActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SecretRecoveryActivator extends HousekeepingActivator {

    private volatile WhiteboardSecretMigrator migrator;
    private volatile WhiteboardSecretService secretService;
    private volatile WhiteboardEncryptedItemDetector whiteboardEncryptedItemDetector;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { CryptoService.class, UserService.class, SecretService.class, SecretUsesPasswordChecker.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final boolean usesPassword = getService(SecretUsesPasswordChecker.class).usesPassword();
        if (usesPassword) {
            /*
             * Initialize whiteboard services
             */
            final WhiteboardSecretMigrator migrator = this.migrator = new WhiteboardSecretMigrator(context);
            final WhiteboardSecretService secretService = this.secretService = new WhiteboardSecretService(context);
            final WhiteboardEncryptedItemDetector whiteboardEncryptedItemDetector = this.whiteboardEncryptedItemDetector = new WhiteboardEncryptedItemDetector(context);
            /*
             * Register SecretInconsistencyDetector
             */
            final CryptoService cryptoService = getService(CryptoService.class);
            final UserService userService = getService(UserService.class);
            final FastSecretInconsistencyDetector detector = new FastSecretInconsistencyDetector(secretService, cryptoService, userService, whiteboardEncryptedItemDetector);
            registerService(SecretInconsistencyDetector.class, detector);
            /*
             * Register SecretMigrator
             */
            final Dictionary<String, Object> properties = new Hashtable<String, Object>(1);
            properties.put(Constants.SERVICE_RANKING, Integer.valueOf(1000));
            registerService(SecretMigrator.class, migrator, properties);
            registerService(SecretMigrator.class, detector); // Needs Migration as well
            /*
             * Open whiteboard services
             */
            secretService.open();
            migrator.open();
            whiteboardEncryptedItemDetector.open();
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
        final WhiteboardSecretService ss = secretService;
        if (null != ss) {
            ss.close();
            secretService = null;
        }
        final WhiteboardSecretMigrator migr = migrator;
        if (null != migr) {
            migr.close();
            migrator = null;
        }
        final WhiteboardEncryptedItemDetector detector = whiteboardEncryptedItemDetector;
        if (null != detector) {
            detector.close();
            whiteboardEncryptedItemDetector = null;
        }
    }

}
