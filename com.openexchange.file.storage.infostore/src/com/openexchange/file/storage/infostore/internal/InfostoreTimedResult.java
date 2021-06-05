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

package com.openexchange.file.storage.infostore.internal;

import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.infostore.InfostoreSearchIterator;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.tools.iterator.SearchIterator;


/**
 * {@link InfostoreTimedResult}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class InfostoreTimedResult implements TimedResult<File> {

    private final TimedResult<DocumentMetadata> documents;

    public InfostoreTimedResult(TimedResult<DocumentMetadata> documents) {
        this.documents = documents;
    }

    @Override
    public SearchIterator<File> results() throws OXException {
        SearchIterator<DocumentMetadata> results = documents.results();
        if (results == null) {
            return null;
        }
        return new InfostoreSearchIterator(results);
    }

    @Override
    public long sequenceNumber() throws OXException {
        return documents.sequenceNumber();
    }

}
