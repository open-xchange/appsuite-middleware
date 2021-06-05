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

package com.openexchange.file.storage.infostore;

import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;


/**
 * {@link InfostoreSearchIterator}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class InfostoreSearchIterator implements SearchIterator<File> {

    private final SearchIterator<DocumentMetadata> delegate;

    /**
     * Initializes a new {@link InfostoreSearchIterator}.
     *
     * @param delegate The delegate iterator
     */
    public InfostoreSearchIterator(SearchIterator<DocumentMetadata> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void addWarning(OXException warning) {
        delegate.addWarning(warning);
    }

    @Override
    public void close() {
        SearchIterators.close(delegate);
    }

    @Override
    public OXException[] getWarnings() {
        return delegate.getWarnings();
    }

    @Override
    public boolean hasNext() throws OXException {
        return delegate.hasNext();
    }

    @Override
    public boolean hasWarnings() {
        return delegate.hasWarnings();
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public File next() throws OXException {
        DocumentMetadata next = delegate.next();
        if (next == null) {
            return null;
        }
        return new InfostoreFile(next);
    }

}
