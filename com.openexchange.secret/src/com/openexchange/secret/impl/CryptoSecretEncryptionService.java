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

package com.openexchange.secret.impl;

import java.security.GeneralSecurityException;
import org.apache.commons.logging.Log;
import com.openexchange.crypto.CryptoService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.log.LogFactory;
import com.openexchange.secret.Decrypter;
import com.openexchange.secret.RankingAwareSecretService;
import com.openexchange.secret.SecretEncryptionService;
import com.openexchange.secret.SecretEncryptionStrategy;
import com.openexchange.secret.SecretService;
import com.openexchange.secret.osgi.tools.WhiteboardSecretService;
import com.openexchange.session.Session;

/**
 * {@link CryptoSecretEncryptionService} - The {@link SecretEncryptionService} backed by {@link CryptoService}.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CryptoSecretEncryptionService<T> implements SecretEncryptionService<T> {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(CryptoSecretEncryptionService.class));

    private static final boolean DEBUG = LOG.isDebugEnabled();

    private final TokenList tokenList;

    private final SecretEncryptionStrategy<T> strategy;

    private final CryptoService crypto;

    /**
     * The {@link SecretService} reference with the highest ranking.
     * <p>
     * See {@link WhiteboardSecretService} implementation.
     */
    private final RankingAwareSecretService secretService;

    private final int off;

    private final PasswordSecretService passwordSecretService;

    /**
     * Initializes a new {@link CryptoSecretEncryptionService}.
     *
     * @param crypto The crypto service reference
     * @param secretService The fall-back secret service
     * @param strategy The strategy to use
     * @param tokenList The token list
     */
    public CryptoSecretEncryptionService(final CryptoService crypto, final RankingAwareSecretService secretService, final SecretEncryptionStrategy<T> strategy, final TokenList tokenList) {
        super();
        this.crypto = crypto;
        this.secretService = secretService;
        this.strategy = strategy;
        this.tokenList = tokenList;
        off = tokenList.size() - 2; // Start at end - 2
        passwordSecretService = new PasswordSecretService();
    }

    @Override
    public String encrypt(final Session session, final String toEncrypt) throws OXException {
        /*
         * Check currently applicable SecretService
         */
        final int ranking = secretService.getRanking();
        if (ranking >= 0) { // Greater than or equal to default ranking of zero
            return crypto.encrypt(toEncrypt, secretService.getSecret(session));
        }
        /*
         * Use token-based entry
         */
        return crypto.encrypt(toEncrypt, tokenList.peekLast().getSecret(session));
    }

    @Override
    public String decrypt(final Session session, final String toDecrypt) throws OXException {
        if (isEmpty(toDecrypt)) {
            return toDecrypt;
        }
        return decrypt(session, toDecrypt, null);
    }

    private static boolean isEmpty(final String str) {
        if (null == str || 0 == str.length()) {
            return true;
        }
        final int len = str.length();
        boolean ret = true;
        for (int i = 0; ret && i < len; i++) {
            ret = Strings.isWhitespace(str.charAt(i));
        }
        return ret;
    }

    @Override
    public String decrypt(final Session session, final String toDecrypt, final T customizationNote) throws OXException {
        /*
         * Check currently applicable SecretService
         */
        final int ranking = secretService.getRanking();
        if (ranking >= 0) { // Greater than or equal to default ranking of zero
            final String secret = secretService.getSecret(session);
            try {
                return crypto.decrypt(toDecrypt, secret);
            } catch (final OXException x) {
                try {
                    final String decrypted = OldStyleDecrypt.decrypt(toDecrypt, secret);
                    final String recrypted = crypto.encrypt(decrypted, secret);
                    strategy.update(recrypted, customizationNote);
                    return decrypted;
                } catch (final GeneralSecurityException e) {
                    // Ignore
                }
                // Ignore and try other
            }
        }
        /*-
         * Use token-based entries.
         *
         * Try with last list entry first
         */
        String secret = tokenList.peekLast().getSecret(session);
        try {
            return crypto.decrypt(toDecrypt, secret);
        } catch (final OXException x) {
            try {
                final String decrypted = OldStyleDecrypt.decrypt(toDecrypt, secret);
                final String recrypted = crypto.encrypt(decrypted, ranking >= 0 ? secretService.getSecret(session) : tokenList.peekLast().getSecret(session));
                strategy.update(recrypted, customizationNote);
                return decrypted;
            } catch (final GeneralSecurityException e) {
                // Ignore
            }
            // Ignore and try other
        }
        /*
         * Try other secrets in list
         */
        String decrypted = null;
        for (int i = off; null == decrypted && i >= 0; i--) {
            secret = tokenList.get(i).getSecret(session);
            try {
                decrypted = crypto.decrypt(toDecrypt, secret);
            } catch (final OXException x) {
                try {
                    decrypted = OldStyleDecrypt.decrypt(toDecrypt, secret);
                } catch (final GeneralSecurityException e) {
                    // Ignore
                }
                // Ignore and try other
            }
        }
        /*
         * Try to decrypt "the old way"
         */
        if (decrypted == null) {
            if (DEBUG) {
                LOG.debug("Failed to decrypt password with 'secrets' token list. Retrying with former crypt mechanism");
            }
            if (customizationNote instanceof Decrypter) {
                try {
                    final Decrypter decrypter = (Decrypter) customizationNote;
                    decrypted = decrypter.getDecrypted(session, toDecrypt);
                    if (DEBUG) {
                        LOG.debug("Decrypted password with former crypt mechanism");
                    }
                } catch (final OXException x) {
                    // Ignore and try other
                }
            }
            if (decrypted == null) {
                try {
                    decrypted = decrypthWithPasswordSecretService(toDecrypt, session);
                } catch (final OXException x) {
                    try {
                        decrypted = OldStyleDecrypt.decrypt(toDecrypt, session.getPassword());
                    } catch (final GeneralSecurityException e) {
                        // Ignore
                    }
                    // Ignore and try other
                }
            }
            if (decrypted == null) {
                try {
                    decrypted = decrypthWithSecretService(toDecrypt, session);
                } catch (final OXException e) {
                    try {
                        decrypted = OldStyleDecrypt.decrypt(toDecrypt, secretService.getSecret(session));
                    } catch (final GeneralSecurityException ignore) {
                        // Ignore
                    }
                    if (null == decrypted) {
                        // No more fall-backs available
                        throw e;
                    }
                }
            }
        }
        /*
         * At last, re-crypt password using current secret service & store it
         */
        {
            final String recrypted = crypto.encrypt(decrypted, ranking >= 0 ? secretService.getSecret(session) : tokenList.peekLast().getSecret(session));
            strategy.update(recrypted, customizationNote);
        }
        /*
         * Return plain-text password
         */
        return decrypted;
    }

    private String decrypthWithPasswordSecretService(final String toDecrypt, final Session session) throws OXException {
        final String secret = passwordSecretService.getSecret(session);
        if (isEmpty(secret)) {
            return null;
        }
        final String decrypted = crypto.decrypt(toDecrypt, secret);
        if (DEBUG) {
            LOG.debug("Decrypted password with former crypt mechanism");
        }
        return decrypted;
    }

    private String decrypthWithSecretService(final String toDecrypt, final Session session) throws OXException {
        final String secret = secretService.getSecret(session);
        final String decrypted = crypto.decrypt(toDecrypt, secret);
        if (DEBUG) {
            LOG.debug("Decrypted password with former crypt mechanism");
        }
        return decrypted;
    }

    @Override
    public String toString() {
        return tokenList.toString();
    }

}
