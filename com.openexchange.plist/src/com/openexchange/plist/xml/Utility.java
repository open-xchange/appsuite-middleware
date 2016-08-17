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

package com.openexchange.plist.xml;

import static com.openexchange.java.Strings.isEmpty;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.lang.time.FastDateFormat;
import com.openexchange.java.Streams;
import com.openexchange.tools.TimeZoneUtils;

/**
 * {@link Utility}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public final class Utility {

    /**
     * Initializes a new {@link Utility}.
     */
    private Utility() {
        super();
    }

    private static final FastDateFormat XML_DATE_FORMAT;
    static {
        // "yyyy-MM-dd'T'HH:mm:ssZ"
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss", TimeZoneUtils.getTimeZone("UTC"), Locale.US);
        XML_DATE_FORMAT = fdf;
    }

    /**
     * Formats given date to XML date time.
     *
     * @param date The date
     * @return The XML date format
     */
    public static String formatDate(Date date) {
        if (null == date) {
            return null;
        }

        return XML_DATE_FORMAT.format(date);
    }

    private static final Pattern PATTERN_CONTROL = Pattern.compile("[\\x00-\\x1F\\x7F]");

    /**
     * Sanitizes given XML content.
     *
     * @param sInput The XML content
     * @return The sanitized XML content
     */
    public static String sanitizeXmlContent(String sInput) {
        if (isEmpty(sInput)) {
            return sInput;
        }

        String s = sInput;

        // Do URL decoding until fully decoded
        {
            int pos;
            while ((pos = s.indexOf('%')) >= 0 && pos < s.length() - 1) {
                try {
                    s = new URLCodec("UTF-8").decode(s);
                } catch (org.apache.commons.codec.DecoderException e) {
                    break;
                }
            }
        }

        // Drop ASCII control characters
        s = PATTERN_CONTROL.matcher(s).replaceAll("");

        // Escape using HTML entities
        s = org.apache.commons.lang.StringEscapeUtils.escapeXml(s);

        // Return result
        return s;
    }

    private static String toXML(String s) {
        Formatter formatter = new Formatter();
        try {
            int len = s.length();
            for (int i = 0; i < len; i = s.offsetByCodePoints(i, 1)) {
                int c = s.codePointAt(i);
                if (c < 32 || c > 126 || c == '&' || c == '<' || c == '>') {
                    formatter.format("&#x%x;", c);
                } else {
                    formatter.format("%c", c);
                }
            }
            return formatter.toString();
        } finally {
            Streams.close(formatter);
        }
    }

    public static void writeTo(final boolean b, final String localName, final XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(localName);
        writer.writeCharacters(Utility.stringFor(b));
        writer.writeEndElement();
    }

    public static void writeTo(final int i, final String localName, final XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(localName);
        writer.writeCharacters(Utility.stringFor(i));
        writer.writeEndElement();
    }

    public static void writeTo(final long l, final String localName, final XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(localName);
        writer.writeCharacters(Utility.stringFor(l));
        writer.writeEndElement();
    }

    public static void writeTo(final String s, final String localName, final XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(localName);
        // writer.writeCharacters(sanitizeXmlContent(s));
        if (null != s) {
            writer.writeCharacters(s);
        }
        writer.writeEndElement();
    }

    public static void writeTo(final Date d, final String localName, final XMLStreamWriter writer) throws XMLStreamException {
        if (null != d) {
            writer.writeStartElement(localName);
            writer.writeCharacters(formatDate(d));
            writer.writeEndElement();
        }
    }

    public static void writeTo(final Object o, final String localName, final XMLStreamWriter writer) throws XMLStreamException {
        if (null != o) {
            writer.writeStartElement(localName);
            writer.writeCharacters(sanitizeXmlContent(o.toString()));
            writer.writeEndElement();
        }
    }

    public static void writeToCData(final boolean b, final String localName, final XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(localName);
        writer.writeCData(Utility.stringFor(b));
        writer.writeEndElement();
    }

    public static void writeToCData(final int i, final String localName, final XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(localName);
        writer.writeCData(Utility.stringFor(i));
        writer.writeEndElement();
    }

    public static void writeToCData(final long l, final String localName, final XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(localName);
        writer.writeCData(Utility.stringFor(l));
        writer.writeEndElement();
    }

    public static void writeToCData(final String s, final String localName, final XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(localName);
        writer.writeCData(writeEmpty(s));
        writer.writeEndElement();
    }

    public static void writeToCData(final Object o, final String localName, final XMLStreamWriter writer) throws XMLStreamException {
        if (null != o) {
            writer.writeStartElement(localName);
            writer.writeCData(o.toString());
            writer.writeEndElement();
        }
    }

    // ------------------------------------------------------------------- //

    public static String stringFor(final boolean b) {
        // According to the XML Schema specification (http://www.w3.org/TR/xmlschema-2/#boolean)
        //
        // 3.2.2.1 Lexical representation
        // An instance of a datatype that is defined as boolean can have the
        // following legal literals {true, false, 1, 0}.
        return b ? "true" : "false";
    }

    public static String stringFor(final int i) {
        return Integer.toString(i);
    }

    public static String stringFor(final long l) {
        return Long.toString(l);
    }

    public static String stringFor(final String s) {
        return s;
    }

    public static String writeEmpty(final String s) {
        return null == s ? "" : s;
    }

}
