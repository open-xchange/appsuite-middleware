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

package com.openexchange.file.storage;

import java.util.Collection;
import com.openexchange.groupware.results.AbstractTimedResult;
import com.openexchange.tools.iterator.ArrayIterator;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;

/**
 * {@link FileTimedResult}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FileTimedResult extends AbstractTimedResult<File> {

    /**
     * Initializes a new {@link FileTimedResult} from given collection.
     *
     * @param collection The collection
     */
    public FileTimedResult(final Collection<File> collection) {
        super(new SearchIteratorAdapter<File>(collection.iterator(), collection.size()));
    }

    /**
     * Initializes a new {@link FileTimedResult} from given array.
     *
     * @param array The array
     */
    public FileTimedResult(final File[] array) {
        super(new ArrayIterator<File>(array));
    }

    /**
     * Initializes a new {@link FileTimedResult} from given search iterator.
     *
     * @param iter The search iterator
     */
    public FileTimedResult(final SearchIterator<File> iter) {
        super(iter);
    }

    @Override
    protected long extractTimestamp(final File object) {
        return object.getSequenceNumber();
    }

}
