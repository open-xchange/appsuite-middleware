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

package com.openexchange.file.storage.json;

import java.util.Date;
import java.util.List;
import org.json.JSONArray;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.json.actions.files.AJAXInfostoreRequest;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link FileConverter}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class FileConverter implements ResultConverter {

    private final FileMetadataWriter writer;

    /**
     * Initializes a new {@link FileConverter}.
     *
     * @param fieldCollector The collector for additional file fields
     */
    public FileConverter(FileFieldCollector fieldCollector) {
        super();
        writer = new FileMetadataWriter(fieldCollector);
    }

    @Override
    public void convert(final AJAXRequestData requestData, final AJAXRequestResult result, final ServerSession session, final Converter converter) throws OXException {
        AJAXInfostoreRequest infostoreRequest = new AJAXInfostoreRequest(requestData, session);
        Object resultObject = result.getResultObject();
        if (File.class.isInstance(resultObject)) {
            /*
             * write single file result
             */
            resultObject = writer.write(infostoreRequest, (File) resultObject);
        } else if (Delta.class.isInstance(resultObject)) {
            /*
             * write delta result
             */
            SearchIterator<File> newAndModifiedIterator = null;
            SearchIterator<File> deletedIterator = null;
            try {
                Delta<File> deltaResult = (Delta<File>) resultObject;
                newAndModifiedIterator = deltaResult.results();
                JSONArray jsonArray = writer.write(infostoreRequest, newAndModifiedIterator);
                deletedIterator = deltaResult.getDeleted();
                while (deletedIterator.hasNext()) {
                    jsonArray.put(deletedIterator.next().getId());
                }
                resultObject = jsonArray;
            } finally {
                SearchIterators.close(newAndModifiedIterator);
                SearchIterators.close(deletedIterator);
            }
        } else if (TimedResult.class.isInstance(resultObject)) {
            /*
             * write timed files result
             */
            SearchIterator<File> searchIterator = null;
            try {
                TimedResult<File> timedResult = (TimedResult<File>) resultObject;
                result.setTimestamp(new Date(timedResult.sequenceNumber()));
                searchIterator = timedResult.results();
                resultObject = writer.write(infostoreRequest, searchIterator);
            } finally {
                SearchIterators.close(searchIterator);
            }
        } else if (SearchIterator.class.isInstance(resultObject)) {
            /*
             * write search iterator result
             */
            SearchIterator<File> searchIterator = null;
            try {
                searchIterator = (SearchIterator<File>) resultObject;
                resultObject = writer.write(infostoreRequest, searchIterator);
            } finally {
                SearchIterators.close(searchIterator);
            }
        } else if (List.class.isInstance(resultObject)) {
            @SuppressWarnings("unchecked") List<File> list = (List<File>) resultObject;
            resultObject = writer.write(infostoreRequest, list);
        } else {
            throw new UnsupportedOperationException("unknown result object");
        }
        result.setResultObject(resultObject);
    }

    @Override
    public String getInputFormat() {
        return "infostore";
    }

    @Override
    public String getOutputFormat() {
        return "json";
    }

    @Override
    public Quality getQuality() {
        return Quality.GOOD;
    }

}
