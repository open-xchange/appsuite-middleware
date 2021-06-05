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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * {@link TokenRow} - A token row; e.g. <i>"&lt;user-id&gt; + '-' +  &lt;random&gt; + '-' + &lt;context-id&gt;"</i>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class TokenRow implements Iterable<Token> {

    /** The default token row */
    private static final List<Token> DEFAULT_TOKEN_LIST = new CopyOnWriteArrayList<Token>(Arrays.<Token> asList(ReservedToken.USER_ID, new LiteralToken("@"), ReservedToken.CONTEXT_ID));

    /** The default token row */
    public static final TokenRow DEFAULT_TOKEN_ROW = new TokenRow(DEFAULT_TOKEN_LIST);

    // ------------------------------------------------------------------------------------------------------------------------------------

    private final List<Token> tokenList;

    /**
     * Initializes a new {@link TokenRow}.
     */
    public TokenRow(List<Token> tokenList) {
        super();
        this.tokenList = null == tokenList || tokenList.isEmpty() ? DEFAULT_TOKEN_LIST : new CopyOnWriteArrayList<Token>(tokenList);
    }

    /**
     * Gets the size.
     *
     * @return The size
     */
    public int size() {
        return tokenList.size();
    }

    /**
     * Checks if this row of tokens is empty.
     *
     * @return <code>true</code> if empty, otherwise <code>false</code>
     */
    public boolean isEmpty() {
        return tokenList.isEmpty();
    }

    /**
     * Gets an {@link Iterator} instance for the tokens contained in this token row
     *
     * @return The iterator
     */
    @Override
    public Iterator<Token> iterator() {
        return tokenList.iterator();
    }

    /**
     * Gets the token at the specified index position
     *
     * @param index The index
     * @return The token
     * @throws IndexOutOfBoundsException If the index is out of range (index < 0 || index >= size())
     */
    public Token get(int index) {
        return tokenList.get(index);
    }

}
