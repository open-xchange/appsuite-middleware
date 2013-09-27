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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.imap.cache.util;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import com.javacodegeeks.concurrent.ConcurrentLinkedHashMap;
import com.openexchange.mail.dataobjects.MailFolder;

/**
 * {@link FolderMap} - A folder map.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderMap {

    private final ConcurrentMap<String, MailFolder> map;

    /**
     * Initializes a new {@link FolderMap}.
     */
    public FolderMap(final int maximumCapacity) {
        super();
        map = new ConcurrentLinkedHashMap<String, MailFolder>(maximumCapacity, 0.75F, 4, maximumCapacity, new ExpirationPolicy(0, 300000));
    }

    public MailFolder putIfAbsent(final String key, final MailFolder value) {
        return map.putIfAbsent(key, value);
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public boolean containsKey(final String fullName) {
        return map.containsKey(fullName);
    }

    public MailFolder get(final String fullName) {
        return map.get(fullName);
    }

    public MailFolder put(final String key, final MailFolder value) {
        return map.put(key, value);
    }

    public MailFolder remove(final String fullName) {
        return map.remove(fullName);
    }

    public void clear() {
        map.clear();
    }

    public Set<String> keySet() {
        return map.keySet();
    }

    public Collection<MailFolder> values() {
        return map.values();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((map == null) ? 0 : map.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof FolderMap)) {
            return false;
        }
        final FolderMap other = (FolderMap) obj;
        if (map == null) {
            if (other.map != null) {
                return false;
            }
        } else if (!map.equals(other.map)) {
            return false;
        }
        return true;
    }

}
