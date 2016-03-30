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

package com.openexchange.conversion;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link DataProperties} - Container for data properties like content type, version, character set, name , size, etc.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DataProperties implements Cloneable {

    /**
     * Property for content-type
     */
    public static final String PROPERTY_CONTENT_TYPE = "com.openexchange.conversion.content-type";

    /**
     * Property for version
     */
    public static final String PROPERTY_VERSION = "com.openexchange.conversion.version";

    /**
     * Property for charset
     */
    public static final String PROPERTY_CHARSET = "com.openexchange.conversion.charset";

    /**
     * Property for name
     */
    public static final String PROPERTY_NAME = "com.openexchange.conversion.name";

    /**
     * Property for size
     */
    public static final String PROPERTY_SIZE = "com.openexchange.conversion.size";

    /**
     * Property for disposition
     */
    public static final String PROPERTY_DISPOSITION = "com.openexchange.conversion.disposition";

    /**
     * Property for E-Mail header prefix.
     */
    public static final String PROPERTY_EMAIL_HEADER_PREFIX= "com.openexchange.conversion.email.header";

    /**
     * Property for identifier
     */
    public static final String PROPERTY_ID = "com.openexchange.conversion.id";

    /**
     * Property for folder identifier.
     */
    public static final String PROPERTY_FOLDER_ID = "com.openexchange.conversion.folderId";

    /**
     * Constant for empty data arguments
     */
    public static final DataProperties EMPTY_PROPS = new DataProperties(true, 0);

    private Map<String, String> map;

    /**
     * Initializes a new {@link DataProperties} with the default initial capacity (4).
     */
    public DataProperties() {
        this(false, 4);
    }

    /**
     * Initializes a new {@link DataProperties}
     *
     * @param initialCapacity The initial capacity
     */
    public DataProperties(final int initialCapacity) {
        this(false, initialCapacity);
    }

    private DataProperties(final boolean empty, final int initialCapacity) {
        super();
        if (empty) {
            map = Collections.unmodifiableMap(new HashMap<String, String>(initialCapacity));
        } else {
            map = new HashMap<String, String>(initialCapacity);
        }
    }

    /**
     * Returns <code>true</code> if these data properties contain a mapping for the specified key.
     *
     * @param key The key whose presence in these data properties is to be tested.
     * @return <code>true</code> if this data properties contain a mapping for the specified key; otherwise <code>false</code>
     */
    public boolean containsKey(final String key) {
        return map.containsKey(key);
    }

    /**
     * Returns the value for the specified key. Returns <code>null</code> if the data properties contain no mapping for this key. A return
     * value of <code>null</code> does not necessarily indicate that the data properties contain no mapping for the key; it's also possible
     * that the data properties explicitly map the key to <code>null</code>. The {@link #containsKey(String)} operation may be used to
     * distinguish these two cases.
     *
     * @param key The key whose associated value is to be returned.
     * @return The value to for the specified key, or <code>null</code> if the data properties contain no mapping for this key.
     */
    public String get(final String key) {
        return map.get(key);
    }

    /**
     * Associates the specified value with the specified key. If the data properties previously contained a mapping for this key, the old
     * value is replaced by the specified value.
     *
     * @param key The key with which the specified value is to be associated.
     * @param value The value to be associated with the specified key.
     * @return The previous value associated with specified key, or <code>null</code> if there was no mapping for key. A <code>null</code>
     *         return can also indicate that the data properties previously associated <code>null</code> with the specified key.
     */
    public String put(final String key, final String value) {
        if (null == value) {
            /*
             * Consider as a remove
             */
            return map.remove(key);
        }
        return map.put(key, value);
    }

    /**
     * Removes the mapping for this key from these data properties if it is present.
     *
     * @param key The key whose mapping is to be removed from the data properties.
     * @return The previous value associated with specified key, or <code>null</code> if there was no mapping for key.
     */
    public String remove(final String key) {
        return map.remove(key);
    }

    /**
     * Gets this data properties as a {@link Map java.util.Map}
     *
     * @return This data properties as a {@link Map java.util.Map}
     */
    public Map<String, String> toMap() {
        if (map.isEmpty()) {
            return Collections.emptyMap();
        }
        return new HashMap<String, String>(map);
    }

    @Override
    public Object clone() {
        try {
            final DataProperties clone = (DataProperties) super.clone();
            clone.map = new HashMap<String, String>(map.size());
            clone.map.putAll(map);
            return clone;
        } catch (final CloneNotSupportedException e) {
            // Cannot occur
            throw new InternalError("CloneNotSupportedException although Cloneable is implemented");
        }
    }
}
