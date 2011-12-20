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

package com.openexchange.secret.impl;

import com.openexchange.crypto.CryptoService;
import com.openexchange.exception.OXException;
import com.openexchange.secret.Decrypter;
import com.openexchange.secret.SecretEncryptionService;
import com.openexchange.secret.SecretEncryptionStrategy;
import com.openexchange.secret.SecretService;
import com.openexchange.session.Session;

/**
 * {@link CryptoSecretEncryptionService} - The {@link SecretEncryptionService} backed by {@link CryptoService}.
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CryptoSecretEncryptionService<T> implements SecretEncryptionService<T> {

    private final TokenList tokenList;

    private final SecretEncryptionStrategy<T> strategy;

    private final CryptoService crypto;

    private final SecretService secretService;

    private final int size;

    /**
     * Initializes a new {@link CryptoSecretEncryptionService}.
     * 
     * @param crypto The crypto service reference
     * @param secretService The fall-back secret service
     * @param strategy The strategy to use
     * @param tokenList The token list
     */
    public CryptoSecretEncryptionService(final CryptoService crypto, final SecretService secretService, final SecretEncryptionStrategy<T> strategy, final TokenList tokenList) {
        super();
        this.crypto = crypto;
        this.secretService = secretService;
        this.strategy = strategy;
        this.tokenList = tokenList;
        size = tokenList.size();
    }

    @Override
    public String encrypt(final Session session, final String toEncrypt) throws OXException {
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
            ret = Character.isWhitespace(str.charAt(i));
        }
        return ret;
    }

    @Override
    public String decrypt(final Session session, final String toDecrypt, final T customizationNote) throws OXException {
        /*
         * Try with last list entry
         */
        try {
            return crypto.decrypt(toDecrypt, tokenList.peekLast().getSecret(session));
        } catch (final OXException x) {
            // Ignore and try other secret
        }
        /*
         * Try other secrets in list
         */
        String decrypted = null;
        for (int i = size - 2; i >= 0; i--) {
            try {
                decrypted = crypto.decrypt(toDecrypt, tokenList.get(i).getSecret(session));
                break;
            } catch (final OXException x) {
                // Ignore and try other secret
            }
        }
        if (decrypted == null) {
            if (customizationNote instanceof Decrypter) {
                final Decrypter decrypter = (Decrypter) customizationNote;
                decrypted = decrypter.getDecrypted(session, toDecrypt);
            } else {
                final String secret = secretService.getSecret(session);
                decrypted = crypto.decrypt(toDecrypt, secret);
            }
        }
        {
            final String recrypted = crypto.encrypt(decrypted, tokenList.peekLast().getSecret(session));
            strategy.update(recrypted, customizationNote);
        }
        return decrypted;
    }

    @Override
    public String toString() {
        return tokenList.toString();
    }

}
