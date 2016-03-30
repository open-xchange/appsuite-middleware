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

package com.openexchange.charset;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.charset.spi.CharsetProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * {@link CustomCharsetProvider} - A custom charset provider which maps unknown charset names to supported charsets.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CustomCharsetProvider extends CharsetProvider {

    /**
     * The logger instance.
     */
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CustomCharsetProvider.class);

    /**
     * The charset map.
     */
    private static volatile ConcurrentMap<String, Charset> name2charset;

    /**
     * The charset map for startsWith.
     */
    private static volatile ConcurrentMap<String, Charset> startsWith2charset;

    /**
     * Initializes the charset maps.
     */
    public static void initCharsetMap() {
        if (name2charset == null) {
            synchronized (CustomCharsetProvider.class) {
                if (name2charset == null) {
                    name2charset = new ConcurrentHashMap<String, Charset>(8, 0.9f, 1);
                    startsWith2charset = new ConcurrentHashMap<String, Charset>(8, 0.9f, 1);
                }
            }
        }
    }

    /**
     * Frees the charset maps.
     */
    public static void releaseCharsetMap() {
        if (name2charset != null) {
            synchronized (CustomCharsetProvider.class) {
                if (name2charset != null) {
                    name2charset = null;
                    startsWith2charset = null;
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
     * <b>Note</b>: Any instance of {@link CustomCharsetProvider} works on the same charset map.
     */
    public CustomCharsetProvider() {
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
        final Charset charset;
        try {
            charset = Charset.forName(delegateName);
        } catch (final IllegalCharsetNameException e) {
            LOG.error("Illegal charset name \"{}\"", e.getCharsetName(), e);
            return false;
        } catch (final UnsupportedCharsetException e) {
            LOG.error("Detected no support for charset \"{}\"", e.getCharsetName(), e);
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
        String key = aliasCharset.name().toLowerCase();
        final ConcurrentMap<String, Charset> map = name2charset;
        if (null == map) {
            /*
             * Not initialized, yet
             */
            return false;
        }
        boolean added = (null == map.putIfAbsent(key, aliasCharset));
        // Add aliases one-by-one
        final Set<String> aliasSet = aliasCharset.aliases();
        if (added && !aliasSet.isEmpty()) {
            final List<String> addedKeys = new ArrayList<String>(aliasSet.size() + 1);
            for (final Iterator<String> iter = aliasSet.iterator(); added && iter.hasNext();) {
                addedKeys.add(key);
                key = iter.next().toLowerCase();
                added = (null == map.putIfAbsent(key, aliasCharset));
            }
            if (!added) {
                // Charset could not be added with all its aliases
                for (final String removeMe : addedKeys) {
                    map.remove(removeMe);
                }
            }
        }
        // Indicate if charset was added
        return added;
    }

    /**
     * Adds a {@link StartsWithCharset starts-with charset} to this charset provider.
     * <p>
     * If charset look-up for given <tt>delegateName</tt> throws an {@link IllegalCharsetNameException} or an
     * {@link UnsupportedCharsetException}, <code>false</code> is returned and exception is logged.
     *
     * @param delegateName The name of the delegate charset; e.g. <code>&quot;UTF-8&quot;</code>
     * @param startsWithName The starts-with name of the starts-with charset; e.g. <code>&quot;UTF_8&quot;</code>
     * @return <code>true</code> if an appropriate starts-with charset could be added to this provider; otherwise <code>false</code>
     */
    public boolean addStartsWithCharset(final String delegateName, final String startsWithName) {
        /*
         * Look-up charset
         */
        final Charset charset;
        try {
            charset = Charset.forName(delegateName);
        } catch (final IllegalCharsetNameException e) {
            LOG.error("Illegal charset name \"{}\"", e.getCharsetName(), e);
            return false;
        } catch (final UnsupportedCharsetException e) {
            LOG.error("Detected no support for charset \"{}\"", e.getCharsetName(), e);
            return false;
        }
        return addStartsWithCharset(charset, startsWithName);
    }

    /**
     * Adds a {@link StartsWithCharset starts-with charset} to this charset provider.
     *
     * @param delegate The delegate charset
     * @param startsWithName The starts-with name of the starts-with charset; e.g. <code>&quot;UTF_8&quot;</code>
     * @return <code>true</code> if an appropriate starts-with charset could be added to this provider; otherwise <code>false</code>
     */
    public boolean addStartsWithCharset(final Charset delegate, final String startsWithName) {
        final ConcurrentMap<String, Charset> startsWithMap = startsWith2charset;
        if (null == startsWithMap) {
            return false;
        }
        final StartsWithCharset startsWithCharset = new StartsWithCharset(startsWithName, delegate);
        return (null == startsWithMap.putIfAbsent(startsWithName.toLowerCase(), startsWithCharset));
    }

    /**
     * Retrieves a charset for the given charset name.
     *
     * @param charsetName The name of the requested charset; may be either a canonical name or an alias
     * @return A charset object for the named charset, or <tt>null</tt> if the named charset is not supported by this provider
     */
    @Override
    public Charset charsetForName(final String charsetName) {
        /*
         * Get charset instance for given name (case insensitive)
         */
        final ConcurrentMap<String, Charset> map = name2charset;
        if (null == map) {
            /*
             * Not initialized, yet
             */
            return null;
        }
        Charset retval = map.get(charsetName.toLowerCase());
        if (null != retval) {
            // Direct hit
            return retval;
        }
        /*
         * Traverse starts-with charsets
         */
        final ConcurrentMap<String, Charset> startsWithMap = startsWith2charset;
        if (!startsWithMap.isEmpty()) {
            for (final Iterator<Map.Entry<String, Charset>> iter = startsWithMap.entrySet().iterator(); (null == retval) && iter.hasNext();) {
                final Map.Entry<String, Charset> entry = iter.next();
                if (charsetName.toLowerCase().startsWith(entry.getKey())) {
                    retval = entry.getValue();
                }
            }
        }
        return retval;
    }

    /**
     * Creates an iterator that iterates over the charsets supported by this provider. This method is used in the implementation of the
     * {@link java.nio.charset.Charset#availableCharsets Charset.availableCharsets} method.
     *
     * @return The new iterator with the <tt>remove()</tt> functionality stripped.
     */
    @Override
    public Iterator<Charset> charsets() {
        final ConcurrentMap<String, Charset> map = name2charset;
        if (null == map) {
            /*
             * Not initialized, yet
             */
            return Collections.<Charset> emptyList().iterator();
        }
        final Set<Charset> set = new HashSet<Charset>(map.values());
        set.addAll(startsWith2charset.values());
        return unmodifiableIterator(set.iterator());
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
}
