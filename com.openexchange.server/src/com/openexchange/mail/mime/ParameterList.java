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
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.internet.MimeUtility;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.mime.utils.MimeMessageUtility;

/**
 * {@link ParameterList} - Represents the parameter list of a parameterized header.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ParameterList implements Cloneable, Serializable, Comparable<ParameterList> {

    /** The logger constant */
    static final transient org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ParameterList.class);

    private static final long serialVersionUID = 1085330725813918879L;

    /**
     * The regular expression to parse parameters
     */
    private static final Pattern PATTERN_PARAM_LIST;

    /**
     * The regular expression to correct parameters:<br>
     * <tt>&gt;name=text with whitespace&lt;</tt> is corrected to <tt>&gt;name=&quot;text with whitespace&quot;&lt;</tt>
     */
    private static final Pattern PATTERN_PARAM_CORRECT;

    static {
        final String paramNameRegex = "([\\p{L}\\p{ASCII}&&[^=\"\\s;]]+)";
        final String tokenRegex = "(?:[^\"][\\S&&[^\\s,;:\\\\\"/\\[\\]?()<>@]]*)";
        final String quotedStringRegex = "(?:\"(?:(?:\\\\\\\")|[^\"])+?\")"; // Grab '\"' char sequence or any non-quote character
        PATTERN_PARAM_LIST = Pattern.compile("(?:\\s*;\\s*|\\s+)" + paramNameRegex + "(?: *= *(" + tokenRegex + '|' + quotedStringRegex + "))?");

        PATTERN_PARAM_CORRECT = Pattern.compile("(?:\\s*;\\s*|\\s+)" + paramNameRegex + "( *= *)([^\" ][^; \t]*[ \t][^;]*)($|;)");
    }

    private static final String CHARSET_UTF_8 = "utf-8";

    private Map<String, Parameter> parameters;

    /**
     * Initializes a new, empty parameter list.
     */
    public ParameterList() {
        super();
        parameters = new HashMap<String, Parameter>();
    }

    /**
     * Initializes a new parameter list from specified parameter list's string representation.
     *
     * @param parameterList The parameter list's string representation
     */
    public ParameterList(final String parameterList) {
        this();
        parseParameterList(correctParamList(parameterList.trim()));
    }

    /**
     * Corrects given parameter list string.
     * <ul>
     * <li>Ensures starting <code>';'</code> character</li>
     * <li>Turns any unquoted strings to quoted strings</li>
     * </ul>
     *
     * @param parameterList The parameter list's string representation to correct
     * @return The corrected parameter list's string representation.
     */
    private static String correctParamList(final String parameterList) {
        String toParse = parameterList;
        int len = toParse.length();
        if (len > 0 && ';' != toParse.charAt(0)) {
            toParse = new StringBuilder(len + 2).append("; ").append(toParse).toString();
        }
        toParse = PATTERN_PARAM_CORRECT.matcher(toParse).replaceAll("$1$2\"$3\"$4");
        len = toParse.length();
        if (len > 0 && ';' != toParse.charAt(0)) {
            toParse = new StringBuilder(len + 2).append("; ").append(toParse).toString();
        }
        return toParse;
    }

    @Override
    public int compareTo(final ParameterList other) {
        if (this == other) {
            return 0;
        }
        if (parameters == null) {
            if (other.parameters != null) {
                return -1;
            }
            return 0;
        } else if (other.parameters == null) {
            return 1;
        }
        return toString().compareToIgnoreCase(other.toString());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((parameters == null) ? 0 : toString().hashCode());
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
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ParameterList other = (ParameterList) obj;
        if (parameters == null) {
            if (other.parameters != null) {
                return false;
            }
        } else if (!toString().equalsIgnoreCase(other.toString())) {
            return false;
        }
        return true;
    }

    @Override
    public Object clone() {
        try {
            final ParameterList clone = (ParameterList) super.clone();
            final int size = parameters.size();
            clone.parameters = new HashMap<String, Parameter>(size);
            if (size > 0) {
                final Iterator<Map.Entry<String, Parameter>> iter = parameters.entrySet().iterator();
                for (int i = 0; i < size; i++) {
                    final Map.Entry<String, Parameter> e = iter.next();
                    clone.parameters.put(e.getKey(), (Parameter) e.getValue().clone());
                }
            }
            return clone;
        } catch (final CloneNotSupportedException e) {
            /*
             * Cannot occur since it's cloneable
             */
            throw new RuntimeException("Clone failed even though 'Cloneable' interface is implemented");
        }

    }

    private void parseParameterList(final String parameterList) {
        try {
            final Matcher m = PATTERN_PARAM_LIST.matcher(parameterList);
            while (m.find()) {
                parseParameter(Strings.toLowerCase(m.group(1)), m.group(2));
            }
        } catch (final StackOverflowError regexFailed) {
            /*
             * Regex failed for given parameter list. Perform very simple manual parsing.
             */
            int pos = parameterList.indexOf(';');
            while (pos >= 0) {
                final int delim = parameterList.indexOf('=', pos);
                if (delim < 0) {
                    final int pos2 = parameterList.indexOf(';', pos + 1);
                    final String name = pos2 < 0 ? parameterList.substring(pos + 1).trim() : parameterList.substring(pos + 1, pos2).trim();
                    parseParameter(asciiLowerCase(name), "");
                    pos = pos2;
                } else {
                    final String name = parameterList.substring(pos + 1, delim).trim();
                    pos = parameterList.indexOf(';', pos + 1);
                    final String value = pos < 0 ? parameterList.substring(delim + 1) : parameterList.substring(delim + 1, pos);
                    parseParameter(asciiLowerCase(name), value);
                }
            }
        }
    }

    private void parseParameter(final String name, final String value) {
        String val = Strings.isEmpty(value) ? "" : (value.charAt(0) == '"') && (value.charAt(value.length() - 1) == '"') ? unescape(value.substring(1, value.length() - 1)) : value;
        int pos = name.indexOf('*');
        if (pos < 0) {
            parameters.put(name, new Parameter(name, MimeMessageUtility.decodeMultiEncodedHeader(val)));
        } else {
            Parameter p = null;
            final String soleName = name.substring(0, pos);
            String procName = name;
            String rawValue;
            /*
             * Check if parameter is marked as encoded: name
             */
            if (procName.charAt(procName.length() - 1) == '*') { // encoded
                procName = procName.substring(0, procName.length() - 1);
                pos = procName.indexOf('*');
                p = parameters.get(soleName);
                if (null == p || !p.rfc2231) {
                    /*
                     * Expect utf-8'EN'%C2%A4%20txt
                     */
                    String[] encInfos = RFC2231Tools.parseRFC2231Value(val);
                    if (null == encInfos) {
                        return;
                    }
                    p = new Parameter(soleName, encInfos[0], encInfos[1]);
                    rawValue = encInfos[2];
                    parameters.put(soleName, p);
                } else {
                    /*
                     * Expect %C2%A4%20txt
                     */
                    rawValue = val;
                }
            } else { // non-encoded
                p = parameters.get(soleName);
                if (null == p) {
                    p = new Parameter(soleName, "UTF-8", "");
                    parameters.put(soleName, p);
                }
                rawValue = val;
            }
            /*
             * Check for parameter continuation: name1
             */
            if (pos == -1) {
                p.addContiguousEncodedValue(rawValue);
            } else {
                int num = -1;
                try {
                    num = Integer.parseInt(procName.substring(pos + 1));
                } catch (final NumberFormatException e) {
                    num = -1;
                }
                if (num != -1) {
                    p.setContiguousEncodedValue(num, rawValue);
                }
            }
        }
    }

    /**
     * Sets the given parameter. Existing value is overwritten.
     *
     * @param name The sole parameter name
     * @param value The parameter value
     */
    public void setParameter(final String name, final String value) {
        if ((null == name) || containsSpecial(name)) {
            LOG.warn("", MailExceptionCode.INVALID_PARAMETER.create(name));
            return;
        }
        parameters.put(asciiLowerCase(name), new Parameter(name, value));
    }

    /**
     * Sets the given parameter. Existing value is overwritten.
     *
     * @param name The sole parameter name
     * @param value The parameter value
     * @throws OXException If parameter name/value is invalid
     */
    public void setParameterErrorAware(final String name, final String value) throws OXException {
        if ((null == name) || containsSpecial(name)) {
            throw MailExceptionCode.INVALID_PARAMETER.create(name);
        }
        parameters.put(asciiLowerCase(name), new Parameter(name, value));
    }

    /**
     * Adds specified value to given parameter name. If existing, the parameter is treated as a contiguous parameter according to RFC2231.
     *
     * @param name The parameter name
     * @param value The parameter value to add
     */
    public void addParameter(final String name, final String value) {
        if ((null == name) || containsSpecial(name)) {
            final OXException me = MailExceptionCode.INVALID_PARAMETER.create(name);
            LOG.error("", me);
            return;
        }
        final String key = asciiLowerCase(name);
        final Parameter p = parameters.get(key);
        if (null == p) {
            parameters.put(key, new Parameter(name, value));
            return;
        }
        if (!p.rfc2231) {
            p.rfc2231 = true;
        }
        p.addContiguousEncodedValue(value);
    }

    /**
     * Gets specified parameter's value
     *
     * @param name The parameter name
     * @return The parameter's value or <code>null</code> if not existing
     */
    public String getParameter(final String name) {
        if (null == name) {
            return null;
        }
        final Parameter p = parameters.get(asciiLowerCase(name));
        if (null == p) {
            return null;
        }
        return p.getValue();
    }

    /**
     * Removes specified parameter and returns its value
     *
     * @param name The parameter name
     * @return The parameter's value or <code>null</code> if not existing
     */
    public String removeParameter(final String name) {
        if (null == name) {
            return null;
        }
        final Parameter p = parameters.remove(asciiLowerCase(name));
        if (null == p) {
            return null;
        }
        return p.getValue();
    }

    /**
     * Checks if parameter is present
     *
     * @param name the parameter name
     * @return <code>true</code> if parameter is present; otherwise <code>false</code>
     */
    public boolean containsParameter(final String name) {
        if (null == name) {
            return false;
        }
        final Parameter p = parameters.get(asciiLowerCase(name));
        if (null == p) {
            return false;
        }
        return true;
    }

    /**
     * Gets all parameter names wrapped in an {@link Iterator}
     *
     * @return All parameter names wrapped in an {@link Iterator}
     */
    public Iterator<String> getParameterNames() {
        return parameters.keySet().iterator();
    }

    /**
     * Clears all parameters contained in this parameter list.
     */
    public void clearParameters() {
        parameters.clear();
    }

    /**
     * Appends the RFC2045 style (ASCII-only) string representation of this parameter list including empty parameters.
     *
     * @param sb The string builder to append to
     * @see #appendRFC2045String(StringBuilder, boolean)
     */
    public void appendRFC2045String(final StringBuilder sb) {
        appendRFC2045String(sb, false);
    }

    /**
     * Appends the RFC2045 style (ASCII-only) string representation of this parameter list.
     *
     * @param sb The string builder to append to
     * @param skipEmptyParam <code>true</code> to skip empty parameters; otherwise <code>false</code>
     */
    public void appendRFC2045String(final StringBuilder sb, final boolean skipEmptyParam) {
        int size = parameters.size();
        List<String> names = new ArrayList<String>(size);
        names.addAll(parameters.keySet());
        Collections.sort(names);
        if (skipEmptyParam) {
            for (String name : names) {
                Parameter parameter = parameters.get(name);
                parameter.appendRFC2045String(sb);
            }
        } else {
            for (String name : names) {
                parameters.get(name).appendRFC2045String(sb);
            }
        }

        // final Iterator<Parameter> iter = parameters.values().iterator();
        // for (int i = 0; i < size; i++) {
        // iter.next().appendUnicodeString(sb);
        // }
    }

    /**
     * Returns the unicode (mail-safe) string representation of this parameter list
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(64);
        final int size = parameters.size();
        final List<String> names = new ArrayList<String>(size);
        names.addAll(parameters.keySet());
        Collections.sort(names);
        for (final String name : names) {
            parameters.get(name).appendRFC2045String(sb);
        }

        // final Iterator<Parameter> iter = parameters.values().iterator();
        // for (int i = 0; i < size; i++) {
        // iter.next().appendUnicodeString(sb);
        // }
        return sb.toString();
    }

    /**
     * Special characters (binary sorted) that must be in quoted-string to be used within parameter values
     */
    private static final char[] SPECIALS = { '\t', '\n', '\r', ' ', '"', '(', ')', ',', '/', ':', ';', '<', '=', '>', '?', '@', '[', '\\', ']' };

    private static boolean containsSpecial(final String str) {
        final int length = str.length();
        boolean quote = false;
        for (int i = 0; (i < length) && !quote; i++) {
            quote |= (Arrays.binarySearch(SPECIALS, str.charAt(i)) >= 0);
        }
        return quote;
    }

    private static final Pattern PAT_BSLASH = Pattern.compile("\\\\");

    private static final Pattern PAT_QUOTE = Pattern.compile("\"");

    static String checkQuotation(final String str) {
        if (containsSpecial(str)) {
            return new StringBuilder(2 + str.length()).append('"').append(PAT_QUOTE.matcher(PAT_BSLASH.matcher(str).replaceAll("\\\\\\\\")).replaceAll("\\\\\\\"")).append('"').toString();
        }
        return str;
    }

    private static String unescape(final String escaped) {
        final StringBuilder sb = new StringBuilder(escaped.length());

        final int length = escaped.length();

        boolean ignore = false;
        for (int i = 0; i < length; i++) {
            final char c = escaped.charAt(i);
            if ('\\' == c) {
                if (ignore) {
                    ignore = false;
                } else {
                    sb.append(c);
                    ignore = true;
                }
            } else {
                sb.append(c);
                ignore = true;
            }
        }

        return sb.toString();
    }

    /**
     * {@link Parameter} - Inner class to represent a parameter.
     *
     * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
     */
    private static final class Parameter implements Cloneable, Serializable, Comparable<Parameter> {

        private static final long serialVersionUID = 7978948703870567515L;

        boolean rfc2231;
        final String name;
        Value value;

        /**
         * Initializes a new rfc2231 parameter.
         *
         * @param name The parameter name without asterix characters
         */
        public Parameter(String name, String charset, String language) {
            super();
            rfc2231 = true;
            this.name = name;
            value = new RFC2231Value(charset, language);
        }

        /**
         * Initializes a new rfc2047 parameter.
         *
         * @param name The parameter name
         * @param value The parameter value
         */
        public Parameter(final String name, final String value) {
            super();
            rfc2231 = false;
            this.name = name;
            this.value = new DirectValue(value);
        }

        @Override
        public int compareTo(final Parameter other) {
            if (this == other) {
                return 0;
            }
            if (name == null) {
                if (other.name != null) {
                    return -1;
                }
                return 0;
            } else if (other.name == null) {
                return 1;
            }
            final int nameComp = name.compareToIgnoreCase(other.name);
            if (nameComp != 0) {
                return nameComp;
            }
            return getValue().compareToIgnoreCase(other.getValue());
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (rfc2231 ? 1231 : 1237);
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + ((getValue() == null) ? 0 : getValue().hashCode());
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
            if (getClass() != obj.getClass()) {
                return false;
            }
            Parameter other = (Parameter) obj;
            if (rfc2231 != other.rfc2231) {
                return false;
            }
            if (name == null) {
                if (other.name != null) {
                    return false;
                }
            } else if (!name.equals(other.name)) {
                return false;
            }
            if (getValue() == null) {
                if (other.getValue() != null) {
                    return false;
                }
            } else if (!getValue().equals(other.getValue())) {
                return false;
            }
            return true;
        }

        @Override
        public Object clone() {
            try {
                final Parameter clone = (Parameter) super.clone();
                return clone;
            } catch (final CloneNotSupportedException e) {
                LOG.error("", e);
                throw new RuntimeException("Clone failed even though 'Cloneable' interface is implemented");
            }

        }

        public void addContiguousEncodedValue(String encodedValue) {
            value.addContiguousValue(encodedValue);
        }

        public void setContiguousEncodedValue(int num, String encodedValue) {
            value.setContiguousValue(num, encodedValue);
        }

        public String getValue() {
            return null == value ? null : value.getValue();
        }

        public void appendRFC2045String(final StringBuilder sb) {
            value.appendRFC2045String(name, sb);
        }

    } // End of class Parameter

    private static abstract class Value implements Cloneable, Serializable {

        /** serialVersionUID */
        private static final long serialVersionUID = 1419299271485737013L;

        protected Value() {
            super();
        }

        abstract void addContiguousValue(final String contiguousValue);

        abstract void setContiguousValue(final int num, final String contiguousValue);

        abstract String getValue();

        abstract void appendRFC2045String(String name, StringBuilder sb);
    }

    private static class DirectValue extends Value {

        private final String value;

        DirectValue(String value) {
            super();
            this.value = Strings.isEmpty(value) ? null : value;
        }

        @Override
        void addContiguousValue(String contiguousValue) {
            // Ignore
        }

        @Override
        void setContiguousValue(int num, String contiguousValue) {
            // Ignore
        }

        @Override
        String getValue() {
            return value;
        }

        @Override
        void appendRFC2045String(String name, StringBuilder sb) {
            if (null == value) {
                sb.append("; ").append(name);
                return;
            }

            try {
                sb.append("; ").append(name).append('=').append(checkQuotation(MimeUtility.encodeText(getValue(), CHARSET_UTF_8, "Q")));
            } catch (final UnsupportedEncodingException e) {
                /*
                 * Cannot occur
                 */
                LOG.error("", e);
            }
        }

        @Override
        public Object clone() {
            try {
                DirectValue clone = (DirectValue) super.clone();
                return clone;
            } catch (final CloneNotSupportedException e) {
                LOG.error("", e);
                throw new RuntimeException("Clone failed even though 'Cloneable' interface is implemented");
            }
        }
    }

    private static class RFC2231Value extends Value {

        private String value;
        private List<String> encodedValues;
        private final String charset;
        private final String language;

        RFC2231Value(String charset, String language) {
            super();
            this.charset = charset;
            this.language = language;
            encodedValues = new ArrayList<>(4);
        }

        @Override
        public void addContiguousValue(String encodedValue) {
            if (null != value) {
                value = null;
            }
            if (Strings.isNotEmpty(encodedValue)) {
                encodedValues.add(encodedValue);
            }
        }

        @Override
        public void setContiguousValue(int num, String encodedValue) {
            if (null != value) {
                value = null;
            }
            if (Strings.isNotEmpty(encodedValue)) {
                while (num >= encodedValues.size()) {
                    encodedValues.add("");
                }
                encodedValues.set(num, encodedValue);
            }
        }

        @Override
        public String getValue() {
            if (null == value) {
                StringBuilder sb = new StringBuilder(64);
                for (String encodedValue : encodedValues) {
                    sb.append(encodedValue);
                }
                value = RFC2231Tools.rfc2231Decode(sb.toString(), charset);
            }
            return value;
        }

        @Override
        void appendRFC2045String(String name, StringBuilder sb) {
            int size = encodedValues.size();
            if (size == 0) {
                sb.append("; ").append(name);
                return;
            }

            if (size == 1) {
                sb.append("; ").append(name);
                if (getNextValidPos(0, size) != -1) {
                    if (RFC2231Tools.isAscii(getValue())) {
                        sb.append('=').append(checkQuotation(getValue()));
                    } else {
                        sb.append("*=").append(checkQuotation(RFC2231Tools.rfc2231Encode(getValue(), CHARSET_UTF_8, null, true)));
                    }
                }
            } else {
                // Append first
                int startPos = getNextValidPos(0, size);
                if (startPos == -1) {
                    sb.append("; ").append(name);
                    return;
                }
                int count = 1;
                sb.append("; ").append(name).append('*').append(count++);
                {
                    String firstChunk = encodedValues.get(startPos);
                    if (firstChunk.indexOf('%') < 0) {
                        sb.append("=");
                    } else {
                        sb.append("*=");
                    }
                    sb.append(charset).append('\'').append((language == null) || (language.length() == 0) ? "" : language).append('\'');
                    sb.append(firstChunk);
                }

                // Append remaining values
                while ((startPos = getNextValidPos(startPos + 1, size)) != -1) {
                    sb.append("; ").append(name).append('*').append(count++);
                    String chunk = encodedValues.get(startPos);
                    if (chunk.indexOf('%') < 0) {
                        sb.append('=').append(checkQuotation(chunk));
                    } else {
                        sb.append("*=").append(chunk);
                    }
                }
            }
        }

        private int getNextValidPos(final int fromPos, final int size) {
            for (int i = fromPos; i < size; i++) {
                String val = encodedValues.get(i);
                if (val != null && val.length() > 0) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public Object clone() {
            try {
                RFC2231Value clone = (RFC2231Value) super.clone();
                int size = encodedValues.size();
                clone.encodedValues = new ArrayList<String>(size);
                for (int i = 0; i < size; i++) {
                    clone.encodedValues.add(encodedValues.get(i));
                }
                clone.value = null;
                return clone;
            } catch (final CloneNotSupportedException e) {
                LOG.error("", e);
                throw new RuntimeException("Clone failed even though 'Cloneable' interface is implemented");
            }

        }
    }

}
