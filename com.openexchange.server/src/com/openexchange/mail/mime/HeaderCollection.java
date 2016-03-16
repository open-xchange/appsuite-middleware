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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Pattern;
import javax.mail.MessageRemovedException;
import javax.mail.internet.MimeUtility;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.java.CharsetDetector;
import com.openexchange.java.Charsets;
import com.openexchange.java.Strings;
import com.openexchange.java.UnsynchronizedByteArrayInputStream;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * {@link HeaderCollection} - Represents a collection of <small><b><a href="http://www.ietf.org/rfc/rfc822.txt">RFC822</a></b></small>
 * headers.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class HeaderCollection implements Serializable {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(HeaderCollection.class);

    private static final String ERR_HEADER_NAME_IS_INVALID = "Header name is invalid";

    private static final long serialVersionUID = 6939560514144351286L;

    /**
     * Read-only constant for an empty header collection
     */
    public static final HeaderCollection EMPTY_COLLECTION = new HeaderCollection(0).getReadOnlyCollection();

    private static final String CRLF = "\r\n";

    private static final int DEFAULT_CAPACITY = 4096;

    private static final HeaderName RETURN_PATH = MessageHeaders.RETURN_PATH;

    private static final HeaderName RECEIVED = MessageHeaders.RECEIVED;

    private static final HeaderName CONTENT_TYPE = MessageHeaders.CONTENT_TYPE;

    private static final Locale ENGLISH = Locale.ENGLISH;

    private static final Map<String, String> CASE_SENSITIVE_LOOKUP;

    static {
        final Map<String, String> map = new HashMap<String, String>(24);
        final Locale l = ENGLISH;
        map.put("Date".toLowerCase(l), "Date");
        map.put("From".toLowerCase(l), "From");
        map.put("Sender".toLowerCase(l), "Sender");
        map.put("Reply-To".toLowerCase(l), "Reply-To");
        map.put("To".toLowerCase(l), "To");
        map.put("Cc".toLowerCase(l), "Cc");
        map.put("Bcc".toLowerCase(l), "Bcc");
        map.put("Message-ID".toLowerCase(l), "Message-ID");
        map.put("In-Reply-To".toLowerCase(l), "In-Reply-To");
        map.put("References".toLowerCase(l), "References");
        map.put("Subject".toLowerCase(l), "Subject");
        map.put("Comments".toLowerCase(l), "Comments");
        map.put("Keywords".toLowerCase(l), "Keywords");
        map.put("Resent-Date".toLowerCase(l), "Resent-Date");
        map.put("Resent-From".toLowerCase(l), "Resent-From");
        map.put("Resent-Sender".toLowerCase(l), "Resent-Sender");
        map.put("Resent-To".toLowerCase(l), "Resent-To");
        map.put("Resent-Cc".toLowerCase(l), "Resent-Cc");
        map.put("Resent-Bcc".toLowerCase(l), "Resent-Bcc");
        map.put("Resent-Message-ID".toLowerCase(l), "Resent-Message-ID");
        CASE_SENSITIVE_LOOKUP = Collections.unmodifiableMap(map);
    }

    /**
     * Gets the case-sensitive header name as per RFC2822.
     *
     * @param name The header name to check
     * @return The case-sensitive header name
     */
    public static String caseSensitiveHeaderNameFor(final String name) {
        final String cshn = CASE_SENSITIVE_LOOKUP.get(name.toLowerCase(ENGLISH));
        return null == cshn ? name : cshn;
    }

    /*-
     * ------------------------ Member stuff ---------------------------------
     */

    private final Map<HeaderName, List<String>> map;

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
        super();
        map = new LinkedHashMap<HeaderName, List<String>>(headers.map.size());
        addHeaders(headers);
    }

    /**
     * Initializes a new {@link HeaderCollection} from specified headers' <small><b><a
     * href="http://www.ietf.org/rfc/rfc822.txt">RFC822</a></b></small> source
     *
     * @param headerSrc The headers' <small><b><a href="http://www.ietf.org/rfc/rfc822.txt" >RFC822</a></b></small> source
     */
    public HeaderCollection(final String headerSrc) {
        super();
        map = new LinkedHashMap<HeaderName, List<String>>(40);
        load(headerSrc);
    }

    /**
     * Initializes a new {@link HeaderCollection} from specified headers' <small><b><a
     * href="http://www.ietf.org/rfc/rfc822.txt">RFC822</a></b></small> input stream
     *
     * @param inputStream The headers' <small><b><a href="http://www.ietf.org/rfc/rfc822.txt" >RFC822</a></b></small> input stream
     * @throws OXException If parsing the header input stream fails
     */
    public HeaderCollection(final InputStream inputStream) throws OXException {
        super();
        map = new LinkedHashMap<HeaderName, List<String>>(40);
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
     * @throws OXException If reading from headers' <small><b><a href="http://www.ietf.org/rfc/rfc822.txt" >RFC822</a></b></small> input
     *             stream fails
     */
    public void load(final InputStream inputStream) throws OXException {
        /*
         * Gather bytes until empty line or EOF
         */
        final ByteArrayOutputStream buffer = new UnsynchronizedByteArrayOutputStream(DEFAULT_CAPACITY);
        try {
            int i = -1;
            NextRead: while ((i = inputStream.read()) != -1) {
                int count = 0;
                while (('\r' == i) || ('\n' == i)) {
                    buffer.write(i);
                    if (('\n' == i) && (++count >= 2)) {
                        break NextRead;
                    }
                    i = inputStream.read();
                    if (-1 == i) { // EOF
                        break NextRead;
                    }
                }
                buffer.write(i);
            }
            load(new String(buffer.toByteArray(), Charsets.ISO_8859_1));
        } catch (final IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            }
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    private static final Pattern SPLIT = Pattern.compile("\r?\n");

    /**
     * Read and parse the given headers' <small><b><a href="http://www.ietf.org/rfc/rfc822.txt">RFC822</a></b></small> source till the blank
     * line separating the header from the body.
     * <p>
     * Note that the header lines are added, so any existing headers in this object will not be affected. Headers are added to the end of
     * the existing list of headers, in order.
     *
     * @param headerSrc The headers' <small><b><a href="http://www.ietf.org/rfc/rfc822.txt" >RFC822</a></b></small> source
     */
    public void load(final String headerSrc) {
        if ((null == headerSrc) || (0 == headerSrc.length())) {
            // Nothing to load
            return;
        }
        /*
         * Read header lines until a blank line.
         */
        final String[] lines = SPLIT.split(headerSrc, 0);

        final StringBuilder lineBuffer = new StringBuilder(76);
        boolean emptyBuffer = true;

        String prevline = null;

        String line;
        int length;

        for (int i = 0; i < lines.length && (length = (line = lines[i]).length()) > 0; i++) {
            if ((length > 0) && isSpaceOrTab(line.charAt(0))) {
                /*
                 * Header continuation
                 */
                if (prevline != null) {
                    lineBuffer.append(prevline);
                    prevline = null;
                }
                lineBuffer.append(CRLF);
                lineBuffer.append(line);
                emptyBuffer = false;
            } else {
                /*
                 * A new header
                 */
                if (prevline != null) {
                    addHeaderLine(prevline);
                } else if (!emptyBuffer) {
                    /*
                     * Store previous header first
                     */
                    addHeaderLine(lineBuffer.toString());
                    lineBuffer.setLength(0);
                    emptyBuffer = true;
                }
                prevline = line;
            }
        }
        /*
         * Check for pending header line
         */
        if (prevline != null) {
            addHeaderLine(prevline);
        } else if (!emptyBuffer) {
            addHeaderLine(lineBuffer.toString());
        }
    }

    private static boolean isSpaceOrTab(final char c) {
        return (c == ' ' || c == '\t');
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
            LOG.debug("{0}: {1}", ERR_HEADER_NAME_IS_INVALID, name, new IllegalArgumentException());
            // Do nothing...
            return;
        }
        if (isInvalid(value, false)) {
            /*
             * Ignore an empty value
             */
            return;
        }
        final HeaderName headerName = HeaderName.valueOf(caseSensitiveHeaderNameFor(name));
        List<String> values = map.get(headerName);
        if (values == null) {
            values = new ArrayList<String>(2);
            map.put(headerName, values);
        } else if (clear) {
            values.clear();
        }
        if (RECEIVED.equals(headerName) || RETURN_PATH.equals(headerName) || CONTENT_TYPE.equals(headerName)) {
            /*
             * Append
             */
            values.add(checkValue(value));
        } else {
            /*
             * Prepend
             */
            values.add(0, checkValue(value));
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
     * Gets an  instance of {@link Iterator} to iterate all header names.
     *
     * @return An  instance of {@link Iterator} to iterate all header names
     */
    public Iterator<String> getHeaderNames() {
        final List<String> tmp = new ArrayList<String>(map.size());
        for (final HeaderName headerName : map.keySet()) {
            tmp.add(headerName.toString());
        }
        return tmp.iterator();
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
        for (String matchingHeader : matchingHeaders) {
            set.add(HeaderName.valueOf(matchingHeader));
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
        for (String nonMatchingHeader : nonMatchingHeaders) {
            set.add(HeaderName.valueOf(nonMatchingHeader));
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
        for (HeaderName name : names) {
            final List<String> list = map.get(name);
            final List<String> otherList = other.map.get(name);
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
        for (HeaderName name : names) {
            final List<String> list = map.get(name);
            result = prime * result + ((list == null) ? 0 : list.hashCode());
        }
        return result;
    }

    /*-
     * ############ UTILITY METHODS ##############
     */

    private static final transient Iterator<Map.Entry<String, String>> EMPTY_ITER = new Iterator<Map.Entry<String, String>>() {

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Entry<String, String> next() {
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            // Nothing to remove
        }
    };

    private static final class HeaderIterator implements Iterator<Map.Entry<String, String>> {

        private final Iterator<Map.Entry<HeaderName, List<String>>> iter;

        private final Set<HeaderName> headers;

        private final boolean matches;

        private int size;

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

        @Override
        public boolean hasNext() {
            if (entry == null || index >= size) {
                while (iter.hasNext()) {
                    entry = iter.next();
                    if (headers == null || (matches ? headers.contains(entry.getKey()) : !headers.contains(entry.getKey()))) {
                        size = entry.getValue().size();
                        index = 0;
                        return true;
                    }
                }
                entry = null;
                return false;
            }
            return (index < size);
        }

        @Override
        public Entry<String, String> next() {
            if (entry == null || index >= size) {
                while (iter.hasNext()) {
                    entry = iter.next();
                    if (headers == null || (matches ? headers.contains(entry.getKey()) : !headers.contains(entry.getKey()))) {
                        size = entry.getValue().size();
                        index = 0;
                        return new HeaderEntry(entry, index++);
                    }
                }
                entry = null;
                throw new NoSuchElementException();
            }
            return new HeaderEntry(entry, index++);
        }

        @Override
        public void remove() {
            if (entry == null) {
                throw new IllegalStateException(
                    new StringBuilder(64).append("next() method has not yet been called, or the remove()").append(
                        " method has already been called after the last call to the next() method.").toString());
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

        @Override
        public String getKey() {
            return entry.getKey().toString();
        }

        @Override
        public String getValue() {
            return entry.getValue().get(index);
        }

        @Override
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
        if (isName) {
            final int length = str.length();
            if (length == 0) {
                return true;
            }
            for (int i = 0; i < length; i++) {
                final char c = str.charAt(i);
                if ((c >= 128) || Strings.isWhitespace(c)) {
                    /*
                     * Whitespace or non-ascii character
                     */
                    return true;
                }
            }
            /*
             * All fine
             */
            return false;
        }
        /*
         * A header value must not be empty
         */
        return com.openexchange.java.Strings.isEmpty(str);
    }

    /**
     * Checks whether the specified string's characters are ASCII 7 bit
     *
     * @param s The string to check
     * @return <code>true</code> if string's characters are ASCII 7 bit; otherwise <code>false</code>
     */
    private static final boolean isAscii(final String s) {
        final int length = s.length();
        boolean isAscci = true;
        for (int i = 0; (i < length) && isAscci; i++) {
            isAscci &= (s.charAt(i) < 128);
        }
        return isAscci;
    }

    private static String checkValue(final String value) {
        if (isAscii(value)) {
            return value;
        }
        final int length = value.length();
        final byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = (byte) value.charAt(i);
        }
        try {
            final String detectCharset = CharsetDetector.detectCharset(new UnsynchronizedByteArrayInputStream(bytes));
            final String unicodeVal = MessageUtility.readStream(new UnsynchronizedByteArrayInputStream(bytes), detectCharset);
            return MimeUtility.encodeWord(unicodeVal, "UTF-8", "Q");
        } catch (final IOException e) {
            // Cannot occur
            return value;
        }
    }

    /**
     * Simple test method
     */
    public static final void test() {
        final Logger log2 = org.slf4j.LoggerFactory.getLogger(HeaderCollection.class);
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
                log2.info(MessageFormat.format("{0}: {1}", e.getKey(), e.getValue()));
                if ("Faust".equals(e.getKey())) {
                    iter.remove();
                }
            }

            log2.info("\n\nAfter removal through iterator");

            final Iterator<Map.Entry<String, String>> iter2 = hc.getAllHeaders();
            while (iter2.hasNext()) {
                final Map.Entry<String, String> e = iter2.next();
                log2.info(MessageFormat.format("{0}: {1}", e.getKey(), e.getValue()));
            }

            log2.info("\n\nNon-Matching");

            final Iterator<Map.Entry<String, String>> iter3 = hc.getNonMatchingHeaders(new String[] { "To", "From" });
            while (iter3.hasNext()) {
                final Map.Entry<String, String> e = iter3.next();
                log2.info(MessageFormat.format("{0}: {1}", e.getKey(), e.getValue()));
            }

            log2.info("\n\nMatching");

            final Iterator<Map.Entry<String, String>> iter4 = hc.getMatchingHeaders(new String[] { "To", "From" });
            while (iter4.hasNext()) {
                final Map.Entry<String, String> e = iter4.next();
                log2.info(MessageFormat.format("{0}: {1}", e.getKey(), e.getValue()));
            }

            log2.info("\n\nEquals");

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

            log2.info(Boolean.toString(hc.equals(hc2)));

        } catch (final Exception e) {
            log2.error("", e);
        }
    }
}
