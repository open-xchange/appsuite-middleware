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

import com.openexchange.groupware.results.AbstractTimedResult;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * An infostore timed result that knows how to extract timestamps from document metadatas.
 * {@link InfostoreTimedResult}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class InfostoreTimedResult extends AbstractTimedResult<DocumentMetadata> {

    public InfostoreTimedResult(SearchIterator<DocumentMetadata> results) {
        super(results);
    }

    @Override
    protected long extractTimestamp(DocumentMetadata object) {
        return object.getSequenceNumber();
    }

}
