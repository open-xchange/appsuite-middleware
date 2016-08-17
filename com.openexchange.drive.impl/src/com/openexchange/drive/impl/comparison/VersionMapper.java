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

package com.openexchange.drive.impl.comparison;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import com.openexchange.drive.DriveVersion;
import com.openexchange.drive.impl.internal.PathNormalizer;
import com.openexchange.drive.impl.internal.Tracer;
import com.openexchange.java.Strings;


/**
 * {@link VersionMapper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @param <T>
 */
public abstract class VersionMapper<T extends DriveVersion> implements Iterable<Map.Entry<String, ThreeWayComparison<T>>> {

    private final Collection<? extends T> originalVersions;
    private final Collection<? extends T> clientVersions;
    private final Collection<? extends T> serverVersions;
    private final SortedMap<String, ThreeWayComparison<T>> map;
    private final MappingProblems<T> mappingProblems;

    /**
     * Initializes a new {@link VersionMapper} using collections of original-, client- and server versions.
     *
     * @param originalVersions The original, i.e. previously known versions
     * @param clientVersions The current client versions
     * @param serverVersions The current server versions
     */
    public VersionMapper(Collection<? extends T> originalVersions, Collection<? extends T> clientVersions, Collection<? extends T> serverVersions) {
        super();
        this.map = new TreeMap<String, ThreeWayComparison<T>>(String.CASE_INSENSITIVE_ORDER);
        this.mappingProblems = new MappingProblems<T>();
        if (null == originalVersions) {
            this.originalVersions = Collections.emptyList();
        } else {
            this.originalVersions = originalVersions;
            for (T originalVersion : originalVersions) {
                String normalizedKey = PathNormalizer.normalize(getKey(originalVersion));
                ThreeWayComparison<T> twc = getOrCreate(normalizedKey);
                if (null == twc.getOriginalVersion()) {
                    twc.setOriginalVersion(originalVersion);
                }
            }
        }
        if (null == clientVersions) {
            this.clientVersions = Collections.emptyList();
        } else {
            this.clientVersions = clientVersions;
            for (T clientVersion : clientVersions) {
                String key = getKey(clientVersion);
                String normalizedKey = PathNormalizer.normalize(key);
                ThreeWayComparison<T> comparison = getOrCreate(normalizedKey);
                T existingVersion = comparison.getClientVersion();
                if (null != existingVersion) {
                    /*
                     * case / normalization conflict - choose version to use
                     */
                    String existingKey = getKey(existingVersion);
                    comparison.setClientVersion(mappingProblems.chooseClientVersion(existingVersion, existingKey, clientVersion, key));
                } else {
                    comparison.setClientVersion(clientVersion);
                }
            }
        }
        if (null == serverVersions) {
            this.serverVersions = Collections.emptyList();
        } else {
            this.serverVersions = serverVersions;
            for (T serverVersion : serverVersions) {
                String key = getKey(serverVersion);
                String normalizedKey = PathNormalizer.normalize(key);
                ThreeWayComparison<T> comparison = getOrCreate(normalizedKey);
                T existingVersion = comparison.getServerVersion();
                if (null != existingVersion) {
                    /*
                     * case / normalization conflict - choose version to use
                     */
                    String existingKey = getKey(existingVersion);
                    comparison.setServerVersion(mappingProblems.chooseServerVersion(existingVersion, existingKey, serverVersion, key));
                } else {
                    comparison.setServerVersion(serverVersion);
                }
            }
        }
    }

    @Override
    public Iterator<Map.Entry<String, ThreeWayComparison<T>>> iterator() {
        return map.entrySet().iterator();
    }

    /**
     * Gets a set of all keys that were mapped to versions.
     *
     * @return The key names
     */
    public Set<String> getKeys() {
        return Collections.unmodifiableSet(map.keySet());
    }

    /**
     * Gets the three way comparison mapped to the supplied key.
     *
     * @param key The key name
     * @return The comparison, or <code>null</code> if none exists
     */
    public ThreeWayComparison<T> get(String key) {
        return map.get(key);
    }

    /**
     * Gets the original versions
     *
     * @return The original versions
     */
    public Collection<? extends T> getOriginalVersions() {
        return originalVersions;
    }

    /**
     * Gets the client versions
     *
     * @return The client versions
     */
    public Collection<? extends T> getClientVersions() {
        return clientVersions;
    }

    /**
     * Gets the server versions
     *
     * @return The server versions
     */
    public Collection<? extends T> getServerVersions() {
        return serverVersions;
    }

    /**
     * Gets any client- and server-versions that were causing a conflict, i.e. versions that would have been mapped to an already used key.
     *
     * @return The mapping problems
     */
    public MappingProblems<? extends T> getMappingProblems() {
        return mappingProblems;
    }

    /**
     * Gets the key used to map the supplied version.
     *
     * @param version The version
     * @return The key name
     */
    protected abstract String getKey(T version);

    private ThreeWayComparison<T> getOrCreate(String key) {
        ThreeWayComparison<T> comparison = map.get(key);
        if (null == comparison) {
            comparison = new ThreeWayComparison<T>();
            map.put(key, comparison);
        }
        return comparison;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("                                         | Original Version                 | Client Version                     | Server Version                    \n");
        stringBuilder.append("-----------------------------------------+----------------------------------+------------------------------------+-----------------------------------\n");
        for (Entry<String, ThreeWayComparison<T>> entry : this) {
            String name = Strings.abbreviate(entry.getKey(), entry.getKey().length() + 1, 40);
            ThreeWayComparison<T> comparison = entry.getValue();
            stringBuilder.append(name);
            for (int i = 0; i < 40 - name.length(); i++) {
                stringBuilder.append(' ');
            }
            stringBuilder.append(" | ");
            stringBuilder.append(null != comparison.getOriginalVersion() ? comparison.getOriginalVersion().getChecksum() : "                                ");
            stringBuilder.append(" | ");
            stringBuilder.append(null != comparison.getClientVersion() ? comparison.getClientVersion().getChecksum() : "                                ");
            stringBuilder.append(Change.NONE == comparison.getClientChange() ? "  " : ' ' + comparison.getClientChange().toString().substring(0, 1));
            stringBuilder.append(" | ");
            stringBuilder.append(null != comparison.getServerVersion() ? comparison.getServerVersion().getChecksum() : "                                ");
            stringBuilder.append(Change.NONE == comparison.getServerChange() ? "  " : ' ' + comparison.getServerChange().toString().substring(0, 1));
            stringBuilder.append('\n');
            if (Tracer.MAX_SIZE < stringBuilder.length()) {
                stringBuilder.append('\n').append("[...]");
                break;
            }
        }
        stringBuilder.append(mappingProblems);
        return stringBuilder.toString();
    }

}
