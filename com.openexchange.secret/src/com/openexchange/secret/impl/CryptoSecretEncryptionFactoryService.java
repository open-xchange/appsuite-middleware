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

import com.openexchange.crypto.CryptoService;
import com.openexchange.secret.RankingAwareSecretService;
import com.openexchange.secret.SecretEncryptionFactoryService;
import com.openexchange.secret.SecretEncryptionService;
import com.openexchange.secret.SecretEncryptionStrategy;
import com.openexchange.secret.SecretService;
import com.openexchange.secret.osgi.tools.WhiteboardSecretService;
import com.openexchange.session.Session;

/**
 * {@link CryptoSecretEncryptionFactoryService} - The factory.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CryptoSecretEncryptionFactoryService implements SecretEncryptionFactoryService {

    private static final class WrappingRankingAwareSecretService implements RankingAwareSecretService {

        private final SecretService secretService;

        protected WrappingRankingAwareSecretService(final SecretService secretService) {
            super();
            this.secretService = secretService;
        }

        @Override
        public String getSecret(final Session session) {
            return secretService.getSecret(session);
        }

        @Override
        public int getRanking() {
            // Default ranking
            return 0;
        }
    }

    private final CryptoService crypto;

    private final TokenList tokenList;

    private final RankingAwareSecretService secretService;

    /**
     * Initializes a new {@link CryptoSecretEncryptionFactoryService}.
     *
     * @param crypto The crypto service reference
     * @param secretService The fall-back secret service
     * @param patterns The patterns to parse
     */
    public CryptoSecretEncryptionFactoryService(final CryptoService crypto, final SecretService secretService, final String... patterns) {
        super();
        this.crypto = crypto;
        this.secretService =
            secretService instanceof RankingAwareSecretService ? (RankingAwareSecretService) secretService : new WrappingRankingAwareSecretService(
                secretService);
        this.tokenList = TokenList.parsePatterns(patterns);
    }

    /**
     * Initializes a new {@link CryptoSecretEncryptionFactoryService}.
     *
     * @param crypto The crypto service reference
     * @param secretService The fall-back secret service
     * @param tokenList The token list
     */
    public CryptoSecretEncryptionFactoryService(final CryptoService crypto, final WhiteboardSecretService secretService, final TokenList tokenList) {
        super();
        this.crypto = crypto;
        this.secretService = secretService;
        this.tokenList = tokenList;
    }

    @Override
    public <T> SecretEncryptionService<T> createService(final SecretEncryptionStrategy<T> strategy) {
        return new CryptoSecretEncryptionService<T>(crypto, secretService, strategy, tokenList);
    }

}
