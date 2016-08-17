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

package com.openexchange.oauth.provider.resourceserver.scope;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.java.StringAppender;
import com.openexchange.java.Strings;
import com.openexchange.oauth.provider.authorizationserver.grant.Grant;

/**
 * The scope of an {@link Grant}. A scope consists of one or more <code>tokens</code>
 * that respectively map to a certain set of permissions/capabilities. A scopes string
 * representation is a whitespace-delimited list of its tokens. The according BNF is:
 * <pre>
 * scope = scope-token *( SP scope-token )
 * scope-token = 1*( %x21 / %x23-5B / %x5D-7E )
 * </pre>
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 * @see https://tools.ietf.org/html/rfc6749#section-3.3
 */
public class Scope implements Serializable {

    private static final long serialVersionUID = -544112321774479825L;

    /*
     * Allowed chars taken from https://tools.ietf.org/html/rfc6749#section-3.3:
     * scope = scope-token *( SP scope-token )
     * scope-token = 1*( %x21 / %x23-5B / %x5D-7E )
     */
    private static final Pattern OAUTH_SCOPE_STRING = Pattern.compile("([\\x21\\x23-\\x5b\\x5d-\\x7e]+)((\\s([\\x21\\x23-\\x5b\\x5d-\\x7e]+))?)+");

    private static final Pattern OAUTH_SCOPE = Pattern.compile("[\\x21\\x23-\\x5b\\x5d-\\x7e]+");

    private final Set<String> tokens;


    /**
     * Initializes a new {@link Scope}.
     *
     * @param tokens The scope tokens, must not be <code>null</code> or empty
     * @throws IllegalArgumentException If the set is <code>null</code>, empty or if any token is syntactically invalid
     */
    private Scope(LinkedHashSet<String> tokens) {
        super();
        this.tokens = tokens;
    }

    /**
     * Parses the scope from specified string representation.
     * A passed string must be valid. You probably want to check
     * it via {@link #isValidScopeString(String)} before.
     * Duplicate tokens will be removed.
     *
     * @param scopeStr The scopes string representation
     * @return The parsed scope
     * @throws IllegalArgumentException If the scope string is invalid
     */
    public static Scope parseScope(String scopeStr) {
        if (Strings.isEmpty(scopeStr)) {
            throw new IllegalArgumentException("Scope must consist of at least one token!");
        }

        String[] tokens = Strings.splitByWhitespaces(scopeStr);
        LinkedHashSet<String> tokenSet = new LinkedHashSet<>();
        for (String token : tokens) {
            tokenSet.add(token);
        }

        return new Scope(tokenSet);
    }

    /**
     * Constructs a new {@link Scope} out of the passed tokens.
     * The list must not be <code>null</code> or empty. It must
     * contain at least one token and every token must be valid
     * in terms of the BNF. Duplicate tokens will be removed.
     *
     * @param tokens The tokens
     * @return The scope
     * @throws IllegalArgumentException If the set is <code>null</code>, empty or if any token is syntactically invalid
     */
    public static Scope newInstance(List<String> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            throw new IllegalArgumentException("At least one valid token must be passed!");
        }

        for (String token : tokens) {
            if (token == null) {
                throw new IllegalArgumentException("A scope token must not be null!");
            } else {
                Matcher matcher = OAUTH_SCOPE.matcher(token);
                if (!matcher.matches()) {
                    throw new IllegalArgumentException("Invalid token: " + token);
                }
            }
        }

        return new Scope(new LinkedHashSet<>(tokens));
    }

    /**
     * Constructs a new {@link Scope} out of the passed tokens.
     * At least one valid token must be passed. Duplicate tokens will be removed.
     *
     * @param tokens The tokens
     * @return The scope
     * @throws IllegalArgumentException If the vararg is <code>null</code>, empty or if any token is syntactically invalid
     */
    public static Scope newInstance(String... tokens) {
        if (tokens == null || tokens.length == 0) {
            throw new IllegalArgumentException("At least one valid token must be passed!");
        }

        LinkedHashSet<String> tokenSet = new LinkedHashSet<>();
        for (String token : tokens) {
            if (token == null) {
                throw new IllegalArgumentException("A scope token must not be null!");
            } else {
                Matcher matcher = OAUTH_SCOPE.matcher(token);
                if (matcher.matches()) {
                    tokenSet.add(token);
                } else {
                    throw new IllegalArgumentException("Invalid token: " + token);
                }
            }
        }

        return new Scope(tokenSet);
    }

    /**
     * Checks if the given string is a valid scope in terms of its syntax.
     *
     * @param scope The scope to check
     * @return <code>true</code> if the scope is valid
     */
    public static boolean isValidScopeString(String scope) {
        return OAUTH_SCOPE_STRING.matcher(scope).matches();
    }

    /**
     * Gets all contained tokens.
     *
     * @return An immutable set of scopes
     */
    public Set<String> get() {
        return Collections.unmodifiableSet(tokens);
    }

    /**
     * Checks whether the given token is contained.
     *
     * @param token The token
     * @return <code>true</code> if the token is contained, <code>false</code> if not
     */
    public boolean has(String token) {
        return tokens.contains(token);
    }

    /**
     * Gets the number of contained tokens.
     *
     * @return The number of tokens
     */
    public int size() {
        return tokens.size();
    }

    /**
     * Gets the string representation of the scope as defined in RFC 6749.
     *
     * @return The scope string
     */
    @Override
    public String toString() {
        if (null == tokens || tokens.isEmpty()) {
            return "";
        }

        StringAppender sa = new StringAppender(' ');
        for (String scope : tokens) {
            sa.append(scope);
        }
        return sa.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((tokens == null) ? 0 : tokens.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Scope)) {
            return false;
        }
        Scope other = (Scope) obj;
        if (tokens == null) {
            if (other.tokens != null) {
                return false;
            }
        } else if (!tokens.equals(other.tokens)) {
            return false;
        }
        return true;
    }

}
