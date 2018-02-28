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

package com.openexchange.jslob.config;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * {@link ConfigTreeEquivalent} - A bi-directional map for config tree to JSlob mappings and vice versa.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ConfigTreeEquivalent {

    /** The config tree to JSlob mappings */
    public final ConcurrentMap<String, String> config2lob;

    /** The JSlob to config tree mappings */
    public final ConcurrentMap<String, String> lob2config;

    /**
     * Initializes a new {@link ConfigTreeEquivalent}.
     */
    public ConfigTreeEquivalent() {
        super();
        config2lob = new ConcurrentHashMap<String, String>(32, 0.9f, 1);
        lob2config = new ConcurrentHashMap<String, String>(32, 0.9f, 1);
    }

    /**
     * Merges specified config tree equivalent into this one.
     *
     * @param other The other one to merge
     */
    public void mergeWith(ConfigTreeEquivalent other) {
        if (null == other) {
            return;
        }

        ConcurrentMap<String, String> thisConfig2lob = this.config2lob;
        for (Map.Entry<String, String> e : other.config2lob.entrySet()) {
            thisConfig2lob.putIfAbsent(e.getKey(), e.getValue());
        }

        ConcurrentMap<String, String> thisLob2config = this.lob2config;
        for (Map.Entry<String, String> e : other.lob2config.entrySet()) {
            thisLob2config.putIfAbsent(e.getKey(), e.getValue());
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(config2lob.size() << 4);

        Set<Map.Entry<String, String>> entrySet = new TreeSet<>(new Comparator<Map.Entry<String, String>>() {

            @Override
            public int compare(Map.Entry<String, String> entry1, Map.Entry<String, String> entry2) {
                return entry1.getKey().compareToIgnoreCase(entry2.getKey());
            }
        });
        entrySet.addAll(config2lob.entrySet());

        boolean first = true;
        for (Map.Entry<String, String> e : entrySet) {
            if (first) {
                first = false;
            } else {
                sb.append('\n');
            }
            sb.append(e.getKey()).append(" > ").append(e.getValue());
        }
        return sb.toString();
    }

}
