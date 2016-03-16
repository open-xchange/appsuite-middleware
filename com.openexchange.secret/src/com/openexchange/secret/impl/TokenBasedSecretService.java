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
