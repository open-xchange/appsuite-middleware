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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.messaging.twitter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.regex.Pattern;
import com.openexchange.messaging.ContentType;
import com.openexchange.messaging.MessagingException;

/**
 * {@link TwitterContentType}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TwitterContentType implements ContentType {

    private static final String PRIMARY = "text";

    private static final String SUB = "plain";

    private static final String BASE = "text/plain";

    private static final String FULL = "text/plain; charset=UTF-8";

    private static final TwitterContentType instance = new TwitterContentType();

    /**
     * Gets the instance.
     * 
     * @return The instance
     */
    public static TwitterContentType getInstance() {
        return instance;
    }

    /*-
     * ------------------------------------ MEMBER AREA ------------------------------------
     */

    /**
     * Initializes a new {@link TwitterContentType}.
     */
    private TwitterContentType() {
        super();
    }

    public boolean containsCharsetParameter() {
        return true;
    }

    public boolean containsNameParameter() {
        return false;
    }

    public String getBaseType() {
        return BASE;
    }

    public String getCharsetParameter() {
        return "UTF-8";
    }

    public String getNameParameter() {
        return null;
    }

    public String getPrimaryType() {
        return PRIMARY;
    }

    public String getSubType() {
        return SUB;
    }

    public boolean isMimeType(final String pattern) {
        return Pattern.compile(wildcardToRegex(pattern), Pattern.CASE_INSENSITIVE).matcher(getBaseType()).matches();
    }

    public void setBaseType(final String baseType) throws MessagingException {
        throw new UnsupportedOperationException("TwitterContentType.setBaseType()");
    }

    public void setCharsetParameter(final String charset) {
        throw new UnsupportedOperationException("TwitterContentType.setCharsetParameter()");
    }

    public void setContentType(final String contentType) throws MessagingException {
        throw new UnsupportedOperationException("TwitterContentType.setContentType()");
    }

    public void setContentType(final ContentType contentType) {
        throw new UnsupportedOperationException("TwitterContentType.setContentType()");
    }

    public void setNameParameter(final String filename) {
        throw new UnsupportedOperationException("TwitterContentType.setNameParameter()");
    }

    public void setPrimaryType(final String primaryType) {
        throw new UnsupportedOperationException("TwitterContentType.setPrimaryType()");
    }

    public void setSubType(final String subType) {
        throw new UnsupportedOperationException("TwitterContentType.setSubType()");
    }

    public boolean startsWith(final String prefix) {
        if (null == prefix) {
            throw new IllegalArgumentException("Prefix is null");
        }
        return toLowerCase(getBaseType()).startsWith(toLowerCase(prefix), 0);
    }

    public void addParameter(final String key, final String value) {
        throw new UnsupportedOperationException("TwitterContentType.addParameter()");
    }

    public boolean containsParameter(final String key) {
        return "charset".equalsIgnoreCase(key);
    }

    public String getParameter(final String key) {
        return "charset".equalsIgnoreCase(key) ? getCharsetParameter() : null;
    }

    public Iterator<String> getParameterNames() {
        return new ArrayList<String>(Arrays.asList("charset")).iterator();
    }

    public String removeParameter(final String key) {
        throw new UnsupportedOperationException("TwitterContentType.removeParameter()");
    }

    public void setParameter(final String key, final String value) {
        throw new UnsupportedOperationException("TwitterContentType.setParameter()");
    }

    public String getName() {
        return "Content-Type";
    }

    public String getValue() {
        return FULL;
    }

    private static final String toLowerCase(final String str) {
        final char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            chars[i] = Character.toLowerCase(chars[i]);
        }
        return new String(chars);
    }

    /**
     * Converts specified wildcard string to a regular expression
     * 
     * @param wildcard The wildcard string to convert
     * @return An appropriate regular expression ready for being used in a {@link Pattern pattern}
     */
    private static String wildcardToRegex(final String wildcard) {
        final StringBuilder s = new StringBuilder(wildcard.length());
        s.append('^');
        final int len = wildcard.length();
        for (int i = 0; i < len; i++) {
            final char c = wildcard.charAt(i);
            if (c == '*') {
                s.append(".*");
            } else if (c == '?') {
                s.append('.');
            } else {
                s.append(c);
            }
        }
        s.append('$');
        return (s.toString());
    }

}
