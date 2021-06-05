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

import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.secret.RankingAwareSecretService;
import com.openexchange.secret.SecretService;
import com.openexchange.session.Session;

/**
 * {@link TokenBasedSecretService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class TokenBasedSecretService implements RankingAwareSecretService {

    /** The random. */
    public static final AtomicReference<String> RANDOM = new AtomicReference<String>();

    // --------------------------------------------------------------------------------------------------------------------------------

    private volatile SecretService impl;
    private final int ranking;

    /**
     * Initializes a new {@link TokenBasedSecretService}.
     */
    public TokenBasedSecretService(TokenRow tokenRow) {
        super();
        ranking = Integer.MIN_VALUE;
        applyTokenRow(tokenRow);
    }

    /**
     * Initializes a new {@link TokenBasedSecretService}.
     */
    public TokenBasedSecretService(TokenList tokenList) {
        super();
        ranking = Integer.MIN_VALUE;
        this.impl = tokenList.peekLast();
    }

    @Override
    public int getRanking() {
        return ranking;
    }

    /**
     * Sets the token row
     *
     * @param tokenRow The token row to set
     */
    public void setTokenList(TokenRow tokenRow) {
        applyTokenRow(tokenRow);
    }

    private void applyTokenRow(TokenRow tokenRow) {
        final TokenRow tr = (null == tokenRow || tokenRow.isEmpty()) ? TokenRow.DEFAULT_TOKEN_ROW : tokenRow;
        final int size = tr.size();
        if (1 == size) {
            final Token token = tr.get(0);
            impl = new SecretService() {

                @Override
                public String getSecret(final Session session) {
                    return token.getFrom(session);
                }

                @Override
                public String toString() {
                    return token.toString();
                }
            };
        } else {
            impl = new SecretService() {

                @Override
                public String getSecret(Session session) {
                    StringBuilder sb = new StringBuilder(16);
                    for (Token token : tr) {
                        sb.append(token.getFrom(session));
                    }
                    return sb.toString();
                }

                @Override
                public String toString() {
                    if (tr.isEmpty()) {
                        return "<empty>";
                    }
                    StringBuilder sb = new StringBuilder(32);
                    sb.append(tr.get(0));
                    for (int i = 1; i < size; i++) {
                        sb.append(" + ").append(tr.get(i));
                    }
                    return sb.toString();
                }
            };
        }
    }

    @Override
    public String getSecret(Session session) {
        SecretService impl = this.impl;
        return null == impl ? null : impl.getSecret(session);
    }

    @Override
    public String toString() {
        SecretService impl = this.impl;
        return null == impl ? "<not-initialized>" : impl.toString();
    }

}
