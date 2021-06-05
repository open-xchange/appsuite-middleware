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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import com.openexchange.secret.SecretService;

/**
 * {@link TokenList}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TokenList {

    /**
     * Parses specified text to a token list.
     *
     * @param text The text to parse
     * @return The token list
     */
    public static TokenList parseText(final String text) {
        if (text == null) {
            return new TokenList(Collections.<List<Token>> emptyList());
        }
        return parsePatterns(text.split("\r?\n"));
    }

    /**
     * Parses specified patterns to a token list.
     *
     * @param patterns The patterns to parse
     * @return The token list
     */
    public static TokenList parsePatterns(final String[] patterns) {
        final List<List<Token>> ret = new ArrayList<List<Token>>(patterns.length);
        for (String pattern : patterns) {
            pattern = pattern.trim();
            if (0 == pattern.length() || '#' == pattern.charAt(0)) {
                continue;
            }
            if (pattern.charAt(0) == '"') {
                pattern = pattern.substring(1);
            }
            if (pattern.charAt(pattern.length() - 1) == '"') {
                pattern = pattern.substring(0, pattern.length() - 1);
            }

            final String[] tokens = pattern.split(" *\\+ *");
            final List<Token> tokenList = new ArrayList<Token>(tokens.length);
            for (String token : tokens) {
                token = token.trim();
                final boolean isReservedToken = ('<' == token.charAt(0));
                if (isReservedToken || ('\'' == token.charAt(0))) {
                    token = token.substring(1);
                    token = token.substring(0, token.length() - 1);
                }
                final ReservedToken rt = ReservedToken.reservedTokenFor(token);
                if (null == rt) {
                    if (isReservedToken) {
                        throw new IllegalStateException("Unknown reserved token: " + token);
                    }
                    tokenList.add(new LiteralToken(token));
                } else {
                    tokenList.add(rt);
                }
            }
            ret.add(tokenList);
        }
        return new TokenList(ret);
    }

    /**
     * Creates a new token list from specified collection.
     *
     * @param collection The collection
     * @return The new token list
     */
    public static TokenList newInstance(final Collection<List<Token>> collection) {
        return new TokenList(collection);
    }

    /*-
     * ------------------------------------- Member stuff ------------------------------------
     */

    private final List<SecretService> queue;
    private final boolean usesPassword;

    /**
     * Initializes a new {@link TokenList}.
     */
    private TokenList(Collection<List<Token>> collection) {
        super();
        queue = new ArrayList<SecretService>(collection.size());
        List<Token> last = null;
        for (List<Token> list : collection) {
            last = list;
            queue.add(new TokenBasedSecretService(new TokenRow(list)));
        }
        usesPassword = null == last ? false : last.contains(ReservedToken.PASSWORD);
    }

    /**
     * Checks if last entry uses password secret source.
     *
     * @return <code>true</code> if last entry uses password secret source; otherwise <code>false</code>
     */
    public boolean isUsesPassword() {
        return usesPassword;
    }

    @Override
    public String toString() {
        if (queue.isEmpty()) {
            return "<empty>";
        }
        final StringBuilder sb = new StringBuilder(128);
        final Iterator<SecretService> it = queue.iterator();
        sb.append(it.next().toString());
        while (it.hasNext()) {
            sb.append('\n').append(it.next().toString());
        }
        return sb.toString();
    }

    /**
     * Returns <tt>true</tt> if this token list contains no elements.
     *
     * @return <tt>true</tt> if this token list contains no elements
     */
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    /**
     * Retrieves, but does not remove, the first element of this token list, or returns <tt>null</tt> if this token list is empty.
     *
     * @return the head of this token list, or <tt>null</tt> if this token list is empty
     */
    public SecretService peekFirst() {
        return queue.get(0);
    }

    /**
     * Retrieves, but does not remove, the last element of this token list, or returns <tt>null</tt> if this token list is empty.
     *
     * @return the tail of this token list, or <tt>null</tt> if this token list is empty
     */
    public SecretService peekLast() {
        return queue.get(queue.size() - 1);
    }

    /**
     * Returns the number of elements in this token list.
     *
     * @return the number of elements in this token list
     */
    public int size() {
        return queue.size();
    }

    /**
     * Returns the element at the specified position in this token list.
     *
     * @param index The index of the element to return
     * @return The element at the specified position in this token list
     * @throws IndexOutOfBoundsException If the index is out of range (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    public SecretService get(final int index) {
        return queue.get(index);
    }

    /**
     * Returns an iterator over the elements in this token list in proper sequence. The elements will be returned in order from first (head)
     * to last (tail).
     *
     * @return an iterator over the elements in this token list in proper sequence
     */
    public Iterator<SecretService> iterator() {
        return queue.iterator();
    }

}
