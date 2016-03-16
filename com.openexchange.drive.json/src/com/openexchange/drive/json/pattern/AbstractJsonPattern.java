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

package com.openexchange.drive.json.pattern;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.exception.OXException;

/**
 * {@link AbstractJsonPattern}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class AbstractJsonPattern {

    protected final PatternType type;
    protected final boolean caseSensitive;

    /**
     * Initializes a new {@link AbstractJsonPattern}.
     *
     * @param type The pattern type
     * @param caseSensitive <code>true</code> to match the patterns in a case sensitive way, <code>false</code>, otherwise
     */
    protected AbstractJsonPattern(PatternType type, boolean caseSensitive) {
        super();
        this.type = type;
        this.caseSensitive = caseSensitive;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (caseSensitive ? 1231 : 1237);
        result = prime * result + ((type == null) ? 0 : type.ordinal());
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
        if (!(obj instanceof AbstractJsonPattern)) {
            return false;
        }
        AbstractJsonPattern other = (AbstractJsonPattern) obj;
        if (caseSensitive != other.caseSensitive) {
            return false;
        }
        if (type != other.type) {
            return false;
        }
        return true;
    }

    /**
     * Creates a ready-to-use {@link Pattern} for the supplied parameters.
     *
     * @param type The pattern type
     * @param pattern The pattern
     * @param caseSensitive <code>true</code> to match the patterns in a case sensitive way, <code>false</code>, otherwise
     * @return The pattern
     */
    protected static Pattern createPattern(PatternType type, String pattern, boolean caseSensitive) throws OXException {
        int flags = Pattern.CANON_EQ | Pattern.UNICODE_CASE;
        if (false == caseSensitive) {
            flags |= Pattern.CASE_INSENSITIVE;
        }
        try {
            switch (type) {
            case EXACT:
                return Pattern.compile(pattern, flags | Pattern.LITERAL);
            case GLOB:
                return Pattern.compile(regexFromGlob(pattern), flags);
            case REGEX:
                return Pattern.compile(pattern, flags);
            default:
                throw new UnsupportedOperationException("Unknown pattern type: " + type);
            }
        } catch (PatternSyntaxException e) {
            throw DriveExceptionCodes.INVALID_PATTERN.create(e, pattern, e.getMessage());
        }
    }

    /**
     * Creates a regular expression string from the supplied glob-style wildcard string.
     *
     * @param glob The glob-style wildcard string to create the regex for
     * @return The regex string
     */
    private static String regexFromGlob(String glob) {
        StringBuilder stringBuilder = new StringBuilder(glob.length() + 10);
        /*
         * assert position at the beginning of the string
         */
        stringBuilder.append('^');
        for (int i = 0; i < glob.length(); i++) {
            char c = glob.charAt(i);
            switch (c) {
            case '\\':
                stringBuilder.append('\\');
                if (i + 1 < glob.length() && ('*' == glob.charAt(i + 1) || '?' == glob.charAt(i + 1))) {
                    /*
                     * match escaped reserved glob character literally
                     */
                    stringBuilder.append(glob.charAt(i + 1));
                    i++;
                } else {
                    /*
                     * match backslash character literally
                     */
                    stringBuilder.append('\\');
                }
                break;
            case '*':
                /*
                 * match any character, as many times as possible
                 */
                stringBuilder.append(".*");
                break;
            case '?':
                /*
                 * match a single character
                 */
                stringBuilder.append('.');
                break;
            case '^':
            case '$':
            case '.':
            case '|':
            case '+':
            case '(':
            case ')':
            case '{':
            case '}':
            case '[':
            case ']':
                /*
                 * match escaped reserved regex character literally
                 */
                stringBuilder.append('\\').append(c);
                break;
            default:
                /*
                 * match any other character literally
                 */
                stringBuilder.append(c);
                break;
            }
        }
        /*
         * assert position at the end of the string
         */
        return stringBuilder.append('$').toString();
    }

}
