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

package com.openexchange.i18n.tools;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * {@link RenderMap} - A map containing replacements for tokens to render a {@link Template template}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class RenderMap implements Cloneable {

    private EnumMap<TemplateToken, TemplateReplacement> map;

    /**
     * Initializes a new {@link RenderMap}
     */
    public RenderMap() {
        super();
        map = new EnumMap<TemplateToken, TemplateReplacement>(TemplateToken.class);
    }

    /**
     * Initializes a new {@link RenderMap} from given replacements
     *
     * @param replacements The replacements
     */
    public RenderMap(final TemplateReplacement... replacements) {
        this();
        for (final TemplateReplacement replacement : replacements) {
            put(replacement);
        }
    }

    /**
     * Gets those {@link TemplateReplacement}s that are marked as changed.
     *
     * @return The changed {@link TemplateReplacement}s
     */
    public List<TemplateReplacement> getChanges() {
        final List<TemplateReplacement> changes = new ArrayList<TemplateReplacement>(map.size() >> 1);
        for (final TemplateReplacement templateReplacement : map.values()) {
            if (templateReplacement.changed()) {
                changes.add(templateReplacement);
            }
        }
        return changes;
    }

    /**
     * Removes all mappings from this map.
     */
    public void clear() {
        map.clear();
    }

    /**
     * Returns <code>true</code> if this map contains no token-replacement mappings.
     *
     * @return <code>true</code> if this map contains no token-replacement mappings; otherwise <code>false</code>
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Returns the number of token-replacement mappings in this map.
     *
     * @return The number of token-replacement mappings in this map.
     */
    public int size() {
        return map.size();
    }

    /**
     * Associates the specified replacement with the specified token in this map. If the map previously contained a mapping for this token,
     * the old replacement is replaced.
     *
     * @param replacement The replacement to put into this map
     * @return The previous replacement associated with specified token, or <code>null</code> if there was no mapping for token. (A
     *         <code>null</code> return can also indicate that the map previously associated <code>null</code> with the specified token.)
     */
    public TemplateReplacement put(final TemplateReplacement replacement) {
        return map.put(replacement.getToken(), replacement);
    }

    /**
     * Returns the replacement to which this map maps the specified token, or <code>null</code> if this map contains no mapping for the
     * specified token.
     *
     * @param token The token whose associated value is to be returned
     * @return The replacement to which this map maps the specified token, or <code>null</code> if this map contains no mapping for the
     *         specified token.
     */
    public TemplateReplacement get(final TemplateToken token) {
        return map.get(token);
    }

    /**
     * Returns the replacement to which this map maps the specified token, or <code>null</code> if this map contains no mapping for the
     * specified token.
     *
     * @param tokenStr The token whose associated value is to be returned
     * @return The replacement to which this map maps the specified token, or <code>null</code> if this map contains no mapping for the
     *         specified token.
     */
    public TemplateReplacement get(final String tokenStr) {
        final TemplateToken token = TemplateToken.getByString(tokenStr);
        if (token == null) {
            return null;
        }
        return map.get(token);
    }

    /**
     * Returns <code>true</code> if this map contains a mapping for the specified token.
     *
     * @param token The token whose presence in this map is to be tested
     * @return <code>true</code> if this map contains a mapping for the specified token
     */
    public boolean contains(final TemplateToken token) {
        return map.containsKey(token);
    }

    /**
     * Returns an iterator over the tokens in this map. The template tokens are returned in no particular order.
     *
     * @return An iterator over the tokens in this map.
     */
    public Iterator<TemplateToken> getKeys() {
        return map.keySet().iterator();
    }

    /**
     * Returns an iterator over the replacements in this map. The replacements are returned in no particular order.
     *
     * @return An iterator over the replacements in this map.
     */
    public Iterator<TemplateReplacement> getValues() {
        return map.values().iterator();
    }

    /**
     * Removes the mapping for specified token from this map if present.
     *
     * @param token The token whose mapping is to be removed from the map
     * @return The previous replacement associated with specified token, or <code>null</code> if there was no entry for token. (A
     *         <code>null</code> return can also indicate that the map previously associated <code>null</code> with the specified token.)
     */
    public TemplateReplacement remove(final TemplateToken token) {
        return map.remove(token);
    }

    /**
     * Applies specified changed status to all contained token-replacement mappings.
     *
     * @param changed The changed status to apply
     * @return This render map with specified changed status applied to all contained token-replacement mappings.
     */
    public RenderMap applyChangedStatus(final boolean changed) {
        final Iterator<TemplateReplacement> iter = map.values().iterator();
        final int size = map.size();
        for (int i = 0; i < size; i++) {
            iter.next().setChanged(changed);
        }
        return this;
    }

    /**
     * Applies specified locale to all contained token-replacement mappings.
     *
     * @param locale The locale to apply
     * @return This render map with specified locale applied to all contained token-replacement mappings.
     */
    public RenderMap applyLocale(final Locale locale) {
        final Iterator<TemplateReplacement> iter = map.values().iterator();
        final int size = map.size();
        for (int i = 0; i < size; i++) {
            iter.next().setLocale(locale);
        }
        return this;
    }

    /**
     * Applies specified time zone to all contained token-replacement mappings.
     *
     * @param timeZone The time zone to apply
     * @return This render map with specified time zone applied to all contained token-replacement mappings.
     */
    public RenderMap applyTimeZone(final TimeZone timeZone) {
        final Iterator<TemplateReplacement> iter = map.values().iterator();
        final int size = map.size();
        for (int i = 0; i < size; i++) {
            iter.next().setTimeZone(timeZone);
        }
        return this;
    }

    @Override
    public Object clone() {
        try {
            final RenderMap clone = (RenderMap) super.clone();
            clone.map = new EnumMap<TemplateToken, TemplateReplacement>(TemplateToken.class);
            final Iterator<TemplateReplacement> iter = map.values().iterator();
            final int size = map.size();
            for (int i = 0; i < size; i++) {
                clone.put(iter.next().getClone());
            }
            return clone;
        } catch (final CloneNotSupportedException e) {
            throw new InternalError(e.getMessage());
        }
    }

    /**
     * Merges this render map's token-replacement mappings with specified render map's token-replacement mappings.
     *
     * @param other The other render map to merge with
     * @return This render map merged with specified render map
     */
    public RenderMap merge(final RenderMap other) {
        final Iterator<TemplateReplacement> iter = this.map.values().iterator();
        final int size = this.map.size();
        final EnumMap<TemplateToken, TemplateReplacement> otherMap = other.map;
        for (int i = 0; i < size; i++) {
            final TemplateReplacement replacement = iter.next();
            replacement.merge(otherMap.get(replacement.getToken()));
        }
        return this;
    }
}
