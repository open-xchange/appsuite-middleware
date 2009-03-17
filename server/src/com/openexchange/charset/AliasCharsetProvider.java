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

package com.openexchange.charset;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.charset.spi.CharsetProvider;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * {@link AliasCharsetProvider} - An alias charset provider which maps unknown charset names to supported charsets.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AliasCharsetProvider extends CharsetProvider {

    /**
     * The logger instance.
     */
    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(AliasCharsetProvider.class);

    /**
     * The charset map.
     */
    private static volatile ConcurrentMap<String, Charset> name2charset;

    /**
     * Initializes the charset map.
     */
    public static void initCharsetMap() {
        if (name2charset == null) {
            synchronized (AliasCharsetProvider.class) {
                if (name2charset == null) {
                    name2charset = new ConcurrentHashMap<String, Charset>(8);
                }
            }
        }
    }

    /**
     * Frees the charset map.
     */
    public static void releaseCharsetMap() {
        if (name2charset != null) {
            synchronized (AliasCharsetProvider.class) {
                if (name2charset != null) {
                    name2charset = null;
                }
            }
        }
    }

    /*-
     * ++++++++++++++++++++++++++++++++++++++++++++++ MEMBER SECTION ++++++++++++++++++++++++++++++++++++++++++++++
     */

    /**
     * Default constructor.
     * <p>
     * <b>Note</b>: Any instance of {@link AliasCharsetProvider} works on the same charset map.
     */
    public AliasCharsetProvider() {
        super();
    }

    /**
     * Adds an {@link AliasCharset alias charset} to this charset provider.
     * <p>
     * If charset look-up for given <tt>delegateName</tt> throws an {@link IllegalCharsetNameException} or an
     * {@link UnsupportedCharsetException}, <code>false</code> is returned and exception is logged.
     * 
     * @param delegateName The name of the delegate charset; e.g. <code>&quot;UTF-8&quot;</code>
     * @param canonicalName The canonical name of the alias charset; e.g. <code>&quot;UTF_8&quot;</code>
     * @param aliases The aliases of the alias charset
     * @return <code>true</code> if an appropriate alias charset could be added to this provider; otherwise <code>false</code>
     */
    public boolean addAliasCharset(final String delegateName, final String canonicalName, final String... aliases) {
        /*
         * Look-up charset
         */
        Charset charset = null;
        try {
            charset = Charset.forName(delegateName);
        } catch (final IllegalCharsetNameException e) {
            LOG.error(new StringBuilder("Illegal charset name \"").append(e.getCharsetName()).append('"').toString(), e);
            return false;
        } catch (final UnsupportedCharsetException e) {
            LOG.error(new StringBuilder("Detected no support for charset \"").append(e.getCharsetName()).append('"').toString(), e);
            return false;
        }
        return addAliasCharset(charset, canonicalName, aliases);
    }

    /**
     * Adds an {@link AliasCharset alias charset} to this charset provider.
     * 
     * @param delegate The delegate charset
     * @param canonicalName The canonical name of the alias charset; e.g. <code>&quot;UTF_8&quot;</code>
     * @param aliases The aliases of the alias charset
     * @return <code>true</code> if an appropriate alias charset could be added to this provider; otherwise <code>false</code>
     */
    public boolean addAliasCharset(final Charset delegate, final String canonicalName, final String... aliases) {
        final AliasCharset aliasCharset = new AliasCharset(canonicalName, null == aliases || aliases.length == 0 ? null : aliases, delegate);
        return (name2charset.putIfAbsent(aliasCharset.name().toLowerCase(), aliasCharset) == null);
    }

    /**
     * Retrieves a charset for the given charset name. </p>
     * 
     * @param charsetName The name of the requested charset; may be either a canonical name or an alias
     * @return A charset object for the named charset, or <tt>null</tt> if the named charset is not supported by this provider
     */
    @Override
    public Charset charsetForName(final String charsetName) {
        /*
         * Get charset instance for given name (case insensitive)
         */
        return name2charset.get(charsetName.toLowerCase());
    }

    /**
     * Creates an iterator that iterates over the charsets supported by this provider. This method is used in the implementation of the
     * {@link java.nio.charset.Charset#availableCharsets Charset.availableCharsets} method. </p>
     * 
     * @return The new iterator with the <tt>remove()</tt> functionality stripped.
     */
    @Override
    public Iterator<Charset> charsets() {
        return unmodifiableIterator(name2charset.values().iterator());
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
    private static <T> Iterator<T> unmodifiableIterator(final Iterator<T> iterator) {
        if (iterator == null) {
            throw new NullPointerException();
        }

        return new Iterator<T>() {

            public boolean hasNext() {
                return iterator.hasNext();
            }

            public T next() {
                return iterator.next();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
