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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.passcrypt;

import com.openexchange.config.ConfigurationService;
import com.openexchange.crypto.CryptoService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserAttributeAccess;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.login.LoginResult;
import com.openexchange.secret.recovery.EncryptedItemCleanUpService;
import com.openexchange.secret.recovery.EncryptedItemDetectorService;
import com.openexchange.secret.recovery.SecretMigrator;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link PasswordCrypter} - Crypts password to support digest authentication mechanisms.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class PasswordCrypter implements LoginHandlerService, EncryptedItemDetectorService, SecretMigrator, EncryptedItemCleanUpService {

    private static final String PASSCRYPT = "passcrypt";

    private final String key;

    /**
     * Initializes a new {@link PasswordCrypter}.
     */
    public PasswordCrypter(final ConfigurationService configurationService) {
        super();
        if (null == configurationService) {
            throw new IllegalArgumentException("configurationService is null.");
        }
        key = configurationService.getProperty("com.openexchange.passcrypt.key");
        if (null == key) {
            throw new IllegalStateException("Missing property \"com.openexchange.passcrypt.key\".");
        }
    }

    @Override
    public void handleLogin(final LoginResult login) throws OXException {
        /*
         * Crypt & store password to support digest authentication mechanisms which require to look-up a user's password by a certain
         * user identifier (login)
         */
        final CryptoService cryptoService = PasscryptServiceRegistry.getServiceRegistry().getService(CryptoService.class);
        final String password = login.getSession().getPassword();
        if (null != cryptoService && null != password) {
            final UserAttributeAccess attributeAccess = UserAttributeAccess.getDefaultInstance();
            final User user = login.getUser();
            /*
             * Previous pass-crypt
             */
            final String prevPassCrypt = attributeAccess.getAttribute(PASSCRYPT, user, null);
            /*
             * New pass-crypt
             */
            final String newPassCrypt = cryptoService.encrypt(password, key);
            if (null == prevPassCrypt || !prevPassCrypt.equals(newPassCrypt)) {
                attributeAccess.setAttribute(PASSCRYPT, newPassCrypt, user, login.getContext());
            }
        }
    }

    @Override
    public void handleLogout(final LoginResult logout) {
        // Nothing to to.
    }

    @Override
    public boolean hasEncryptedItems(final ServerSession session) throws OXException {
        return false;
    }

    @Override
    public void cleanUpEncryptedItems(String secret, ServerSession session) throws OXException {
        // Ignore
    }
    
    @Override
    public void removeUnrecoverableItems(String secret, ServerSession session) throws OXException {
        // Ignore
    }

    @Override
    public void migrate(final String oldSecret, final String newSecret, final ServerSession session) throws OXException {
        final CryptoService cryptoService = PasscryptServiceRegistry.getServiceRegistry().getService(CryptoService.class, true);
        final UserAttributeAccess attributeAccess = UserAttributeAccess.getDefaultInstance();
        /*
         * Encrypt & save new secret
         */
        final String newPassCrypt = cryptoService.encrypt(newSecret, key);
        attributeAccess.setAttribute(PASSCRYPT, newPassCrypt, session.getUser(), session.getContext());
    }

}
