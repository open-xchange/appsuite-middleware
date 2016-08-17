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

import java.util.ArrayList;
import java.util.List;
import com.openexchange.drive.impl.internal.PathNormalizer;


/**
 * {@link MappingProblems}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @param <T>
 */
public class MappingProblems<T> {

    private List<T> caseConflictingClientVersions;
    private List<T> unicodeConflictingClientVersions;
    private List<T> duplicateClientVersions;
    private List<T> caseConflictingServerVersions;
    private List<T> unicodeConflictingServerVersions;
    private List<T> duplicateServerVersions;

    /**
     * Initializes a new {@link MappingProblems}.
     */
    public MappingProblems() {
        super();
    }

    /**
     * Gets a value indicating whether there are recorded mapping problems or not.
     *
     * @return <code>true</code> if there are no conflicting version, <code>false</code>, otherwise
     */
    public boolean isEmpty() {
        return (null == caseConflictingClientVersions || 0 == caseConflictingClientVersions.size()) &&
            (null == unicodeConflictingClientVersions || 0 == unicodeConflictingClientVersions.size()) &&
            (null == duplicateClientVersions || 0 == duplicateClientVersions.size()) &&
            (null == caseConflictingServerVersions || 0 == caseConflictingServerVersions.size()) &&
            (null == unicodeConflictingServerVersions || 0 == unicodeConflictingServerVersions.size() &&
            (null == duplicateServerVersions || 0 == duplicateServerVersions.size())
        );
    }

    /**
     * Gets the caseConflictingClientVersions
     *
     * @return The caseConflictingClientVersions
     */
    public List<T> getCaseConflictingClientVersions() {
        return caseConflictingClientVersions;
    }

    /**
     * Gets the unicodeConflictingClientVersions
     *
     * @return The unicodeConflictingClientVersions
     */
    public List<T> getUnicodeConflictingClientVersions() {
        return unicodeConflictingClientVersions;
    }

    /**
     * Gets the duplicateClientVersions
     *
     * @return The duplicateClientVersions
     */
    public List<T> getDuplicateClientVersions() {
        return duplicateClientVersions;
    }

    /**
     * Gets the caseConflictingServerVersions
     *
     * @return The caseConflictingServerVersions
     */
    public List<T> getCaseConflictingServerVersions() {
        return caseConflictingServerVersions;
    }

    /**
     * Gets the unicodeConflictingServerVersions
     *
     * @return The unicodeConflictingServerVersions
     */
    public List<T> getUnicodeConflictingServerVersions() {
        return unicodeConflictingServerVersions;
    }

    /**
     * Gets the duplicateServerVersions
     *
     * @return The duplicateServerVersions
     */
    public List<T> getDuplicateServerVersions() {
        return duplicateServerVersions;
    }

    /**
     * Decides which of the two conflicting versions is chosen for synchronization, while the other one is recorded in an internal list of
     * problematic server versions.
     *
     * @param version1 The first conflicting version
     * @param key1 The mapping key of the first version
     * @param version2 The second conflicting version
     * @param key2 the mapping key of the second version
     * @return The version that should be used for synchronization, i.e. either version1 or version2
     */
    public T chooseServerVersion(T version1, String key1, T version2, String key2) {
        boolean key1Normalized = PathNormalizer.isNormalized(key1);
        boolean key2Normalized = PathNormalizer.isNormalized(key2);
        if (key1Normalized && key2Normalized) {
            /*
             * both keys already in normalized form
             */
            if (key1.equals(key2)) {
                /*
                 * both keys are equal in normalized form - choose the first version
                 */
                if (null == duplicateServerVersions) {
                    duplicateServerVersions = new ArrayList<T>();
                }
                duplicateServerVersions.add(version2);
            } else {
                /*
                 * consider as case-conflict - choose the first version
                 */
                if (null == caseConflictingServerVersions) {
                    caseConflictingServerVersions = new ArrayList<T>();
                }
                caseConflictingServerVersions.add(version2);
            }
            return version1;
        } else {
            /*
             * unicode normalization conflict - prefer the normalized version, or choose the first in case both are not normalized
             */
            if (null == unicodeConflictingServerVersions) {
                unicodeConflictingServerVersions = new ArrayList<T>();
            }
            if (key2Normalized) {
                unicodeConflictingServerVersions.add(version1);
                return version2;
            } else {
                unicodeConflictingServerVersions.add(version2);
                return version1;
            }
        }
    }

    /**
     * Decides which of the two conflicting versions is chosen for synchronization, while the other one is recorded in an internal list of
     * problematic server versions.
     *
     * @param version1 The first conflicting version
     * @param key1 The mapping key of the first version
     * @param version2 The second conflicting version
     * @param key2 the mapping key of the second version
     * @return The version that should be used for synchronization, i.e. either version1 or version2
     */
    public T chooseClientVersion(T version1, String key1, T version2, String key2) {
        boolean key1Normalized = PathNormalizer.isNormalized(key1);
        boolean key2Normalized = PathNormalizer.isNormalized(key2);
        if (key1Normalized && key2Normalized) {
            /*
             * both keys already in normalized form
             */
            if (key1.equals(key2)) {
                /*
                 * both keys are equal in normalized form - choose the first version
                 */
                if (null == duplicateClientVersions) {
                    duplicateClientVersions = new ArrayList<T>();
                }
                duplicateClientVersions.add(version2);
            } else {
                /*
                 * consider as case-conflict - choose the first version
                 */
                if (null == caseConflictingClientVersions) {
                    caseConflictingClientVersions = new ArrayList<T>();
                }
                caseConflictingClientVersions.add(version2);
            }
            return version1;
        } else {
            /*
             * unicode normalization conflict - prefer the normalized version, or choose the first in case both are not normalized
             */
            if (null == unicodeConflictingClientVersions) {
                unicodeConflictingClientVersions = new ArrayList<T>();
            }
            if (key2Normalized) {
                unicodeConflictingClientVersions.add(version1);
                return version2;
            } else {
                unicodeConflictingClientVersions.add(version2);
                return version1;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        appendVersions(stringBuilder, caseConflictingClientVersions, "Case conflicting client versions");
        appendVersions(stringBuilder, unicodeConflictingClientVersions, "Unicode conflicting client versions");
        appendVersions(stringBuilder, duplicateClientVersions, "Duplicate client versions");
        appendVersions(stringBuilder, caseConflictingServerVersions, "Case conflicting server versions");
        appendVersions(stringBuilder, unicodeConflictingServerVersions, "Unicode conflicting server versions");
        appendVersions(stringBuilder, duplicateServerVersions, "Duplicate server versions");
        return stringBuilder.toString();
    }

    private void appendVersions(StringBuilder stringBuilder, List<T> versions, String header) {
        if (null != versions && 0 < versions.size()) {
            stringBuilder.append('\n').append(header).append(":\n");
            for (T version : versions) {
                stringBuilder.append("  ").append(version).append('\n');
            }
        }
    }

}
