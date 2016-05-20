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

package com.openexchange.mail.mime;

import static com.openexchange.java.Strings.asciiLowerCase;
import static com.openexchange.java.Strings.toUpperCase;
import static com.openexchange.mail.mime.utils.MimeMessageUtility.decodeMultiEncodedHeader;
import java.util.Iterator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.Collections;

/**
 * {@link ContentType} - Parses value of MIME header <code>Content-Type</code>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ContentType extends ParameterizedHeader {

    private static final long serialVersionUID = -9197784872892324694L;

    private volatile static Boolean contentTypeRegexFallback;
    private static boolean contentTypeRegexFallback() {
        Boolean b = contentTypeRegexFallback;
        if (null == b) {
            synchronized (ContentType.class) {
                b = contentTypeRegexFallback;
                if (null == b) {
                    final ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
                    b = Boolean.valueOf(null == service || service.getBoolProperty("com.openexchange.mail.mime.contentTypeRegexFallback", true));
                    contentTypeRegexFallback = b;
                }
            }
        }
        return b.booleanValue();
    }

    /**
     * {@link UnmodifiableContentType} - An unmodifiable content type.
     *
     * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
     * @since Open-Xchange v6.16
     */
    public static final class UnmodifiableContentType extends ContentType {

        private static final long serialVersionUID = 2473639344400699522L;

        private final ContentType contentType;

        /**
         * Initializes a new {@link UnmodifiableContentType}.
         *
         * @param contentType The backing content type
         */
        public UnmodifiableContentType(final ContentType contentType) {
            super();
            this.contentType = contentType;
        }

        @Override
        public void addParameter(final String key, final String value) {
            throw new UnsupportedOperationException("ContentType.UnmodifiableContentType.addParameter()");
        }

        @Override
        public int compareTo(final ParameterizedHeader other) {
            return contentType.compareTo(other);
        }

        @Override
        public boolean containsCharsetParameter() {
            return contentType.containsCharsetParameter();
        }

        @Override
        public boolean containsNameParameter() {
            return contentType.containsNameParameter();
        }

        @Override
        public boolean containsParameter(final String key) {
            return contentType.containsParameter(key);
        }

        @Override
        public boolean equals(final Object obj) {
            return contentType.equals(obj);
        }

        @Override
        public String getBaseType() {
            return contentType.getBaseType();
        }

        @Override
        public String getCharsetParameter() {
            return contentType.getCharsetParameter();
        }

        @Override
        public String getNameParameter() {
            return contentType.getNameParameter();
        }

        @Override
        public String getParameter(final String key) {
            return contentType.getParameter(key);
        }

        @Override
        public Iterator<String> getParameterNames() {
            return Collections.unmodifiableIterator(contentType.getParameterNames());
        }

        @Override
        public String getPrimaryType() {
            return contentType.getPrimaryType();
        }

        @Override
        public String getSubType() {
            return contentType.getSubType();
        }

        @Override
        public int hashCode() {
            return contentType.hashCode();
        }

        @Override
        public boolean isMimeType(final String pattern) {
            return contentType.isMimeType(pattern);
        }

        @Override
        public String removeParameter(final String key) {
            throw new UnsupportedOperationException("ContentType.UnmodifiableContentType.removeParameter()");
        }

        @Override
        public ContentType setBaseType(final String baseType) throws OXException {
            throw new UnsupportedOperationException("ContentType.UnmodifiableContentType.setCharsetParameter()");
        }

        @Override
        public ContentType setCharsetParameter(final String charset) {
            throw new UnsupportedOperationException("ContentType.UnmodifiableContentType.setCharsetParameter()");
        }

        @Override
        public void setContentType(final ContentType contentType) {
            throw new UnsupportedOperationException("ContentType.UnmodifiableContentType.setContentType()");
        }

        @Override
        public void setContentType(final String contentType) throws OXException {
            throw new UnsupportedOperationException("ContentType.UnmodifiableContentType.setContentType()");
        }

        @Override
        public ContentType setNameParameter(final String filename) {
            throw new UnsupportedOperationException("ContentType.UnmodifiableContentType.setNameParameter()");
        }

        @Override
        public void setParameter(final String key, final String value) {
            throw new UnsupportedOperationException("ContentType.UnmodifiableContentType.setParameter()");
        }

        @Override
        public ContentType setPrimaryType(final String primaryType) {
            throw new UnsupportedOperationException("ContentType.UnmodifiableContentType.setPrimaryType()");
        }

        @Override
        public ContentType setSubType(final String subType) {
            throw new UnsupportedOperationException("ContentType.UnmodifiableContentType.setSubType()");
        }

        @Override
        public boolean startsWith(final String prefix) {
            return contentType.startsWith(prefix);
        }

        @Override
        public String toString() {
            return contentType.toString();
        }

        @Override
        public String toString(final boolean skipEmptyParams) {
            return contentType.toString(skipEmptyParams);
        }

    } // End of UnmodifiableContentType

    /**
     * The (unmodifiable) default content type: <code>text/plain; charset=us-ascii</code>
     */
    public static final ContentType DEFAULT_CONTENT_TYPE;

    static {
        final ContentType tmp = new ContentType();
        tmp.setPrimaryType("text");
        tmp.setSubType("plain");
        tmp.setCharsetParameter("us-ascii");
        DEFAULT_CONTENT_TYPE = new UnmodifiableContentType(tmp);
    }

    /**
     * The regular expression that should match whole content type
     */
    // private static final Pattern PATTERN_CONTENT_TYPE = Pattern.compile("(?:\"?([[\\p{L}\\p{ASCII}]&&[^/;\\s\"]]+)(?:/([[\\p{L}\\p{ASCII}]&&[^;\\s\"]]+))?\"?)|(?:/([[\\p{L}\\p{ASCII}]&&[^;\\s\"]]+))");
    private static final Pattern PATTERN_CONTENT_TYPE = Pattern.compile("(?:\"?([\\p{L}_0-9-]+)(?:/([\\p{L}_0-9-]+))?\"?)|(?:/([\\p{L}_0-9-]+))");

    private static final Pattern PATTERN_TOKEN = Pattern.compile("[\\p{L}_0-9-.+*]*");

    private static boolean isInvalidToken(final String token) {
        if (null == token) {
            return true;
        }
        return !PATTERN_TOKEN.matcher(token).matches();
    }

    /**
     * The regular expression capturing whitespace characters.
     */
    private static final Pattern PATTERN_WHITESPACE = Pattern.compile("\\s+");

    private static final String EMPTY = "";

    private static String clearWhitespaces(final String str) {
        if (null == str) {
            return null;
        }
        return PATTERN_WHITESPACE.matcher(str).replaceAll(EMPTY);
    }

    /**
     * The MIME type delimiter
     *
     * @value /
     */
    private static final char DELIMITER = '/';

    private static final String DEFAULT_PRIMTYPE = "APPLICATION";

    private static final String DEFAULT_SUBTYPE = "OCTET-STREAM";

    private static final String PARAM_CHARSET = "charset";

    private static final String PARAM_NAME = "name";

    private static final String PARAM_BOUNDARY = "boundary";

    private String primaryType;

    private String subType;

    private String baseType;

    private String lcBaseType;

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
     * @throws OXException If content type cannot be parsed
     */
    public ContentType(final String contentType) throws OXException {
        super();
        parseContentType(contentType);
    }

    /**
     * Resets this {@link ContentType} instance.
     */
    public void reset() {
        parameterList = new ParameterList();
        primaryType = null;
        subType = null;
        baseType = null;
        lcBaseType = null;
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

    private String getLowerCaseBaseType() {
        if (null == lcBaseType) {
            lcBaseType = toLowerCase(getBaseType());
        }
        return lcBaseType;
    }

    private void parseContentType(final String contentType) throws OXException {
        parseContentType(contentType, true);
    }

    private void parseContentType(final String contentType, final boolean paramList) throws OXException {
        if ((null == contentType) || (contentType.length() == 0)) {
            setContentType(DEFAULT_CONTENT_TYPE);
            return;
        }
        try {
            final String cts = prepareParameterizedHeader(contentType);
            int semicolonPos = cts.indexOf(';');
            int commaPos = -1;
            final String type = semicolonPos < 0 ? cts : cts.substring(0, semicolonPos);
            // Check for '/' character
            final int slashPos = type.indexOf(DELIMITER);
            if (slashPos >= 0) {
                try {
                    // Primary type
                    {
                        String pt = 0 == slashPos ? DEFAULT_PRIMTYPE : type.substring(0, slashPos).trim();
                        if (pt.indexOf('%') >= 0) {
                            // Possibly encoded
                            pt = decodeUrl(pt);
                        }
                        char fc;
                        while ((fc = pt.charAt(0)) == '"' || fc == '\'') {
                            pt = pt.substring(1);
                        }
                        if (Strings.toLowerCase(pt).startsWith("content-type:")) {
                            pt = pt.substring(13);
                            if ((fc = pt.charAt(0)) == '"' || fc == '\'') {
                                pt = pt.substring(1);
                            }
                        }
                        if (isInvalidToken(pt)) {
                            throw MailExceptionCode.INVALID_CONTENT_TYPE.create(contentType);
                        }
                        primaryType = pt.length() <= 0 ? DEFAULT_PRIMTYPE : pt;
                    }
                    // Subtype
                    {
                        String st = slashPos < type.length() ? type.substring(slashPos + 1).trim() : DEFAULT_SUBTYPE;
                        {
                            commaPos = st.indexOf(',');
                            st = commaPos > 0 ? st.substring(0, commaPos) : st;
                        }
                        if (st.indexOf('%') >= 0) {
                            // Possibly encoded
                            st = decodeUrl(st);
                        }
                        int mlen;
                        char lc;
                        while ((mlen = st.length() - 1) > 0 && ((lc = st.charAt(mlen)) == '"' || lc == '\'')) {
                            st = st.substring(0, mlen);
                        }
                        if (isInvalidToken(st)) {
                            throw MailExceptionCode.INVALID_CONTENT_TYPE.create(contentType);
                        }
                        if (st.trim().length() <= 0) {
                            if ("multipart".equals(primaryType)) {
                                subType = "mixed";
                            } else if ("text".equals(primaryType)) {
                                subType = "plain";
                            } else {
                                subType = DEFAULT_SUBTYPE;
                            }
                        } else {
                            subType = st;
                        }
                    }
                    baseType = new StringBuilder(16).append(primaryType).append(DELIMITER).append(subType).toString();
                    lcBaseType = toLowerCase(baseType);
                    if (paramList) {
                        if (semicolonPos < 0) {
                            if (commaPos < 0) {
                                parameterList = new ParameterList();
                            } else {
                                // Encountered a comma during sub-type parsing
                                try {
                                    commaPos = cts.indexOf(','); // Detect comma's real position
                                    parameterList = commaPos < cts.length() ? new ParameterList(cts.substring(commaPos + 1)) : new ParameterList();
                                } catch (final RuntimeException e) {
                                    throw MailExceptionCode.INVALID_CONTENT_TYPE.create(e, contentType);
                                }
                            }
                        } else {
                            try {
                                parameterList = semicolonPos < cts.length() ? new ParameterList(cts.substring(semicolonPos + 1)) : new ParameterList();
                            } catch (final RuntimeException e) {
                                throw MailExceptionCode.INVALID_CONTENT_TYPE.create(e, contentType);
                            }
                        }
                    }
                    return;
                } catch (final OXException e) {
                    if (!contentTypeRegexFallback()) {
                        throw e;
                    }
                    // Content-Type could not be parsed the simple way
                    final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ContentType.class);
                    logger.debug("", e);
                }
            }
            // Try with regex-based parsing
            final Matcher ctMatcher = PATTERN_CONTENT_TYPE.matcher(type);
            if (ctMatcher.find()) {
                if (ctMatcher.start() != 0) {
                    throw MailExceptionCode.INVALID_CONTENT_TYPE.create(contentType);
                }
                final String alt = ctMatcher.group(3);
                if (null != alt) {
                    primaryType = DEFAULT_PRIMTYPE;
                    final String decoded = clearWhitespaces(decodeMultiEncodedHeader(alt));
                    subType = null == decoded ? alt : decoded;
                    if ((subType == null) || (subType.length() == 0)) {
                        subType = DEFAULT_SUBTYPE;
                    }
                } else {
                    {
                        final String pt = ctMatcher.group(1);
                        final String decoded = clearWhitespaces(decodeMultiEncodedHeader(pt));
                        primaryType = null == decoded ? pt : decoded;
                        if ((primaryType == null) || (primaryType.length() == 0)) {
                            primaryType = DEFAULT_PRIMTYPE;
                        }
                    }
                    semicolonPos = primaryType.indexOf(DELIMITER);
                    if (semicolonPos >= 0) {
                        subType = primaryType.substring(semicolonPos + 1);
                        primaryType = primaryType.substring(0, semicolonPos);
                    } else {
                        {
                            final String st = ctMatcher.group(2);
                            final String decoded = clearWhitespaces(decodeMultiEncodedHeader(st));
                            subType = null == decoded ? st : decoded;
                        }
                        if ((subType == null) || (subType.length() == 0)) {
                            subType = DEFAULT_SUBTYPE;
                        }
                    }
                    if ((subType == null) || (subType.length() == 0)) {
                        subType = DEFAULT_SUBTYPE;
                    }
                }
                baseType = new StringBuilder(16).append(primaryType).append(DELIMITER).append(subType).toString();
                lcBaseType = null;
                if (paramList) {
                    parameterList = new ParameterList(cts.substring(ctMatcher.end()));
                }
            } else {
                primaryType = "text";
                subType = "plain";
                baseType = new StringBuilder(16).append(primaryType).append(DELIMITER).append(subType).toString();
                lcBaseType = null;
                if (paramList) {
                    parameterList = new ParameterList(semicolonPos < 0 ? cts : cts.substring(semicolonPos));
                    final String name = parameterList.getParameter("name");
                    if (null != name) {
                        final String byName = MimeType2ExtMap.getContentType(name);
                        if (null != byName) {
                            final int slash = byName.indexOf('/');
                            primaryType = byName.substring(0, slash);
                            subType = byName.substring(slash + 1);
                            if ((subType == null) || (subType.length() == 0)) {
                                subType = DEFAULT_SUBTYPE;
                            }
                            baseType = new StringBuilder(16).append(primaryType).append(DELIMITER).append(subType).toString();
                            lcBaseType = null;
                        }
                    }
                }
            }
        } catch (RuntimeException e) {
            throw MailExceptionCode.INVALID_CONTENT_TYPE.create(e, contentType);
        }
    }

    private void parseBaseType(final String baseType) throws OXException {
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
        baseType = new StringBuilder(16).append(primaryType).append(DELIMITER).append(subType).toString();
        lcBaseType = null;
    }

    /**
     * @return primary type
     */
    public String getPrimaryType() {
        return primaryType;
    }

    /**
     * Sets primary type
     *
     * @return This content type with new primary type applied
     */
    public ContentType setPrimaryType(final String primaryType) {
        this.primaryType = primaryType;
        baseType = null;
        lcBaseType = null;
        return this;
    }

    /**
     * @return sub-type
     */
    public String getSubType() {
        return subType;
    }

    /**
     * Sets sub-type
     *
     * @return This content type with new sub-type applied
     */
    public ContentType setSubType(final String subType) {
        this.subType = subType;
        baseType = null;
        lcBaseType = null;
        return this;
    }

    /**
     * Checks if this Content-Type has specified base type.
     *
     * @param baseType The base type; e.g. <code>"text/plain"</code>
     * @return <code>true</code> if equals given base type; otherwise <code>false</code>
     */
    public boolean isBaseType(final String baseType) {
        if (null == baseType) {
            throw new IllegalArgumentException("baseType is null");
        }
        return getLowerCaseBaseType().equals(toLowerCase(baseType));
    }

    /**
     * Checks if this Content-Type has specified base type.
     *
     * @param primaryType The primary type; e.g. <code>"text"</code>
     * @param subType The secondary type; e.g. <code>"plain"</code>
     * @return <code>true</code> if equals given base type; otherwise <code>false</code>
     */
    public boolean isBaseType(final String primaryType, final String subType) {
        if (null == this.primaryType) {
            if (null != primaryType) {
                return false;
            }
        } else if (!this.primaryType.equalsIgnoreCase(primaryType)) {
            return false;
        }
        if (null == this.subType) {
            if (null != subType) {
                return false;
            }
        } else if (!this.subType.equalsIgnoreCase(subType)) {
            return false;
        }
        return true;
    }

    /**
     * Checks if this Content-Type has specified base type.
     *
     * @param primaryType The primary type; e.g. <code>"text"</code>
     * @param subTypeExpression The expression for secondary type; e.g. <code>"*"</code>
     * @return <code>true</code> if matches given base type; otherwise <code>false</code>
     */
    public boolean matchesBaseType(final String primaryType, final String subTypeExpression) {
        if (null == this.primaryType) {
            if (null != primaryType) {
                return false;
            }
        } else if (!this.primaryType.equalsIgnoreCase(primaryType)) {
            return false;
        }
        if (null == this.subType) {
            return "*".equals(subTypeExpression);
        } else if (!Pattern.matches(wildcardToRegex(subTypeExpression), this.subType)) {
            return false;
        }
        return true;
    }

    /**
     * Gets this content type's base type without any parameters appended; e.g. <code>"text/plain"</code>.
     *
     * @return The base type
     */
    public String getBaseType() {
        if (baseType != null) {
            return baseType;
        }
        return (baseType = new StringBuilder(16).append(primaryType).append(DELIMITER).append(subType).toString());
    }

    /**
     * Sets base type (e.g. text/plain)
     *
     * @return This content type with new base type applied
     */
    public ContentType setBaseType(final String baseType) throws OXException {
        parseBaseType(baseType);
        return this;
    }

    /**
     * Sets <code>"charset"</code> parameter
     *
     * @param charset The charset parameter value; e.g. <code>"UTF-8"</code>
     * @return This content type with new <code>"charset"</code> parameter applied
     */
    public ContentType setCharsetParameter(final String charset) {
        setParameter(PARAM_CHARSET, charset);
        return this;
    }

    /**
     * @return the <code>"charset"</code> value or <code>null</code> if not present
     */
    public String getCharsetParameter() {
        return getParameter(PARAM_CHARSET);
    }

    /**
     * @return <code>true</code> if <code>"charset"</code> parameter is present, <code>false</code> otherwise
     */
    public boolean containsCharsetParameter() {
        return containsParameter(PARAM_CHARSET);
    }

    /**
     * @return The removed <code>"charset"</code> parameter value or <code>null</code> if there was none
     */
    public String removeCharsetParameter() {
        return removeParameter(PARAM_CHARSET);
    }

    /**
     * Sets <code>"name"</code> parameter
     *
     * @param filename The name parameter
     * @return This content type with new <code>"name"</code> parameter applied
     */
    public ContentType setNameParameter(final String filename) {
        setParameter(PARAM_NAME, filename);
        return this;
    }

    /**
     * @return the <code>"name"</code> value or <code>null</code> if not present
     */
    public String getNameParameter() {
        return getParameter(PARAM_NAME);
    }

    /**
     * @return <code>true</code> if <code>"name"</code> parameter is present, <code>false</code> otherwise
     */
    public boolean containsNameParameter() {
        return containsParameter(PARAM_NAME);
    }

    /**
     * @return The removed <code>"name"</code> parameter value or <code>null</code> if there was none
     */
    public String removeNameParameter() {
        return removeParameter(PARAM_NAME);
    }

    /**
     * @return the <code>"boundary"</code> value or <code>null</code> if not present
     */
    public String getBoundaryParameter() {
        return getParameter(PARAM_BOUNDARY);
    }

    /**
     * @return <code>true</code> if <code>"boundary"</code> parameter is present, <code>false</code> otherwise
     */
    public boolean containsBoundaryParameter() {
        return containsParameter(PARAM_BOUNDARY);
    }

    /**
     * Sets the content type to specified content type string; e.g. "text/plain; charset=US-ASCII"
     *
     * @param contentType The content type string
     * @throws OXException If specified content type string cannot be parsed
     */
    public void setContentType(final String contentType) throws OXException {
        parseContentType(contentType);
    }

    /**
     * Checks if Content-Type's base type matches given wildcard pattern (e.g text/plain, text/* or text/htm*)
     *
     * @return <code>true</code> if Content-Type's base type matches given pattern, <code>false</code> otherwise
     */
    public boolean isMimeType(final String pattern) {
        return Pattern.compile(wildcardToRegex(pattern), Pattern.CASE_INSENSITIVE).matcher(getBaseType()).matches();
    }

    /**
     * Checks if Content-Type's base type ignore-case contains specified string.
     *
     * @param string The string
     * @return <code>true</code> if Content-Type's base type ignore-case starts with specified prefix; otherwise <code>false</code>
     * @throws IllegalArgumentException If specified string is <code>null</code>
     */
    public boolean contains(final String string) {
        if (null == string) {
            throw new IllegalArgumentException("String is null");
        }
        return getLowerCaseBaseType().indexOf(Strings.asciiLowerCase(string)) >= 0;
    }

    /**
     * Checks if Content-Type's base type ignore-case contains specified strings.
     *
     * @param s The strings
     * @return <code>true</code> if Content-Type's base type ignore-case starts with specified prefix; otherwise <code>false</code>
     */
    public boolean containsAny(final String... strings) {
        if (null == strings) {
            return false;
        }
        final String lowerCase = getLowerCaseBaseType();
        for (final String string : strings) {
            if (null != string && lowerCase.indexOf(Strings.asciiLowerCase(string)) >= 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if Content-Type's base type ignore-case starts with specified prefix.
     *
     * @param prefix The prefix
     * @return <code>true</code> if Content-Type's base type ignore-case starts with specified prefix; otherwise <code>false</code>
     * @throws IllegalArgumentException If specified prefix is <code>null</code>
     */
    public boolean startsWith(final String prefix) {
        if (null == prefix) {
            throw new IllegalArgumentException("Prefix is null");
        }
        return getLowerCaseBaseType().startsWith(toLowerCase(prefix), 0);
    }

    /**
     * Checks if Content-Type's base type ignore-case starts with any of specified prefixes.
     *
     * @param prefixes The prefixes
     * @return <code>true</code> if Content-Type's base type ignore-case starts with any of specified prefixes; otherwise <code>false</code>
     */
    public boolean startsWithAny(final String... prefixes) {
        if (null == prefixes) {
            return false;
        }
        final String lowerCase = getLowerCaseBaseType();
        for (final String prefix : prefixes) {
            if (null != prefix && lowerCase.startsWith(toLowerCase(prefix), 0)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Parses and prepares specified content-type string for being inserted into a MIME part's headers.
     *
     * @param contentType The content-type string to process
     * @return Prepared content-type string ready for being inserted into a MIME part's headers.
     * @throws OXException If parsing content-type string fails
     */
    public static String prepareContentTypeString(final String contentType) throws OXException {
        return MimeMessageUtility.foldContentType(new ContentType(contentType).toString());
    }

    /**
     * Parses and prepares specified content-type string for being inserted into a MIME part's headers.
     *
     * @param contentType The content-type string to process
     * @param name The optional name parameter to set if no <tt>"name"</tt> parameter is present in specified content-type string; pass
     *            <code>null</code> to ignore
     * @return Prepared content-type string ready for being inserted into a MIME part's headers.
     * @throws OXException If parsing content-type string fails
     */
    public static String prepareContentTypeString(final String contentType, final String name) throws OXException {
        final ContentType ct = new ContentType(contentType);
        if (name != null && !ct.containsNameParameter()) {
            ct.setNameParameter(name);
        }
        return MimeMessageUtility.foldContentType(ct.toString());
    }

    /**
     * Checks if given MIME type's base type matches given wildcard pattern (e.g text/plain, text/* or text/htm*)
     *
     * @param mimeType The MIME type
     * @param pattern The pattern
     * @return <code>true</code> if pattern matches; otherwise <code>false</code>
     * @throws OXException If an invalid MIME type is detected
     */
    public static boolean isMimeType(final String mimeType, final String pattern) throws OXException {
        return Pattern.compile(wildcardToRegex(pattern), Pattern.CASE_INSENSITIVE).matcher(getBaseType(mimeType)).matches();
    }

    /**
     * Detects the base type of given MIME type
     *
     * @param mimeType The MIME type
     * @return the base type
     * @throws OXException If an invalid MIME type is detected
     */
    public static String getBaseType(final String mimeType) throws OXException {
        final Matcher m = PATTERN_CONTENT_TYPE.matcher(mimeType);
        if (m.find()) {
            String subType = m.group(2);
            if ((subType == null) || (subType.length() == 0)) {
                subType = DEFAULT_SUBTYPE;
            }
            return new StringBuilder(32).append(m.group(1)).append('/').append(subType).toString();
        }
        throw MailExceptionCode.INVALID_CONTENT_TYPE.create(mimeType);
    }

    private static String toLowerCase(final String s) {
        final int length = s.length();
        final StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(Character.toLowerCase(s.charAt(i)));
        }
        return sb.toString();
    }

    /**
     * Converts specified wild-card string to a regular expression
     *
     * @param wildcard The wild-card string to convert
     * @return An appropriate regular expression ready for being used in a {@link Pattern#compile(String) pattern}
     */
    private static String wildcardToRegex(final String wildcard) {
        if (null == wildcard) {
            // Accept all if null
            return "^.*$";
        }
        if (wildcard.indexOf('*') < 0 && wildcard.indexOf('?') < 0) {
            // Literal pattern
            return Pattern.quote(wildcard);
        }
        // Generate appropriate regex
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

    /**
     * Returns a RFC2045 style (ASCII-only) string representation of this content type.
     *
     * @return A RFC2045 style (ASCII-only) string representation of this content type
     */
    public String toLowerCaseString() {
        return toLowerCaseString(false);
    }

    /**
     * Returns a RFC2045 style (ASCII-only) string representation of this content type.
     *
     * @param skipEmptyParams <code>true</code> to skip empty parameters; otherwise <code>false</code>
     * @return A RFC2045 style (ASCII-only) string representation of this content type
     */
    public String toLowerCaseString(boolean skipEmptyParams) {
        StringBuilder sb = new StringBuilder(64);
        sb.append(asciiLowerCase(primaryType)).append(DELIMITER).append(asciiLowerCase(subType));
        if (null != parameterList) {
            parameterList.appendRFC2045String(sb, skipEmptyParams);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return toString(false);
    }

    /**
     * Returns a RFC2045 style (ASCII-only) string representation of this content type.
     *
     * @param skipEmptyParams <code>true</code> to skip empty parameters; otherwise <code>false</code>
     * @return A RFC2045 style (ASCII-only) string representation of this content type
     */
    public String toString(boolean skipEmptyParams) {
        return toString(skipEmptyParams, false);
    }

    /**
     * Returns a RFC2045 style (ASCII-only) string representation of this content type.
     *
     * @param skipEmptyParams <code>true</code> to skip empty parameters; otherwise <code>false</code>
     * @param upperCase <code>true</code> to write base type upper-case; otherwise <code>false</code>
     * @return A RFC2045 style (ASCII-only) string representation of this content type
     */
    public String toString(boolean skipEmptyParams, boolean upperCase) {
        StringBuilder sb = new StringBuilder(64);

        if (upperCase) {
            sb.append(toUpperCase(primaryType)).append(DELIMITER).append(toUpperCase(subType));
        } else {
            sb.append(primaryType).append(DELIMITER).append(subType);
        }

        if (null != parameterList) {
            parameterList.appendRFC2045String(sb, skipEmptyParams);
        }
        return sb.toString();
    }

}
