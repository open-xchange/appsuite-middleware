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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.mail.mime;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.mail.MailException;

/**
 * {@link ContentType} - Parses value of MIME header <code>Content-Type</code>
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ContentType extends ParameterizedHeader {

    private static final long serialVersionUID = -9197784872892324694L;

    /**
     * The default content type: <code>text/plain; charset=us-ascii</code>
     */
    public static final ContentType DEFAULT_CONTENT_TYPE;

    static {
        DEFAULT_CONTENT_TYPE = new ContentType();
        DEFAULT_CONTENT_TYPE.setPrimaryType("text");
        DEFAULT_CONTENT_TYPE.setSubType("plain");
        DEFAULT_CONTENT_TYPE.setCharsetParameter("us-ascii");
    }

    /**
     * The regular expression that should match whole content type
     */
    private static final Pattern PATTERN_CONTENT_TYPE = Pattern.compile("(?:([\\p{ASCII}&&[^/;\\s\"]]+)(?:/([\\p{ASCII}&&[^;\\s\"]]+))?)");

    /**
     * The MIME type delimiter
     * 
     * @value /
     */
    private static final char DELIMITER = '/';

    private static final String DEFAULT_SUBTYPE = "OCTET-STREAM";

    private static final String PARAM_CHARSET = "charset";

    private String primaryType;

    private String subType;

    private String baseType;

    /**
     * Initializes a new {@link ContentType}
     */
    public ContentType() {
        super();
        parameterList = new ParameterList();
    }

    /**
     * Initializes a new {@link ContentType}
     * 
     * @param contentType The content type
     * @throws MailException If content type cannot be parsed
     */
    public ContentType(final String contentType) throws MailException {
        super();
        parseContentType(contentType);
    }

    @Override
    public int compareTo(final ParameterizedHeader other) {
        if (this == other) {
            return 0;
        }
        if (ContentType.class.isInstance(other)) {
            final int baseComp = getBaseType().compareToIgnoreCase(((ContentType) other).getBaseType());
            if (baseComp != 0) {
                return baseComp;
            }
        }
        return super.compareTo(other);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((primaryType == null) ? 0 : primaryType.toLowerCase(Locale.ENGLISH).hashCode());
        result = prime * result + ((subType == null) ? 0 : subType.toLowerCase(Locale.ENGLISH).hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ContentType other = (ContentType) obj;
        if (primaryType == null) {
            if (other.primaryType != null) {
                return false;
            }
        } else if (!primaryType.equalsIgnoreCase(other.primaryType)) {
            return false;
        }
        if (subType == null) {
            if (other.subType != null) {
                return false;
            }
        } else if (!subType.equalsIgnoreCase(other.subType)) {
            return false;
        }
        return true;
    }

    private void parseContentType(final String contentType) throws MailException {
        parseContentType(contentType, true);
    }

    private void parseContentType(final String contentTypeArg, final boolean paramList) throws MailException {
        if ((null == contentTypeArg) || (contentTypeArg.length() == 0)) {
            setContentType(DEFAULT_CONTENT_TYPE);
            return;
        }
        final String contentType = prepareParameterizedHeader(contentTypeArg);
        final Matcher ctMatcher = PATTERN_CONTENT_TYPE.matcher(contentType);
        if (!ctMatcher.find() || (ctMatcher.start() != 0)) {
            throw new MailException(MailException.Code.INVALID_CONTENT_TYPE, contentTypeArg);
        }
        primaryType = ctMatcher.group(1);
        subType = ctMatcher.group(2);
        if ((subType == null) || (subType.length() == 0)) {
            subType = DEFAULT_SUBTYPE;
        }
        baseType = new StringBuilder(16).append(primaryType).append(DELIMITER).append(subType).toString();
        if (paramList) {
            parameterList = new ParameterList(contentType.substring(ctMatcher.end()));
        }
    }

    private void parseBaseType(final String baseType) throws MailException {
        parseContentType(baseType, false);
        if (parameterList == null) {
            parameterList = new ParameterList();
        }
    }

    /**
     * Applies given content type to this content type
     * 
     * @param contentType The content type to apply
     */
    public void setContentType(final ContentType contentType) {
        if (contentType == this) {
            return;
        }
        primaryType = contentType.getPrimaryType();
        subType = contentType.getSubType();
        parameterList = (ParameterList) contentType.parameterList.clone();
    }

    /**
     * @return primary type
     */
    public String getPrimaryType() {
        return primaryType;
    }

    /**
     * Sets primary type
     */
    public void setPrimaryType(final String primaryType) {
        this.primaryType = primaryType;
        baseType = null;
    }

    /**
     * @return sub-type
     */
    public String getSubType() {
        return subType;
    }

    /**
     * Sets sub-type
     */
    public void setSubType(final String subType) {
        this.subType = subType;
        baseType = null;
    }

    /**
     * @return base type (e.g. text/plain)
     */
    public String getBaseType() {
        if (baseType != null) {
            return baseType;
        }
        return (baseType = new StringBuilder(16).append(primaryType).append(DELIMITER).append(subType).toString());
    }

    /**
     * Sets base type (e.g. text/plain)
     */
    public void setBaseType(final String baseType) throws MailException {
        parseBaseType(baseType);
    }

    /**
     * Sets charset parameter
     */
    public void setCharsetParameter(final String charset) {
        setParameter(PARAM_CHARSET, charset);
    }

    /**
     * @return the charset value or <code>null</code> if not present
     */
    public String getCharsetParameter() {
        return getParameter(PARAM_CHARSET);
    }

    /**
     * @return <code>true</code> if charset parameter is present, <code>false</code> otherwise
     */
    public boolean containsCharsetParameter() {
        return containsParameter(PARAM_CHARSET);
    }

    /**
     * Sets Content-Type
     */
    public void setContentType(final String contentType) throws MailException {
        parseContentType(contentType);
    }

    /**
     * Checks if Content-Type's base type matches given wildcard pattern (e.g text/plain, text/* or text/htm*)
     * 
     * @return <code>true</code> if Content-Type's base type matches given pattern, <code>false</code> otherwise
     */
    public boolean isMimeType(final String pattern) {
        final String baseType = toLowerCase(getBaseType());
        if (pattern.indexOf('?') == -1) {
            /*
             * No single wildcard
             */
            final int index = pattern.indexOf('*');
            if (index == -1) {
                return toLowerCase(pattern).equals(baseType);
            }
            final int len = pattern.length();
            if (index == (len - 1)) {
                /*-
                 * A wildcard path; e.g. text/*
                 */
                final String pat = len == 1 ? "" : toLowerCase(pattern).substring(0, index);
                /*
                 * Make sure base type is longer or equal length
                 */
                return (baseType.length() >= pat.length()) && baseType.startsWith(pat);
            }
        }
        /*
         * Create appropriate regex-pattern
         */
        final Pattern p = Pattern.compile(pattern.replaceAll("\\*", ".*").replaceAll("\\?", ".?"), Pattern.CASE_INSENSITIVE);
        return p.matcher(getBaseType()).matches();
    }

    /**
     * Checks if given MIME type's base type matches given wildcard pattern (e.g text/plain, text/* or text/htm*)
     * 
     * @param mimeType The MIME type
     * @param pattern The pattern
     * @return <code>true</code> if pattern matches; otherwise <code>false</code>
     * @throws MailException If an invalid MIME type is detected
     */
    public static boolean isMimeType(final String mimeType, final String pattern) throws MailException {
        final String baseType = toLowerCase(getBaseType(mimeType));
        if (pattern.indexOf('?') == -1) {
            /*
             * No single wildcard
             */
            final int index = pattern.indexOf('*');
            if (index == -1) {
                return toLowerCase(pattern).equals(baseType);
            }
            final int len = pattern.length();
            if (index == (len - 1)) {
                /*-
                 * A wildcard path; e.g. text/*
                 */
                final String pat = len == 1 ? "" : toLowerCase(pattern).substring(0, index);
                /*
                 * Make sure base type is longer or equal length
                 */
                return (baseType.length() >= pat.length()) && baseType.startsWith(pat);
            }
        }
        /*
         * Create appropriate regex-pattern
         */
        final Pattern p = Pattern.compile(pattern.replaceAll("\\*", ".*").replaceAll("\\?", ".?"), Pattern.CASE_INSENSITIVE);
        return p.matcher(baseType).matches();
    }

    /**
     * Detects the base type of given MIME type
     * 
     * @param mimeType The MIME type
     * @return the base type
     * @throws MailException If an invalid MIME type is detected
     */
    public static String getBaseType(final String mimeType) throws MailException {
        final Matcher m = PATTERN_CONTENT_TYPE.matcher(mimeType);
        if (m.find()) {
            String subType = m.group(2);
            if ((subType == null) || (subType.length() == 0)) {
                subType = DEFAULT_SUBTYPE;
            }
            return new StringBuilder(32).append(m.group(1)).append('/').append(subType).toString();
        }
        throw new MailException(MailException.Code.INVALID_CONTENT_TYPE, mimeType);
    }

    private static final String toLowerCase(final String str) {
        final char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            chars[i] = Character.toLowerCase(chars[i]);
        }
        return new String(chars);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(64);
        sb.append(primaryType).append(DELIMITER).append(subType);
        if (null != parameterList) {
            parameterList.appendUnicodeString(sb);
        }
        return sb.toString();
    }

}
