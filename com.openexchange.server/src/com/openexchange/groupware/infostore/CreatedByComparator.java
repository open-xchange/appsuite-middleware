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

package com.openexchange.groupware.infostore;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.java.Collators;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorDelegator;

/**
 * {@link CreatedByComparator} - Comparator for "created-by" field.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CreatedByComparator implements Comparator<DocumentMetadata> {

    private final TIntObjectMap<String> cache;

    private final Collator collator;

    private final Context context;

    private boolean desc;

    /**
     * Initializes a new {@link CreatedByComparator}.
     *
     * @param locale The session user's locale
     * @param context The associated context
     */
    public CreatedByComparator(final Locale locale, final Context context) {
        super();
        cache = new TIntObjectHashMap<String>(24);
        collator = Collators.getSecondaryInstance(locale);
        this.context = context;
    }

    /**
     * Sets whether descending sorting shall be performed.
     *
     * @param desc <code>true</code> for descending order; otherwise <code>false</code> to ascending
     * @return This comparator with new behavior applied
     */
    public CreatedByComparator setDescending(final boolean desc) {
        this.desc = desc;
        return this;
    }

    @Override
    public int compare(final DocumentMetadata o1, final DocumentMetadata o2) {
        /*
         * Sort by owner's display name
         */
        final int owner1 = o1.getCreatedBy();
        final int owner2 = o2.getCreatedBy();
        final int result;
        if (owner1 > 0 && owner2 > 0) {
            result = collator.compare(getDisplayName(owner1), getDisplayName(owner2));
        } else {
            result = (owner1 < owner2 ? -1 : (owner1 == owner2 ? 0 : 1));
        }
        if (desc) {
            return result > 0 ? -1 : (result == 0 ? 0 : 1);
        }
        return result;
    }

    private String getDisplayName(final int userId) {
        String displayName = cache.get(userId);
        if (null == displayName) {
            try {
                displayName = UserStorage.getInstance().getUser(userId, context).getDisplayName();
            } catch (OXException e) {
                return null;
            }
            cache.put(userId, displayName);
        }
        return displayName;
    }

    /**
     * Re-Sorts specified iterator according to given comparator
     *
     * @param iter The iterator
     * @param comparator The comparator
     * @return The re-sorted view of given {@link SearchIterator}
     * @throws OXException If iterator access fail
     */
    public static SearchIterator<DocumentMetadata> resort(final SearchIterator<DocumentMetadata> iter, final CreatedByComparator comparator) throws OXException {
        if (null == iter) {
            return iter;
        }
        boolean close = true;
        try {
            final List<DocumentMetadata> files = new LinkedList<DocumentMetadata>();
            while (iter.hasNext()) {
                files.add(iter.next());
            }
            iter.close();
            close = false;
            // Re-sort
            Collections.sort(files, comparator);
            return new SearchIteratorDelegator<DocumentMetadata>(files);
        } finally {
            if (close) {
                iter.close();
            }
        }
    }

}
