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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Map.Entry;
import com.openexchange.mail.MailException;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * {@link HeaderCollection} - Represents a collection of <small><b><a href="http://www.ietf.org/rfc/rfc822.txt">RFC822</a></b></small>
 * headers.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class HeaderCollection implements Serializable {

    private static final String ERR_HEADER_NAME_IS_INVALID = "Header name is invalid";

    private static final long serialVersionUID = 6939560514144351286L;

    private static final transient org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(HeaderCollection.class);

    /**
     * Read-only constant for an empty header collection
     */
    public static final HeaderCollection EMPTY_COLLECTION = new HeaderCollection(0).getReadOnlyCollection();

    private static final String CRLF = "\r\n";

    private static final String CHARSET_US_ASCII = "US-ASCII";

    private static final int DEFAULT_CAPACITY = 4096;

    private final HashMap<HeaderName, List<String>> map;

    private int count;

    /**
     * Initializes a new {@link HeaderCollection} with a default initial capacity of <code>40</code>.
     */
    public HeaderCollection() {
        this(40);
    }

    /**
     * Initializes a new {@link HeaderCollection}
     * 
     * @param initialCapacity The collection's initial capacity
     */
    public HeaderCollection(final int initialCapacity) {
        super();
        map = new HashMap<HeaderName, List<String>>(initialCapacity);
    }

    /**
     * Copy constructor for {@link HeaderCollection}
     * 
     * @param headers The source headers
     */
    public HeaderCollection(final HeaderCollection headers) {
        this(headers.map.size());
        addHeaders(headers);
    }

    /**
     * Initializes a new {@link HeaderCollection} from specified headers' <small><b><a
     * href="http://www.ietf.org/rfc/rfc822.txt">RFC822</a></b></small> source
     * 
     * @param headerSrc The headers' <small><b><a href="http://www.ietf.org/rfc/rfc822.txt" >RFC822</a></b></small> source
     * @throws MailException If parsing the header source fails
     */
    public HeaderCollection(final String headerSrc) throws MailException {
        this();
        load(headerSrc);
    }

    /**
     * Initializes a new {@link HeaderCollection} from specified headers' <small><b><a
     * href="http://www.ietf.org/rfc/rfc822.txt">RFC822</a></b></small> input stream
     * 
     * @param inputStream The headers' <small><b><a href="http://www.ietf.org/rfc/rfc822.txt" >RFC822</a></b></small> input stream
     * @throws MailException If parsing the header input stream fails
     */
    public HeaderCollection(final InputStream inputStream) throws MailException {
        this();
        load(inputStream);
    }

    /**
     * Gets a read-only {@link HeaderCollection collection} constructed from this collection's current content
     * 
     * @return A read-only {@link HeaderCollection collection}
     */
    public HeaderCollection getReadOnlyCollection() {
        return new ReadOnlyHeaderCollection(this);
    }

    /**
     * Read and parse the given headers' <small><b><a href="http://www.ietf.org/rfc/rfc822.txt">RFC822</a></b></small> input stream till the
     * blank line separating the header from the body. Thus specified input stream is <b>not</b> closed by this method.
     * <p>
     * Note that the header lines are added, so any existing headers in this object will not be affected. Headers are added to the end of
     * the existing list of headers, in order.
     * <p>
     * Provided input stream is not going to be closed but is read until <code>EOF</code> or two subsequent <code>CRLF</code>s occur.
     * 
     * @param inputStream The headers' <small><b><a href="http://www.ietf.org/rfc/rfc822.txt" >RFC822</a></b></small> input stream
     * @throws MailException If reading from headers' <small><b><a href="http://www.ietf.org/rfc/rfc822.txt" >RFC822</a></b></small> input
     *             stream fails
     */
    public void load(final InputStream inputStream) throws MailException {
        /*
         * Gather bytes until empty line or EOF
         */
        final ByteArrayOutputStream buffer = new UnsynchronizedByteArrayOutputStream(DEFAULT_CAPACITY);
        try {
            int i = -1;
            NextRead: while ((i = inputStream.read()) != -1) {
                int count = 0;
                while ((i == '\r') || (i == '\n')) {
                    if (i != -1) {
                        buffer.write(i);
                    }
                    if ((i == '\n') && (++count >= 2)) {
                        break NextRead;
                    }
                    i = inputStream.read();
                }
                buffer.write(i);
            }
            load(new String(buffer.toByteArray(), CHARSET_US_ASCII));
        } catch (final IOException e) {
            throw new MailException(MailException.Code.IO_ERROR, e, e.getMessage());
        }
    }

    /**
     * Read and parse the given headers' <small><b><a href="http://www.ietf.org/rfc/rfc822.txt">RFC822</a></b></small> source till the blank
     * line separating the header from the body.
     * <p>
     * Note that the header lines are added, so any existing headers in this object will not be affected. Headers are added to the end of
     * the existing list of headers, in order.
     * 
     * @param headerSrc The headers' <small><b><a href="http://www.ietf.org/rfc/rfc822.txt" >RFC822</a></b></small> source
     * @throws MailException If reading from headers' source fails
     */
    public void load(final String headerSrc) throws MailException {
        if (null == headerSrc || headerSrc.length() == 0) {
            // Nothing to load
            return;
        }
        /*
         * Read header lines until a blank line.
         */
        final BufferedReader reader = new BufferedReader(new StringReader(headerSrc));
        final StringBuilder lineBuffer = new StringBuilder(128);
        String line;
        String prevline = null;
        try {
            do {
                line = reader.readLine();
                if (line != null && line.length() > 0 && (line.charAt(0) == ' ' || line.charAt(0) == '\t')) {
                    /*
                     * Header continuation
                     */
                    if (prevline != null) {
                        lineBuffer.append(prevline);
                        prevline = null;
                    }
                    lineBuffer.append(CRLF);
                    lineBuffer.append(line);
                } else {
                    /*
                     * A new header
                     */
                    if (prevline != null) {
                        addHeaderLine(prevline);
                    } else if (lineBuffer.length() > 0) {
                        /*
                         * Store previous header first
                         */
                        addHeaderLine(lineBuffer.toString());
                        lineBuffer.setLength(0);
                    }
                    prevline = line;
                }
            } while (line != null && line.length() > 0);
        } catch (final IOException ioex) {
            throw new MailException(MailException.Code.IO_ERROR, ioex, ioex.getMessage());
        }
    }

    private final void addHeaderLine(final String headerLine) {
        int pos = headerLine.indexOf(':');
        if (pos == -1) {
            throw new IllegalStateException("Invalid header line: " + headerLine);
        }
        final String headerName = headerLine.substring(0, pos);
        // Last valid index position
        final int mlen = headerLine.length() - 1;
        if (pos < mlen && headerLine.charAt(pos + 1) == ' ') {
            pos++;
        }
        addHeader(headerName, pos < mlen ? headerLine.substring(pos + 1) : "");
    }

    /**
     * Clears this header collection.
     */
    public void clear() {
        map.clear();
        count = 0;
    }

    /**
     * Returns <code>true</code> if no headers are contained in this collection
     * 
     * @return <code>true</code> if no headers are contained in this collection
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Gets the number of headers contained in this collection.
     * <p>
     * This method is a constant-time operation.
     * 
     * @return The number of headers contained in this collection
     */
    public int size() {
        return count;
    }

    /**
     * Adds the specified header collection to this header collection.
     * <p>
     * Note that the header lines are added, so any existing headers in this object will not be affected. Headers are added to the end of
     * the existing list of headers, in order.
     * 
     * @param headers The header collection to add
     */
    public void addHeaders(final HeaderCollection headers) {
        final int size = headers.map.size();
        final Iterator<Map.Entry<HeaderName, List<String>>> iter = headers.map.entrySet().iterator();
        for (int i = 0; i < size; i++) {
            final Map.Entry<HeaderName, List<String>> entry = iter.next();
            List<String> values = map.get(entry.getKey());
            if (values == null) {
                values = new ArrayList<String>(entry.getValue().size());
                map.put(entry.getKey(), values);
            }
            values.addAll(entry.getValue());
            count += entry.getValue().size();
        }
    }

    /**
     * Adds a header with the specified name and value
     * <p>
     * The current implementation knows about the preferred order of most well-known headers and will insert headers in that order. In
     * addition, it knows that <code>Received</code> headers should be inserted in reverse order (newest before oldest), and that they
     * should appear at the beginning of the headers, preceded only by a possible <code>Return-Path</code> header.
     * <p>
     * Note that <small><b><a href="http://www.ietf.org/rfc/rfc822.txt">RFC822</a></b></small> headers can only contain
     * <small><b>US-ASCII</b></small> characters.
     * 
     * @param name The header name
     * @param value The header value
     * @return This header collection with specified header added
     * @throws IllegalArgumentException If name or value is invalid
     */
    public HeaderCollection addHeader(final String name, final String value) {
        putHeader(name, value, false);
        return this;
    }

    /**
     * Change the first header that matches name to have value, adding a new header if no existing header matches. Remove all matching
     * headers but the first.
     * <p>
     * Note that <small><b><a href="http://www.ietf.org/rfc/rfc822.txt">RFC822</a></b></small> headers can only contain
     * <small><b>US-ASCII</b></small> characters.
     * 
     * @param name The header name
     * @param value The header value
     * @return This header collection with specified header set
     * @throws IllegalArgumentException If name or value is invalid
     */
    public HeaderCollection setHeader(final String name, final String value) {
        putHeader(name, value, true);
        return this;
    }

    private final void putHeader(final String name, final String value, final boolean clear) {
        if (isInvalid(name, true)) {
            throw new IllegalArgumentException(new StringBuilder(ERR_HEADER_NAME_IS_INVALID).append(": ").append(name).toString());
        } else if (isInvalid(value, false)) {
            throw new IllegalArgumentException(new StringBuilder(32).append("Header value is invalid: ").append(value).toString());
        }
        final HeaderName headerName = HeaderName.valueOf(name);
        List<String> values = map.get(headerName);
        if (values == null) {
            values = new ArrayList<String>(2);
            map.put(headerName, values);
        } else if (clear) {
            values.clear();
        }
        if (MessageHeaders.RECEIVED.equals(headerName) || MessageHeaders.RETURN_PATH.equals(headerName)) {
            // Prepend
            values.add(0, value);
        } else {
            // Append
            values.add(value);
        }
        count++;
    }

    /**
     * Checks if this header collection contains a header entry for specified header
     * 
     * @param name The header name
     * @return <code>true</code> if this header collection contains a header entry for specified header; otherwise <code>false</code>
     */
    public boolean containsHeader(final String name) {
        if (isInvalid(name, true)) {
            throw new IllegalArgumentException(new StringBuilder(ERR_HEADER_NAME_IS_INVALID).append(": ").append(name).toString());
        }
        return map.containsKey(HeaderName.valueOf(name));
    }

    /**
     * Return all the values for the specified header. Returns <code>null</code> if no headers with the specified name exist.
     * 
     * @param name The header name
     * @return An array of header values, or <code>null</code> if none exists
     * @throws IllegalArgumentException If name is invalid
     */
    public String[] getHeader(final String name) {
        if (isInvalid(name, true)) {
            throw new IllegalArgumentException(new StringBuilder(ERR_HEADER_NAME_IS_INVALID).append(": ").append(name).toString());
        }
        final List<String> values = map.get(HeaderName.valueOf(name));
        if (values == null) {
            return null;
        }
        return values.toArray(new String[values.size()]);
    }

    /**
     * Get all the headers for this header name, returned as a single String, with headers separated by the delimiter. If the delimiter is
     * <code>null</code>, only the first header is returned. Returns <code>null</code> if no headers with the specified name exist.
     * 
     * @param name The header name
     * @param delimiter The delimiter
     * @return The value fields for all headers with this name, or <code>null</code> if none
     */
    public String getHeader(final String name, final String delimiter) {
        if (isInvalid(name, true)) {
            throw new IllegalArgumentException(new StringBuilder(ERR_HEADER_NAME_IS_INVALID).append(": ").append(name).toString());
        }
        final List<String> values = map.get(HeaderName.valueOf(name));
        if (values == null) {
            return null;
        }
        final int size;
        if (delimiter == null || (size = values.size()) == 1) {
            return values.get(0);
        }
        final StringBuilder sb = new StringBuilder(values.get(0));
        for (int i = 1; i < size; i++) {
            sb.append(delimiter).append(values.get(i));
        }
        return sb.toString();
    }

    /**
     * Get all the headers for this header name, returned as a single String, with headers separated by the delimiter. If the delimiter is
     * <code>'\0'</code>, only the first header is returned. Returns <code>null</code> if no headers with the specified name exist.
     * 
     * @param name The header name
     * @param delimiter The delimiter character
     * @return The value fields for all headers with this name, or <code>null</code> if none
     */
    public String getHeader(final String name, final char delimiter) {
        if (isInvalid(name, true)) {
            throw new IllegalArgumentException(new StringBuilder(ERR_HEADER_NAME_IS_INVALID).append(": ").append(name).toString());
        }
        final List<String> values = map.get(HeaderName.valueOf(name));
        if (values == null) {
            return null;
        }
        final int size;
        if (delimiter == '\0' || (size = values.size()) == 1) {
            return values.get(0);
        }
        final StringBuilder sb = new StringBuilder(values.get(0));
        for (int i = 1; i < size; i++) {
            sb.append(delimiter).append(values.get(i));
        }
        return sb.toString();
    }

    /**
     * Remove all header entries that match the given name
     * 
     * @param name The header name
     * @return This header collection with specified header removed
     */
    public HeaderCollection removeHeader(final String name) {
        if (isInvalid(name, true)) {
            throw new IllegalArgumentException(new StringBuilder(ERR_HEADER_NAME_IS_INVALID).append(": ").append(name).toString());
        }
        final List<String> removed = map.remove(HeaderName.valueOf(name));
        if (removed != null) {
            count -= removed.size();
        }
        return this;
    }

    /**
     * Gets an instance of {@link Iterator} to iterate all headers.
     * 
     * @return An instance of {@link Iterator} to iterate all headers
     */
    public Iterator<Map.Entry<String, String>> getAllHeaders() {
        if (map.isEmpty()) {
            return EMPTY_ITER;
        }
        return new HeaderIterator(map.entrySet().iterator());
    }

    /**
     * Gets the matching headers
     * 
     * @param matchingHeaders The matching headers
     * @return The matching headers
     */
    public Iterator<Map.Entry<String, String>> getMatchingHeaders(final String[] matchingHeaders) {
        final Set<HeaderName> set = new HashSet<HeaderName>(matchingHeaders.length);
        for (int i = 0; i < matchingHeaders.length; i++) {
            set.add(HeaderName.valueOf(matchingHeaders[i]));
        }
        return new HeaderIterator(map.entrySet().iterator(), set, true);
    }

    /**
     * Gets the non-matching headers
     * 
     * @param nonMatchingHeaders The non-matching headers
     * @return The non-matching headers
     */
    public Iterator<Map.Entry<String, String>> getNonMatchingHeaders(final String[] nonMatchingHeaders) {
        final Set<HeaderName> set = new HashSet<HeaderName>(nonMatchingHeaders.length);
        for (int i = 0; i < nonMatchingHeaders.length; i++) {
            set.add(HeaderName.valueOf(nonMatchingHeaders[i]));
        }
        return new HeaderIterator(map.entrySet().iterator(), set, false);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(4096);
        for (final Iterator<Map.Entry<String, String>> iter = getAllHeaders(); iter.hasNext();) {
            final Map.Entry<String, String> e = iter.next();
            sb.append(e.getKey()).append(": ").append(e.getValue()).append(CRLF);
        }
        return sb.toString();
    }

    /**
     * Indicates whether some other object is "equal to" this one. However this method should be used with care since checking a given
     * header collection for equality with this one requires sorting according to header names and then comparing each header one-by-one.
     * Besides this method behaves exactly as stated in {@link Object#equals(Object) equal} method.
     * 
     * @param obj The reference object with which to compare.
     * @return <code>true</code> if this object is the same as the object argument; <code>false</code> otherwise.
     */
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
        final HeaderCollection other = (HeaderCollection) obj;
        if (count != other.count) {
            return false;
        }
        if (map == null) {
            if (other.map != null) {
                return false;
            }
            return true;
        }
        if (map.size() != other.map.size()) {
            return false;
        }
        final HeaderName[] names = map.keySet().toArray(new HeaderName[map.size()]);
        java.util.Arrays.sort(names);
        final HeaderName[] otherNames = other.map.keySet().toArray(new HeaderName[other.map.size()]);
        java.util.Arrays.sort(otherNames);
        if (!java.util.Arrays.equals(names, otherNames)) {
            return false;
        }
        for (int i = 0; i < names.length; i++) {
            final List<String> list = map.get(names[i]);
            final List<String> otherList = other.map.get(names[i]);
            if (list == null) {
                if (otherList != null) {
                    return false;
                }
            } else if (!list.equals(otherList)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        final HeaderName[] names = map.keySet().toArray(new HeaderName[map.size()]);
        java.util.Arrays.sort(names);
        result = prime * result + java.util.Arrays.hashCode(names);
        for (int i = 0; i < names.length; i++) {
            final List<String> list = map.get(names[i]);
            result = prime * result + ((list == null) ? 0 : list.hashCode());
        }
        return result;
    }

    /*-
     * ############ UTILITY METHODS ##############
     */

    private static final transient Iterator<Map.Entry<String, String>> EMPTY_ITER = new Iterator<Map.Entry<String, String>>() {

        public boolean hasNext() {
            return false;
        }

        public Entry<String, String> next() {
            throw new NoSuchElementException();
        }

        public void remove() {
            // Nothing to remove
        }
    };

    private static final class HeaderIterator implements Iterator<Map.Entry<String, String>> {

        private final Iterator<Map.Entry<HeaderName, List<String>>> iter;

        private final Set<HeaderName> headers;

        private final boolean matches;

        private int index;

        private Map.Entry<HeaderName, List<String>> entry;

        public HeaderIterator(final Iterator<Map.Entry<HeaderName, List<String>>> iter) {
            super();
            this.iter = iter;
            matches = false;
            headers = null;
        }

        public HeaderIterator(final Iterator<Map.Entry<HeaderName, List<String>>> iter, final Set<HeaderName> headers, final boolean matches) {
            super();
            this.iter = iter;
            this.matches = matches;
            this.headers = headers;
        }

        public boolean hasNext() {
            if (entry == null || index < 0) {
                while (iter.hasNext()) {
                    entry = iter.next();
                    if (headers == null || (matches ? headers.contains(entry.getKey()) : !headers.contains(entry.getKey()))) {
                        index = entry.getValue().size() - 1;
                        return true;
                    }
                }
                entry = null;
                return false;
            }
            return (index >= 0);
        }

        public Entry<String, String> next() {
            if (entry == null || index < 0) {
                while (iter.hasNext()) {
                    entry = iter.next();
                    if (headers == null || (matches ? headers.contains(entry.getKey()) : !headers.contains(entry.getKey()))) {
                        index = entry.getValue().size() - 1;
                        return new HeaderEntry(entry, index--);
                    }
                }
                entry = null;
                throw new NoSuchElementException();
            }
            return new HeaderEntry(entry, index--);
        }

        public void remove() {
            if (entry == null) {
                throw new IllegalStateException(
                    "next() method has not yet been called, or the remove()" + " method has already been called after the last call to the next() method.");
            }
            entry.getValue().remove(--index);
            if (entry.getValue().isEmpty()) {
                iter.remove();
                entry = null;
            }
        }

    }

    private static final class HeaderEntry implements Map.Entry<String, String> {

        private final Map.Entry<HeaderName, List<String>> entry;

        private final int index;

        public HeaderEntry(final Map.Entry<HeaderName, List<String>> entry, final int index) {
            super();
            this.entry = entry;
            this.index = index;
        }

        public String getKey() {
            return entry.getKey().toString();
        }

        public String getValue() {
            return entry.getValue().get(index);
        }

        public String setValue(final String value) {
            return entry.getValue().set(index, value);
        }

    }

    private static final class ReadOnlyHeaderCollection extends HeaderCollection {

        private static final long serialVersionUID = 3272885948579962027L;

        public ReadOnlyHeaderCollection(final HeaderCollection headers) {
            super(headers);
        }

        @Override
        public HeaderCollection addHeader(final String name, final String value) {
            throw new UnsupportedOperationException("ReadOnlyHeaderCollection.addHeader() is not supported");
        }

        @Override
        public HeaderCollection setHeader(final String name, final String value) {
            throw new UnsupportedOperationException("ReadOnlyHeaderCollection.setHeader() is not supported");
        }

        @Override
        public void load(final String headersSrc) {
            throw new UnsupportedOperationException("ReadOnlyHeaderCollection.load() is not supported");
        }

        @Override
        public void load(final InputStream inputStream) {
            throw new UnsupportedOperationException("ReadOnlyHeaderCollection.load() is not supported");
        }

        @Override
        public HeaderCollection removeHeader(final String name) {
            throw new UnsupportedOperationException("ReadOnlyHeaderCollection.removeHeader() is not supported");
        }

    }

    /**
     * Specified string is invalid if it is <code>null</code>, empty, its characters are whitespace characters only or contains Non-ASCII 7
     * bit
     * 
     * @param str The string to check
     * @param isName <code>true</code> to check a header name; otherwise <code>false</code> to check a header value
     * @return <code>true</code> if string is invalid; otherwise <code>false</code>
     */
    private static final boolean isInvalid(final String str, final boolean isName) {
        if (str == null) {
            return true;
        }
        final char[] chars = str.toCharArray();
        if (isName) {
            if (str.length() == 0) {
                return true;
            }
            for (int i = 0; i < chars.length; i++) {
                if (Character.isWhitespace(chars[i])) {
                    return true;
                }
            }
            return !isAscii(str);
        }
        if (str.length() == 0) {
            return false;
        }
        for (int i = 0; i < chars.length; i++) {
            if (!Character.isWhitespace(chars[i])) {
                return false/* !isAscii(str) */;
            }
        }
        return true;
    }

    /**
     * Checks whether the specified string's characters are ASCII 7 bit
     * 
     * @param s The string to check
     * @return <code>true</code> if string's characters are ASCII 7 bit; otherwise <code>false</code>
     */
    private static final boolean isAscii(final String s) {
        final char[] chars = s.toCharArray();
        boolean isAscci = true;
        for (int i = 0; (i < chars.length) && isAscci; i++) {
            isAscci &= (chars[i] < 128);
        }
        return isAscci;
    }

    /**
     * Simple test method
     */
    public static final void test() {
        try {
            final HeaderCollection hc = new HeaderCollection();

            hc.addHeader("From", "Jane Doe <jane.doe@somewhere.org>");
            hc.addHeader("To", "Jane Doe2 <jane.doe2@somewhere.org>, Jane Doe3 <jane.doe3@somewhere.org>");
            hc.addHeader(
                "Received",
                "first from [212.227.126.201] (helo=mxintern.foobar.de) " + "by mx.barfoo.de (node=mxeu24) with ESMTP (Nemesis), " + "id 0MKtd6-1ICv9b3jPS-0001BJ for user2@host.de; Mon, 23 Jul 2007 12:28:42 +0200");
            hc.addHeader(
                "Received",
                "second from [172.23.1.244] (helo=titan.foobar.de) " + "by mxintern.barfoo.de with esmtp (Exim 4.50) " + "id 1ICv9b-0004A2-Jn for user2@host.de; Mon, 23 Jul 2007 12:28:39 +0200");
            hc.addHeader("Subject", "The simple subject");
            hc.addHeader("Aaa", "dummy header here");

            final Iterator<Map.Entry<String, String>> iter = hc.getAllHeaders();
            while (iter.hasNext()) {
                final Map.Entry<String, String> e = iter.next();
                LOG.info(e.getKey() + ": " + e.getValue());
                if ("Faust".equals(e.getKey())) {
                    iter.remove();
                }
            }

            LOG.info("\n\nAfter removal through iterator");

            final Iterator<Map.Entry<String, String>> iter2 = hc.getAllHeaders();
            while (iter2.hasNext()) {
                final Map.Entry<String, String> e = iter2.next();
                LOG.info(e.getKey() + ": " + e.getValue());
            }

            LOG.info("\n\nNon-Matching");

            final Iterator<Map.Entry<String, String>> iter3 = hc.getNonMatchingHeaders(new String[] { "To", "From" });
            while (iter3.hasNext()) {
                final Map.Entry<String, String> e = iter3.next();
                LOG.info(e.getKey() + ": " + e.getValue());
            }

            LOG.info("\n\nMatching");

            final Iterator<Map.Entry<String, String>> iter4 = hc.getMatchingHeaders(new String[] { "To", "From" });
            while (iter4.hasNext()) {
                final Map.Entry<String, String> e = iter4.next();
                LOG.info(e.getKey() + ": " + e.getValue());
            }

            LOG.info("\n\nEquals");

            final HeaderCollection hc2 = new HeaderCollection();

            hc2.addHeader("From", "Jane Doe <jane.doe@somewhere.org>");
            hc2.addHeader("To", "Jane Doe2 <jane.doe2@somewhere.org>, Jane Doe3 <jane.doe3@somewhere.org>");
            hc2.addHeader(
                "Received",
                "first from [212.227.126.201] (helo=mxintern.foobar.de) " + "by mx.barfoo.de (node=mxeu24) with ESMTP (Nemesis), " + "id 0MKtd6-1ICv9b3jPS-0001BJ for user2@host.de; Mon, 23 Jul 2007 12:28:42 +0200");
            hc2.addHeader(
                "Received",
                "second from [172.23.1.244] (helo=titan.foobar.de) " + "by mxintern.barfoo.de with esmtp (Exim 4.50) " + "id 1ICv9b-0004A2-Jn for user2@host.de; Mon, 23 Jul 2007 12:28:39 +0200");
            hc2.addHeader("Subject", "The simple subject");
            hc2.addHeader("Aaa", "dummy header here");

            LOG.info(Boolean.valueOf(hc.equals(hc2)));

        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
