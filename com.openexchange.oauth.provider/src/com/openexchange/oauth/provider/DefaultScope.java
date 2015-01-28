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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.oauth.provider;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.java.StringAppender;
import com.openexchange.java.Strings;


/**
 * {@link DefaultScope}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class DefaultScope implements Scope {

    /*
     * From https://tools.ietf.org/html/rfc6749#section-3.3:
     *   scope       = scope-token *( SP scope-token )
     *   scope-token = 1*( %x21 / %x23-5B / %x5D-7E )
     */
    private static final Pattern PREFIXED_OAUTH_SCOPE = Pattern.compile("(r_|w_|rw_)([\\x21\\x23-\\x5b\\x5d-\\x7e]+)");

    /**
     * Parses the scope from specified string representation.
     *
     * @param scopeStr The scope's string representation
     * @return The parsed scope or <code>null</code> if string is {@link Strings#isEmpty(String) empty}
     */
    public static DefaultScope parseScope(String scopeStr) {
        if (Strings.isEmpty(scopeStr)) {
            return new DefaultScope();
        }

        return new DefaultScope(Strings.splitByWhitespaces(scopeStr));
    }

    // -----------------------------------------------------------------------------------------------------------------------------

    /** The scope set */
    private final Set<String> scopes;

    /**
     * Initializes a new {@link DefaultScope}.
     *
     * @param scopes The scopes
     */
    public DefaultScope(String... scopes) {
        super();
        Set<String> set = new HashSet<>();
        if (scopes != null) {
            for (String scope : scopes) {
                if (scope != null) {
                    set.add(scope);
                }
            }
        }
        this.scopes = set;
    }

    /**
     * Initializes a new {@link DefaultScope}.
     *
     * @param scopes The scope set
     */
    public DefaultScope(Set<String> scopes) {
        super();
        this.scopes = null == scopes ? Collections.<String> emptySet() : scopes;
    }

    @Override
    public boolean has(String requiredScope) {
        if (scopes.contains(requiredScope)) {
            return true;
        }

        // Given scope is not literally contained; check for others that might include it
        Matcher prefixedScopeMatcher = PREFIXED_OAUTH_SCOPE.matcher(requiredScope);
        if (prefixedScopeMatcher.matches()) {
            String prefix = prefixedScopeMatcher.group(1);
            String scope = prefixedScopeMatcher.group(2);
            switch (prefix) {
                case "r_":
                    // Do also check for "rw_" which includes "r"
                    return scopes.contains("rw_" + scope);
                case "w_":
                    // Do also check for "rw_" which includes "w"
                    return scopes.contains("rw_" + scope);
                case "rw_":
                    // Do also for separate "r" and "w"
                    return scopes.contains("r_" + scope) && scopes.contains("w_" + scope);
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((scopes == null) ? 0 : scopes.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DefaultScope)) {
            return false;
        }
        DefaultScope other = (DefaultScope) obj;
        if (scopes == null) {
            if (other.scopes != null) {
                return false;
            }
        } else if (!scopes.equals(other.scopes)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return scopeString();
    }

    @Override
    public String scopeString() {
        Set<String> scopes = this.scopes;
        if (null == scopes || scopes.isEmpty()) {
            return "";
        }

        StringAppender sa = new StringAppender(' ');
        for (String scope : scopes) {
            sa.append(scope);
        }
        return sa.toString();
    }

}
