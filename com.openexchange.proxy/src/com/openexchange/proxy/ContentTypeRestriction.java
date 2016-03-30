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

package com.openexchange.proxy;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * {@link ContentTypeRestriction} - A {@link Restriction} for <i>Content-Type</i> header.
 * <p>
 * It is allowed to specify certain values like <code>"text/plain"</code> or <code>"image/jpeg"</code>, but wild-card patterns are
 * supported, too: <code>"text/*"</code> or <code>"text/htm*"</code>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ContentTypeRestriction implements Restriction {

    /**
     * The Content-Type header name.
     */
    protected static final String CONTENT_TYPE = "Content-Type";

    /**
     * The set of allowed content type values/patterns.
     */
    protected final Set<String> contentTypes;

    /**
     * Initializes a new {@link ContentTypeRestriction}.
     *
     * @param contentTypes The allowed content types
     */
    public ContentTypeRestriction(final Set<String> contentTypes) {
        super();
        if (contentTypes == null || contentTypes.isEmpty()) {
            this.contentTypes = Collections.<String> emptySet();
        } else {
            this.contentTypes = new HashSet<String>(contentTypes.size());
            for (final String contentType : contentTypes) {
                this.contentTypes.add(contentType.toLowerCase(Locale.ENGLISH));
            }
        }
    }

    /**
     * Initializes a new {@link ContentTypeRestriction}.
     *
     * @param contentTypes The allowed content types
     */
    public ContentTypeRestriction(final String... contentTypes) {
        super();
        if (contentTypes == null || 0 == contentTypes.length) {
            this.contentTypes = Collections.<String> emptySet();
        } else {
            this.contentTypes = new HashSet<String>(contentTypes.length);
            for (final String contentType : contentTypes) {
                this.contentTypes.add(contentType.toLowerCase(Locale.ENGLISH));
            }
        }
    }

    @Override
    public boolean allow(final Response response) {
        final Header header = response.getResponseHeader(CONTENT_TYPE);
        final String lcValue;
        {
            final String value = header.getValue();
            if (null == value) {
                /*
                 * Content-Type header missing
                 */
                return false;
            }
            lcValue = value.toLowerCase(Locale.ENGLISH).trim();
        }
        for (final String allowedContentType : contentTypes) {
            if (containsWildcardChar(allowedContentType)) {
                if (Pattern.compile(wildcardToRegex(allowedContentType)).matcher(lcValue).matches()) {
                    return true;
                }
            } else if (lcValue.startsWith(allowedContentType)) {
                return true;
            }
        }
        /*
         * No match found
         */
        return false;
    }

    /**
     * Checks if specified string contains any of the wild-card characters <code>'*'</code> or <code>'?'</code>.
     *
     * @param toCheck The string to check
     * @return <code>true</code> if specified string contains any wild-card character; otherwise <code>false</code>
     */
    protected static boolean containsWildcardChar(final String toCheck) {
        return toCheck.indexOf('*') >= 0 || toCheck.indexOf('?') >= 0;
    }

    /**
     * Converts specified wildcard string to a regular expression
     *
     * @param wildcard The wildcard string to convert
     * @return An appropriate regular expression ready for being used in a {@link Pattern pattern}
     */
    protected static String wildcardToRegex(final String wildcard) {
        final StringBuilder s = new StringBuilder(wildcard.length());
        s.append('^');
        final int len = wildcard.length();
        for (int i = 0; i < len; i++) {
            final char c = wildcard.charAt(i);
            if (c == '*') {
                s.append(".*");
            } else if (c == '?') {
                s.append('.');
            } else if (c == '(' || c == ')' || c == '[' || c == ']' || c == '$' || c == '^' || c == '.' || c == '{' || c == '}' || c == '|' || c == '\\') {
                s.append('\\');
                s.append(c);
            } else {
                s.append(c);
            }
        }
        s.append('$');
        return (s.toString());
    }

    @Override
    public String getDescription() {
        return "Content-Type header must be equal or match one of: " + contentTypes.toString();
    }

    @Override
    public String toString() {
        return getDescription();
    }

}
