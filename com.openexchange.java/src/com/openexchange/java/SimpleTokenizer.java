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

package com.openexchange.java;

import java.util.LinkedList;
import java.util.List;


/**
 * A tokenizer that splits user input on white spaces and preserves phrases.<br>
 *
 * Example:<br>
 * These are "three tokens" => ["These", "are", "three tokens"]
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class SimpleTokenizer {

    /**
     * Tokenizes a list of strings.
     *
     * @param queries The list of queries to tokenize
     * @return A list of tokens; never <code>null</code> but possibly empty,
     * if none of the strings did contain any valid token (e.g. empty strings or only white spaces).
     */
    public static List<String> tokenize(List<String> queries) {
        List<String> tokens = new LinkedList<String>();
        for (String query : queries) {
            tokenize(tokens, query, 1);
        }
        return tokens;
    }

    /**
     * Tokenizes the given input.
     *
     * @param query The query to tokenize
     * @return A list of tokens; never <code>null</code> but possibly empty,
     * if the string did not contain any valid token (e.g. empty string or only white spaces).
     */
    public static List<String> tokenize(String query) {
        List<String> tokens = new LinkedList<String>();
        tokenize(tokens, query, 1);
        return tokens;
    }

    /**
     * Tokenizes the given input and keeps only tokens which are longer than <code>minTokenLen</code> characters.
     *
     * @param query The query to tokenize
     * @param minTokenLen The minimum number of characters every token must have
     * @return A list of tokens; never <code>null</code> but possibly empty,
     * if the string did not contain any valid token (e.g. empty string or only white spaces).
     */
    public static List<String> tokenize(String query, int minTokenLen) {
        List<String> tokens = new LinkedList<String>();
        tokenize(tokens, query, minTokenLen);
        return tokens;
    }

    private static void tokenize(List<String> tokens, String query, int minTokenLen) {
        int lastQuotePos = -1;
        boolean inQuotes = false;
        char[] chars = query.trim().toCharArray();
        int len = chars.length;
        if (len > 0) {
            StringBuilder tokenBuilder = new StringBuilder(query.length());
            for (int i = 0; i < len; i++) {
                char c = chars[i];
                if (c == '"') {
                    lastQuotePos = i;
                    if (inQuotes) {
                        inQuotes = false;
                    } else {
                        inQuotes = true;
                    }
                } else if (Character.isWhitespace(c)) {
                    if (inQuotes) {
                        tokenBuilder.append(' ');
                    } else {
                        String token = tokenBuilder.toString();
                        if (token.length() >= minTokenLen) {
                            tokens.add(tokenBuilder.toString());
                        }
                        tokenBuilder.setLength(0);
                    }
                } else {
                    tokenBuilder.append(c);
                }
            }

            if (inQuotes && lastQuotePos < (len - 1)) {
                tokens.addAll(tokenize(new String(chars, lastQuotePos + 1, chars.length - (lastQuotePos + 1))));
            } else {
                String token = tokenBuilder.toString();
                if (token.trim().length() >= minTokenLen) {
                    tokens.add(token);
                }
            }
        }
    }

}
