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
