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
            }
            Matcher matcher = OAUTH_SCOPE.matcher(token);
            if (!matcher.matches()) {
                throw new IllegalArgumentException("Invalid token: " + token);
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
            }
            Matcher matcher = OAUTH_SCOPE.matcher(token);
            if (!matcher.matches()) {
                throw new IllegalArgumentException("Invalid token: " + token);
            }
            tokenSet.add(token);
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
