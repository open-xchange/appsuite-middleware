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

package com.openexchange.charset.internal;

import static com.openexchange.java.Strings.asciiLowerCase;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.spi.CharsetProvider;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.java.Strings;

/**
 * {@link <code>CollectionCharsetProvider</code>} - A charset provider which performs the
 * {@link CollectionCharsetProvider#charsetForName(String)} and {@link CollectionCharsetProvider#charsets()} method invocations by iterating
 * over collected charset providers.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CollectionCharsetProvider extends CharsetProvider {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CollectionCharsetProvider.class);

    private final AtomicReference<Map<String, Charset>> charsetMapReference;
    private final Map<Class<? extends CharsetProvider>, CharsetProvider> providerList;
    private final AtomicBoolean gathered;

    /**
     * Initializes a new {@link <code>CollectionCharsetProvider</code>}
     */
    public CollectionCharsetProvider() {
        super();
        providerList = new ConcurrentHashMap<Class<? extends CharsetProvider>, CharsetProvider>(3);
        charsetMapReference = new AtomicReference<Map<String, Charset>>(Collections.<String, Charset> emptyMap());
        gathered = new AtomicBoolean(false);
    }

    /**
     * Initializes a new {@link <code>CollectionCharsetProvider</code>} with specified instance of {@link <code>CharsetProvider</code>}
     *
     * @param provider The charset provider to be initially added
     */
    public CollectionCharsetProvider(final CharsetProvider provider) {
        this();
        providerList.put(provider.getClass(), provider);
    }

    /**
     * Initializes a new {@link <code>CollectionCharsetProvider</code>} with specified collection of {@link <code>CharsetProvider</code>}
     * instances
     *
     * @param providers The charset provider collection to be initially added
     */
    public CollectionCharsetProvider(final Collection<CharsetProvider> providers) {
        this();
        for (final Iterator<CharsetProvider> iterator = providers.iterator(); iterator.hasNext();) {
            final CharsetProvider charsetProvider = iterator.next();
            providerList.put(charsetProvider.getClass(), charsetProvider);
        }
    }

    /**
     * Adds an instance of {@link <code>CharsetProvider</code>} to this provider's collection
     *
     * @param charsetProvider The charset provider to add
     */
    public void addCharsetProvider(final CharsetProvider charsetProvider) {
        providerList.put(charsetProvider.getClass(), charsetProvider);
        gathered.set(false);
    }

    /**
     * Removes given charset provider from this charset provider's collection
     *
     * @param provider The provider which shall be removed
     * @return The removed charset provider or <code>null</code> if none present
     */
    public CharsetProvider removeCharsetProvider(final CharsetProvider provider) {
        return removeCharsetProvider(provider.getClass());
    }

    /**
     * Removes the charset provider denoted by specified class argument from this charset provider's collection
     *
     * @param clazz The class of the charset provider which shall be removed
     * @return The removed charset provider or <code>null</code> if no collected charset provider is denoted by given class argument
     */
    public CharsetProvider removeCharsetProvider(final Class<? extends CharsetProvider> clazz) {
        CharsetProvider retval = providerList.remove(clazz);
        if (null != retval) {
            gathered.set(false);
        }
        return retval;
    }

    @Override
    public Charset charsetForName(final String charsetName) {
        if (Strings.isEmpty(charsetName)) {
            throw new IllegalCharsetNameException(charsetName);
        }
        gatherProviderCharsetsIfNeeded();
        Map<String, Charset> currentCharsetMap = charsetMapReference.get();
        Charset charset = currentCharsetMap.get(asciiLowerCase(charsetName));
        if (charset == null) {
            if (charsetName.charAt(0) == '\'') {
                String unquoted = unquote(charsetName, true);
                if (unquoted != null) {
                    charset = currentCharsetMap.get(asciiLowerCase(unquoted));
                }
            } else if (charsetName.charAt(0) == '"') {
                String unquoted = unquote(charsetName, false);
                if (unquoted != null) {
                    charset = currentCharsetMap.get(asciiLowerCase(unquoted));
                }
            }
        }
        return charset;
    }

    /**
     * Removes single or double quotes from charset name.
     *
     * @param charsetName The charset name to be unquoted
     * @param singleQuote Whether charset name starts with a single quote or double quote
     * @return The unquoted charset name or <code>null</code>
     */
    private static String unquote(String charsetName, boolean singleQuote) {
        return charsetName.endsWith(singleQuote ? "'" : "\"") ? charsetName.substring(1, charsetName.length() - 1) : null;
    }

    @Override
    public Iterator<Charset> charsets() {
        gatherProviderCharsetsIfNeeded();
        return charsetMapReference.get().values().iterator();
    }

    private void gatherProviderCharsetsIfNeeded() {
        if (!gathered.get()) {
            synchronized (this) {
                if (!gathered.get()) {
                    Map<String, Charset> charsetMap = new HashMap<>();
                    for (CharsetProvider provider : providerList.values()) {
                        for (Iterator<Charset> iter = provider.charsets(); iter.hasNext();) {
                            // Put by charset name
                            Charset cs = iter.next();
                            String name = asciiLowerCase(cs.name());
                            if (charsetMap.containsKey(name)) {
                                LOG.debug("Discarding duplicate charset: {}", name);
                            } else {
                                charsetMap.put(name, cs);
                            }

                            // Check charset's aliases
                            for (String aliaz : cs.aliases()) {
                                // Put by charset alias
                                String alias = asciiLowerCase(aliaz);
                                if (charsetMap.containsKey(alias)) {
                                    LOG.debug("Discarding duplicate charset: {}", alias);
                                } else {
                                    charsetMap.put(alias, cs);
                                }
                            }
                        }
                    }
                    charsetMapReference.set(charsetMap);
                    gathered.set(true);
                }
            }
        }
    }

}
