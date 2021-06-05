/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.plist.xml;

import static com.openexchange.java.Strings.isEmpty;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.lang.time.FastDateFormat;
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
