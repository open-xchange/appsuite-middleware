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

package com.openexchange.secret.impl;

import java.security.GeneralSecurityException;
import com.openexchange.crypto.CryptoService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.secret.Decrypter;
import com.openexchange.secret.RankingAwareSecretService;
import com.openexchange.secret.SecretEncryptionService;
import com.openexchange.secret.SecretEncryptionStrategy;
import com.openexchange.secret.SecretExceptionCodes;
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

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CryptoSecretEncryptionService.class);

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
        /*-
         * Check currently applicable SecretService
         *
         * Is it greater than or equal to default ranking of zero?
         * If not use token-based entry
         */
        String secret = (secretService.getRanking() >= 0) ? secretService.getSecret(session) : tokenList.peekLast().getSecret(session);
        if (Strings.isEmpty(secret)) {
            throw SecretExceptionCodes.EMPTY_SECRET.create();
        }
        return crypto.encrypt(toEncrypt, secret);
    }

    @Override
    public String decrypt(final Session session, final String toDecrypt) throws OXException {
        if (Strings.isEmpty(toDecrypt)) {
            return toDecrypt;
        }
        return decrypt(session, toDecrypt, null);
    }

    @Override
    public String decrypt(final Session session, final String toDecrypt, final T customizationNote) throws OXException {
        // Ranking and secret from currently highest-ranked SecretService implementation
        int ranking = secretService.getRanking();
        String secretFromSecretService = secretService.getSecret(session);
        Integer iUserId = Integer.valueOf(session.getUserId());
        Integer iContextId = Integer.valueOf(session.getContextId());

        // Check with currently applicable SecretService
        boolean checkedWithApplicableSecretService = false;
        if (ranking >= 0) { // Greater than or equal to default ranking of zero
            String secret = secretFromSecretService;
            if (Strings.isEmpty(secret)) {
                throw SecretExceptionCodes.EMPTY_SECRET.create();
            }
            try {
                return crypto.decrypt(toDecrypt, secret);
            } catch (OXException x) {
                LOG.debug("Failed to decrypt using privileged SecretService '{}' (user={}, context={})", secretService.getClass().getName(), iUserId, iContextId, x);
                try {
                    final String decrypted = OldStyleDecrypt.decrypt(toDecrypt, secret);
                    final String recrypted = recrypt(decrypted, secret);
                    strategy.update(recrypted, customizationNote);
                    return decrypted;
                } catch (final GeneralSecurityException e) {
                    LOG.debug("Failed to decrypt using old-style decryptor after failed privileged SecretService '{}' (user={}, context={})", secretService.getClass().getName(), iUserId, iContextId, e);
                }
                // Ignore and try other
            }
            checkedWithApplicableSecretService = true;
        }

        /*-
         * Use token-based entries.
         *
         * Try with last list entry first
         */
        String secret = tokenList.peekLast().getSecret(session);
        {
            boolean emptySecret = Strings.isEmpty(secret);
            if (!checkedWithApplicableSecretService && emptySecret) {
                throw SecretExceptionCodes.EMPTY_SECRET.create();
            }
            if (!emptySecret) {
                try {
                    return crypto.decrypt(toDecrypt, secret);
                } catch (final OXException x) {
                    SecretService last = tokenList.peekLast();
                    LOG.debug("Failed to decrypt using currently applicable SecretService '{}' (user={}, context={})", last, iUserId, iContextId, x);
                    try {
                        final String decrypted = OldStyleDecrypt.decrypt(toDecrypt, secret);
                        final String recrypted = recrypt(decrypted, ranking >= 0 ? secretFromSecretService : last.getSecret(session));
                        strategy.update(recrypted, customizationNote);
                        return decrypted;
                    } catch (final GeneralSecurityException e) {
                        LOG.debug("Failed to decrypt using old-style decryptor after failed currently applicable SecretService '{}' (user={}, context={})", last, iUserId, iContextId, e);
                    }
                    // Ignore and try other
                }
            }
        }

        // Try other secrets in list
        String decrypted = null;
        for (int i = off; null == decrypted && i >= 0; i--) {
            SecretService current = tokenList.get(i);
            secret = current.getSecret(session);
            if (!Strings.isEmpty(secret)) {
                try {
                    decrypted = crypto.decrypt(toDecrypt, secret);
                } catch (final OXException x) {
                    LOG.debug("Failed to decrypt using next SecretService '{}' (user={}, context={})", current, iUserId, iContextId, x);
                    try {
                        decrypted = OldStyleDecrypt.decrypt(toDecrypt, secret);
                    } catch (final GeneralSecurityException e) {
                        LOG.debug("Failed to decrypt using old-style decryptor after failed next SecretService '{}' (user={}, context={})", current, iUserId, iContextId, e);
                    }
                    // Ignore and try other
                }
            }
        }

        // Try to decrypt "the old way"
        if (decrypted == null) {
            LOG.debug("Failed to decrypt password with 'secrets' token list. Retrying with former crypt mechanism (user={}, context={})", iUserId, iContextId);
            if (customizationNote instanceof Decrypter) {
                try {
                    final Decrypter decrypter = (Decrypter) customizationNote;
                    decrypted = decrypter.getDecrypted(session, toDecrypt);
                    LOG.debug("Decrypted password with former crypt mechanism");
                } catch (final OXException x) {
                    // Ignore and try other
                    LOG.debug("Failed to decrypt with former crypt mechanism (user={}, context={})", iUserId, iContextId, x);
                }
            }
            if (decrypted == null) {
                try {
                    decrypted = decrypthWithPasswordSecretService(toDecrypt, session);
                } catch (final OXException x) {
                    LOG.debug("Failed to decrypt using explicit password-based SecretService '{}'", passwordSecretService.getClass().getName(), x);
                    try {
                        decrypted = OldStyleDecrypt.decrypt(toDecrypt, session.getPassword());
                    } catch (final GeneralSecurityException e) {
                        LOG.debug("Failed to decrypt using old-style decryptor after failed explicit password-based SecretService '{}' (user={}, context={})", passwordSecretService.getClass().getName(), iUserId, iContextId, e);
                    }
                    // Ignore and try other
                }
            }
            if (decrypted == null) {
                // Get secret from SecretService
                secret = secretFromSecretService;
                if (Strings.isEmpty(secret)) {
                    throw SecretExceptionCodes.EMPTY_SECRET.create();
                }
                try {
                    decrypted = decrypthWithCryptoService(toDecrypt, secret);
                } catch (final OXException e) {
                    LOG.debug("Failed last attempt to decrypt using privileged SecretService '{}' (user={}, context={})", secretService.getClass().getName(), iUserId, iContextId, e);
                    try {
                        decrypted = OldStyleDecrypt.decrypt(toDecrypt, secret);
                    } catch (final GeneralSecurityException gse) {
                        LOG.debug("Failed to decrypt using old-style decryptor after failed privileged SecretService '{}' (user={}, context={})", secretService.getClass().getName(), iUserId, iContextId, gse);
                    }
                    if (null == decrypted) {
                        // No more fall-backs available
                        throw e;
                    }
                }
            }
        }

        // At last, re-crypt password using current secret service & store it
        {
            SecretService last = tokenList.peekLast();
            String recrypted = recrypt(decrypted, ranking >= 0 ? secretFromSecretService : last.getSecret(session));
            strategy.update(recrypted, customizationNote);
            LOG.debug("Updated encrypted string using currently applicable SecretService '{}' (user={}, context={})", last, iUserId, iContextId);
        }

        // Return decrypted string
        return decrypted;
    }

    private String recrypt(String decrypted, String secret) throws OXException {
        if (Strings.isEmpty(secret)) {
            throw SecretExceptionCodes.EMPTY_SECRET.create();
        }
        return crypto.encrypt(decrypted, secret);
    }

    private String decrypthWithPasswordSecretService(String toDecrypt, Session session) throws OXException {
        String secret = passwordSecretService.getSecret(session);
        if (Strings.isEmpty(secret)) {
            return null;
        }
        String decrypted = crypto.decrypt(toDecrypt, secret);
        LOG.debug("Decrypted password with former crypt mechanism");
        return decrypted;
    }

    private String decrypthWithCryptoService(final String toDecrypt, final String secret) throws OXException {
        String decrypted = crypto.decrypt(toDecrypt, secret);
        LOG.debug("Decrypted password with former crypt mechanism");
        return decrypted;
    }

    @Override
    public String toString() {
        return tokenList.toString();
    }

}
