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

import static com.openexchange.mail.mime.utils.MimeMessageUtility.decodeEnvelopeHeader;
import static com.openexchange.mail.mime.utils.MimeMessageUtility.unfold;
import java.io.Serializable;
import java.util.Iterator;
import java.util.regex.Pattern;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.DecoderException;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.utils.MimeMessageUtility;

/**
 * {@link ParameterizedHeader} - Super class for headers which can hold a parameter list such as <code>Content-Type</code>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class ParameterizedHeader implements Serializable, Comparable<ParameterizedHeader> {

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = -1094716342843794294L;

    protected ParameterList parameterList;

    /**
     * Initializes a new {@link ParameterizedHeader}
     */
    protected ParameterizedHeader() {
        super();
    }

    @Override
    public int compareTo(final ParameterizedHeader other) {
        if (this == other) {
            return 0;
        }
        if (parameterList == null) {
            if (other.parameterList != null) {
                return -1;
            }
            return 0;
        } else if (other.parameterList == null) {
            return 1;
        }
        return parameterList.compareTo(other.parameterList);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((parameterList == null) ? 0 : parameterList.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ParameterizedHeader)) {
            return false;
        }
        final ParameterizedHeader other = (ParameterizedHeader) obj;
        if (parameterList == null) {
            if (other.parameterList != null) {
                return false;
            }
        } else if (!parameterList.equals(other.parameterList)) {
            return false;
        }
        return true;
    }

    /**
     * Adds specified value to given parameter name. If existing, the parameter is treated as a contiguous parameter according to RFC2231.
     *
     * @param key The parameter name
     * @param value The parameter value to add
     */
    public void addParameter(final String key, final String value) {
        parameterList.addParameter(key, value);
    }

    /**
     * Sets the given parameter. Existing value is overwritten.
     *
     * @param key The parameter name
     * @param value The parameter value
     */
    public void setParameter(final String key, final String value) {
        parameterList.setParameter(key, value);
    }

    /**
     * Sets the given parameter. Existing value is overwritten.
     *
     * @param key The parameter name
     * @param value The parameter value
     * @throws OXException If parameter name/value is invalid
     */
    public void setParameterErrorAware(final String key, final String value) throws OXException {
        parameterList.setParameterErrorAware(key, value);
    }

    /**
     * Gets specified parameter's value
     *
     * @param key The parameter name
     * @return The parameter's value or <code>null</code> if not existing
     */
    public String getParameter(final String key) {
        final String value = parameterList.getParameter(key);
        return null == value ? null : MimeMessageUtility.decodeMultiEncodedHeader(value);
    }

    /**
     * Removes specified parameter and returns its value
     *
     * @param key The parameter name
     * @return The parameter's value or <code>null</code> if not existing
     */
    public String removeParameter(final String key) {
        final String value = parameterList.removeParameter(key);
        return null == value ? null : MimeMessageUtility.decodeMultiEncodedHeader(value);
    }

    /**
     * Checks if parameter is present
     *
     * @param key the parameter name
     * @return <code>true</code> if parameter is present; otherwise <code>false</code>
     */
    public boolean containsParameter(final String key) {
        return parameterList.containsParameter(key);
    }

    /**
     * Gets all parameter names wrapped in an {@link Iterator}
     *
     * @return All parameter names wrapped in an {@link Iterator}
     */
    public Iterator<String> getParameterNames() {
        return parameterList.getParameterNames();
    }

    /**
     * Clears all parameters contained in this parameterized header.
     */
    public void clearParameters() {
        parameterList.clearParameters();
    }

    //private static final Pattern PATTERN_CORRECT = Pattern.compile("\\s*=\\s*");

    /**
     * Prepares parameterized header's string representation:
     * <ol>
     * <li>Unfolds the header's string representation</li>
     * <li>Trims starting/ending whitespace characters</li>
     * <li>Removes ending ";" character</li>
     * </ol>
     *
     * @param paramHdrArg The parameterized header string argument
     * @return The prepared parameterized header's string.
     */
    protected static final String prepareParameterizedHeader(final String paramHdrArg) {
        if (paramHdrArg == null) {
            return paramHdrArg;
        }
        String paramHdr = unfold(paramHdrArg.trim());
        if (paramHdr.indexOf("=?") >= 0) {
            // Possibly mail-safe encoded
            paramHdr = decodeEnvelopeHeader(paramHdr).trim();
        }

        if (paramHdr.endsWith(";")) {
            paramHdr = paramHdr.substring(0, paramHdr.length() - 1).trim();
        }

        paramHdr = Strings.unparenthize(paramHdr);

        int pos = paramHdr.indexOf(';');
        if (pos > 0) {
            String value = paramHdr.substring(0, pos).trim();
            value = Strings.unparenthize(value);
            value = Strings.unquote(value);

            paramHdr = new StringBuilder(value).append(paramHdr.substring(pos)).toString();
        }
        return paramHdr;
    }

    private static final org.apache.commons.codec.net.URLCodec URL_CODEC = new org.apache.commons.codec.net.URLCodec(CharEncoding.ISO_8859_1);
    private static final Pattern P_ENC = Pattern.compile("%\\s([0-9a-fA-F]{2})");

    /**
     * URL decodes given string.
     * <p>
     * Using <code>org.apache.commons.codec.net.URLCodec</code>.
     */
    protected static String decodeUrl(final String s) {
        try {
            return com.openexchange.java.Strings.isEmpty(s) ? s : (URL_CODEC.decode(P_ENC.matcher(s).replaceAll("%$1")));
        } catch (final DecoderException e) {
            return s;
        }
    }
}
