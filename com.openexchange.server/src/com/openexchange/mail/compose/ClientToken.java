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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.mail.compose;

import static com.openexchange.java.Strings.isEmpty;
import java.util.Objects;
import org.apache.commons.lang.RandomStringUtils;
import com.openexchange.annotation.NonNull;
import com.openexchange.java.Strings;

/**
 * {@link ClientToken} - Represents a client token optionally passed from calling client used to detect possible modification attempts on
 * either out-dated or newer composition spaces.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.5
 */
public class ClientToken {

    /**
     * The constant for absent client token.
     */
    public static final @NonNull ClientToken NONE = new ClientToken(null);

    private static final int TOKEN_LENGTH = 16;

    /**
     * Creates the client token using given string representation
     *
     * @param token The token string
     * @return The token instance
     * @throws IllegalArgumentException If given token is syntactically wrong
     */
    public static @NonNull ClientToken of(String token) {
        if (isEmpty(token)) {
            return ClientToken.NONE;
        }

        int length = token.length();
        if (TOKEN_LENGTH != length) {
            throw new IllegalArgumentException("Given token has invalid length: " + length);
        }
        for (int i = length; i-- > 0;) {
            if (Strings.isAsciiLetterOrDigit(token.charAt(i)) == false) {
                throw new IllegalArgumentException("Given token contains invalid character: `" + token.charAt(i) + "\u00b4");
            }
        }
        return new ClientToken(token);
    }

    /** Simple class to delay initialization of ASCII-only alpha-numeric character array until needed */
    private static class CharsHolder {

        /** Character array consisting of ASCII-only alpha-numeric characters */
        static final char[] ALNUM_CHARS = {
            'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
            'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
            '0','1','2','3','4','5','6','7','8','9'
        };
    }

    /**
     * Generates a new, random client token
     *
     * @return The token
     */
    public static ClientToken generate() {
        return ClientToken.of(RandomStringUtils.random(TOKEN_LENGTH, CharsHolder.ALNUM_CHARS));
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final String token;
    private int hash;

    /**
     * Initializes a new {@link ClientToken}.
     *
     * @param token The token string
     */
    private ClientToken(String token) {
        super();
        this.token = token;
        hash = 0;
    }

    /**
     * Gets the token value.
     *
     * @return The token value
     */
    public String getToken() {
        return token;
    }

    @Override
    public int hashCode() {
        int result = hash;
        if (result == 0) {
            result = Objects.hashCode(token);
            hash = result;
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ClientToken other = (ClientToken) obj;
        if (token == null) {
            if (other.token != null) {
                return false;
            }
        } else if (!token.equals(other.token)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return token == null ? "null" : token;
    }

    /**
     * Checks if this instance represents the absent client token.
     *
     * @return <code>true</code> if absent; otherwise <code>false</code>
     */
    public boolean isAbsent() {
        return token == null;
    }

    /**
     * Checks if this instance represents a present client token.
     *
     * @return <code>true</code> if present; otherwise <code>false</code>
     */
    public boolean isPresent() {
        return token != null;
    }

    /**
     * Checks if given client token differs from the one of this instance.
     * <p>
     * <b>Note</b>: This method does not check if the client token of this instance is valid (e.g. not absent).
     *
     * @param sequenceId The client token to check against
     * @return <code>true</code> if <b>not</b> equal; otherwise <code>false</code> (for equal)
     */
    public boolean isNotEquals(ClientToken other) {
        return !equals(other);
    }

}
