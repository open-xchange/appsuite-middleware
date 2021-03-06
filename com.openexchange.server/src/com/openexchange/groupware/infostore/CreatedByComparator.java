/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.groupware.infostore;

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
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

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
