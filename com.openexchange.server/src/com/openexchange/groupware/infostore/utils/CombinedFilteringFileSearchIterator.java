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

package com.openexchange.groupware.infostore.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorExceptionCodes;
import com.openexchange.tools.iterator.SearchIterators;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

/**
 * {@link CombinedFilteringFileSearchIterator} - Combines one or more instances of {@link SearchIterator}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CombinedFilteringFileSearchIterator implements SearchIterator<DocumentMetadata> {

    private final SearchIterator<DocumentMetadata>[] iterators;
    private final TIntSet ids;
    private DocumentMetadata next = null;
    private int i = 0;
    private OXException[] warnings;
    private Boolean hasWarnings;

    /**
     * Initializes a new {@link CombinedFilteringFileSearchIterator}
     *
     * @param iterators The instances of {@link SearchIterator}
     * @throws OXException If initialization fails
     */
    public CombinedFilteringFileSearchIterator(SearchIterator<DocumentMetadata> first, SearchIterator<DocumentMetadata> second) throws OXException {
        super();
        this.iterators = new SearchIterator[] {first, second};
        ids = new TIntHashSet(16);
        next = initNext();
    }

    /**
     * Initializes a new {@link CombinedFilteringFileSearchIterator}
     *
     * @param iterators The instances of {@link SearchIterator}
     * @throws OXException If initialization fails
     */
    public CombinedFilteringFileSearchIterator(final SearchIterator<DocumentMetadata>... iterators) throws OXException {
        super();
        this.iterators = iterators;
        ids = new TIntHashSet(16);
        next = initNext();
    }

    @Override
    public boolean hasNext() throws OXException{
        return null != next;
    }

    @Override
    public DocumentMetadata next() throws OXException {
        DocumentMetadata retval = next;
        if (null == retval) {
            throw SearchIteratorExceptionCodes.NO_SUCH_ELEMENT.create().setPrefix("NON");
        }

        next = initNext();
        return retval;
    }

    private DocumentMetadata initNext() throws OXException {
        if (iterators.length == 0) {
            return null;
        }

        DocumentMetadata next = null;
        while (i < iterators.length && null == next) {
            SearchIterator<DocumentMetadata> iter = iterators[i];
            if (iter.hasNext()) {
                DocumentMetadata metadata = iter.next();
                if (null == metadata || !ids.add(metadata.getId())) {
                    continue;
                }
                next = metadata;
            } else {
                i++;
            }
        }

        return next;
    }

    @Override
    public void close() {
        for (SearchIterator<DocumentMetadata> iter : iterators) {
            SearchIterators.close(iter);
        }
    }

    @Override
    public int size() {
        return -1;
    }

    @Override
    public void addWarning(OXException warning) {
        throw new UnsupportedOperationException("Mehtod addWarning() not implemented");
    }

    @Override
    public OXException[] getWarnings() {
        if (null == warnings) {
            if (iterators.length == 0) {
                warnings = new OXException[0];
            } else {
                List<OXException> list = new ArrayList<OXException>(iterators.length << 1);
                for (SearchIterator<?> iter : iterators) {
                    if (iter.hasWarnings()) {
                        list.addAll(Arrays.asList(iterators[i].getWarnings()));
                    }
                }
                warnings = list.toArray(new OXException[list.size()]);
            }
        }
        return warnings.length == 0 ? null : warnings;
    }

    @Override
    public boolean hasWarnings() {
        if (null == hasWarnings) {
            if (iterators.length == 0) {
                hasWarnings = Boolean.FALSE;
            } else {
                hasWarnings = Boolean.FALSE;
                for (SearchIterator<?> iter : iterators) {
                    if (iter.hasWarnings()) {
                        hasWarnings = Boolean.TRUE;
                        break;
                    }
                }
            }
        }
        return hasWarnings.booleanValue();
    }
}
