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

package com.openexchange.messaging.generic.internet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.ContentType;
import com.openexchange.messaging.generic.internal.ParameterizedHeader;

/**
 * {@link MimeContentType} - The MIME content type.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public final class MimeContentType extends ParameterizedHeader implements ContentType {

    private static final long serialVersionUID = 8048448895301469418L;

    private static final String CONTENT_TYPE = "Content-Type";

    /**
     * Gets the <i>Content-Type</i> name.
     *
     * @return The <i>Content-Type</i> name
     */
    public static String getContentTypeName() {
        return CONTENT_TYPE;
    }

    /**
     * The (unmodifiable) default content type: <code>text/plain; charset=us-ascii</code>
     */
    public static final MimeContentType DEFAULT_CONTENT_TYPE;

    static {
        DEFAULT_CONTENT_TYPE = new MimeContentType(com.openexchange.mail.mime.ContentType.DEFAULT_CONTENT_TYPE);
    }

    private final com.openexchange.mail.mime.ContentType cto;

    /**
     * Initializes a new {@link MimeContentType}
     */
    public MimeContentType() {
        super(new com.openexchange.mail.mime.ContentType());
        cto = (com.openexchange.mail.mime.ContentType) delegate;
    }

    /**
     * Initializes a new {@link MimeContentType}
     *
     * @param contentType The content type
     * @throws OXException If content type cannot be parsed
     */
    public MimeContentType(final String contentType) throws OXException {
        super(toContentType(contentType));
        cto = (com.openexchange.mail.mime.ContentType) delegate;
    }

    private MimeContentType(final com.openexchange.mail.mime.ContentType cto) {
        super(cto);
        this.cto = cto;
    }

    private static com.openexchange.mail.mime.ContentType toContentType(final String contentType) throws OXException {
        return new com.openexchange.mail.mime.ContentType(contentType);
    }

    @Override
    public int compareTo(final ParameterizedHeader other) {
        if (this == other) {
            return 0;
        }
        if (MimeContentType.class.isInstance(other)) {
            final int baseComp = getBaseType().compareToIgnoreCase(((MimeContentType) other).getBaseType());
            if (baseComp != 0) {
                return baseComp;
            }
        }
        return super.compareTo(other);
    }

    @Override
    public int hashCode() {
        return cto.hashCode();
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
        final MimeContentType other = (MimeContentType) obj;
        if (cto == null) {
            if (other.cto != null) {
                return false;
            }
        } else if (!cto.equals(other.cto)) {
            return false;
        }
        return true;
    }

    @Override
    public void setContentType(final ContentType contentType) {
        if (contentType == this) {
            return;
        }
        if (contentType instanceof MimeContentType) {
            cto.setContentType(((MimeContentType) contentType).cto);
        } else {
            cto.setPrimaryType(contentType.getPrimaryType());
            cto.setSubType(contentType.getSubType());
            {
                final List<String> tmp = new ArrayList<String>(4);
                for (final Iterator<String> it = cto.getParameterNames(); it.hasNext();) {
                    tmp.add(it.next());
                }
                for (final String name : tmp) {
                    cto.removeParameter(name);
                }
            }
            for (final Iterator<String> it = contentType.getParameterNames(); it.hasNext();) {
                final String name = it.next();
                cto.addParameter(name, contentType.getParameter(name));
            }
        }
    }

    @Override
    public String getName() {
        return CONTENT_TYPE;
    }

    @Override
    public HeaderType getHeaderType() {
        return HeaderType.PARAMETERIZED;
    }

    @Override
    public String getValue() {
        return toString();
    }

    /**
     * Applies given content type to this content type
     *
     * @param contentType The content type to apply
     */
    public void setContentType(final MimeContentType contentType) {
        if (contentType == this) {
            return;
        }
        cto.setContentType(contentType.cto);
    }

    /**
     * @return primary type
     */
    @Override
    public String getPrimaryType() {
        return cto.getPrimaryType();
    }

    /**
     * Sets primary type
     */
    @Override
    public void setPrimaryType(final String primaryType) {
        cto.setPrimaryType(primaryType);
    }

    /**
     * @return sub-type
     */
    @Override
    public String getSubType() {
        return cto.getSubType();
    }

    /**
     * Sets sub-type
     */
    @Override
    public void setSubType(final String subType) {
        cto.setSubType(subType);
    }

    /**
     * @return base type (e.g. text/plain)
     */
    @Override
    public String getBaseType() {
        return cto.getBaseType();
    }

    /**
     * Sets base type (e.g. text/plain)
     */
    @Override
    public void setBaseType(final String baseType) throws OXException {
        cto.setBaseType(baseType);
    }

    /**
     * Sets charset parameter
     */
    @Override
    public void setCharsetParameter(final String charset) {
        cto.setCharsetParameter(charset);
    }

    /**
     * @return the charset value or <code>null</code> if not present
     */
    @Override
    public String getCharsetParameter() {
        return cto.getCharsetParameter();
    }

    /**
     * @return <code>true</code> if charset parameter is present, <code>false</code> otherwise
     */
    @Override
    public boolean containsCharsetParameter() {
        return cto.containsCharsetParameter();
    }

    /**
     * Sets name parameter
     *
     * @param filename The name parameter
     */
    @Override
    public void setNameParameter(final String filename) {
        cto.setNameParameter(filename);
    }

    /**
     * @return the name value or <code>null</code> if not present
     */
    @Override
    public String getNameParameter() {
        return cto.getNameParameter();
    }

    /**
     * @return <code>true</code> if name parameter is present, <code>false</code> otherwise
     */
    @Override
    public boolean containsNameParameter() {
        return cto.containsNameParameter();
    }

    /**
     * Sets Content-Type
     */
    @Override
    public void setContentType(final String contentType) throws OXException {
        cto.setContentType(contentType);
    }

    /**
     * Checks if Content-Type's base type matches given wildcard pattern (e.g text/plain, text/* or text/htm*)
     *
     * @return <code>true</code> if Content-Type's base type matches given pattern, <code>false</code> otherwise
     */
    @Override
    public boolean isMimeType(final String pattern) {
        return cto.isMimeType(pattern);
    }

    /**
     * Checks if Content-Type's base type starts ignore-case with specified prefix.
     *
     * @param prefix The prefix
     * @return <code>true</code> if Content-Type's base type starts ignore-case with specified prefix; otherwise <code>false</code>
     * @throws IllegalArgumentException If specified prefix is <code>null</code>
     */
    @Override
    public boolean startsWith(final String prefix) {
        return cto.startsWith(prefix);
    }

    /**
     * Parses and prepares specified content-type string for being inserted into a MIME part's headers.
     *
     * @param contentType The content-type string to process
     * @return Prepared content-type string ready for being inserted into a MIME part's headers.
     * @throws OXException If parsing content-type string fails
     */
    public static String prepareContentTypeString(final String contentType) throws OXException {
        return com.openexchange.mail.mime.ContentType.prepareContentTypeString(contentType);
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
        return com.openexchange.mail.mime.ContentType.prepareContentTypeString(contentType, name);
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
        return com.openexchange.mail.mime.ContentType.isMimeType(mimeType, pattern);
    }

    /**
     * Detects the base type of given MIME type
     *
     * @param mimeType The MIME type
     * @return the base type
     * @throws OXException If an invalid MIME type is detected
     */
    public static String getBaseType(final String mimeType) throws OXException {
        return com.openexchange.mail.mime.ContentType.getBaseType(mimeType);
    }

    @Override
    public String toString() {
        return cto.toString();
    }

    /**
     * Returns a RFC2045 style (ASCII-only) string representation of this content type.
     *
     * @param skipEmptyParams <code>true</code> to skip empty parameters; otherwise <code>false</code>
     * @return A RFC2045 style (ASCII-only) string representation of this content type
     */
    public String toString(final boolean skipEmptyParams) {
        return cto.toString(skipEmptyParams);
    }

}
