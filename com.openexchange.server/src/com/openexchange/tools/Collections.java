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

package com.openexchange.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Methods for easy handling of collections. TODO use Collections in com.openexchange.tools.arrays.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Collections {

    /**
     * Logger.
     */
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Collections.class);

    /**
     * Prevent instantiation
     */
    private Collections() {
        super();
    }

    public static <K, V> V opt(ConcurrentHashMap<K, V> map, K key, V defaultValue) {
        V v = map.get(key);
        if (v == null) {
            v = defaultValue;
            V meantime = map.putIfAbsent(key, v);
            v = (meantime == null) ? v : meantime;
        }

        return v;
    }

    /**
     * Strips the <tt>remove()</tt> functionality from an existing iterator.
     * <p>
     * Wraps the supplied iterator into a new one that will always throw an <tt>UnsupportedOperationException</tt> if its <tt>remove()</tt>
     * method is called.
     *
     * @param iterator The iterator to turn into an unmodifiable iterator.
     * @return An iterator with no remove functionality.
     */
    public static <T> Iterator<T> unmodifiableIterator(final Iterator<T> iterator) {
        if (iterator == null) {
            throw new NullPointerException();
        }

        return new Iterator<T>() {

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public T next() {
                return iterator.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Creates a new {@link HashMap} instance.
     * <p>
     * Convenience method to substitute<br>
     * <i> Map&lt;String, String&gt; m = new HashMap&lt;String, String&gt;(); </i><br>
     * with<br>
     * <i> Map&lt;String, String&gt; m = newHashMap(); </i>
     *
     * @param <K> The key instances' type
     * @param <V> The value instances' type
     * @return A new {@link HashMap} instance
     */
    public static <K, V> HashMap<K, V> newHashMap() {
        return new HashMap<K, V>();
    }

    /**
     * Creates a new {@link HashMap} instance.
     * <p>
     * Convenience method to substitute<br>
     * <i> Map&lt;String, String&gt; m = new HashMap&lt;String, String&gt;(initialCapacity); </i><br>
     * with<br>
     * <i> Map&lt;String, String&gt; m = newHashMap(initialCapacity); </i>
     *
     * @param <K> The key instances' type
     * @param <V> The value instances' type
     * @param initialCapacity The initial capacity
     * @return A new {@link HashMap} instance
     */
    public static <K, V> HashMap<K, V> newHashMap(final int initialCapacity) {
        return new HashMap<K, V>(initialCapacity);
    }

    /**
     * Finds the first element in a collection that satisfies the filter
     *
     * @return
     */
    public static <T> T findFirst(final Collection<T> collection, final Filter<T> filter) {
        for (final T object : collection) {
            if (filter.accept(object)) {
                return object;
            }
        }
        return null;
    }

    /**
     * Adds all elements from input that satisfy the filter to output
     */
    public static <T> void collect(final Collection<T> input, final Filter<T> filter, final Collection<T> output) {
        for (final T object : input) {
            if (filter.accept(object)) {
                output.add(object);
            }
        }
    }

    /**
     * Checks whether there is one element within the array that matches one of the given ones
     */
    public static <T> boolean any(final Collection<T> haystack, T... needles){
        for(T hay: haystack){
            for(T needle: needles){
                if(needle == null){
                    if(hay == null) {
                        return true;
                    }
                    continue;
                }
                if(needle.equals(hay)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Converts a list of Integer into an int array.
     *
     * @param col list to convert.
     * @return the converted int array.
     */
    public static int[] toArray(final Collection<Integer> col) {
        final int[] retval = new int[col.size()];
        final Iterator<Integer> iter = col.iterator();
        for (int i = 0; i < retval.length; i++) {
            retval[i] = iter.next().intValue();
        }
        return retval;
    }

    /**
     * ByteArrayOutputStream implementation that does not synchronize methods and does not copy the data on toByteArray().
     */
    public static class FastByteArrayOutputStream extends OutputStream {

        /**
         * Buffer and size
         */
        protected byte[] buf;

        protected int size;

        /**
         * Constructs a stream with buffer capacity size 5K
         */
        public FastByteArrayOutputStream() {
            this(5 << 10);
        }

        /**
         * Constructs a stream with the given initial size
         */
        public FastByteArrayOutputStream(final int initSize) {
            size = 0;
            buf = new byte[initSize];
        }

        /**
         * Ensures that we have a large enough buffer for the given size.
         */
        private final void verifyBufferSize(final int sz) {
            if (sz > buf.length) {
                final byte[] old = buf;
                buf = new byte[Math.max(sz, 2 * buf.length)];
                System.arraycopy(old, 0, buf, 0, old.length);
            }
        }

        public int getSize() {
            return size;
        }

        /**
         * Returns the byte array containing the written data. Note that this array will almost always be larger than the amount of data
         * actually written.
         */
        public byte[] getByteArray() {
            final byte[] retval = new byte[buf.length];
            System.arraycopy(buf, 0, retval, 0, buf.length);
            return retval;
        }

        @Override
        public final void write(final byte b[]) {
            verifyBufferSize(size + b.length);
            System.arraycopy(b, 0, buf, size, b.length);
            size += b.length;
        }

        @Override
        public final void write(final byte b[], final int off, final int len) {
            verifyBufferSize(size + len);
            System.arraycopy(b, off, buf, size, len);
            size += len;
        }

        @Override
        public final void write(final int b) {
            verifyBufferSize(size + 1);
            buf[size++] = (byte) b;
        }

        public void reset() {
            size = 0;
        }

        /**
         * Returns a ByteArrayInputStream for reading back the written data
         */
        public InputStream getInputStream() {
            return new FastByteArrayInputStream(buf, size);
        }

    }

    /**
     * ByteArrayInputStream implementation that does not synchronize methods.
     */
    public static class FastByteArrayInputStream extends InputStream {

        /**
         * Our byte buffer
         */
        protected byte[] buf;

        /**
         * Number of bytes that we can read from the buffer
         */
        protected int count;

        /**
         * Number of bytes that have been read from the buffer
         */
        protected int pos;

        public FastByteArrayInputStream(final byte[] buf, final int count) {
            this.buf = new byte[buf.length];
            System.arraycopy(buf, 0, this.buf, 0, buf.length);
            this.count = count;
        }

        @Override
        public final int available() {
            return count - pos;
        }

        @Override
        public final int read() {
            return (pos < count) ? (buf[pos++] & 0xff) : -1;
        }

        @Override
        public final int read(final byte[] b, final int off, final int lenArg) {
            if (pos >= count) {
                return -1;
            }
            int len = lenArg;
            if ((pos + len) > count) {
                len = (count - pos);
            }
            System.arraycopy(buf, pos, b, off, len);
            pos += len;
            return len;
        }

        @Override
        public final long skip(final long nArg) {
            long n = nArg;
            if ((pos + n) > count) {
                n = count - pos;
            }
            if (n < 0) {
                return 0;
            }
            pos += n;
            return n;
        }

    }

    /**
     * Returns a copy of the object, or <tt>null</tt> if the object cannot be serialized.
     */
    public static Object copy(final Serializable orig) {
        try {
            /*
             * Write the object out to a byte array
             */
            final FastByteArrayOutputStream fbos = new FastByteArrayOutputStream();
            final ObjectOutputStream out = new ObjectOutputStream(fbos);
            out.writeObject(orig);
            out.flush();
            out.close();
            /*
             * Retrieve an input stream from the byte array and read a copy of the object back in
             */
            final ObjectInputStream in = new ObjectInputStream(fbos.getInputStream());
            return in.readObject();
        } catch (final IOException e) {
            LOG.error("", e);
            return null;
        } catch (final ClassNotFoundException cnfe) {
            LOG.error("", cnfe);
            return null;
        }
    }

    public static <T> Enumeration<T> iter2enum(final Iterator<T> iter) {
        return new IteratorEnumeration<T>(iter);
    }

    private static class IteratorEnumeration<T> implements Enumeration<T> {

        private final Iterator<T> iter;

        public IteratorEnumeration(final Iterator<T> iter) {
            this.iter = iter;
        }

        /*
         * (non-Javadoc)
         * @see java.util.Enumeration#hasMoreElements()
         */
        @Override
        public boolean hasMoreElements() {
            return iter.hasNext();
        }

        /*
         * (non-Javadoc)
         * @see java.util.Enumeration#nextElement()
         */
        @Override
        public T nextElement() {
            return iter.next();
        }
    }

    /**
     * Interface to provide filtering opportunities for collections
     *
     * @author <a href="mailto:francisco.laguna@open-xchange.org">Francisco Laguna</a>
     * @param <T>
     */
    public interface Filter<T> {

        public boolean accept(T object);
    }

}
